package com.jesperapps.email.api.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

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
import javax.mail.UIDFolder;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.jesperapps.email.api.extra.GeneratePlainPassword;
import com.jesperapps.email.api.extra.GetTextFromMimeMultipart;
import com.jesperapps.email.api.extra.HexStringToByteArray;
import com.jesperapps.email.api.message.EmailResponseEntity;
import com.jesperapps.email.api.message.FolderResponseEntity;
import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.EmailFolder;
import com.jesperapps.email.api.model.MultipleAttachment;
import com.jesperapps.email.api.model.SubFolder;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.UserService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class EmailListingController {
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper objectMapper;
	EmailResponseEntity res = new EmailResponseEntity();
	
	@Autowired
	private FolderController folderController;
	User userData;

	String username;
	String pwd;
	String OriginalPassword;
	String from;
	String subject;
	String message1;

	String cc;
	String to;
	String bcc;

	String[] toArray;
	String[] ccArray;
	String[] bccArray;

	Email email2;
	List<Email> emailList;
	
	@GetMapping("/draft/{userId}")
	public ResponseEntity ReceiveEmail(@PathVariable("userId") Integer id) throws Exception {
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
		       // //System.out.println(OriginalPassword);
		    	//RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchDraftEmailList(host, mailstoreType, username, OriginalPassword, port, auth, security);
		}else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchDraftEmailList(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security) throws Exception {

		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> relativebase64array = new ArrayList<String>();

		BodyPart messageBodyPart = new MimeBodyPart();
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
		// //System.out.println("emailFolder" + emailFolder);
		emailFolder.open(Folder.READ_ONLY);

		// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Folder[] f = store.getDefaultFolder().list();
		for(Folder fd:f)
		   System.out.println(">> "+fd.getName());
		// retrieve the messages from the folder in an array and print it
		Message[] messages = emailFolder.getMessages();
//				User user = userService.findByUserName(uname);
		// //System.out.println("user" + user);
		 emailList = new ArrayList<Email>();
		////System.out.println(" messages.length" + messages.length);
		for (int i = 0; i < messages.length; i++) {

			Message message = messages[i];
			 FetchProfile fp = new FetchProfile();
	            fp.add(UIDFolder.FetchProfileItem.UID);
	            emailFolder.fetch(emailFolder.getMessages(), fp);
	            
	            UIDFolder  pf =(UIDFolder )emailFolder;      
			long uid = pf.getUID(message);
	
			Address[] a;

			// FROM
			if ((a = message.getFrom()) != null) {
				for (int j = 0; j < a.length; j++)
					from = a[j].toString();
			}

			List<String> toMail = new ArrayList<String>();
			// TO
			if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
				for (int j = 0; j < a.length; j++) {
					to = a[j].toString();
					toMail.add(to);
					toArray = toMail.toArray(new String[toMail.size()]);
				}
			} else {
				toArray = null;
			}
			List<String> ccMail = new ArrayList<String>();
			// CC
			if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					cc = a[j].toString();
					ccMail.add(cc);
					ccArray = ccMail.toArray(new String[ccMail.size()]);
				}
			} else {
				ccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}
			
			List<String> bccMail = new ArrayList<String>();
			// BCC
			if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					bcc = a[j].toString();
					bccMail.add(bcc);
					bccArray = bccMail.toArray(new String[bccMail.size()]);
				}
			} else {
				bccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}
			
			// SUBJECT
			if (message.getSubject() != null) {
				subject = message.getSubject();
			} else {
				//System.out.println("no sub");
				subject = null;
			}
			
			

			// writePart(message);

			String result = "";
			if (message.isMimeType("text/plain")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				// //System.out.println("mimeMultipart" + mimeMultipart.toString());
				result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

				result = result.replace("\\", "");
				// //System.out.println("result " + result);
			}

			List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//			Enumeration headers = messages[i].getAllHeaders();
//			  while (headers.hasMoreElements()) {
//				  Header h = (Header) headers.nextElement();
//				  //System.out.println("dt::"+h.getName());
//				  //System.out.println(h.getName() + ": " + h.getValue());
//				  }
			// //System.out.println(" Email Data null");
			DateTime dtRec = new DateTime(message.getReceivedDate());
			
			
			email2 = new Email(from, toArray, subject, result, dtRec, ccArray, bccArray,
					message.getFolder(),uid, message.getFlags(),uid, multipleAttachment);
			 if(message.getFlags().contains(Flag.ANSWERED)) {
				 email2.setRepliedFlag(true);	
			 }
			 
			 if(message.getFlags().contains("Forwarded")) {
				 email2.setForwardedFlag(true);	
			 }
          ////System.out.println(result.getBytes());
			Document document = Jsoup.parse(result);
			String contentType = message.getContentType();
			if (message.isMimeType("multipart/*")) { 
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();	     
                        //   //System.out.println("filename : " + fileName);
                        final File file = new File(fileName);			                       	                        
                         String[] FileType = part.getContentType().split("\\s");	                      
             			InputStream fileData = part.getInputStream();
             			
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	             			
             			long Filesize =  part.getSize();
             			MultipleAttachment multi  = new MultipleAttachment();
             			multi.setFileName(fileName);
             			multi.setFileType(FileType[0]);	
             			multi.setFileSize(Filesize);
             			multi.setFileByte(byteArray);			             		
             			multipleAttachment.add(multi);
             			
             			 email2.setMultipleAttachment(multipleAttachment);
                    }
                    else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
                    {	                    	
                    	//System.out.println("inline one");
                         String[] FileType = part.getContentType().split("\\s");		              
             			InputStream fileData = part.getInputStream();			  	    	  
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	
             			String imageStr = Base64.encodeBase64String(byteArray);			             			
             			long Filesize =  part.getSize();			  
             			String fileName = part.getFileName();
					
             			  
             			
             			 String attributeValue ;
             			  
             			  if(FileType[0].toString().contains(";"))
             			  {
             				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
             			  }else {
             				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
             			  }
             			
             			  
             			  
             			 Elements paragraphs = document.getElementsByTag("img"); 
			                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
				             
			                for ( org.jsoup.nodes.Element paragraph :paragraphs )
			                {
			                	 String src = paragraph.attr("src").replace("cid:", "");
					              
					             
					                    if(contentid.equals(src)) 
					                    {
					                    	
					                    	paragraph.attr("src", attributeValue);
					                    	
					                    }
			                }								   
             			
                    }else if(part.isMimeType("multipart/*"))
                    {                    	
                    	 Multipart multiPart1 = (Multipart) part.getContent();
                    	
                    	int numberOfParts1 = multiPart1.getCount();
                    	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
                    	 {
                    		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
                    		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
		                    	{			                    		
                    			 
                    			    //System.out.println("inline two");
                    			 
			                    	 String[] FileType = part1.getContentType().split("\\s");				                    
			             			InputStream fileData = part1.getInputStream();			  
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	
			             			String imageStr = Base64.encodeBase64String(byteArray);
			             			long Filesize = part.getSize();
									String fileName = part.getFileName();
									
			             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
			             			 
			             			
			             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
			             			   
			             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
			             			  
						                Elements paragraphs = document.getElementsByTag("img"); 
						                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
							             
						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
						                {
						                	 String src = paragraph.attr("src").replace("cid:", "");
								              
								             
								                    if(contentid.equals(src)) 
								                    {
								                    	
								                    	paragraph.attr("src", attributeValue);
								                    	
								                    }
						                }								                  
		                    	}
                    		 else if(part1.getDisposition() ==  null)
		                    	{
		                    		
		                    		   if(part1.isMimeType("multipart/*")) 
		                    		   {
		                    			   Multipart multiPart2 = (Multipart) part1.getContent();
		                    			  // //System.out.println("multiPart2" + multiPart2);	    
					                    	int numberOfParts2 = multiPart2.getCount();
					                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
					                    	 {
					                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
					                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
					                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
							                    	{
					                    			 //System.out.println("mutipart inline  has inline if ");	         
							                    		
								                    	 String[] FileType = part2.getContentType().split("\\s");						                     
								             			InputStream fileData = part2.getInputStream();			   						             			 
								            			 						             			  
								             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
								             			int nRead;
								             			byte[] data = new byte[1024];
								             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
								             				buffer.write(data, 0, nRead);
								             			}
								             			buffer.flush();
								             			byte[] byteArray = buffer.toByteArray();	
								             			String imageStr = Base64.encodeBase64String(byteArray);
								             			long Filesize = part.getSize();
														String fileName = part.getFileName();
														
								             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
								             			  // //System.out.println(" inline attributeValue"+attributeValue);
											                Elements paragraphs = document.getElementsByTag("img"); 
											                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
											               // //System.out.println(" part2 contentid"+contentid);
											                for ( org.jsoup.nodes.Element paragraph :paragraphs )
											                {
											                	 String src = paragraph.attr("src").replace("cid:", "");
													             
											                	//  //System.out.println(" part2 src"+src);
													                    if(contentid.equals(src)) 
													                    {
													                    	// //System.out.println("if condition mutipart ");	
													                    	paragraph.attr("src", attributeValue);
													                    	
													                    }
											                }								                  
							                    	}
					                    	 }
		                    			   
		                    			  
		                    		   }
			                    	
		                    	}
                    	 }			                    	
                    	
                    	  email2.setBody(document.toString());
                    }
                }
                
                email2.setBody(document.toString());
            }

