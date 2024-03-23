package com.dreamgames.backendengineeringcasestudy.backendservice;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import com.dreamgames.backendengineeringcasestudy.exceptions.NoSuchTournamentException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentGroupHasNotBegunException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentHasNotEndedException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentScheduler;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.persistence.EntityNotFoundException;

import java.nio.file.attribute.GroupPrincipal;
import java.time.Duration;


@Service
public class BackendService {
    
    private UserService userService;
    private TournamentService tournamentService;
    private TournamentScheduler tournamentScheduler;

    private final Map<Object,List<SseEmitter>> emitters = new HashMap<>();

    @Autowired
    private final RedisTemplate<String,Object> realtimeleaderboard; //TODO maybe I need to make this a list of templates. or maybe have one for counrty one for group

    @Autowired
    private SimpMessagingTemplate webSocket;

    public BackendService(UserService userService, TournamentService tournamentService, RedisTemplate<String,Object> realtimeleaderboard, TournamentScheduler tournamentScheduler, SimpMessagingTemplate webSocket) {
        this.userService = userService;
        this.tournamentService = tournamentService;
        this.realtimeleaderboard = realtimeleaderboard;
        this.tournamentScheduler = tournamentScheduler;
        this.webSocket = webSocket;
    }

    /**
     * Creates a new user, returning a user object that contains, its id, level, coins and country
     * @param username
     * @return
     */
    public User createUser(String username) {
        return userService.createUser(username);
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
            System.out.println("UPDATING USER SCORE TO :" + scoreAndgroupId.getValue0());
            updateRedisGroupLeaderboard(scoreAndgroupId.getValue1(),userId, scoreAndgroupId.getValue0());
            updateRedisCountryLeaderBoard(userService.retreiveUsersCountry(userId));
        } catch (TournamentGroupHasNotBegunException e) {
            // do nothin 
        } catch (NoSuchTournamentException e) {
            // do nothin 
        } catch (EntityNotFoundException e) {
            // user has no entry to update
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
    public User claimReward(Long userId, Long tournamentId) throws TournamentHasNotEndedException, TournamentGroupHasNotBegunException {
        if (!tournamentService.hasEnded(tournamentId)) {
            throw new TournamentHasNotEndedException("Tournament is still going");
        }
        int rank = tournamentService.getGroupRank(userId, tournamentId);
        tournamentService.claimReward(userId);
        return userService.claimReward(userId,rank);
    }

    /**
     * Retrieves the player's rank for any tournament 
     * @param userId
     * @param tournamentId
     * @return
     */ 
    public int getGroupRank(Long userId, Long tournamentId) throws EntityNotFoundException, TournamentGroupHasNotBegunException {
        return tournamentService.getGroupRank(userId, tournamentId);
    }
    
    /**
     * Gets the leaderboard data of a tournament group. The data contains as user object (id, username, country) as well as the users tournament score
     * If it is the first call the mysql database is queried and a copy is created for the redis database
     * After that updates are live on the redis database so there is no need query the datbase every time.  
     * @param groupId
     * @return
     */
    public GroupLeaderBoard getGroupLeaderboard(Long groupId) { // TODO test some more now that we have score in there too
       // TODO REFACTOR AWAY REDIS LOGIC TO ANOTHER CLASS 
        System.out.println("GETTING GROUP LEADER BOARD");
        String redisKey = "leaderboard:group:" + groupId;
        Boolean exists = realtimeleaderboard.hasKey(redisKey);
        if (exists == null || !exists) {
            System.out.println("FIRST CALL SO NO REDIS");
            GroupLeaderBoard groupLeaderBoard = tournamentService.getGroupLeaderboard(groupId); // this call queries the database
            groupLeaderBoard.getLeaderboard().forEach(pair -> {
                realtimeleaderboard.opsForZSet().add(redisKey, pair.getValue0().getId() + ":" + pair.getValue1(), pair.getValue1());
            });
            realtimeleaderboard.expire(redisKey,Duration.ofHours(1));
            return groupLeaderBoard;
        } else {
            System.out.println("REDIS ALREADY EXISTS SO PULLING FROM CACHE");
            Set<Object> leaderboardData = realtimeleaderboard.opsForZSet().reverseRange(redisKey, 0, -1);
            List<Pair<User,Integer>> leaderboard = new ArrayList<>();
            leaderboardData.forEach(item -> {
                if (item instanceof String) {
                    String[] parts = ((String) item).split(":");
                    if (parts.length == 2) {
                        try {
                            Long userId = Long.parseLong(parts[0]);
                            Integer score = Integer.parseInt(parts[1]);
                            User user = userService.getUser(userId);
                            if (user != null) {
                                leaderboard.add(Pair.with(user, score));
                            } else {
                                System.out.println("User not found for ID: " + userId);
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Error parsing userID or score from: " + item);
                        }
                    }
                }
            });
            return new GroupLeaderBoard(leaderboard, groupId);
        }
    }
    
    /**
     * Gets the leaderboard data of the countries for a tournament. The data includes country name and its total tournament score
     * @param country
     * @param tournamentId
     * @return
     */
    public List<Pair<User.Country,Integer>> getCountryLeaderboard(Long tournamentId) throws NoSuchTournamentException { // TODO test some more now that you have changed it arround 
        String redisKey = "leaderboard:country";
       // TODO REFACTOR AWAY REDIS LOGIC TO ANOTHER CLASS 
        // Check if the leaderboard exists in Redis
        Boolean exists = realtimeleaderboard.hasKey(redisKey);
        if (exists == null || !exists) {
            System.out.println("First call so no Redis, generating country leaderboard...");
    
            // Use the existing service method to generate the initial leaderboard
            List<Pair<User.Country, Integer>> initialLeaderboard = tournamentService.getCountryLeaderboard(tournamentId);
    
            // Save each country's score to Redis
            initialLeaderboard.forEach(pair -> {
                String country = pair.getValue0().name();
                Integer score = pair.getValue1();
                realtimeleaderboard.opsForZSet().add(redisKey, country, score.doubleValue());
            });
    
            return initialLeaderboard;
        } else {
            System.out.println("Redis already exists, pulling from cache...");
    
            // Retrieve the leaderboard from Redis
            Set<Object> leaderboardData = realtimeleaderboard.opsForZSet().reverseRange(redisKey, 0, -1);
            List<Pair<User.Country, Integer>> leaderboard = new ArrayList<>();
            
            leaderboardData.forEach(item -> {
                String countryName = (String) item;
                Double score = realtimeleaderboard.opsForZSet().score(redisKey, item);
                leaderboard.add(Pair.with(User.Country.valueOf(countryName), score.intValue()));
            });
    
            return leaderboard;
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
                emitters.remove(key); // Remove the key if no emitters are left
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
    
    // Remove dead emitters
    relevantEmitters.removeAll(deadEmitters);
    if (relevantEmitters.isEmpty()) {
        emitters.remove(key); // Clean up if no live emitters are left for this key
    } else {
        emitters.put(key, relevantEmitters); // Update the list of emitters for this key
    }
}


    // ========== DEV METHODS ================== //
   
    /**
     * Starts a localtime tournament for dev purposes
     */
    public Tournament startALocalTimeTournament() {
        return tournamentScheduler.startLocalTimeTournament();
    }
    
    
    // =========== HELPER MEHTODS ============== //

    private void updateRedisGroupLeaderboard(Long groupId, Long userId, Integer newScore) { // Could refactor this away to a helper class
        String redisKey = "leaderboard:group:" + groupId;
        Boolean exists = realtimeleaderboard.hasKey(redisKey);
        if (exists != null && exists) {
            Set<Object> userScores = realtimeleaderboard.opsForZSet().rangeByScore(redisKey, 0, Double.MAX_VALUE);
            if(userScores != null){
                userScores.stream()
                          .filter(score -> score.toString().startsWith(userId + ":"))
                          .findFirst()
                          .ifPresent(score -> realtimeleaderboard.opsForZSet().remove(redisKey, score));
            }
            String valueToStore = userId + ":" + newScore;
            realtimeleaderboard.opsForZSet().add(redisKey, valueToStore, newScore);
            broadCastGroupLeaderBoardUpdate(groupId);
        } else {
            System.out.println("Leaderboard for group " + groupId + " does not exist in Redis.");
        }
    }
    /**
     * Assumes level change is just plus one per call
     * @param country
     */
    public void updateRedisCountryLeaderBoard(User.Country country) {
        String redisKey = "leaderboard:country";
        String countryMember = country.name();
        Integer scoreDelta = 1; 
        realtimeleaderboard.opsForZSet().incrementScore(redisKey, countryMember, scoreDelta);
        broadCastCountryLeaderboardUpdate();
    }

    
    public void broadCastGroupLeaderBoardUpdate(Long groupId) {
        System.out.println("BROADCASTING UPDATE TO GROUP LEADERBOARD: " + groupId );
        GroupLeaderBoard updatedLeaderBoard = getGroupLeaderboard(groupId);
        broadCastUpdate(groupId,updatedLeaderBoard);
    }

    public void broadCastCountryLeaderboardUpdate() {
        System.out.println("BROADCASTING UPDATE TO COUNTRY LEADERBOARD");
        try {
            broadCastUpdate("counrty",getCountryLeaderboard(tournamentService.getCurrentTournamentId()));
        } catch (Exception e) {
            System.out.println("couldn't broadcast: " + e);
        }
    }


    public Tournament getCurrentTournament() throws NoSuchTournamentException {
        return tournamentService.getCurrentTournament();
    }

    public void integrationTestMethod() {
    }
    
    public User getUser(Long userId) {
        return userService.getUser(userId);
    }
}