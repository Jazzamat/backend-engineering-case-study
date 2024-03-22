package com.dreamgames.backendengineeringcasestudy.backendservice;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

@RestController
@RequestMapping("/backend")
public class BackendController {
    
    
    private final BackendService backendService;
    
    @Autowired
    public BackendController(BackendService backendService) {
        this.backendService = backendService;
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestParam String username) {
        try {
            User user = backendService.createUser(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/users/updateLevel")
    public ResponseEntity<?> updateUserLevelAndCoins(@RequestParam Long userId) {
        try {
            User updatedUser = backendService.updateUserLevelAndCoins(userId, 25); // TODO remove magic number and perhaps place it elsewhere (maybe in user)  
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/tournaments/enter")
    public ResponseEntity<?> enterTournament(@RequestParam Long userId) {
        try {
            GroupLeaderBoard groupLeaderBoard = backendService.enterTournament(userId);
            return ResponseEntity.ok(groupLeaderBoard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/tournaments/claimReward")
    public ResponseEntity<?> claimReward(@RequestParam("userId") Long userId, @RequestParam("tournamentId") Long tournamentId) {
        try {
            User updatedUser = backendService.claimReward(userId, tournamentId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/tournaments/rank")
    public ResponseEntity<?> getGroupRank(@RequestParam("userId") Long userId, @RequestParam("tournamentId") Long tournamentId) {
        try {
            int rank = backendService.getGroupRank(userId, tournamentId);
            return ResponseEntity.ok().body(rank);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/tournaments/leaderboard/group")
    public ResponseEntity<?> getGroupLeaderboard(@RequestParam("groupId") Long groupId) {
        try {
            GroupLeaderBoard groupLeaderBoard = backendService.getGroupLeaderboard(groupId);
            return ResponseEntity.ok(groupLeaderBoard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/tournaments/leaderboard/country")
    public ResponseEntity<?> getCountryLeaderboard(@RequestParam("tournamentId") Long tournamentId) {
        try {
            var countryLeaderboard = backendService.getCountryLeaderboard(tournamentId);
            return ResponseEntity.ok(countryLeaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