//			email2.setBody(document.toString());
			emailList.add(email2)	;
		}
		if (emailList.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
			return new ResponseEntity(emailList.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(ArrayList::new), lst -> {
                                                    Collections.reverse(lst);
                                                    return lst.stream();
                                                }
                        )).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);
	}
	

	@GetMapping("/unread/{userId}")
	public ResponseEntity unReadList(@PathVariable("userId") Integer id) throws Exception {
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchUnReadList(host, mailstoreType, username, OriginalPassword, port, auth, security);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}

	}

	public ResponseEntity fetchUnReadList(String hostval, String mailStrProt, String uname, String password, int port,
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
		emailList = new ArrayList<Email>();
		Folder[] folder = store.getDefaultFolder().list();
		for (int k = 0; k <= folder.length; k++) {
			if (folder.length > k) {

				// create the folder object and open it
				Folder emailFolder = store.getFolder(folder[k].getName());
				if (emailFolder.getMessageCount() != 0) {
					emailFolder.open(Folder.READ_ONLY);
					Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
					for (int i = 0; i < messages.length; i++) {
						Message message = messages[i];
						FetchProfile fp = new FetchProfile();
						fp.add(UIDFolder.FetchProfileItem.UID);
						emailFolder.fetch(emailFolder.getMessages(), fp);

						UIDFolder pf = (UIDFolder) emailFolder;
						long uid = pf.getUID(message);

						Address[] a;

						// FROM
						if ((a = message.getFrom()) != null) {
							for (int j = 0; j < a.length; j++)
								from = a[j].toString();
						}

						List<String> toMail = new ArrayList<String>();
						// TO
						if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
							for (int j = 0; j < a.length; j++) {
								to = a[j].toString();
								toMail.add(to);
								toArray = toMail.toArray(new String[toMail.size()]);
							}
						} else {
							toArray = null;
						}
						List<String> ccMail = new ArrayList<String>();
						// CC
						if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
							for (int j = 0; j < a.length; j++) {
								cc = a[j].toString();
								ccMail.add(cc);
								ccArray = ccMail.toArray(new String[ccMail.size()]);
							}
						} else {
							ccArray = null;
						}

						List<String> bccMail = new ArrayList<String>();
						// BCC
						if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
							for (int j = 0; j < a.length; j++) {
								bcc = a[j].toString();
								bccMail.add(bcc);
								bccArray = bccMail.toArray(new String[bccMail.size()]);
							}
						} else {
							bccArray = null;
						}

						// SUBJECT
						if (message.getSubject() != null) {
							subject = message.getSubject();
						} else {
							subject = null;
						}

						// writePart(message);

						String result = "";
						if (message.isMimeType("text/plain")) {
							result = message.getContent().toString();
						} else if (message.isMimeType("multipart/*")) {
							MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
							result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

							result = result.replace("\\", "");
						}
						List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//							Enumeration headers = messages[i].getAllHeaders();
//							  while (headers.hasMoreElements()) {
//								  Header h = (Header) headers.nextElement();
//								  }
						DateTime dtRec = new DateTime(message.getReceivedDate());

						email2 = new Email(from, toArray, subject, uid, result, dtRec, ccArray, bccArray,
								message.getFolder(), uid, message.getFlags(), multipleAttachment);
						if (message.getFlags().contains(Flag.ANSWERED)) {
							email2.setRepliedFlag(true);
						}

						if (message.getFlags().contains("Forwarded")) {
							email2.setForwardedFlag(true);
						}
						Document document = Jsoup.parse(result);
						String contentType = message.getContentType();
						if (message.isMimeType("multipart/*")) { 
			                // content may contain attachments
			                Multipart multiPart = (Multipart) message.getContent();
			                int numberOfParts = multiPart.getCount();
			                for (int partCount = 0; partCount < numberOfParts; partCount++) {
			                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
			                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			                        // this part is attachment
			                        String fileName = part.getFileName();	     
			                           //System.out.println("filename : " + fileName);
			                        final File file = new File(fileName);			                       	                        
			                         String[] FileType = part.getContentType().split("\\s");	                      
			             			InputStream fileData = part.getInputStream();
			             			
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	             			
			             			long Filesize =  part.getSize();
			             			MultipleAttachment multi  = new MultipleAttachment();
			             			multi.setFileName(fileName);
			             			multi.setFileType(FileType[0]);	
			             			multi.setFileSize(Filesize);
			             			multi.setFileByte(byteArray);			             		
			             			multipleAttachment.add(multi);
			             			
			             			 email2.setMultipleAttachment(multipleAttachment);
			                    }
			                    else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
			                    {	                    	
			                    	//System.out.println("inline one");
			                         String[] FileType = part.getContentType().split("\\s");		              
			             			InputStream fileData = part.getInputStream();			  	    	  
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	
			             			String imageStr = Base64.encodeBase64String(byteArray);			             			
			             			long Filesize =  part.getSize();			  
			             			String fileName = part.getFileName();
									
			             			  
			             			
			             			 String attributeValue ;
			             			  
			             			  if(FileType[0].toString().contains(";"))
			             			  {
			             				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
			             			  }else {
			             				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
			             			  }
			             			
			             			  
			             			  
			             			 Elements paragraphs = document.getElementsByTag("img"); 
						                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
							             
						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
						                {
						                	 String src = paragraph.attr("src").replace("cid:", "");
								              
								             
								                    if(contentid.equals(src)) 
								                    {
								                    	
								                    	paragraph.attr("src", attributeValue);
								                    	
								                    }
						                }								   
			             			
			                    }else if(part.isMimeType("multipart/*"))
			                    {                    	
			                    	 Multipart multiPart1 = (Multipart) part.getContent();
			                    	
			                    	int numberOfParts1 = multiPart1.getCount();
			                    	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
			                    	 {
			                    		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
			                    		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
					                    	{			                    		
			                    			 
			                    			    //System.out.println("inline two");
			                    			 
						                    	 String[] FileType = part1.getContentType().split("\\s");				                    
						             			InputStream fileData = part1.getInputStream();			  
						             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						             			int nRead;
						             			byte[] data = new byte[1024];
						             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
						             				buffer.write(data, 0, nRead);
						             			}
						             			buffer.flush();
						             			byte[] byteArray = buffer.toByteArray();	
						             			String imageStr = Base64.encodeBase64String(byteArray);
						             			long Filesize = part.getSize();
												String fileName = part.getFileName();
												
						             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
						             			 
						             			
						             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
						             			   
						             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
						             			  
									                Elements paragraphs = document.getElementsByTag("img"); 
									                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
										             
									                for ( org.jsoup.nodes.Element paragraph :paragraphs )
									                {
									                	 String src = paragraph.attr("src").replace("cid:", "");
											              
											             
											                    if(contentid.equals(src)) 
											                    {
											                    	
											                    	paragraph.attr("src", attributeValue);
											                    	
											                    }
									                }								                  
					                    	}
			                    		 else if(part1.getDisposition() ==  null)
					                    	{
					                    		
					                    		   if(part1.isMimeType("multipart/*")) 
					                    		   {
					                    			   Multipart multiPart2 = (Multipart) part1.getContent();
					                    			  // //System.out.println("multiPart2" + multiPart2);	    
								                    	int numberOfParts2 = multiPart2.getCount();
								                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
								                    	 {
								                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
								                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
								                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
										                    	{
								                    			 //System.out.println("mutipart inline  has inline if ");	         
										                    		
											                    	 String[] FileType = part2.getContentType().split("\\s");						                     
											             			InputStream fileData = part2.getInputStream();			   						             			 
											            			 						             			  
											             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
											             			int nRead;
											             			byte[] data = new byte[1024];
											             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
											             				buffer.write(data, 0, nRead);
											             			}
											             			buffer.flush();
											             			byte[] byteArray = buffer.toByteArray();	
											             			String imageStr = Base64.encodeBase64String(byteArray);
											             			long Filesize = part.getSize();
																	String fileName = part.getFileName();
																	
											             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
											             			  // //System.out.println(" inline attributeValue"+attributeValue);
														                Elements paragraphs = document.getElementsByTag("img"); 
														                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
														               // //System.out.println(" part2 contentid"+contentid);
														                for ( org.jsoup.nodes.Element paragraph :paragraphs )
														                {
														                	 String src = paragraph.attr("src").replace("cid:", "");
																             
														                	//  //System.out.println(" part2 src"+src);
																                    if(contentid.equals(src)) 
																                    {
																                    	// //System.out.println("if condition mutipart ");	
																                    	paragraph.attr("src", attributeValue);
																                    	
																                    }
														                }								                  
										                    	}
								                    	 }
					                    			   
					                    			  
					                    		   }
						                    	
					                    	}
			                    	 }			                    	
			                    	
			                    	  email2.setBody(document.toString());
			                    }
			                }
			                
			                email2.setBody(document.toString());
			            }
//							email2 = new Email(from, toArray, subject, result, ccArray, bccArray, uid);
						emailList.add(email2);
//						}

//						}
					}
				}
			}

		}
		if (emailList.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity(emailList.stream()
				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), lst -> {
					Collections.reverse(lst);
					return lst.stream();
				})).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);
	}

	@GetMapping("/{emailType}/{userId}/{emailId}")
	public ResponseEntity idBasedList(@PathVariable("userId") Integer id, @PathVariable("emailType") String emailType,
			@PathVariable("emailId") Integer emailId) throws Exception {
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
			//System.out.println(OriginalPassword);
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

		String folderName;
//        Folder[] folder = store.getDefaultFolder().list();
//        for (int k = 0; k <= folder.length; k++) {
//			if (folder.length > k) {
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
		Message message;
		try {
			Store store = session.getStore("imaps");
			store.connect(user.get().getEmailConfiguration().getIncomingHost(), username, OriginalPassword);
			Folder emailFolder = store.getFolder(folderName);
			if (emailFolder.getMessageCount() != 0) {
				emailFolder.open(Folder.READ_ONLY);
				UIDFolder uf = (UIDFolder) emailFolder;
				message = uf.getMessageByUID(emailId);
				//System.out.println("message" + message);
				if (message == null) {
					//System.out.println("if");
					ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", res.setErrorCode(404));
					jsonObject.put("message", res.setMessage("Email with id=" + emailId + " not found"));
					// //System.out.println(jsonObject);
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
				} else {
					//System.out.println("else");
//						Message message = messages[i];
					Flags add = message.getFlags();
					//System.out.println("add" + add);

					FetchProfile fp = new FetchProfile();
					fp.add(UIDFolder.FetchProfileItem.UID);
					emailFolder.fetch(emailFolder.getMessages(), fp);
					UIDFolder pf = (UIDFolder) emailFolder;
					long uid = pf.getUID(message);

					Address[] a;

					// FROM
					if ((a = message.getFrom()) != null) {
						for (int j = 0; j < a.length; j++)
							from = a[j].toString();
					}

					List<String> toMail = new ArrayList<String>();
					// TO
					if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
						for (int j = 0; j < a.length; j++) {
							to = a[j].toString();
							toMail.add(to);
							toArray = toMail.toArray(new String[toMail.size()]);
						}
					} else {
						toArray = null;
					}
					List<String> ccMail = new ArrayList<String>();
					// CC
					if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
						// //System.out.println("ifccArray"+ccArray);
						for (int j = 0; j < a.length; j++) {
							cc = a[j].toString();
							ccMail.add(cc);
							ccArray = ccMail.toArray(new String[ccMail.size()]);
						}
					} else {
						ccArray = null;
						// //System.out.println("elseccArray"+ccArray);
					}

					List<String> bccMail = new ArrayList<String>();
					// BCC
					if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
						// //System.out.println("ifccArray"+ccArray);
						for (int j = 0; j < a.length; j++) {
							bcc = a[j].toString();
							bccMail.add(bcc);
							bccArray = bccMail.toArray(new String[bccMail.size()]);
						}
					} else {
						bccArray = null;
						// //System.out.println("elseccArray"+ccArray);
					}

					// SUBJECT
					if (message.getSubject() != null) {
						subject = message.getSubject();
					} else {
						subject = null;
					}

					// writePart(message);
					String result = "";
					if (message.isMimeType("text/plain")) {
						result = message.getContent().toString();
					} else if (message.isMimeType("multipart/*")) {
						MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
						////System.out.println("mimeMultipart" + mimeMultipart.toString());
						result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);
						
						result = result.replace("\\", "");
					}
					
					List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
					DateTime dtRec = new DateTime(message.getReceivedDate());
					email2 = new Email(from, toArray, subject, result, dtRec, ccArray, bccArray, message.getFolder(),
							uid, uid, message.getFlags(), multipleAttachment);

					if (message.getFlags().contains(Flag.ANSWERED)) {
						email2.setRepliedFlag(true);
					}

					if (message.getFlags().contains("Forwarded")) {
						email2.setForwardedFlag(true);
					}
						 Document document = Jsoup.parse(result);
						String contentType = message.getContentType();
						if (message.isMimeType("multipart/*")) { 
			                // content may contain attachments
			                Multipart multiPart = (Multipart) message.getContent();
			                int numberOfParts = multiPart.getCount();
			                for (int partCount = 0; partCount < numberOfParts; partCount++) {
			                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
			                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			                        // this part is attachment
			                        String fileName = part.getFileName();	     
			                           //System.out.println("filename : " + fileName);
			                        final File file = new File(fileName);			                       	                        
			                         String[] FileType = part.getContentType().split("\\s");	                      
			             			InputStream fileData = part.getInputStream();
			             			
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	             			
			             			long Filesize =  part.getSize();
			             			MultipleAttachment multi  = new MultipleAttachment();
			             			multi.setFileName(fileName);
			             			multi.setFileType(FileType[0]);	
			             			multi.setFileSize(Filesize);
			             			multi.setFileByte(byteArray);			             		
			             			multipleAttachment.add(multi);
			             			
			             			 email2.setMultipleAttachment(multipleAttachment);
			                    }
			                    else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
			                    {	                    	
			                    	//System.out.println("inline one");
			                         String[] FileType = part.getContentType().split("\\s");		              
			             			InputStream fileData = part.getInputStream();			  	    	  
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	
			             			String imageStr = Base64.encodeBase64String(byteArray);			             			
			             			long Filesize =  part.getSize();			  
			             			String fileName = part.getFileName();
									
			             			  
			             			
			             			 String attributeValue ;
			             			  
			             			  if(FileType[0].toString().contains(";"))
			             			  {
			             				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
			             			  }else {
			             				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
			             			  }
			             			
			             			  
			             			  
			             			 Elements paragraphs = document.getElementsByTag("img"); 
						                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
							             
						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
						                {
						                	 String src = paragraph.attr("src").replace("cid:", "");
								              
								             
								                    if(contentid.equals(src)) 
								                    {
								                    	
								                    	paragraph.attr("src", attributeValue);
								                    	
								                    }
						                }								   
			             			
			                    }else if(part.isMimeType("multipart/*"))
			                    {                    	
			                    	 Multipart multiPart1 = (Multipart) part.getContent();
			                    	
			                    	int numberOfParts1 = multiPart1.getCount();
			                    	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
			                    	 {
			                    		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
			                    		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
					                    	{			                    		
			                    			 
			                    			    //System.out.println("inline two");
			                    			 
						                    	 String[] FileType = part1.getContentType().split("\\s");				                    
						             			InputStream fileData = part1.getInputStream();			  
						             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						             			int nRead;
						             			byte[] data = new byte[1024];
						             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
						             				buffer.write(data, 0, nRead);
						             			}
						             			buffer.flush();
						             			byte[] byteArray = buffer.toByteArray();	
						             			String imageStr = Base64.encodeBase64String(byteArray);
						             			long Filesize = part.getSize();
												
												
														 
						             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
						             			 
						             			
						             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
						             			   
						             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
						             			  
									                Elements paragraphs = document.getElementsByTag("img"); 
									                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
										             
									                for ( org.jsoup.nodes.Element paragraph :paragraphs )
									                {
									                	 String src = paragraph.attr("src").replace("cid:", "");
											              
											             
											                    if(contentid.equals(src)) 
											                    {
											                    	
											                    	paragraph.attr("src", attributeValue);
											                    	
											                    }
									                }								                  
					                    	}
			                    		 else if(part1.getDisposition() ==  null)
					                    	{
					                    		
					                    		   if(part1.isMimeType("multipart/*")) 
					                    		   {
					                    			   Multipart multiPart2 = (Multipart) part1.getContent();
					                    			  // //System.out.println("multiPart2" + multiPart2);	    
								                    	int numberOfParts2 = multiPart2.getCount();
								                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
								                    	 {
								                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
								                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
								                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
										                    	{
								                    			 //System.out.println("mutipart inline  has inline if ");	         
										                    		
											                    	 String[] FileType = part2.getContentType().split("\\s");						                     
											             			InputStream fileData = part2.getInputStream();			   						             			 
											            			 						             			  
											             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
											             			int nRead;
											             			byte[] data = new byte[1024];
											             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
											             				buffer.write(data, 0, nRead);
											             			}
											             			buffer.flush();
											             			byte[] byteArray = buffer.toByteArray();	
											             			String imageStr = Base64.encodeBase64String(byteArray);
											             			long Filesize = part.getSize();
																	String fileName = part.getFileName();
																	
											             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
											             			  // //System.out.println(" inline attributeValue"+attributeValue);
														                Elements paragraphs = document.getElementsByTag("img"); 
														                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
														               // //System.out.println(" part2 contentid"+contentid);
														                for ( org.jsoup.nodes.Element paragraph :paragraphs )
														                {
														                	 String src = paragraph.attr("src").replace("cid:", "");
																             
														                	//  //System.out.println(" part2 src"+src);
																                    if(contentid.equals(src)) 
																                    {
																                    	// //System.out.println("if condition mutipart ");	
																                    	paragraph.attr("src", attributeValue);
																                    	
																                    }
														                }								                  
										                    	}
								                    	 }
					                    			   
					                    			  
					                    		   }
						                    	
					                    	}
			                    	 }			                    	
			                    	
			                    	  email2.setBody(document.toString());
			                    }
			                }
			                
			                email2.setBody(document.toString());
			            }
					store.close();
				}
