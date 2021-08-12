package com.jesperapps.email.api.controller;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.extra.ByteArrayToHexString;
import com.jesperapps.email.api.extra.GenerateEncryptionPassword;
import com.jesperapps.email.api.extra.GeneratePlainPassword;
import com.jesperapps.email.api.extra.HexStringToByteArray;
import com.jesperapps.email.api.message.UserRequestEntity;
import com.jesperapps.email.api.message.UserResponseEntity;
import com.jesperapps.email.api.model.ListTypeValues;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.EmailService;
import com.jesperapps.email.api.service.OtpSmsService;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ForgotPasswordController {
	@Autowired
	private EmailService emailService;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	UserResponseEntity res = new UserResponseEntity();
	@Autowired
	private ObjectMapper objectMapper;
//	String fromMail = "rose.pauline@jespersoft.com";
	@Autowired
	private UserService userService;
	String username;

	@Autowired
	private OtpSmsService otpSmsService;
	String pwd;
	String OriginalPassword;

	// in login//a link will be sent to mail
	@SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	@PostMapping("forgot_password")
	public ResponseEntity forgotPassword(@RequestBody UserRequestEntity userRequestEntity) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		Optional<User> user1 = userService.findById(userRequestEntity.getId());
		if (user1.isPresent()) {
			username = user1.get().getUserName();
			pwd = user1.get().getPassword();
			// RETRIVE ENCRYPTED PASSWORD
			byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user1.get().getKeyValue());
			SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
			Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
			cipher.init(Cipher.DECRYPT_MODE, sks);
			byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
			OriginalPassword = new String(decrypted);
			// RETRIVE ENCRYPTED PASSWORD
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		String extraEmail = userRequestEntity.getExtraEmail();
		List<User> userData = userService.findByExtraEmail(extraEmail);
		if (userData.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("Email not found"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		} else {
			for (User usr : userData) {
				if (usr.getStatus().equals(4)) {

				} else {
					try {

						Properties props = new Properties();
						props.put("mail.smtp.auth", user1.get().getEmailConfiguration().isAuthentication());
						props.put("mail.smtp.starttls.enable", user1.get().getEmailConfiguration().isAuthentication());
						props.put("mail.smtp.host", user1.get().getEmailConfiguration().getOutgoingHost());
						props.put("mail.smtp.port", user1.get().getEmailConfiguration().getOutgoingPort());
						Authenticator auth = new Authenticator() {
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(username, OriginalPassword);
							}
						};
						String appUrl = "https://www.jespersoft.com:6003/login/";
						Session session = Session.getInstance(props, auth);
						usr.setToken(UUID.randomUUID().toString());
						
						Message msg = new MimeMessage(session);
						msg.setFrom(new InternetAddress(username, false));
						InternetAddress to1 = new InternetAddress(userRequestEntity.getExtraEmail());
						msg.setRecipient(Message.RecipientType.TO, to1);
						msg.setSubject("Password Reset Request");
						msg.setText("To reset your password, click the link below:\n" + appUrl + "reset-password/"
								+ usr.getToken());
						Transport.send(msg);
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("statusCode", res.SUCCESS);
						jsonObject.put("description", res.setDescription("A link has been sent to your mail"));
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					} catch (Exception ex) {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", 400);
						jsonObject.put("message", res.setMessage("Unable to send mail, Please enter valid EmailId"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}

				}

			}
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(404));
			jsonObject.put("message", res.setMessage("User not found"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })

	// reset the password using the link sent to mail
	@PutMapping("reset_password/{token}")
	public ResponseEntity ResetPassword(@PathVariable("token") String token,
			@RequestBody UserRequestEntity userRequestEntity) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		User dbtoken = userService.findByToken(token);
		if (dbtoken == null) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("Token not found"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		} else {
			// ENCRYPT PASSWORD
			byte[] bytekey = HexStringToByteArray.hexStringToByteArray(dbtoken.getKeyValue());
			SecretKeySpec sks = new SecretKeySpec(bytekey, GenerateEncryptionPassword.AES);
			Cipher cipher = Cipher.getInstance(GenerateEncryptionPassword.AES);
			cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
			byte[] encrypted = cipher.doFinal(userRequestEntity.getPassword().getBytes());
			String encryptedpwd = ByteArrayToHexString.byteArrayToHexString(encrypted);
			String password = userRequestEntity.setPassword(encryptedpwd);
			dbtoken.setPassword(password);
			dbtoken.setToken(null);
			User userData = userService.save(dbtoken);
			if (userData == null) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(400));
				jsonObject.put("message", res.setMessage("Unable to Reset Password"));
				return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("statusCode", res.SUCCESS);
				jsonObject.put("description", res.setDescription("Password Reset Successful"));
				return new ResponseEntity(jsonObject, HttpStatus.OK);
			}

		}
	}
	
	@PostMapping("forgot_password_sms/{contactnumber}")
	public ResponseEntity forgotPasswordSMS(@PathVariable("contactnumber") String contactnumber)  {
		Optional<User> user1 = userService.findByContactNumber(contactnumber);
		if (user1.isPresent()) {
			
			   int otp =  otpSmsService.generateOTP(contactnumber);
			    if(otp != 0 )			
			    {
			    	   User user = new User(user1.get(),user1.get());
			    	   user.setSmsotp(Integer.toString(otp));
			    	   userService.save(user);
			    	   
			    	     otpSmsService.sendSms("Your One Time Password(OTP) is " + otp , contactnumber);
			    	     ObjectNode jsonObject = objectMapper.createObjectNode();
							jsonObject.put("errorCode", res.setErrorCode(404));
							jsonObject.put("message", res.setMessage("Otp send sucessfully"));
							return new ResponseEntity(jsonObject, HttpStatus.OK);
			    	   
			    }else 
			    {
			    	ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(404));
					jsonObject.put("message", res.setMessage("please try again later"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			    }
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("ContactNumber  not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}
	
	@PostMapping("/validate_otp/{otp}")
	public ResponseEntity uservalidation(@PathVariable("otp") String otp) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
	             //  System.out.println("otp " + otp);
		Optional<User> users = userService.findbyOtp(otp);
		if (users.isPresent() == false) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setStatusCode(404));
			jsonObject.put("message", res.setDescription("Otp not found"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		} else {
			
				if (users.get().getStatus().equals(4)) {//Deleted
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("statusCode", res.setStatusCode(404));
					jsonObject.put("message", res.setDescription("User not found"));
					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
					
				} else {
					   // System.out.println("otp" + users.get().getSmsotp().equals(otp));
					if (users.get().getSmsotp().equals(otp)) {
					//	   System.out.println("otp matached");
						UserResponseEntity userResponseEntity = new UserResponseEntity(users.get(), users);	
						ListTypeValues lType = new ListTypeValues(userResponseEntity.getListTypeValues());
						userResponseEntity.setListTypeValues(lType);
						userResponseEntity.setStatusCode(200);
						userResponseEntity.setDescription("Login Successful");
					return new ResponseEntity(userResponseEntity, HttpStatus.OK);
				}else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("statusCode", res.FAILURE);
					jsonObject.put("message", res.setDescription("Incorrect Otp"));
					return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
				}
				}
			
		}
			

	}
}
