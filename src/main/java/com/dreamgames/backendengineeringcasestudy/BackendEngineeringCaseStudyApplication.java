package com.dreamgames.backendengineeringcasestudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "com.dreamgames.backendengineeringcasestudy.userservice.repository",
    "com.dreamgames.backendengineeringcasestudy.tournamentservice.repository"
})
@EntityScan(basePackages = {
    "com.dreamgames.backendengineeringcasestudy.userservice.model",
    "com.dreamgames.backendengineeringcasestudy.tournamentservice.model"
})

@EnableCaching
public class BackendEngineeringCaseStudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendEngineeringCaseStudyApplication.class, args);
		System.out.println("**********************************************************************");
		System.out.println("************* Server is started. Listening port 8080 ... *************");
		System.out.println("**********************************************************************");
	}
}
