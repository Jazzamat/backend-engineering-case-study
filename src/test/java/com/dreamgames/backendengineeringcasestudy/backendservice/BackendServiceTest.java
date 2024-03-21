
package com.dreamgames.backendengineeringcasestudy.backendservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dreamgames.backendengineeringcasestudy.exceptions.AlreadyInCurrentTournamentException;
import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.exceptions.NotEnoughFundsException;
import com.dreamgames.backendengineeringcasestudy.exceptions.TournamentGroupHasNotBegunException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.GroupLeaderBoard;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentEntryRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentScheduler;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;

import jakarta.persistence.EntityNotFoundException;
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
        this.backendService = new BackendService(this.userService,this.tournamentService);
    }


    @Test
    public void startUpTest() {
    }

    @Test
    public void startATournament() {
        tournamentScheduler.startLocalTimeTournament();
    } 


    @Test
    @Transactional
    public void basicCreateUserTestOne() {
        User testUser = backendService.createUser("testUser");
        User gottenUser = backendService.getUser(testUser.getId());
        assertTrue(testUser.equals(gottenUser));
    }

    @Test
    // @Transactional
    public void basicUpdateLevelTestOne() {
        User testUser = backendService.createUser("testUser");
        assertDoesNotThrow(() -> {
            backendService.updateUserLevelAndCoins(testUser.getId(), 25);
        });
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
        tournamentScheduler.startLocalTimeTournament();
        User testUserBelow20 = backendService.createUser("testUser"); 
        assertThrows(LevelNotHighEnoughException.class, () -> backendService.enterTournament(testUserBelow20.getId()));
    }

    
    @Test
    @Transactional
    public void TestEnterTournamentAbove20() { // TODO: This test is flaky
        tournamentScheduler.startLocalTimeTournament();
        User testUserAbove20 = backendService.createUser("above20");
        for (int i = 0; i < 21; i++) {
            backendService.updateUserLevelAndCoins(testUserAbove20.getId(), 25);
        } 
        assertDoesNotThrow(() -> {backendService.enterTournament(testUserAbove20.getId());});
    }
    
    @Test 
    @Transactional
    public void TestEnterTwice() {
        tournamentScheduler.startLocalTimeTournament();
        User user = backendService.createUser("letmeintwice!");
        for (int i = 0; i < 20; i++) { // Level up to 20
            backendService.updateUserLevelAndCoins(user.getId(), 25);
        }
        assertThrows(AlreadyInCurrentTournamentException.class, () -> {
            backendService.enterTournament(user.getId());
            backendService.enterTournament(user.getId()); 

        });
    }

    @Test
    @Transactional
    public void TestDoesntHaveEnoughMoney() {
        tournamentScheduler.startLocalTimeTournament(); 
        User user = new User("pooruser", User.Country.FRANCE);
        user.setCoins(500);
        user.setLevel(30);
        userRepository.save(user);
        assertThrows(NotEnoughFundsException.class, () -> {backendService.enterTournament(user.getId());});
    }

    @Test 
    @Transactional
    public void TestMoneyIsDeducted() {
        User user = new User("pooruser", User.Country.FRANCE);
        user.setCoins(2000);
        user.setLevel(30);
        userRepository.save(user);
        assertDoesNotThrow(() -> {backendService.enterTournament(user.getId());}); 
        assertTrue(user.getCoins() == 1000);
    }

    @Test
    @Transactional
    public void TestMatchMixingOne() {

    }

    @Test
    @Transactional
    public void TestMatchMixingTwo() {
        tournamentScheduler.startLocalTimeTournament();
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
    public void TestLevelUpBeforeGroupBegins() {
        tournamentScheduler.startLocalTimeTournament();
        User turkish = new User("Kemal", User.Country.TURKEY);
        User french = new User("Julien", User.Country.FRANCE); 

        turkish.setLevel(20);
        french.setLevel(20);

        User turkishR = userRepository.save(turkish);
        User frenchR = userRepository.save(french);

        
        assertDoesNotThrow( () -> {
            Tournament currentTournament = backendService.getCurrentTournament(); 
            backendService.enterTournament(turkish.getId());
            backendService.enterTournament(french.getId());
           
            Integer turksScore = entryRepository.findByUserIdAndTournamentId(turkish.getId(), currentTournament.getId()).get().getScore();

            assertTrue(turksScore == 0);
            backendService.updateUserLevelAndCoins(turkish.getId(),25);
            assertTrue(turksScore == 0);
        });
    }

    // @Test
    // @Transactional
    // public void TestLevelUpAferGroupBegins() { // TODO see while its failing
    //     tournamentScheduler.startLocalTimeTournament();

    //     User turkish = new User("Kemal", User.Country.TURKEY);
    //     User french = new User("Julien", User.Country.FRANCE); 
    //     User american = new User("Bob", User.Country.USA);
    //     User british = new User("Charles", User.Country.UK);
    //     User german = new User("Johanes", User.Country.GERMANY);

    //     turkish.setLevel(20);
    //     french.setLevel(20);
    //     american.setLevel(20);
    //     british.setLevel(20);
    //     german.setLevel(20);

    //     User turkishR = userRepository.save(turkish);
    //     User frenchR = userRepository.save(french);
    //     User americanR = userRepository.save(american);
    //     User britishR = userRepository.save(british);
    //     User germanR = userRepository.save(german);

        
    //     assertDoesNotThrow( () -> {
    //         Tournament currentTournament = backendService.getCurrentTournament(); 
    //         backendService.enterTournament(turkish.getId());
    //         backendService.enterTournament(french.getId());
    //         backendService.enterTournament(americanR.getId());
    //         backendService.enterTournament(britishR.getId());
    //         backendService.enterTournament(germanR.getId());
 
 
    //         Integer turksScore = entryRepository.findByUserIdAndTournamentId(turkish.getId(), currentTournament.getId()).get().getScore();

    //         assertTrue(turksScore == 0);
    //         backendService.updateUserLevelAndCoins(turkish.getId(),25);
    //         assertTrue(turksScore == 1);
    //     });
    // }


    @Test
    @Transactional
    public void TestGroupLeaderBoardNonTransactional() { // TODO reset db talbes after each run and make it non transactional
        tournamentScheduler.startLocalTimeTournament();
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
            GroupLeaderBoard TurksLeaderBoard = backendService.enterTournament(turkishR.getId());
            GroupLeaderBoard FrenchLeaderBoard = backendService.enterTournament(frenchR.getId());
            GroupLeaderBoard AmericansLeaderBoard = backendService.enterTournament(americanR.getId());
            GroupLeaderBoard BritsLeaderBoard = backendService.enterTournament(britishR.getId());
            GroupLeaderBoard GermansLeaderBoard = backendService.enterTournament(germanR.getId());

            Long TurkgroupId = TurksLeaderBoard.getGroupId();
            Long FrenchgroupId = FrenchLeaderBoard.getGroupId();

            assert(TurkgroupId == FrenchgroupId);

            // assertTrue(TurksLeaderBoard.equals(FrenchLeaderBoard));
            // assertTrue(FrenchLeaderBoard.equals(AmericansLeaderBoard));
            // assertTrue(AmericansLeaderBoard.equals(BritsLeaderBoard));
            // assertTrue(BritsLeaderBoard.equals(GermansLeaderBoard));
            // assertTrue(GermansLeaderBoard.equals(TurksLeaderBoard)); 

            for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
                backendService.updateUserLevelAndCoins(turkish.getId(), 5);
            }
           
            for (int i = 0; i < 10; i++) {
                backendService.updateUserLevelAndCoins(french.getId(), 5);
            }

            GroupLeaderBoard updatedGroupLeaderBoard = backendService.getGroupLeaderboard(TurkgroupId);
            List<Pair<User,Integer>> leaderboard = updatedGroupLeaderBoard.getLeaderboard();

            assertTrue(french.equals(leaderboard.get(0).getValue0()));
        });   
    }
}

