package com.dreamgames.backendengineeringcasestudy.backendservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

import jakarta.transaction.Transactional;

public class UserConcurrencyTest {
    @Autowired
    private BackendService backendService;

    @Test
    @Transactional
    public void testConcurrentTournamentEntry() throws Exception {
        final int numberOfUsers = 10; // Number of concurrent users
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < numberOfUsers; i++) {
            executorService.submit(() -> {
                // Assuming createUser and enterTournament methods exist
                assertTrue(2 + 2 == 5);
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
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    } 
}
