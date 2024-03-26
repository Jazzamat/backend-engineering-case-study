package com.dreamgames.backendengineeringcasestudy.backendservice;


import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public CompletableFuture<ResponseEntity<?>> createUser(@RequestParam String username) throws Exception {
        return backendService.createUser(username).thenApply(ResponseEntity::ok);
    }
    
    @PutMapping("/users/updateLevel")
    public CompletableFuture<ResponseEntity<?>> updateUserLevelAndCoins(@RequestParam Long userId) throws Exception {
        try {
            User updatedUser = backendService.updateUserLevelAndCoinsAsyncWrapper(userId, 25).get(); // TODO remove magic number and perhaps place it elsewhere (maybe in user)  
            return CompletableFuture.completedFuture(ResponseEntity.ok(updatedUser));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(e.getMessage()));
        }
    }
    
    @PostMapping("/tournaments/enter")
    public CompletableFuture<ResponseEntity<?>> enterTournament(@RequestParam Long userId) {
        try {
            GroupLeaderBoard groupLeaderBoard = backendService.enterTournament(userId);
            return CompletableFuture.completedFuture(ResponseEntity.ok(groupLeaderBoard));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(e.getMessage()));
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
    
    // ====== REAL TIME (SSE) ======== //
    
    @GetMapping("tournaments/subscribe/countryLeaderboard")
    public SseEmitter subscribeToCountryLeaderBoardUpdates() { // TODO duplicate code in these two methods
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        backendService.addEmitter("country",emitter);
        try {
            emitter.send(SseEmitter.event().name("test-event").data("SSE connection established successfully!"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @GetMapping("tournaments/subscribe/leaderboard/group")
    public SseEmitter subscibeToGroupLeaderBoardUpdates(@RequestParam("groupId") Long groupId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        backendService.addEmitter(groupId, emitter);
        try {
            emitter.send(SseEmitter.event().name("test-event").data("SSE connection established successfully!"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
    
    // ============ DEV ============== //
    
    @GetMapping("tournaments/dev/schedule") 
    public ResponseEntity<?> schedulteLocalTournament() {
        try {
            var tournament = backendService.startALocalTimeTournament();
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}

