package com.jesperapps.email.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jesperapps.email.api.model.Signature;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.repository.SignatureRepository;

@Service
public class SignatureServiceImpl implements SignatureService {
	@Autowired
	private SignatureRepository signatureRepository;


	@Override
	public Signature deleteSignature(Integer id) {

		Optional<Signature> signOptional = signatureRepository.findById(id);
		if (signOptional.isPresent()) {
			Signature dbSignature = signOptional.get();
			dbSignature.setStatus(4);// deleted
			return signatureRepository.save(dbSignature);
		} else {
			return null;
		}
	}

	@Override
	public List<Signature> findAllByUserAndStatus(User user, Integer i) {
		// TODO Auto-generated method stub
		return signatureRepository.findAllByUserAndStatus(user, i);
	}


	@Override
	public Signature save(Signature signature) {
		// TODO Auto-generated method stub
		return signatureRepository.save(signature);
	}


	@Override
	public Optional<Signature> findById(Integer id) {
		// TODO Auto-generated method stub
		return signatureRepository.findById(id);
	}


	@Override
	public List<Signature> findAllByUser(User user) {
		// TODO Auto-generated method stub
		return signatureRepository.findAllByUser(user);
	}


	@Override
	public List<Signature> listSignature() {
		// TODO Auto-generated method stub
		return signatureRepository.findAll();
	}

	@Override
	public Signature findByUser(User user) {
		// TODO Auto-generated method stub
		return signatureRepository.findByUser(user);
	}

	@Override
	public void delete(Signature sign) {
		signatureRepository.delete(sign);
	}

	@Override
	public List<Signature> findAllByUserAndName(User user, String name) {
		// TODO Auto-generated method stub
		return signatureRepository.findAllByUserAndName(user, name);
	}




}
