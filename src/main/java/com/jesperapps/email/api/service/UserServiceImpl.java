package com.jesperapps.email.api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public List<User> findAllByUserName(String userName) {
		
		return userRepository.findAllByUserName(userName);
	}

	@Override
	public User save(User user) {
		
		return userRepository.save(user);
	}

	@Override
	public Optional<User> findById(Integer id) {
		
		return userRepository.findById(id);
	}
	@Override
	public User deleteUser(Integer id) {
		Optional<User> user = userRepository.findById(id);
		if (user.isPresent()) {
			User dbUser = user.get();
			dbUser.setStatus(4);
			// dbUser.getUserProfile().setStatus("Deleted");
			dbUser.setCreateDateTime(user.get().getCreateDateTime());
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			dbUser.setUpdateDateTime(now);
			return userRepository.save(dbUser);
		} else {
			return null;
		}
	}

	@Override
	public List<User> listUser() {
		return userRepository.findAll();
	}

	@Override
	public List<User> findByExtraEmail(String extraEmail) {
		
		return userRepository.findByExtraEmail(extraEmail);
	}

	@Override
	public User findByToken(String token) {
		
		return userRepository.findByToken(token) ;
	}

	@Override
	public Optional<User> findByContactNumber(String contactnumber) {
		
		return userRepository.findByContactNumber(contactnumber);
	}

	@Override
	public Optional<User> findbyOtp(String otp) {
		// TODO Auto-generated method stub
		return userRepository.findBySmsotp(otp);
	}


}
