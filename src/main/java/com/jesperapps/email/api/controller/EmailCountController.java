package com.jesperapps.email.api.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class EmailCountController {
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	EmailResponseEntity res = new EmailResponseEntity();


	@GetMapping("/inbox/unread/count/{userId}")
	public ResponseEntity inboxUnReadCount(@PathVariable("userId") Integer id) throws Exception {
		Optional<User> user = userService.findById(id);
		
		if(user.isPresent()) {
			String username = user.get().getUserName();
			String pwd = user.get().getPassword();
			
			//RETRIVE ENCRYPTED PASSWORD
		        byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
		        SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
		        Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
		        cipher.init(Cipher.DECRYPT_MODE, sks);
		        byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
		        String OriginalPassword = new String(decrypted);
		        System.out.println(OriginalPassword);
		    	//RETRIVE ENCRYPTED PASSWORD
		    	System.out.println("host:"+user.get().getEmailConfiguration().getIncomingHost());
			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchInboxUnReadCount(host, mailstoreType, username, OriginalPassword, port, auth, security);
		}else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchInboxUnReadCount(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security) throws Exception {
		// create properties field
		Properties properties = new Properties();
		properties.put("mail.store.protocol", mailStrProt);
		properties.put("mail.host", hostval);
		properties.put("mail.port", port);
		properties.put("mail.smtp.ssl.enable", security);
		Session emailSession = Session.getDefaultInstance(properties);
		// emailSession.setDebug(true);
		Store store = emailSession.getStore(mailStrProt);
		store.connect(hostval, port, uname, password);
				// create the folder object and open it
				Folder emailFolder = store.getFolder("INBOX");
				System.out.println("emailFolder" + emailFolder.getMessageCount());
				if (emailFolder.getMessageCount() != 0) {
					emailFolder.open(Folder.READ_ONLY);
					Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
					int inboxUnReadCount = messages.length;
					
					if (inboxUnReadCount == 0) {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("unReadCount", 0);
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					} else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("unReadCount", inboxUnReadCount);
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					}
				}else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("unReadCount", 0);
					return new ResponseEntity(jsonObject, HttpStatus.OK);
				}
				
	}
	
	@GetMapping("/trash/unread/count/{userId}")
	public ResponseEntity trashUnReadCount(@PathVariable("userId") Integer id) throws Exception {
		Optional<User> user = userService.findById(id);
		
		if(user.isPresent()) {
			String username = user.get().getUserName();
			String pwd = user.get().getPassword();
			
			//RETRIVE ENCRYPTED PASSWORD
		        byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
		        SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
		        Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
		        cipher.init(Cipher.DECRYPT_MODE, sks);
		        byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
		        String OriginalPassword = new String(decrypted);
		        System.out.println(OriginalPassword);
		    	//RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchTrashUnReadCount(host, mailstoreType, username, OriginalPassword, port, auth, security);
		}else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}
	
	public ResponseEntity fetchTrashUnReadCount(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security) throws Exception {
		// create properties field
		Properties properties = new Properties();
		properties.put("mail.store.protocol", mailStrProt);
		properties.put("mail.host", hostval);
		properties.put("mail.port", port);
		properties.put("mail.smtp.ssl.enable", security);
		Session emailSession = Session.getDefaultInstance(properties);
		// emailSession.setDebug(true);
		Store store = emailSession.getStore(mailStrProt);
		store.connect(hostval, port, uname, password);
				// create the folder object and open it
				Folder emailFolder = store.getFolder("Trash");
				System.out.println("emailFolder" + emailFolder.getMessageCount());
				if (emailFolder.getMessageCount() != 0) {
					emailFolder.open(Folder.READ_ONLY);
					Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
					int trashUnReadCount = messages.length;
					
					if (trashUnReadCount == 0) {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("unReadCount", 0);
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					} else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("unReadCount", trashUnReadCount);
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					}
				}else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("unReadCount", 0);
					return new ResponseEntity(jsonObject, HttpStatus.OK);
				}
					
				
	} 
	
	@GetMapping("/draft/all/count/{userId}")
	public ResponseEntity draftAllCount(@PathVariable("userId") Integer id) throws Exception {
		Optional<User> user = userService.findById(id);
		
		if(user.isPresent()) {
			String username = user.get().getUserName();
			String pwd = user.get().getPassword();
			
			//RETRIVE ENCRYPTED PASSWORD
		        byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
		        SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
		        Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
		        cipher.init(Cipher.DECRYPT_MODE, sks);
		        byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
		        String OriginalPassword = new String(decrypted);
		        System.out.println(OriginalPassword);
		    	//RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchDraftAllCount(host, mailstoreType, username, OriginalPassword, port, auth, security);
		}else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}
	
	public ResponseEntity fetchDraftAllCount(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security) throws Exception {
		// create properties field
		Properties properties = new Properties();
		properties.put("mail.store.protocol", mailStrProt);
		properties.put("mail.host", hostval);
		properties.put("mail.port", port);
		properties.put("mail.smtp.ssl.enable", security);
		Session emailSession = Session.getDefaultInstance(properties);
		// emailSession.setDebug(true);
		Store store = emailSession.getStore(mailStrProt);
		store.connect(hostval, port, uname, password);
				// create the folder object and open it
				Folder emailFolder = store.getFolder("Drafts");
				System.out.println("emailFolder" + emailFolder.getMessageCount());
				if (emailFolder.getMessageCount() != 0) {
					emailFolder.open(Folder.READ_ONLY);
					Message[] messages = emailFolder.getMessages();
					int trashAllCount = messages.length;
					if (trashAllCount == 0) {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("unReadCount", 0);
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					} else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("unReadCount", trashAllCount);
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					}
				}else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("unReadCount", 0);
					return new ResponseEntity(jsonObject, HttpStatus.OK);
				}
				
	} 

	
}
