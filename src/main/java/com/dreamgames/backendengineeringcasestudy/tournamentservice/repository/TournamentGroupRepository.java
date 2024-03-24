
package com.dreamgames.backendengineeringcasestudy.tournamentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentGroup;


/**
 * This interface represents the repository for TournamentGroup entities.
 * It extends the JpaRepository interface, providing CRUD operations for TournamentGroup entities.
 * 
 * @author E. Omer Gul
 */
@Repository
public interface TournamentGroupRepository extends JpaRepository<TournamentGroup,Long> {
}