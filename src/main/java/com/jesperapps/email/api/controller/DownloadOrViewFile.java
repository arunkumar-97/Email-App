package com.jesperapps.email.api.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.jesperapps.email.api.model.MultipleAttachment;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class DownloadOrViewFile {

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;
	String username;
	String pwd;
	String OriginalPassword;
	EmailResponseEntity res = new EmailResponseEntity();
	byte[] byteArray;
	Message message;

	@GetMapping("/download/{fileName}/{emailType}/{userId}/{emailId}")
	public ResponseEntity<ByteArrayResource> download(@PathVariable("fileName") String fileName,
			@PathVariable("emailType") String emailType, @PathVariable("userId") Integer userId,
			@PathVariable("emailId") Long emailId) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, MessagingException, IOException {
		System.out.println("fileName:" + fileName);
		Optional<User> user = userService.findById(userId);
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
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
		Folder emailFolder = store.getFolder(folderName);
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_ONLY);
			UIDFolder uf = (UIDFolder) emailFolder;
			message = uf.getMessageByUID(emailId);
			if (message == null) {
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("Email with id=" + emailId + " not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			} else {
				Multipart multiPart = (Multipart) message.getContent();
				int numberOfParts = multiPart.getCount();
				for (int partCount = 0; partCount < numberOfParts; partCount++) {
					MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
						System.out.println("attach" + part.getFileName() + fileName);

						
						if (fileName.equals(part.getFileName()) == true) {
							InputStream fileData = part.getInputStream();
							ByteArrayOutputStream buffer = new ByteArrayOutputStream();
							int nRead;
							byte[] data = new byte[1024];
							while ((nRead = fileData.read(data, 0, data.length)) != -1) {
								buffer.write(data, 0, nRead);
							}
							buffer.flush();
							byteArray = buffer.toByteArray();
							System.out.println("attach1" + part.getFileName() + fileName);
							return ResponseEntity.ok().contentType(MediaType.parseMediaType(part.getContentType()))
									.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + part.getFileName() + "\"")
									.body(new ByteArrayResource(byteArray));
						}

					}
				}
			}
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("errorCode", 404);
		jsonObject.put("message", "image or file not found");
		return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
	}

	@GetMapping("/view/{fileName}/{emailType}/{userId}/{emailId}")
	public ResponseEntity<ByteArrayResource> view(@PathVariable("fileName") String fileName,
			@PathVariable("emailType") String emailType, @PathVariable("userId") Integer userId,
			@PathVariable("emailId") Long emailId) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, MessagingException, IOException {
		System.out.println("fileName"+fileName);
		Optional<User> user = userService.findById(userId);
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
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
		Folder emailFolder = store.getFolder(folderName);
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_ONLY);
			UIDFolder uf = (UIDFolder) emailFolder;
			message = uf.getMessageByUID(emailId);
			System.out.println("message" + message);
			if (message == null) {
				System.out.println("if");
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("Email with id=" + emailId + " not found"));
				// System.out.println(jsonObject);
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			} else {
				Multipart multiPart = (Multipart) message.getContent();
				int numberOfParts = multiPart.getCount();
				for (int partCount = 0; partCount < numberOfParts; partCount++) {
					MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
						System.out.println("attach" + part.getFileName() + fileName);

						
						if (fileName.equals(part.getFileName()) == true) {
							InputStream fileData = part.getInputStream();
							ByteArrayOutputStream buffer = new ByteArrayOutputStream();
							int nRead;
							byte[] data = new byte[1024];
							while ((nRead = fileData.read(data, 0, data.length)) != -1) {
								buffer.write(data, 0, nRead);
							}
							buffer.flush();
							byteArray = buffer.toByteArray();
							System.out.println("attach1" + part.getFileName() + fileName);
							return ResponseEntity.ok().contentType(MediaType.parseMediaType(part.getContentType()))
									.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + part.getFileName() + "\"")
									.body(new ByteArrayResource(byteArray));
						}

					}
				}
			}
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("errorCode", 404);
		jsonObject.put("message", "image or file not found");
		return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);

//		ObjectNode jsonObject = objectMapper.createObjectNode();
//		jsonObject.put("errorCode", 404);
//		jsonObject.put("message", "image or file not found");
//		return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);

	}
}
