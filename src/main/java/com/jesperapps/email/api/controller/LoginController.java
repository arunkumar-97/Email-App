package com.jesperapps.email.api.controller;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.extra.GeneratePlainPassword;
import com.jesperapps.email.api.extra.HexStringToByteArray;
import com.jesperapps.email.api.message.UserRequestEntity;
import com.jesperapps.email.api.message.UserResponseEntity;
import com.jesperapps.email.api.model.ListTypeValues;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class LoginController {
	
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserService service;

	@Autowired
	private PasswordEncoder passwordEncoder;
	UserResponseEntity res = new UserResponseEntity();
	//User Login
		@PostMapping("/user/login")
		public ResponseEntity uservalidation(@RequestBody UserRequestEntity userRequestEntity) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
			System.out.println(userRequestEntity);
			List<User> users = service.findAllByUserName(userRequestEntity.getUserName());
			if (users.isEmpty()) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("statusCode", res.setStatusCode(404));
				jsonObject.put("message", res.setDescription("User not found"));
				return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
			} else {
				for (User users1 : users) {
					if (users1.getStatus().equals(4)) {//Deleted
					} else {
						//RETRIVE ENCRYPTED PASSWORD
				        byte[] bytekey = HexStringToByteArray.hexStringToByteArray(users1.getKeyValue());
				        SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
				        Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
				        cipher.init(Cipher.DECRYPT_MODE, sks);
				        byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(users1.getPassword()));
				        String OriginalPassword = new String(decrypted);
				        System.out.println(OriginalPassword);
				    	//RETRIVE ENCRYPTED PASSWORD
						
						if (userRequestEntity.getPassword().equals(OriginalPassword)) {
							UserResponseEntity userResponseEntity = new UserResponseEntity(users1, userRequestEntity.getPassword());	
							ListTypeValues lType = new ListTypeValues(userResponseEntity.getListTypeValues());
							userResponseEntity.setListTypeValues(lType);
							userResponseEntity.setStatusCode(200);
							userResponseEntity.setDescription("Login Successful");
						return new ResponseEntity(userResponseEntity, HttpStatus.OK);
					}else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("statusCode", res.FAILURE);
						jsonObject.put("message", res.setDescription("Incorrect Password"));
						return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
					}
					}
				}
			}
				return null;

		}
}
