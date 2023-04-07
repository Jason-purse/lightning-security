package com.generatera.security.authorization.server.specification.components.token.format.jwt.jose;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

public final class Jwks {

	private Jwks() {
	}

	/**
	 * 随机生成一个  rsa ...JWKSource
	 */
	public static JWKSource<SecurityContext> defaultRsaRandomJwkSource() {
		RSAKey rsaKey = Jwks.generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	public static JWKSource<SecurityContext> customRsaJwkSource(
			String keyId,
			RSAPublicKey publicKey,RSAPrivateKey rsaPrivateKey
	) {
		JWKSet jwkSet = new JWKSet(forRsa(keyId,publicKey, rsaPrivateKey));
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	public static JWKSource<SecurityContext> defaultSecretRandomJwkSource() {
		OctetSequenceKey octetSequenceKey = Jwks.generateSecret();
		JWKSet jwkSet = new JWKSet(octetSequenceKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	public static JWKSource<SecurityContext> customSecretJwkSource(String secret,String algorithm) {
		JWKSet jwkSet = new JWKSet(forSecret(secret,algorithm));
		return ((jwkSelector, securityContext) -> jwkSelector.select(jwkSet));
	}

	public static JWKSource<SecurityContext> defaultEcRandomJwkSource() {
		ECKey ecKey = Jwks.generateEc();
		JWKSet jwkSet = new JWKSet(ecKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}


	public static RSAKey generateRsa() {
		KeyPair keyPair = KeyGeneratorUtils.generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		
		return new RSAKey.Builder(publicKey)
			.privateKey(privateKey)
			.keyID(UUID.randomUUID().toString())
			.build();
	}

	public static RSAKey forRsa(String keyId,RSAPublicKey publicKey,RSAPrivateKey privateKey) {
		return new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(keyId)
				.algorithm(JWSAlgorithm.RS256)
				.build();
	}

	public static ECKey generateEc() {
		KeyPair keyPair = KeyGeneratorUtils.generateEcKey();
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
		Curve curve = Curve.forECParameterSpec(publicKey.getParams());
		
		return new ECKey.Builder(curve, publicKey)
			.privateKey(privateKey)
			.keyID(UUID.randomUUID().toString())
			.build();
	}

	public static OctetSequenceKey generateSecret() {
		
		SecretKey secretKey = KeyGeneratorUtils.generateSecretKey();
		return new OctetSequenceKey.Builder(secretKey)
			.keyID(UUID.randomUUID().toString())
			.build();
	}

	public static OctetSequenceKey forSecret(String secret,String algorithm) {
		return new OctetSequenceKey.Builder(forSecretKey(secret,algorithm))
				.build();
	}

	public static SecretKey forSecretKey(String secret,String algorithm) {
		return new SecretKeySpec(secret.getBytes(),algorithm);
	}
	
}
