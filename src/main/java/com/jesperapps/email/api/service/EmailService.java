package com.jesperapps.email.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.mail.SimpleMailMessage;

import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.User;

public interface EmailService {
	
	void sendEmail(SimpleMailMessage passwordResetEmail);

}