//     @Test
//     @Transactional
//     public void TestGetGroupLeaderBoardOne() throws Exception {
//         User user = backendService.createUser("testUserForGetGroup");
//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(user.getId(), 25);
//         }
//         assertDoesNotThrow( () -> {
//             GroupLeaderBoard groupLeaderBoard = backendService.enterTournament(user.getId());
//             assertDoesNotThrow(() -> {
//                 backendService.getGroupLeaderboard(groupLeaderBoard.getGroupId());
            
            
//             });
//         });
//     }



//     @Test
//     @Transactional
//     public void TestGetGroupLeaderBoardTwo() throws Exception {
//         User turkish = new User("Kemal", User.Country.TURKEY);
//         User french = new User("Julien", User.Country.FRANCE); 
//         User american = new User("Bob", User.Country.USA);
//         User british = new User("Charles", User.Country.UK);
//         User german = new User("Johanes", User.Country.GERMANY);

//         User turkishR = userRepository.save(turkish);
//         User frenchR = userRepository.save(french);
//         User americanR = userRepository.save(american);
//         User britishR = userRepository.save(british);
//         User germanR = userRepository.save(german);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//             backendService.updateUserLevelAndCoins(french.getId(), 25);
//             backendService.updateUserLevelAndCoins(american.getId(), 25);
//             backendService.updateUserLevelAndCoins(british.getId(), 25);
//             backendService.updateUserLevelAndCoins(german.getId(), 25);
//         }

//         assertDoesNotThrow( () -> {
//             TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//             TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//             TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//             TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//             TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

//             assertTrue(turksGroup.equals(frenchmansGroup));
//             assertTrue(frenchmansGroup.equals(americansGroup));
//             assertTrue(americansGroup.equals(britsGroup));
//             assertTrue(britsGroup.equals(germansGroup));

           
//             List<Pair<User,Integer>> TurksLeaderBoard = backendService.getGroupLeaderboard(turksGroup.getId());
//             List<Pair<User,Integer>> FrenchLeaderBoard = backendService.getGroupLeaderboard(frenchmansGroup.getId());
//             List<Pair<User,Integer>> AmericansLeaderBoard = backendService.getGroupLeaderboard(americansGroup.getId());
//             List<Pair<User,Integer>> BritsLeaderBoard = backendService.getGroupLeaderboard(britsGroup.getId());
//             List<Pair<User,Integer>> GermansLeaderBoard = backendService.getGroupLeaderboard(germansGroup.getId());          


//             assertTrue(TurksLeaderBoard.equals(FrenchLeaderBoard));
//             assertTrue(FrenchLeaderBoard.equals(AmericansLeaderBoard));
//             assertTrue(AmericansLeaderBoard.equals(BritsLeaderBoard));
//             assertTrue(BritsLeaderBoard.equals(GermansLeaderBoard));
//             assertTrue(GermansLeaderBoard.equals(TurksLeaderBoard)); 
            
            
//         });
//     }

