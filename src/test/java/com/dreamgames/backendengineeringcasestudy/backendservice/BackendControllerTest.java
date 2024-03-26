package com.dreamgames.backendengineeringcasestudy.backendservice;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
public class BackendControllerTest {

    @Autowired
    private MockMvc mockMvc;

     @BeforeEach
     public void cleanDB() {

     }

    @Test
    @Transactional
    public void testCreateUser() throws Exception {
        mockMvc.perform(post("/backend/users")
                .param("username", "testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testUser")))
                .andExpect(jsonPath("$.coins", is(5000)))
                .andExpect(jsonPath("$.level", is(1)))
                .andExpect(jsonPath("$.country", not(emptyOrNullString())));
    }

    @Test
    @Transactional
    public void testUpdateUserLevelAndCoins() throws Exception {
        String username = "levelUpUser";
        String responseBody = mockMvc.perform(post("/backend/users")
                .param("username", username))
                .andReturn().getResponse().getContentAsString();
        Long userId = Long.parseLong(responseBody.split(",")[0].split(":")[1].trim());

        mockMvc.perform(put("/backend/users/updateLevel")
                .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level", greaterThan(1)))
                .andExpect(jsonPath("$.coins", greaterThan(5000)));
    }

    @Test
    @Transactional
    public void testEnterTournamentImmediately() throws Exception {
        String username = "tournamentUser";
        String responseBody = mockMvc.perform(post("/backend/users")
                .param("username", username))
                .andReturn().getResponse().getContentAsString();
        Long userId = Long.parseLong(responseBody.split(",")[0].split(":")[1].trim());

        mockMvc.perform(post("/backend/tournaments/enter")
                .param("userId", String.valueOf(userId)))
                .andExpect(status().isBadRequest());
    }


    @Test 
    @Transactional
    public void TestCountryLeaderboardOne() {
     
    }

    @Test
    @Transactional
    public void testClaimReward() throws Exception {
         // TODO 
    }

    @Test
    @Transactional
    public void testGetGroupRank() throws Exception {
         // TODO 
    }

    @Test
    @Transactional
    public void testGetGroupLeaderboard() throws Exception {
         // TODO 
      
    }

    @Test
    @Transactional
    public void testGetCountryLeaderboard() throws Exception {
         // TODO 
    }
}
