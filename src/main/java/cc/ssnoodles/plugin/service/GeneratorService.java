package cc.ssnoodles.plugin.service;

import cc.ssnoodles.db.constant.TemplateType;
import cc.ssnoodles.db.domain.Config;
import cc.ssnoodles.db.domain.Table;
import cc.ssnoodles.db.handler.*;
import cc.ssnoodles.db.util.*;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.ExceptionUtil;

import java.io.*;

/**
 * @author ssnoodles
 * @version 1.0
 * Create at 2020/2/3 16:16
 */
public class GeneratorService {
    private GeneratorService() {
    }

    private static final GeneratorService G = new GeneratorService();

    public static GeneratorService of() {
        return G;
    }

    public static final String DEFAULT_DIR = "/templates";

    public String loadTemplate(TemplateType templateType) {
        try {
            return UrlUtil.loadText(GeneratorService.class.getResource(DEFAULT_DIR + File.separator + templateType.getType() + ".vm")).replace("\r", "");
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
        }
        return "";
    }

    public void create(Project project, Config config, Table table) {
        String projectPath = project.getBasePath();
        if (StringUtil.isEmpty(config.getCustomTemplate())) {
            for (TemplateType type : config.getTemplates()) {
                if (TemplateType.POJO.equals(type)) {
                    PojoTemplate template = new PojoTemplate();
                    FileUtil.write2JavaFiles(projectPath + File.separator + config.getOutPath() + File.separator + template.className(table.getName(), config.getSingleTableRename()),
                            domainPackage(config.getOutPath()) + VelocityUtil.generate(loadTemplate(TemplateType.POJO), template.getContent(config, table)),
                            config.isOverwriteFiles());
                }
                if (TemplateType.DTO.equals(type)) {
                    DtoTemplate template = new DtoTemplate();
                    FileUtil.write2JavaFiles(projectPath + File.separator + config.getOutPath() + File.separator + template.className(table.getName(), config.getSingleTableRename()),
                            domainPackage(config.getOutPath()) + VelocityUtil.generate(loadTemplate(TemplateType.DTO), template.getContent(config, table)),
                            config.isOverwriteFiles());
                }
                if (TemplateType.JPA.equals(type)) {
                    JpaTemplate template = new JpaTemplate();
                    FileUtil.write2JavaFiles(projectPath + File.separator + config.getOutPath() + File.separator + template.className(table.getName(), config.getSingleTableRename()),
                            domainPackage(config.getOutPath()) + VelocityUtil.generate(loadTemplate(TemplateType.JPA), template.getContent(config, table, "javax.persistence.*")),
                            config.isOverwriteFiles());
                }
                if (TemplateType.REPOSITORY.equals(type)) {
                    RepositoryTemplate template = new RepositoryTemplate();
                    FileUtil.write2JavaFiles(projectPath + File.separator + config.getOutPath() + File.separator + template.className(table.getName(), config.getSingleTableRename()),
                            domainPackage(config.getOutPath()) + VelocityUtil.generate(loadTemplate(TemplateType.REPOSITORY), template.getContent(config, table)),
                            config.isOverwriteFiles());
                }
            }
        } else {
            // has custom
            CustomTemplate template = new CustomTemplate();
            FileUtil.write2JavaFiles(projectPath + File.separator + config.getOutPath() + File.separator + template.className(table.getName(), config.getSingleTableRename()),
                    domainPackage(config.getOutPath()) + VelocityUtil.generate(config.getCustomTemplate(), template.getContent(config, table)),
                    config.isOverwriteFiles());
        }
    }

    private static String domainPackage(String packagePath) {
        String parsePackage = parsePackage(packagePath);
        String separator = System.getProperty("line.separator");
        return "".equals(parsePackage) ? "" : "package " + parsePackage + ";" + separator + separator;
    }

    private static String parsePackage(String packagePath) {
        String packageString = "";

        int index = packagePath.indexOf("src/main/java");
        if (index == -1) {
            index = packagePath.indexOf("src\\main\\java");
        }
        if (index == -1) {
            return packageString;
        }
        String substring = packagePath.substring(index + 13);
        if (substring.contains("/")) {
            packageString = substring.replace("/", ".");
        }else {
            packageString = substring.replace("\\", ".");
        }

        if (packageString.startsWith(".")) {
            packageString = packageString.substring(1);
        }
        if (packageString.endsWith(".")) {
            packageString = packageString.substring(0, packageString.length() - 1);
        }
        return packageString;
    }
}
