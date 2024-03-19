package com.dreamgames.backendengineeringcasestudy.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private UserRepository userRepository; 

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username) {  
        User user = new User();
        user.setUsername(username);
        user.setCountry(User.Country.getRandomCountry());
        return userRepository.save(user);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException());
    }

    public User updateUserLevelAndCoins(Long userId, int cointsToAdd) throws EntityNotFoundException {  //TODO needs to update users entries scores
        User user = getUser(userId); 
        user.setLevel(user.getLevel() + 1); 
        user.setCoins(user.getCoins() + cointsToAdd);
        // user.increaseEntryScore();
        return userRepository.save(user);
    }
}