//     @Test
//     @Transactional
//     public void TestGetGroupLeaderBoardThree() throws Exception {
//         User turkish = new User("Kemal", User.Country.TURKEY);
//         User french = new User("Julien", User.Country.FRANCE); 
//         User american = new User("Bob", User.Country.USA);
//         User british = new User("Charles", User.Country.UK);
//         User german = new User("Johanes", User.Country.GERMANY);


//         User extra = backendService.createUser("extraUser");

//         User turkishR = userRepository.save(turkish);
//         User frenchR = userRepository.save(french);
//         User americanR = userRepository.save(american);
//         User britishR = userRepository.save(british);
//         User germanR = userRepository.save(german);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//             backendService.updateUserLevelAndCoins(french.getId(), 25);
//             backendService.updateUserLevelAndCoins(american.getId(), 25);
//             backendService.updateUserLevelAndCoins(british.getId(), 25);
//             backendService.updateUserLevelAndCoins(german.getId(), 25);
//             backendService.updateUserLevelAndCoins(extra.getId(), 25);
//         }

//         assertDoesNotThrow( () -> {
//             TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//             TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//             TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//             TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//             TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());
//             TournamentGroup extrasGroup = backendService.enterTournament(extra.getId());

//             assertTrue(turksGroup.equals(frenchmansGroup));
//             assertTrue(frenchmansGroup.equals(americansGroup));
//             assertTrue(americansGroup.equals(britsGroup));
//             assertTrue(britsGroup.equals(germansGroup));

//             assertFalse(extrasGroup.equals(turksGroup));
           
//             List<Pair<User,Integer>> TurksLeaderBoard = backendService.getGroupLeaderboard(turksGroup.getId());
//             List<Pair<User,Integer>> FrenchLeaderBoard = backendService.getGroupLeaderboard(frenchmansGroup.getId());
//             List<Pair<User,Integer>> AmericansLeaderBoard = backendService.getGroupLeaderboard(americansGroup.getId());
//             List<Pair<User,Integer>> BritsLeaderBoard = backendService.getGroupLeaderboard(britsGroup.getId());
//             List<Pair<User,Integer>> GermansLeaderBoard = backendService.getGroupLeaderboard(germansGroup.getId());          

//             List<Pair<User,Integer>> ExtrasLeaderBoard = backendService.getGroupLeaderboard(extrasGroup.getId());

//             assertTrue(TurksLeaderBoard.equals(FrenchLeaderBoard));
//             assertTrue(FrenchLeaderBoard.equals(AmericansLeaderBoard));
//             assertTrue(AmericansLeaderBoard.equals(BritsLeaderBoard));
//             assertTrue(BritsLeaderBoard.equals(GermansLeaderBoard));
//             assertTrue(GermansLeaderBoard.equals(TurksLeaderBoard)); 
           
//             assertFalse(ExtrasLeaderBoard.equals(TurksLeaderBoard)); 
//         });
//     }

//     @Test
//     @Transactional
//     public void TestGetGroupLeaderBoardFour() throws Exception {
//         User kemal = new User("Kemal", User.Country.TURKEY);
//         User oltan = new User("Oltan", User.Country.TURKEY);
//         User omer = new User("Omer", User.Country.TURKEY);
//         User kubilay = new User("Kubilay", User.Country.TURKEY);
//         User hasan = new User("Hasan", User.Country.TURKEY);

//         User kemalR = userRepository.save(kemal);
//         User oltanR = userRepository.save(oltan);
//         User omerR = userRepository.save(omer);
//         User kubilayR = userRepository.save(kubilay);
//         User hasanR = userRepository.save(hasan);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(kemal.getId(), 25);
//             backendService.updateUserLevelAndCoins(oltan.getId(), 25);
//             backendService.updateUserLevelAndCoins(omer.getId(), 25);
//             backendService.updateUserLevelAndCoins(kubilay.getId(), 25);
//             backendService.updateUserLevelAndCoins(hasan.getId(), 25);
//         }

//         assertDoesNotThrow( () -> {

//             TournamentGroup kemalGroup = backendService.enterTournament(kemalR.getId());
//             TournamentGroup oltanGroup = backendService.enterTournament(oltanR.getId());
//             TournamentGroup omerGroup = backendService.enterTournament(omerR.getId());
//             TournamentGroup kubilayGroup = backendService.enterTournament(kubilayR.getId());
//             TournamentGroup hasanGroup = backendService.enterTournament(hasanR.getId());


//             assertFalse(kemalGroup.equals(oltanGroup));
//             assertFalse(oltanGroup.equals(omerGroup));
//             assertFalse(omerGroup.equals(kubilayGroup));
//             assertFalse(kubilayGroup.equals(hasanGroup));
//             assertFalse(hasanGroup.equals(kemalGroup));


//             List<Pair<User,Integer>> KemalLeaderBoard = backendService.getGroupLeaderboard(kemalGroup.getId());
//             List<Pair<User,Integer>> OltanLeaderBoard = backendService.getGroupLeaderboard(oltanGroup.getId());
//             List<Pair<User,Integer>> OmerLeaderBoard = backendService.getGroupLeaderboard(omerGroup.getId());
//             List<Pair<User,Integer>> KubilayLeaderBoard = backendService.getGroupLeaderboard(kubilayGroup.getId());
//             List<Pair<User,Integer>> HasanLeaderBoard = backendService.getGroupLeaderboard(hasanGroup.getId());


