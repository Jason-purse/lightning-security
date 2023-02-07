package com.generatera.maven.plugin.resource.server;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Map;

/**
 * 解析 项目的项目名称,并判断打包类型,如果未打包,则不做任何事情,否则,生成一个文件,尝试在运行时进行读取 ...
 * 编译 ...
 */
@Mojo(name = "compile",defaultPhase = LifecyclePhase.COMPILE)
public class ProjectInfoMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.name}")
    private String projectName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Map<?,?> pluginContext = this.getPluginContext();
        Log log = getLog();
        log.info("----------- context --------");
        StringBuilder builder = new StringBuilder();
        for (Object value : pluginContext.values()) {
            String s = value.toString();
            builder.append("value: \n");
            builder.append(s);
            builder.append("\n");
        }
        int size = pluginContext.values().size();
        String value = String.valueOf(size);

        builder.append("上下文内容: ");
        builder.append(value);
        log.info(builder.toString());
    }
}
