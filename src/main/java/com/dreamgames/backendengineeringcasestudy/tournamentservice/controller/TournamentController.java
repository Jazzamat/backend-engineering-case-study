// package com.dreamgames.backendengineeringcasestudy.tournamentservice.controller;

// import java.util.List;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;
// import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;

// @RestController
// @RequestMapping("/tournaments")
// public class TournamentController {

//     private final TournamentService tournamentService;

//     @Autowired
//     public TournamentController(TournamentService tournamentService) {
//         this.tournamentService = tournamentService;
//     }

//     // // Enter Tournament and get current group leaderboard
//     // @PostMapping("/enter")
//     // public ResponseEntity<?> enterTournament(@RequestParam Long userId) { // TODO Fix law of demeter
//     //     try {
//     //         TournamentEntry entry = tournamentService.enterTournament(userId);
//     //         List<TournamentEntry> groupLeaderboard = tournamentService.getGroupLeaderboard(entry.getTournamentGroup().getId());
//     //         return ResponseEntity.ok(groupLeaderboard);
//     //     } catch (Exception e) {
//     //         return ResponseEntity.badRequest().body(e.getMessage());
//     //     }
//     // }

// //     // Claim Tournament Rewards and get updated progress data
// //     @PostMapping("/{userId}/claimRewards")
// //     public ResponseEntity<?> claimRewards(@PathVariable Long userId) {
// //         try {
// //             tournamentService.claimReward(userId);
// //             User updatedUser = tournamentService.getUserProgress(userId);
// //             return ResponseEntity.ok(updatedUser);
// //         } catch (Exception e) {
// //             return ResponseEntity.badRequest().body(e.getMessage());
// //         }
// //     }

// //     // Get Player's Rank in a tournament
// //     @GetMapping("/{tournamentId}/{userId}/rank")
// //     public ResponseEntity<?> getGroupRank(@PathVariable Long tournamentId, @PathVariable Long userId) {
// //         try {
// //             int rank = tournamentService.getUserRankInTournament(userId, tournamentId);
// //             return ResponseEntity.ok(Map.of("rank", rank));
// //         } catch (Exception e) {
// //             return ResponseEntity.badRequest().body(e.getMessage());
// //         }
// //     }

// //     // Get Group Leaderboard
// //     @GetMapping("/group/{groupId}/leaderboard")
// //     public ResponseEntity<List<TournamentEntry>> getGroupLeaderboard(@PathVariable Long groupId) {
// //         List<TournamentEntry> leaderboard = tournamentService.getGroupLeaderboard(groupId);
// //         return ResponseEntity.ok(leaderboard);
// //     }

// //     // Get Country Leaderboard for a tournament
// //     @GetMapping("/{tournamentId}/countryLeaderboard")
// //     public ResponseEntity<Map<String, Integer>> getCountryLeaderboard(@PathVariable Long tournamentId) {
// //         Map<String, Integer> leaderboard = tournamentService.getCountryLeaderboard(tournamentId);
// //         return ResponseEntity.ok(leaderboard);
// //     }
// }
