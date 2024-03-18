package com.dreamgames.backendengineeringcasestudy.tournamentservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ExceptionCollector;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentEntryRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TournamentService {
    
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired UserRepository userRepository;
    @Autowired
    private TournamentGroupRepository tournamentGroupRepository;
    @Autowired
    private TournamentEntryRepository tournamentEntryRepository;
    // Inject other necessary repositories or services, e.g., UserRepository
   
    public TournamentService(TournamentRepository tournamentRepository, UserRepository userRepository,
        TournamentGroupRepository tournamentGroupRepository, TournamentEntryRepository tournamentEntryRepository) {
        this.tournamentRepository = tournamentRepository;
        this.userRepository = userRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.tournamentEntryRepository = tournamentEntryRepository;
    }

    public Tournament createTournament(LocalDateTime startTime, LocalDateTime endTime) {
        Tournament tournament = new Tournament(startTime, endTime);
        tournamentRepository.save(tournament);
        return tournament;
    }

	public void integrationTestMethod() {
        return;
    }

    @Cacheable("currentTournament")
    public Tournament getCurrentTournament() {
        LocalDateTime now = LocalDateTime.now();
        return tournamentRepository.findTournamentByStartTimeBeforeAndEndTimeAfter(now, now)
            .orElseThrow(() -> new RuntimeException("No current tournament found"));
    }
  
    @Transactional
    public TournamentGroup enterTournament(Long userId) throws Exception { // TODO: This should return group leader board
        Tournament currentTournament = getCurrentTournament();
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException()); 

        if (tournamentEntryRepository.existsByUserIdAndTournamentId(userId, currentTournament.getId())) {
            throw new Exception("User already entered in the current tournament");
        } 

        int level = user.getLevel();
        if (level < 20) {
            throw new Exception("Level not high enough");
        }
        TournamentGroup group = currentTournament.addUser(user);          
        tournamentGroupRepository.save(group);    
        return group;
    }

    public List<User> getGroupLeaderboard(Long groupId) {
        TournamentGroup group = tournamentGroupRepository.findById(groupId).orElseThrow(() -> new EntityNotFoundException());
        return group.getGroupLeaderboard();
    }
    
    public TournamentEntry updateScore(Long userId, int scoreIncrement) { // maybe i dont need this method
        // Find the TournamentEntry for the user and increment the score
        return null;
    }
    
    public void claimRewards(Long userId) {
        // Allow users to claim rewards based on their rankings
    }
    
    // Additional methods for handling tournament logic
}
