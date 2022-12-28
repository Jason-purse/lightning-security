package com.generatera.authorization.service.impl;

import com.generatera.authorization.model.params.AppParam;
import com.generatera.authorization.oauth2.entity.OAuth2ClientEntity;
import com.generatera.authorization.service.AppService;
import com.jianyue.lightning.boot.starter.generic.crud.service.support.service.AbstractCrudService;
import org.springframework.stereotype.Service;
/**
 * @author FLJ
 * @date 2022/12/28
 * @time 10:44
 * @Description app 服务实现
 */
@Service
public class AppServiceImpl extends AbstractCrudService<AppParam, OAuth2ClientEntity> implements AppService {


}
