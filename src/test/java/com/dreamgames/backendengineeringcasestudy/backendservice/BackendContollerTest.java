package com.dreamgames.backendengineeringcasestudy.backendservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@SpringBootTest
@AutoConfigureMockMvc
public class BackendContollerTest {
    
    
    @Autowired
    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    public void testCreateUser() throws Exception {
        String username = "testUser";
        
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("backend/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(username)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value('"' + username + '"'))
        .andReturn();
        // Add more assertions as needed
        
        // Convert JSON response to map
        String contentAsString = result.getResponse().getContentAsString();
        HashMap<String, Object> userMap = objectMapper.readValue(contentAsString, new TypeReference<HashMap<String, Object>>() {});
        
        // Extract the user ID
        Object userId = userMap.get("id");
        
        // Assertions
        assertNotNull(userId, "User ID should not be null");
        System.out.println("Created user ID: " + userId);
    }
    
    @Test
    public void testUpdateUserLevelAndCoins() throws Exception {
        
        String username = "testUser";
        
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(username)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value('"' + username + '"'))
        .andReturn();
        // Add more assertions as needed
        
        // Convert JSON response to map
        String contentAsString = result.getResponse().getContentAsString();
        HashMap<String, Object> userMap = objectMapper.readValue(contentAsString, new TypeReference<HashMap<String, Object>>() {});
        
        // Extract the user ID
        Integer userIdInt = (Integer) userMap.get("id");
        
        Long userId = (Long) Long.valueOf(userIdInt);
        
        mockMvc.perform(MockMvcRequestBuilders.put("/users/" + userId + "/level")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.level").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$.coins").value(5025));
        // Add more assertions to validate the updated fields
    }
}