//             assertFalse(KemalLeaderBoard.equals(OltanLeaderBoard));
//             assertFalse(OltanLeaderBoard.equals(OmerLeaderBoard));
//             assertFalse(OmerLeaderBoard.equals(KubilayLeaderBoard));
//             assertFalse(KubilayLeaderBoard.equals(HasanLeaderBoard));
//             assertFalse(HasanLeaderBoard.equals(KemalLeaderBoard));

//         });
//     }



//     @Test
//     @Transactional
//     public void TestGroupLeaderBoardOrder() throws Exception {
//         User turkish = new User("Kemal", User.Country.TURKEY);
//         User french = new User("Julien", User.Country.FRANCE); 
//         User american = new User("Bob", User.Country.USA);
//         User british = new User("Charles", User.Country.UK);
//         User german = new User("Johanes", User.Country.GERMANY);

//         User turkishR = userRepository.save(turkish);
//         User frenchR = userRepository.save(french);
//         User americanR = userRepository.save(american);
//         User britishR = userRepository.save(british);
//         User germanR = userRepository.save(german);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//             backendService.updateUserLevelAndCoins(french.getId(), 25);
//             backendService.updateUserLevelAndCoins(american.getId(), 25);
//             backendService.updateUserLevelAndCoins(british.getId(), 25);
//             backendService.updateUserLevelAndCoins(german.getId(), 25);
//         }

//         assertDoesNotThrow( () -> {
//             TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//             TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//             TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//             TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//             TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

//             assertTrue(turksGroup.equals(frenchmansGroup));
//             assertTrue(frenchmansGroup.equals(americansGroup));
//             assertTrue(americansGroup.equals(britsGroup));
//             assertTrue(britsGroup.equals(germansGroup));



//             for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
//                 backendService.updateUserLevelAndCoins(turkish.getId(), 5);
//             }
           
//             List<Pair<User,Integer>> leaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
//             assertTrue(turkish.equals(leaderboard.get(0).getValue0())); // assert that the turk is at the front

//             for (int i = 0; i < 10; i++) {
//                 backendService.updateUserLevelAndCoins(french.getId(), 5);
//             }

//             List<Pair<User,Integer>> updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
//             assertTrue(french.equals(updatedLeaderboard.get(0).getValue0()));

//             for (int i = 0; i < 11; i++) {
//                 backendService.updateUserLevelAndCoins(british.getId(), 5);
//             }

//             updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
//             assertTrue(british.equals(updatedLeaderboard.get(0).getValue0()));

//         });
//     }


//     @Test
//     @Transactional
//     public void TestGroupLeaderBoardOrderNonTransactional() throws Exception {
//         User turkish = new User("Kemal", User.Country.TURKEY);
//         User french = new User("Julien", User.Country.FRANCE); 
//         User american = new User("Bob", User.Country.USA);
//         User british = new User("Charles", User.Country.UK);
//         User german = new User("Johanes", User.Country.GERMANY);

//         User turkishR = userRepository.save(turkish);
//         User frenchR = userRepository.save(french);
//         User americanR = userRepository.save(american);
//         User britishR = userRepository.save(british);
//         User germanR = userRepository.save(german);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//             backendService.updateUserLevelAndCoins(french.getId(), 25);
//             backendService.updateUserLevelAndCoins(american.getId(), 25);
//             backendService.updateUserLevelAndCoins(british.getId(), 25);
//             backendService.updateUserLevelAndCoins(german.getId(), 25);
//         }

//         assertDoesNotThrow( () -> {
//             TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//             TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//             TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//             TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//             TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

//             assertTrue(turksGroup.getId().equals(frenchmansGroup.getId()));
//             assertTrue(frenchmansGroup.getId().equals(americansGroup.getId()));
//             assertTrue(americansGroup.getId().equals(britsGroup.getId()));
//             assertTrue(britsGroup.getId().equals(germansGroup.getId()));

//             for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
//                 backendService.updateUserLevelAndCoins(turkish.getId(), 5);
//             }
           
//             List<Pair<User,Integer>> leaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
//             assertTrue(leaderboard.get(0).getValue0().getUsername().equals("Kemal")); // assert that the turk is at the front

//             for (int i = 0; i < 10; i++) {
//                 backendService.updateUserLevelAndCoins(french.getId(), 5);
//             }

//             List<Pair<User,Integer>> updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
//             assertTrue(updatedLeaderboard.get(0).getValue0().getUsername().equals("Julien")); // assert that the french is now at the front

//             for (int i = 0; i < 11; i++) {
//                 backendService.updateUserLevelAndCoins(british.getId(), 5);
//             }

//             updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getId());
//             assertTrue(updatedLeaderboard.get(0).getValue0().getUsername().equals("Charles")); // assert that the brit is in the lead

//         });
//     }


//     @Test
//     @Transactional
//     public void TestCountryLeaderboardOne() {
//         User turkish = new User("Kemal", User.Country.TURKEY);
//         User french = new User("Julien", User.Country.FRANCE); 
//         User american = new User("Bob", User.Country.USA);
//         User british = new User("Charles", User.Country.UK);
//         User german = new User("Johanes", User.Country.GERMANY);

