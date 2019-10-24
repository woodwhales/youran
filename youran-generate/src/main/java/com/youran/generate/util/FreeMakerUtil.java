package com.youran.generate.util;

import com.youran.common.exception.BusinessException;
import com.youran.generate.constant.JFieldType;
import com.youran.generate.constant.MetaConstType;
import com.youran.generate.constant.MetaSpecialField;
import com.youran.generate.constant.QueryType;
import com.youran.generate.exception.SkipCurrentException;
import freemarker.core._TemplateModelException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * <p>Title:FreeMaker工具类</p>
 * <p>Description:</p>
 * @author: cbb
 * @date: 2017/5/13
 */
public class FreeMakerUtil {

    private static final Logger logger = LoggerFactory.getLogger(FreeMakerUtil.class);

    private static Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
    private static BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_21);

    static {
        cfg.setClassForTemplateLoading(FreeMakerUtil.class, "/ftl");
        cfg.setNumberFormat("#");
        // 设置可访问的静态工具类
        cfg.setSharedVariable("MetaConstType",getStaticModel(MetaConstType.class));
        cfg.setSharedVariable("MetadataUtil",getStaticModel(MetadataUtil.class));
        cfg.setSharedVariable("TemplateUtil",getStaticModel(TemplateUtil.class));
        cfg.setSharedVariable("JFieldType",getStaticModel(JFieldType.class));
        cfg.setSharedVariable("QueryType",getStaticModel(QueryType.class));
        cfg.setSharedVariable("MetaSpecialField",getStaticModel(MetaSpecialField.class));
        builder.setExposeFields(true);
    }

    /**
     * 获取模板文件
     *
     * @param name
     * @return
     */
    public static Template getTemplate(String name) {
        try {
            Template template = cfg.getTemplate(name, "utf-8");
            return template;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BusinessException("获取freemarker模版异常");
        }
    }

    /**
     * 控制台输出
     *
     * @param templateName
     * @param dataModel
     */
    public static void print(String templateName, Object dataModel) {
        try {
            Template template = getTemplate(templateName);
            template.process(dataModel, new PrintWriter(System.out));
        } catch (Exception e) {
            if(e instanceof _TemplateModelException){
                Throwable cause = e.getCause();
                if(cause!=null && cause instanceof SkipCurrentException){
                    throw (SkipCurrentException)cause;
                }
            }
            logger.error(e.getMessage(), e);
            throw new BusinessException("freemarker解析异常");
        }

    }

    /**
     * PrintWriter写入
     * @param templateName
     * @param dataModel
     */
    public static void write(String templateName, Object dataModel, PrintWriter printWriter) {
        try {
            Template template = getTemplate(templateName);
            template.process(dataModel, printWriter);
        } catch (Exception e) {
            if(e instanceof _TemplateModelException){
                Throwable cause = e.getCause();
                if(cause!=null && cause instanceof SkipCurrentException){
                    throw (SkipCurrentException)cause;
                }
            }
            logger.error(e.getMessage(), e);
            throw new BusinessException("freemarker解析异常");
        }

    }

    /**
     * 输出文本
     *
     * @param templateName
     * @param dataModel
     */
    public static String writeToStr(String templateName, Object dataModel) {
        try {
            StringWriter stringWriter = new StringWriter();
            BufferedWriter writer = new BufferedWriter(stringWriter);
            Template template = getTemplate(templateName);
            template.process(dataModel, writer);
            return stringWriter.getBuffer().toString();
        } catch (Exception e) {
            String extraErrorMsg = "";
            if(e instanceof _TemplateModelException){
                Throwable cause = e.getCause();
                if(cause!=null){
                    if(cause instanceof SkipCurrentException){
                        throw (SkipCurrentException)cause;
                    }else if(cause instanceof BusinessException){
                        extraErrorMsg = ",原因："+cause.getMessage();
                    }
                }
            }
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            throw new BusinessException("freemarker解析异常,dataModel="
                +dataModel+",templateName="+templateName+extraErrorMsg,e);
        }
    }


    /**
     * 生成文件
     * @param templateName:模板名
     * @param dataModel：数据原型
     * @param outFilePath：输出路径(全路径名)
     */
    public static void generateFile(String templateName, Object dataModel, String outFilePath) {
        // 通过一个文件输出流，就可以写到相应的文件中，此处用的是绝对路径
        try (FileWriter out = new FileWriter(outFilePath)) {
            Template temp = getTemplate(templateName);
            temp.process(dataModel, out);
        } catch (Exception e) {
            if(e instanceof _TemplateModelException){
                Throwable cause = e.getCause();
                if(cause!=null && cause instanceof SkipCurrentException){
                    throw (SkipCurrentException)cause;
                }
            }
            logger.error(e.getMessage(), e);
            throw new BusinessException("freemarker解析异常");
        }
    }


    /**
     * 获取freemarker可使用的bean
     * @param clz
     * @return
     */
    public static TemplateModel getStaticModel(Class clz) {
        BeansWrapper beansWrapper = builder.build();
        try {
            return beansWrapper.getStaticModels().get(clz.getName());
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
        return null;
    }

}