//					
			}
		} catch (Exception e) {
			//System.out.println("ex" + e);

		}
//    	if (emailList.isEmpty()) {
//			ObjectNode jsonObject = objectMapper.createObjectNode();
//			jsonObject.put("errorCode", res.setStatusCode(204));
//			jsonObject.put("message", res.setMessage("No Data"));
//			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
//		}
		return new ResponseEntity(email2, HttpStatus.OK);
	}

	@GetMapping("/inbox/{userId}")
	public ResponseEntity inboxList(@PathVariable("userId") Integer id) throws Exception {
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchInboxList(host, mailstoreType, username, OriginalPassword, port, auth, security);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchInboxList(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security) throws Exception {

		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> relativebase64array = new ArrayList<String>();

		BodyPart messageBodyPart = new MimeBodyPart();
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
		//System.out.println("emailFolder" + store.getDefaultFolder().list());
		emailFolder.open(Folder.READ_ONLY);
		Folder[] f = store.getDefaultFolder().list();
		for (Folder fd : f)
			System.out.println(">> " + fd.getName());
		// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		// retrieve the messages from the folder in an array and print it
		Message[] messages = emailFolder.getMessages();
//				User user = userService.findByUserName(uname);
		// //System.out.println("user" + user);
		emailList = new ArrayList<Email>();

		//System.out.println(" messages.length" + messages.length);
		for (int i = 0; i < messages.length; i++) {

			Message message = messages[i];

			FetchProfile fp = new FetchProfile();
			fp.add(UIDFolder.FetchProfileItem.UID);
			emailFolder.fetch(emailFolder.getMessages(), fp);
			UIDFolder pf = (UIDFolder) emailFolder;
			long uid = pf.getUID(message);

			Address[] a;

			// FROM
			if ((a = message.getFrom()) != null) {
				for (int j = 0; j < a.length; j++)
					from = a[j].toString();
			}

			List<String> toMail = new ArrayList<String>();
			// TO
			if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
				for (int j = 0; j < a.length; j++) {
					to = a[j].toString();
					toMail.add(to);
					toArray = toMail.toArray(new String[toMail.size()]);
				}
			} else {
				toArray = null;
			}
			List<String> ccMail = new ArrayList<String>();
			// CC
			if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					cc = a[j].toString();
					ccMail.add(cc);
					ccArray = ccMail.toArray(new String[ccMail.size()]);
				}
			} else {
				ccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}

			List<String> bccMail = new ArrayList<String>();
			// BCC
			if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					bcc = a[j].toString();
					bccMail.add(bcc);
					bccArray = bccMail.toArray(new String[bccMail.size()]);
				}
			} else {
				bccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}

			// SUBJECT
			if (message.getSubject() != null) {
				subject = message.getSubject();
			} else {
				subject = null;
			}

			// writePart(message);

			String result = "";
			if (message.isMimeType("text/plain")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				// //System.out.println("mimeMultipart" + mimeMultipart.toString());
				result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

				result = result.replace("\\", "");
				// //System.out.println("result " + result);
			}

			List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//			Enumeration headers = messages[i].getAllHeaders();
//			  while (headers.hasMoreElements()) {
//				  Header h = (Header) headers.nextElement();
//				  //System.out.println("dt::"+h.getName());
//				  //System.out.println(h.getName() + ": " + h.getValue());
//				  }
			// //System.out.println(" Email Data null");
			DateTime dtRec = new DateTime(message.getReceivedDate());

			email2 = new Email(from, toArray, subject, result, dtRec, ccArray, bccArray, message.getFolder(), uid,
					message.getFlags(), multipleAttachment);

			if (message.getFlags().contains(Flag.ANSWERED)) {
				email2.setRepliedFlag(true);
			}

			if (message.getFlags().contains("Forwarded")) {
				email2.setForwardedFlag(true);
			}

			//System.out.println(result.getBytes());
			Document document = Jsoup.parse(result);
			String contentType = message.getContentType();
			if (message.isMimeType("multipart/*")) { 
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();	     
                           //System.out.println("filename : " + fileName);
                        final File file = new File(fileName);			                       	                        
                         String[] FileType = part.getContentType().split("\\s");	                      
             			InputStream fileData = part.getInputStream();
             			
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	             			
             			long Filesize =  part.getSize();
             			MultipleAttachment multi  = new MultipleAttachment();
             			multi.setFileName(fileName);
             			multi.setFileType(FileType[0]);	
             			multi.setFileSize(Filesize);
             			multi.setFileByte(byteArray);			             		
             			multipleAttachment.add(multi);
             			
             			 email2.setMultipleAttachment(multipleAttachment);
                    }
                    else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
                    {	                    	
                    	//System.out.println("inline one");
                         String[] FileType = part.getContentType().split("\\s");		              
             			InputStream fileData = part.getInputStream();			  	    	  
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	
             			String imageStr = Base64.encodeBase64String(byteArray);			             			
             			long Filesize =  part.getSize();			  
             			String fileName = part.getFileName();
						
//             			 //System.out.println("FileType[0].toString()" + FileType[0].toString());
//             			   
//             			  //System.out.println(" part.getEncoding()" +  part.getEncoding());
             			  
             			
             			 String attributeValue ;
             			  
             			  if(FileType[0].toString().contains(";"))
             			  {
             				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
             			  }else {
             				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
             			  }
             			
             			  
             			  
             			 Elements paragraphs = document.getElementsByTag("img"); 
			                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
				             
			                for ( org.jsoup.nodes.Element paragraph :paragraphs )
			                {
			                	 String src = paragraph.attr("src").replace("cid:", "");
					              
					             
					                    if(contentid.equals(src)) 
					                    {
					                    	
					                    	paragraph.attr("src", attributeValue);
					                    	
					                    }
			                }								   
             			
                    }else if(part.isMimeType("multipart/*"))
                    {                    	
                    	 Multipart multiPart1 = (Multipart) part.getContent();
                    	
                    	int numberOfParts1 = multiPart1.getCount();
                    	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
                    	 {
                    		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
                    		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
		                    	{			                    		
                    			 
                    			    //System.out.println("inline two");
                    			 
			                    	 String[] FileType = part1.getContentType().split("\\s");				                    
			             			InputStream fileData = part1.getInputStream();			  
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	
			             			String imageStr = Base64.encodeBase64String(byteArray);
			             			long Filesize = part.getSize();
									String fileName = part.getFileName();
									

								//	email2.setMultipleAttachment(multipleAttachment);  			 
			             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
			             			 
			             			
			             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
			             			   
			             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
			             			  
						                Elements paragraphs = document.getElementsByTag("img"); 
						                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
							             
						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
						                {
						                	 String src = paragraph.attr("src").replace("cid:", "");
								              
								             
								                    if(contentid.equals(src)) 
								                    {
								                    	
								                    	paragraph.attr("src", attributeValue);
								                    	
								                    }
						                }								                  
		                    	}
                    		 else if(part1.getDisposition() ==  null)
		                    	{
		                    		
		                    		   if(part1.isMimeType("multipart/*")) 
		                    		   {
		                    			   Multipart multiPart2 = (Multipart) part1.getContent();
		                    			  // //System.out.println("multiPart2" + multiPart2);	    
					                    	int numberOfParts2 = multiPart2.getCount();
					                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
					                    	 {
					                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
					                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
					                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
							                    	{
					                    			 //System.out.println("mutipart inline  has inline if ");	         
							                    		
								                    	 String[] FileType = part2.getContentType().split("\\s");						                     
								             			InputStream fileData = part2.getInputStream();			   						             			 
								            			 						             			  
								             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
								             			int nRead;
								             			byte[] data = new byte[1024];
								             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
								             				buffer.write(data, 0, nRead);
								             			}
								             			buffer.flush();
								             			byte[] byteArray = buffer.toByteArray();	
								             			String imageStr = Base64.encodeBase64String(byteArray);
								             			long Filesize = part.getSize();
														String fileName = part.getFileName();
														
								             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
								             			  // //System.out.println(" inline attributeValue"+attributeValue);
											                Elements paragraphs = document.getElementsByTag("img"); 
											                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
											               // //System.out.println(" part2 contentid"+contentid);
											                for ( org.jsoup.nodes.Element paragraph :paragraphs )
											                {
											                	 String src = paragraph.attr("src").replace("cid:", "");
													             
											                	//  //System.out.println(" part2 src"+src);
													                    if(contentid.equals(src)) 
													                    {
													                    	// //System.out.println("if condition mutipart ");	
													                    	paragraph.attr("src", attributeValue);
													                    	
													                    }
											                }								                  
							                    	}
					                    	 }
		                    			   
		                    			  
		                    		   }
			                    	
		                    	}
                    	 }			                    	
                    	
                    	  email2.setBody(document.toString());
                    }
                }
                
                email2.setBody(document.toString());
            }

