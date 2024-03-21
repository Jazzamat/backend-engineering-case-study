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
    public ResponseEntity<User> createUser(@RequestBody String username) {
        User user = backendService.createUser(username);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/users/{userId}/level")
    public ResponseEntity<User> updateUserLevelAndCoins(@PathVariable Long userId) {
        User updatedUser = backendService.updateUserLevelAndCoins(userId, 25); // Assuming 25 coins per level as a constant
        return ResponseEntity.ok(updatedUser);
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
    
    @PostMapping("/tournaments/{tournamentId}/users/{userId}/claimReward")
    public ResponseEntity<?> claimReward(@PathVariable Long userId, @PathVariable Long tournamentId) {
        try {
            User updatedUser = backendService.claimReward(userId, tournamentId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/tournaments/{tournamentId}/users/{userId}/rank")
    public ResponseEntity<?> getGroupRank(@PathVariable Long userId, @PathVariable Long tournamentId) {
        try {
            int rank = backendService.getGroupRank(userId, tournamentId);
            return ResponseEntity.ok().body(rank);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/tournaments/{groupId}/leaderboard")
    public ResponseEntity<?> getGroupLeaderboard(@PathVariable Long groupId) {
        try {
            GroupLeaderBoard groupLeaderBoard = backendService.getGroupLeaderboard(groupId);
            return ResponseEntity.ok(groupLeaderBoard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/tournaments/{tournamentId}/leaderboard")
    public ResponseEntity<?> getCountryLeaderboard(@PathVariable Long tournamentId) {
        try {
            var countryLeaderboard = backendService.getCountryLeaderboard(tournamentId);
            return ResponseEntity.ok(countryLeaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

