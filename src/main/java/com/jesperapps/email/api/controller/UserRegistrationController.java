package com.jesperapps.email.api.controller;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.extra.ByteArrayToHexString;
import com.jesperapps.email.api.extra.GenerateEncryptionPassword;
import com.jesperapps.email.api.extra.HexStringToByteArray;
import com.jesperapps.email.api.message.UserRequestEntity;
import com.jesperapps.email.api.message.UserResponseEntity;
import com.jesperapps.email.api.model.EmailConfiguration;
import com.jesperapps.email.api.model.ListTypeValues;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.repository.ListTypeValuesRepository;
import com.jesperapps.email.api.service.UserService;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class UserRegistrationController {

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ListTypeValuesRepository listTypeValuesRepository;

	UserResponseEntity res = new UserResponseEntity();
	public static final String AES = "AES";


	@PostMapping("/user")
	public ResponseEntity ReceiveEmail(@RequestBody UserRequestEntity userRequestEntity) throws Exception {
		if ( userRequestEntity.getListTypeValues() == null) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", 400);
			jsonObject.put("message", res.setMessage("UserType can't be empty"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		String pass = userRequestEntity.getPassword();

		// ENCRYPT PASSWORD
		KeyGenerator keyGen = KeyGenerator.getInstance(UserRegistrationController.AES);
		keyGen.init(128);
		SecretKey sk = keyGen.generateKey();
		String key = ByteArrayToHexString.byteArrayToHexString(sk.getEncoded());
		byte[] bytekey = HexStringToByteArray.hexStringToByteArray(key);
		SecretKeySpec sks = new SecretKeySpec(bytekey, GenerateEncryptionPassword.AES);
		Cipher cipher = Cipher.getInstance(GenerateEncryptionPassword.AES);
		cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
		byte[] encrypted = cipher.doFinal(pass.getBytes());
		String encryptedpwd = ByteArrayToHexString.byteArrayToHexString(encrypted);
		// ENCRYPT PASSWORD

		userRequestEntity.setPassword(encryptedpwd);
		List<User> userData = userService.findAllByUserName(userRequestEntity.getUserName());
		if (userData.isEmpty()) {
		} else {
			for (User user1 : userData) {
				if (user1.getStatus().equals(4)) {// Deleted
					System.out.println("user1.getStatus()"+user1.getStatus());
				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(409));
					jsonObject.put("message",
							res.setMessage("UserName(Email) '" + userRequestEntity.getUserName() + "' already exist"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				}
			}
			
		}
		User user = new User(userRequestEntity);
		user.setKeyValue(key);
		try {
			// create properties field
			// incoming
			Properties properties = new Properties();
			properties.put("mail.store.protocol", userRequestEntity.getEmailConfiguration().getIncomingProtocol());
			properties.put("mail.host", userRequestEntity.getEmailConfiguration().getIncomingHost());
			properties.put("mail.port", userRequestEntity.getEmailConfiguration().getIncomingPort());
			properties.put("mail.smtp.ssl.enable", userRequestEntity.getEmailConfiguration().isSecurity());
			Session emailSession = Session.getDefaultInstance(properties);
			// emailSession.setDebug(true);
			Store store = emailSession.getStore(userRequestEntity.getEmailConfiguration().getIncomingProtocol());
			store.connect(userRequestEntity.getEmailConfiguration().getIncomingHost(),
					userRequestEntity.getEmailConfiguration().getIncomingPort(), userRequestEntity.getUserName(), pass);
			try {
				// Get the session object
				// outgoing
				Properties props = new Properties();
				props.put("mail.smtp.auth", userRequestEntity.getEmailConfiguration().isAuthentication());
				props.put("mail.smtp.starttls.enable", userRequestEntity.getEmailConfiguration().isSecurity());
				props.put("mail.smtp.host", userRequestEntity.getEmailConfiguration().getOutgoingHost());
				props.put("mail.smtp.port", userRequestEntity.getEmailConfiguration().getOutgoingPort());
				Authenticator auth = new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(userRequestEntity.getUserName(), pass);
					}
				};
				Session session = Session.getInstance(props, auth);
				Store store1 = session.getStore(userRequestEntity.getEmailConfiguration().getOutgoingProtocol());
				store1.connect(userRequestEntity.getEmailConfiguration().getOutgoingHost(),
						userRequestEntity.getUserName(), pass);

			} catch (Exception e) {
				System.out.println("e1"+e);
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.FAILURE);
				jsonObject.put("message", res.setDescription("Unable to Connect to the Mail Server."));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setDescription("Unable to Connect to the Mail Server"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		User users = userService.save(user);
		if (users != null) {
			UserResponseEntity userResponseEntity = new UserResponseEntity(users);
			Optional<ListTypeValues> ltValues = listTypeValuesRepository
					.findById(userResponseEntity.getListTypeValues().getListTypeValueId());
			if (ltValues.isPresent()) {
				ListTypeValues lType = new ListTypeValues(ltValues.get());
				userResponseEntity.setListTypeValues(lType);
			} else {
				
			}
			userResponseEntity.setStatusCode(200);
			userResponseEntity.setDescription("User Registered Successfully");
			return new ResponseEntity(userResponseEntity, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Register User"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	
}