//         User turkishR = userRepository.save(turkish);
//         User frenchR = userRepository.save(french);
//         User americanR = userRepository.save(american);
//         User britishR = userRepository.save(british);
//         User germanR = userRepository.save(german);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//             backendService.updateUserLevelAndCoins(french.getId(), 25);
//             backendService.updateUserLevelAndCoins(american.getId(), 25);
//             backendService.updateUserLevelAndCoins(british.getId(), 25);
//             backendService.updateUserLevelAndCoins(german.getId(), 25);
//         }

//         assertDoesNotThrow( () -> {
//             TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//             TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//             TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//             TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//             TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

//             assertTrue(turksGroup.getId().equals(frenchmansGroup.getId()));
//             assertTrue(frenchmansGroup.getId().equals(americansGroup.getId()));
//             assertTrue(americansGroup.getId().equals(britsGroup.getId()));
//             assertTrue(britsGroup.getId().equals(germansGroup.getId()));

//             Tournament currTournament = backendService.getCurrentTournament();
//             List<Pair<User.Country,Integer>> turkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> franceLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> usaLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> ukLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> germanyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());

//             assertTrue(turkeyLeaderBoard.size() == 5);
//             assertTrue(franceLeaderBoard.size() == 5);
//             assertTrue(usaLeaderBoard.size() == 5);
//             assertTrue(ukLeaderBoard.size() == 5);
//             assertTrue(germanyLeaderBoard.size() == 5);
            
//             User newTurkishUser = new User("NuvoTurk", User.Country.TURKEY); 

//             User newTurkishuserR = userRepository.save(newTurkishUser);

//             for (int i = 0; i < 19; i++) {
//                 backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//             }

//             backendService.enterTournament(newTurkishuserR.getId());

//             List<Pair<User.Country,Integer>> updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> updateFranceLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
//             List<Pair<User.Country,Integer>> updateUsaLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
//             List<Pair<User.Country,Integer>> updateUkLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
//             List<Pair<User.Country,Integer>> updateGermanyLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());

//             assertTrue(updateTurkeyLeaderBoard.size() == 5);
//             assertTrue(updateFranceLeaderBoard.size() == 5);
//             assertTrue(updateUsaLeaderBoard.size() == 5);
//             assertTrue(updateUkLeaderBoard.size() == 5);
//             assertTrue(updateGermanyLeaderBoard.size() == 5);
//             assertTrue(updateTurkeyLeaderBoard.get(0).getValue0().equals(User.Country.TURKEY));

//             for (int i = 0; i < 50; i++) {
//                 backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//             }
//             // Nuvo turk isn't in a tournament that has begun yet so updating their level won't change the position of the country leader board.
//             updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             assertTrue(updateTurkeyLeaderBoard.get(0).getValue0().equals(User.Country.TURKEY));
//        });
//     } 


//     @Test
//     @Transactional
//     public void TestCountryLeaderboardTwo() {
//         User kemal = new User("Kemal", User.Country.TURKEY);
//         User ayse = new User("Ayse", User.Country.TURKEY);
//         User omer = new User("Omer", User.Country.TURKEY);
//         User kubilay = new User("Kubilay", User.Country.TURKEY);
//         User merve = new User("Merve", User.Country.TURKEY);

//         User kemalR = userRepository.save(kemal);
//         User asyeR = userRepository.save(ayse);
//         User omerR = userRepository.save(omer);
//         User kubilayR = userRepository.save(kubilay);
//         User merveR = userRepository.save(merve);

//         User charles  = new User("Charles", User.Country.UK);
//         User xavier = new User("Xavier", User.Country.UK);
//         User henry = new User("Henry", User.Country.UK);
//         User susan = new User("Susan", User.Country.UK);
//         User Elizabeth = new User("Elizabeth", User.Country.UK);

//         User charlesR = userRepository.save(charles);
//         User xavierR = userRepository.save(xavier);
//         User henryR = userRepository.save(henry);
//         User susanR = userRepository.save(susan);
//         User elizabethR = userRepository.save(Elizabeth);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(kemal.getId(), 25);
//             backendService.updateUserLevelAndCoins(ayse.getId(), 25);
//             backendService.updateUserLevelAndCoins(omer.getId(), 25);
//             backendService.updateUserLevelAndCoins(kubilay.getId(), 25);
//             backendService.updateUserLevelAndCoins(merve.getId(), 25);

//             backendService.updateUserLevelAndCoins(charles.getId(), 25);
//             backendService.updateUserLevelAndCoins(xavier.getId(), 25);
//             backendService.updateUserLevelAndCoins(henry.getId(), 25);
//             backendService.updateUserLevelAndCoins(susan.getId(), 25);
//             backendService.updateUserLevelAndCoins(Elizabeth.getId(), 25);
//         }
//     }


//     @Test
//     @Transactional
//     public void TestGetGroupRankOne() {
//         User user = backendService.createUser("user");
//         Long tournamentId = tournamentService.getCurrentTournamentId();
//         assertThrows(EntityNotFoundException.class, () -> {
//             backendService.getGroupRank(user.getId(), tournamentId);
//         });
//     }

