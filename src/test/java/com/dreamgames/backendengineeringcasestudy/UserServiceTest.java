package com.dreamgames.backendengineeringcasestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.ignoreStubs;

import org.awaitility.reflect.exception.FieldNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
public class UserServiceTest {
   
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void startService() { 
        this.userService = new UserService(userRepository);
    }

    @Test
    public void basicCreateUserTestOne() {
        User testUser = userService.createUser("testUser");
        User gottenUser = userService.getUser(testUser.getId());
        assertTrue(testUser.equals(gottenUser));
    }

    @Test
    public void basicCreateUserTestTwo() {
        User testUser = userService.createUser("testUser");
        User testUser2 = userService.createUser("testUser2");

        User gottenUser1 = userService.getUser(testUser.getId());
        User gottenUser2 = userService.getUser(testUser2.getId());

        assertFalse(gottenUser1.equals(gottenUser2));
        assertFalse(testUser.equals(gottenUser2));
        assertFalse(testUser2.equals(gottenUser1));
        assertTrue(testUser2.equals(gottenUser2)); 
        assertEquals(testUser, gottenUser1);
    }

    @Test
    public void basicUserLevelUpdateTestOne() {
        User testUser = userService.createUser("testUser");
        assertEquals(testUser.getCoins(), 5000);
        assertEquals(testUser.getLevel(), 1);

        User updateUser = userService.updateUserLevelAndCoins(testUser.getId(), 25);

        assertEquals(updateUser.getLevel(), 2);
        assertEquals(updateUser.getCoins(), 5025);
    }

}
