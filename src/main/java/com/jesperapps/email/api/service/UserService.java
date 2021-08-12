package com.jesperapps.email.api.service;

import java.util.List;
import java.util.Optional;

import com.jesperapps.email.api.model.User;

public interface UserService {

	List<User> findAllByUserName(String userName);

	User save(User user);

	Optional<User> findById(Integer id);
	User deleteUser(Integer id);

	List<User> listUser();

	List<User> findByExtraEmail(String extraEmail);

	User findByToken(String token);

	Optional<User> findByContactNumber(String contactnumber);

	Optional<User> findbyOtp(String otp);

}
