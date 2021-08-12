package com.jesperapps.email.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jesperapps.email.api.model.Signature;
import com.jesperapps.email.api.model.User;
@Repository 
public interface  SignatureRepository extends JpaRepository<Signature, Integer> 
{

	List<Signature> findAllByUserAndStatus(User user, Integer i);

	List<Signature> findAllByUser(User user);

	Signature findByUser(User user);

	List<Signature> findAllByUserAndName(User user, String name);


}