//     @Test
//     @Transactional
//     public void TestGetGroupRankTwo() {
//         User user = backendService.createUser("user");
//         Long tournamentId = tournamentService.getCurrentTournamentId();
//         assertThrows(EntityNotFoundException.class, () -> {
//             backendService.getGroupRank(user.getId(), tournamentId);
//         });
//     }


//     @Test
//     @Transactional
//     public void TestGetGroupRankThree() {
//          User turkish = new User("Kemal", User.Country.TURKEY);
//         User french = new User("Julien", User.Country.FRANCE); 
//         User american = new User("Bob", User.Country.USA);
//         User british = new User("Charles", User.Country.UK);
//         User german = new User("Johanes", User.Country.GERMANY);

//         User turkishR = userRepository.save(turkish);
//         User frenchR = userRepository.save(french);
//         User americanR = userRepository.save(american);
//         User britishR = userRepository.save(british);
//         User germanR = userRepository.save(german);

//         for (int i = 0; i < 20; i++) { // Level up to 20
//             backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//             backendService.updateUserLevelAndCoins(french.getId(), 25);
//             backendService.updateUserLevelAndCoins(american.getId(), 25);
//             backendService.updateUserLevelAndCoins(british.getId(), 25);
//             backendService.updateUserLevelAndCoins(german.getId(), 25);
//         }

//         assertDoesNotThrow( () -> {
//             TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//             TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//             TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//             TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//             TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

//             assertTrue(turksGroup.getId().equals(frenchmansGroup.getId()));
//             assertTrue(frenchmansGroup.getId().equals(americansGroup.getId()));
//             assertTrue(americansGroup.getId().equals(britsGroup.getId()));
//             assertTrue(britsGroup.getId().equals(germansGroup.getId()));

//             Tournament currTournament = backendService.getCurrentTournament();
//             List<Pair<User.Country,Integer>> turkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> franceLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> usaLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> ukLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             List<Pair<User.Country,Integer>> germanyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());

//             assertTrue(turkeyLeaderBoard.size() == 5);
//             assertTrue(franceLeaderBoard.size() == 5);
//             assertTrue(usaLeaderBoard.size() == 5);
//             assertTrue(ukLeaderBoard.size() == 5);
//             assertTrue(germanyLeaderBoard.size() == 5);
            
//             User newTurkishUser = new User("NuvoTurk", User.Country.TURKEY); 

//             User newTurkishuserR = userRepository.save(newTurkishUser);

//             for (int i = 0; i < 19; i++) {
//                 backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//             }

//             backendService.enterTournament(newTurkishuserR.getId());

//             List<Pair<User.Country,Integer>> updateTurkeyLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
//             List<Pair<User.Country,Integer>> updateFranceLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
//             List<Pair<User.Country,Integer>> updateUsaLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
//             List<Pair<User.Country,Integer>> updateUkLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
//             List<Pair<User.Country,Integer>> updateGermanyLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());

//             assertTrue(updateTurkeyLeaderBoard.size() == 5);
//             assertTrue(updateFranceLeaderBoard.size() == 5);
//             assertTrue(updateUsaLeaderBoard.size() == 5);
//             assertTrue(updateUkLeaderBoard.size() == 5);
//             assertTrue(updateGermanyLeaderBoard.size() == 5);

            
//             backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//             int turkishRank = backendService.getGroupRank(turkish.getId(), currTournament.getId());
//             assertTrue(turkishRank == 1);
            
//             assertThrows(TournamentGroupHasNotBegunException.class, () -> {
//                 backendService.getGroupRank(newTurkishuserR.getId(), currTournament.getId());
//             });

//             for (int i = 0; i < 50; i++) {
//                 backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//             }
//             // Nuvo turk isn't in a tournament that has begun yet so updating their level won't change their position in the country leader board.
//             updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
//             assertThrows(TournamentGroupHasNotBegunException.class, () -> {
//                 backendService.getGroupRank(newTurkishuserR.getId(), currTournament.getId());
//             });
//         });
//     }


//     // @Test
//     // @Transactional // TODO to come back to same as the test below
//     // public void TestGetGroupRankFour() {
//     //     User turkish = new User("Kemal", User.Country.TURKEY);
//     //     User french = new User("Julien", User.Country.FRANCE); 
//     //     User american = new User("Bob", User.Country.USA);
//     //     User british = new User("Charles", User.Country.UK);
//     //     User german = new User("Johanes", User.Country.GERMANY);

//     //     User turkishR = userRepository.save(turkish);
//     //     User frenchR = userRepository.save(french);
//     //     User americanR = userRepository.save(american);
//     //     User britishR = userRepository.save(british);
//     //     User germanR = userRepository.save(german);

//     //     for (int i = 0; i < 20; i++) { // Level up to 20
//     //         backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(french.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(american.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(british.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(german.getId(), 25);
//     //     }

//     //     assertDoesNotThrow( () -> {
//     //         TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//     //         TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//     //         TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//     //         TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//     //         TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

//     //         assertTrue(turksGroup.getId().equals(frenchmansGroup.getId()));
//     //         assertTrue(frenchmansGroup.getId().equals(americansGroup.getId()));
//     //         assertTrue(americansGroup.getId().equals(britsGroup.getId()));
//     //         assertTrue(britsGroup.getId().equals(germansGroup.getId()));

