package com.dreamgames.backendengineeringcasestudy.backendservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.dreamgames.backendengineeringcasestudy.backendservice.BackendService;
import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentEntryRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentScheduler;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
public class BackendConcurrencyTest {

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

    private static final Logger logger = LoggerFactory.getLogger(BackendConcurrencyTest.class);

    @Test
    public void testConcurrentUserCreation() throws InterruptedException {
        final int numberOfUsers = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        AtomicInteger successCounter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfUsers; i++) {
            executorService.submit(() -> {
                assertDoesNotThrow(() -> {
                    User user = backendService.createUser("testUser" + Thread.currentThread().getId()).get();
                    logger.info("User created: {}", user.getUsername());
                });
                successCounter.incrementAndGet();
            });
        }
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        if (finished) {
            // logger.info("All threads completed. Time taken: {} ms", (endTime - startTime));
            System.out.println("#################### All threads completed. Time taken: " + (endTime - startTime) + " ms");
        } else {
            logger.warn("Executor did not terminate in the specified time.");
        }
    }

    @Test
    public void testConcurrentTournamentEntry() throws InterruptedException {
        final int numberOfUsers = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        AtomicInteger successCounter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfUsers; i++) {
            executorService.submit(() -> {
                assertDoesNotThrow(() -> {
                    User user = backendService.createUser("testUser" + Thread.currentThread().getId()).get();
                    assertThrows(LevelNotHighEnoughException.class, () -> {
                        backendService.enterTournamentAsyncWrapper(user.getId());
                    });
                });
                successCounter.incrementAndGet();
            });
        }

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        if (finished) {
            logger.info("All threads completed. Time taken: {} ms", (endTime - startTime));
        } else {
            logger.warn("Executor did not terminate in the specified time.");
        }
    }



    @Test
public void testConcurrentTournamentEntryWithLevelRequirement() throws InterruptedException {
    final int numberOfUsers = 50; // Number of users attempting to enter
    ExecutorService executor = Executors.newFixedThreadPool(10); // Adjust based on resources

    try {
        Long tournamentId = tournamentService.getCurrentTournamentId();
    } catch (Exception e) {
    }

    AtomicInteger successCounter = new AtomicInteger(0);
    AtomicInteger levelNotHighEnoughCounter = new AtomicInteger(0);

    for (int i = 0; i < numberOfUsers; i++) {
        executor.submit(() -> {
            try {
                // Create a new user
                User user = backendService.createUser("tournamentUser" + System.nanoTime()).join();

                // Increment user level to meet entry requirement (assuming level increment per method call)
                for (int j = 0; j < 20; j++) { // Assuming each call increments level by 1
                    backendService.updateUserLevelAndCoinsAsyncWrapper(user.getId(), 5).join(); // Assume some coins are also added
                }

                // Attempt to enter the tournament
                try {
                    backendService.enterTournamentAsyncWrapper(user.getId()).join();
                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    if (e.getCause() instanceof LevelNotHighEnoughException) {
                        levelNotHighEnoughCounter.incrementAndGet();
                    } else {
                        throw e; // Rethrow if it's not a level issue
                    }
                }
            } catch (Exception e) {
                logger.error("Error during user creation or tournament entry", e);
            }
        });
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(2, TimeUnit.MINUTES), "Executor did not terminate in the expected timeframe.");

    assertEquals(0, levelNotHighEnoughCounter.get(), "Some users did not have high enough levels to enter the tournament.");
    assertEquals(numberOfUsers, successCounter.get(), "Not all users could enter the tournament.");
}

}
