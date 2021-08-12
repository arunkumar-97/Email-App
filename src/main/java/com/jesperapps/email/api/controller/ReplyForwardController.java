package com.jesperapps.email.api.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.extra.ContentIdGenerator;
import com.jesperapps.email.api.extra.GeneratePlainPassword;
import com.jesperapps.email.api.extra.HexStringToByteArray;
import com.jesperapps.email.api.message.EmailResponseEntity;
import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.MultipleAttachment;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ReplyForwardController {

	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	EmailResponseEntity res = new EmailResponseEntity();
	String username;
	String pwd;
	String OriginalPassword;
	

	@PostMapping("/reply")
	public ResponseEntity replyEmail(@RequestBody Email email) throws Exception {
		Optional<User> user = userService.findById(email.getUser().getId());

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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		try {
		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		
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

		Session session = Session.getInstance(props, auth);
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> cidarray = new ArrayList<String>();
		Folder emailFolder = store.getFolder(folderName);
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_WRITE);
			UIDFolder uf = (UIDFolder) emailFolder;
			Message message = uf.getMessageByUID(email.getId());
			message.setFlag(Flag.ANSWERED, true);
//			 emailFolder.appendMessages(new Message[] {message});
//			message.setFlag(Flag.ANSWERED, true);
			   Message message2 = new MimeMessage(session);  
			    message2= (MimeMessage) message.reply(false);
			   message2.setSubject("RE: " + email.getSubject());  
			   message2.setFrom(new InternetAddress(email.getFrom()));  
			   
				List<InternetAddress> listTo = new ArrayList<InternetAddress>();
				for (String to : email.getTo()) {
					InternetAddress to1 = new InternetAddress(to);
					message2.setRecipient(Message.RecipientType.TO, to1);
					listTo.add(to1);
				}
				Address[] addressTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressTo);
			   
				List<InternetAddress> listReplyTo = new ArrayList<InternetAddress>();
				for (String replyTo : email.getReplyTo()) {
					InternetAddress replyTo1 = new InternetAddress(replyTo);
					message2.setRecipient(Message.RecipientType.TO, replyTo1);
					listReplyTo.add(replyTo1);
				}
				Address[] addressReplyTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressReplyTo);
				
				List<InternetAddress> listCc = new ArrayList<InternetAddress>();
				for (String cc : email.getCc()) {
					InternetAddress cc1 = new InternetAddress(cc);
					message2.setRecipient(Message.RecipientType.CC, cc1);
					listCc.add(cc1);
				}
				Address[] addressCc = listCc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.CC, addressCc);
				
				List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
				for (String bcc : email.getBcc()) {
					InternetAddress bcc1 = new InternetAddress(bcc);
					message2.setRecipient(Message.RecipientType.BCC, bcc1);
					listBcc.add(bcc1);
				}
				Address[] addressBcc = listBcc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.BCC, addressBcc);
			   
				Document document = Jsoup.parse(email.getBody());
				Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
			     if(images.isEmpty() == false)
			     {
			    	  //System.out.println("has inline");
			    	 MimeMultipart multipart = new MimeMultipart("related");
			    	 for ( org.jsoup.nodes.Element image :images )
			         {
						 // ContentID is used by both parts
						    String cid = ContentIdGenerator.getContentId();
						    //System.out.println("cid" + cid);
						    
			        	 String src = image.attr("src");
			        	//System.out.println("src " + src);
			        	 String[] base64string = src.split(",");
			        	 String[] datatype = src.split(";");
			        	// //System.out.println("datatype " + base64string[1]);
			        	// //System.out.println("base64string " + datatype[1]);
			        	// //System.out.println("file encoding " + datatype[0]);		        
			        	image.attr("src","cid:" + cid);      
			        	base64array.add(base64string[1]);
			        	cidarray.add(cid);	        				
			         }
			    	 
			    	 //System.out.println("html string " + document.toString());
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			  		  multipart.addBodyPart(messageBodyPart);
			  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
			  		  if(base64array.isEmpty() == false )
			  		  {
			  			if( base64array.size() > 1)
			  	         {
			  	  			for ( int i=0; i<base64array.size(); ++i)
			  		        {
			  		  			messageBodyPart = new MimeBodyPart(); 
			  	                    
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
			  		  			//System.out.println("decodedString"+decodedString);
			  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
			  		  			     
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  		        }
			  	         }else 
			  	         {
			  	        	 messageBodyPart = new MimeBodyPart(); 
			  	             
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  	         }
			  		  }
			  		 
			
					    //System.out.println("htmlPart " + messageBodyPart.getContent());
					    
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }else 
			     {    
			    	 //System.out.println("no   inline");
			    	 MimeMultipart multipart = new MimeMultipart();
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }
			 
				
				Folder folder = store.getFolder("Sent");
		        folder.open(Folder.READ_WRITE);
		        message2.setFlag(Flag.SEEN, true);
//		        reply.setFlag(Flag.ANSWERED, true);
		    	//System.out.println("buf1");
		        folder.appendMessages(new Message[] {message2});
		    	//System.out.println("buf2");
