package com.dreamgames.backendengineeringcasestudy.backendservice;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;

import org.hibernate.sql.exec.ExecutionException;
import org.javatuples.Pair;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.time.Duration;

import com.dreamgames.backendengineeringcasestudy.exceptions.NoSuchTournamentException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;


/**
* This class provides methods for retrieving leaderboards from Redis cache or database.
*  The Redis class provides methods for retrieving and updating leaderboard data from Redis cache or database.
* It interacts with Redis using the provided RedisTemplate and communicates with the tournament and user services
* to retrieve relevant data.
* 
* @author E. Omer Gul
*/
public class Redis {
    
    /**
    * Retrieves the group leaderboard from Redis cache or database.
    * If the leaderboard is not present in Redis cache, it queries the database and stores the result in Redis.
    * 
    * @param groupId The ID of the group.
    * @param realtimeleaderboard The RedisTemplate used for interacting with Redis.
    * @param tournamentService The service for retrieving tournament-related data.
    * @param userService The service for retrieving user-related data.
    * @return The group leaderboard.
    */
    public static GroupLeaderBoard redisGetGroupLeaderboard(Long groupId, RedisTemplate<String,Object> realtimeleaderboard, TournamentService tournamentService , UserService userService) throws InterruptedException, ExecutionException, Exception {
        String redisKey = "leaderboard:group:" + groupId;
        Boolean exists = realtimeleaderboard.hasKey(redisKey);
        if (exists == null || !exists) {
            GroupLeaderBoard groupLeaderBoard = tournamentService.getGroupLeaderboard(groupId); // this call queries the database
            groupLeaderBoard.getLeaderboard().forEach(pair -> {
                realtimeleaderboard.opsForZSet().add(redisKey, pair.getValue0().getId() + ":" + pair.getValue1(), pair.getValue1());
            });
            realtimeleaderboard.expire(redisKey,Duration.ofHours(1));
            return groupLeaderBoard;
        } else {
            Set<Object> leaderboardData = realtimeleaderboard.opsForZSet().reverseRange(redisKey, 0, -1);
            List<Pair<User,Integer>> leaderboard = new ArrayList<>();
            leaderboardData.forEach(item -> {
                try {
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
                } catch (Exception e) {
                    System.err.print(e.getMessage());
                }
            });
            return new GroupLeaderBoard(leaderboard, groupId);
        }
    }
    
    /**
    * Retrieves the country leaderboard from Redis cache or database.
    * If the leaderboard is not present in Redis cache, it queries the database and stores the result in Redis.
    * 
    * @param realtimeleaderboard The RedisTemplate used for interacting with Redis.
    * @param tournamentService The service for retrieving tournament-related data.
    * @param tournamentId The ID of the tournament.
    * @return The country leaderboard.
    * @throws NoSuchTournamentException If the tournament does not exist.
    */
    public static List<Pair<User.Country,Integer>> redisGetCountryLeaderboard(RedisTemplate<String,Object> realtimeleaderboard, TournamentService tournamentService, Long tournamentId) throws NoSuchTournamentException , Exception {
        String redisKey = "leaderboard:country";
        Boolean exists = realtimeleaderboard.hasKey(redisKey);
        if (exists == null || !exists) {
            System.out.println("First call so no Redis, generating country leaderboard...");
            List<Pair<User.Country, Integer>> initialLeaderboard = tournamentService.getCountryLeaderboard(tournamentId);
            initialLeaderboard.forEach(pair -> {
                String country = pair.getValue0().name();
                Integer score = pair.getValue1();
                realtimeleaderboard.opsForZSet().add(redisKey, country, score.doubleValue());
            });
            return initialLeaderboard;
        } else {
            System.out.println("Redis already exists, pulling from cache...");
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
    
    /**
    * 
    * @param groupId
    * @param userId
    * @param newScore
    * @param realtimeleaderboard
    * @return
    */
    public static boolean updateRedisGroupLeaderboard(Long groupId, Long userId, Integer newScore, RedisTemplate<String,Object> realtimeleaderboard) { // Could refactor this away to a helper class
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
            return true;
        } else {
            System.out.println("Leaderboard for group " + groupId + " does not exist in Redis.");
            return false;
        }
    }
    
    /**
    * Assumes level change is just plus one per call
    * @param country
    */
    public static boolean updateRedisCountryLeaderBoard(User.Country country, RedisTemplate<String,Object> realtimeleaderboard) { // TODO don't we need to chech if redis exists or not?
        String redisKey = "leaderboard:country";
        String countryMember = country.name();
        Integer scoreDelta = 1; 
        realtimeleaderboard.opsForZSet().incrementScore(redisKey, countryMember, scoreDelta);
        return true;
    }
}
