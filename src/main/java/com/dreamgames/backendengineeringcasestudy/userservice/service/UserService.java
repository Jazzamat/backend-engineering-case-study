package com.dreamgames.backendengineeringcasestudy.userservice.service;

import java.util.concurrent.CompletableFuture;

import org.assertj.core.api.CompletableFutureAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ExceptionCollector;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {

    private UserRepository userRepository; 

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user with the given username.
     * 
     * @param username The username of the user.
     * @return The created user.
     */
    public User createUser(String username) {  
        User user = new User();
        user.setUsername(username);
        user.setCountry(User.Country.getRandomCountry());
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param userId The ID of the user.
     * @return The retrieved user.
     * @throws EntityNotFoundException If the user with the given ID does not exist.
     */
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException());
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    /**
     * Retrieves the country of a user.
     * 
     * @param userId The ID of the user.
     * @return The country of the user.
     */
    public User.Country retreiveUsersCountry(Long userId) throws Exception {
        User user = getUser(userId);
        return user.getCountry();
    }

    /**
     * Updates the level and coins of a user.
     * 
     * @param userId The ID of the user.
     * @param coinsToAdd The number of coins to add to the user's current coins.
     * @return The updated user.
     * @throws EntityNotFoundException If the user with the given ID does not exist.
     */
    public User updateUserLevelAndCoins(Long userId, int coinsToAdd) throws EntityNotFoundException, Exception { 
        User user = getUser(userId); 
        user.updateLevelAndCoins(coinsToAdd);
        return userRepository.save(user);
    } 

    /**
     * Claims a reward for a user.
     * 
     * @param userId The ID of the user.
     * @param rank The rank of the reward to claim.
     * @return The updated user.
     */
    public User claimReward(Long userId, int rank) throws Exception {
        User user = getUser(userId);
        user.claimReward(rank);
        return userRepository.save(user);
    } 
}
