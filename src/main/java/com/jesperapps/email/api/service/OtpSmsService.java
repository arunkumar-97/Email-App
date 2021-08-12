package com.jesperapps.email.api.service;

public interface OtpSmsService {

	
	int generateOTP(String contactnumber);
	
	void sendSms(String string, String phone);
}
