
package com.dreamgames.backendengineeringcasestudy.backendservice;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ExceptionCollector;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.HashMap;

import com.dreamgames.backendengineeringcasestudy.exceptions.NoSuchTournamentException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentGroupHasNotBegunException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentHasNotEndedException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentScheduler;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * This class represents the backend service for the DreamGames application.
 * It provides various methods for user management, tournament participation, leaderboard retrieval, and real-time updates.
 * The service interacts with the UserService, TournamentService, and RedisTemplate to perform its operations.
 * 
 * @author E.Omer Gul
 * 
 */
@Service
@EnableAsync
public class BackendService {
    
    private UserService userService;
    private TournamentService tournamentService;
    private TournamentScheduler tournamentScheduler;

    private final Map<Object,List<SseEmitter>> emitters = new HashMap<>();

    @Autowired
    private final RedisTemplate<String,Object> realtimeleaderboard; //TODO maybe I need to make this a list of templates. or maybe have one for counrty one for group


    public BackendService(UserService userService, TournamentService tournamentService, RedisTemplate<String,Object> realtimeleaderboard, TournamentScheduler tournamentScheduler) {
        this.userService = userService;
        this.tournamentService = tournamentService;
        this.realtimeleaderboard = realtimeleaderboard;
        this.tournamentScheduler = tournamentScheduler;
    }

    /**
     * Creates a new user, returning a user object that contains, its id, level, coins and country
     * @param username
     * @return
     */
    @Async
    public CompletableFuture<User> createUser(String username) {
        return CompletableFuture.supplyAsync(() -> userService.createUser(username));
    }

    /**
     * Creates a new user, returning a user object that contains, its id, level, coins and country
     * @param userId
     * @param cointsToAdd
     * @return
     */
    public User updateUserLevelAndCoins(Long userId, int cointsToAdd) {
            try {
                Pair<Integer,Long> scoreAndgroupId = tournamentService.incrementEntryScore(userId, tournamentService.getCurrentTournamentId());
                System.out.println("UPDATING USER SCORE TO :" + scoreAndgroupId.getValue0()); // TODO delete debug statemetn
                updateRedisGroupLeaderboard(scoreAndgroupId.getValue1(),userId, scoreAndgroupId.getValue0());
                updateRedisCountryLeaderBoard(userService.retreiveUsersCountry(userId));
            } catch (TournamentGroupHasNotBegunException e) { 
               System.err.println("Tournament group has not begun");
                // do nothin 
            } catch (NoSuchTournamentException e) {
                System.err.println("No such tournament");
                // do nothin 
            } catch (EntityNotFoundException e) {
                // user has no entry to update
                System.err.println("Entity not found");
            } catch (Exception e) {
                System.err.println("##### EXCEPTION: " + e.getMessage());
            }
            return userService.updateUserLevelAndCoins(userId, cointsToAdd);
    }


    /**
     * Allows a user to join the current tournament and returns the current group leaderboard. For realtime updates of the leaderboard it calls the redis database
     * @param userId
     * @return
     * @throws Exception
     */
    public GroupLeaderBoard enterTournament(Long userId) throws Exception { // TODO: Test that users can't enter a new tournament without claiming previous rewards
        User user = userService.getUser(userId);
        GroupLeaderBoard groupLeaderBoard = tournamentService.enterTournament(user);
        userService.saveUser(user);
        Integer initialScore = 0;
        Long groupId = groupLeaderBoard.getGroupId();
        updateRedisGroupLeaderboard(groupId, userId, initialScore);
        return groupLeaderBoard;
    } 


    /**
     *  Allows users to claim tournament rewards and returns updated progress data.
     * @param userId
     * @return
     */
    public User claimReward(Long userId, Long tournamentId) throws TournamentHasNotEndedException, TournamentGroupHasNotBegunException, Exception {
        if (!tournamentService.hasEnded(tournamentId)) {
            throw new TournamentHasNotEndedException("Tournament is still going");
        }
        int rank = tournamentService.getGroupRank(userId, tournamentId).get();
        tournamentService.claimReward(userId);
        return userService.claimReward(userId,rank);
    }


    /**
     * Retrieves the player's rank for any tournament 
     * @param userId
     * @param tournamentId
     * @return
     */ 
    public int getGroupRank(Long userId, Long tournamentId) throws EntityNotFoundException, TournamentGroupHasNotBegunException, Exception {
        return tournamentService.getGroupRank(userId, tournamentId).get();
    }


    /**
     * Gets the leaderboard data of a tournament group. The data contains as user object (id, username, country) as well as the users tournament score
     * If it is the first call the mysql database is queried and a copy is created for the redis database
     * After that updates are live on the redis database so there is no need query the datbase every time.  
     * @param groupId
     * @return
     */
    public GroupLeaderBoard getGroupLeaderboard(Long groupId) throws Exception { // TODO test some more now that we have score in there too
        return Redis.redisGetGroupLeaderboard(groupId, realtimeleaderboard, tournamentService, userService);
    } 


