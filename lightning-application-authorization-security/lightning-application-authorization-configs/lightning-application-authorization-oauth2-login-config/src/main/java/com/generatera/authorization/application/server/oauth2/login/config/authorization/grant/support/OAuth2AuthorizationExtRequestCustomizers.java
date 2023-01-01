package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class OAuth2AuthorizationExtRequestCustomizers {
    private static final StringKeyGenerator DEFAULT_SECURE_KEY_GENERATOR = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

    private OAuth2AuthorizationExtRequestCustomizers() {
    }

    public static Consumer<OAuth2AuthorizationExtRequest.Builder> withPkce() {
        return OAuth2AuthorizationExtRequestCustomizers::applyPkce;
    }

    private static void applyPkce(OAuth2AuthorizationExtRequest.Builder builder) {
        if (!isPkceAlreadyApplied(builder)) {
            String codeVerifier = DEFAULT_SECURE_KEY_GENERATOR.generateKey();
            builder.attributes((attrs) -> {
                attrs.put("code_verifier", codeVerifier);
            });
            builder.additionalParameters((params) -> {
                try {
                    String codeChallenge = createHash(codeVerifier);
                    params.put("code_challenge", codeChallenge);
                    params.put("code_challenge_method", "S256");
                } catch (NoSuchAlgorithmException var3) {
                    params.put("code_challenge", codeVerifier);
                }

            });
        }
    }

    private static boolean isPkceAlreadyApplied(OAuth2AuthorizationExtRequest.Builder builder) {
        AtomicBoolean pkceApplied = new AtomicBoolean(false);
        builder.additionalParameters((params) -> {
            if (params.containsKey("code_challenge")) {
                pkceApplied.set(true);
            }

        });
        return pkceApplied.get();
    }

    private static String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}