//				Folder folder1 = store.getFolder("Inbox");
//				emailFolder.open(Folder.READ_WRITE);
			   // Send message  
			   Transport.send(message2);  

		}
		}catch(Exception e) {
			//System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Reply to Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Email Replied Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);

	}
	@PostMapping("/reply/all")
	public ResponseEntity replyAllEmail(@RequestBody Email email) throws Exception {
		Optional<User> user = userService.findById(email.getUser().getId());

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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		try {
		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		
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

		Session session = Session.getInstance(props, auth);
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> cidarray = new ArrayList<String>();
		Folder emailFolder = store.getFolder(folderName);
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_WRITE);
			UIDFolder uf = (UIDFolder) emailFolder;
			Message message = uf.getMessageByUID(email.getId());
			message.setFlag(Flag.ANSWERED, true);
//			 emailFolder.appendMessages(new Message[] {message});
//			message.setFlag(Flag.ANSWERED, true);
			   Message message2 = new MimeMessage(session);  
			    message2= (MimeMessage) message.reply(true);
			   message2.setSubject("RE: " + email.getSubject());  
			   message2.setFrom(new InternetAddress(email.getFrom()));  
			   
				List<InternetAddress> listTo = new ArrayList<InternetAddress>();
				for (String to : email.getTo()) {
					InternetAddress to1 = new InternetAddress(to);
					message2.setRecipient(Message.RecipientType.TO, to1);
					listTo.add(to1);
				}
				Address[] addressTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressTo);
			   
				List<InternetAddress> listReplyTo = new ArrayList<InternetAddress>();
				for (String replyTo : email.getReplyTo()) {
					InternetAddress replyTo1 = new InternetAddress(replyTo);
					message2.setRecipient(Message.RecipientType.TO, replyTo1);
					listReplyTo.add(replyTo1);
				}
				Address[] addressReplyTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressReplyTo);
				
				List<InternetAddress> listCc = new ArrayList<InternetAddress>();
				for (String cc : email.getCc()) {
					InternetAddress cc1 = new InternetAddress(cc);
					message2.setRecipient(Message.RecipientType.CC, cc1);
					listCc.add(cc1);
				}
				Address[] addressCc = listCc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.CC, addressCc);
				
				List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
				for (String bcc : email.getBcc()) {
					InternetAddress bcc1 = new InternetAddress(bcc);
					message2.setRecipient(Message.RecipientType.BCC, bcc1);
					listBcc.add(bcc1);
				}
				Address[] addressBcc = listBcc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.BCC, addressBcc);
			   
				Document document = Jsoup.parse(email.getBody());
				 Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
							     if(images.isEmpty() == false)
							     {
							    	  //System.out.println("has inline");
							    	 MimeMultipart multipart = new MimeMultipart("related");
							    	 for ( org.jsoup.nodes.Element image :images )
							         {
										 // ContentID is used by both parts
										    String cid = ContentIdGenerator.getContentId();
										    //System.out.println("cid" + cid);
										    
							        	 String src = image.attr("src");
							        	//System.out.println("src " + src);
							        	 String[] base64string = src.split(",");
							        	 String[] datatype = src.split(";");
							        	// //System.out.println("datatype " + base64string[1]);
							        	// //System.out.println("base64string " + datatype[1]);
							        	// //System.out.println("file encoding " + datatype[0]);		        
							        	image.attr("src","cid:" + cid);      
							        	base64array.add(base64string[1]);
							        	cidarray.add(cid);	        				
							         }
							    	 
							    	 //System.out.println("html string " + document.toString());
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							  		  multipart.addBodyPart(messageBodyPart);
							  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
							  		  if(base64array.isEmpty() == false )
							  		  {
							  			if( base64array.size() > 1)
							  	         {
							  	  			for ( int i=0; i<base64array.size(); ++i)
							  		        {
							  		  			messageBodyPart = new MimeBodyPart(); 
							  	                    
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
							  		  			//System.out.println("decodedString"+decodedString);
							  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
							  		  			     
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  		        }
							  	         }else 
							  	         {
							  	        	 messageBodyPart = new MimeBodyPart(); 
							  	             
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  	         }
							  		  }
							  		 
							
									    //System.out.println("htmlPart " + messageBodyPart.getContent());
									    
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }else 
							     {    
							    	 //System.out.println("no   inline");
							    	 MimeMultipart multipart = new MimeMultipart();
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }
							 
				Folder folder = store.getFolder("Sent");
		        folder.open(Folder.READ_WRITE);
		        message2.setFlag(Flag.SEEN, true);
//		        reply.setFlag(Flag.ANSWERED, true);
		    	//System.out.println("buf1");
		        folder.appendMessages(new Message[] {message2});
		    	//System.out.println("buf2");
//				Folder folder1 = store.getFolder("Inbox");
//				emailFolder.open(Folder.READ_WRITE);
			   // Send message  
			   Transport.send(message2);  

		}
		}catch(Exception e) {
			//System.out.println("e1"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Reply to Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Email Replied Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	


	}
	
	@PostMapping("/forward")
	public ResponseEntity forwardEmail(@RequestBody Email email ) throws Exception {

		Optional<User> user = userService.findById(email.getUser().getId());

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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		//System.out.println("1");
		try {
			ArrayList<String> base64array = new ArrayList<String>();
			ArrayList<String> cidarray = new ArrayList<String>();
			// Get the session object
			Properties props = new Properties();
			props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
			props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}

			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
			//System.out.println("2");
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
			Folder emailFolder = store.getFolder(folderName);
			if (emailFolder.getMessageCount() != 0) {
				//System.out.println("3");
				emailFolder.open(Folder.READ_WRITE);

				UIDFolder uf = (UIDFolder) emailFolder;
				Message message = uf.getMessageByUID(email.getId());


				//			Flags processedFlag = new Flags("Forwarded");
//			emailFolder.s.setFlags(processedFlag, true);
//			// or
//			message.setFlags(processedFlag, true);
	
				//System.out.println("6");
				Flags flagsToSet = new Flags();
				 flagsToSet.add("Forwarded");
				  //System.out.println("7");
					message.setFlags(flagsToSet, true);
					//System.out.println("8");
				// Get all the information from the message
				String from = InternetAddress.toString(message.getFrom());
				if (from != null) {
					//System.out.println("From: " + from);
				}
				String replyTo = InternetAddress.toString(message.getReplyTo());
				if (replyTo != null) {
					//System.out.println("Reply-to: " + replyTo);
				}
				String to = InternetAddress.toString(message.getRecipients(Message.RecipientType.TO));
				if (to != null) {
					//System.out.println("To: " + to);
				}

				String subject = message.getSubject();
				if (subject != null) {
					//System.out.println("Subject: " + subject);
				}
				Date sent = message.getSentDate();
				if (sent != null) {
					//System.out.println("Sent: " + sent);
				}
				//System.out.println(message.getContent());

				// compose the message to forward
				Message message2 = new MimeMessage(session);
				message2.setSubject("Fwd: " + email.getSubject());
				message2.setFrom(new InternetAddress(email.getFrom()));
				for (int i = 0; i < email.getTo().length; i++) {
					//System.out.println("for" + email.getTo()[i]);
					message2.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo()[i]));
				}
				for (int i = 0; i < email.getCc().length; i++) {
					//System.out.println("for" + email.getCc()[i]);
					message2.addRecipient(Message.RecipientType.CC, new InternetAddress(email.getCc()[i]));
				}
				// Create your new message part
				Document document = Jsoup.parse(email.getBody());
				 Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
							     if(images.isEmpty() == false)
							     {
							    	  //System.out.println("has inline");
							    	 MimeMultipart multipart = new MimeMultipart("related");
							    	 for ( org.jsoup.nodes.Element image :images )
							         {
										 // ContentID is used by both parts
										    String cid = ContentIdGenerator.getContentId();
										    //System.out.println("cid" + cid);
										    
							        	 String src = image.attr("src");
							        	//System.out.println("src " + src);
							        	 String[] base64string = src.split(",");
							        	 String[] datatype = src.split(";");
							        	// //System.out.println("datatype " + base64string[1]);
							        	// //System.out.println("base64string " + datatype[1]);
							        	// //System.out.println("file encoding " + datatype[0]);		        
							        	image.attr("src","cid:" + cid);      
							        	base64array.add(base64string[1]);
							        	cidarray.add(cid);	        				
							         }
							    	 
							    	 //System.out.println("html string " + document.toString());
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							  		  multipart.addBodyPart(messageBodyPart);
							  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
							  		  if(base64array.isEmpty() == false )
							  		  {
							  			if( base64array.size() > 1)
							  	         {
							  	  			for ( int i=0; i<base64array.size(); ++i)
							  		        {
							  		  			messageBodyPart = new MimeBodyPart(); 
							  	                    
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
							  		  			//System.out.println("decodedString"+decodedString);
							  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
							  		  			     
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  		        }
							  	         }else 
							  	         {
							  	        	 messageBodyPart = new MimeBodyPart(); 
							  	             
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  	         }
							  		  }
							  		 
							
									    //System.out.println("htmlPart " + messageBodyPart.getContent());
									    
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }else 
							     {    
							    	 //System.out.println("no   inline");
							    	 MimeMultipart multipart = new MimeMultipart();
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }
							 
				
				Folder folder = store.getFolder("Sent");
				folder.open(Folder.READ_WRITE);
				message2.setFlag(Flag.SEEN, true);
