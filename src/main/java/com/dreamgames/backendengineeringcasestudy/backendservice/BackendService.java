package com.dreamgames.backendengineeringcasestudy.backendservice;
import org.springframework.stereotype.Service;

import java.util.List;

import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentGroupHasNotBegunException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;


@Service
public class BackendService {
    
    private UserService userService;
    private TournamentService tournamentService;

    public BackendService(UserService userService, TournamentService tournamentService) {
        this.userService = userService;
        this.tournamentService = tournamentService;
    }

    public void integrationTestMethod() {
    }

    public User createUser(String username) {
        return userService.createUser(username);
    }

    public User getUser(Long userId) {
        return userService.getUser(userId);
    }

    public User updateUserLevelAndCoins(Long userId, int cointsToAdd)  {
        try {
            tournamentService.incrementEntryScore(userId, tournamentService.getCurrentTournamentId());
        } catch (TournamentGroupHasNotBegunException e) {
            // do nothin 
        }
        return userService.updateUserLevelAndCoins(userId, cointsToAdd);
    }

    public TournamentGroup enterTournament(Long userId) throws Exception { // TODO: This should return group leader board
        User user = userService.getUser(userId);
        return tournamentService.enterTournament(user);
    } 

    public List<User> getGroupLeaderboard(Long groupId) {
        return tournamentService.getGroupLeaderboard(groupId);
    }

    public List<User> getCountryLeaderboard(User.Country country, Long tournamentId) {
        return tournamentService.getCountryLeaderboard(country, tournamentId);
    }

    public Tournament getCurrentTournament() {
        return tournamentService.getCurrentTournament();
    }

}