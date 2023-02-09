package com.generatera.maven.plugin.resource.server.annotation.processor;

import com.generatera.resource.server.common.EnableLightningMethodSecurity;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author FLJ
 * @date 2023/2/8
 * @time 11:07
 * @Description 修改对应的类文件 ...
 */
@AutoService(Processor.class)
public class ProjectInspectProcessor extends AbstractProcessor {
    private  ProcessingEnvironment processingEnvironment;

    private Messager messager;

    /**
     * 封装了创建AST节点的一些方法
     *
     * AST(抽象语法树)是一种用来描述程序代码语法结构的树形表示方式，
     * 语法树的每一个节点都代表着程序代码中的一个语法结构，
     * 例如包、类型、修饰符、运算符、接口、返回值，甚至代码注释等都可以是一个语法结构。
     */
    private JavacTrees trees;

    private Context context;

    /**
     * 封装了创建AST节点的一些方法
     */
    private TreeMaker treeMaker;

    /**
     * Names 提供了创建标识符的方法
     */
    private Names names;

    private JavacElements elementUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnvironment = processingEnv;
        trees = JavacTrees.instance(processingEnv);
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);

        elementUtils = (JavacElements)processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableLightningMethodSecurity.class);
        // 寻找参数名
        for (Element element : elements) {
            EnableLightningMethodSecurity annotation = element.getAnnotation(EnableLightningMethodSecurity.class);
            String value = annotation.value();
            if(StringUtils.isNotBlank(value)) {

                elementUtils.getTree(element).accept(new TreeTranslator() {
                    @Override
                    public void visitSelect(JCTree.JCFieldAccess tree) {
                        if (tree.name.equals(names.fromString(value))) {
                            // 设置为字符串 ...
                            tree.selected =  treeMaker.Literal("23456");
                        }
                    }
                });
            }
        }



        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(EnableLightningMethodSecurity.class.getName());
    }
}
