package com.jesperapps.email.api.service;

import java.util.List;
import java.util.Optional;

import com.jesperapps.email.api.model.Signature;
import com.jesperapps.email.api.model.User;


public interface SignatureService {


	Signature save(Signature signature);

	Optional<Signature> findById(Integer id);


	List<Signature> listSignature();

	Signature deleteSignature(Integer id);

	List<Signature> findAllByUser(User user);

	List<Signature> findAllByUserAndStatus(User user, Integer i);

	Signature findByUser(User user);

	void delete(Signature sign);

	List<Signature> findAllByUserAndName(User user, String name);



}
