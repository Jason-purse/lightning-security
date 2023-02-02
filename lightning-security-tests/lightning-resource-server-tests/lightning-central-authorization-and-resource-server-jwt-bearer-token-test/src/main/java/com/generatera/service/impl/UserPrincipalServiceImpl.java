package com.generatera.service.impl;

import com.generatera.entity.LightningOAuth2UserDetails;
import com.generatera.entity.OAuth2UserEntity;
import com.generatera.repository.OAuth2UserRepository;
import com.generatera.repository.UserPrincipalRepository;
import com.generatera.service.UserPrincipalService;
import com.jianyue.lightning.boot.starter.util.BeanUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Services are the next layer above repositories. Services encapsulate the business logic of the application and consume other
 * services and repositories but do not consume resources in higher application layers like controllers (again, something that
 * Spring does not actively enforce). Services are marked with the @Service annotation, making them eligible for automatic
 * instantiation and dependency injection, among other benefits. Like repositories, services are created from interfaces so that
 * dependent resources may program against an interface. This pattern of basing each layer on a set of interfaces allows each layer
 * to be tested in isolation from all the other layers. From a transactional point of view, the execution of a service method from a
 * higher layer (such as a controller) can be thought of as a transactional unit of work. It may perform several operations on
 * multiple repositories and other services in the context that all operations must either succeed or fail as a single unit. When a
 * service method is executed from another service method, it is generally thought of as belonging to the same unit of work that the
 * calling method belongs to.
 *
 * It should be noted that this concept of unit of work does not imply that it can always be handled with traditional relational
 * database transactions. The operations performed during a unit of work may have multiple consequences across different data stores
 * or file media. These operations may include transmission of intra- or inter-application messages, e-mails, text messages, or
 * mobile notifications that in most cases cannot be rolled back.
 *
 * Some developers do not like to use the term �service� to describe this layer of the application, as that can sometimes be confused
 * with web services. What you call your business logic layer doesn�t matter. You don�t even have to use the @Service annotation.
 * You could instead use the @Component annotation or a custom annotation meta-annotated with @Component. What you call it and how
 * you mark it does not change its purpose.
 *
 * @author basit.ahmed
 *
 */
@Service
public class UserPrincipalServiceImpl implements UserPrincipalService {

    private final UserPrincipalRepository userPrincipalRepository;

	private final OAuth2UserRepository oAuth2UserRepository;

    /**
	 * The constructor is annotated with @Autowired, meaning that it injects the UserPrincipalRepository<UserPrincipal>
	 * implementation. This annotation can be omitted; Spring automatically injects any declared dependency since version 4.3.
	 */
	public UserPrincipalServiceImpl(UserPrincipalRepository userPrincipalRepository, OAuth2UserRepository oAuth2UserRepository) {
		this.userPrincipalRepository = userPrincipalRepository;
		this.oAuth2UserRepository = oAuth2UserRepository;
	}

	/**
   	 * Notice that the implementation lacks any validation related annotations. This is the way it should be because the interface
   	 * contains that contract.
   	 *
   	 * The supports method indicates that this AuthenticationProvider can authenticate using only
   	 * UsernamePasswordAuthenticationTokens. After casting the Authentication to a UsernamePasswordAuthenticationToken and
   	 * retrieving the username and password, authenticate erases the plain-text password stored in the token so that it can�t
   	 * accidentally leak anywhere. It then retrieves the UserPrincipal and runs through the standard checks it previously ran
   	 * through. After the user identity has been confirmed, it sets the authenticated flag to true (in bold) to confirm the
   	 * authentication succeeded.
   	 */
    @Override
    @Transactional
    public LightningOAuth2UserDetails loadUserByUsername(String username) {

//    	UserPrincipal principal = userPrincipalRepository.getByUsername(username);
		Optional<OAuth2UserEntity> optional = oAuth2UserRepository.findByUsernameAndDeletedIsFalse(username);
		// make sure the authorities and password are loaded
		return optional.map(
				entity -> {
					LightningOAuth2UserDetails lightningOAuth2UserDetails = BeanUtils.transformFrom(entity, LightningOAuth2UserDetails.class);
					assert lightningOAuth2UserDetails != null;
					return lightningOAuth2UserDetails;
				}
		).orElseThrow(() -> new BadCredentialsException("密码错误或者用户名不对"));
    }

}
