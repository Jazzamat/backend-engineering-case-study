package com.dreamgames.backendengineeringcasestudy.backendservice;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class BackendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
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
    public void testUpdateUserLevelAndCoins() throws Exception {
        // Create a user to update
        String username = "levelUpUser";
        String responseBody = mockMvc.perform(post("/backend/users")
                .param("username", username))
                .andReturn().getResponse().getContentAsString();
        Long userId = Long.parseLong(responseBody.split(",")[0].split(":")[1].trim());

        // Update user's level and coins
        mockMvc.perform(put("/backend/users/updateLevel")
                .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level", greaterThan(1)))
                .andExpect(jsonPath("$.coins", greaterThan(5000)));
    }

    @Test
    public void testEnterTournamentImmediately() throws Exception {
        // Assuming creating a user and entering a tournament would be a valid sequence
        String username = "tournamentUser";
        String responseBody = mockMvc.perform(post("/backend/users")
                .param("username", username))
                .andReturn().getResponse().getContentAsString();
        Long userId = Long.parseLong(responseBody.split(",")[0].split(":")[1].trim());

        mockMvc.perform(post("/backend/tournaments/enter")
                .param("userId", String.valueOf(userId)))
                .andExpect(status().isBadRequest());
                // Validate specific fields if required
    }

    @Test
    public void testClaimReward() throws Exception {
      
    }

    @Test
    public void testGetGroupRank() throws Exception {
      
    }

    @Test
    public void testGetGroupLeaderboard() throws Exception {
      
    }

    @Test
    public void testGetCountryLeaderboard() throws Exception {
      
    }
}
