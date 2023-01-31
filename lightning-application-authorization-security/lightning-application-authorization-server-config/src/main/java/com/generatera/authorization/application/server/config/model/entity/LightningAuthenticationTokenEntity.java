package com.generatera.authorization.application.server.config.model.entity;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 14:01
 * @Description lightning Token Entity
 *
 *
 * 目前主要包含了访问 token  / 刷新 token的内容
 */
@Document("lightning_authentication_token")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MappedSuperclass
public class LightningAuthenticationTokenEntity implements Serializable {

    @Id
    @MongoId
    private String id;

    @Column(name = "principal_name")
    @Field(name = "principal_name")
    private String principalName;

    @Column(name = "access_token_value",length = 2000)
    @Field(name = "access_token_value")
    private String accessTokenValue;

    @Column(name = "access_issued_at")
    @Field(name = "access_issued_at")
    private Long accessIssuedAt;

    @Column(name = "access_expired_at")
    @Field(name = "access_expired_at")
    private Long accessExpiredAt;

    /**
     * @see com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningTokenValueType
     */
    @Column(name = "access_token_type")
    @Field(name = "access_token_type")
    private String accessTokenType;

    /**
     * @see LightningTokenType.LightningTokenValueFormat
     */
    @Column(name = "access_token_value_format")
    @Field(name = "access_token_value_format")
    private String accessTokenValueFormat;


    @Column(name = "access_token_meta_data",columnDefinition = "text")
    @Field(name = "access_token_meta_data")
    private String accessTokenMetadata;

    @Column(name = "refresh_token_value",length = 2000)
    @Field(name = "refresh_token_value")
    private String refreshTokenValue;

    @Column(name = "refresh_issued_at")
    @Field(name = "access_issued_at")
    private Long refreshIssuedAt;

    @Column(name = "refresh_expired_at")
    @Field(name = "access_expired_at")
    private Long refreshExpiredAt;

    @Column(name = "refresh_token_type")
    @Field(name = "access_token_type")
    private String refreshTokenType;


    @Column(name = "refresh_token_type_format")
    @Field(name = "refresh_token_type_format")
    private String refreshTokenTypeFormat;


    @Column(name = "refresh_token_meta_data",columnDefinition = "text")
    @Field(name = "refresh_token_meta_data")
    private String refreshTokenMetadata;

}
