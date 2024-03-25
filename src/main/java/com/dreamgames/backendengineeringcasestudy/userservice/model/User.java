package com.dreamgames.backendengineeringcasestudy.userservice.model;

import java.util.Random;

import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

/**
 * Represents a user for this application.
 * 
 * This class represents a user in the application. It contains information such as the user's ID, username, coins, level, and country.
 * Users can deduct fees, claim rewards, and update their level and coins.
 * 
 * The User class also defines an inner enum class called Country, which represents the country of the user.
 * 
 * 
 * This class is annotated with the @Entity annotation, indicating that it is a persistent entity in the database.
 * 
 * @author E. Omer Gul 
 */
@OptimisticLocking(type=OptimisticLockType.ALL)
@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String username;
	private int coins = 5000;
	private int level = 1;
	private static final int TOURNAMENT_ENTRY_FEE = 1000;
    private static final int FIRST_PLACE_WIN = 10000;
    private static final int SECOND_PLACE_WIN = 5000;

	@Version
	private Long version;

	@Enumerated(EnumType.STRING)
	private Country country;

	public static enum Country {
		TURKEY, USA, UK, FRANCE, GERMANY;
		
		/**
		 * Returns a random country from the available countries.
		 * 
		 * @return a random country
		 */
		public static Country getRandomCountry() {
			Random random = new Random();
			return values()[random.nextInt(values().length)];
		}
	}

	public User() {
	}

	/**
	 * Constructs a new User object with the specified username and country.
	 * 
	 * @param username the username of the user
	 * @param country the country of the user
	 */
	public User(String username, Country country) {
		this.username = username;
		this.country = country;
	}

	/**
	 * Deducts a fee of 1000 coins from the user's balance.
	 * 
	 * @precondition coins >= 1000
	 * @postcondition coins >= 0
	 */
	public void deductFee() {
		this.coins = this.coins - TOURNAMENT_ENTRY_FEE;
	}

	/**
	 * Claims a reward based on the user's rank.
	 * If the rank is 1, the user receives a reward of FIRST_PLACE_WIN coins.
	 * If the rank is 2, the user receives a reward of SECOND_PLACE_WIN coins.
	 * 
	 * @param rank the rank of the user
	 */
	public void claimReward(int rank) {
		if (rank == 1) {
            setCoins(coins + FIRST_PLACE_WIN);
        } else if (rank == 2) {
			setCoins(coins + SECOND_PLACE_WIN);
        }
	}

	/**
	 * Updates the user's level and coins by the specified amount.
	 * The level is incremented by 1, and the coins are increased by the specified amount.
	 * 
	 * @param coinsToAdd the amount of coins to add
	 */
	public void updateLevelAndCoins(int coinsToAdd) {
		setLevel(getLevel() + 1); 
        setCoins(getCoins() + coinsToAdd);
	}

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (coins != other.coins)
			return false;
		if (level != other.level)
		return false;
		if (country != other.country)
			return false;
		return true;
	}
}
