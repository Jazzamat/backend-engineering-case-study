package com.dreamgames.backendengineeringcasestudy.tournamentservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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

    @Autowired
    private TournamentGroupRepository tournamentGroupRepository;

    @Autowired
    private TournamentEntryRepository tournamentEntryRepository;
   
    public TournamentService(TournamentRepository tournamentRepository, UserRepository userRepository,
        TournamentGroupRepository tournamentGroupRepository, TournamentEntryRepository tournamentEntryRepository) {
        this.tournamentRepository = tournamentRepository;
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

    @Cacheable("currentTournament")
    public Long getCurrentTournamentId() {
        return getCurrentTournament().getId();
    }

    public void incrementEntryScore(Long userId, Long tournamentId) {
        TournamentEntry entry = tournamentEntryRepository.findByUserIdAndTournamentId(userId, tournamentId)
        .orElseThrow(() -> new EntityNotFoundException("Entry not found for user " + userId + " in tournament " + tournamentId));
        entry.incrementScore();
        tournamentEntryRepository.save(entry);
    }
  
    public TournamentGroup enterTournament(User user) throws Exception { // TODO: This should return group leader board
        Tournament currentTournament = getCurrentTournament();
        if (tournamentEntryRepository.existsByUserIdAndTournamentId(user.getId(), currentTournament.getId())) {
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
        List<TournamentEntry> sortedEntries = tournamentEntryRepository.findByTournamentGroupIdOrderByScoreDesc(groupId);
        List<User> users = new ArrayList<>();
        for (TournamentEntry entry : sortedEntries) {
            users.add(entry.getUser());
        }
        return users;
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