//			email2.setBody(document.toString());
			emailList.add(email2);

		}
		if (emailList.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity(emailList.stream()
				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), lst -> {
					Collections.reverse(lst);
					return lst.stream();
				})).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);

	}

	@GetMapping("/important/{userId}")
	public ResponseEntity importantList(@PathVariable("userId") Integer id) throws Exception {
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchImportantList(host, mailstoreType, username, OriginalPassword, port, auth, security);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchImportantList(String hostval, String mailStrProt, String uname, String password,
			int port, boolean auth, boolean security) throws Exception {
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
		emailList = new ArrayList<Email>();
		Folder[] folder = store.getDefaultFolder().list();
		for (int k = 0; k <= folder.length; k++) {
			//System.out.println("k" + k);
			if (folder.length > k) {

				// create the folder object and open it
				Folder emailFolder = store.getFolder(folder[k].getName());
				//System.out.println("emailFolder" + emailFolder.getMessageCount());
				if (emailFolder.getMessageCount() != 0) {
					emailFolder.open(Folder.READ_ONLY);
					Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));
					//System.out.println("messages.length" + messages.length);
//					Message[] messages = emailFolder.getMessages();
					for (int i = 0; i < messages.length; i++) {
						//System.out.println("i" + i);
//						if (k == i) {
						//System.out.println("k==i");
						Message message = messages[i];
						//System.out.println("" + message.getSubject());
//							if (message.getFlags().contains(Flag.FLAGGED)) {
						//System.out.println("FLAGGED");
						FetchProfile fp = new FetchProfile();
						fp.add(UIDFolder.FetchProfileItem.UID);
						emailFolder.fetch(emailFolder.getMessages(), fp);

						UIDFolder pf = (UIDFolder) emailFolder;
						long uid = pf.getUID(message);

						Address[] a;

						// FROM
						if ((a = message.getFrom()) != null) {
							for (int j = 0; j < a.length; j++)
								from = a[j].toString();
						}

						List<String> toMail = new ArrayList<String>();
						// TO
						if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
							for (int j = 0; j < a.length; j++) {
								to = a[j].toString();
								toMail.add(to);
								toArray = toMail.toArray(new String[toMail.size()]);
							}
						} else {
							toArray = null;
						}
						List<String> ccMail = new ArrayList<String>();
						// CC
						if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
							// //System.out.println("ifccArray"+ccArray);
							for (int j = 0; j < a.length; j++) {
								cc = a[j].toString();
								ccMail.add(cc);
								ccArray = ccMail.toArray(new String[ccMail.size()]);
							}
						} else {
							ccArray = null;
							// //System.out.println("elseccArray"+ccArray);
						}

						List<String> bccMail = new ArrayList<String>();
						// BCC
						if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
							// //System.out.println("ifccArray"+ccArray);
							for (int j = 0; j < a.length; j++) {
								bcc = a[j].toString();
								bccMail.add(bcc);
								bccArray = bccMail.toArray(new String[bccMail.size()]);
							}
						} else {
							bccArray = null;
							// //System.out.println("elseccArray"+ccArray);
						}

						// SUBJECT
						if (message.getSubject() != null) {
							subject = message.getSubject();
						} else {
							subject = null;
						}

						// writePart(message);

						String result = "";
						if (message.isMimeType("text/plain")) {
							result = message.getContent().toString();
						} else if (message.isMimeType("multipart/*")) {
							MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
							// //System.out.println("mimeMultipart" + mimeMultipart.toString());
							result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

							result = result.replace("\\", "");

							// //System.out.println("result " + result);
						}
						List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//								Enumeration headers = messages[i].getAllHeaders();
//								  while (headers.hasMoreElements()) {
//									  Header h = (Header) headers.nextElement();
//									  //System.out.println("dt::"+h.getName());
//									  //System.out.println(h.getName() + ": " + h.getValue());
//									  }
						// //System.out.println(" Email Data null");
						DateTime dtRec = new DateTime(message.getReceivedDate());

						email2 = new Email(from, uid, toArray, subject, result, dtRec, ccArray, bccArray,
								message.getFolder(), uid, message.getFlags(), multipleAttachment);
						if (message.getFlags().contains(Flag.ANSWERED)) {
							email2.setRepliedFlag(true);
						}

						if (message.getFlags().contains("Forwarded")) {
							email2.setForwardedFlag(true);
						}
						//System.out.println(result.getBytes());
						Document document = Jsoup.parse(result);
						String contentType = message.getContentType();
						if (message.isMimeType("multipart/*")) { 
			                // content may contain attachments
			                Multipart multiPart = (Multipart) message.getContent();
			                int numberOfParts = multiPart.getCount();
			                for (int partCount = 0; partCount < numberOfParts; partCount++) {
			                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
			                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			                        // this part is attachment
			                        String fileName = part.getFileName();	     
			                           //System.out.println("filename : " + fileName);
			                        final File file = new File(fileName);			                       	                        
			                         String[] FileType = part.getContentType().split("\\s");	                      
			             			InputStream fileData = part.getInputStream();
			             			
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	             			
			             			long Filesize =  part.getSize();
			             			MultipleAttachment multi  = new MultipleAttachment();
			             			multi.setFileName(fileName);
			             			multi.setFileType(FileType[0]);	
			             			multi.setFileSize(Filesize);
			             			multi.setFileByte(byteArray);			             		
			             			multipleAttachment.add(multi);
			             			
			             			 email2.setMultipleAttachment(multipleAttachment);
			                    }
			                    else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
			                    {	                    	
			                    	//System.out.println("inline one");
			                         String[] FileType = part.getContentType().split("\\s");		              
			             			InputStream fileData = part.getInputStream();			  	    	  
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	
			             			String imageStr = Base64.encodeBase64String(byteArray);			             			
			             			long Filesize =  part.getSize();			  
			             			String fileName = part.getFileName();
									
			             			
			             			 String attributeValue ;
			             			  
			             			  if(FileType[0].toString().contains(";"))
			             			  {
			             				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
			             			  }else {
			             				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
			             			  }
			             			
			             			  
			             			  
			             			 Elements paragraphs = document.getElementsByTag("img"); 
						                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
							             
						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
						                {
						                	 String src = paragraph.attr("src").replace("cid:", "");
								              
								             
								                    if(contentid.equals(src)) 
								                    {
								                    	
								                    	paragraph.attr("src", attributeValue);
								                    	
								                    }
						                }								   
			             			
			                    }else if(part.isMimeType("multipart/*"))
			                    {                    	
			                    	 Multipart multiPart1 = (Multipart) part.getContent();
			                    	
			                    	int numberOfParts1 = multiPart1.getCount();
			                    	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
			                    	 {
			                    		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
			                    		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
					                    	{			                    		
			                    			 
			                    			    //System.out.println("inline two");
			                    			 
						                    	 String[] FileType = part1.getContentType().split("\\s");				                    
						             			InputStream fileData = part1.getInputStream();			  
						             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						             			int nRead;
						             			byte[] data = new byte[1024];
						             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
						             				buffer.write(data, 0, nRead);
						             			}
						             			buffer.flush();
						             			byte[] byteArray = buffer.toByteArray();	
						             			String imageStr = Base64.encodeBase64String(byteArray);
						             			long Filesize = part.getSize();
												String fileName = part.getFileName();
														 
						             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
						             			 
						             			
						             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
						             			   
						             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
						             			  
									                Elements paragraphs = document.getElementsByTag("img"); 
									                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
										             
									                for ( org.jsoup.nodes.Element paragraph :paragraphs )
									                {
									                	 String src = paragraph.attr("src").replace("cid:", "");
											              
											             
											                    if(contentid.equals(src)) 
											                    {
											                    	
											                    	paragraph.attr("src", attributeValue);
											                    	
											                    }
									                }								                  
					                    	}
			                    		 else if(part1.getDisposition() ==  null)
					                    	{
					                    		
					                    		   if(part1.isMimeType("multipart/*")) 
					                    		   {
					                    			   Multipart multiPart2 = (Multipart) part1.getContent();
					                    			  // //System.out.println("multiPart2" + multiPart2);	    
								                    	int numberOfParts2 = multiPart2.getCount();
								                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
								                    	 {
								                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
								                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
								                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
										                    	{
								                    			 //System.out.println("mutipart inline  has inline if ");	         
										                    		
											                    	 String[] FileType = part2.getContentType().split("\\s");						                     
											             			InputStream fileData = part2.getInputStream();			   						             			 
											            			 						             			  
											             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
											             			int nRead;
											             			byte[] data = new byte[1024];
											             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
											             				buffer.write(data, 0, nRead);
											             			}
											             			buffer.flush();
											             			byte[] byteArray = buffer.toByteArray();	
											             			String imageStr = Base64.encodeBase64String(byteArray);
											             			long Filesize = part.getSize();
																	String fileName = part.getFileName();
																	
											             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
											             			  // //System.out.println(" inline attributeValue"+attributeValue);
														                Elements paragraphs = document.getElementsByTag("img"); 
														                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
														               // //System.out.println(" part2 contentid"+contentid);
														                for ( org.jsoup.nodes.Element paragraph :paragraphs )
														                {
														                	 String src = paragraph.attr("src").replace("cid:", "");
																             
														                	//  //System.out.println(" part2 src"+src);
																                    if(contentid.equals(src)) 
																                    {
																                    	// //System.out.println("if condition mutipart ");	
																                    	paragraph.attr("src", attributeValue);
																                    	
																                    }
														                }								                  
										                    	}
								                    	 }
					                    			   
					                    			  
					                    		   }
						                    	
					                    	}
			                    	 }			                    	
			                    	
			                    	  email2.setBody(document.toString());
			                    }
			                }
			                
			                email2.setBody(document.toString());
			            }
//								email2 = new Email(from, toArray, subject, result, ccArray, bccArray, uid);
						emailList.add(email2);
//							}

