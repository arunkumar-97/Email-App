package com.jesperapps.email.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jesperapps.email.api.model.ListTypeValues;
import com.jesperapps.email.api.model.ListTypes;


public interface ListTypeValuesRepository extends JpaRepository<ListTypeValues, Integer>{

	List<ListTypeValues> findAllByListTypes(ListTypes listTypes);

	List<ListTypeValues> findAllByListTypeValueId(Integer listTypeValueId);

}
