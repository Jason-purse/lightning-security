package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.TokenIssueFormat;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.Jwks;
import com.generatera.security.authorization.server.specification.util.RsaKeyConversionUtils;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author FLJ
 * @date 2023/1/6
 * @time 14:15
 * @Description JWKSource 提供器
 */
public class JWKSourceProvider {

    private final JWKSource<SecurityContext> source;

    private final TokenIssueFormat tokenIssueFormat;

    protected JWKSourceProvider() {
        this.source = Jwks.defaultRsaRandomJwkSource();
        this.tokenIssueFormat = TokenIssueFormat.SELF_CONTAINED;
    }

    protected JWKSourceProvider(JWKSource<SecurityContext> source,TokenIssueFormat tokenIssueFormat) {
        this.source = source;
        this.tokenIssueFormat = tokenIssueFormat;
    }

    @NotNull
    public  JWKSource<SecurityContext> getJWKSource() {
        return source;
    }

    @NotNull
    public TokenIssueFormat getTokenIssueFormat() {
        return tokenIssueFormat;
    }


    public static JWKSourceProvider rsaJWKSourceProvider() {
        return new JWKSourceProvider();
    }

    public static JWKSourceProvider customRsaJWKSourceProvider(String rsaPublicKey, String rsaPrivateKey) {
        return new JWKSourceProvider(
                Jwks.customRsaJwkSource(RsaKeyConversionUtils.convertRsaPublicKey(rsaPublicKey),RsaKeyConversionUtils.convertRsaPrivateKey(rsaPrivateKey)),
                TokenIssueFormat.REFERENCE
        );
    }

    public static JWKSourceProvider customSecretJwkSourceProvider(String secret,String algorithm) {
        return new JWKSourceProvider(
                Jwks.customSecretJwkSource(secret,algorithm),
                TokenIssueFormat.REFERENCE
        );
    }

    public static JWKSourceProvider secretJWKSourceProvider() {
        return new JWKSourceProvider(Jwks.defaultSecretRandomJwkSource(),TokenIssueFormat.SELF_CONTAINED);
    }

    public static JWKSourceProvider ecJWKSourceProvider() {
        return new JWKSourceProvider(Jwks.defaultEcRandomJwkSource(),TokenIssueFormat.SELF_CONTAINED);
    }


    public static JWKSourceProvider of(JWKSource<SecurityContext> jwkSource,TokenIssueFormat tokenIssueFormat) {
        return new JWKSourceProvider(jwkSource,tokenIssueFormat);
    }

}