//						}
					}
				}
			}

		}
		if (emailList.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity(emailList.stream()
				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), lst -> {
					Collections.reverse(lst);
					return lst.stream();
				})).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);
	}

	@GetMapping("/sent/{userId}")
	public ResponseEntity sentEmailsListing(@PathVariable("userId") Integer id) throws Exception {
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchSentEmailsListing(host, mailstoreType, username, OriginalPassword, port, auth, security);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchSentEmailsListing(String hostval, String mailStrProt, String uname, String password,
			int port, boolean auth, boolean security) throws Exception {

		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> relativebase64array = new ArrayList<String>();

		BodyPart messageBodyPart = new MimeBodyPart();
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
		Folder emailFolder = store.getFolder("Sent");
		// ////System.out.println("emailFolder" + emailFolder);
		emailFolder.open(Folder.READ_ONLY);

		// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		// retrieve the messages from the folder in an array and print it
		Message[] messages = emailFolder.getMessages();
//				User user = userService.findByUserName(uname);
		// //System.out.println("user" + user);
		emailList = new ArrayList<Email>();
		//System.out.println(" messages.length" + messages.length);
		for (int i = 0; i < messages.length; i++) {

			Message message = messages[i];
			FetchProfile fp = new FetchProfile();
			fp.add(UIDFolder.FetchProfileItem.UID);
			emailFolder.fetch(emailFolder.getMessages(), fp);

			UIDFolder pf = (UIDFolder) emailFolder;
			long uid = pf.getUID(message);

			Address[] a;

			// FROM
			if ((a = message.getFrom()) != null) {
				for (int j = 0; j < a.length; j++)
					from = a[j].toString();
			}

			List<String> toMail = new ArrayList<String>();
			// TO
			if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
				for (int j = 0; j < a.length; j++) {
					to = a[j].toString();
					toMail.add(to);
					toArray = toMail.toArray(new String[toMail.size()]);
				}
			} else {
				toArray = null;
			}
			List<String> ccMail = new ArrayList<String>();
			// CC
			if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					cc = a[j].toString();
					ccMail.add(cc);
					ccArray = ccMail.toArray(new String[ccMail.size()]);
					//System.out.println("CCIF" + ccArray);
				}
			} else {
				//System.out.println("CCEL" + ccArray);
				ccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}

			List<String> bccMail = new ArrayList<String>();
			// BCC
			if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					bcc = a[j].toString();
					bccMail.add(bcc);
					bccArray = bccMail.toArray(new String[bccMail.size()]);
				}
			} else {
				bccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}

			// SUBJECT
			if (message.getSubject() != null) {
				subject = message.getSubject();
			} else {
				subject = null;
			}

			// writePart(message);

			String result = "";
			if (message.isMimeType("text/plain")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				// //System.out.println("mimeMultipart" + mimeMultipart.toString());
				result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

				result = result.replace("\\", "");
				// //System.out.println("result " + result);
			}

			List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//			Enumeration headers = messages[i].getAllHeaders();
//			  while (headers.hasMoreElements()) {
//				  Header h = (Header) headers.nextElement();
//				  //System.out.println("dt::"+h.getName());
//				  //System.out.println(h.getName() + ": " + h.getValue());
//				  }
			// //System.out.println(" Email Data null");
			DateTime dtRec = new DateTime(message.getReceivedDate());

			email2 = new Email(from, toArray, subject, result, dtRec, ccArray, bccArray, message.getFolder(), uid,
					message.getFlags(), multipleAttachment, uid);
			if (message.getFlags().contains(Flag.ANSWERED)) {
				email2.setRepliedFlag(true);
			}

			if (message.getFlags().contains("Forwarded")) {
				email2.setForwardedFlag(true);
			}
			//System.out.println("email2cc" + email2.getCc());
			//System.out.println(result.getBytes());
			Document document = Jsoup.parse(result);
			String contentType = message.getContentType();
			if (message.isMimeType("multipart/*")) { 
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();	     
                           //System.out.println("filename : " + fileName);
                        final File file = new File(fileName);			                       	                        
                         String[] FileType = part.getContentType().split("\\s");	                      
             			InputStream fileData = part.getInputStream();
             			
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	             			
             			long Filesize =  part.getSize();
             			MultipleAttachment multi  = new MultipleAttachment();
             			multi.setFileName(fileName);
             			multi.setFileType(FileType[0]);	
             			multi.setFileSize(Filesize);
             			multi.setFileByte(byteArray);			             		
             			multipleAttachment.add(multi);
             			
             			 email2.setMultipleAttachment(multipleAttachment);
                    }
                    else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
                    {	                    	
                    	//System.out.println("inline one");
                         String[] FileType = part.getContentType().split("\\s");		              
             			InputStream fileData = part.getInputStream();			  	    	  
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	
             			String imageStr = Base64.encodeBase64String(byteArray);			             			
             			long Filesize =  part.getSize();			  
             			String fileName = part.getFileName();
						
             			 String attributeValue ;
             			  
             			  if(FileType[0].toString().contains(";"))
             			  {
             				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
             			  }else {
             				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
             			  }
             			
             			  
             			  
             			 Elements paragraphs = document.getElementsByTag("img"); 
			                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
				             
			                for ( org.jsoup.nodes.Element paragraph :paragraphs )
			                {
			                	 String src = paragraph.attr("src").replace("cid:", "");
					              
					             
					                    if(contentid.equals(src)) 
					                    {
					                    	
					                    	paragraph.attr("src", attributeValue);
					                    	
					                    }
			                }								   
             			
                    }else if(part.isMimeType("multipart/*"))
                    {                    	
                    	 Multipart multiPart1 = (Multipart) part.getContent();
                    	
                    	int numberOfParts1 = multiPart1.getCount();
                    	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
                    	 {
                    		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
                    		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
		                    	{			                    		
                    			 
                    			    //System.out.println("inline two");
                    			 
			                    	 String[] FileType = part1.getContentType().split("\\s");				                    
			             			InputStream fileData = part1.getInputStream();			  
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	
			             			String imageStr = Base64.encodeBase64String(byteArray);
			             			long Filesize = part.getSize();
									String fileName = part.getFileName();
									
			             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
			             			 
			             			
			             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
			             			   
			             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
			             			  
						                Elements paragraphs = document.getElementsByTag("img"); 
						                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
							             
						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
						                {
						                	 String src = paragraph.attr("src").replace("cid:", "");
								              
								             
								                    if(contentid.equals(src)) 
								                    {
								                    	
								                    	paragraph.attr("src", attributeValue);
								                    	
								                    }
						                }								                  
		                    	}
                    		 else if(part1.getDisposition() ==  null)
		                    	{
		                    		
		                    		   if(part1.isMimeType("multipart/*")) 
		                    		   {
		                    			   Multipart multiPart2 = (Multipart) part1.getContent();
		                    			  // //System.out.println("multiPart2" + multiPart2);	    
					                    	int numberOfParts2 = multiPart2.getCount();
					                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
					                    	 {
					                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
					                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
					                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
							                    	{
					                    			 //System.out.println("mutipart inline  has inline if ");	         
							                    		
								                    	 String[] FileType = part2.getContentType().split("\\s");						                     
								             			InputStream fileData = part2.getInputStream();			   						             			 
								            			 						             			  
								             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
								             			int nRead;
								             			byte[] data = new byte[1024];
								             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
								             				buffer.write(data, 0, nRead);
								             			}
								             			buffer.flush();
								             			byte[] byteArray = buffer.toByteArray();	
								             			String imageStr = Base64.encodeBase64String(byteArray);
								             			long Filesize = part.getSize();
														String fileName = part.getFileName();
														
								             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
								             			  // //System.out.println(" inline attributeValue"+attributeValue);
											                Elements paragraphs = document.getElementsByTag("img"); 
											                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
											               // //System.out.println(" part2 contentid"+contentid);
											                for ( org.jsoup.nodes.Element paragraph :paragraphs )
											                {
											                	 String src = paragraph.attr("src").replace("cid:", "");
													             
											                	//  //System.out.println(" part2 src"+src);
													                    if(contentid.equals(src)) 
													                    {
													                    	// //System.out.println("if condition mutipart ");	
													                    	paragraph.attr("src", attributeValue);
													                    	
													                    }
											                }								                  
							                    	}
					                    	 }
		                    			   
		                    			  
		                    		   }
			                    	
		                    	}
                    	 }			                    	
                    	
                    	  email2.setBody(document.toString());
                    }
                }
                
                email2.setBody(document.toString());
            }

//			email2.setBody(document.toString());
			emailList.add(email2);
		}
		if (emailList.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity(emailList.stream()
				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), lst -> {
					Collections.reverse(lst);
					return lst.stream();
				})).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);

	}

	@GetMapping("/trash/{userId}")
	public ResponseEntity trashListing(@PathVariable("userId") Integer id) throws Exception {
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			return fetchTrashListing(host, mailstoreType, username, OriginalPassword, port, auth, security);
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchTrashListing(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security) throws Exception {

		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> relativebase64array = new ArrayList<String>();

		BodyPart messageBodyPart = new MimeBodyPart();
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
		// //System.out.println("emailFolder" + emailFolder);
		emailFolder.open(Folder.READ_ONLY);

		// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		// retrieve the messages from the folder in an array and print it
		Message[] messages = emailFolder.getMessages();
//				User user = userService.findByUserName(uname);
		// //System.out.println("user" + user);
		emailList = new ArrayList<Email>();
		//System.out.println(" messages.length" + messages.length);
		for (int i = 0; i < messages.length; i++) {

			Message message = messages[i];
			FetchProfile fp = new FetchProfile();
			fp.add(UIDFolder.FetchProfileItem.UID);
			emailFolder.fetch(emailFolder.getMessages(), fp);

			UIDFolder pf = (UIDFolder) emailFolder;
			long uid = pf.getUID(message);

			Address[] a;

			// FROM
			if ((a = message.getFrom()) != null) {
				for (int j = 0; j < a.length; j++)
					from = a[j].toString();
			}

			List<String> toMail = new ArrayList<String>();
			// TO
			if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
				for (int j = 0; j < a.length; j++) {
					to = a[j].toString();
					toMail.add(to);
					toArray = toMail.toArray(new String[toMail.size()]);
				}
			} else {
				toArray = null;
			}
			List<String> ccMail = new ArrayList<String>();
			// CC
			if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					cc = a[j].toString();
					ccMail.add(cc);
					ccArray = ccMail.toArray(new String[ccMail.size()]);
				}
			} else {
				ccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}

			List<String> bccMail = new ArrayList<String>();
			// BCC
			if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
				// //System.out.println("ifccArray"+ccArray);
				for (int j = 0; j < a.length; j++) {
					bcc = a[j].toString();
					bccMail.add(bcc);
					bccArray = bccMail.toArray(new String[bccMail.size()]);
				}
			} else {
				bccArray = null;
				// //System.out.println("elseccArray"+ccArray);
			}

			// SUBJECT
			if (message.getSubject() != null) {
				subject = message.getSubject();
			} else {
				subject = null;
			}

			// writePart(message);

			String result = "";
			if (message.isMimeType("text/plain")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				// //System.out.println("mimeMultipart" + mimeMultipart.toString());
				result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

				result = result.replace("\\", "");
				// //System.out.println("result " + result);
			}

			List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//			Enumeration headers = messages[i].getAllHeaders();
