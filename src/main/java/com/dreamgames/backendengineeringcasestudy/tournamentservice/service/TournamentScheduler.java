package com.dreamgames.backendengineeringcasestudy.tournamentservice.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dreamgames.backendengineeringcasestudy.exceptions.NoSuchTournamentException;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;

/**
 * This class represents a scheduler for starting new tournaments.
 * It is responsible for scheduling the start of new tournaments based on a specified cron expression.
 * The scheduler also handles caching and eviction of tournament data.
 * 
 * @author E. Omer Gul
 */
@Component
public class TournamentScheduler {
    @Autowired
    private TournamentService tournamentService;
    
    /**
     * Constructs a new TournamentScheduler with the specified TournamentService.
     * 
     * @param tournamentService the TournamentService to be used by the scheduler
     */
    public TournamentScheduler(TournamentService tournamentService) {
		this.tournamentService = tournamentService;
	}

    /**
     * Scheduled method that starts a new tournament.
     * This method is triggered every day at 00:00 UTC.
     * It evicts the cache for the current tournament and creates a new tournament if one does not exist.
     * 
     * @return the newly created tournament, or null if a tournament already exists
     */
	@Scheduled(cron = "0 0 0 * * *", zone = "UTC") // Every day at 00:00 UTC
    @CacheEvict(value = "currentTournament", allEntries = true)
    public Tournament startNewTournament() {
        try {
            tournamentService.getCurrentTournament();
        } catch (NoSuchTournamentException e) {
            return tournamentService.createTournament(LocalDateTime.now(ZoneOffset.UTC), 
                                        LocalDateTime.now(ZoneOffset.UTC).withHour(20)); 
        }
        return null;
    }

    // ========= DEV METHODS ========= // 

    /**
     * Method that starts a new tournament based on the local time.
     * It evicts the cache for the current tournament and creates a new tournament if one does not exist.
     * 
     * @return the newly created tournament, or null if a tournament already exists
     */
    public Tournament startLocalTimeTournament() {
        try {
            tournamentService.getCurrentTournament();
        } catch (NoSuchTournamentException e) {
            return tournamentService.createTournament(LocalDateTime.now(), 
                                        LocalDateTime.now().withHour(23).withMinute(59)); 
        }
        return null;
    }

    public void endTournament(Long tournamentId) {
        try {
            tournamentService.endTournament(tournamentId);
        } catch (NoSuchTournamentException e) {
            System.out.println("No such tournament to end"); 
        }
    }
}
