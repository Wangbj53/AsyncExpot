package com.asyncexport.boot.utils;

/**
 * @author Wangbj
 * @date 2024年07月02日 14:49
 */

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试
 *
 * @date 2024/1/5 11:14
 **/
public class ClassUtil {

    public static void main(String[] args) throws ClassNotFoundException {

        List<Class<?>> classAll = getClassAll("src/main/java/com/asyncexport/boot/controller");
        classAll.forEach(item -> {
            try {
                System.out.println("类："+item.getName());
                Class<?> clazz = item;
                CompilationUnit compilationUnit = StaticJavaParser.parse(new FileInputStream("src/main/java/" + clazz.getName().replace(".", "/") + ".java"));
                // 获取所有方法声明
                List<MethodDeclaration> methodDeclarations = compilationUnit.findAll(MethodDeclaration.class);
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                String url = requestMapping.value()[0];
                // 遍历方法声明，获取方法参数信息和局部变量信息
                for (MethodDeclaration method : methodDeclarations) {
                    try {
                        List<Node> childNodes = method.getChildNodes();

                        for (int i = 0; i < childNodes.size(); i++) {
                            // 遍历每一行数据 遇到基础url类型注解 或者 遇到权限关键字跳出循环
                            try {
                                SingleMemberAnnotationExpr singleMemberAnnotationExpr = (SingleMemberAnnotationExpr) childNodes.get(i);
                                StringLiteralExpr memberValue = (StringLiteralExpr) singleMemberAnnotationExpr.getMemberValue();
                                System.out.println("url: /" + url + memberValue.getValue());
                                break;
                            } catch (Exception e) {
                                continue;
                            }
                        }
                        String content = method.getComment().get().getContent();
                        System.out.println("注释：" + content.substring(content.indexOf("*") + 1, content.indexOf("* @") != -1 ? content.indexOf("* @") : content.length()));
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {
                System.out.println("当前类不存在URL");

            }
        });
    }


    public static List<Class<?>> getClassAll(String path) {

        Path dir = Paths.get(path);

        List<Class<?>> classList = new ArrayList<>();
        try {
            // 使用Files.walkFileTree遍历目录树
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @SneakyThrows
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // 检查文件是否是Java文件
                    if (file.toString().endsWith(".java")) {
                        String fileName = file.getFileName().toString();
                        String substringName = path.substring(path.indexOf("java/") + 5).replace("/", ".") + "." + fileName.substring(0, fileName.indexOf(".java"));
                        classList.add(Class.forName(substringName));
                    }
                    return FileVisitResult.CONTINUE; // 继续遍历
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // 处理访问文件时发生的错误
                    System.err.println(exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classList;
    }
}