package com.dreamgames.backendengineeringcasestudy.tournamentservice.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;
import com.dreamgames.backendengineeringcasestudy.userservice.model.User.Country;


/**
 * Repository interface for managing TournamentEntry entities.
 * Provides methods for querying and manipulating TournamentEntry data in the database.
 * 
 * @author E. Omer Gul
 */
@Repository
public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {


    @Query("SELECT te FROM TournamentEntry te WHERE te.user.id = :userId")
    Optional<TournamentEntry> findByUserId(@Param("userId") Long userId); 

    /**
     * Checks if a TournamentEntry exists for a given user and tournament.
     *
     * @param userId       the ID of the user
     * @param tournamentId the ID of the tournament
     * @return true if a TournamentEntry exists, false otherwise
     */
    @Query("SELECT COUNT(te) > 0 FROM TournamentEntry te WHERE te.user.id = :userId AND te.tournamentGroup.tournament.id = :tournamentId")
    boolean existsByUserIdAndTournamentId(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);

    /**
     * Retrieves a TournamentEntry for a given user and tournament.
     *
     * @param userId       the ID of the user
     * @param tournamentId the ID of the tournament
     * @return an Optional containing the TournamentEntry, or an empty Optional if not found
     */
    @Query("SELECT te FROM TournamentEntry te WHERE te.user.id = :userId AND te.tournamentGroup.tournament.id = :tournamentId")
    Optional<TournamentEntry> findByUserIdAndTournamentId(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);

    /**
     * Retrieves a list of TournamentEntries for a given tournament group, ordered by score in descending order.
     *
     * @param groupId the ID of the tournament group
     * @return a list of TournamentEntries
     */
    List<TournamentEntry> findByTournamentGroupIdOrderByScoreDesc(Long groupId);

    /**
     * Retrieves a list of TournamentEntries for a given country and tournament, ordered by score in descending order.
     *
     * @param country      the country of the user
     * @param tournamentId the ID of the tournament
     * @return a list of TournamentEntries
     */
    @Query("SELECT te FROM TournamentEntry te WHERE te.user.country = :country AND te.tournamentGroup.tournament.id = :tournamentId ORDER BY te.score DESC")
    List<TournamentEntry> findByCountryAndTournamentIdOrderedByScoreDesc(@Param("country") Country country, @Param("tournamentId") Long tournamentId);

    /**
     * Checks if a TournamentEntry exists for a given user with an unclaimed reward.
     *
     * @param userId the ID of the user
     * @return true if a TournamentEntry exists with an unclaimed reward, false otherwise
     */
    @Query("SELECT COUNT(te) > 0 FROM TournamentEntry te WHERE te.user.id = :userId AND te.rewardClaimed = false")
    boolean existsByUserIdAndUnclaimedReward(@Param("userId") Long userId);
}