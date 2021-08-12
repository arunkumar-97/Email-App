package com.jesperapps.email.api.controller;

import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.message.UserResponseEntity;
import com.jesperapps.email.api.model.Attachment;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.AttachmentService;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class AttachmentController {

	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;

	UserResponseEntity res = new UserResponseEntity();
	@Autowired
	private AttachmentService attachmentService;

	// To View User Pic
	@GetMapping("/image/{userId}")
	public ResponseEntity<Resource> viewFile(@PathVariable Integer userId) {
		User user = new User(userId);
		Attachment att = attachmentService.findByUser(user);
		if (att != null) {
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(att.getFileType()))
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"inline; filename=\"" + att.getFileName() + "\"")
					.body(new ByteArrayResource(att.getFileByte()));
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("Image not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	@PostMapping("/image")
	public ResponseEntity<User> postImage(@RequestBody Attachment attachment)
			throws IOException {
		Optional<User> uid = userService.findById(attachment.getUser().getId());
		System.out.println("uid" + uid.get());
		if (uid.isPresent()) {
			Attachment attachmentData = attachmentService.findByUser(attachment.getUser());
			if (attachmentData == null) {
				Attachment att = attachmentService.save(attachment);
				if (att != null) {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("statusCode", res.SUCCESS);
					jsonObject.put("description", res.setDescription("Image Created Successfully"));
					return new ResponseEntity(jsonObject, HttpStatus.OK);
				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.FAILURE);
					jsonObject.put("message", res.setMessage("Unable to Create Image"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				}
			} else {
				if(attachmentData.getStatus().equals(4)) {
					System.out.println("if"+attachmentData);
					try {
						System.out.println("try");
						attachmentService.deleteAtt(attachmentData);
					}catch(Exception e){
						System.out.println("catch"+e);
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.FAILURE);
						jsonObject.put("message", res.setMessage("Unable to Create Image"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}
					Attachment att = attachmentService.save(attachment);
					if (att != null) {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("statusCode", res.SUCCESS);
						jsonObject.put("description", res.setDescription("Image Created Successfully"));
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					} else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.FAILURE);
						jsonObject.put("message", res.setMessage("Unable to Create Image"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}
				}else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(409));
					jsonObject.put("message", res.setMessage("Image Already Exists"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);	
				}
			}
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("UserId Not Found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	@PutMapping("/image/{id}")
	public ResponseEntity<User> updateUserProfile(@PathVariable("id") Integer id, @RequestBody Attachment attachment)
			throws IOException {
		Optional<User> uid = userService.findById(attachment.getUser().getId());
		System.out.println("uid" + uid.get());
		if (uid.isPresent()) {
			Attachment attachmentData = attachmentService.findByUser(attachment.getUser());
			if (attachmentData == null) {
				return updateImage(attachment);

			} else {
				return updateImage(attachment);
			}
		}else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		
	}

	private ResponseEntity updateImage(Attachment attachment) {
		Attachment att = attachmentService.save(attachment);
		if (att != null) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.SUCCESS);
			jsonObject.put("description", res.setDescription("Image Updated Successfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Update Image"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	// To Delete User Pic
	@SuppressWarnings({ "static-access", "rawtypes", "unchecked" })
	@DeleteMapping("/image/{userId}")
	public ResponseEntity<User> deleteImage(@PathVariable("userId") Integer id) {
		Optional<User> user = userService.findById(id);
		if (user.isPresent()) {
			Attachment attachmentData = attachmentService.findByUser(user.get());
			if (attachmentData == null) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("Image not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			} else {
				Attachment att = attachmentService.delete(attachmentData);
				if (att == null) {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.FAILURE);
					jsonObject.put("message", res.setMessage("Unable to delete Image"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("statusCode", res.SUCCESS);
					jsonObject.put("description", res.setDescription("Image deleted successfully"));
					return new ResponseEntity(jsonObject, HttpStatus.OK);
				}
			}

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}
}
