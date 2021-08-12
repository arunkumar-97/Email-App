package com.jesperapps.email.api.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.activation.DataHandler;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
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
import org.springframework.web.bind.annotation.PathVariable;
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
public class EmailCreateController {
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	EmailResponseEntity res = new EmailResponseEntity();
	String username;
	String pwd;
	String OriginalPassword;
	Map<String, String> mapInlineImages;

	@PostMapping("/send")
	public ResponseEntity sendEmail(@RequestBody Email email) throws Exception {
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
			// RETRIVE ENCRYPTED PASSWORD
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

		try {
			
			ArrayList<String> base64array = new ArrayList<String>();
			 ArrayList<String> cidarray = new ArrayList<String>();
			 ArrayList<String> iframesrcarray = new ArrayList<String>();
			 ArrayList<String> iframecidarray = new ArrayList<String>();
			 
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
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username, false));

			msg.setSubject(email.getSubject());
			msg.setContent(email.getBody(), "text/html;charset=UTF-8");

			List<InternetAddress> listTo = new ArrayList<InternetAddress>();
			for (String to : email.getTo()) {
				InternetAddress to1 = new InternetAddress(to);
				msg.setRecipient(Message.RecipientType.TO, to1);
				listTo.add(to1);
			}
			Address[] addressTo = listTo.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			msg.setSentDate(new Date());
			List<InternetAddress> listCc = new ArrayList<InternetAddress>();
			for (String cc : email.getCc()) {
				InternetAddress cc1 = new InternetAddress(cc);
				msg.setRecipient(Message.RecipientType.CC, cc1);
				listCc.add(cc1);
			}
			Address[] addressCc = listCc.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.CC, addressCc);

			List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
			for (String bcc : email.getBcc()) {
				InternetAddress bcc1 = new InternetAddress(bcc);
				msg.setRecipient(Message.RecipientType.BCC, bcc1);
				listBcc.add(bcc1);
			}
			Address[] addressBcc = listBcc.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.BCC, addressBcc);

			Document document = Jsoup.parse(email.getBody());
			 Elements images  = document.getElementsByTag("img"); 
			
			 BodyPart messageBodyPart = new MimeBodyPart();
			     if(images.isEmpty() == false)
			     {
			    	  System.out.println("has inline");
			    	 MimeMultipart multipart = new MimeMultipart("related");
			    	 for ( org.jsoup.nodes.Element image :images )
			         {
						 // ContentID is used by both parts
						    String cid = ContentIdGenerator.getContentId();
						    System.out.println("cid" + cid);
						    
			        	 String src = image.attr("src");
			        	System.out.println("src " + src);
			        	 String[] base64string = src.split(",");
			        	 String[] datatype = src.split(";");
			        	// System.out.println("datatype " + base64string[1]);
			        	// System.out.println("base64string " + datatype[1]);
			        	// System.out.println("file encoding " + datatype[0]);		        
			        	image.attr("src","cid:" + cid);      
			        	base64array.add(base64string[1]);
			        	cidarray.add(cid);	        				
			         }
			    	 
			    	 System.out.println("html string " + document.toString());
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			  		  multipart.addBodyPart(messageBodyPart);
			  		 System.out.println("base64array.isEmpty()" + base64array.isEmpty());
			  		  if(base64array.isEmpty() == false )
			  		  {
			  			if( base64array.size() > 1)
			  	         {
			  	  			for ( int i=0; i<base64array.size(); ++i)
			  		        {
			  		  			messageBodyPart = new MimeBodyPart(); 
			  	                    
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
			  		  			System.out.println("decodedString"+decodedString);
			  		  			System.out.println("cidarray.get(i)"+cidarray.get(i));
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
			  		 
			
					    System.out.println("htmlPart " + messageBodyPart.getContent());
					    
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
						msg.setContent(multipart,"text/html; charset=UTF-8");	
			     }else 
			     {    
			    	 System.out.println("no   inline");
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
						msg.setContent(multipart,"text/html; charset=UTF-8");	
			     }
			 
			 
			    
			     msg.saveChanges();
					Store store = session.getStore("imaps");
					store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);

					Folder folder = store.getFolder("Sent");
					folder.open(Folder.READ_WRITE);
					msg.setFlag(Flag.SEEN, true);
					folder.appendMessages(new Message[] { msg });
					store.close();
					Transport.send(msg);
			
		} catch (MessagingException mex) {
			System.out.println("e" + mex);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setDescription("Unable to Send Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Email Sent Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}

	@PostMapping("/draft")
	public ResponseEntity saveDraftEmail(@RequestBody Email email) throws Exception {
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
		props.put("mail.smtp.host", user.get().getEmailConfiguration().getOutgoingHost());
		props.put("mail.smtp.port", user.get().getEmailConfiguration().getOutgoingPort());
		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, OriginalPassword);
			}

		};
		Session session = Session.getInstance(props, auth);

		// compose the message
		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username, false));
			List<InternetAddress> list = new ArrayList<InternetAddress>();
			for (String to : email.getTo()) {
				InternetAddress to1 = new InternetAddress(to);
				msg.setRecipient(Message.RecipientType.TO, to1);
				list.add(to1);
			}
			Address[] addressTo = list.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			msg.setSubject(email.getSubject());
			msg.setContent(email.getBody(), "text/html;charset=UTF-8");

			msg.setSentDate(new Date());
			List<InternetAddress> listOfToAddress = new ArrayList<InternetAddress>();
			for (String cc : email.getCc()) {
				InternetAddress cc1 = new InternetAddress(cc);
				msg.setRecipient(Message.RecipientType.CC, cc1);
				listOfToAddress.add(cc1);
			}
			Address[] address = listOfToAddress.toArray(new Address[] {});

			msg.setRecipients(Message.RecipientType.CC, address);

			List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
			for (String bcc : email.getCc()) {
				InternetAddress bcc1 = new InternetAddress(bcc);
				msg.setRecipient(Message.RecipientType.BCC, bcc1);
				listBcc.add(bcc1);
			}
			Address[] addressBcc = listBcc.toArray(new Address[] {});

			msg.setRecipients(Message.RecipientType.BCC, addressBcc);

			Document document = Jsoup.parse(email.getBody());
			Elements images = document.getElementsByTag("img");
			 BodyPart messageBodyPart = new MimeBodyPart();
		     if(images.isEmpty() == false)
		     {
		    	  System.out.println("has inline");
		    	 MimeMultipart multipart = new MimeMultipart("related");
		    	 for ( org.jsoup.nodes.Element image :images )
		         {
					 // ContentID is used by both parts
					    String cid = ContentIdGenerator.getContentId();
					    System.out.println("cid" + cid);
					    
		        	 String src = image.attr("src");
		        	System.out.println("src " + src);
		        	 String[] base64string = src.split(",");
		        	 String[] datatype = src.split(";");
		        	// System.out.println("datatype " + base64string[1]);
		        	// System.out.println("base64string " + datatype[1]);
		        	// System.out.println("file encoding " + datatype[0]);		        
		        	image.attr("src","cid:" + cid);      
		        	base64array.add(base64string[1]);
		        	cidarray.add(cid);	        				
		         }
		    	 
		    	 System.out.println("html string " + document.toString());
		    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
		  		  multipart.addBodyPart(messageBodyPart);
		  		 System.out.println("base64array.isEmpty()" + base64array.isEmpty());
		  		  if(base64array.isEmpty() == false )
		  		  {
		  			if( base64array.size() > 1)
		  	         {
		  	  			for ( int i=0; i<base64array.size(); ++i)
		  		        {
		  		  			messageBodyPart = new MimeBodyPart(); 
		  	                    
		  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
		  		  			System.out.println("decodedString"+decodedString);
		  		  			System.out.println("cidarray.get(i)"+cidarray.get(i));
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
		  		 
		
				    System.out.println("htmlPart " + messageBodyPart.getContent());
				    
		    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
		    		 MimeBodyPart attachPart = new MimeBodyPart();
						attachPart.setContent(file1.getFileByte(), file1.getFileType());
						attachPart.setFileName(file1.getFileName());
						attachPart.setDisposition(Part.ATTACHMENT);
						multipart.addBodyPart(attachPart);
					}
					
			        // add image to the multipart
			        multipart.addBodyPart(messageBodyPart);
					msg.setContent(multipart,"text/html; charset=UTF-8");	
		     }else 
		     {    
		    	 System.out.println("no   inline");
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
					msg.setContent(multipart,"text/html; charset=UTF-8");	
		     }
		 
			msg.saveChanges();
//			Transport.send(msg);
			try {
				Store store = session.getStore("imaps");
				store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);
				System.out.println("fol:" + store.getFolder("Drafts"));
//		        store.connect(email.getHost(), email.getPort(), email.getFrom(), email.getPassword());
				Folder folder = store.getFolder("Drafts");
				folder.open(Folder.READ_WRITE);
				msg.setFlag(Flag.SEEN, true);
				folder.appendMessages(new Message[] { msg });
				
				Message[] messages = folder.getMessages();
				Message message = messages[messages.length-1];

				FetchProfile fp = new FetchProfile();
				fp.add(UIDFolder.FetchProfileItem.UID);
				folder.fetch(folder.getMessages(), fp);
				UIDFolder pf = (UIDFolder) folder;
				messageuid = pf.getUID(message);
				
			//System.out.println("uid"+messageuid);
				store.close();
			} catch (Exception ex) {
				System.out.println("e" + ex);
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.FAILURE);
				jsonObject.put("message", res.setDescription("Unable to Save Email as Draft"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("messageid", messageuid);
		jsonObject.put("description", res.setDescription("Email Saved as Draft"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}

	@PostMapping("/send/draft")
	public ResponseEntity sendDraftEmail(@RequestBody Email email) throws Exception {
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
			Folder emailFol = store.getFolder("Drafts");
			System.out.println("2");
			if (emailFol.getMessageCount() != 0) {
				emailFol.open(Folder.READ_WRITE);
				UIDFolder uf = (UIDFolder) emailFol;
				Message msgData = uf.getMessageByUID(email.getId());
				if (msgData == null) {

					System.out.println("if");
//					ObjectNode jsonObject = objectMapper.createObjectNode();
//					jsonObject.put("errorCode", res.setErrorCode(404));
//					jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//					// System.out.println(jsonObject);
//					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				}
			}

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username, false));

			msg.setSubject(email.getSubject());
			msg.setContent(email.getBody(), "text/html;charset=UTF-8");

			List<InternetAddress> listTo = new ArrayList<InternetAddress>();
			for (String to : email.getTo()) {
				InternetAddress to1 = new InternetAddress(to);
				msg.setRecipient(Message.RecipientType.TO, to1);
				listTo.add(to1);
			}
			Address[] addressTo = listTo.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			msg.setSentDate(new Date());
			List<InternetAddress> listCc = new ArrayList<InternetAddress>();
			for (String cc : email.getCc()) {
				InternetAddress cc1 = new InternetAddress(cc);
				msg.setRecipient(Message.RecipientType.CC, cc1);
				listCc.add(cc1);
			}
			Address[] addressCc = listCc.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.CC, addressCc);

			List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
			for (String bcc : email.getBcc()) {
				InternetAddress bcc1 = new InternetAddress(bcc);
				msg.setRecipient(Message.RecipientType.BCC, bcc1);
				listBcc.add(bcc1);
			}
			Address[] addressBcc = listBcc.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.BCC, addressBcc);

			Document document = Jsoup.parse(email.getBody());
			Elements images = document.getElementsByTag("img");
			 BodyPart messageBodyPart = new MimeBodyPart();
		     if(images.isEmpty() == false)
		     {
		    	  System.out.println("has inline");
		    	 MimeMultipart multipart = new MimeMultipart("related");
		    	 for ( org.jsoup.nodes.Element image :images )
		         {
					 // ContentID is used by both parts
					    String cid = ContentIdGenerator.getContentId();
					    System.out.println("cid" + cid);
					    
		        	 String src = image.attr("src");
		        	System.out.println("src " + src);
		        	 String[] base64string = src.split(",");
		        	 String[] datatype = src.split(";");
		        	// System.out.println("datatype " + base64string[1]);
		        	// System.out.println("base64string " + datatype[1]);
		        	// System.out.println("file encoding " + datatype[0]);		        
		        	image.attr("src","cid:" + cid);      
		        	base64array.add(base64string[1]);
		        	cidarray.add(cid);	        				
		         }
		    	 
		    	 System.out.println("html string " + document.toString());
		    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
		  		  multipart.addBodyPart(messageBodyPart);
		  		 System.out.println("base64array.isEmpty()" + base64array.isEmpty());
		  		  if(base64array.isEmpty() == false )
		  		  {
		  			if( base64array.size() > 1)
		  	         {
		  	  			for ( int i=0; i<base64array.size(); ++i)
		  		        {
		  		  			messageBodyPart = new MimeBodyPart(); 
		  	                    
		  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
		  		  			System.out.println("decodedString"+decodedString);
		  		  			System.out.println("cidarray.get(i)"+cidarray.get(i));
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
		  		 
		
				    System.out.println("htmlPart " + messageBodyPart.getContent());
				    
		    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
		    		 MimeBodyPart attachPart = new MimeBodyPart();
						attachPart.setContent(file1.getFileByte(), file1.getFileType());
						attachPart.setFileName(file1.getFileName());
						attachPart.setDisposition(Part.ATTACHMENT);
						multipart.addBodyPart(attachPart);
					}
					
			        // add image to the multipart
			        multipart.addBodyPart(messageBodyPart);
					msg.setContent(multipart,"text/html; charset=UTF-8");	
		     }else 
		     {    
		    	 System.out.println("no   inline");
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
					msg.setContent(multipart,"text/html; charset=UTF-8");	
		     }
		 
			msg.saveChanges();

			Folder folder = store.getFolder("Sent");
			folder.open(Folder.READ_WRITE);
			msg.setFlag(Flag.SEEN, true);
			folder.appendMessages(new Message[] { msg });

			Transport.send(msg);
			System.out.println("4");
			Folder emailFolder = store.getFolder("Drafts");
			System.out.println("2");
			if (emailFolder.getMessageCount() != 0) {
				System.out.println("000");
				emailFolder.open(Folder.READ_WRITE);
				UIDFolder uf = (UIDFolder) emailFolder;
				Message msg1 = uf.getMessageByUID(email.getId());
				if (msg1 == null) {
//					System.out.println("if");
//					ObjectNode jsonObject = objectMapper.createObjectNode();
//					jsonObject.put("errorCode", res.setErrorCode(404));
//					jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//					// System.out.println(jsonObject);
//					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				} else {
					System.out.println("1");

					msg1.setFlag(Flags.Flag.DELETED, true);
					Message[] messageList = emailFolder.expunge();
					System.out.println("messageList" + messageList);
					emailFolder.close(true);

				}

			}

			Folder emailFolder1 = store.getFolder("Trash");
			System.out.println("2");
			if (emailFolder1.getMessageCount() != 0) {
				System.out.println("000");
				emailFolder1.open(Folder.READ_WRITE);
				UIDFolder uf = (UIDFolder) emailFolder1;
				Message msg1 = uf.getMessageByUID(email.getId());
				if (msg1 == null) {
					System.out.println("if");
//					ObjectNode jsonObject = objectMapper.createObjectNode();
//					jsonObject.put("errorCode", res.setErrorCode(404));
//					jsonObject.put("message", res.setMessage("Email with id=" + email.getId() + " not found"));
//					// System.out.println(jsonObject);
//					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				} else {
					System.out.println("1");

					msg1.setFlag(Flags.Flag.DELETED, true);
					Message[] messageList = emailFolder.expunge();
					System.out.println("messageList" + messageList);
					emailFolder.close(true);

				}

			}
			store.close();
		} catch (MessagingException mex) {
			System.out.println("ex" + mex);
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setDescription("Unable to Send Email:" + mex));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		ObjectNode jsonObject = objectMapper.createObjectNode();
		jsonObject.put("statusCode", res.SUCCESS);
		jsonObject.put("description", res.setDescription("Email Sent Successfully"));
		return new ResponseEntity(jsonObject, HttpStatus.OK);
	}
	
	
	@PostMapping("/email_Delay/{date}/{hour}/{minute}")
	public ResponseEntity<Email> createDelayEmail(@PathVariable("date") String date , @PathVariable("hour") int hour ,@PathVariable("minute") int minute ,@RequestBody Email emailRequestEntity) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Email email;
		
		
		Optional<User> user = userService.findById(emailRequestEntity.getUser().getId());
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
		
		try {
			 System.out.println("date"+date);
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			    Date dateObj = sdf.parse(date);
			 System.out.println("date.getDate()"+dateObj.getDate());
			String hr;
			String min;
			String sec;
			
			
			System.out.println("hour"+hour);
			System.out.println("minute"+minute);
			Calendar c = Calendar.getInstance();
			
			c.setTime(dateObj);			
			System.out.println("c.getTime();"+c.getTime());
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND, 00);

			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
			    @Override
			    public void run() {
			        try {
			        	System.out.println("delay function called");
						sendmaildelay(emailRequestEntity ,user);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }

				
			}, c.getTime(), 86400000);
		
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.SUCCESS);
			jsonObject.put("message", res.setDescription("Email Scheduled Sucessfully"));
			return new ResponseEntity(jsonObject, HttpStatus.OK);
		} catch (Exception ex) {
			System.out.println("ex" + ex.getMessage());
			
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.FAILURE);
			jsonObject.put("message", res.setDescription("Unable to Send Email"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		
		
		
	}

	protected void sendmaildelay(Email email, Optional<User> user) throws IOException {
		  
try {
			
			ArrayList<String> base64array = new ArrayList<String>();
			 ArrayList<String> cidarray = new ArrayList<String>();
			 ArrayList<String> iframesrcarray = new ArrayList<String>();
			 ArrayList<String> iframecidarray = new ArrayList<String>();
			 
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
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(username, false));

			msg.setSubject(email.getSubject());
			msg.setContent(email.getBody(), "text/html;charset=UTF-8");

			List<InternetAddress> listTo = new ArrayList<InternetAddress>();
			for (String to : email.getTo()) {
				InternetAddress to1 = new InternetAddress(to);
				msg.setRecipient(Message.RecipientType.TO, to1);
				listTo.add(to1);
			}
			Address[] addressTo = listTo.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			msg.setSentDate(new Date());
			List<InternetAddress> listCc = new ArrayList<InternetAddress>();
			for (String cc : email.getCc()) {
				InternetAddress cc1 = new InternetAddress(cc);
				msg.setRecipient(Message.RecipientType.CC, cc1);
				listCc.add(cc1);
			}
			Address[] addressCc = listCc.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.CC, addressCc);

			List<InternetAddress> listBcc = new ArrayList<InternetAddress>();
			for (String bcc : email.getBcc()) {
				InternetAddress bcc1 = new InternetAddress(bcc);
				msg.setRecipient(Message.RecipientType.BCC, bcc1);
				listBcc.add(bcc1);
			}
			Address[] addressBcc = listBcc.toArray(new Address[] {});
			msg.setRecipients(Message.RecipientType.BCC, addressBcc);

			Document document = Jsoup.parse(email.getBody());
			 Elements images  = document.getElementsByTag("img"); 
			
			 BodyPart messageBodyPart = new MimeBodyPart();
			     if(images.isEmpty() == false)
			     {
			    	  System.out.println("has inline");
			    	 MimeMultipart multipart = new MimeMultipart("related");
			    	 for ( org.jsoup.nodes.Element image :images )
			         {
						 // ContentID is used by both parts
						    String cid = ContentIdGenerator.getContentId();
						    System.out.println("cid" + cid);
						    
			        	 String src = image.attr("src");
			        	System.out.println("src " + src);
			        	 String[] base64string = src.split(",");
			        	 String[] datatype = src.split(";");
			        	// System.out.println("datatype " + base64string[1]);
			        	// System.out.println("base64string " + datatype[1]);
			        	// System.out.println("file encoding " + datatype[0]);		        
			        	image.attr("src","cid:" + cid);      
			        	base64array.add(base64string[1]);
			        	cidarray.add(cid);	        				
			         }
			    	 
			    	 System.out.println("html string " + document.toString());
			    	 messageBodyPart.setContent(document.toString(),  "text/html; charset=UTF-8");
			  		  multipart.addBodyPart(messageBodyPart);
			  		 System.out.println("base64array.isEmpty()" + base64array.isEmpty());
			  		  if(base64array.isEmpty() == false )
			  		  {
			  			if( base64array.size() > 1)
			  	         {
			  	  			for ( int i=0; i<base64array.size(); ++i)
			  		        {
			  		  			messageBodyPart = new MimeBodyPart(); 
			  	                    
			  		  			byte[] decodedString = Base64.getDecoder().decode(new String(base64array.get(i)).getBytes("UTF-8"));
			  		  			System.out.println("decodedString"+decodedString);
			  		  			System.out.println("cidarray.get(i)"+cidarray.get(i));
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
			  		 
			
					    System.out.println("htmlPart " + messageBodyPart.getContent());
					    
			    	 for (MultipleAttachment file1 : email.getMultipleAttachment()) {
			    		 MimeBodyPart attachPart = new MimeBodyPart();
							attachPart.setContent(file1.getFileByte(), file1.getFileType());
							attachPart.setFileName(file1.getFileName());
							attachPart.setDisposition(Part.ATTACHMENT);
							multipart.addBodyPart(attachPart);
						}
						
				        // add image to the multipart
				        multipart.addBodyPart(messageBodyPart);
						msg.setContent(multipart,"text/html; charset=UTF-8");	
			     }else 
			     {    
			    	 System.out.println("no   inline");
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
						msg.setContent(multipart,"text/html; charset=UTF-8");	
			     }
			 
			 
			    
			     msg.saveChanges();
					Store store = session.getStore("imaps");
					store.connect(user.get().getEmailConfiguration().getOutgoingHost(), username, OriginalPassword);

					Folder folder = store.getFolder("Sent");
					folder.open(Folder.READ_WRITE);
					msg.setFlag(Flag.SEEN, true);
					folder.appendMessages(new Message[] { msg });
					store.close();
					Transport.send(msg);
			
		} catch (MessagingException mex) {
			System.out.println("e" + mex);
			
		}
		
	}
	
}