//     //         Tournament currTournament = backendService.getCurrentTournament();
//     //         List<User> turkeyLeaderBoard = backendService.getCountryLeaderboard(User.Country.TURKEY,currTournament.getId());
//     //         List<User> franceLeaderBoard = backendService.getCountryLeaderboard(User.Country.FRANCE,currTournament.getId());
//     //         List<User> usaLeaderBoard = backendService.getCountryLeaderboard(User.Country.USA,currTournament.getId());
//     //         List<User> ukLeaderBoard = backendService.getCountryLeaderboard(User.Country.UK,currTournament.getId());
//     //         List<User> germanyLeaderBoard = backendService.getCountryLeaderboard(User.Country.GERMANY,currTournament.getId());

//     //         assertTrue(turkeyLeaderBoard.size() == 1);
//     //         assertTrue(franceLeaderBoard.size() == 1);
//     //         assertTrue(usaLeaderBoard.size() == 1);
//     //         assertTrue(ukLeaderBoard.size() == 1);
//     //         assertTrue(germanyLeaderBoard.size() == 1);
            
//     //         User newTurkishUser = new User("NuvoTurk", User.Country.TURKEY); 
//     //         User newFrencUser = new User("NuvoFranc" , User.Country.FRANCE);
//     //         User newAmericanUser = new User("NuvoAmerican", User.Country.USA);
//     //         User newBritishUser = new User("NuvoBritish", User.Country.UK);
//     //         User newGermanUser = new User("NuvoGerman", User.Country.GERMANY);

//     //         User newTurkishuserR = userRepository.save(newTurkishUser);
//     //         User newFrenchuserR = userRepository.save(newFrencUser);
//     //         User newAmericanuserR = userRepository.save(newAmericanUser);
//     //         User newBritishuserR = userRepository.save(newBritishUser);
//     //         User newGermanuserR = userRepository.save(newGermanUser);
            
//     //         for (int i = 0; i < 19; i++) {
//     //             backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newFrenchuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newAmericanuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newBritishuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newGermanuserR.getId(), 25);
//     //         }

//     //         backendService.enterTournament(newTurkishuserR.getId());
//     //         backendService.enterTournament(newFrenchuserR.getId());
//     //         backendService.enterTournament(newAmericanuserR.getId());
//     //         backendService.enterTournament(newBritishuserR.getId());
//     //         backendService.enterTournament(newGermanuserR.getId());

//     //         List<User> updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(User.Country.TURKEY, currTournament.getId());
//     //         List<User> updateFranceLeaderBoard = backendService.getCountryLeaderboard(User.Country.FRANCE, currTournament.getId());
//     //         List<User> updateUsaLeaderBoard = backendService.getCountryLeaderboard(User.Country.USA, currTournament.getId());
//     //         List<User> updateUkLeaderBoard = backendService.getCountryLeaderboard(User.Country.UK, currTournament.getId());
//     //         List<User> updateGermanyLeaderBoard = backendService.getCountryLeaderboard(User.Country.GERMANY, currTournament.getId());

//     //         assertTrue(updateTurkeyLeaderBoard.size() == 2);
//     //         assertTrue(updateFranceLeaderBoard.size() == 2);
//     //         assertTrue(updateUsaLeaderBoard.size() == 2);
//     //         assertTrue(updateUkLeaderBoard.size() == 2);
//     //         assertTrue(updateGermanyLeaderBoard.size() == 2);

//     //         assertTrue(updateTurkeyLeaderBoard.get(0).getUsername().equals("Kemal"));

//     //         backendService.updateUserLevelAndCoins(turkish.getId(), 25);

//     //         int turkishRank = backendService.getGroupRank(turkish.getId(), currTournament.getId());
//     //         int newTurkishRank = backendService.getGroupRank(newTurkishUser.getId(), currTournament.getId());

//     //         assertTrue(turkishRank == 1); 
//     //         assertTrue(newTurkishRank == 2);

//     //         for (int i = 0; i < 20; i++) {
//     //             backendService.updateUserLevelAndCoins(turkish.getId(), i);
//     //         }
//     //         for (int i = 0; i < 50; i++) {
//     //             backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//     //         }

//     //         int updatedTurkishRank = backendService.getGroupRank(turkish.getId(), currTournament.getId());
//     //         int updatedNewTurkishRank = backendService.getGroupRank(newTurkishUser.getId(), currTournament.getId());

//     //         assertTrue(updatedNewTurkishRank == 1);
//     //         assertTrue(updatedTurkishRank == 2);
//     //    });
//     // }

//     // @Test
//     // @Transactional
//     // public void TestCountryLeaderboardThree() { // TODO THIS TEST DOES NOT WORK WHEN TRANSACTIONAL
//     //     User turkish = new User("Kemal", User.Country.TURKEY);
//     //     User french = new User("Julien", User.Country.FRANCE); 
//     //     User american = new User("Bob", User.Country.USA);
//     //     User british = new User("Charles", User.Country.UK);
//     //     User german = new User("Johanes", User.Country.GERMANY);

//     //     User turkishR = userRepository.save(turkish);
//     //     User frenchR = userRepository.save(french);
//     //     User americanR = userRepository.save(american);
//     //     User britishR = userRepository.save(british);
//     //     User germanR = userRepository.save(german);

//     //     for (int i = 0; i < 20; i++) { // Level up to 20
//     //         backendService.updateUserLevelAndCoins(turkish.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(french.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(american.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(british.getId(), 25);
//     //         backendService.updateUserLevelAndCoins(german.getId(), 25);
//     //     }

