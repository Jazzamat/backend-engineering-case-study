package com.dreamgames.backendengineeringcasestudy.backendservice;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

import com.dreamgames.backendengineeringcasestudy.exceptions.NoSuchTournamentException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentGroupHasNotBegunException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentHasNotEndedException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;
import java.util.Set;
import java.util.ArrayList;

import jakarta.persistence.EntityNotFoundException;

import java.nio.file.attribute.GroupPrincipal;
import java.time.Duration;


@Service
public class BackendService {
    
    private UserService userService;
    private TournamentService tournamentService;

    @Autowired
    private final RedisTemplate<String,Object> realtimeleaderboard; //TODO maybe I need to make this a list of templates. or maybe have one for counrty one for group

    public BackendService(UserService userService, TournamentService tournamentService, RedisTemplate<String,Object> realtimeleaderboard) {
        this.userService = userService;
        this.tournamentService = tournamentService;
        this.realtimeleaderboard = realtimeleaderboard;
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
    public User updateUserLevelAndCoins(Long userId, int cointsToAdd)  {
        try {
            Pair<Integer,Long> scoreAndgroupId = tournamentService.incrementEntryScore(userId, tournamentService.getCurrentTournamentId());
            updateRedisGroupLeaderboard(scoreAndgroupId.getValue1(),userId, scoreAndgroupId.getValue0());
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
     * Allows a user to join the current tournament and returns the current group leaderboard
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
     *  Allows users to claim tournament rewards and returns updated progress data
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
     * @param groupId
     * @return
     */
    public GroupLeaderBoard getGroupLeaderboard(Long groupId) { // TODO test some more now that we have score in there too
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
                String[] parts = ((String)item).split(":");
                Long userId = Long.parseLong(parts[0]); // Extracting user ID
                Integer score = Integer.parseInt(parts[1]); // Extracting score
                User user = userService.getUser(userId);
                leaderboard.add(Pair.with(user,score));
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
        return tournamentService.getCountryLeaderboard(tournamentId);
    }
    
    
    // =========== HELPER MEHTODS ============== //

    private void updateRedisGroupLeaderboard(Long groupId, Long userId, Integer newScore) {
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
        } else {
            System.out.println("Leaderboard for group " + groupId + " does not exist in Redis.");
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