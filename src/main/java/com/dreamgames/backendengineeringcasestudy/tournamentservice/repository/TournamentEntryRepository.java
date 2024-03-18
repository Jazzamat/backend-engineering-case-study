package com.dreamgames.backendengineeringcasestudy.tournamentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;

@Repository
public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {
    //  @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM tournament_entry te " +
    //         "INNER JOIN tournament_group tg ON te.tournament_group_id = tg.id " +
    //         "WHERE te.user_id = :userId AND tg.tournament_id = :tournamentId", nativeQuery = true)
    // boolean existsByUserIdAndTournamentId(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);


    @Query("SELECT COUNT(te) > 0 FROM TournamentEntry te WHERE te.user.id = :userId AND te.tournamentGroup.tournament.id = :tournamentId")
    boolean existsByUserIdAndTournamentId(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);
}