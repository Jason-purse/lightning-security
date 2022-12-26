package com.generatera.authorization.oauth2.repository;

import com.generatera.authorization.oauth2.entity.OAuth2Client;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OAuth2ClientRepository extends CrudRepository<OAuth2Client, Long> {

	List<OAuth2Client> findByRegisteredFalse();
	
}
