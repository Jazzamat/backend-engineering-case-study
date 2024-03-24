package com.dreamgames.backendengineeringcasestudy.backendservice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.event.TransactionalEventListener;

import com.dreamgames.backendengineeringcasestudy.exceptions.LevelNotHighEnoughException;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class TournamentConcurrencyTest {

    @Autowired
    private BackendService backendService;

    @Test
    @Transactional
    public void testConcurrentTournamentEntry() throws InterruptedException {
        final int numberOfUsers = 10; // Number of concurrent users
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < numberOfUsers; i++) {
            executorService.submit(() -> {
                // Assuming createUser and enterTournament methods exist
                User user = backendService.createUser("testUser" + Thread.currentThread().getId());
                assertThrows(LevelNotHighEnoughException.class, () -> {
                    backendService.enterTournament(user.getId());
                });
                successCounter.incrementAndGet();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }
}
