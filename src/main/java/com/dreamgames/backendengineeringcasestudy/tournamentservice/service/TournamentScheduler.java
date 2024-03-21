package com.dreamgames.backendengineeringcasestudy.tournamentservice.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;


@Component
public class TournamentScheduler {
    @Autowired
    private TournamentService tournamentService;
    
    public TournamentScheduler(TournamentService tournamentService) {
		this.tournamentService = tournamentService;
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "UTC") // Every day at 00:00 UTC
    @CacheEvict(value = "currentTournament", allEntries = true)
    public Tournament startNewTournament() {
        try {
            tournamentService.getCurrentTournament();
        } catch (RuntimeException e) {
            return tournamentService.createTournament(LocalDateTime.now(ZoneOffset.UTC), 
                                        LocalDateTime.now(ZoneOffset.UTC).withHour(20)); 
        }
        return null;
    }


    public Tournament startLocalTimeTournament() {
        try {
            tournamentService.getCurrentTournament();
        } catch (RuntimeException e) {
            return tournamentService.createTournament(LocalDateTime.now(), 
                                        LocalDateTime.now().withHour(23)); 
        }
        return null;
    }


}
