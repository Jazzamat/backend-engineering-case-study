
package com.dreamgames.backendengineeringcasestudy.backendservice;

import static org.hamcrest.Matchers.array;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout.Group;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ExceptionCollector;

import com.dreamgames.backendengineeringcasestudy.exceptions.AlreadyInCurrentTournamentException;
import com.dreamgames.backendengineeringcasestudy.exceptions.HasUnclaimedRewards;
import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.exceptions.NoSuchTournamentException;
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
@ActiveProfiles("test")
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
    
    @Autowired
    private RedisTemplate<String,Object> realtimeleaderboard;


    @BeforeEach
    public void startService() { 
        this.tournamentService = new TournamentService(tournamentRepository, userRepository, groupRepository, entryRepository);
        this.userService = new UserService(userRepository);
        this.tournamentScheduler = new TournamentScheduler(tournamentService);
        this.backendService = new BackendService(userService, tournamentService, realtimeleaderboard, tournamentScheduler);
    }

    @Test
    public void startUpTest() {
    }

    // @Test
    // public void startATournament() {
    //     tournamentScheduler.startLocalTimeTournament();
    // } 

    @BeforeEach
    public void clearDB() {
        entryRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    public void basicCreateUserTestOne() {
        assertDoesNotThrow(() -> {
            User testUser = backendService.createUser("testUser").get();
            User gottenUser = backendService.getUser(testUser.getId());
            assertTrue(testUser.equals(gottenUser));
        });
    }

    @Test
    public void basicUpdateLevelTestOne() {
        assertDoesNotThrow(() -> {
            User testUser = backendService.createUser("testUser").get();
            backendService.updateUserLevelAndCoins(testUser.getId(), 25);
        });
    }

    @Test
    public void create100Users() {
        for (int i = 0; i < 99; i++){ 
            backendService.createUser("testUser");
        }
    }

    @Test 
    public void TestEnterTournamentBelow20() {
        entryRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
        tournamentScheduler.startLocalTimeTournament();
        assertDoesNotThrow(()-> {
            User testUserBelow20 = backendService.createUser("testUser").get(); 
            assertThrows(LevelNotHighEnoughException.class, () -> backendService.enterTournament(testUserBelow20.getId()));
        });
    }

    
    @Test
    public void TestEnterTournamentAbove20() { // TODO: This test is flaky
        tournamentScheduler.startLocalTimeTournament();
        assertDoesNotThrow(()-> {
            User testUserAbove20 = backendService.createUser("above20").get();
            for (int i = 0; i < 21; i++) {
                backendService.updateUserLevelAndCoins(testUserAbove20.getId(), 25);
            } 
            assertDoesNotThrow(() -> {backendService.enterTournament(testUserAbove20.getId());});
        });
    }
    
    @Test 
    public void TestEnterTwice() {
        assertDoesNotThrow(()-> {
            tournamentScheduler.startLocalTimeTournament();
            User user = backendService.createUser("letmeintwice!").get();
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(user.getId(), 25);
            }
            assertThrows(AlreadyInCurrentTournamentException.class, () -> {
                backendService.enterTournament(user.getId());
                backendService.enterTournament(user.getId()); 
    
            });
        });
    }

    @Test
    public void TestDoesntHaveEnoughMoney() {
        tournamentScheduler.startLocalTimeTournament(); 
        User user = new User("pooruser", User.Country.FRANCE);
        user.setCoins(500);
        user.setLevel(30);
        userRepository.save(user);
        assertThrows(NotEnoughFundsException.class, () -> {backendService.enterTournament(user.getId());});
    }

    @Test 
    public void TestMoneyIsDeducted() throws Exception  {
        tournamentScheduler.startLocalTimeTournament();
        User user = userService.createUser("pooruser"); 
        for (int i = 0; i < 20; i++) { // Level the all up to 20
            backendService.updateUserLevelAndCoins(user.getId(), 25);
        }

        assertDoesNotThrow(() -> {
            backendService.enterTournament(user.getId());
        }); 

        User updatedUser = userService.getUser(user.getId());
        assertTrue(updatedUser.getCoins() == 4500);
    }

    @Test
    public void TestMatchMixingTwo() {
        assertDoesNotThrow(()-> {
            tournamentScheduler.startLocalTimeTournament();
            ArrayList<User> users = new ArrayList<>();
            for (int i = 0; i < 100; i ++ ) { // Create 100 users
                users.add(backendService.createUser("USER: " + Integer.toString(i)).get());
            }
            for (User user : users) {
                for (int i = 0; i < 20; i++) { // Level the all up to 20
                    backendService.updateUserLevelAndCoins(user.getId(), 25);
                }
                assertDoesNotThrow( () -> {backendService.enterTournament(user.getId());} );
            }
        });
    }

    @Test
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

    @Test
    public void TestLevelUpAferGroupBegins() { // TODO see while its failing
        tournamentScheduler.startLocalTimeTournament();

        User turkish = new User("Kemal", User.Country.TURKEY);
        User french = new User("Julien", User.Country.FRANCE); 
        User american = new User("Bob", User.Country.USA);
        User british = new User("Charles", User.Country.UK);
        User german = new User("Johanes", User.Country.GERMANY);

        turkish.setLevel(20);
        french.setLevel(20);
        american.setLevel(20);
        british.setLevel(20);
        german.setLevel(20);

        User turkishR = userRepository.save(turkish);
        User frenchR = userRepository.save(french);
        User americanR = userRepository.save(american);
        User britishR = userRepository.save(british);
        User germanR = userRepository.save(german);

        
        assertDoesNotThrow( () -> {
            Tournament currentTournament = backendService.getCurrentTournament(); 
            backendService.enterTournament(turkish.getId());
            backendService.enterTournament(french.getId());
            backendService.enterTournament(americanR.getId());
            backendService.enterTournament(britishR.getId());
            backendService.enterTournament(germanR.getId());
 
 
            Integer turksScore = entryRepository.findByUserIdAndTournamentId(turkish.getId(), currentTournament.getId()).get().getScore();

            assertTrue(turksScore == 0);
            backendService.updateUserLevelAndCoins(turkish.getId(),25);
            
            Integer turksScoreUpdated = entryRepository.findByUserIdAndTournamentId(turkish.getId(), currentTournament.getId()).get().getScore();
            assertTrue(turksScoreUpdated == 1);
        });
    }

    @Test
    public void TestGroupLeaderBoardNonTransactional() { // TODO reset db talbes after each run and make it non transactional
        entryRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
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


        assertDoesNotThrow( () -> {
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(turkish.getId(), 25);
                backendService.updateUserLevelAndCoins(french.getId(), 25);
                backendService.updateUserLevelAndCoins(american.getId(), 25);
                backendService.updateUserLevelAndCoins(british.getId(), 25);
                backendService.updateUserLevelAndCoins(german.getId(), 25);
            }
        });

        assertDoesNotThrow( () -> {
            GroupLeaderBoard TurksLeaderBoard = backendService.enterTournament(turkishR.getId());
            GroupLeaderBoard FrenchLeaderBoard = backendService.enterTournament(frenchR.getId());
            GroupLeaderBoard AmericansLeaderBoard = backendService.enterTournament(americanR.getId());
            GroupLeaderBoard BritsLeaderBoard = backendService.enterTournament(britishR.getId());
            GroupLeaderBoard GermansLeaderBoard = backendService.enterTournament(germanR.getId());

            Long TurkgroupId = TurksLeaderBoard.getGroupId();
            Long FrenchgroupId = FrenchLeaderBoard.getGroupId();

            assert(TurkgroupId.equals(FrenchgroupId));

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

            User updateFrench = userService.getUser(french.getId());
            GroupLeaderBoard updatedGroupLeaderBoard = backendService.getGroupLeaderboard(TurkgroupId);
            List<Pair<User,Integer>> leaderboard = updatedGroupLeaderBoard.getLeaderboard();

            assertTrue(updateFrench.equals(leaderboard.get(0).getValue0()));
        });   
    }

    @Test
    public void TestGetGroupLeaderBoardOne() throws Exception {
        assertDoesNotThrow(()-> {
            tournamentScheduler.startLocalTimeTournament();
            User user = backendService.createUser("testUserForGetGroup").get();
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(user.getId(), 25);
            }
            assertDoesNotThrow( () -> {
                GroupLeaderBoard groupLeaderBoard = backendService.enterTournament(user.getId());
                assertDoesNotThrow(() -> {
                    backendService.getGroupLeaderboard(groupLeaderBoard.getGroupId());
                });
            });
        });
    }



    @Test
    public void TestGetGroupLeaderBoardTwo() throws Exception {
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
            GroupLeaderBoard turksGroup = backendService.enterTournament(turkishR.getId());
            GroupLeaderBoard frenchmansGroup = backendService.enterTournament(frenchR.getId());
            GroupLeaderBoard americansGroup = backendService.enterTournament(americanR.getId());
            GroupLeaderBoard britsGroup = backendService.enterTournament(britishR.getId());
            GroupLeaderBoard germansGroup = backendService.enterTournament(germanR.getId());

            assertTrue(turksGroup.getGroupId().equals(frenchmansGroup.getGroupId()));
            assertTrue(britsGroup.getGroupId().equals(germansGroup.getGroupId()));
           
            GroupLeaderBoard TurksLeaderBoard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
            GroupLeaderBoard FrenchLeaderBoard = backendService.getGroupLeaderboard(frenchmansGroup.getGroupId());
            GroupLeaderBoard AmericansLeaderBoard = backendService.getGroupLeaderboard(americansGroup.getGroupId());
            GroupLeaderBoard BritsLeaderBoard = backendService.getGroupLeaderboard(britsGroup.getGroupId());
            GroupLeaderBoard GermansLeaderBoard = backendService.getGroupLeaderboard(germansGroup.getGroupId());          

            assertTrue(FrenchLeaderBoard.getGroupId().equals(AmericansLeaderBoard.getGroupId()));
            assertTrue(BritsLeaderBoard.getGroupId().equals(GermansLeaderBoard.getGroupId()));
            
        });
    }

    @Test
    public void TestGetGroupLeaderBoardThree() throws Exception {
        assertDoesNotThrow(()-> {
            tournamentScheduler.startLocalTimeTournament();
            User turkish = new User("Kemal", User.Country.TURKEY);
            User french = new User("Julien", User.Country.FRANCE); 
            User american = new User("Bob", User.Country.USA);
            User british = new User("Charles", User.Country.UK);
            User german = new User("Johanes", User.Country.GERMANY);
    
    
            User extra = backendService.createUser("extraUser").get();
    
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
                GroupLeaderBoard turksGroup = backendService.enterTournament(turkishR.getId());
                GroupLeaderBoard frenchmansGroup = backendService.enterTournament(frenchR.getId());
                GroupLeaderBoard americansGroup = backendService.enterTournament(americanR.getId());
                GroupLeaderBoard britsGroup = backendService.enterTournament(britishR.getId());
                GroupLeaderBoard germansGroup = backendService.enterTournament(germanR.getId());
                GroupLeaderBoard extrasGroup = backendService.enterTournament(extra.getId());
    
                assertTrue(turksGroup.getGroupId().equals(frenchmansGroup.getGroupId()));
                assertFalse(turksGroup.getGroupId().equals(extrasGroup.getGroupId()));
               
                GroupLeaderBoard TurksLeaderBoard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
                GroupLeaderBoard FrenchLeaderBoard = backendService.getGroupLeaderboard(frenchmansGroup.getGroupId());
                GroupLeaderBoard AmericansLeaderBoard = backendService.getGroupLeaderboard(americansGroup.getGroupId());
                GroupLeaderBoard BritsLeaderBoard = backendService.getGroupLeaderboard(britsGroup.getGroupId());
                GroupLeaderBoard GermansLeaderBoard = backendService.getGroupLeaderboard(germansGroup.getGroupId());          
    
                GroupLeaderBoard ExtrasLeaderBoard = backendService.getGroupLeaderboard(extrasGroup.getGroupId());
    
                assertTrue(TurksLeaderBoard.getGroupId().equals(FrenchLeaderBoard.getGroupId()));
               
                assertFalse(ExtrasLeaderBoard.getGroupId().equals(TurksLeaderBoard.getGroupId())); 
            });
        });
    }

    @Test
    public void TestGetGroupLeaderBoardFour() throws Exception {
        tournamentScheduler.startLocalTimeTournament();
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

            GroupLeaderBoard kemalGroup = backendService.enterTournament(kemalR.getId());
            GroupLeaderBoard oltanGroup = backendService.enterTournament(oltanR.getId());
            GroupLeaderBoard omerGroup = backendService.enterTournament(omerR.getId());
            GroupLeaderBoard kubilayGroup = backendService.enterTournament(kubilayR.getId());
            GroupLeaderBoard hasanGroup = backendService.enterTournament(hasanR.getId());


            assertFalse(kemalGroup.equals(oltanGroup));
            assertFalse(oltanGroup.equals(omerGroup));
            assertFalse(omerGroup.equals(kubilayGroup));
            assertFalse(kubilayGroup.equals(hasanGroup));
            assertFalse(hasanGroup.equals(kemalGroup));


            GroupLeaderBoard KemalLeaderBoard = backendService.getGroupLeaderboard(kemalGroup.getGroupId());
            GroupLeaderBoard OltanLeaderBoard = backendService.getGroupLeaderboard(oltanGroup.getGroupId());
            GroupLeaderBoard OmerLeaderBoard = backendService.getGroupLeaderboard(omerGroup.getGroupId());
            GroupLeaderBoard KubilayLeaderBoard = backendService.getGroupLeaderboard(kubilayGroup.getGroupId());
            GroupLeaderBoard HasanLeaderBoard = backendService.getGroupLeaderboard(hasanGroup.getGroupId());


            assertFalse(KemalLeaderBoard.equals(OltanLeaderBoard));
            assertFalse(OltanLeaderBoard.equals(OmerLeaderBoard));
            assertFalse(OmerLeaderBoard.equals(KubilayLeaderBoard));
            assertFalse(KubilayLeaderBoard.equals(HasanLeaderBoard));
            assertFalse(HasanLeaderBoard.equals(KemalLeaderBoard));

        });
    }

    @Test
    public void TestGroupLeaderBoardOrder() throws Exception {
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
            GroupLeaderBoard turksGroup = backendService.enterTournament(turkishR.getId());
            GroupLeaderBoard frenchmansGroup = backendService.enterTournament(frenchR.getId());
            GroupLeaderBoard americansGroup = backendService.enterTournament(americanR.getId());
            GroupLeaderBoard britsGroup = backendService.enterTournament(britishR.getId());
            GroupLeaderBoard germansGroup = backendService.enterTournament(germanR.getId());

            assertTrue(turksGroup.getGroupId().equals(americansGroup.getGroupId()));
            assertTrue(americansGroup.getGroupId().equals(germansGroup.getGroupId()));

            for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
                backendService.updateUserLevelAndCoins(turkish.getId(), 5);
            }
          
            User updatedTurkish = userService.getUser(turkish.getId());
            GroupLeaderBoard leaderboard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
            assertTrue(updatedTurkish.equals(leaderboard.getLeaderboard().get(0).getValue0())); // assert that the turk is at the front

            for (int i = 0; i < 10; i++) {
                backendService.updateUserLevelAndCoins(french.getId(), 5);
            }

            User updatedFrench = userService.getUser(french.getId());
            GroupLeaderBoard updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
            assertTrue(updatedFrench.equals(updatedLeaderboard.getLeaderboard().get(0).getValue0()));

            for (int i = 0; i < 11; i++) {
                backendService.updateUserLevelAndCoins(british.getId(), 5);
            }
            
            User updatedBritish = userService.getUser(british.getId());
            updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
            assertTrue(updatedBritish.equals(updatedLeaderboard.getLeaderboard().get(0).getValue0()));

        });
    }

    @Test
    public void TestGroupLeaderBoardOrderNonTransactional() throws Exception {
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
            GroupLeaderBoard turksGroup = backendService.enterTournament(turkishR.getId());
            GroupLeaderBoard frenchmansGroup = backendService.enterTournament(frenchR.getId());
            GroupLeaderBoard americansGroup = backendService.enterTournament(americanR.getId());
            GroupLeaderBoard britsGroup = backendService.enterTournament(britishR.getId());
            GroupLeaderBoard germansGroup = backendService.enterTournament(germanR.getId());

            for (int i = 0; i < 5; i++) { // Turk levels up 5 while in tournament
                backendService.updateUserLevelAndCoins(turkish.getId(), 5);
            }
           
            GroupLeaderBoard leaderboard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
            assertTrue(leaderboard.getLeaderboard().get(0).getValue0().getUsername().equals("Kemal")); // assert that the turk is at the front

            for (int i = 0; i < 10; i++) {
                backendService.updateUserLevelAndCoins(french.getId(), 5);
            }

            GroupLeaderBoard updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
            assertTrue(updatedLeaderboard.getLeaderboard().get(0).getValue0().getUsername().equals("Julien")); // assert that the french is now at the front

            for (int i = 0; i < 11; i++) {
                backendService.updateUserLevelAndCoins(british.getId(), 5);
            }

            updatedLeaderboard = backendService.getGroupLeaderboard(turksGroup.getGroupId());
            assertTrue(updatedLeaderboard.getLeaderboard().get(0).getValue0().getUsername().equals("Charles")); // assert that the brit is in the lead

        });
    }

    @Test
    public void TestCountryLeaderboardOne() {
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


        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(turkish.getId(), 25);
                backendService.updateUserLevelAndCoins(french.getId(), 25);
                backendService.updateUserLevelAndCoins(american.getId(), 25);
                backendService.updateUserLevelAndCoins(british.getId(), 25);
                backendService.updateUserLevelAndCoins(german.getId(), 25);
            }
        });

        assertDoesNotThrow( () -> {
            GroupLeaderBoard turksGroup = backendService.enterTournament(turkishR.getId());
            GroupLeaderBoard frenchmansGroup = backendService.enterTournament(frenchR.getId());
            GroupLeaderBoard americansGroup = backendService.enterTournament(americanR.getId());
            GroupLeaderBoard britsGroup = backendService.enterTournament(britishR.getId());
            GroupLeaderBoard germansGroup = backendService.enterTournament(germanR.getId());

            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(turkish.getId(), 25);
                backendService.updateUserLevelAndCoins(french.getId(), 25);
                backendService.updateUserLevelAndCoins(american.getId(), 25);
                backendService.updateUserLevelAndCoins(british.getId(), 25);
                backendService.updateUserLevelAndCoins(german.getId(), 25);
            }


            Tournament currTournament = backendService.getCurrentTournament();
            List<Pair<User.Country,Integer>> turkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> franceLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> usaLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> ukLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> germanyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());

            assertTrue(turkeyLeaderBoard.size() == 5);
            assertTrue(franceLeaderBoard.size() == 5);
            assertTrue(usaLeaderBoard.size() == 5);
            assertTrue(ukLeaderBoard.size() == 5);
            assertTrue(germanyLeaderBoard.size() == 5);
            
            User newTurkishUser = new User("NuvoTurk", User.Country.TURKEY); 

            User newTurkishuserR = userRepository.save(newTurkishUser);

            for (int i = 0; i < 19; i++) {
                backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
                backendService.updateUserLevelAndCoins(turkish.getId(), 25);
            }

            backendService.enterTournament(newTurkishuserR.getId());

            List<Pair<User.Country,Integer>> updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> updateFranceLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
            List<Pair<User.Country,Integer>> updateUsaLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
            List<Pair<User.Country,Integer>> updateUkLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
            List<Pair<User.Country,Integer>> updateGermanyLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());

            assertTrue(updateTurkeyLeaderBoard.size() == 5);
            assertTrue(updateFranceLeaderBoard.size() == 5);
            assertTrue(updateUsaLeaderBoard.size() == 5);
            assertTrue(updateUkLeaderBoard.size() == 5);
            assertTrue(updateGermanyLeaderBoard.size() == 5);
            assertTrue(updateTurkeyLeaderBoard.get(0).getValue0().equals(User.Country.TURKEY));

            for (int i = 0; i < 50; i++) {
                backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
            }
            // Nuvo turk isn't in a tournament that has begun yet so updating their level won't change the position of the country leader board.
            updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            assertTrue(updateTurkeyLeaderBoard.get(0).getValue0().equals(User.Country.TURKEY));
       });
    } 

    @Test
    public void TestCountryLeaderboardTwo() {
        User kemal = new User("Kemal", User.Country.TURKEY);
        User ayse = new User("Ayse", User.Country.TURKEY);
        User omer = new User("Omer", User.Country.TURKEY);
        User kubilay = new User("Kubilay", User.Country.TURKEY);
        User merve = new User("Merve", User.Country.TURKEY);

        User kemalR = userRepository.save(kemal);
        User asyeR = userRepository.save(ayse);
        User omerR = userRepository.save(omer);
        User kubilayR = userRepository.save(kubilay);
        User merveR = userRepository.save(merve);

        User charles  = new User("Charles", User.Country.UK);
        User xavier = new User("Xavier", User.Country.UK);
        User henry = new User("Henry", User.Country.UK);
        User susan = new User("Susan", User.Country.UK);
        User Elizabeth = new User("Elizabeth", User.Country.UK);

        User charlesR = userRepository.save(charles);
        User xavierR = userRepository.save(xavier);
        User henryR = userRepository.save(henry);
        User susanR = userRepository.save(susan);
        User elizabethR = userRepository.save(Elizabeth);

        assertDoesNotThrow(()->{
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(kemal.getId(), 25);
                backendService.updateUserLevelAndCoins(ayse.getId(), 25);
                backendService.updateUserLevelAndCoins(omer.getId(), 25);
                backendService.updateUserLevelAndCoins(kubilay.getId(), 25);
                backendService.updateUserLevelAndCoins(merve.getId(), 25);
    
                backendService.updateUserLevelAndCoins(charles.getId(), 25);
                backendService.updateUserLevelAndCoins(xavier.getId(), 25);
                backendService.updateUserLevelAndCoins(henry.getId(), 25);
                backendService.updateUserLevelAndCoins(susan.getId(), 25);
                backendService.updateUserLevelAndCoins(Elizabeth.getId(), 25);
            }
        });
    }

    @Test
    public void TestGetGroupRankOne() {
        assertDoesNotThrow(()-> {
            tournamentScheduler.startLocalTimeTournament();
            User user = backendService.createUser("user").get();
            assertDoesNotThrow(() -> {
                Long tournamentId = tournamentService.getCurrentTournamentId().get();
                assertThrows(EntityNotFoundException.class, () -> {
                    backendService.getGroupRank(user.getId(), tournamentId);
                });
            });
        });
        
    }

    @Test
    public void TestGetGroupRankTwo() {
        assertDoesNotThrow(()-> {
            tournamentScheduler.startLocalTimeTournament();
            User user = backendService.createUser("user").get();
            assertDoesNotThrow(() -> {
                Long tournamentId = tournamentService.getCurrentTournamentId().get();
                assertThrows(EntityNotFoundException.class, () -> {
                    backendService.getGroupRank(user.getId(), tournamentId);
                });
            });
        });
    }


    @Test
    public void TestGetGroupRankThree() {
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

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(turkish.getId(), 25);
                backendService.updateUserLevelAndCoins(french.getId(), 25);
                backendService.updateUserLevelAndCoins(american.getId(), 25);
                backendService.updateUserLevelAndCoins(british.getId(), 25);
                backendService.updateUserLevelAndCoins(german.getId(), 25);
            }
        });

        assertDoesNotThrow( () -> {
            GroupLeaderBoard turksGroup = backendService.enterTournament(turkishR.getId());
            GroupLeaderBoard frenchmansGroup = backendService.enterTournament(frenchR.getId());
            GroupLeaderBoard americansGroup = backendService.enterTournament(americanR.getId());
            GroupLeaderBoard britsGroup = backendService.enterTournament(britishR.getId());
            GroupLeaderBoard germansGroup = backendService.enterTournament(germanR.getId());


            Tournament currTournament = backendService.getCurrentTournament();
            List<Pair<User.Country,Integer>> turkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> franceLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> usaLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> ukLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            List<Pair<User.Country,Integer>> germanyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());

            assertTrue(turkeyLeaderBoard.size() == 5);
            assertTrue(franceLeaderBoard.size() == 5);
            assertTrue(usaLeaderBoard.size() == 5);
            assertTrue(ukLeaderBoard.size() == 5);
            assertTrue(germanyLeaderBoard.size() == 5);
            
            User newTurkishUser = new User("NuvoTurk", User.Country.TURKEY); 

            User newTurkishuserR = userRepository.save(newTurkishUser);

            for (int i = 0; i < 19; i++) {
                backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
            }

            backendService.enterTournament(newTurkishuserR.getId());

            List<Pair<User.Country,Integer>> updateTurkeyLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
            List<Pair<User.Country,Integer>> updateFranceLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
            List<Pair<User.Country,Integer>> updateUsaLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
            List<Pair<User.Country,Integer>> updateUkLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());
            List<Pair<User.Country,Integer>> updateGermanyLeaderBoard = backendService.getCountryLeaderboard( currTournament.getId());

            assertTrue(updateTurkeyLeaderBoard.size() == 5);
            assertTrue(updateFranceLeaderBoard.size() == 5);
            assertTrue(updateUsaLeaderBoard.size() == 5);
            assertTrue(updateUkLeaderBoard.size() == 5);
            assertTrue(updateGermanyLeaderBoard.size() == 5);

            
            backendService.updateUserLevelAndCoins(turkish.getId(), 25);
            int turkishRank = backendService.getGroupRank(turkish.getId(), currTournament.getId());
            assertTrue(turkishRank == 1);
            
            assertThrows(TournamentGroupHasNotBegunException.class, () -> {
                backendService.getGroupRank(newTurkishuserR.getId(), currTournament.getId());
            });

            for (int i = 0; i < 50; i++) {
                backendService.updateUserLevelAndCoins(newTurkishuserR.getId(), 25);
            }
            // Nuvo turk isn't in a tournament that has begun yet so updating their level won't change their position in the country leader board.
            updateTurkeyLeaderBoard = backendService.getCountryLeaderboard(currTournament.getId());
            assertThrows(TournamentGroupHasNotBegunException.class, () -> {
                backendService.getGroupRank(newTurkishuserR.getId(), currTournament.getId());
            });
        });
    }


    @Test
    public void TestWin() {
        Tournament tournament = tournamentScheduler.startLocalTimeTournament();
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


        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(turkish.getId(), 25);
                backendService.updateUserLevelAndCoins(french.getId(), 25);
                backendService.updateUserLevelAndCoins(american.getId(), 25);
                backendService.updateUserLevelAndCoins(british.getId(), 25);
                backendService.updateUserLevelAndCoins(german.getId(), 25);
            }
        });


        assertDoesNotThrow( () -> {
            backendService.enterTournament(turkishR.getId());
            backendService.enterTournament(frenchR.getId());
            backendService.enterTournament(americanR.getId());
            backendService.enterTournament(britishR.getId());
            backendService.enterTournament(germanR.getId());

            for (int i = 0; i < 2; i++) {
                backendService.updateUserLevelAndCoins(german.getId(), 25); // the german is ahead by one
            }
            for (int i = 0; i < 1; i++) {
                backendService.updateUserLevelAndCoins(turkish.getId(), 25); // the turk gets runner up 
            }

            int germanCoinsBeforeWin = userService.getUser(german.getId()).getCoins();
            int turksihCoinsBeforeWind = userService.getUser(turkish.getId()).getCoins();
            int frenchCoinsBeforeClaim = userService.getUser(french.getId()).getCoins();

            tournamentScheduler.endTournament(tournament.getId());
           
            backendService.claimReward(german.getId(), tournament.getId());
            backendService.claimReward(turkish.getId(), tournament.getId());
            backendService.claimReward(french.getId(), tournament.getId());

            int turksihCoinsAfterWin = userService.getUser(turkish.getId()).getCoins();
            int germanCoinsAfterWin = userService.getUser(german.getId()).getCoins();
            int frenchCoinsAfterClaim = userService.getUser(french.getId()).getCoins();

            assertTrue(germanCoinsAfterWin - germanCoinsBeforeWin == 10000);
            assertTrue(turksihCoinsAfterWin - turksihCoinsBeforeWind == 5000);
            assertTrue(frenchCoinsAfterClaim - frenchCoinsBeforeClaim == 0);
        });
    }

    @Test
    public void TestCantEnterNewTournamentWithoutCollectingReward() {
        Tournament tournament = tournamentScheduler.startLocalTimeTournament();
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

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) { // Level up to 20
                backendService.updateUserLevelAndCoins(turkish.getId(), 25);
                backendService.updateUserLevelAndCoins(french.getId(), 25);
                backendService.updateUserLevelAndCoins(american.getId(), 25);
                backendService.updateUserLevelAndCoins(british.getId(), 25);
                backendService.updateUserLevelAndCoins(german.getId(), 25);
            }
         });


        assertDoesNotThrow( () -> {
            backendService.enterTournament(turkishR.getId());
            backendService.enterTournament(frenchR.getId());
            backendService.enterTournament(americanR.getId());
            backendService.enterTournament(britishR.getId());
            backendService.enterTournament(germanR.getId());

            for (int i = 0; i < 2; i++) {
                backendService.updateUserLevelAndCoins(german.getId(), 25); // the german is ahead by one
            }
            for (int i = 0; i < 1; i++) {
                backendService.updateUserLevelAndCoins(turkish.getId(), 25); // the turk gets runner up 
            }

            assertThrows(AlreadyInCurrentTournamentException.class, () -> {
                backendService.enterTournament(german.getId());
            });

            tournamentService.endTournament(tournament.getId());

            assertThrows(NoSuchTournamentException.class, () -> {
                backendService.enterTournament(german.getId());
            });

            Tournament newTournament = tournamentScheduler.startLocalTimeTournament();

            assertThrows(HasUnclaimedRewards.class, () -> {
                backendService.enterTournament(german.getId());
            });
            assertThrows(HasUnclaimedRewards.class, () -> {
                backendService.enterTournament(turkish.getId());
            });
            assertThrows(HasUnclaimedRewards.class, () -> {
                backendService.enterTournament(french.getId());
            });

            backendService.claimReward(german.getId(), tournament.getId());
            backendService.claimReward(turkish.getId(), tournament.getId());
            backendService.claimReward(french.getId(),tournament.getId());

            assertDoesNotThrow(() -> {
                backendService.enterTournament(german.getId());
            });
            assertDoesNotThrow(() -> {
                backendService.enterTournament(turkish.getId());
            });
            assertDoesNotThrow(() -> {
                backendService.enterTournament(french.getId());
            });


            assertThrows(AlreadyInCurrentTournamentException.class, () -> {
                backendService.enterTournament(german.getId());
            });
            assertThrows(AlreadyInCurrentTournamentException.class, () -> {
                backendService.enterTournament(turkish.getId());
            });
            assertThrows(AlreadyInCurrentTournamentException.class, () -> {
                backendService.enterTournament(french.getId());
            });

        }); 
    }

} 