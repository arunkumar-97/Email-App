package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.message.UserRequestEntity;
import com.jesperapps.email.api.message.UserResponseEntity;
import com.jesperapps.email.api.model.Attachment;
import com.jesperapps.email.api.model.ListTypeValues;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	private Logger logger = LoggerFactory.getLogger("UserController");
	UserResponseEntity res = new UserResponseEntity();

	UserRequestEntity userRequestEntity;
	String password;
	User userReq;
	

	// Update User
	@SuppressWarnings("unchecked")
	@PutMapping("/user/{id}")
	public ResponseEntity<User> updateUser(@RequestBody UserRequestEntity userRequestEntity) {
		Optional<User> uid = userService.findById(userRequestEntity.getId());	
			userRequestEntity.setPassword(uid.get().getPassword());
		Optional<User> Id = userService.findById(userRequestEntity.getId());
		if (Id.isPresent()) {
			userReq = new User(Id.get(), userRequestEntity);
			User users = userService.save(userReq);
			// User users = userService.save(userReq);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.SUCCESS);
			jsonObject.put("description", res.setDescription("User Updated Successfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Update User"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}
// to update only firstName and ContactNumber
	@PutMapping("/user_data/{id}")
	public ResponseEntity<User> updateUserFewFields(@PathVariable("id") Integer id,
			@RequestBody UserRequestEntity userRequestEntity) {
		Optional<User> uid = userService.findById(id);
		if (uid.isPresent()) {
			User user1 = new User(uid.get(), userRequestEntity, userRequestEntity);
			User userSaved = userService.save(user1);
			if (userSaved == null) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.FAILURE);
				jsonObject.put("message", res.setMessage("Unable to Update User"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("statusCode", res.SUCCESS);
				jsonObject.put("description", res.setDescription("User Updated Successfully"));
				return new ResponseEntity(jsonObject, HttpStatus.OK);
			}
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("UserId Not Found"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
	}

	// Delete User
	@SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	@DeleteMapping("/user/{id}")
	public ResponseEntity<User> deleteUser(@PathVariable("id") Integer id) {
		Optional<User> user = userService.findById(id);
		if (user.isPresent()) {
			userService.deleteUser(id);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.SUCCESS);
			jsonObject.put("description", res.setDescription("User Deleted Successfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User not found"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
	}

	// Get User By UserId
	UserResponseEntity asList1;

	@GetMapping("/user/{id}")
	public ResponseEntity<UserResponseEntity> getUser(@PathVariable("id") Integer id) {
		Optional<User> user = userService.findById(id);
		System.out.println("user" + user);
		if (user.isPresent()) {
			UserResponseEntity userRes = new UserResponseEntity(user.get());
			ListTypeValues lt = new ListTypeValues(userRes.getListTypeValues());
			userRes.setListTypeValues(lt);
			if (userRes.getAttachment() == null) {

			} else {
				Attachment attachment = new Attachment(userRes.getAttachment());
				userRes.setAttachment(attachment);
			}

			return new ResponseEntity<UserResponseEntity>(userRes, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User not found"));
			System.out.println(jsonObject);
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
	}

	// List all Users
	String jsonArray;
	List<UserResponseEntity> asList;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/user")
	public ResponseEntity<List<UserResponseEntity>> listUsers() {
		List<UserResponseEntity> resEntity = new ArrayList<UserResponseEntity>();
		List<User> users = userService.listUser();
		System.out.println("user" + users);
		if (users.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setStatusCode(204));
			jsonObject.put("description", res.setDescription("no data"));
			return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
		} else {
			for (User user : users) {

				if (user.getStatus() == null || user.getStatus().equals(1) || user.getStatus().equals(2)
						|| user.getStatus().equals(3)) {
					UserResponseEntity userResponseEntity = new UserResponseEntity(user, user);
					ListTypeValues lt = new ListTypeValues(userResponseEntity.getListTypeValues());
					userResponseEntity.setListTypeValues(lt);
					if (userResponseEntity.getAttachment() == null) {

					} else {
						Attachment attachment = new Attachment(userResponseEntity.getAttachment());
						userResponseEntity.setAttachment(attachment);
					}

					resEntity.add(userResponseEntity);
				}
			}
			if (resEntity.isEmpty()) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("statusCode", res.setStatusCode(204));
				jsonObject.put("description", res.setDescription("no data"));
				return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
			} else {
				return new ResponseEntity<List<UserResponseEntity>>(resEntity, HttpStatus.OK);
			}
		}
	}

}
