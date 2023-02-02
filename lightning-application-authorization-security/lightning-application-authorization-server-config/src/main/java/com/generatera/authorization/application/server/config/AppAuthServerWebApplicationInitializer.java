package com.generatera.authorization.application.server.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.Banner;
import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
/**
 * @author FLJ
 * @date 2023/2/2
 * @time 11:27
 * @Description 仅作一个记录 ...
 *
 * 此初始化器将不会被使用,当服务器为{@link org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext}
 * 的时候,应该直接添加 {@link org.springframework.boot.web.servlet.ServletContextInitializer} 作为bean 进行 服务器配置 ...
 *
 * 本质上是因为SpringBoot 使用内嵌TOMCAT,没有遵循 servlet3.0相关的引导规范(如果需要生效需要加入 {@link org.springframework.boot.web.servlet.support.SpringBootServletInitializer}
 * )
 *
 * 例如{@link org.springframework.boot.web.embedded.tomcat.TomcatStarter} 会引导servlet3.0 规范的 ServletContainerInitializer 进行初始化 ..
 * 但是默认是没有的 ....
 *
 * 但是我们不可以使用{@link org.springframework.context.ApplicationContextInitializer},
 * 详情查看{@link ServletWebServerApplicationContext#refresh()}
 *
 * 其次,ServletContextAware 本质上是通过后置处理器处理 详情查看 {@link org.springframework.boot.web.servlet.context.WebApplicationContextServletContextAwareProcessor}
 * 
 * 本质上 {@link org.springframework.context.ApplicationContextInitializer} 在spring boot 应用中会被提前抓取使用
 * {@link org.springframework.boot.BootstrapContext} 之后的某个时机执行这些初始化器 ...
 * 详情查看{@link org.springframework.boot.SpringApplication#prepareContext(DefaultBootstrapContext, ConfigurableApplicationContext, ConfigurableEnvironment, SpringApplicationRunListeners, ApplicationArguments, Banner)}
 *
 */
public class AppAuthServerWebApplicationInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
    }
}