//			        message2.setFlag(Flag.ANSWERED, true);
				//System.out.println("buf1");
				folder.appendMessages(new Message[] { message2 });
				//System.out.println("45");

//			        emailFolder.open(Folder.READ_WRITE);
//					  
//					  emailFolder.appendMessages(new Message[] {message});
//					//System.out.println("5");
//			        folder1.appendMessages(new Message[] {message});
				// Send message
				Transport.send(message2);

				//System.out.println("message forwarded ....");
			}
		
		} catch (Exception e) {
			//System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to forward Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Email Forwarded Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);

	}
	
	
	
	@PostMapping("/reply/draft")
	public ResponseEntity replydraftEmail(@RequestBody Email email) throws Exception {
		
		Optional<User> user = userService.findById(email.getUser().getId());
	    long messageuid = 0 ;
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		try {
		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		
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

		Session session = Session.getInstance(props, auth);
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> cidarray = new ArrayList<String>();
		Folder emailFolder = store.getFolder(folderName);
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_WRITE);
			UIDFolder uf = (UIDFolder) emailFolder;
			Message message = uf.getMessageByUID(email.getId());
			message.setFlag(Flag.ANSWERED, true);
//			 emailFolder.appendMessages(new Message[] {message});
//			message.setFlag(Flag.ANSWERED, true);
			   Message message2 = new MimeMessage(session);  
			    message2= (MimeMessage) message.reply(false);
			   message2.setSubject("RE: " + email.getSubject());  
			   message2.setFrom(new InternetAddress(email.getFrom()));  
			   
				List<InternetAddress> listTo = new ArrayList<InternetAddress>();
				for (String to : email.getTo()) {
					InternetAddress to1 = new InternetAddress(to);
					message2.setRecipient(Message.RecipientType.TO, to1);
					listTo.add(to1);
				}
				Address[] addressTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressTo);
			   
				List<InternetAddress> listReplyTo = new ArrayList<InternetAddress>();
				for (String replyTo : email.getReplyTo()) {
					InternetAddress replyTo1 = new InternetAddress(replyTo);
					message2.setRecipient(Message.RecipientType.TO, replyTo1);
					listReplyTo.add(replyTo1);
				}
				Address[] addressReplyTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressReplyTo);
				
				List<InternetAddress> listCc = new ArrayList<InternetAddress>();
				for (String cc : email.getCc()) {
					InternetAddress cc1 = new InternetAddress(cc);
					message2.setRecipient(Message.RecipientType.CC, cc1);
					listCc.add(cc1);
				}
				Address[] addressCc = listCc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.CC, addressCc);
				
				List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
				for (String bcc : email.getBcc()) {
					InternetAddress bcc1 = new InternetAddress(bcc);
					message2.setRecipient(Message.RecipientType.BCC, bcc1);
					listBcc.add(bcc1);
				}
				Address[] addressBcc = listBcc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.BCC, addressBcc);
			   
				Document document = Jsoup.parse(email.getBody());
				Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
			     if(images.isEmpty() == false)
			     {
			    	  //System.out.println("has inline");
			    	 MimeMultipart multipart = new MimeMultipart("related");
			    	 for ( org.jsoup.nodes.Element image :images )
			         {
						 // ContentID is used by both parts
						    String cid = ContentIdGenerator.getContentId();
						    //System.out.println("cid" + cid);
						    
			        	 String src = image.attr("src");
			        	//System.out.println("src " + src);
			        	 String[] base64string = src.split(",");
			        	 String[] datatype = src.split(";");
			        	// //System.out.println("datatype " + base64string[1]);
			        	// //System.out.println("base64string " + datatype[1]);
			        	// //System.out.println("file encoding " + datatype[0]);		        
			        	image.attr("src","cid:" + cid);      
			        	base64array.add(base64string[1]);
			        	cidarray.add(cid);	        				
			         }
			    	 
			    	 //System.out.println("html string " + document.toString());
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			  		  multipart.addBodyPart(messageBodyPart);
			  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
			  		  if(base64array.isEmpty() == false )
			  		  {
			  			if( base64array.size() > 1)
			  	         {
			  	  			for ( int i=0; i<base64array.size(); ++i)
			  		        {
			  		  			messageBodyPart = new MimeBodyPart(); 
			  	                    
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
			  		  			//System.out.println("decodedString"+decodedString);
			  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
			  		  			     
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  		        }
			  	         }else 
			  	         {
			  	        	 messageBodyPart = new MimeBodyPart(); 
			  	             
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  	         }
			  		  }
			  		 
			
					    //System.out.println("htmlPart " + messageBodyPart.getContent());
					    
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }else 
			     {    
			    	 //System.out.println("no   inline");
			    	 MimeMultipart multipart = new MimeMultipart();
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }
			 
			
			     message2.saveChanges();
			     try {
						Store storedraft = session.getStore("imaps");
						storedraft.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
						//System.out.println("fol:" + storedraft.getFolder("Drafts"));
//				        store.connect(email.getHost(), email.getPort(), email.getFrom(), email.getPassword());
						Folder folder = store.getFolder("Drafts");
						folder.open(Folder.READ_WRITE);
						message2.setFlag(Flag.SEEN, true);
						folder.appendMessages(new Message[] { message2 });
						
						Message[] messages = folder.getMessages();
						Message draftmessage = messages[messages.length-1];

						FetchProfile fp = new FetchProfile();
						fp.add(UIDFolder.FetchProfileItem.UID);
						folder.fetch(folder.getMessages(), fp);
						UIDFolder pf = (UIDFolder) folder;
						messageuid = pf.getUID(draftmessage);
						
					////System.out.println("uid"+messageuid);
						store.close();
					} catch (Exception ex) {
						//System.out.println("e" + ex);
						ObjectNode jsonObject = objectMapper.createObjectNode();
						jsonObject.put("errorCode", res.FAILURE);
						jsonObject.put("message", res.setDescription("Unable to Save Email as Draft"));
						return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
					}

		}
		}catch(Exception e) {
			//System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Reply to Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Draft Saved Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);

	}

	@PostMapping("/reply/draft/send")
