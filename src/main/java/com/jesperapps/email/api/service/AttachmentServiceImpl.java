package com.jesperapps.email.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jesperapps.email.api.model.Attachment;
import com.jesperapps.email.api.model.Signature;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.repository.AttachmentRepository;

@Service
public class AttachmentServiceImpl implements AttachmentService{

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Override
	public Attachment findByUser(User user) {
		// TODO Auto-generated method stub
		return attachmentRepository.findByUser(user);
	}

	@Override
	public Attachment save(Attachment attachment) {
		// TODO Auto-generated method stub
		return attachmentRepository.save(attachment);
	}

	@Override
	public Attachment delete(Attachment attachment) {
		Optional<Attachment> attOptional = attachmentRepository.findById(attachment.getId());
		if (attOptional.isPresent()) {
			Attachment dbAttachment = attOptional.get();
			dbAttachment.setStatus(4);// deleted
			return attachmentRepository.save(dbAttachment);
		} else {
			return null;
		}
	}

	

	@Override
	public void deleteAtt(Attachment attachment) {
		attachmentRepository.delete(attachment);
		
	}

	
}