//			  while (headers.hasMoreElements()) {
//				  Header h = (Header) headers.nextElement();
//				  //System.out.println("dt::"+h.getName());
//				  //System.out.println(h.getName() + ": " + h.getValue());
//				  }
			// //System.out.println(" Email Data null");
			DateTime dtRec = new DateTime(message.getReceivedDate());

			email2 = new Email(uid, from, toArray, subject, result, dtRec, ccArray, bccArray, message.getFolder(), uid,
					message.getFlags(), multipleAttachment);
			if (message.getFlags().contains(Flag.ANSWERED)) {
				email2.setRepliedFlag(true);
			}

			if (message.getFlags().contains("Forwarded")) {
				email2.setForwardedFlag(true);
			}
			//System.out.println(result.getBytes());
			Document document = Jsoup.parse(result);
			String contentType = message.getContentType();
			if (message.isMimeType("multipart/*")) { 
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();	     
                           //System.out.println("filename : " + fileName);
                        final File file = new File(fileName);			                       	                        
                         String[] FileType = part.getContentType().split("\\s");	                      
             			InputStream fileData = part.getInputStream();
             			
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	             			
             			long Filesize =  part.getSize();
             			MultipleAttachment multi  = new MultipleAttachment();
             			multi.setFileName(fileName);
             			multi.setFileType(FileType[0]);	
             			multi.setFileSize(Filesize);
             			multi.setFileByte(byteArray);			             		
             			multipleAttachment.add(multi);
             			
             			 email2.setMultipleAttachment(multipleAttachment);
                    }
                    else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
                    {	                    	
                    	//System.out.println("inline one");
                         String[] FileType = part.getContentType().split("\\s");		              
             			InputStream fileData = part.getInputStream();			  	    	  
             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             			int nRead;
             			byte[] data = new byte[1024];
             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
             				buffer.write(data, 0, nRead);
             			}
             			buffer.flush();
             			byte[] byteArray = buffer.toByteArray();	
             			String imageStr = Base64.encodeBase64String(byteArray);			             			
             			long Filesize =  part.getSize();			  
             			String fileName = part.getFileName();
						
             			  
             			
             			 String attributeValue ;
             			  
             			  if(FileType[0].toString().contains(";"))
             			  {
             				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
             			  }else {
             				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
             			  }
             			
             			  
             			  
             			 Elements paragraphs = document.getElementsByTag("img"); 
			                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
				             
			                for ( org.jsoup.nodes.Element paragraph :paragraphs )
			                {
			                	 String src = paragraph.attr("src").replace("cid:", "");
					              
					             
					                    if(contentid.equals(src)) 
					                    {
					                    	
					                    	paragraph.attr("src", attributeValue);
					                    	
					                    }
			                }								   
             			
                    }else if(part.isMimeType("multipart/*"))
                    {                    	
                    	 Multipart multiPart1 = (Multipart) part.getContent();
                    	
                    	int numberOfParts1 = multiPart1.getCount();
                    	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
                    	 {
                    		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
                    		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
		                    	{			                    		
                    			 
                    			    //System.out.println("inline two");
                    			 
			                    	 String[] FileType = part1.getContentType().split("\\s");				                    
			             			InputStream fileData = part1.getInputStream();			  
			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			             			int nRead;
			             			byte[] data = new byte[1024];
			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
			             				buffer.write(data, 0, nRead);
			             			}
			             			buffer.flush();
			             			byte[] byteArray = buffer.toByteArray();	
			             			String imageStr = Base64.encodeBase64String(byteArray);
			             			long Filesize = part.getSize();
									String fileName = part.getFileName();
								
									//email2.setMultipleAttachment(multipleAttachment);  			 
			             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
			             			 
			             			
			             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
			             			   
			             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
			             			  
						                Elements paragraphs = document.getElementsByTag("img"); 
						                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
							             
						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
						                {
						                	 String src = paragraph.attr("src").replace("cid:", "");
								              
								             
								                    if(contentid.equals(src)) 
								                    {
								                    	
								                    	paragraph.attr("src", attributeValue);
								                    	
								                    }
						                }								                  
		                    	}
                    		 else if(part1.getDisposition() ==  null)
		                    	{
		                    		
		                    		   if(part1.isMimeType("multipart/*")) 
		                    		   {
		                    			   Multipart multiPart2 = (Multipart) part1.getContent();
		                    			  // //System.out.println("multiPart2" + multiPart2);	    
					                    	int numberOfParts2 = multiPart2.getCount();
					                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
					                    	 {
					                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
					                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
					                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
							                    	{
					                    			 //System.out.println("mutipart inline  has inline if ");	         
							                    		
								                    	 String[] FileType = part2.getContentType().split("\\s");						                     
								             			InputStream fileData = part2.getInputStream();			   						             			 
								            			 						             			  
								             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
								             			int nRead;
								             			byte[] data = new byte[1024];
								             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
								             				buffer.write(data, 0, nRead);
								             			}
								             			buffer.flush();
								             			byte[] byteArray = buffer.toByteArray();	
								             			String imageStr = Base64.encodeBase64String(byteArray);
								             			long Filesize = part.getSize();
														String fileName = part.getFileName();
														

														//email2.setMultipleAttachment(multipleAttachment);	 
								             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
								             			  // //System.out.println(" inline attributeValue"+attributeValue);
											                Elements paragraphs = document.getElementsByTag("img"); 
											                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
											               // //System.out.println(" part2 contentid"+contentid);
											                for ( org.jsoup.nodes.Element paragraph :paragraphs )
											                {
											                	 String src = paragraph.attr("src").replace("cid:", "");
													             
											                	//  //System.out.println(" part2 src"+src);
													                    if(contentid.equals(src)) 
													                    {
													                    	// //System.out.println("if condition mutipart ");	
													                    	paragraph.attr("src", attributeValue);
													                    	
													                    }
											                }								                  
							                    	}
					                    	 }
		                    			   
		                    			  
		                    		   }
			                    	
		                    	}
                    	 }			                    	
                    	
                    	  email2.setBody(document.toString());
                    }
                }
                
                email2.setBody(document.toString());
            }
//			email2.setBody(document.toString());
			emailList.add(email2);
		}
		if (emailList.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setStatusCode(204));
			jsonObject.put("message", res.setMessage("No Data"));
			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity(emailList.stream()
				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), lst -> {
					Collections.reverse(lst);
					return lst.stream();
				})).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);

	}

	

	@GetMapping("/foldermail/{foldername}/{userId}")
	public ResponseEntity FolderList(@PathVariable("foldername") String  foldername, @PathVariable("userId") Integer id) throws Exception {
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			
			
			Properties properties = new Properties();
			properties.put("mail.store.protocol", mailstoreType);
			properties.put("mail.host", host);
			properties.put("mail.port", port);
			properties.put("mail.smtp.ssl.enable", security);
			Session emailSession = Session.getDefaultInstance(properties);
			Store store = emailSession.getStore(mailstoreType);
			store.connect(host, port, username, OriginalPassword);

			Folder f = store.getDefaultFolder().getFolder(foldername);
				System.out.println(">> " + f.getName());
				EmailFolder emailFolder = new EmailFolder(f);
				  System.out.println("foldername "+emailFolder.getFolderName());
			if(emailFolder == null)
			{
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("Folder id not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}else {
				return fetchFolderEmailList(host, mailstoreType, username, OriginalPassword, port, auth, security , foldername);
			}
			
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	public ResponseEntity fetchFolderEmailList(String hostval, String mailStrProt, String uname, String password, int port,
			boolean auth, boolean security , String  foldername) throws Exception {
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> relativebase64array = new ArrayList<String>();
         try {
        	 
        	 BodyPart messageBodyPart = new MimeBodyPart();
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
     		Folder emailFolder = store.getFolder(foldername);
     		System.out.println("emailFolder" + emailFolder.getFullName());
     		Folder[] f1 = store.getDefaultFolder().list();
//     		for (Folder fd : f1)
//     			System.out.println(">>>>>> before open " + fd.getName());
     	
     		   
     		emailFolder.open(Folder.READ_ONLY);
     		Folder[] f = store.getDefaultFolder().list();
     		for (Folder fd : f)
     			System.out.println(">>>>>> " + fd.getName());
     		// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                       
     		// retrieve the messages from the folder in an array and print it
     		Message[] messages = emailFolder.getMessages();
//     				User user = userService.findByUserName(uname);
     		// //System.out.println("user" + user);
     		emailList = new ArrayList<Email>();

     		//System.out.println(" messages.length" + messages.length);
     		for (int i = 0; i < messages.length; i++) {

     			Message message = messages[i];

     			FetchProfile fp = new FetchProfile();
     			fp.add(UIDFolder.FetchProfileItem.UID);
     			emailFolder.fetch(emailFolder.getMessages(), fp);
     			UIDFolder pf = (UIDFolder) emailFolder;
     			long uid = pf.getUID(message);

     			Address[] a;

     			// FROM
     			if ((a = message.getFrom()) != null) {
     				for (int j = 0; j < a.length; j++)
     					from = a[j].toString();
     			}

     			List<String> toMail = new ArrayList<String>();
     			// TO
     			if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
     				for (int j = 0; j < a.length; j++) {
     					to = a[j].toString();
     					toMail.add(to);
     					toArray = toMail.toArray(new String[toMail.size()]);
     				}
     			} else {
     				toArray = null;
     			}
     			List<String> ccMail = new ArrayList<String>();
     			// CC
     			if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
     				// //System.out.println("ifccArray"+ccArray);
     				for (int j = 0; j < a.length; j++) {
     					cc = a[j].toString();
     					ccMail.add(cc);
     					ccArray = ccMail.toArray(new String[ccMail.size()]);
     				}
     			} else {
     				ccArray = null;
     				// //System.out.println("elseccArray"+ccArray);
     			}

     			List<String> bccMail = new ArrayList<String>();
     			// BCC
     			if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
     				// //System.out.println("ifccArray"+ccArray);
     				for (int j = 0; j < a.length; j++) {
     					bcc = a[j].toString();
     					bccMail.add(bcc);
     					bccArray = bccMail.toArray(new String[bccMail.size()]);
     				}
     			} else {
     				bccArray = null;
     				// //System.out.println("elseccArray"+ccArray);
     			}

     			// SUBJECT
     			if (message.getSubject() != null) {
     				subject = message.getSubject();
     			} else {
     				subject = null;
     			}

     			// writePart(message);

     			String result = "";
     			if (message.isMimeType("text/plain")) {
     				result = message.getContent().toString();
     			} else if (message.isMimeType("multipart/*")) {
     				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
     				// //System.out.println("mimeMultipart" + mimeMultipart.toString());
     				result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

     				result = result.replace("\\", "");
     				// //System.out.println("result " + result);
     			}

     			List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//     			Enumeration headers = messages[i].getAllHeaders();
//     			  while (headers.hasMoreElements()) {
//     				  Header h = (Header) headers.nextElement();
//     				  //System.out.println("dt::"+h.getName());
//     				  //System.out.println(h.getName() + ": " + h.getValue());
//     				  }
     			// //System.out.println(" Email Data null");
     			DateTime dtRec = new DateTime(message.getReceivedDate());

     			email2 = new Email(from, toArray, subject, result, dtRec, ccArray, bccArray, message.getFolder(), uid,
     					message.getFlags(), multipleAttachment);

     			if (message.getFlags().contains(Flag.ANSWERED)) {
     				email2.setRepliedFlag(true);
     			}

     			if (message.getFlags().contains("Forwarded")) {
     				email2.setForwardedFlag(true);
     			}

     			//System.out.println(result.getBytes());
     			Document document = Jsoup.parse(result);
     			String contentType = message.getContentType();
     			if (message.isMimeType("multipart/*")) { 
                     // content may contain attachments
                     Multipart multiPart = (Multipart) message.getContent();
                     int numberOfParts = multiPart.getCount();
                     for (int partCount = 0; partCount < numberOfParts; partCount++) {
                         MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                         if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                             // this part is attachment
                             String fileName = part.getFileName();	     
                                //System.out.println("filename : " + fileName);
                             final File file = new File(fileName);			                       	                        
                              String[] FileType = part.getContentType().split("\\s");	                      
                  			InputStream fileData = part.getInputStream();
                  			
                  			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                  			int nRead;
                  			byte[] data = new byte[1024];
                  			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
                  				buffer.write(data, 0, nRead);
                  			}
                  			buffer.flush();
                  			byte[] byteArray = buffer.toByteArray();	             			
                  			long Filesize =  part.getSize();
                  			MultipleAttachment multi  = new MultipleAttachment();
                  			multi.setFileName(fileName);
                  			multi.setFileType(FileType[0]);	
                  			multi.setFileSize(Filesize);
                  			multi.setFileByte(byteArray);			             		
                  			multipleAttachment.add(multi);
                  			
                  			 email2.setMultipleAttachment(multipleAttachment);
                         }
                         else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
                         {	                    	
                         	//System.out.println("inline one");
                              String[] FileType = part.getContentType().split("\\s");		              
                  			InputStream fileData = part.getInputStream();			  	    	  
                  			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                  			int nRead;
                  			byte[] data = new byte[1024];
                  			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
                  				buffer.write(data, 0, nRead);
                  			}
                  			buffer.flush();
                  			byte[] byteArray = buffer.toByteArray();	
                  			String imageStr = Base64.encodeBase64String(byteArray);			             			
                  			long Filesize =  part.getSize();			  
                  			String fileName = part.getFileName();
     						
//                  			 //System.out.println("FileType[0].toString()" + FileType[0].toString());
//                  			   
//                  			  //System.out.println(" part.getEncoding()" +  part.getEncoding());
                  			  
                  			
                  			 String attributeValue ;
                  			  
                  			  if(FileType[0].toString().contains(";"))
                  			  {
                  				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
                  			  }else {
                  				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
                  			  }
                  			
                  			  
                  			  
                  			 Elements paragraphs = document.getElementsByTag("img"); 
     			                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
     				             
     			                for ( org.jsoup.nodes.Element paragraph :paragraphs )
     			                {
     			                	 String src = paragraph.attr("src").replace("cid:", "");
     					              
     					             
     					                    if(contentid.equals(src)) 
     					                    {
     					                    	
     					                    	paragraph.attr("src", attributeValue);
     					                    	
     					                    }
     			                }								   
                  			
                         }else if(part.isMimeType("multipart/*"))
                         {                    	
                         	 Multipart multiPart1 = (Multipart) part.getContent();
                         	
                         	int numberOfParts1 = multiPart1.getCount();
                         	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
                         	 {
                         		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
                         		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
     		                    	{			                    		
                         			 
                         			    //System.out.println("inline two");
                         			 
     			                    	 String[] FileType = part1.getContentType().split("\\s");				                    
     			             			InputStream fileData = part1.getInputStream();			  
     			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
     			             			int nRead;
     			             			byte[] data = new byte[1024];
     			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
     			             				buffer.write(data, 0, nRead);
     			             			}
     			             			buffer.flush();
     			             			byte[] byteArray = buffer.toByteArray();	
     			             			String imageStr = Base64.encodeBase64String(byteArray);
     			             			long Filesize = part.getSize();
     									String fileName = part.getFileName();
     									

     								//	email2.setMultipleAttachment(multipleAttachment);  			 
     			             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
     			             			 
     			             			
     			             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
     			             			   
     			             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
     			             			  
     						                Elements paragraphs = document.getElementsByTag("img"); 
     						                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
     							             
     						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
     						                {
     						                	 String src = paragraph.attr("src").replace("cid:", "");
     								              
     								             
     								                    if(contentid.equals(src)) 
     								                    {
     								                    	
     								                    	paragraph.attr("src", attributeValue);
     								                    	
     								                    }
     						                }								                  
     		                    	}
                         		 else if(part1.getDisposition() ==  null)
     		                    	{
     		                    		
     		                    		   if(part1.isMimeType("multipart/*")) 
     		                    		   {
     		                    			   Multipart multiPart2 = (Multipart) part1.getContent();
     		                    			  // //System.out.println("multiPart2" + multiPart2);	    
     					                    	int numberOfParts2 = multiPart2.getCount();
     					                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
     					                    	 {
     					                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
     					                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
     					                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
     							                    	{
     					                    			 //System.out.println("mutipart inline  has inline if ");	         
     							                    		
     								                    	 String[] FileType = part2.getContentType().split("\\s");						                     
     								             			InputStream fileData = part2.getInputStream();			   						             			 
     								            			 						             			  
     								             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
     								             			int nRead;
     								             			byte[] data = new byte[1024];
     								             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
     								             				buffer.write(data, 0, nRead);
     								             			}
     								             			buffer.flush();
     								             			byte[] byteArray = buffer.toByteArray();	
     								             			String imageStr = Base64.encodeBase64String(byteArray);
     								             			long Filesize = part.getSize();
     														String fileName = part.getFileName();
     														
     								             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
     								             			  // //System.out.println(" inline attributeValue"+attributeValue);
     											                Elements paragraphs = document.getElementsByTag("img"); 
     											                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
     											               // //System.out.println(" part2 contentid"+contentid);
     											                for ( org.jsoup.nodes.Element paragraph :paragraphs )
     											                {
     											                	 String src = paragraph.attr("src").replace("cid:", "");
     													             
     											                	//  //System.out.println(" part2 src"+src);
     													                    if(contentid.equals(src)) 
     													                    {
     													                    	// //System.out.println("if condition mutipart ");	
     													                    	paragraph.attr("src", attributeValue);
     													                    	
     													                    }
     											                }								                  
     							                    	}
     					                    	 }
     		                    			   
     		                    			  
     		                    		   }
     			                    	
     		                    	}
                         	 }			                    	
                         	
                         	  email2.setBody(document.toString());
                         }
                     }
                     
                     email2.setBody(document.toString());
                 }

