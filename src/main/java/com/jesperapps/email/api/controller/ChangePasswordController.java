
package com.jesperapps.email.api.controller;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")

@RestController
public class ChangePasswordController {
	private static final Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	UserResponseEntity res = new UserResponseEntity();

	String jsonArray;
	List<User> asLis;
	public static final String AES = "AES";
	@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
//for settings //admin will change the password for employees
	@PutMapping("/change_password/admin/{userId}/{adminUserId}")
	public ResponseEntity changePassword(@PathVariable("userId") Integer userId,
			@PathVariable("adminUserId") Integer adminUserId, @RequestBody UserRequestEntity userRequestEntity)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Optional<User> userAdmin = userService.findById(adminUserId);
		if (userAdmin.isPresent()) {
			if (userAdmin.get().getStatus().equals(4)) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.FAILURE);
				jsonObject.put("message", res.setMessage("User Not Found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			} else {
//				boolean result = passwordEncoder.matches(userRequestEntity.getAdminPassword(),
//						userAdmin.get().getPassword());
				//RETRIVE ENCRYPTED PASSWORD
		        byte[] bytekeyDec = HexStringToByteArray.hexStringToByteArray(userAdmin.get().getKeyValue());
		        SecretKeySpec sksDec = new SecretKeySpec(bytekeyDec, GeneratePlainPassword.AES);
		        Cipher cipherDec = Cipher.getInstance(GeneratePlainPassword.AES);
		        cipherDec.init(Cipher.DECRYPT_MODE, sksDec);
		        byte[] decrypted = cipherDec.doFinal(HexStringToByteArray.hexStringToByteArray(userAdmin.get().getPassword()));
		        String OriginalPassword = new String(decrypted);
		        System.out.println(OriginalPassword);
		    	//RETRIVE ENCRYPTED PASSWORD
				
				if (userRequestEntity.getAdminPassword().equals(OriginalPassword)) {
//					
//				}
//				if (result) {
					Optional<User> user = userService.findById(userId);
					if (user.isPresent()) {
						
						String pass = userRequestEntity.getPassword();
						// ENCRYPT PASSWORD
						byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
						SecretKeySpec sks = new SecretKeySpec(bytekey, GenerateEncryptionPassword.AES);
						Cipher cipher = Cipher.getInstance(GenerateEncryptionPassword.AES);
						cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
						byte[] encrypted = cipher.doFinal(pass.getBytes());
						String encryptedpwd = ByteArrayToHexString.byteArrayToHexString(encrypted);
						// ENCRYPT PASSWORD
						user.get().setPassword(encryptedpwd);
						User resData = userService.save(user.get());
						if (resData == null) {
							ObjectNode jsonObject = objectMapper.createObjectNode();
							jsonObject.put("errorCode", res.FAILURE);
							jsonObject.put("message", res.setMessage("Unable to change Password"));
							return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
						} else {
							ObjectNode jsonObject = objectMapper.createObjectNode();
							jsonObject.put("statusCode", res.SUCCESS);
							jsonObject.put("description", res.setDescription("Password Changed Successfully"));
							return new ResponseEntity(jsonObject, HttpStatus.OK);
						}
					}else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.FAILURE);
						jsonObject.put("message", res.setMessage("User Not Found"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}
				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.FAILURE);
					jsonObject.put("message", res.setMessage("Admin Password is InCorrect"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				}
			}

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Admin Not Found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}
	
	//in login//USER WILL CHANGE THE PASSWORD FOR THEMSELVES
		@PutMapping("change_password/user/{userId}")
		public ResponseEntity changePassword(@PathVariable("userId") Integer id,
				@RequestBody UserRequestEntity userRequestEntity) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
			Optional<User> user = userService.findById(id);
			if (user.isPresent()) {
				
				// ENCRYPT PASSWORD
				byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
				SecretKeySpec sks = new SecretKeySpec(bytekey, GenerateEncryptionPassword.AES);
				Cipher cipher = Cipher.getInstance(GenerateEncryptionPassword.AES);
				cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
				byte[] encrypted = cipher.doFinal(userRequestEntity.getPassword().getBytes());
				String encryptedpwd = ByteArrayToHexString.byteArrayToHexString(encrypted);
				// ENCRYPT PASSWORD
				userRequestEntity.setPassword(encryptedpwd);
				
				User userData = new User(user.get(), userRequestEntity, user.get());
				User userSaved = userService.save(userData);
				if (userSaved == null) {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.FAILURE);
					jsonObject.put("message", res.setMessage("Unable to change Password"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("statusCode", res.SUCCESS);
					jsonObject.put("description", res.setDescription("Password Changed Successfully"));
					return new ResponseEntity(jsonObject, HttpStatus.OK);
				}
			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.FAILURE);
				jsonObject.put("message", res.setMessage("User Not Found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}
		}

}
