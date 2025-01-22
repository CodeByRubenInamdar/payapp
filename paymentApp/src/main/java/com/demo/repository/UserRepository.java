package com.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.demo.model.Transaction;
import com.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	   boolean existsByEmail(String email);
	    boolean existsByPhoneNumber(String phoneNumber);
	    Optional<User> findByPhoneNumber(String phoneNumber);
	    Optional<User> findByEmail(String email);
	    List<User> findByPhoneNumberContaining(String phoneNumber);
	    List<User> findByNameContainingIgnoreCase(String name);
		User findByName(String name);
		Optional<User> findByResetToken(String token);
		
	
}