    /**
     * Gets the leaderboard data of the countries for a tournament. The data includes country name and its total tournament score
     * @param country
     * @param tournamentId
     * @return
     */
    public List<Pair<User.Country,Integer>> getCountryLeaderboard(Long tournamentId) throws NoSuchTournamentException, Exception { // TODO test some more now that you have changed it arround 
        return Redis.redisGetCountryLeaderboard(realtimeleaderboard, tournamentService, tournamentId);
    }


    // ============== ASYNC WRAPPERS ================= //

    @Async
    public CompletableFuture<User> updateUserLevelAndCoinsAsyncWrapper(Long userId, int cointsToAdd) {
        return CompletableFuture.supplyAsync(() -> updateUserLevelAndCoins(userId, cointsToAdd));
    }

    @Async
    public CompletableFuture<GroupLeaderBoard> enterTournamentAsyncWrapper(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return enterTournament(userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Async
    public CompletableFuture<User> claimRewardAsyncWrapper(Long userId, Long tournamentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return claimReward(userId, tournamentId);
            } catch (TournamentHasNotEndedException | TournamentGroupHasNotBegunException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("An unexpected error occurred while claiming rewards", e);
            }
        });
    }

    @Async
    public CompletableFuture<Integer> getGroupRankAsyncWrapper(Long userId, Long tournamentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getGroupRank(userId, tournamentId);
            } catch (EntityNotFoundException | TournamentGroupHasNotBegunException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while retrieving group rank", e);
            }
        });
    }

    @Async 
    public CompletableFuture<GroupLeaderBoard> getGroupLeaderboardAsyncWrapper(Long groupId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getGroupLeaderboard(groupId);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while retrieving group leaderboard", e);
            }
        });
    }

    @Async
    public CompletableFuture<List<Pair<User.Country, Integer>>> getCountryLeaderboardAsyncWrapper(Long tournamentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getCountryLeaderboard(tournamentId);
            } catch (NoSuchTournamentException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while retrieving country leaderboard", e);
            }
        });
    }

    // =========== REDIS HELPER MEHTODS ============== //

    private void updateRedisGroupLeaderboard(Long groupId, Long userId, Integer newScore) throws Exception { // Could refactor this away to a helper class
        if(Redis.updateRedisGroupLeaderboard(groupId, userId, newScore, realtimeleaderboard)) {
            broadCastGroupLeaderBoardUpdate(groupId);
        }
    }

    /**
     * Assumes level change is just plus one per call
     * @param country
     */

    public void updateRedisCountryLeaderBoard(User.Country country) {
        if (Redis.updateRedisCountryLeaderBoard(country, realtimeleaderboard)) {
            broadCastCountryLeaderboardUpdate();
        }
    }


    // ========== REAL TIME (SSE) ============== //

    public void addEmitter(Object key ,SseEmitter emitter) {
        List<SseEmitter> newEmitters = this.emitters.getOrDefault(key, new ArrayList<>());
        newEmitters.add(emitter);
        this.emitters.put(key, newEmitters);
        emitter.onCompletion(() -> removeEmitter(key, emitter));
        emitter.onTimeout(() -> removeEmitter(key,emitter)); 
    }

    public void removeEmitter(Object key, SseEmitter emitter) {
        List<SseEmitter> emittersList = emitters.get(key);
        if (emittersList != null) {
            emittersList.remove(emitter);
            if (emittersList.isEmpty()) {
                emitters.remove(key); 
            }
        }
    }

    public void broadCastUpdate(Object key, Object update) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        List<SseEmitter> relevantEmitters = emitters.getOrDefault(key, new ArrayList<>());
    
        for (SseEmitter emitter : relevantEmitters) {
            try {
                emitter.send(SseEmitter.event().data(update));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        relevantEmitters.removeAll(deadEmitters);
        if (relevantEmitters.isEmpty()) {
            emitters.remove(key); 
        } else {
            emitters.put(key, relevantEmitters); 
        }
    }
    
    public void broadCastGroupLeaderBoardUpdate(Long groupId) throws Exception {
        System.out.println("BROADCASTING UPDATE TO GROUP LEADERBOARD: " + groupId );
        GroupLeaderBoard updatedLeaderBoard = getGroupLeaderboard(groupId);
        broadCastUpdate(groupId,updatedLeaderBoard);
    }

    public void broadCastCountryLeaderboardUpdate() {
        System.out.println("BROADCASTING UPDATE TO COUNTRY LEADERBOARD");
        try {
            broadCastUpdate("country",getCountryLeaderboard(tournamentService.getCurrentTournamentId()));
        } catch (Exception e) {
            System.out.println("couldn't broadcast: " + e);
        }
    }

    public Tournament getCurrentTournament() throws NoSuchTournamentException, Exception {
        return tournamentService.getCurrentTournament();
    }

    public void integrationTestMethod() {
    }
    
    public User getUser(Long userId) throws Exception {
        return userService.getUser(userId);
    }

    // ========== DEV METHODS ================== //
   
    /**
     * Starts a localtime tournament for dev purposes
     */
    public Tournament startALocalTimeTournament() {
        return tournamentScheduler.startLocalTimeTournament();
    }
    
}