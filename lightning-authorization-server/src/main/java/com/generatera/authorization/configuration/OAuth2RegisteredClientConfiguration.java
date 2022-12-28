//package com.generatera.authorization.configuration;
//
//import com.generatera.authorization.oauth2.service.OAuth2RegisteredClientService;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
//
//import java.util.List;
//
///**
// * 自动导入配置的client3.0没效果还需要找问题(2.7是可以的)
// * @author weir
// *
// */
//@Configuration
//@PropertySource("classpath:oauth2-registered-client.properties")
//public class OAuth2RegisteredClientConfiguration {
//
//	private static final Logger LOGGER = LogManager.getLogger();
////
////	private final OAuth2RegisteredClientService oauth2RegisteredClientService;
////
////	public OAuth2RegisteredClientConfiguration(OAuth2RegisteredClientService oauth2RegisteredClientService) {
////		this.oauth2RegisteredClientService = oauth2RegisteredClientService;
////	}
////
////	@Bean
////	public RegisteredClientRepository registeredClientRepository(OAuth2RegisteredClientRepository oauth2RegisteredClientRepository) {
////
////		RegisteredClientRepository registeredClientRepository = new JpaRegisteredClientRepository(oauth2RegisteredClientRepository);
////
////		LOGGER.debug("in registeredClientRepository");
////
////		List<RegisteredClient> registeredClients = oauth2RegisteredClientService.getOAuth2RegisteredClient();
////		registeredClients.forEach(registeredClient -> {
////			registeredClientRepository.save(registeredClient);
////		});
////
////		return registeredClientRepository;
////	}
//
//	@Autowired
//	private OAuth2RegisteredClientService oauth2RegisteredClientService;
//
//	@Bean
//	public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
//
//		LOGGER.debug("in registeredClientRepository");
//
//		List<RegisteredClient> registeredClients = oauth2RegisteredClientService.getOAuth2RegisteredClient();
//
//		JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
//		registeredClients.forEach(registeredClientRepository::save);
//
//		return registeredClientRepository;
//	}
//
//}