//     //     assertDoesNotThrow( () -> {
//     //         TournamentGroup turksGroup = backendService.enterTournament(turkishR.getId());
//     //         TournamentGroup frenchmansGroup = backendService.enterTournament(frenchR.getId());
//     //         TournamentGroup americansGroup = backendService.enterTournament(americanR.getId());
//     //         TournamentGroup britsGroup = backendService.enterTournament(britishR.getId());
//     //         TournamentGroup germansGroup = backendService.enterTournament(germanR.getId());

//     //         assertTrue(turksGroup.getId().equals(frenchmansGroup.getId()));
//     //         assertTrue(frenchmansGroup.getId().equals(americansGroup.getId()));
//     //         assertTrue(americansGroup.getId().equals(britsGroup.getId()));
//     //         assertTrue(britsGroup.getId().equals(germansGroup.getId()));

//     //         Tournament currTournament = backendService.getCurrentTournament();
//     //         List<User> turkeyLeaderBoard = backendService.getCountryLeaderboard(User.Country.TURKEY,currTournament.getId());
//     //         List<User> franceLeaderBoard = backendService.getCountryLeaderboard(User.Country.FRANCE,currTournament.getId());
//     //         List<User> usaLeaderBoard = backendService.getCountryLeaderboard(User.Country.USA,currTournament.getId());
//     //         List<User> ukLeaderBoard = backendService.getCountryLeaderboard(User.Country.UK,currTournament.getId());
//     //         List<User> germanyLeaderBoard = backendService.getCountryLeaderboard(User.Country.GERMANY,currTournament.getId());

//     //         assertTrue(turkeyLeaderBoard.size() == 1);
//     //         assertTrue(franceLeaderBoard.size() == 1);
//     //         assertTrue(usaLeaderBoard.size() == 1);
//     //         assertTrue(ukLeaderBoard.size() == 1);
//     //         assertTrue(germanyLeaderBoard.size() == 1);
            
//     //         User newTurkishUser = new User("NuvoTurk", User.Country.TURKEY); 
//     //         User newFrencUser = new User("NuvoFranc" , User.Country.FRANCE);
//     //         User newAmericanUser = new User("NuvoAmerican", User.Country.USA);
//     //         User newBritishUser = new User("NuvoBritish", User.Country.UK);
//     //         User newGermanUser = new User("NuvoGerman", User.Country.GERMANY);

//     //         User newTurkishuserR = userRepository.save(newTurkishUser);
//     //         User newFrenchuserR = userRepository.save(newFrencUser);
//     //         User newAmericanuserR = userRepository.save(newAmericanUser);
//     //         User newBritishuserR = userRepository.save(newBritishUser);
//     //         User newGermanuserR = userRepository.save(newGermanUser);
            
//     //         for (int i = 0; i < 19; i++) {
//     //             backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newFrenchuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newAmericanuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newBritishuserR.getId(), 25);
//     //             backendService.updateUserLevelAndCoins(newGermanuserR.getId(), 25);
//     //         }

//     //         TournamentGroup newTournamentGroup = backendService.enterTournament(newTurkishuserR.getId());
//     //         TournamentEntry newTurksihEntry = newTournamentGroup.getEntries().get(0);
            
//     //         backendService.enterTournament(newFrenchuserR.getId());
//     //         backendService.enterTournament(newAmericanuserR.getId());
//     //         backendService.enterTournament(newBritishuserR.getId());
//     //         backendService.enterTournament(newGermanuserR.getId());

//     //         List<User> updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(User.Country.TURKEY, currTournament.getId());
//     //         List<User> updateFranceLeaderBoard = backendService.getCountryLeaderboard(User.Country.FRANCE, currTournament.getId());
//     //         List<User> updateUsaLeaderBoard = backendService.getCountryLeaderboard(User.Country.USA, currTournament.getId());
//     //         List<User> updateUkLeaderBoard = backendService.getCountryLeaderboard(User.Country.UK, currTournament.getId());
//     //         List<User> updateGermanyLeaderBoard = backendService.getCountryLeaderboard(User.Country.GERMANY, currTournament.getId());

//     //         assertTrue(updateTurkeyLeaderBoard.size() == 2);
//     //         assertTrue(updateFranceLeaderBoard.size() == 2);
//     //         assertTrue(updateUsaLeaderBoard.size() == 2);
//     //         assertTrue(updateUkLeaderBoard.size() == 2);
//     //         assertTrue(updateGermanyLeaderBoard.size() == 2);

//     //         assertTrue(updateTurkeyLeaderBoard.get(0).getUsername().equals("Kemal"));

//     //         for (int i = 0; i < 20; i++) {
//     //             backendService.updateUserLevelAndCoins(turkish.getId(), i);
//     //         }
//     //         for (int i = 0; i < 50; i++) {
//     //             backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
//     //         }

            
//     //         TournamentEntry updatedNewTurkishEntry = entryRepository.findById(newTurksihEntry.getId()).get();
//     //         TournamentEntry updatedTurkishEntry = entryRepository.findById(newTurksihEntry.getId()).get();

//     //         // Nuvo turk is in a tournament now so their country leaderboard positions should update
//     //         updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(User.Country.TURKEY, currTournament.getId());
//     //         assertTrue(updateTurkeyLeaderBoard.get(0).getUsername().equals("NuvoTurk"));
//     //    });
//     // } 
    
// }

