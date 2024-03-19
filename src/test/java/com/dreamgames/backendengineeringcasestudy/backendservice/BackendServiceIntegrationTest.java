package com.dreamgames.backendengineeringcasestudy.backendservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class BackendServiceIntegrationTest {
   
    @Autowired
    private BackendService backendService;

    @Test
    public void testCreateUser() {
       backendService.integrationTestMethod();
    }
}
