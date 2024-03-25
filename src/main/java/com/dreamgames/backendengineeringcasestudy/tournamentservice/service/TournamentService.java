
package com.dreamgames.backendengineeringcasestudy.tournamentservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.concurrent.CompletableFuture;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.dreamgames.backendengineeringcasestudy.exceptions.AlreadyInCurrentTournamentException;
import com.dreamgames.backendengineeringcasestudy.exceptions.HasUnclaimedRewards;
import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.exceptions.NoSuchTournamentException;
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
import com.dreamgames.backendengineeringcasestudy.userservice.model.User.Country;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;

import io.netty.util.concurrent.CompleteFuture;
import jakarta.persistence.EntityNotFoundException;

/**
 * The TournamentService class provides methods for managing tournaments and tournament entries.
 * It allows creating tournaments, entering tournaments, incrementing entry scores, retrieving leaderboards,
 * and claiming rewards.
 * 
 * This class is responsible for interacting with the TournamentRepository, TournamentGroupRepository,
 * TournamentEntryRepository, and UserRepository to perform the necessary operations.
 * 
 * @author E. Omer Gul
 */
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
    
    /**
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public Tournament createTournament(LocalDateTime startTime, LocalDateTime endTime) {
        Tournament tournament = new Tournament(startTime, endTime);
        tournamentRepository.save(tournament);
        return tournament;
    }

	public void integrationTestMethod() {
        return;
    }


    /**
     * Gets the current tournament   
     * @return
     * @throws NoSuchTournamentException
     */     
    @Cacheable("currentTournament")
    @Async
    public CompletableFuture<Tournament> getCurrentTournament() throws NoSuchTournamentException {
        LocalDateTime now = LocalDateTime.now();
        return CompletableFuture.completedFuture(tournamentRepository.findTournamentByStartTimeBeforeAndEndTimeAfter(now, now)
            .orElseThrow(() -> new NoSuchTournamentException("No current tournament found")));
    }

    @Cacheable("currentTournament")
    @Async
    public CompletableFuture<Long> getCurrentTournamentId() throws NoSuchTournamentException, Exception {
        return CompletableFuture.completedFuture(getCurrentTournament().get().getId());
    }
    /**
     * Checks if the tournament has ended
     * @param tournamentId
     * @return true if the tournament has ended, false if is still on going
     */
    @Async
    public CompletableFuture<Boolean> hasEnded(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new EntityNotFoundException("No such tournament"));
        return CompletableFuture.completedFuture(tournament.hasEnded());
    }

    /**
     * Increments the entries score and returns the new score and the groupId of the entry
     * @param userId
     * @param tournamentId
     * @throws TournamentGroupHasNotBegunException
     */
    @Async
    public CompletableFuture<Pair<Integer, Long>> incrementEntryScore(Long userId, Long tournamentId) throws TournamentGroupHasNotBegunException {
        TournamentEntry entry = tournamentEntryRepository.findByUserIdAndTournamentId(userId, tournamentId)
        .orElseThrow(() -> new EntityNotFoundException("Entry not found for user " + userId + " in tournament " + tournamentId));
        if (!entry.groupHasBegun()) {
            throw new TournamentGroupHasNotBegunException("Will not update users tournament group score since the group has not begun (less that 5 players)");
        }
        Integer score = entry.incrementScore();
        Long id = entry.getGroupId();
        tournamentEntryRepository.save(entry);
        return CompletableFuture.completedFuture(Pair.with(score, id));
    }
  

    /**
     * Allows a user to enter the current tournament and returns the current group leaderboard
     * @param user
     * @return
     * @throws Exception
     */
    public GroupLeaderBoard enterTournament(User user) throws Exception { // TODO maybe can refactor to make look nicer 
        Tournament currentTournament = getCurrentTournament().get() ;
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

        GroupLeaderBoard groupLeaderBoard = getGroupLeaderboard(group.getId()).get(); 
        return groupLeaderBoard;
    }

    /**
     * Queries the data base and gets the leader board for the given group
     * @param groupId
     * @return
     */
    public CompletableFuture<GroupLeaderBoard> getGroupLeaderboard(Long groupId) {
        List<TournamentEntry> sortedEntries = tournamentEntryRepository.findByTournamentGroupIdOrderByScoreDesc(groupId);
        List<Pair<User,Integer>> pairs = new ArrayList<>();
        for (TournamentEntry entry : sortedEntries) {
            Pair<User,Integer> pair = new Pair<User,Integer>(entry.getUser(), entry.getScore());
            pairs.add(pair);
        }
        return CompletableFuture.completedFuture(new GroupLeaderBoard(pairs, groupId));
    }

    /**
     * Queries the database and gets the country leaderboard for the given tournament
     * @param tournamentId
     * @return
     * @throws NoSuchTournamentException
     */
    public CompletableFuture<List<Pair<User.Country,Integer>>> getCountryLeaderboard(Long tournamentId) throws NoSuchTournamentException , Exception {
        List<Pair<User.Country,Integer>> countryLeaderBoard = new ArrayList<>();
        for (User.Country country : User.Country.values()) {
            List<TournamentEntry> sortedEntries = tournamentEntryRepository.findByCountryAndTournamentIdOrderedByScoreDesc(country, getCurrentTournamentId().get()); // TODO can make a new query that doesn't sort for performance purposes
            Integer totalScore = sortedEntries.stream().mapToInt(TournamentEntry::getScore).sum();
            Pair<User.Country,Integer> pair = new Pair<User.Country,Integer>(country, totalScore);
            countryLeaderBoard.add(pair);
        }
        countryLeaderBoard.sort((pair1, pair2) -> pair2.getValue1().compareTo(pair1.getValue1()));
        return CompletableFuture.completedFuture(countryLeaderBoard);
    }

    /**
     * Queries the the database to find the rank of the player for the given tournament;
     * @param userId
     * @param tournamentId
     * @return
     * @throws EntityNotFoundException
     * @throws TournamentGroupHasNotBegunException
     */
    public CompletableFuture<Integer> getGroupRank(Long userId, Long tournamentId) throws EntityNotFoundException, TournamentGroupHasNotBegunException {
        TournamentEntry usersEntry = tournamentEntryRepository.findByUserIdAndTournamentId(userId, tournamentId)
            .orElseThrow(() -> new EntityNotFoundException("Entry not found for user " + userId + " in tournament " + tournamentId));
        if (!usersEntry.groupHasBegun()) {
            throw new TournamentGroupHasNotBegunException("Group has not begun");
        }
        Long groupId = usersEntry.getTournamentGroup().getId();
        List<TournamentEntry> sortedEntries = tournamentEntryRepository.findByTournamentGroupIdOrderByScoreDesc(groupId);
        int rank = IntStream.range(0, sortedEntries.size())
                .filter(i -> sortedEntries.get(i).getId().equals(usersEntry.getId()))
                .findFirst()
                .orElse(-1) + 1; // Adjusting for zero-based index
        if (rank == 0) {
            throw new EntityNotFoundException("Entry not found in sorted list for user " + userId);
        }
        return CompletableFuture.completedFuture(rank);
    }
   
    /**
     * Claims a reward for a user. Sets the users reward claimed field to true (persistent)
     * @param userId
     */
    @Async
    public void claimReward(Long userId) {
        TournamentEntry entry = tournamentEntryRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException());
        entry.setRewardClaimed(true);
        tournamentEntryRepository.save(entry);
    }


    // =========== DEV METHODS ======== // 

    @Async 
    public void endTournament(Long tournamentId) throws NoSuchTournamentException {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new NoSuchTournamentException("can't end tournament that don't exist"));  
        tournament.endTournament();
        tournamentRepository.save(tournament);
    }
}
    
