package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Iterator;

public class DelegateClientRegistrationRepository implements LightningOAuth2ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final ClientRegistrationRepository registrationRepository;
    private final Iterable<ClientRegistration> iterable;

    @SuppressWarnings("unchecked")
    public DelegateClientRegistrationRepository(ClientRegistrationRepository registrationRepository) {
        Assert.notNull(registrationRepository, "registrationRepository must not be null !!!");
        this.registrationRepository = registrationRepository;
        if (registrationRepository instanceof Iterable<?>) {
            this.iterable = (Iterable<ClientRegistration>) registrationRepository;
        }
        else {
            this.iterable = null;
        }
    }

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        if (this.iterable != null) {
            return this.iterable.iterator();
        }
        return Collections.emptyIterator();
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return registrationRepository.findByRegistrationId(registrationId);
    }

}
