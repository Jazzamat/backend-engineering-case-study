package com.dreamgames.backendengineeringcasestudy.tournamentservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.dreamgames.backendengineeringcasestudy.exceptions.AlreadyInCurrentTournamentException;
import com.dreamgames.backendengineeringcasestudy.exceptions.HasUnclaimedRewards;
import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.exceptions.NotEnoughFundsException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentGroupHasNotBegunException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentEntryRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

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

    public boolean hasEnded(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new EntityNotFoundException("No such tournament"));
        return tournament.hasEnded();
    }

    public void incrementEntryScore(Long userId, Long tournamentId) throws TournamentGroupHasNotBegunException {
        TournamentEntry entry = tournamentEntryRepository.findByUserIdAndTournamentId(userId, tournamentId)
        .orElseThrow(() -> new EntityNotFoundException("Entry not found for user " + userId + " in tournament " + tournamentId));
        if (!entry.groupHasBegun()) {
            throw new TournamentGroupHasNotBegunException("Will not update users tournament group score since the group has not begun (less that 5 players)");
        }
        entry.incrementScore();
        tournamentEntryRepository.save(entry);
    }
  
    public GroupLeaderBoard enterTournament(User user) throws Exception { // TODO maybe can refactor to make look nicer 
        Tournament currentTournament = getCurrentTournament();
        if (tournamentEntryRepository.existsByUserIdAndTournamentId(user.getId(), currentTournament.getId())) {
            throw new AlreadyInCurrentTournamentException("User " + user.getUsername() + " already entered in the current tournament");
        }
        if (tournamentEntryRepository.existsByUserIdAndUnclaimedReward(user.getId())) {
            throw new HasUnclaimedRewards("User can't enter a new tournament without claiming their previous reward");
        }
        int level = user.getLevel();
        if (level < 20) {
            throw new LevelNotHighEnoughException("User " + user.getUsername()+ " not high enough level to enter the tournament");
        }   
        if (user.getCoins() < 1000) {
            throw new NotEnoughFundsException("User " + user.getUsername() + " does not have enough coins to enter the tournament");
        }

        user.deductFee();
        TournamentGroup group = currentTournament.addUser(user);          
        tournamentGroupRepository.save(group);    

        GroupLeaderBoard groupLeaderBoard = getGroupLeaderboard(group.getId()); 
        return groupLeaderBoard;
    }

    public GroupLeaderBoard getGroupLeaderboard(Long groupId) {
        List<TournamentEntry> sortedEntries = tournamentEntryRepository.findByTournamentGroupIdOrderByScoreDesc(groupId);
        List<Pair<User,Integer>> pairs = new ArrayList<>();
        for (TournamentEntry entry : sortedEntries) {
            Pair<User,Integer> pair = new Pair<User,Integer>(entry.getUser(), entry.getScore());
            pairs.add(pair);
        }
        return new GroupLeaderBoard(pairs, groupId);
    }

    public List<Pair<User.Country,Integer>> getCountryLeaderboard(Long tournamentId) {
        List<Pair<User.Country,Integer>> countryLeaderBoard = new ArrayList<>();
        for (User.Country country : User.Country.values()) {
            List<TournamentEntry> sortedEntries = tournamentEntryRepository.findByCountryAndTournamentIdOrderedByScoreDesc(country, getCurrentTournamentId()); // TODO can make a new query that doesn't sort for performance purposes
            Integer totalScore = sortedEntries.stream().mapToInt(TournamentEntry::getScore).sum();
            Pair<User.Country,Integer> pair = new Pair<User.Country,Integer>(country, totalScore);
            countryLeaderBoard.add(pair);
        }
        countryLeaderBoard.sort((pair1, pair2) -> pair2.getValue1().compareTo(pair1.getValue1()));
        return countryLeaderBoard;
    }

    public int getGroupRank(Long userId, Long tournamentId) throws EntityNotFoundException, TournamentGroupHasNotBegunException {
        TournamentEntry usersEntry = tournamentEntryRepository.findByUserIdAndTournamentId(userId, tournamentId)
            .orElseThrow(() -> new EntityNotFoundException("Entry not found for user " + userId + " in tournament " + tournamentId));
    
        if (!usersEntry.groupHasBegun()) {
            throw new TournamentGroupHasNotBegunException("Group has not begun");
        }
    
        Long groupId = usersEntry.getTournamentGroup().getId();
        List<TournamentEntry> sortedEntries = tournamentEntryRepository.findByTournamentGroupIdOrderByScoreDesc(groupId);
    
        // Using streams to find the index based on ID comparison
        int rank = IntStream.range(0, sortedEntries.size())
                .filter(i -> sortedEntries.get(i).getId().equals(usersEntry.getId()))
                .findFirst()
                .orElse(-1) + 1; // Adjusting for zero-based index
    
        if (rank == 0) {
            throw new EntityNotFoundException("Entry not found in sorted list for user " + userId);
        }
    
        return rank;
    }
    
    public void claimReward(Long userId) {
        TournamentEntry entry = tournamentEntryRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException());
        entry.setRewardClaimed(true);
        tournamentEntryRepository.save(entry);
    }
}
    
