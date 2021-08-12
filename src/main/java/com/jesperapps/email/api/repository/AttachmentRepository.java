package com.jesperapps.email.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jesperapps.email.api.model.Attachment;
import com.jesperapps.email.api.model.User;

@Repository 
public interface AttachmentRepository extends JpaRepository<Attachment, Integer>{

	Attachment findByUser(User user);



}
