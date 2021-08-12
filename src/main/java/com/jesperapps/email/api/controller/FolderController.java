package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.extra.GeneratePlainPassword;
import com.jesperapps.email.api.extra.HexStringToByteArray;
import com.jesperapps.email.api.message.EmailResponseEntity;
import com.jesperapps.email.api.model.EmailFolder;
import com.jesperapps.email.api.model.SubFolder;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class FolderController {

	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	EmailResponseEntity res = new EmailResponseEntity();
	String username;
	String pwd;
	String OriginalPassword;

	@PostMapping("/folder")
	public ResponseEntity createFolder(@RequestBody EmailFolder emailFolder) throws Exception {
		Optional<User> user = userService.findById(emailFolder.getUser().getId());
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
			return postFolder(user.get().getEmailConfiguration().getIncomingHost(),
					user.get().getEmailConfiguration().getIncomingProtocol(), username, OriginalPassword,
					user.get().getEmailConfiguration().getIncomingPort(),
					user.get().getEmailConfiguration().isAuthentication(),
					user.get().getEmailConfiguration().isSecurity(), emailFolder);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	private ResponseEntity postFolder(String incomingHost, String incomingProtocol, String username2,
			String originalPassword2, Integer incomingPort, boolean authentication, boolean security,
			EmailFolder emailFolder) {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", authentication);
			props.put("mail.smtp.starttls.enable", security);
			props.put("mail.smtp.host", incomingHost);
			props.put("mail.smtp.port", incomingPort);
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}
			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore(incomingProtocol);
			store.connect(incomingHost, incomingPort, username2, originalPassword2);

			String[] folderparts = emailFolder.getFolderName().split("/");
			Folder f = store.getDefaultFolder();
			System.out.println("f"+f);
			// Open destination folder
			for (int i = 0; i < folderparts.length; i++) {
				System.out.println("folderparts[i]"+folderparts[i]);
				f = f.getFolder(folderparts[i]);
				if (!f.exists() == true) {
					// Create folder
					boolean isCreated = f.create(Folder.HOLDS_MESSAGES);
					if (true) {
						f.setSubscribed(emailFolder.getStatus());
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("statusCode", res.SUCCESS);
						jsonObject.put("description", res.setDescription("Folder Created Successfully"));
						return new ResponseEntity(jsonObject, HttpStatus.OK);
					} else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.FAILURE);
						jsonObject.put("message", res.setMessage("Unable to Create Folder"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}

				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(409));
					jsonObject.put("message", res.setMessage("Folder Already Exists"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				}
			}
		} catch (Exception e) {

		}

		return null;
	}

	@PutMapping("/folder/{renameFolder}")
	public ResponseEntity updateFolder(@RequestBody EmailFolder emailFolder,
			@PathVariable("renameFolder") String renameFolder) throws Exception {
		Optional<User> user = userService.findById(emailFolder.getUser().getId());
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
			return putFolder(user.get().getEmailConfiguration().getIncomingHost(),
					user.get().getEmailConfiguration().getIncomingProtocol(), username, OriginalPassword,
					user.get().getEmailConfiguration().getIncomingPort(),
					user.get().getEmailConfiguration().isAuthentication(),
					user.get().getEmailConfiguration().isSecurity(), emailFolder, renameFolder);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	private ResponseEntity putFolder(String incomingHost, String incomingProtocol, String username2,
			String originalPassword2, Integer incomingPort, boolean authentication, boolean security,
			EmailFolder emailFolder, String renameFolder) {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", authentication);
			props.put("mail.smtp.starttls.enable", security);
			props.put("mail.smtp.host", incomingHost);
			props.put("mail.smtp.port", incomingPort);
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}
			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore(incomingProtocol);
			store.connect(incomingHost, incomingPort, username2, originalPassword2);

			String[] folderparts = emailFolder.getFolderName().split("/");

			Folder f = store.getDefaultFolder();
			// Open destination folder
			for (int i = 0; i < folderparts.length; i++) {
				System.out.println("folderparts" + folderparts[i]);
				f = f.getFolder(folderparts[i]);
//				f.setSubscribed(emailFolder.getStatus());
				System.out.println(emailFolder.getStatus()+"emailFolder.getStatus()");
				if (f.exists() & f.isSubscribed()) {
					 
					// Create folder
					Folder rf = store.getFolder(renameFolder);
					if(rf.exists()) {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.setErrorCode(409));
						jsonObject.put("message",
								res.setMessage("Folder already exist"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}else {
						boolean isRenamed = f.renameTo(rf);
						
						if (isRenamed) {
							
							rf.setSubscribed(emailFolder.getStatus());
							
							try {
								if ((rf.getType() & Folder.HOLDS_FOLDERS) != 0) {
									Folder[] f1 = rf.list();
									for (int i1 = 0; i1 < f1.length; i1++) {
										// Search for sub folders
										if ((f1[i1].getType() & Folder.HOLDS_FOLDERS) != 0) {
											f1[i1].setSubscribed(true);
										}
									}
								}
							} catch (MessagingException m) {
//							      throw new KettleException( m );
							}
							ObjectNode jsonObject = objectMapper.createObjectNode();
							jsonObject.put("statusCode", res.SUCCESS);
							jsonObject.put("description", res.setDescription("Folder Updated Successfully"));
							return new ResponseEntity(jsonObject, HttpStatus.OK);
						} else {
							ObjectNode jsonObject = objectMapper.createObjectNode();
							jsonObject.put("errorCode", res.FAILURE);
							jsonObject.put("message", res.setMessage("Unable to Update Folder"));
							return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
						}

					}
					
				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(404));
					jsonObject.put("message", res.setMessage("Folder Not Found"));
					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
				}
			}
		} catch (Exception e) {

		}

		return null;
	}

	@DeleteMapping("/folder")
	public ResponseEntity delFolder(@RequestBody EmailFolder emailFolder) throws Exception {
		Optional<User> user = userService.findById(emailFolder.getUser().getId());
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
			return deleteFolder(user.get().getEmailConfiguration().getIncomingHost(),
					user.get().getEmailConfiguration().getIncomingProtocol(), username, OriginalPassword,
					user.get().getEmailConfiguration().getIncomingPort(),
					user.get().getEmailConfiguration().isAuthentication(),
					user.get().getEmailConfiguration().isSecurity(), emailFolder);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	private ResponseEntity deleteFolder(String incomingHost, String incomingProtocol, String username2,
			String originalPassword2, Integer incomingPort, boolean authentication, boolean security,
			EmailFolder emailFolder) {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", authentication);
			props.put("mail.smtp.starttls.enable", security);
			props.put("mail.smtp.host", incomingHost);
			props.put("mail.smtp.port", incomingPort);
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}
			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore(incomingProtocol);
			store.connect(incomingHost, incomingPort, username2, originalPassword2);


			Folder f = store.getDefaultFolder();
				f = f.getFolder(emailFolder.getFolderName());
//				f.setSubscribed(emailFolder.getStatus());
				System.out.println("f"+f);
				System.out.println("ex"+f.exists()+f.isSubscribed());
				if (f.exists() & f.isSubscribed()) {
				
					if (f.getMessageCount() == 0 || f.list().length == 0) {
						// Create folder
						boolean isDeleted = f.delete(true);
						if (true) {
							ObjectNode jsonObject = objectMapper.createObjectNode();
							jsonObject.put("statusCode", res.SUCCESS);
							jsonObject.put("description", res.setDescription("Folder Deleted Successfully"));
							return new ResponseEntity(jsonObject, HttpStatus.OK);
						} else {
							ObjectNode jsonObject = objectMapper.createObjectNode();
							jsonObject.put("errorCode", res.FAILURE);
							jsonObject.put("message", res.setMessage("Unable to Delete Folder"));
							return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
						}
					} else {
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.FAILURE);
						jsonObject.put("message", res.setMessage("This Folder Has Emails or SubFolders"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}

				} else {
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(404));
					jsonObject.put("message", res.setMessage("Folder not found"));
					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
				}
			
		} catch (Exception e) {

		}

		return null;
	}

	@GetMapping("/folder/{userId}")
	public ResponseEntity folderList(@PathVariable("userId") Integer id) throws Exception {
		Optional<User> user = userService.findById(id);
		if (user.isPresent()) {
			String username = user.get().getUserName();
			String pwd = user.get().getPassword();

			// RETRIVE ENCRYPTED PASSWORD
			byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
			SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
			Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
			cipher.init(Cipher.DECRYPT_MODE, sks);
			byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
			String OriginalPassword = new String(decrypted);
			System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchFolderList(host, mailstoreType, username, OriginalPassword, port, auth, security);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchFolderList(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security) throws Exception {
		Properties properties = new Properties();
		properties.put("mail.store.protocol", mailStrProt);
		properties.put("mail.host", hostval);
		properties.put("mail.port", port);
		properties.put("mail.smtp.ssl.enable", security);
		Session emailSession = Session.getDefaultInstance(properties);
		Store store = emailSession.getStore(mailStrProt);
		store.connect(hostval, port, uname, password);

		List<EmailFolder> emailFolderList = new ArrayList<EmailFolder>();
		Folder[] f = store.getDefaultFolder().list();
		for (Folder fd : f) {

			List<SubFolder> subFolderList = new ArrayList<SubFolder>();
			if ((fd.getType() & Folder.HOLDS_FOLDERS) != 0) {
				Folder[] sf = fd.list();
				for (int i = 0; i < sf.length; i++) {
					// Search for sub folders
					if ((sf[i].getType() & Folder.HOLDS_FOLDERS) != 0) {
						SubFolder subFolder = new SubFolder(sf[i]);
						subFolderList.add(subFolder);
					}
				}
			}
			System.out.println(">> " + fd.getName());
			EmailFolder emailFolder = new EmailFolder(fd, subFolderList);
			emailFolderList.add(emailFolder);
		}

		if (emailFolderList.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity(emailFolderList, HttpStatus.OK);
		}

	}
	
	@GetMapping("/folder/{folderName}/{userId}")
	public ResponseEntity folderByFolderName(@PathVariable("folderName") String folderName,@PathVariable("userId") Integer id) throws Exception {
		Optional<User> user = userService.findById(id);

		if (user.isPresent()) {
			String username = user.get().getUserName();
			String pwd = user.get().getPassword();

			// RETRIVE ENCRYPTED PASSWORD
			byte[] bytekey = HexStringToByteArray.hexStringToByteArray(user.get().getKeyValue());
			SecretKeySpec sks = new SecretKeySpec(bytekey, GeneratePlainPassword.AES);
			Cipher cipher = Cipher.getInstance(GeneratePlainPassword.AES);
			cipher.init(Cipher.DECRYPT_MODE, sks);
			byte[] decrypted = cipher.doFinal(HexStringToByteArray.hexStringToByteArray(pwd));
			String OriginalPassword = new String(decrypted);
			System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchFolderByFolderName(host, mailstoreType, username, OriginalPassword, port, auth, security, folderName);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchFolderByFolderName(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security, String folderName) throws Exception {
		Properties properties = new Properties();
		properties.put("mail.store.protocol", mailStrProt);
		properties.put("mail.host", hostval);
		properties.put("mail.port", port);
		properties.put("mail.smtp.ssl.enable", security);
		Session emailSession = Session.getDefaultInstance(properties);
		Store store = emailSession.getStore(mailStrProt);
		store.connect(hostval, port, uname, password);

		Folder f = store.getDefaultFolder().getFolder(folderName);
			System.out.println(">> " + f.getName());
			EmailFolder emailFolder = new EmailFolder(f);
		if (emailFolder == null) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity(emailFolder, HttpStatus.OK);
		}

	}

}
