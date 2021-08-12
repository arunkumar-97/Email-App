package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.extra.GeneratePlainPassword;
import com.jesperapps.email.api.extra.HexStringToByteArray;
import com.jesperapps.email.api.message.EmailResponseEntity;
import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class EmailDeleteController {
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	EmailResponseEntity res = new EmailResponseEntity();
	String username;
	String pwd;
	String OriginalPassword;

	@DeleteMapping("/delete/{emailType}/{userId}")
	public ResponseEntity deleteMultipleEmails(@PathVariable("emailType") String emailType,
			@PathVariable("userId") Integer userId, @RequestBody Email[] email) {
		Optional<User> user = userService.findById(userId);
		System.out.println(user.get());
		try {
			if (user.isPresent()) {
				username = user.get().getUserName();
				pwd = user.get().getPassword();

				// RETRIVE ENCRYPTED PASSWORD
				byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
				SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
				Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
				cipher.init(Cipher.DECRYPT_MODE, sks);
				byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
				OriginalPassword = new String(decrypted);
				System.out.println(OriginalPassword);
				// RETRIVE ENCRYPTED PASSWORD

			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("User id not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}

			// Get the session object
			ArrayList<String> base64array = new ArrayList<String>();
			ArrayList<String> cidarray = new ArrayList<String>();
			Properties props = new Properties();
			props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.host", user.get().getEmailConfiguration().getIncomingHost());
			props.put("mail.smtp.port", user.get().getEmailConfiguration().getIncomingPort());
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}

			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
			String folderName;
//		        Folder[] folder = store.getDefaultFolder().list();
//		        for (int k = 0; k <= folder.length; k++) {
//					if (folder.length > k) {
			if (emailType.equals("Draft")) {
				folderName = "Drafts";
			} else if (emailType.equals("Sent")) {
				folderName = "Sent";
			} else if (emailType.equals("Received")) {
				folderName = "INBOX";
			} else if (emailType.equals("Deleted")) {
				folderName = "Trash";
			} else {
				folderName = emailType;
			}
			Folder emailFolder = store.getFolder(folderName);
			
			for (Email e : email) {
				if (emailFolder.getMessageCount() != 0) {
					System.out.println("000");
					emailFolder.open(Folder.READ_WRITE);
					UIDFolder uf = (UIDFolder) emailFolder;
					Message msg = uf.getMessageByUID(e.getId());
					if (msg == null) {
						System.out.println("if");
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.setErrorCode(404));
						jsonObject.put("message", res.setMessage("Email with id=" + e.getId() + " not found"));
						// System.out.println(jsonObject);
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					} else {
						System.out.println("else");

						Folder folder = store.getFolder("Trash");
						System.out.println("else1");
//						msg.setFlag(Flags.Flag.DELETED, true);
//						System.out.println("else2");
						folder.open(Folder.READ_WRITE);
						System.out.println("else3");
//						msg.setFlag(Flags.Flag.SEEN, true);
//						System.out.println("else4");
						folder.appendMessages(new Message[] { msg });
						System.out.println("else5");
						
					}
					msg.setFlag(Flags.Flag.DELETED, true);
					Message[] messageList = emailFolder.expunge();
					System.out.println("messageList"+messageList);
					emailFolder.close(true);
				}
				
			}
		
			
			store.close();
		} catch (Exception e) {
			System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Delete Mails"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Emails Deleted Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}
	
	@DeleteMapping("/delete/{emailType}/{userId}/{emailId}")
	public ResponseEntity deleteSingleEmail(@PathVariable("emailType") String emailType,
			@PathVariable("userId") Integer userId, @PathVariable("emailId") Long emailId) {
		Optional<User> user = userService.findById(userId);
		System.out.println(user.get());
		try {
			if (user.isPresent()) {
				username = user.get().getUserName();
				pwd = user.get().getPassword();

				// RETRIVE ENCRYPTED PASSWORD
				byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
				SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
				Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
				cipher.init(Cipher.DECRYPT_MODE, sks);
				byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
				OriginalPassword = new String(decrypted);
				System.out.println(OriginalPassword);
				// RETRIVE ENCRYPTED PASSWORD

			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("User id not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}

			// Get the session object
			ArrayList<String> base64array = new ArrayList<String>();
			ArrayList<String> cidarray = new ArrayList<String>();
			Properties props = new Properties();
			props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.host", user.get().getEmailConfiguration().getIncomingHost());
			props.put("mail.smtp.port", user.get().getEmailConfiguration().getIncomingPort());
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}

			};
			Session session = Session.getInstance(props, auth);

			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
			String folderName;
//		        Folder[] folder = store.getDefaultFolder().list();
//		        for (int k = 0; k <= folder.length; k++) {
//					if (folder.length > k) {
			if (emailType.equals("Draft")) {
				folderName = "Drafts";
			} else if (emailType.equals("Sent")) {
				folderName = "Sent";
			} else if (emailType.equals("Received")) {
				folderName = "INBOX";
			} else if (emailType.equals("Deleted")) {
				folderName = "Trash";
			} else {
				folderName = emailType;
			}
			Folder emailFolder = store.getFolder(folderName);
			if (emailFolder.getMessageCount() != 0) {
				System.out.println("000");
				emailFolder.open(Folder.READ_WRITE);
				UIDFolder uf = (UIDFolder) emailFolder;
				Message msg = uf.getMessageByUID(emailId);
				if (msg == null) {
					System.out.println("if");
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(404));
					jsonObject.put("message", res.setMessage("Email with id=" + emailId + " not found"));
					// System.out.println(jsonObject);
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				} else {
					System.out.println("else");

					Folder folder = store.getFolder("Trash");
					System.out.println("else1");
					msg.setFlag(Flags.Flag.DELETED, true);
					System.out.println("else2");
					folder.open(Folder.READ_WRITE);
					System.out.println("else3");
					msg.setFlag(Flags.Flag.SEEN, true);
					System.out.println("else4");
					folder.appendMessages(new Message[] { msg });
					System.out.println("else5");
				}
				emailFolder.close(true);
			}

			
			store.close();
		} catch (Exception e) {
			System.out.println("ex"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Delete Mails"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Emails Deleted Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}
	
	@DeleteMapping("/delete/permanently/{userId}")
	public ResponseEntity deleteMultipleEmailsPermanantly(
			@PathVariable("userId") Integer userId, @RequestBody Email[] email) {
		Optional<User> user = userService.findById(userId);
		System.out.println(user.get());
		try {
			if (user.isPresent()) {
				username = user.get().getUserName();
				pwd = user.get().getPassword();

				// RETRIVE ENCRYPTED PASSWORD
				byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
				SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
				Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
				cipher.init(Cipher.DECRYPT_MODE, sks);
				byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
				OriginalPassword = new String(decrypted);
				System.out.println(OriginalPassword);
				// RETRIVE ENCRYPTED PASSWORD

			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("User id not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}

			// Get the session object
			ArrayList<String> base64array = new ArrayList<String>();
			ArrayList<String> cidarray = new ArrayList<String>();
			Properties props = new Properties();
			props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.host", user.get().getEmailConfiguration().getIncomingHost());
			props.put("mail.smtp.port", user.get().getEmailConfiguration().getIncomingPort());
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}

			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
			Folder emailFolder = store.getFolder("Trash");
			
			for (Email e : email) {
				if (emailFolder.getMessageCount() != 0) {
					System.out.println("000");
					emailFolder.open(Folder.READ_WRITE);
					UIDFolder uf = (UIDFolder) emailFolder;
					Message msg = uf.getMessageByUID(e.getId());
					if (msg == null) {
						System.out.println("if");
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.setErrorCode(404));
						jsonObject.put("message", res.setMessage("Email with id=" + e.getId() + " not found"));
						// System.out.println(jsonObject);
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					} else {
	

						msg.setFlag(Flags.Flag.DELETED, true);
						Message[] messageList = emailFolder.expunge();
						System.out.println("messageList"+messageList);
						emailFolder.close(true);
						
					}
					
				}
				
			}
		
			
			store.close();
		} catch (Exception e) {
			System.out.println("ee"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Delete Mails"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Emails Deleted Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}

	@DeleteMapping("/delete/permanently/{userId}/{emailId}")
	public ResponseEntity deleteSingleEmail(
			@PathVariable("userId") Integer userId, @PathVariable("emailId") Long emailId) {
		Optional<User> user = userService.findById(userId);
		System.out.println(user.get());
		try {
			if (user.isPresent()) {
				username = user.get().getUserName();
				pwd = user.get().getPassword();

				// RETRIVE ENCRYPTED PASSWORD
				byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
				SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
				Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
				cipher.init(Cipher.DECRYPT_MODE, sks);
				byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
				OriginalPassword = new String(decrypted);
				System.out.println(OriginalPassword);
				// RETRIVE ENCRYPTED PASSWORD

			} else {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("User id not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}

			// Get the session object
			ArrayList<String> base64array = new ArrayList<String>();
			ArrayList<String> cidarray = new ArrayList<String>();
			Properties props = new Properties();
			props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.host", user.get().getEmailConfiguration().getIncomingHost());
			props.put("mail.smtp.port", user.get().getEmailConfiguration().getIncomingPort());
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}

			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
			Folder emailFolder = store.getFolder("Trash");
			
				if (emailFolder.getMessageCount() != 0) {
					System.out.println("000");
					emailFolder.open(Folder.READ_WRITE);
					UIDFolder uf = (UIDFolder) emailFolder;
					Message msg = uf.getMessageByUID(emailId);
					if (msg == null) {
						System.out.println("if");
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.setErrorCode(404));
						jsonObject.put("message", res.setMessage("Email with id=" + emailId + " not found"));
						// System.out.println(jsonObject);
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					} else {
	

						msg.setFlag(Flags.Flag.DELETED, true);
						Message[] messageList = emailFolder.expunge();
						System.out.println("messageList"+messageList);
						emailFolder.close(true);
						
					}
					
				}
				
			
		
			
			store.close();
		} catch (Exception e) {
			System.out.println("xe"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Delete Mail"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Email Deleted Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}

}
