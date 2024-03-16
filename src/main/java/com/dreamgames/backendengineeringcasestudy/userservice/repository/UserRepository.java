package com.dreamgames.backendengineeringcasestudy.userservice.repository;


import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long>{
}
