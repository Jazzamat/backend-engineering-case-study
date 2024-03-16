package com.dreamgames.backendengineeringcasestudy.userservice.controller;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import com.dreamgames.backendengineeringcasestudy.userservice.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
   
    private final int COINS_PER_LEVEL = 25;
    
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody String username) {
        return ResponseEntity.ok(userService.createUser(username));
    }

    @PutMapping("/{userId}/level")
    public ResponseEntity<User> updateUserLevelAndCoins(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.updateUserLevelAndCoins(userId, COINS_PER_LEVEL));
    }
    
}
