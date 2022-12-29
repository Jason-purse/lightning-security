package com.generatera.authorization.service.impl

import com.generatera.authorization.model.params.AppParam
import com.generatera.authorization.oauth2.entity.OAuth2ClientEntity
import com.generatera.authorization.service.AppService
import com.jianyue.lightning.boot.starter.generic.crud.service.support.DefaultJpaValidationSupportForQueryAdapter
import com.jianyue.lightning.boot.starter.generic.crud.service.support.converters.EntityConverter
import com.jianyue.lightning.boot.starter.generic.crud.service.support.query.QuerySupport
import com.jianyue.lightning.boot.starter.generic.crud.service.support.query.jpa.DefaultJpaQuery
import com.jianyue.lightning.boot.starter.generic.crud.service.support.query.jpa.JpaQueryInfo
import com.jianyue.lightning.boot.starter.generic.crud.service.support.service.AbstractCrudService
import com.jianyue.lightning.boot.starter.util.BeanUtils
import com.jianyue.lightning.boot.starter.util.isNotNull
import com.jianyue.lightning.framework.generic.crud.abstracted.param.Param
import com.jianyue.lightning.util.JsonUtil
import com.safone.order.service.model.order.verification.support.converters.QueryConverter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.lang.reflect.Type

/**
 * @author FLJ
 * @date 2022/12/28
 * @time 15:58
 * @Description 默认 app 服务实现
 */
@Service
class DefaultAppServiceImpl : AbstractCrudService<AppParam, OAuth2ClientEntity>(), AppService {

    override fun getParamClass(): Class<out Param> {
        return AppParam::class.java;
    }

}

interface AppQueryHandler : QueryConverter<AppParam> {
    override fun getSourceClass(): Type {
        return AppParam::class.java
    }
}

@Component
class DefaultAppQueryHandler : DefaultJpaValidationSupportForQueryAdapter<AppParam>, AppQueryHandler {

    override fun addGroupHandle(s: AppParam): QuerySupport {

        return DefaultJpaQuery(
            // 根据客户端名称唯一性约束
            JpaQueryInfo(
                OAuth2ClientEntity().apply {
                    clientName = s.clientName
                }
            )
        )
    }

    override fun deleteGroupHandle(s: AppParam): QuerySupport {
        // 根据客户端名称唯一性约束
        return DefaultJpaQuery(
            JpaQueryInfo(
                OAuth2ClientEntity().apply {
                    clientName = s.clientName
                }
            )
        )
    }

    override fun selectListGroupHandle(s: AppParam?): QuerySupport {
        return DefaultJpaQuery(
            JpaQueryInfo(OAuth2ClientEntity())
        )
    }

}

/**
 * 转换器 ...
 */
@Component
class AppParamConverter: EntityConverter<AppParam,OAuth2ClientEntity> {

    override fun getSourceClass(): Type {
        return AppParam::class.java;
    }

    override fun getTargetClass(): Type {
        return OAuth2ClientEntity::class.java
    }

    override fun support(param: Any): Boolean {
        return AppParam::class.isInstance(param)
    }

    override fun convertToEntity(param: AppParam): OAuth2ClientEntity {
        return BeanUtils.transformFrom(param,OAuth2ClientEntity::class.java)!!.apply {
            if(param.authorizationGrantTypes.isNotNull()) {
                // 设置
                authorizationGrantTypes = param.authorizationGrantTypes.joinToString(transform = {it.value})
            }

            if(param.clientAuthenticationMethods.isNotNull()) {
                clientAuthenticationMethods = param.clientAuthenticationMethods.joinToString(transform = {it.value})
            }
            if(param.redirectUris.isNotNull()) {
                redirectUris = param.redirectUris.joinToString()
            }
            if(param.scopes.isNotNull()) {
                scopes = param.scopes.joinToString()
            }
            if(param.tokenOtherSettings.isNotNull()) {
                tokenOtherSettings = JsonUtil.asJSON(param.tokenOtherSettings)
            }
            if(param.clientOtherSettings.isNotNull()) {
                clientOtherSettings = JsonUtil.asJSON(param.clientOtherSettings)
            }
            if(param.accessTokenFormat.isNotNull()) {
                accessTokenFormat = param.accessTokenFormat.value
            }
            if(param.idTokenSignatureAlgorithm.isNotNull()) {
                idTokenSignatureAlgorithm = param.idTokenSignatureAlgorithm.name
            }
            if(param.tokenEndpointAuthenticationSigningAlgorithm.isNotNull()) {
                tokenEndpointAuthenticationSigningAlgorithm = param.tokenEndpointAuthenticationSigningAlgorithm.name
            }
        }
    }
}