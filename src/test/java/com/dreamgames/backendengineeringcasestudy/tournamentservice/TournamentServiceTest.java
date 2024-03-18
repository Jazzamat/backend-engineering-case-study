package com.dreamgames.backendengineeringcasestudy.tournamentservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentEntryRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentScheduler;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;

import jakarta.transaction.Transactional;


@SpringBootTest
public class TournamentServiceTest {
   
    private TournamentService tournamentService;

    private UserService userService;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentGroupRepository groupRepository;

    @Autowired 
    TournamentEntryRepository entryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TournamentScheduler tournamentScheduler;


    @BeforeEach
    public void startService() { 
        this.tournamentService = new TournamentService(tournamentRepository, userRepository, groupRepository, entryRepository);
        this.userService = new UserService(userRepository);
        this.tournamentScheduler = new TournamentScheduler(tournamentService);
        tournamentScheduler.startNewTournament();
    }

    @Test 
    @Transactional
    public void getCurrentTournament() {
        tournamentScheduler.startNewTournament();
        assertDoesNotThrow( () -> {tournamentService.getCurrentTournament();});
        assertNotNull(tournamentService.getCurrentTournament());
    }

    @Test
    @Transactional
    public void basicCreateUserTestOne() {
        User testUser = userService.createUser("testUser");
        User gottenUser = userService.getUser(testUser.getId());
        assertTrue(testUser.equals(gottenUser));
    }

    @Test 
    @Transactional
    public void TestEnterTournamentBelow20() {
        User testUserBelow20 = userService.createUser("testUser"); 
        assertThrows(Exception.class, () -> tournamentService.enterTournament(testUserBelow20.getId()));
    }

    
    @Test
    @Transactional
    public void TestEnterTournamentAbove20() { // TODO: This test is flaky
        User testUserAbove20 = userService.createUser("above20");
        for (int i = 0; i < 21; i++) {
            userService.updateUserLevelAndCoins(testUserAbove20.getId(), 25);
        } 
        assertDoesNotThrow(() -> {tournamentService.enterTournament(testUserAbove20.getId());});
    }
    
    @Test 
    @Transactional
    public void TestEnterTwice() {
        //TODO
    }

    @Test
    @Transactional
    public void TestMatchMixingOne() {

    }

    @Test
    @Transactional
    public void TestMatchMixingTwo() {
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i ++ ) { // Create 100 users
            users.add(userService.createUser("USER: " + Integer.toString(i)));
        }
        for (User user : users) {
            for (int i = 0; i < 20; i++) { // Level the all up to 20
                userService.updateUserLevelAndCoins(user.getId(), 25);
            }
            assertDoesNotThrow( () -> { tournamentService.enterTournament(user.getId());} );
        }
    }

    @Test
    @Transactional
    public void TestGetGroupLeaderBoardOne() throws Exception {
        User user = userService.createUser("testUserForGetGroup");
        for (int i = 0; i < 20; i++) { // Level up to 20
            userService.updateUserLevelAndCoins(user.getId(), 25);
        }
        assertDoesNotThrow( () -> {
            TournamentGroup group = tournamentService.enterTournament(user.getId());
            assertDoesNotThrow(() -> {group.getGroupLeaderboard();});
            assertTrue(group.getEntries().get(0).getUser().getId().equals(user.getId()));
        });
    }
}
