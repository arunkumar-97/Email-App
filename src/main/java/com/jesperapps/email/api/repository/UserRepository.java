package com.jesperapps.email.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jesperapps.email.api.model.User;

@Repository 
public interface UserRepository extends JpaRepository<User, Integer>{

	List<User> findAllByUserName(String userName);



	List<User> findByExtraEmail(String extraEmail);

	User findByToken(String token);
	
	Optional<User> findByContactNumber(String contactnumber);
	
	Optional<User> findBySmsotp(String otp);
}
