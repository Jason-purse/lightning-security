package com.generatera.authorization.controller;

import com.generatera.central.authorization.server.configuration.client.AppService;
import com.generatera.central.authorization.server.configuration.model.param.AppParam;
import com.jianyue.lightning.boot.starter.generic.crud.service.support.controller.AbstractGenericController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author FLJ
 * @date 2022/12/28
 * @time 10:31
 * @Description App controller
 * <p>
 * 这里主要负责 客户端的注册管理
 */
@RestController
@RequestMapping("/api/app/v1")
public class AppController extends AbstractGenericController<AppParam, AppService> {

    @Autowired
    public AppController(AppService appService) {
        super(appService);
    }

}