public ResponseEntity replydraftsendEmail(@RequestBody Email email) throws Exception {
		
		Optional<User> user = userService.findById(email.getUser().getId());
	    long messageuid = 0 ;
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		try {
		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		
	
		Session session = Session.getInstance(props, auth);
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> cidarray = new ArrayList<String>();
		Folder emailFolder = store.getFolder("Drafts");
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_WRITE);
			UIDFolder uf = (UIDFolder) emailFolder;
			Message message = uf.getMessageByUID(email.getId());
			message.setFlag(Flag.ANSWERED, true);
//			 emailFolder.appendMessages(new Message[] {message});
//			message.setFlag(Flag.ANSWERED, true);
			   Message message2 = new MimeMessage(session);  
			    message2= (MimeMessage) message.reply(false);
			   message2.setSubject( email.getSubject());  
			   message2.setFrom(new InternetAddress(email.getFrom()));  
			   
				List<InternetAddress> listTo = new ArrayList<InternetAddress>();
				for (String to : email.getTo()) {
					InternetAddress to1 = new InternetAddress(to);
					message2.setRecipient(Message.RecipientType.TO, to1);
					listTo.add(to1);
				}
				Address[] addressTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressTo);
			   
				List<InternetAddress> listReplyTo = new ArrayList<InternetAddress>();
				for (String replyTo : email.getReplyTo()) {
					InternetAddress replyTo1 = new InternetAddress(replyTo);
					message2.setRecipient(Message.RecipientType.TO, replyTo1);
					listReplyTo.add(replyTo1);
				}
				Address[] addressReplyTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressReplyTo);
				
				List<InternetAddress> listCc = new ArrayList<InternetAddress>();
				for (String cc : email.getCc()) {
					InternetAddress cc1 = new InternetAddress(cc);
					message2.setRecipient(Message.RecipientType.CC, cc1);
					listCc.add(cc1);
				}
				Address[] addressCc = listCc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.CC, addressCc);
				
				List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
				for (String bcc : email.getBcc()) {
					InternetAddress bcc1 = new InternetAddress(bcc);
					message2.setRecipient(Message.RecipientType.BCC, bcc1);
					listBcc.add(bcc1);
				}
				Address[] addressBcc = listBcc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.BCC, addressBcc);
			   
				Document document = Jsoup.parse(email.getBody());
				Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
			     if(images.isEmpty() == false)
			     {
			    	  //System.out.println("has inline");
			    	 MimeMultipart multipart = new MimeMultipart("related");
			    	 for ( org.jsoup.nodes.Element image :images )
			         {
						 // ContentID is used by both parts
						    String cid = ContentIdGenerator.getContentId();
						    //System.out.println("cid" + cid);
						    
			        	 String src = image.attr("src");
			        	//System.out.println("src " + src);
			        	 String[] base64string = src.split(",");
			        	 String[] datatype = src.split(";");
			        	// //System.out.println("datatype " + base64string[1]);
			        	// //System.out.println("base64string " + datatype[1]);
			        	// //System.out.println("file encoding " + datatype[0]);		        
			        	image.attr("src","cid:" + cid);      
			        	base64array.add(base64string[1]);
			        	cidarray.add(cid);	        				
			         }
			    	 
			    	 //System.out.println("html string " + document.toString());
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			  		  multipart.addBodyPart(messageBodyPart);
			  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
			  		  if(base64array.isEmpty() == false )
			  		  {
			  			if( base64array.size() > 1)
			  	         {
			  	  			for ( int i=0; i<base64array.size(); ++i)
			  		        {
			  		  			messageBodyPart = new MimeBodyPart(); 
			  	                    
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
			  		  			//System.out.println("decodedString"+decodedString);
			  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
			  		  			     
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  		        }
			  	         }else 
			  	         {
			  	        	 messageBodyPart = new MimeBodyPart(); 
			  	             
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  	         }
			  		  }
			  		 
			
					    //System.out.println("htmlPart " + messageBodyPart.getContent());
					    
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }else 
			     {    
			    	 //System.out.println("no   inline");
			    	 MimeMultipart multipart = new MimeMultipart();
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }
			 
			
			     message2.saveChanges();

					Folder folder = store.getFolder("Sent");
					folder.open(Folder.READ_WRITE);
					message2.setFlag(Flag.SEEN, true);
					folder.appendMessages(new Message[] { message2 });

					Transport.send(message2);
					//System.out.println("4");
					Folder emailFolder1 = store.getFolder("Drafts");
					//System.out.println("2");
					if (emailFolder1.getMessageCount() != 0) {
						//System.out.println("000");
						emailFolder1.open(Folder.READ_WRITE);
						UIDFolder uf1 = (UIDFolder) emailFolder1;
						Message msg1 = uf1.getMessageByUID(email.getId());
						if (msg1 == null) {
//							//System.out.println("if");
//							ObjectNode jsonObject = objectMapper.createObjectNode();
//							jsonObject.put("errorCode", res.setErrorCode(404));
//							jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//							// //System.out.println(jsonObject);
//							return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
						} else {
							//System.out.println("1");

							msg1.setFlag(Flags.Flag.DELETED, true);
							Message[] messageList = emailFolder.expunge();
							//System.out.println("messageList" + messageList);
							emailFolder.close(true);

						}

					}

					Folder emailFolder2 = store.getFolder("Trash");
					//System.out.println("2");
					if (emailFolder1.getMessageCount() != 0) {
						//System.out.println("000");
						emailFolder2.open(Folder.READ_WRITE);
						UIDFolder uff = (UIDFolder) emailFolder2;
						Message msg1 = uff.getMessageByUID(email.getId());
						if (msg1 == null) {
							//System.out.println("if");
//							ObjectNode jsonObject = objectMapper.createObjectNode();
//							jsonObject.put("errorCode", res.setErrorCode(404));
//							jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//							// //System.out.println(jsonObject);
//							return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
						} else {
							//System.out.println("1");

							msg1.setFlag(Flags.Flag.DELETED, true);
							Message[] messageList = emailFolder.expunge();
							//System.out.println("messageList" + messageList);
							emailFolder.close(true);

						}

					}
					store.close();

		}
		}catch(Exception e) {
			//System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Reply to Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Reply Send Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);

	}
	
	
	@PostMapping("/forward/draft")
	public ResponseEntity forwardDraftEmail(@RequestBody Email email ) throws Exception {
		 long messageuid = 0 ;
		Optional<User> user = userService.findById(email.getUser().getId());

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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		//System.out.println("1");
		try {
			ArrayList<String> base64array = new ArrayList<String>();
			ArrayList<String> cidarray = new ArrayList<String>();
			// Get the session object
			Properties props = new Properties();
			props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
			props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
			props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, OriginalPassword);
				}

			};
			Session session = Session.getInstance(props, auth);
			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
			//System.out.println("2");
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
			Folder emailFolder = store.getFolder(folderName);
			if (emailFolder.getMessageCount() != 0) {
				//System.out.println("3");
				emailFolder.open(Folder.READ_WRITE);

				UIDFolder uf = (UIDFolder) emailFolder;
				Message message = uf.getMessageByUID(email.getId());


				//			Flags processedFlag = new Flags("Forwarded");
//			emailFolder.s.setFlags(processedFlag, true);
//			// or
//			message.setFlags(processedFlag, true);
	
				//System.out.println("6");
				Flags flagsToSet = new Flags();
				 flagsToSet.add("Forwarded");
				  //System.out.println("7");
					message.setFlags(flagsToSet, true);
					//System.out.println("8");
				// Get all the information from the message
				String from = InternetAddress.toString(message.getFrom());
				if (from != null) {
					//System.out.println("From: " + from);
				}
				String replyTo = InternetAddress.toString(message.getReplyTo());
				if (replyTo != null) {
					//System.out.println("Reply-to: " + replyTo);
				}
				String to = InternetAddress.toString(message.getRecipients(Message.RecipientType.TO));
				if (to != null) {
					//System.out.println("To: " + to);
				}

				String subject = message.getSubject();
				if (subject != null) {
					//System.out.println("Subject: " + subject);
				}
				Date sent = message.getSentDate();
				if (sent != null) {
					//System.out.println("Sent: " + sent);
				}
				//System.out.println(message.getContent());

				// compose the message to forward
				Message message2 = new MimeMessage(session);
				message2.setSubject("Fwd: " + email.getSubject());
				message2.setFrom(new InternetAddress(email.getFrom()));
				for (int i = 0; i < email.getTo().length; i++) {
					//System.out.println("for" + email.getTo()[i]);
					message2.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getTo()[i]));
				}
				for (int i = 0; i < email.getCc().length; i++) {
					//System.out.println("for" + email.getCc()[i]);
					message2.addRecipient(Message.RecipientType.CC, new InternetAddress(email.getCc()[i]));
				}
				// Create your new message part
				Document document = Jsoup.parse(email.getBody());
				 Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
							     if(images.isEmpty() == false)
							     {
							    	  //System.out.println("has inline");
							    	 MimeMultipart multipart = new MimeMultipart("related");
							    	 for ( org.jsoup.nodes.Element image :images )
							         {
										 // ContentID is used by both parts
										    String cid = ContentIdGenerator.getContentId();
										    //System.out.println("cid" + cid);
										    
							        	 String src = image.attr("src");
							        	//System.out.println("src " + src);
							        	 String[] base64string = src.split(",");
							        	 String[] datatype = src.split(";");
							        	// //System.out.println("datatype " + base64string[1]);
							        	// //System.out.println("base64string " + datatype[1]);
							        	// //System.out.println("file encoding " + datatype[0]);		        
							        	image.attr("src","cid:" + cid);      
							        	base64array.add(base64string[1]);
							        	cidarray.add(cid);	        				
							         }
							    	 
							    	 //System.out.println("html string " + document.toString());
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							  		  multipart.addBodyPart(messageBodyPart);
							  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
							  		  if(base64array.isEmpty() == false )
							  		  {
							  			if( base64array.size() > 1)
							  	         {
							  	  			for ( int i=0; i<base64array.size(); ++i)
							  		        {
							  		  			messageBodyPart = new MimeBodyPart(); 
							  	                    
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
							  		  			//System.out.println("decodedString"+decodedString);
							  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
							  		  			     
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  		        }
							  	         }else 
							  	         {
							  	        	 messageBodyPart = new MimeBodyPart(); 
							  	             
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  	         }
							  		  }
							  		 
							
									    //System.out.println("htmlPart " + messageBodyPart.getContent());
									    
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }else 
							     {    
							    	 //System.out.println("no   inline");
							    	 MimeMultipart multipart = new MimeMultipart();
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }
							 
				
							     message2.saveChanges();
							     try {
										Store storedraft = session.getStore("imaps");
										storedraft.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
										//System.out.println("fol:" + storedraft.getFolder("Drafts"));
//								        store.connect(email.getHost(), email.getPort(), email.getFrom(), email.getPassword());
										Folder folder = store.getFolder("Drafts");
										folder.open(Folder.READ_WRITE);
										message2.setFlag(Flag.SEEN, true);
										folder.appendMessages(new Message[] { message2 });
										
										Message[] messages = folder.getMessages();
										Message draftmessage = messages[messages.length-1];

										FetchProfile fp = new FetchProfile();
										fp.add(UIDFolder.FetchProfileItem.UID);
										folder.fetch(folder.getMessages(), fp);
										UIDFolder pf = (UIDFolder) folder;
										messageuid = pf.getUID(draftmessage);
										
									////System.out.println("uid"+messageuid);
										store.close();
									} catch (Exception ex) {
										//System.out.println("e" + ex);
										ObjectNode jsonObject = objectMapper.createObjectNode();
										jsonObject.put("errorCode", res.FAILURE);
										jsonObject.put("message", res.setDescription("Unable to Save Email as Draft"));
										return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
									}
			}
		
		} catch (Exception e) {
			System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Save Draft"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Draft Saved Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);

	}
	
	
	
	@PostMapping("/forward/draft/send")