//     			email2.setBody(document.toString());
     			emailList.add(email2);

     		}
     		if (emailList.isEmpty()) {
     			ObjectNode jsonObject = objectMapper.createObjectNode();
     			jsonObject.put("errorCode", res.setStatusCode(204));
     			jsonObject.put("message", res.setMessage("No Data"));
     			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
     		}

     		return new ResponseEntity(emailList.stream()
     				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), lst -> {
     					Collections.reverse(lst);
     					return lst.stream();
     				})).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);
     	
         }catch (Exception e) {
        	    System.out.println("e"+e);
        	 ObjectNode jsonObject = objectMapper.createObjectNode();
  			jsonObject.put("errorCode", res.setStatusCode(204));
  			jsonObject.put("message", res.setMessage("somthing went wrong"));
  			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
		

	}
	
	@GetMapping("/subfoldermail/{sufoldername}/{userId}")
	public ResponseEntity SubfloderFolderMailList(@PathVariable("sufoldername") String sufoldername  , @PathVariable("userId") Integer id) throws Exception {
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
			//System.out.println(OriginalPassword);
			// RETRIVE ENCRYPTED PASSWORD

			String host = user.get().getEmailConfiguration().getIncomingHost();
			String mailstoreType = user.get().getEmailConfiguration().getIncomingProtocol();
			int port = user.get().getEmailConfiguration().getIncomingPort();
			boolean auth = user.get().getEmailConfiguration().isAuthentication();
			boolean security = user.get().getEmailConfiguration().isSecurity();
			
			
			Properties properties = new Properties();
			properties.put("mail.store.protocol", mailstoreType);
			properties.put("mail.host", host);
			properties.put("mail.port", port);
			properties.put("mail.smtp.ssl.enable", security);
			Session emailSession = Session.getDefaultInstance(properties);
			Store store = emailSession.getStore(mailstoreType);
			store.connect(host, port, username, OriginalPassword);

			Folder f = store.getDefaultFolder().getFolder(sufoldername);
				System.out.println(">> " + f.getName());
				EmailFolder emailFolder = new EmailFolder(f);
				  System.out.println("foldername "+emailFolder.getFolderName());
			if(emailFolder == null)
			{
				ObjectNode jsonObject = objectMapper.createObjectNode();
				jsonObject.put("errorCode", res.setErrorCode(404));
				jsonObject.put("message", res.setMessage("Folder id not found"));
				return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
			}else {
				return fetchSubFolderEmailList(host, mailstoreType, username, OriginalPassword, port, auth, security , sufoldername);
			}
			
		} else {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("errorCode", res.setErrorCode(404));
			jsonObject.put("message", res.setMessage("User id not found"));
			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

	private ResponseEntity fetchSubFolderEmailList(String host, String mailStrProt, String uname,
			String password, int port, boolean auth, boolean security, String  sufoldername) {
		ArrayList<String> base64array = new ArrayList<String>();
		ArrayList<String> relativebase64array = new ArrayList<String>();
         try {
        	 
        	 BodyPart messageBodyPart = new MimeBodyPart();
     		// create properties field
     		Properties properties = new Properties();
     		properties.put("mail.store.protocol", mailStrProt);
     		properties.put("mail.host", host);
     		properties.put("mail.port", port);
     		properties.put("mail.smtp.ssl.enable", security);
     		Session emailSession = Session.getDefaultInstance(properties);
     		// emailSession.setDebug(true);
     		Store store = emailSession.getStore(mailStrProt);

     		store.connect(host, port, uname, password);
     		// create the folder object and open it
     		
     		
     		
//     	   
//			   String[] folderparts = subFolder.getFolder().getFolderName().split("/");
//			String subfolderparts = subFolder.getSubFolderName();
//			Folder f = store.getDefaultFolder();
//			Folder sf = store.getDefaultFolder();
			// Open destination folder
//			for (int i = 0; i < folderparts.length; i++) {
//				f = f.getFolder(folderparts[i]);
////				f.setSubscribed(subFolder.getFolder().getStatus());
//				if (f.exists()& f.isSubscribed()) {
//					sf = sf.getFolder(folderparts[i] + "." + subfolderparts);
////					sf.setSubscribed(subFolder.getStatus());
//					System.out.println(subFolder.getStatus()+"subFolder.getStatus()");
//				
//					if (sf.exists()& sf.isSubscribed()) {
//						
//						System.out.println("f folder name " + f.getFullName());
//						   System.out.println("sf folder name " + sf.getFullName());
//						   
//						  
//			     		    
//			     		  
//						
//					} else {
//						ObjectNode jsonObject = objectMapper.createObjectNode();
//						jsonObject.put("errorCode", res.setErrorCode(404));
//						jsonObject.put("message", res.setMessage("SubFolder Not Found"));
//						return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
//					}
//				} else {
//					ObjectNode jsonObject = objectMapper.createObjectNode();
//					jsonObject.put("errorCode", res.setErrorCode(404));
//					jsonObject.put("message", res.setMessage("Folder Not Found"));
//					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
//				}
//			}
			
			
     		Folder emailFolder = store.getFolder(sufoldername);
     		emailFolder.open(Folder.READ_ONLY);
     		// retrieve the messages from the folder in an array and print it
     		Message[] messages = emailFolder.getMessages();
//     				User user = userService.findByUserName(uname);
     		// //System.out.println("user" + user);
     		emailList = new ArrayList<Email>();

     		//System.out.println(" messages.length" + messages.length);
     		for (int i = 0; i < messages.length; i++) {

     			Message message = messages[i];

     			FetchProfile fp = new FetchProfile();
     			fp.add(UIDFolder.FetchProfileItem.UID);
     			emailFolder.fetch(emailFolder.getMessages(), fp);
     			UIDFolder pf = (UIDFolder) emailFolder;
     			long uid = pf.getUID(message);

     			Address[] a;

     			// FROM
     			if ((a = message.getFrom()) != null) {
     				for (int j = 0; j < a.length; j++)
     					from = a[j].toString();
     			}

     			List<String> toMail = new ArrayList<String>();
     			// TO
     			if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
     				for (int j = 0; j < a.length; j++) {
     					to = a[j].toString();
     					toMail.add(to);
     					toArray = toMail.toArray(new String[toMail.size()]);
     				}
     			} else {
     				toArray = null;
     			}
     			List<String> ccMail = new ArrayList<String>();
     			// CC
     			if ((a = message.getRecipients(Message.RecipientType.CC)) != null) {
     				// //System.out.println("ifccArray"+ccArray);
     				for (int j = 0; j < a.length; j++) {
     					cc = a[j].toString();
     					ccMail.add(cc);
     					ccArray = ccMail.toArray(new String[ccMail.size()]);
     				}
     			} else {
     				ccArray = null;
     				// //System.out.println("elseccArray"+ccArray);
     			}

     			List<String> bccMail = new ArrayList<String>();
     			// BCC
     			if ((a = message.getRecipients(Message.RecipientType.BCC)) != null) {
     				// //System.out.println("ifccArray"+ccArray);
     				for (int j = 0; j < a.length; j++) {
     					bcc = a[j].toString();
     					bccMail.add(bcc);
     					bccArray = bccMail.toArray(new String[bccMail.size()]);
     				}
     			} else {
     				bccArray = null;
     				// //System.out.println("elseccArray"+ccArray);
     			}

     			// SUBJECT
     			if (message.getSubject() != null) {
     				subject = message.getSubject();
     			} else {
     				subject = null;
     			}

     			// writePart(message);

     			String result = "";
     			if (message.isMimeType("text/plain")) {
     				result = message.getContent().toString();
     			} else if (message.isMimeType("multipart/*")) {
     				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
     				// //System.out.println("mimeMultipart" + mimeMultipart.toString());
     				result = GetTextFromMimeMultipart.getTextFromMimeMultipart(mimeMultipart);

     				result = result.replace("\\", "");
     				// //System.out.println("result " + result);
     			}

     			List<MultipleAttachment> multipleAttachment = new ArrayList<MultipleAttachment>();
//     			Enumeration headers = messages[i].getAllHeaders();
//     			  while (headers.hasMoreElements()) {
//     				  Header h = (Header) headers.nextElement();
//     				  //System.out.println("dt::"+h.getName());
//     				  //System.out.println(h.getName() + ": " + h.getValue());
//     				  }
     			// //System.out.println(" Email Data null");
     			DateTime dtRec = new DateTime(message.getReceivedDate());

     			email2 = new Email(from, toArray, subject, result, dtRec, ccArray, bccArray, message.getFolder(), uid,
     					message.getFlags(), multipleAttachment);

     			if (message.getFlags().contains(Flag.ANSWERED)) {
     				email2.setRepliedFlag(true);
     			}

     			if (message.getFlags().contains("Forwarded")) {
     				email2.setForwardedFlag(true);
     			}

     			//System.out.println(result.getBytes());
     			Document document = Jsoup.parse(result);
     			String contentType = message.getContentType();
     			if (message.isMimeType("multipart/*")) { 
                     // content may contain attachments
                     Multipart multiPart = (Multipart) message.getContent();
                     int numberOfParts = multiPart.getCount();
                     for (int partCount = 0; partCount < numberOfParts; partCount++) {
                         MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                         if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                             // this part is attachment
                             String fileName = part.getFileName();	     
                                //System.out.println("filename : " + fileName);
                             final File file = new File(fileName);			                       	                        
                              String[] FileType = part.getContentType().split("\\s");	                      
                  			InputStream fileData = part.getInputStream();
                  			
                  			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                  			int nRead;
                  			byte[] data = new byte[1024];
                  			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
                  				buffer.write(data, 0, nRead);
                  			}
                  			buffer.flush();
                  			byte[] byteArray = buffer.toByteArray();	             			
                  			long Filesize =  part.getSize();
                  			MultipleAttachment multi  = new MultipleAttachment();
                  			multi.setFileName(fileName);
                  			multi.setFileType(FileType[0]);	
                  			multi.setFileSize(Filesize);
                  			multi.setFileByte(byteArray);			             		
                  			multipleAttachment.add(multi);
                  			
                  			 email2.setMultipleAttachment(multipleAttachment);
                         }
                         else if (Part.INLINE.equalsIgnoreCase(part.getDisposition())) 
                         {	                    	
                         	//System.out.println("inline one");
                              String[] FileType = part.getContentType().split("\\s");		              
                  			InputStream fileData = part.getInputStream();			  	    	  
                  			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                  			int nRead;
                  			byte[] data = new byte[1024];
                  			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
                  				buffer.write(data, 0, nRead);
                  			}
                  			buffer.flush();
                  			byte[] byteArray = buffer.toByteArray();	
                  			String imageStr = Base64.encodeBase64String(byteArray);			             			
                  			long Filesize =  part.getSize();			  
                  			String fileName = part.getFileName();
     						
//                  			 //System.out.println("FileType[0].toString()" + FileType[0].toString());
//                  			   
//                  			  //System.out.println(" part.getEncoding()" +  part.getEncoding());
                  			  
                  			
                  			 String attributeValue ;
                  			  
                  			  if(FileType[0].toString().contains(";"))
                  			  {
                  				  attributeValue ="data:"+ FileType[0].toString()+ part.getEncoding()+ ","+imageStr;
                  			  }else {
                  				  attributeValue ="data:"+ FileType[0].toString()+";"+ part.getEncoding()+ ","+imageStr;
                  			  }
                  			
                  			  
                  			  
                  			 Elements paragraphs = document.getElementsByTag("img"); 
     			                String  contentid =   part.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
     				             
     			                for ( org.jsoup.nodes.Element paragraph :paragraphs )
     			                {
     			                	 String src = paragraph.attr("src").replace("cid:", "");
     					              
     					             
     					                    if(contentid.equals(src)) 
     					                    {
     					                    	
     					                    	paragraph.attr("src", attributeValue);
     					                    	
     					                    }
     			                }								   
                  			
                         }else if(part.isMimeType("multipart/*"))
                         {                    	
                         	 Multipart multiPart1 = (Multipart) part.getContent();
                         	
                         	int numberOfParts1 = multiPart1.getCount();
                         	 for (int partCount1= 0; partCount1 < numberOfParts1; partCount1++) 
                         	 {
                         		 MimeBodyPart part1 = (MimeBodyPart) multiPart1.getBodyPart(partCount1);
                         		 if(Part.INLINE.equalsIgnoreCase(part1.getDisposition()))
     		                    	{			                    		
                         			 
                         			    //System.out.println("inline two");
                         			 
     			                    	 String[] FileType = part1.getContentType().split("\\s");				                    
     			             			InputStream fileData = part1.getInputStream();			  
     			             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
     			             			int nRead;
     			             			byte[] data = new byte[1024];
     			             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
     			             				buffer.write(data, 0, nRead);
     			             			}
     			             			buffer.flush();
     			             			byte[] byteArray = buffer.toByteArray();	
     			             			String imageStr = Base64.encodeBase64String(byteArray);
     			             			long Filesize = part.getSize();
     									String fileName = part.getFileName();
     									

     								//	email2.setMultipleAttachment(multipleAttachment);  			 
     			             			 String attributeValue ="data:"+ FileType[0].toString()+ part1.getEncoding()+ ","+imageStr;
     			             			 
     			             			
     			             			//System.out.println("FileType[0].toString()" + FileType[0].toString());
     			             			   
     			             			  //System.out.println(" part.getEncoding()" +  part1.getEncoding());
     			             			  
     						                Elements paragraphs = document.getElementsByTag("img"); 
     						                String  contentid =   part1.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
     							             
     						                for ( org.jsoup.nodes.Element paragraph :paragraphs )
     						                {
     						                	 String src = paragraph.attr("src").replace("cid:", "");
     								              
     								             
     								                    if(contentid.equals(src)) 
     								                    {
     								                    	
     								                    	paragraph.attr("src", attributeValue);
     								                    	
     								                    }
     						                }								                  
     		                    	}
                         		 else if(part1.getDisposition() ==  null)
     		                    	{
     		                    		
     		                    		   if(part1.isMimeType("multipart/*")) 
     		                    		   {
     		                    			   Multipart multiPart2 = (Multipart) part1.getContent();
     		                    			  // //System.out.println("multiPart2" + multiPart2);	    
     					                    	int numberOfParts2 = multiPart2.getCount();
     					                    	 for (int partCount2= 0; partCount2 < numberOfParts2; partCount2++) 
     					                    	 {
     					                    		 MimeBodyPart part2 = (MimeBodyPart) multiPart2.getBodyPart(partCount2);
     					                    		 //System.out.println("part2.getDisposition()"+part2.getDisposition());
     					                    		 if(Part.INLINE.equalsIgnoreCase(part2.getDisposition()))
     							                    	{
     					                    			 //System.out.println("mutipart inline  has inline if ");	         
     							                    		
     								                    	 String[] FileType = part2.getContentType().split("\\s");						                     
     								             			InputStream fileData = part2.getInputStream();			   						             			 
     								            			 						             			  
     								             			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
     								             			int nRead;
     								             			byte[] data = new byte[1024];
     								             			while ((nRead = fileData.read(data, 0, data.length)) != -1) {
     								             				buffer.write(data, 0, nRead);
     								             			}
     								             			buffer.flush();
     								             			byte[] byteArray = buffer.toByteArray();	
     								             			String imageStr = Base64.encodeBase64String(byteArray);
     								             			long Filesize = part.getSize();
     														String fileName = part.getFileName();
     														
     								             			 String attributeValue ="data:"+ FileType[0].toString()+";"+ part2.getEncoding()+ ","+imageStr;
     								             			  // //System.out.println(" inline attributeValue"+attributeValue);
     											                Elements paragraphs = document.getElementsByTag("img"); 
     											                String  contentid =   part2.getContentID().replaceAll("\\s+|&|'|\\(|\\)|<|>|#", "");
     											               // //System.out.println(" part2 contentid"+contentid);
     											                for ( org.jsoup.nodes.Element paragraph :paragraphs )
     											                {
     											                	 String src = paragraph.attr("src").replace("cid:", "");
     													             
     											                	//  //System.out.println(" part2 src"+src);
     													                    if(contentid.equals(src)) 
     													                    {
     													                    	// //System.out.println("if condition mutipart ");	
     													                    	paragraph.attr("src", attributeValue);
     													                    	
     													                    }
     											                }								                  
     							                    	}
     					                    	 }
     		                    			   
     		                    			  
     		                    		   }
     			                    	
     		                    	}
                         	 }			                    	
                         	
                         	  email2.setBody(document.toString());
                         }
                     }
                     
                     email2.setBody(document.toString());
                 }

//     			email2.setBody(document.toString());
     			emailList.add(email2);

     		}
     		if (emailList.isEmpty()) {
     			ObjectNode jsonObject = objectMapper.createObjectNode();
     			jsonObject.put("errorCode", res.setStatusCode(204));
     			jsonObject.put("message", res.setMessage("No Data"));
     			return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
     		}

     		return new ResponseEntity(emailList.stream()
     				.collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), lst -> {
     					Collections.reverse(lst);
     					return lst.stream();
     				})).collect(Collectors.toCollection(ArrayList::new)), HttpStatus.OK);
     	
         }catch (Exception e) {
        	    System.out.println("e"+e);
        	 ObjectNode jsonObject = objectMapper.createObjectNode();
  			jsonObject.put("errorCode", res.setStatusCode(204));
  			jsonObject.put("message", res.setMessage("somthing went wrong"));
  			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		}
	}

}
