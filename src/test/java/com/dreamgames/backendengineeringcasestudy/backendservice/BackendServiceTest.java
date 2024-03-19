
package com.dreamgames.backendengineeringcasestudy.backendservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;

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
public class BackendServiceTest {

    private BackendService backendService;

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
        tournamentScheduler.startLocalTimeTournament();
        this.backendService = new BackendService(this.userService,this.tournamentService);
    }


    @Test
    public void startUpTest() {
    }


    @Test
    @Transactional
    public void basicCreateUserTestOne() {
        User testUser = backendService.createUser("testUser");
        User gottenUser = backendService.getUser(testUser.getId());
        assertTrue(testUser.equals(gottenUser));
    }

    @Test
    @Transactional
    public void create100Users() {
        for (int i = 0; i < 99; i++){ 
            backendService.createUser("testUser");
        }
    }

    @Test 
    @Transactional
    public void TestEnterTournamentBelow20() {
        User testUserBelow20 = backendService.createUser("testUser"); 
        assertThrows(Exception.class, () -> backendService.enterTournament(testUserBelow20.getId()));
    }

    
    @Test
    @Transactional
    public void TestEnterTournamentAbove20() { // TODO: This test is flaky
        User testUserAbove20 = backendService.createUser("above20");
        for (int i = 0; i < 21; i++) {
            backendService.updateUserLevelAndCoins(testUserAbove20.getId(), 25);
        } 
        assertDoesNotThrow(() -> {backendService.enterTournament(testUserAbove20.getId());});
    }
    
    @Test 
    @Transactional
    public void TestEnterTwice() {
        User user = backendService.createUser("letmeintwice!");
        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(user.getId(), 25);
        }
        assertThrows(Exception.class, () -> {
            TournamentGroup group = backendService.enterTournament(user.getId());
            TournamentGroup groupDup = backendService.enterTournament(user.getId()); 

        });
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
            users.add(backendService.createUser("USER: " + Integer.toString(i)));
        }
        for (User user : users) {
            for (int i = 0; i < 20; i++) { // Level the all up to 20
                backendService.updateUserLevelAndCoins(user.getId(), 25);
            }
            assertDoesNotThrow( () -> {backendService.enterTournament(user.getId());} );
        }
    }


    @Test
    @Transactional
    public void TestGroupLeaderBoardNonTransactional() { // TODO reset db talbes after each run
        User turkish = new User("Kemal", User.Country.TURKEY);
        User french = new User("Julien", User.Country.FRANCE); 
        User american = new User("Bob", User.Country.USA);
        User british = new User("Charles", User.Country.UK);
        User german = new User("Johanes", User.Country.GERMANY);

        User turkishR = userRepository.save(turkish);
        User frenchR = userRepository.save(french);
        User americanR = userRepository.save(american);
        User britishR = userRepository.save(british);
        User germanR = userRepository.save(german);

        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(turkish.getId(), 25);
            backendService.updateUserLevelAndCoins(french.getId(), 25);
            backendService.updateUserLevelAndCoins(american.getId(), 25);
            backendService.updateUserLevelAndCoins(british.getId(), 25);
            backendService.updateUserLevelAndCoins(german.getId(), 25);
        }

        assertDoesNotThrow( () -> {
            TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
            TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
            TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
            TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
            TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

            // assertTrue(turksGroup.equals(frenchmansGroup));
            // assertTrue(frenchmansGroup.equals(americansGroup));
            // assertTrue(americansGroup.equals(britsGroup));
            // assertTrue(britsGroup.equals(germansGroup));

            List<User> TurksLeaderBoard = backendService.getGroupLeaderboard(turksGroup.getId());
            List<User> FrenchLeaderBoard = backendService.getGroupLeaderboard(frenchmansGroup.getId());
            List<User> AmericansLeaderBoard = backendService.getGroupLeaderboard(americansGroup.getId());
            List<User> BritsLeaderBoard = backendService.getGroupLeaderboard(britsGroup.getId());
            List<User> GermansLeaderBoard = backendService.getGroupLeaderboard(germansGroup.getId());            


            // assertTrue(TurksLeaderBoard.equals(FrenchLeaderBoard));
            // assertTrue(FrenchLeaderBoard.equals(AmericansLeaderBoard));
            // assertTrue(AmericansLeaderBoard.equals(BritsLeaderBoard));
            // assertTrue(BritsLeaderBoard.equals(GermansLeaderBoard));
            // assertTrue(GermansLeaderBoard.equals(TurksLeaderBoard)); 

            for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
                backendService.updateUserLevelAndCoins(turkish.getId(), 5);
            }
           
            List<User> leaderboard = backendService.getGroupLeaderboard(turksGroup.getId()); 

            for (int i = 0; i < 10; i++) {
                backendService.updateUserLevelAndCoins(french.getId(), 5);
            }

            List<User> updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
            assertTrue(french.equals(updatedLeaderboard.get(0)));
        });   
    }

    @Test
    @Transactional
    public void TestGetGroupLeaderBoardOne() throws Exception {
        User user = backendService.createUser("testUserForGetGroup");
        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(user.getId(), 25);
        }
        assertDoesNotThrow( () -> {
            TournamentGroup group = backendService.enterTournament(user.getId());
            assertDoesNotThrow(() -> {backendService.getGroupLeaderboard(group.getId());});
            assertTrue(group.getEntries().get(0).getUser().getId().equals(user.getId()));
        });
    }



    @Test
    @Transactional
    public void TestGetGroupLeaderBoardTwo() throws Exception {
        User turkish = new User("Kemal", User.Country.TURKEY);
        User french = new User("Julien", User.Country.FRANCE); 
        User american = new User("Bob", User.Country.USA);
        User british = new User("Charles", User.Country.UK);
        User german = new User("Johanes", User.Country.GERMANY);

        User turkishR = userRepository.save(turkish);
        User frenchR = userRepository.save(french);
        User americanR = userRepository.save(american);
        User britishR = userRepository.save(british);
        User germanR = userRepository.save(german);

        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(turkish.getId(), 25);
            backendService.updateUserLevelAndCoins(french.getId(), 25);
            backendService.updateUserLevelAndCoins(american.getId(), 25);
            backendService.updateUserLevelAndCoins(british.getId(), 25);
            backendService.updateUserLevelAndCoins(german.getId(), 25);
        }

        assertDoesNotThrow( () -> {
            TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
            TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
            TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
            TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
            TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

            assertTrue(turksGroup.equals(frenchmansGroup));
            assertTrue(frenchmansGroup.equals(americansGroup));
            assertTrue(americansGroup.equals(britsGroup));
            assertTrue(britsGroup.equals(germansGroup));

           
            List<User> TurksLeaderBoard = backendService.getGroupLeaderboard(turksGroup.getId());
            List<User> FrenchLeaderBoard = backendService.getGroupLeaderboard(frenchmansGroup.getId());
            List<User> AmericansLeaderBoard = backendService.getGroupLeaderboard(americansGroup.getId());
            List<User> BritsLeaderBoard = backendService.getGroupLeaderboard(britsGroup.getId());
            List<User> GermansLeaderBoard = backendService.getGroupLeaderboard(germansGroup.getId());          


            assertTrue(TurksLeaderBoard.equals(FrenchLeaderBoard));
            assertTrue(FrenchLeaderBoard.equals(AmericansLeaderBoard));
            assertTrue(AmericansLeaderBoard.equals(BritsLeaderBoard));
            assertTrue(BritsLeaderBoard.equals(GermansLeaderBoard));
            assertTrue(GermansLeaderBoard.equals(TurksLeaderBoard)); 
            
            
        });
    }

    @Test
    @Transactional
    public void TestGetGroupLeaderBoardThree() throws Exception {
        User turkish = new User("Kemal", User.Country.TURKEY);
        User french = new User("Julien", User.Country.FRANCE); 
        User american = new User("Bob", User.Country.USA);
        User british = new User("Charles", User.Country.UK);
        User german = new User("Johanes", User.Country.GERMANY);


        User extra = backendService.createUser("extraUser");

        User turkishR = userRepository.save(turkish);
        User frenchR = userRepository.save(french);
        User americanR = userRepository.save(american);
        User britishR = userRepository.save(british);
        User germanR = userRepository.save(german);

        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(turkish.getId(), 25);
            backendService.updateUserLevelAndCoins(french.getId(), 25);
            backendService.updateUserLevelAndCoins(american.getId(), 25);
            backendService.updateUserLevelAndCoins(british.getId(), 25);
            backendService.updateUserLevelAndCoins(german.getId(), 25);
            backendService.updateUserLevelAndCoins(extra.getId(), 25);
        }

        assertDoesNotThrow( () -> {
            TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
            TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
            TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
            TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
            TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());
            TournamentGroup extrasGroup = backendService.enterTournament(extra.getId());

            assertTrue(turksGroup.equals(frenchmansGroup));
            assertTrue(frenchmansGroup.equals(americansGroup));
            assertTrue(americansGroup.equals(britsGroup));
            assertTrue(britsGroup.equals(germansGroup));

            assertFalse(extrasGroup.equals(turksGroup));
           
            List<User> TurksLeaderBoard = backendService.getGroupLeaderboard(turksGroup.getId());
            List<User> FrenchLeaderBoard = backendService.getGroupLeaderboard(frenchmansGroup.getId());
            List<User> AmericansLeaderBoard = backendService.getGroupLeaderboard(americansGroup.getId());
            List<User> BritsLeaderBoard = backendService.getGroupLeaderboard(britsGroup.getId());
            List<User> GermansLeaderBoard = backendService.getGroupLeaderboard(germansGroup.getId());          

            List<User> ExtrasLeaderBoard = backendService.getGroupLeaderboard(extrasGroup.getId());

            assertTrue(TurksLeaderBoard.equals(FrenchLeaderBoard));
            assertTrue(FrenchLeaderBoard.equals(AmericansLeaderBoard));
            assertTrue(AmericansLeaderBoard.equals(BritsLeaderBoard));
            assertTrue(BritsLeaderBoard.equals(GermansLeaderBoard));
            assertTrue(GermansLeaderBoard.equals(TurksLeaderBoard)); 
           
            assertFalse(ExtrasLeaderBoard.equals(TurksLeaderBoard)); 
        });
    }

    @Test
    @Transactional
    public void TestGetGroupLeaderBoardFour() throws Exception {
        User kemal = new User("Kemal", User.Country.TURKEY);
        User oltan = new User("Oltan", User.Country.TURKEY);
        User omer = new User("Omer", User.Country.TURKEY);
        User kubilay = new User("Kubilay", User.Country.TURKEY);
        User hasan = new User("Hasan", User.Country.TURKEY);

        User kemalR = userRepository.save(kemal);
        User oltanR = userRepository.save(oltan);
        User omerR = userRepository.save(omer);
        User kubilayR = userRepository.save(kubilay);
        User hasanR = userRepository.save(hasan);

        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(kemal.getId(), 25);
            backendService.updateUserLevelAndCoins(oltan.getId(), 25);
            backendService.updateUserLevelAndCoins(omer.getId(), 25);
            backendService.updateUserLevelAndCoins(kubilay.getId(), 25);
            backendService.updateUserLevelAndCoins(hasan.getId(), 25);
        }

        assertDoesNotThrow( () -> {

            TournamentGroup kemalGroup = backendService.enterTournament(kemalR.getId());
            TournamentGroup oltanGroup = backendService.enterTournament(oltanR.getId());
            TournamentGroup omerGroup = backendService.enterTournament(omerR.getId());
            TournamentGroup kubilayGroup = backendService.enterTournament(kubilayR.getId());
            TournamentGroup hasanGroup = backendService.enterTournament(hasanR.getId());


            assertFalse(kemalGroup.equals(oltanGroup));
            assertFalse(oltanGroup.equals(omerGroup));
            assertFalse(omerGroup.equals(kubilayGroup));
            assertFalse(kubilayGroup.equals(hasanGroup));
            assertFalse(hasanGroup.equals(kemalGroup));


            List<User> KemalLeaderBoard = backendService.getGroupLeaderboard(kemalGroup.getId());
            List<User> OltanLeaderBoard = backendService.getGroupLeaderboard(oltanGroup.getId());
            List<User> OmerLeaderBoard = backendService.getGroupLeaderboard(omerGroup.getId());
            List<User> KubilayLeaderBoard = backendService.getGroupLeaderboard(kubilayGroup.getId());
            List<User> HasanLeaderBoard = backendService.getGroupLeaderboard(hasanGroup.getId());


            assertFalse(KemalLeaderBoard.equals(OltanLeaderBoard));
            assertFalse(OltanLeaderBoard.equals(OmerLeaderBoard));
            assertFalse(OmerLeaderBoard.equals(KubilayLeaderBoard));
            assertFalse(KubilayLeaderBoard.equals(HasanLeaderBoard));
            assertFalse(HasanLeaderBoard.equals(KemalLeaderBoard));

        });
    }



    @Test
    @Transactional
    public void TestGroupLeaderBoardOrder() throws Exception {
        User turkish = new User("Kemal", User.Country.TURKEY);
        User french = new User("Julien", User.Country.FRANCE); 
        User american = new User("Bob", User.Country.USA);
        User british = new User("Charles", User.Country.UK);
        User german = new User("Johanes", User.Country.GERMANY);

        User turkishR = userRepository.save(turkish);
        User frenchR = userRepository.save(french);
        User americanR = userRepository.save(american);
        User britishR = userRepository.save(british);
        User germanR = userRepository.save(german);

        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(turkish.getId(), 25);
            backendService.updateUserLevelAndCoins(french.getId(), 25);
            backendService.updateUserLevelAndCoins(american.getId(), 25);
            backendService.updateUserLevelAndCoins(british.getId(), 25);
            backendService.updateUserLevelAndCoins(german.getId(), 25);
        }

        assertDoesNotThrow( () -> {
            TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
            TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
            TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
            TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
            TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

            assertTrue(turksGroup.equals(frenchmansGroup));
            assertTrue(frenchmansGroup.equals(americansGroup));
            assertTrue(americansGroup.equals(britsGroup));
            assertTrue(britsGroup.equals(germansGroup));



            for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
                backendService.updateUserLevelAndCoins(turkish.getId(), 5);
            }
           
            List<User> leaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
            assertTrue(turkish.equals(leaderboard.get(0))); // assert that the turk is at the front

            for (int i = 0; i < 10; i++) {
                backendService.updateUserLevelAndCoins(french.getId(), 5);
            }

            List<User> updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
            assertTrue(french.equals(updatedLeaderboard.get(0)));

            for (int i = 0; i < 11; i++) {
                backendService.updateUserLevelAndCoins(british.getId(), 5);
            }

            updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
            assertTrue(british.equals(updatedLeaderboard.get(0)));

        });
    }


    @Test
    public void TestGroupLeaderBoardOrderNonTransactional() throws Exception {
        User turkish = new User("Kemal", User.Country.TURKEY);
        User french = new User("Julien", User.Country.FRANCE); 
        User american = new User("Bob", User.Country.USA);
        User british = new User("Charles", User.Country.UK);
        User german = new User("Johanes", User.Country.GERMANY);

        User turkishR = userRepository.save(turkish);
        User frenchR = userRepository.save(french);
        User americanR = userRepository.save(american);
        User britishR = userRepository.save(british);
        User germanR = userRepository.save(german);

        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(turkish.getId(), 25);
            backendService.updateUserLevelAndCoins(french.getId(), 25);
            backendService.updateUserLevelAndCoins(american.getId(), 25);
            backendService.updateUserLevelAndCoins(british.getId(), 25);
            backendService.updateUserLevelAndCoins(german.getId(), 25);
        }

        assertDoesNotThrow( () -> {
            TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
            TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
            TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
            TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
            TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

            assertTrue(turksGroup.getId().equals(frenchmansGroup.getId()));
            assertTrue(frenchmansGroup.getId().equals(americansGroup.getId()));
            assertTrue(americansGroup.getId().equals(britsGroup.getId()));
            assertTrue(britsGroup.getId().equals(germansGroup.getId()));





            for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
                backendService.updateUserLevelAndCoins(turkish.getId(), 5);
            }
           
            List<User> leaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
            assertTrue(leaderboard.get(0).getUsername().equals("Kemal")); // assert that the turk is at the front

            for (int i = 0; i < 10; i++) {
                backendService.updateUserLevelAndCoins(french.getId(), 5);
            }

            List<User> updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
            assertTrue(updatedLeaderboard.get(0).getUsername().equals("Julien")); // assert that the french is now at the front

            for (int i = 0; i < 11; i++) {
                backendService.updateUserLevelAndCoins(british.getId(), 5);
            }

            updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
            assertTrue(updatedLeaderboard.get(0).getUsername().equals("Charles")); // assert that the brit is in the lead

        });

    }
    
}

