package com.dreamgames.backendengineeringcasestudy.tournamentservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.service.TournamentService;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class TournamentServiceIntegrationTest {
   
    @Autowired
    private TournamentService tournamentService;

    @Test
    @Transactional
    public void testCreateUser() {
       tournamentService.integrationTestMethod();
    }
}