public ResponseEntity forwarddraftsendEmail(@RequestBody Email email) throws Exception {
		
		Optional<User> user = userService.findById(email.getUser().getId());
	    long messageuid = 0 ;
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		try {
		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		
	
		Session session = Session.getInstance(props, auth);
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> cidarray = new ArrayList<String>();
		Folder emailFolder = store.getFolder("Drafts");
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_WRITE);
			UIDFolder uf = (UIDFolder) emailFolder;
			Message message = uf.getMessageByUID(email.getId());
//			 emailFolder.appendMessages(new Message[] {message});
//			message.setFlag(Flag.ANSWERED, true);
			   Message message2 = new MimeMessage(session);  
			    message2= (MimeMessage) message.reply(false);
			   message2.setSubject( email.getSubject());  
			   message2.setFrom(new InternetAddress(email.getFrom()));  
			   
				List<InternetAddress> listTo = new ArrayList<InternetAddress>();
				for (String to : email.getTo()) {
					InternetAddress to1 = new InternetAddress(to);
					message2.setRecipient(Message.RecipientType.TO, to1);
					listTo.add(to1);
				}
				Address[] addressTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressTo);
			   
				List<InternetAddress> listReplyTo = new ArrayList<InternetAddress>();
				for (String replyTo : email.getReplyTo()) {
					InternetAddress replyTo1 = new InternetAddress(replyTo);
					message2.setRecipient(Message.RecipientType.TO, replyTo1);
					listReplyTo.add(replyTo1);
				}
				Address[] addressReplyTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressReplyTo);
				
				List<InternetAddress> listCc = new ArrayList<InternetAddress>();
				for (String cc : email.getCc()) {
					InternetAddress cc1 = new InternetAddress(cc);
					message2.setRecipient(Message.RecipientType.CC, cc1);
					listCc.add(cc1);
				}
				Address[] addressCc = listCc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.CC, addressCc);
				
				List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
				for (String bcc : email.getBcc()) {
					InternetAddress bcc1 = new InternetAddress(bcc);
					message2.setRecipient(Message.RecipientType.BCC, bcc1);
					listBcc.add(bcc1);
				}
				Address[] addressBcc = listBcc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.BCC, addressBcc);
			   
				Document document = Jsoup.parse(email.getBody());
				Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
			     if(images.isEmpty() == false)
			     {
			    	  //System.out.println("has inline");
			    	 MimeMultipart multipart = new MimeMultipart("related");
			    	 for ( org.jsoup.nodes.Element image :images )
			         {
						 // ContentID is used by both parts
						    String cid = ContentIdGenerator.getContentId();
						    //System.out.println("cid" + cid);
						    
			        	 String src = image.attr("src");
			        	//System.out.println("src " + src);
			        	 String[] base64string = src.split(",");
			        	 String[] datatype = src.split(";");
			        	// //System.out.println("datatype " + base64string[1]);
			        	// //System.out.println("base64string " + datatype[1]);
			        	// //System.out.println("file encoding " + datatype[0]);		        
			        	image.attr("src","cid:" + cid);      
			        	base64array.add(base64string[1]);
			        	cidarray.add(cid);	        				
			         }
			    	 
			    	 //System.out.println("html string " + document.toString());
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			  		  multipart.addBodyPart(messageBodyPart);
			  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
			  		  if(base64array.isEmpty() == false )
			  		  {
			  			if( base64array.size() > 1)
			  	         {
			  	  			for ( int i=0; i<base64array.size(); ++i)
			  		        {
			  		  			messageBodyPart = new MimeBodyPart(); 
			  	                    
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
			  		  			//System.out.println("decodedString"+decodedString);
			  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
			  		  			     
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  		        }
			  	         }else 
			  	         {
			  	        	 messageBodyPart = new MimeBodyPart(); 
			  	             
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
			  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
			  		  			      	
			  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
			  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
			  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
			  		  					 		  
			  		  			      		
			  		  					 		  multipart.addBodyPart(messageBodyPart);
			  	         }
			  		  }
			  		 
			
					    //System.out.println("htmlPart " + messageBodyPart.getContent());
					    
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }else 
			     {    
			    	 //System.out.println("no   inline");
			    	 MimeMultipart multipart = new MimeMultipart();
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
				        message2.setContent(multipart,"text/html; charset=UTF-8");	
			     }
			 
			
			     message2.saveChanges();

					Folder folder = store.getFolder("Sent");
					folder.open(Folder.READ_WRITE);
					message2.setFlag(Flag.SEEN, true);
					folder.appendMessages(new Message[] { message2 });

					Transport.send(message2);
					//System.out.println("4");
					Folder emailFolder1 = store.getFolder("Drafts");
					//System.out.println("2");
					if (emailFolder1.getMessageCount() != 0) {
						//System.out.println("000");
						emailFolder1.open(Folder.READ_WRITE);
						UIDFolder uf1 = (UIDFolder) emailFolder1;
						Message msg1 = uf1.getMessageByUID(email.getId());
						if (msg1 == null) {
//							//System.out.println("if");
//							ObjectNode jsonObject = objectMapper.createObjectNode();
//							jsonObject.put("errorCode", res.setErrorCode(404));
//							jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//							// //System.out.println(jsonObject);
//							return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
						} else {
							//System.out.println("1");

							msg1.setFlag(Flags.Flag.DELETED, true);
							Message[] messageList = emailFolder.expunge();
							//System.out.println("messageList" + messageList);
							emailFolder.close(true);

						}

					}

					Folder emailFolder2 = store.getFolder("Trash");
					//System.out.println("2");
					if (emailFolder1.getMessageCount() != 0) {
						//System.out.println("000");
						emailFolder2.open(Folder.READ_WRITE);
						UIDFolder uff = (UIDFolder) emailFolder2;
						Message msg1 = uff.getMessageByUID(email.getId());
						if (msg1 == null) {
							//System.out.println("if");
//							ObjectNode jsonObject = objectMapper.createObjectNode();
//							jsonObject.put("errorCode", res.setErrorCode(404));
//							jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//							// //System.out.println(jsonObject);
//							return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
						} else {
							//System.out.println("1");

							msg1.setFlag(Flags.Flag.DELETED, true);
							Message[] messageList = emailFolder.expunge();
							//System.out.println("messageList" + messageList);
							emailFolder.close(true);

						}

					}
					store.close();

		}
		}catch(Exception e) {
			System.out.println("e"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Forward  Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Forward Mail  Send Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);

	}
	
	
	
	
	@PostMapping("/reply/all/draft")
	public ResponseEntity replyAllDraftEmail(@RequestBody Email email) throws Exception {
		Optional<User> user = userService.findById(email.getUser().getId());
		 long  messageuid = 0;
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		try {
		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		
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

		Session session = Session.getInstance(props, auth);
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> cidarray = new ArrayList<String>();
		Folder emailFolder = store.getFolder(folderName);
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_WRITE);
			UIDFolder uf = (UIDFolder) emailFolder;
			Message message = uf.getMessageByUID(email.getId());
			message.setFlag(Flag.ANSWERED, true);
//			 emailFolder.appendMessages(new Message[] {message});
//			message.setFlag(Flag.ANSWERED, true);
			   Message message2 = new MimeMessage(session);  
			    message2= (MimeMessage) message.reply(true);
			   message2.setSubject("RE: " + email.getSubject());  
			   message2.setFrom(new InternetAddress(email.getFrom()));  
			   
				List<InternetAddress> listTo = new ArrayList<InternetAddress>();
				for (String to : email.getTo()) {
					InternetAddress to1 = new InternetAddress(to);
					message2.setRecipient(Message.RecipientType.TO, to1);
					listTo.add(to1);
				}
				Address[] addressTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressTo);
			   
				List<InternetAddress> listReplyTo = new ArrayList<InternetAddress>();
				for (String replyTo : email.getReplyTo()) {
					InternetAddress replyTo1 = new InternetAddress(replyTo);
					message2.setRecipient(Message.RecipientType.TO, replyTo1);
					listReplyTo.add(replyTo1);
				}
				Address[] addressReplyTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressReplyTo);
				
				List<InternetAddress> listCc = new ArrayList<InternetAddress>();
				for (String cc : email.getCc()) {
					InternetAddress cc1 = new InternetAddress(cc);
					message2.setRecipient(Message.RecipientType.CC, cc1);
					listCc.add(cc1);
				}
				Address[] addressCc = listCc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.CC, addressCc);
				
				List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
				for (String bcc : email.getBcc()) {
					InternetAddress bcc1 = new InternetAddress(bcc);
					message2.setRecipient(Message.RecipientType.BCC, bcc1);
					listBcc.add(bcc1);
				}
				Address[] addressBcc = listBcc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.BCC, addressBcc);
			   
				Document document = Jsoup.parse(email.getBody());
				 Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
							     if(images.isEmpty() == false)
							     {
							    	  //System.out.println("has inline");
							    	 MimeMultipart multipart = new MimeMultipart("related");
							    	 for ( org.jsoup.nodes.Element image :images )
							         {
										 // ContentID is used by both parts
										    String cid = ContentIdGenerator.getContentId();
										    //System.out.println("cid" + cid);
										    
							        	 String src = image.attr("src");
							        	//System.out.println("src " + src);
							        	 String[] base64string = src.split(",");
							        	 String[] datatype = src.split(";");
							        	// //System.out.println("datatype " + base64string[1]);
							        	// //System.out.println("base64string " + datatype[1]);
							        	// //System.out.println("file encoding " + datatype[0]);		        
							        	image.attr("src","cid:" + cid);      
							        	base64array.add(base64string[1]);
							        	cidarray.add(cid);	        				
							         }
							    	 
							    	 //System.out.println("html string " + document.toString());
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							  		  multipart.addBodyPart(messageBodyPart);
							  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
							  		  if(base64array.isEmpty() == false )
							  		  {
							  			if( base64array.size() > 1)
							  	         {
							  	  			for ( int i=0; i<base64array.size(); ++i)
							  		        {
							  		  			messageBodyPart = new MimeBodyPart(); 
							  	                    
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
							  		  			//System.out.println("decodedString"+decodedString);
							  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
							  		  			     
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  		        }
							  	         }else 
							  	         {
							  	        	 messageBodyPart = new MimeBodyPart(); 
							  	             
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  	         }
							  		  }
							  		 
							
									    //System.out.println("htmlPart " + messageBodyPart.getContent());
									    
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }else 
							     {    
							    	 //System.out.println("no   inline");
							    	 MimeMultipart multipart = new MimeMultipart();
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }
							     message2.saveChanges();
							     try {
										Store storedraft = session.getStore("imaps");
										storedraft.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
										//System.out.println("fol:" + storedraft.getFolder("Drafts"));
//								        store.connect(email.getHost(), email.getPort(), email.getFrom(), email.getPassword());
										Folder folder = store.getFolder("Drafts");
										folder.open(Folder.READ_WRITE);
										message2.setFlag(Flag.SEEN, true);
										folder.appendMessages(new Message[] { message2 });
										
										Message[] messages = folder.getMessages();
										Message draftmessage = messages[messages.length-1];

										FetchProfile fp = new FetchProfile();
										fp.add(UIDFolder.FetchProfileItem.UID);
										folder.fetch(folder.getMessages(), fp);
										UIDFolder pf = (UIDFolder) folder;
										messageuid = pf.getUID(draftmessage);
										
									////System.out.println("uid"+messageuid);
										store.close();
									} catch (Exception ex) {
										//System.out.println("e" + ex);
										ObjectNode jsonObject = objectMapper.createObjectNode();
										jsonObject.put("errorCode", res.FAILURE);
										jsonObject.put("message", res.setDescription("Unable to Save Email as Draft"));
										return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
									}


		}
		}catch(Exception e) {
			//System.out.println("e1"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Save Email as Draft"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Draft Saved Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	


	}
	

	@PostMapping("/reply/all/draft/send")
	public ResponseEntity replyAllDraftSendEmail(@RequestBody Email email) throws Exception {
		Optional<User> user = userService.findById(email.getUser().getId());
		 long  messageuid = 0;
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		try {
		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.auth", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.starttls.enable", user.get().getEmailConfiguration().isAuthentication());
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		
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

		Session session = Session.getInstance(props, auth);
		Store store = session.getStore("imaps");
		store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> cidarray = new ArrayList<String>();
		Folder emailFolder = store.getFolder(folderName);
		if (emailFolder.getMessageCount() != 0) {
			emailFolder.open(Folder.READ_WRITE);
			UIDFolder uf = (UIDFolder) emailFolder;
			Message message = uf.getMessageByUID(email.getId());
			message.setFlag(Flag.ANSWERED, true);
//			 emailFolder.appendMessages(new Message[] {message});
//			message.setFlag(Flag.ANSWERED, true);
			   Message message2 = new MimeMessage(session);  
			    message2= (MimeMessage) message.reply(true);
			   message2.setSubject(email.getSubject());  
			   message2.setFrom(new InternetAddress(email.getFrom()));  
			   
				List<InternetAddress> listTo = new ArrayList<InternetAddress>();
				for (String to : email.getTo()) {
					InternetAddress to1 = new InternetAddress(to);
					message2.setRecipient(Message.RecipientType.TO, to1);
					listTo.add(to1);
				}
				Address[] addressTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressTo);
			   
				List<InternetAddress> listReplyTo = new ArrayList<InternetAddress>();
				for (String replyTo : email.getReplyTo()) {
					InternetAddress replyTo1 = new InternetAddress(replyTo);
					message2.setRecipient(Message.RecipientType.TO, replyTo1);
					listReplyTo.add(replyTo1);
				}
				Address[] addressReplyTo = listTo.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.TO, addressReplyTo);
				
				List<InternetAddress> listCc = new ArrayList<InternetAddress>();
				for (String cc : email.getCc()) {
					InternetAddress cc1 = new InternetAddress(cc);
					message2.setRecipient(Message.RecipientType.CC, cc1);
					listCc.add(cc1);
				}
				Address[] addressCc = listCc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.CC, addressCc);
				
				List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
				for (String bcc : email.getBcc()) {
					InternetAddress bcc1 = new InternetAddress(bcc);
					message2.setRecipient(Message.RecipientType.BCC, bcc1);
					listBcc.add(bcc1);
				}
				Address[] addressBcc = listBcc.toArray(new Address[] {});
				message2.setRecipients(Message.RecipientType.BCC, addressBcc);
			   
				Document document = Jsoup.parse(email.getBody());
				 Elements images = document.getElementsByTag("img");
				 BodyPart messageBodyPart = new MimeBodyPart();
							     if(images.isEmpty() == false)
							     {
							    	  //System.out.println("has inline");
							    	 MimeMultipart multipart = new MimeMultipart("related");
							    	 for ( org.jsoup.nodes.Element image :images )
							         {
										 // ContentID is used by both parts
										    String cid = ContentIdGenerator.getContentId();
										    //System.out.println("cid" + cid);
										    
							        	 String src = image.attr("src");
							        	//System.out.println("src " + src);
							        	 String[] base64string = src.split(",");
							        	 String[] datatype = src.split(";");
							        	// //System.out.println("datatype " + base64string[1]);
							        	// //System.out.println("base64string " + datatype[1]);
							        	// //System.out.println("file encoding " + datatype[0]);		        
							        	image.attr("src","cid:" + cid);      
							        	base64array.add(base64string[1]);
							        	cidarray.add(cid);	        				
							         }
							    	 
							    	 //System.out.println("html string " + document.toString());
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							  		  multipart.addBodyPart(messageBodyPart);
							  		 //System.out.println("base64array.isEmpty()" + base64array.isEmpty());
							  		  if(base64array.isEmpty() == false )
							  		  {
							  			if( base64array.size() > 1)
							  	         {
							  	  			for ( int i=0; i<base64array.size(); ++i)
							  		        {
							  		  			messageBodyPart = new MimeBodyPart(); 
							  	                    
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
							  		  			//System.out.println("decodedString"+decodedString);
							  		  			//System.out.println("cidarray.get(i)"+cidarray.get(i));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(i) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);	
							  		  			     
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  		        }
							  	         }else 
							  	         {
							  	        	 messageBodyPart = new MimeBodyPart(); 
							  	             
							  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(0)).getBytes("UTF-8"));
							  		  			 ByteArrayDataSource imageDataSource = new ByteArrayDataSource(decodedString,"image/png");
							  		  			      	
							  		  			      	  messageBodyPart.setDataHandler(new DataHandler(imageDataSource));		          
							  		  			      	  ((MimeBodyPart) messageBodyPart).setContentID("<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setHeader("Content-ID","<" + cidarray.get(0) + ">");
							  		  			      	  messageBodyPart.setDisposition(Part.INLINE);		 		 
							  		  					 		  
							  		  			      		
							  		  					 		  multipart.addBodyPart(messageBodyPart);
							  	         }
							  		  }
							  		 
							
									    //System.out.println("htmlPart " + messageBodyPart.getContent());
									    
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }else 
							     {    
							    	 //System.out.println("no   inline");
							    	 MimeMultipart multipart = new MimeMultipart();
							    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
							    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
							    		 MimeBodyPart attachPart = new MimeBodyPart();
											attachPart.setContent(file1.getFileByte(), file1.getFileType());
											attachPart.setFileName(file1.getFileName());
											attachPart.setDisposition(Part.ATTACHMENT);
											multipart.addBodyPart(attachPart);
										}
										
								        // add image to the multipart
								        multipart.addBodyPart(messageBodyPart);
								        message2.setContent(multipart,"text/html; charset=UTF-8");	
							     }
							     message2.saveChanges();

									Folder folder = store.getFolder("Sent");
									folder.open(Folder.READ_WRITE);
									message2.setFlag(Flag.SEEN, true);
									folder.appendMessages(new Message[] { message2 });

									Transport.send(message2);
									//System.out.println("4");
									Folder emailFolder1 = store.getFolder("Drafts");
									//System.out.println("2");
									if (emailFolder1.getMessageCount() != 0) {
										//System.out.println("000");
										emailFolder1.open(Folder.READ_WRITE);
										UIDFolder uf1 = (UIDFolder) emailFolder1;
										Message msg1 = uf1.getMessageByUID(email.getId());
										if (msg1 == null) {
//											//System.out.println("if");
//											ObjectNode jsonObject = objectMapper.createObjectNode();
//											jsonObject.put("errorCode", res.setErrorCode(404));
//											jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//											// //System.out.println(jsonObject);
//											return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
										} else {
											//System.out.println("1");

											msg1.setFlag(Flags.Flag.DELETED, true);
											Message[] messageList = emailFolder.expunge();
											//System.out.println("messageList" + messageList);
											emailFolder.close(true);

										}

									}

									Folder emailFolder2 = store.getFolder("Trash");
									//System.out.println("2");
									if (emailFolder1.getMessageCount() != 0) {
										//System.out.println("000");
										emailFolder2.open(Folder.READ_WRITE);
										UIDFolder uff = (UIDFolder) emailFolder2;
										Message msg1 = uff.getMessageByUID(email.getId());
										if (msg1 == null) {
											//System.out.println("if");
//											ObjectNode jsonObject = objectMapper.createObjectNode();
//											jsonObject.put("errorCode", res.setErrorCode(404));
//											jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//											// //System.out.println(jsonObject);
//											return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
										} else {
											//System.out.println("1");

											msg1.setFlag(Flags.Flag.DELETED, true);
											Message[] messageList = emailFolder.expunge();
											//System.out.println("messageList" + messageList);
											emailFolder.close(true);

										}

									}
									store.close();



		}
		}catch(Exception e) {
			//System.out.println("e1"+e);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setMessage("Unable to Reply Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Email Replied Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	


	}
	

}
