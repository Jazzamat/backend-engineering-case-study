package com.dreamgames.backendengineeringcasestudy.backendservice;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.swing.GroupLayout.Group;

import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentGroupHasNotBegunException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentHasNotEndedException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;


@Service
public class BackendService {
    
    private UserService userService;
    private TournamentService tournamentService;

    public BackendService(UserService userService, TournamentService tournamentService) {
        this.userService = userService;
        this.tournamentService = tournamentService;
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
            tournamentService.incrementEntryScore(userId, tournamentService.getCurrentTournamentId());
        } catch (TournamentGroupHasNotBegunException e) {
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
        return tournamentService.enterTournament(user);
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
        return tournamentService.getGroupLeaderboard(groupId);
    }
    
    /**
     * Gets the leaderboard data of the countries for a tournament. The data includes country name and its total tournament score
     * @param country
     * @param tournamentId
     * @return
     */
    public List<Pair<User.Country,Integer>> getCountryLeaderboard(Long tournamentId) { // TODO test some more now that you have changed it arround 
        return tournamentService.getCountryLeaderboard(tournamentId);
    }
    
   

    
    // =========== HELPER MEHTODS ============== //

    public Tournament getCurrentTournament() {
        return tournamentService.getCurrentTournament();
    }

    public void integrationTestMethod() {
    }
    
    public User getUser(Long userId) {
        return userService.getUser(userId);
    }
    
}