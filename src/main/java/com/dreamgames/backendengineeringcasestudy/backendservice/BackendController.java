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

    private final int COINS_PER_LEVEL = 25;

    private final BackendService backendService;

    /**
     * Constructor for BackendController.
     *
     * @param backendService the backend service to be used
     */
    @Autowired
    public BackendController(BackendService backendService) {
        this.backendService = backendService;
    }

    /**
     * Endpoint for creating a user.
     *
     * @param username the username of the user to be created
     * @return a CompletableFuture containing the ResponseEntity with the created user
     * @throws Exception if an error occurs during user creation
     */
    @PostMapping("/users")
    public CompletableFuture<ResponseEntity<?>> createUser(@RequestParam String username) throws Exception {
        return backendService.createUser(username).thenApply(ResponseEntity::ok);
    }

    /**
     * Endpoint for updating a user's level and coins.
     *
     * @param userId the ID of the user to be updated
     * @return a CompletableFuture containing the ResponseEntity with the updated user
     * @throws Exception if an error occurs during user update
     */
    @PutMapping("/users/updateLevel")
    public CompletableFuture<ResponseEntity<?>> updateUserLevelAndCoins(@RequestParam Long userId) throws Exception {
        try {
            User updatedUser = backendService.updateUserLevelAndCoinsAsyncWrapper(userId, COINS_PER_LEVEL).get(); 
            return CompletableFuture.completedFuture(ResponseEntity.ok(updatedUser));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(e.getMessage()));
        }
    }

    /**
     * Endpoint for entering a tournament.
     *
     * @param userId the ID of the user entering the tournament
     * @return a CompletableFuture containing the ResponseEntity with the group leaderboard
     */
    @PostMapping("/tournaments/enter")
    public CompletableFuture<ResponseEntity<?>> enterTournament(@RequestParam Long userId) {
        try {
            GroupLeaderBoard groupLeaderBoard = backendService.enterTournamentAsyncWrapper(userId).get();
            return CompletableFuture.completedFuture(ResponseEntity.ok(groupLeaderBoard));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(e.getMessage()));
        }
    }

    /**
     * Endpoint for claiming a tournament reward.
     *
     * @param userId       the ID of the user claiming the reward
     * @param tournamentId the ID of the tournament
     * @return the ResponseEntity with the updated user
     */
    @PostMapping("/tournaments/claimReward")
    public ResponseEntity<?> claimReward(@RequestParam("userId") Long userId, @RequestParam("tournamentId") Long tournamentId) {
        try {
            User updatedUser = backendService.claimRewardAsyncWrapper(userId, tournamentId).get();
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint for getting the group rank in a tournament.
     *
     * @param userId       the ID of the user
     * @param tournamentId the ID of the tournament
     * @return the ResponseEntity with the group rank
     */
    @GetMapping("/tournaments/rank")
    public ResponseEntity<?> getGroupRank(@RequestParam("userId") Long userId, @RequestParam("tournamentId") Long tournamentId) {
        try {
            int rank = backendService.getGroupRankAsyncWrapper(userId, tournamentId).get();
            return ResponseEntity.ok().body(rank);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint for getting the group leaderboard in a tournament.
     *
     * @param groupId the ID of the group
     * @return the ResponseEntity with the group leaderboard
     */
    @GetMapping("/tournaments/leaderboard/group")
    public ResponseEntity<?> getGroupLeaderboard(@RequestParam("groupId") Long groupId) {
        try {
            GroupLeaderBoard groupLeaderBoard = backendService.getGroupLeaderboardAsyncWrapper(groupId).get();
            return ResponseEntity.ok(groupLeaderBoard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint for getting the country leaderboard in a tournament.
     *
     * @param tournamentId the ID of the tournament
     * @return the ResponseEntity with the country leaderboard
     */
    @GetMapping("/tournaments/leaderboard/country")
    public ResponseEntity<?> getCountryLeaderboard(@RequestParam("tournamentId") Long tournamentId) {
        try {
            var countryLeaderboard = backendService.getCountryLeaderboardAsyncWrapper(tournamentId).get();
            return ResponseEntity.ok(countryLeaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ====== REAL TIME (SSE) ======== //

    /**
     * Endpoint for subscribing to country leaderboard updates.
     *
     * @return the SseEmitter for receiving updates
     */
    @GetMapping("tournaments/subscribe/countryLeaderboard")
    public SseEmitter subscribeToCountryLeaderBoardUpdates() { 
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        backendService.addEmitter("country", emitter);
        try {
            emitter.send(SseEmitter.event().name("test-event").data("SSE connection established successfully!"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * Endpoint for subscribing to group leaderboard updates.
     *
     * @param groupId the ID of the group
     * @return the SseEmitter for receiving updates
     */
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

    /**
     * Endpoint for scheduling a local tournament.
     *
     * @return the ResponseEntity with the scheduled tournament
     */
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

