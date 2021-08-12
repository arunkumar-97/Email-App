package com.jesperapps.email.api.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.message.SignatureRequestEntity;
import com.jesperapps.email.api.message.SignatureResponseEntity;
import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.Signature;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.SignatureService;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SignatureController {

	@Autowired
	private SignatureService signatureService;

	@Autowired
	private ObjectMapper objectMapper;
	private SignatureResponseEntity signatureResponseEntity;

	private Logger logger = LoggerFactory.getLogger("SignatureController");
	SignatureResponseEntity res = new SignatureResponseEntity();

	// Create Signature
	@PostMapping("/signature")
	public ResponseEntity createSignature(@RequestBody SignatureRequestEntity signatureRequestEntity) {
		List<Signature> signatureData = signatureService.findAllByUserAndName(signatureRequestEntity.getUser(), signatureRequestEntity.getName());
		if (signatureData.isEmpty()) {
		} else {
			for (Signature sign : signatureData) {
				if (sign.getStatus().equals(4)) {// Deleted
//					signatureService.delete(sign);
				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(409));
					jsonObject.put("message", res.setMessage("Signature already exist"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				}
			}
		}
		Signature signature = new Signature(signatureRequestEntity);
		Signature signatures = signatureService.save(signature);
		if (signatures != null) {
			List<Signature> signatures1 = signatureService.findAllByUserAndStatus(signatures.getUser(), 1);// active
			for (Signature Signature : signatures1) {
				if (Signature.getId().equals(signatures.getId()) == false) {
					Signature signature2 = new Signature(Signature);
					signature2.setStatus(2);// inactive
					signatureService.save(signature2);

				}
			}
			SignatureResponseEntity signatureResponseEntity = new SignatureResponseEntity(signatures);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.SUCCESS);
			jsonObject.put("description", res.setDescription("Signature  Created Successfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to  Created  Signature"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	@PutMapping("/signature/{id}")
	public ResponseEntity UpdateSignature(@RequestBody SignatureRequestEntity signatureRequestEntity) {
		Signature signature = new Signature(signatureRequestEntity);
		Optional<Signature> sign = signatureService.findById(signature.getId());
		if (sign.isPresent()) {
			if(signatureRequestEntity.getName().equals(sign.get().getName())) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(409));
				jsonObject.put("message", res.setMessage("Signature already exist"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}
			Signature signature1 = new Signature(signatureRequestEntity, signatureRequestEntity);
//			    	   signature1.setCreatedOn(sign.get().getCreatedOn());
			Signature signatures = signatureService.save(signature1);
			if (signatures != null) {
				SignatureResponseEntity signatureResponseEntity = new SignatureResponseEntity(signatures);
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("statusCode", res.SUCCESS);
				jsonObject.put("description", res.setDescription("Signature  Updated Successfully"));
				return new ResponseEntity(jsonObject, HttpStatus.OK);
			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.FAILURE);
				jsonObject.put("message", res.setMessage("Unable to  Update  Signature"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("Signature Not Found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	@PutMapping("/signature-status/{signatureId}/{signatureStatus}")
	public ResponseEntity<Email> updateSignatureStatus(@PathVariable("signatureId") Integer signatureId,
			@PathVariable("signatureStatus") Integer signatureStatus)
			throws JsonParseException, JsonMappingException, IOException {
		Optional<Signature> signatureOptional = signatureService.findById(signatureId);
		Signature signature = new Signature(signatureOptional.get());

		signature.setStatus(signatureStatus);
//			signature.setCreatedOn(signatureOptional.get().getCreatedOn());
		Signature signatures = signatureService.save(signature);
		if (signatures != null) {
			if (signatureStatus.equals(1)) { // active
				List<Signature> signatures1 = signatureService.findAllByUserAndStatus(signatures.getUser(), 1);// active

				for (Signature Signature1 : signatures1) {
					System.out.println("for");
					if (Signature1.getId().equals(signatureId) == false) {
						Signature signature2 = new Signature(Signature1);
						signature2.setStatus(2);// inactive
						signatureService.save(signature2);

					}
				}
			}
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.SUCCESS);
			jsonObject.put("description", res.setDescription("Signature  Status Updated  Successfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to  Update  Signature"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	@GetMapping("/signature/{id}")
	public ResponseEntity ListSignature(@PathVariable Integer id) {
		Optional<Signature> signatures = signatureService.findById(id);
		if (signatures.isPresent()) {
			SignatureResponseEntity signatureRes = new SignatureResponseEntity(signatures.get());
			User user = new User(signatureRes.getUser());
			signatureRes.setUser(user);
			return new ResponseEntity<SignatureResponseEntity>(signatureRes, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("Signature with id=" + id + " not found"));
			// System.out.println(jsonObject);
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	@GetMapping("/signature-by-user/{userid}")
	public ResponseEntity ListSignaturebyUserId(@PathVariable Integer userid) {
		User user = new User(userid);
		List<SignatureResponseEntity> resEntity = new ArrayList<SignatureResponseEntity>();
		List<Signature> signatures = signatureService.findAllByUser(user);
		System.out.println("signatures" + signatures);
		if (signatures.isEmpty() == false) {
			for (Signature Signature : signatures) {
				if (Signature.getStatus() == null || Signature.getStatus().equals(1)// active
						|| Signature.getStatus().equals(2) || Signature.getStatus().equals(3)) {// inactive//pending
					SignatureResponseEntity signatureResponseEntity = new SignatureResponseEntity(Signature, Signature);
					User user1 = new User(signatureResponseEntity.getUser());
					signatureResponseEntity.setUser(user1);
					resEntity.add(signatureResponseEntity);
				}
			}
			if (resEntity.isEmpty()) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("statusCode", res.setErrorCode(204));
				jsonObject.put("message", res.setMessage("no data"));
				return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
			} else {
				return new ResponseEntity<List<SignatureResponseEntity>>(resEntity, HttpStatus.OK);
			}
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setErrorCode(204));
			jsonObject.put("message", res.setMessage("no data"));
			return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping("/signature-by-status/{Status}/{userId}")
	public ResponseEntity ListSignaturebyStatusAndUserId(@PathVariable Integer Status, @PathVariable Integer userId) {
		User user = new User(userId);
		List<SignatureResponseEntity> resEntity = new ArrayList<SignatureResponseEntity>();
		List<Signature> signatures = signatureService.findAllByUserAndStatus(user, Status);

		if (signatures.isEmpty() == false) {
			for (Signature Signature : signatures) {
				SignatureResponseEntity signatureResponseEntity = new SignatureResponseEntity(Signature, Signature);
				User user1 = new User(signatureResponseEntity.getUser());
				signatureResponseEntity.setUser(user1);
				resEntity.add(signatureResponseEntity);
			}

			return new ResponseEntity<List<SignatureResponseEntity>>(resEntity, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setErrorCode(204));
			jsonObject.put("message", res.setMessage("no data"));
			return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping("/signature")
	public ResponseEntity ListSignature() {
		List<SignatureResponseEntity> resEntity = new ArrayList<SignatureResponseEntity>();
		List<Signature> signatures = signatureService.listSignature();
		System.out.println("signatures" + signatures);
		if (signatures.isEmpty() == false) {
			for (Signature Signature : signatures) {
				if (Signature.getStatus() == null || Signature.getStatus().equals(1) || Signature.getStatus().equals(2)
						|| Signature.getStatus().equals(3)) {
					SignatureResponseEntity signatureResponseEntity = new SignatureResponseEntity(Signature, Signature);
					User user = new User(signatureResponseEntity.getUser());
					signatureResponseEntity.setUser(user);
					resEntity.add(signatureResponseEntity);
				}
			}
			if (resEntity.isEmpty()) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("statusCode", res.setErrorCode(204));
				jsonObject.put("message", res.setMessage("no data"));
				return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
			} else {
				return new ResponseEntity<List<SignatureResponseEntity>>(resEntity, HttpStatus.OK);
			}
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setErrorCode(204));
			jsonObject.put("message", res.setMessage("no data"));
			return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
		}

	}

	@DeleteMapping("/signature/{id}")
	public ResponseEntity<Signature> deleteFolder(@PathVariable("id") Integer id) {
		Optional<Signature> folder = signatureService.findById(id);
		if (folder.isPresent()) {
			signatureService.deleteSignature(id);

			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.SUCCESS);
			jsonObject.put("description", res.setDescription("Signature Deleted Successfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Signature Not Found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}
}
