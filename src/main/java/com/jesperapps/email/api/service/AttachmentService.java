package com.jesperapps.email.api.service;

import com.jesperapps.email.api.model.Attachment;
import com.jesperapps.email.api.model.User;

public interface AttachmentService {

	Attachment findByUser(User user);

	Attachment save(Attachment attachment);

	Attachment delete(Attachment attachment);



	void deleteAtt(Attachment attachment);

	



}
