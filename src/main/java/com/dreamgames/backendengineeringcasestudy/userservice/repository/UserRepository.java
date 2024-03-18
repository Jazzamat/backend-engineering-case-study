package com.dreamgames.backendengineeringcasestudy.userservice.repository;


import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{
}
