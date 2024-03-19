package com.dreamgames.backendengineeringcasestudy.backendservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;

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
        return null;
        //     try {
            //         // Check if the user exists
            //         User user = userService.getUser(userId);
            //         if (user == null) {
                //             return ResponseEntity.badRequest().body("User does not exist");
                //         }
                //         // TournamentEntry entry = tournamentService.enterTournament(userId);
                //         // Assuming a method to get group leaderboard based on TournamentEntry's group ID
                //         // This is just a simplified example. Adjust according to your actual implementation.
                //         return ResponseEntity.ok(entry);
                //     } catch (Exception e) {
                    //         return ResponseEntity.badRequest().body(e.getMessage());
                    //     }
                }
                // Additional endpoints to interact with UserService and TournamentService as needed
}
            
            