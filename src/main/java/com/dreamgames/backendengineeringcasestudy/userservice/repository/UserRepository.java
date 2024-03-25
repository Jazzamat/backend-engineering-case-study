/**
 * The UserRepository interface is responsible for providing CRUD operations for the User entity.
 * It extends the JpaRepository interface, which provides generic CRUD operations for entities.
 * The User entity is the entity class and Long is the type of the primary key.
 */
package com.dreamgames.backendengineeringcasestudy.userservice.repository;


import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User,Long>{
}
