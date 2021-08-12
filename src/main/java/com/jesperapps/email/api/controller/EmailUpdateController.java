package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
public class EmailUpdateController {
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	EmailResponseEntity res = new EmailResponseEntity();
	String username;
	String pwd;
	String OriginalPassword;
	Message[] array;
	int i;
	
	@PutMapping("/move/{fromFolder}/{toFolder}/{id}")
	public ResponseEntity moveMails(@PathVariable("fromFolder") String fromFolder, @PathVariable("id") Integer id,
			@PathVariable("toFolder") String toFolder, @RequestBody List<Email> email) throws Exception {
		Optional<User> user = userService.findById(id);
		
		      System.out.println("from folder " + fromFolder);
		      System.out.println("to folder " + toFolder);
		
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

		try {

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

			String folderName;
//        Folder[] folder = store.getDefaultFolder().list();
//        for (int k = 0; k <= folder.length; k++) {
//			if (folder.length > k) {
			if (fromFolder.equals("Draft")) {
				folderName = "Drafts";
			} else if (fromFolder.equals("Sent")) {
				folderName = "Sent";
			} else if (fromFolder.equals("Received")) {
				folderName = "INBOX";
			} else if (fromFolder.equals("Deleted")) {
				folderName = "Trash";
			} else {
				folderName = fromFolder;
			}

			String folderNameTo;
			if (toFolder.equals("Draft")) {
				folderNameTo = "Drafts";
			} else if (toFolder.equals("Sent")) {
				folderNameTo = "Sent";
			} else if (toFolder.equals("Received")) {
				folderNameTo = "INBOX";
			} else if (toFolder.equals("Deleted")) {
				folderNameTo = "Trash";
			} else {
				folderNameTo = toFolder;
			}
			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getIncomingHost(),username, OriginalPassword);

			List<Message> msgList = new ArrayList<>();

			for (Email e : email) {

				Folder emailFolderFrom = store.getFolder(folderName);
				Folder emailFolderTo = store.getFolder(folderNameTo);
				if (emailFolderFrom.getMessageCount() != 0) {
					emailFolderFrom.open(Folder.READ_WRITE);
					UIDFolder uf = (UIDFolder) emailFolderFrom;
					Message msg = uf.getMessageByUID(e.getId());
					if (msg == null) {
						System.out.println("if");
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.setErrorCode(404));
						jsonObject.put("message", res.setMessage("Email with id=" + e.getId() + " not found"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					} else {
						msgList.add(msg);
						array = msgList.toArray(new Message[i]);
						System.out.println("msg" + msg);
						System.out.println("emailFolderTo" + emailFolderTo.getFullName());
						emailFolderFrom.copyMessages(array, emailFolderTo);
						emailFolderFrom.setFlags(array, new Flags(Flags.Flag.DELETED), true);
						

					}
					msg.setFlag(Flags.Flag.DELETED, true);
					Message[] messageList = emailFolderFrom.expunge();
					System.out.println("messageList"+messageList.toString());
					emailFolderFrom.close(true);
				}
			}
			store.close();
		} catch (Exception e) {
			System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Move"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Emails moved successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}
	@PutMapping("/email-status/{userId}")
	public ResponseEntity updateSingleEmailStatus(@PathVariable("userId") Integer id, @RequestBody Email email)
			throws Exception {
		Optional<User> user = userService.findById(id);
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
			// RETRIVE ENCRYPTED PASSWORD
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		String folderName;
		if (email.getEmailType().equals("Draft")) {
			folderName = "Drafts";
		} else if (email.getEmailType().equals("Sent")) {
			folderName = "Sent";
		} else if (email.getEmailType().equals("Received")) {
			folderName = "INBOX";
		} else if (email.getEmailType().equals("Deleted")) {
			folderName = "Trash";
		} else {
			folderName = email.getEmailType();
		}

		try {
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
			Folder emailFolder = store.getFolder(folderName);
			if (emailFolder.getMessageCount() != 0) {
				emailFolder.open(Folder.READ_ONLY);
				UIDFolder uf = (UIDFolder) emailFolder;
				Message msg = uf.getMessageByUID(email.getId());
				if (msg == null) {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(404));
					jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				} else {
					store.close();
					store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
					emailFolder.open(Folder.READ_WRITE);
					if (email.getEmailStatus().equals("Read")) {
						msg.setFlag(Flag.SEEN, true);
					} else {
						msg.setFlag(Flag.SEEN, false);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Update EmailStatus"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("EmailStatus Updated Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}

	@PutMapping("/emailstatus/{userId}")
	public ResponseEntity updateMultipleEmailStatus(@PathVariable("userId") Integer id, @RequestBody List<Email> email)
			throws Exception {
		Optional<User> user = userService.findById(id);
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
			// RETRIVE ENCRYPTED PASSWORD
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		String folderName;
		for (Email e : email) {
			if (e.getEmailType().equals("Draft")) {
				folderName = "Drafts";
			} else if (e.getEmailType().equals("Sent")) {
				folderName = "Sent";
			} else if (e.getEmailType().equals("Received")) {
				folderName = "INBOX";
			} else if (e.getEmailType().equals("Deleted")) {
				folderName = "Trash";
			} else {
				folderName = e.getEmailType();
			}

			try {
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
				Folder emailFolder = store.getFolder(folderName);
				if (emailFolder.getMessageCount() != 0) {
					emailFolder.open(Folder.READ_ONLY);
					UIDFolder uf = (UIDFolder) emailFolder;
					Message msg = uf.getMessageByUID(e.getId());
					if (msg == null) {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.setErrorCode(404));
						jsonObject.put("message", res.setMessage("Email with id=" + e.getId() + " not found"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					} else {
						store.close();
						store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
						emailFolder.open(Folder.READ_WRITE);
						if (e.getEmailStatus().equals("Read")) {
							msg.setFlag(Flag.SEEN, true);
						} else {
							msg.setFlag(Flag.SEEN, false);
						}
					}
				}
			} catch (Exception ex) {
				System.out.println("ex"+ex);
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.FAILURE);
				jsonObject.put("message", res.setMessage("Unable to Update EmailStatus"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("EmailStatus Updated Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}
	@PutMapping("/priority/{userId}")
	public ResponseEntity updateSinglePriority(@PathVariable("userId") Integer id, @RequestBody Email email) throws Exception {
Optional<User> user = userService.findById(id);
		
		if(user.isPresent()) {
			 username = user.get().getUserName();
			 pwd = user.get().getPassword();
			
			//RETRIVE ENCRYPTED PASSWORD
		        byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
		        SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
		        Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
		        cipher.init(Cipher.DECRYPT_MODE, sks);
		        byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
		         OriginalPassword = new String(decrypted);
		        System.out.println(OriginalPassword);
		    	//RETRIVE ENCRYPTED PASSWORD

		}else {
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
		
        String folderName;
//        Folder[] folder = store.getDefaultFolder().list();
//        for (int k = 0; k <= folder.length; k++) {
//			if (folder.length > k) {
        if(email.getEmailType().equals("Draft")) {
        	 folderName = "Drafts";
        }else if(email.getEmailType().equals("Sent")) {
        	 folderName = "Sent";
        }else if(email.getEmailType().equals("Received")) {
        	 folderName = "INBOX";
        }else if(email.getEmailType().equals("Deleted")) {
        	 folderName = "Trash";
        }else {
        	 folderName = email.getEmailType();
        }
    	try {
        Store store = session.getStore("imaps");
        store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
				Folder emailFolder = store.getFolder(folderName);
				if (emailFolder.getMessageCount() != 0) {
					emailFolder.open(Folder.READ_ONLY);
					UIDFolder uf = (UIDFolder)emailFolder;
					Message msg = uf.getMessageByUID(email.getId());
					if(msg == null) {
						System.out.println("if");
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.setErrorCode(404));
						jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
						//System.out.println(jsonObject);
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}else {
						System.out.println("msg"+msg);
//						 Folder folder = store.getFolder(folderName);
//						emailFolder.open(Folder.READ_WRITE);
					        
					        store.close();
					        store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
					        emailFolder.open(Folder.READ_WRITE);
					        System.out.println("out");
					        if(email.getEmailPriority().equals("Important")) {
								System.out.println("imp");
								msg.setFlag(Flag.FLAGGED, true);
							}else {
								System.out.println("unimp");
								msg.setFlag(Flag.FLAGGED, false);
							}
					}
				
					}
					}catch(Exception e) {
						System.out.println("e"+e);
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.FAILURE);
						jsonObject.put("message", res.setMessage("Unable to Update Priority"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					
				}
        
    	
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.SUCCESS);
			jsonObject.put("description", res.setDescription("Priority Updated Successfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);
		
	}
	
}
