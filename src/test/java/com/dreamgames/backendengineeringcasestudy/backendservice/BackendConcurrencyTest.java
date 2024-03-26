package com.dreamgames.backendengineeringcasestudy.backendservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dreamgames.backendengineeringcasestudy.backendservice.BackendService;
import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
public class BackendConcurrencyTest {

    @Autowired
    private BackendService backendService;

    private static final Logger logger = LoggerFactory.getLogger(BackendConcurrencyTest.class);

    @Test
    public void testConcurrentUserCreation() throws InterruptedException {
        final int numberOfUsers = 500;
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
        final int numberOfUsers = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        AtomicInteger successCounter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfUsers; i++) {
            executorService.submit(() -> {
                assertDoesNotThrow(() -> {
                    User user = backendService.createUser("testUser" + Thread.currentThread().getId()).get();
                    assertThrows(LevelNotHighEnoughException.class, () -> {
                        backendService.enterTournament(user.getId());
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
}
