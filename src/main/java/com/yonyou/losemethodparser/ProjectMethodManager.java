package com.yonyou.losemethodparser;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import javax.swing.*;
import java.util.*;

public class ProjectMethodManager {

    private static volatile boolean isCancelled = false;

    /**
     * 编译项目并获取所有为被引用的方法
     * @param project
     * @param parseButton
     * @param action
     * @param methodInputArea
     * @param excludedScopes
     * @throws Exception
     */
    public static void compileAndGetAllClassesAndMethods(Project project, JButton parseButton, JButton cancelButton, String action, JTextArea methodInputArea, List<String> excludedScopes) throws Exception {
        methodInputArea.setEditable(true);
        methodInputArea.setText(""); // 清空旧内容


        PsiPackage rootPackage = JavaPsiFacade.getInstance(project).findPackage("");
        if (rootPackage == null) {
            Messages.showErrorDialog("项目结构异常", "错误");
            return;
        }

        // 初始化取消标志
        isCancelled = false;

        // 启动后台线程处理包扫描
        new Thread(() -> {
            ReadAction.run(() -> {
                try {
                    processPackage(excludedScopes, rootPackage, GlobalSearchScope.projectScope(project), classMethodName -> {
                        if (isCancelled) return;

                        // 使用 invokeLater 在 EDT 中更新 UI
                        SwingUtilities.invokeLater(() -> {
                            methodInputArea.append(classMethodName + "\n");
                            methodInputArea.setCaretPosition(methodInputArea.getDocument().getLength());
                        });
                    });

                    // 完成后弹出提示
                    if (!isCancelled) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null, "分析完成！");
                        });
                    }

                } catch (Exception e) {
                    String errorMessage = "获取失效方法失败：" + e.getClass().getSimpleName() + ": " + e.getMessage();
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog(errorMessage, "错误");
                    });
                } finally {
                    // 恢复 UI
                    SwingUtilities.invokeLater(() -> {
                        parseButton.setText("获取失效方法");
                        parseButton.setEnabled(true);
                        cancelButton.setText("取消");
                        cancelButton.setEnabled(false);
                    });
                }
            });
        }).start();

        // 设置取消按钮可用
        parseButton.setText("读取失效方法中...");
        parseButton.setEnabled(false);
        cancelButton.setText("取消");
        cancelButton.setEnabled(true);
        cancelButton.addActionListener(e -> isCancelled = true);

    }

    /**
     * 处理包中未被引用的废弃的类和方法
     * @param excludedScopes
     * @param psiPackage 包对象
     * @param scope 搜索范围
     * @param callback 结果
     */
    private static void processPackage(List<String> excludedScopes, PsiPackage psiPackage, GlobalSearchScope scope, UnusedMethodCallback callback)  {
        if (isCancelled) return;
        // 处理当前包中的类
        for (PsiClass psiClass : psiPackage.getClasses(scope)) {
            if (!psiClass.isValid() || isCancelled) continue;
            String className = psiClass.getName();
            String qualifiedName = psiClass.getQualifiedName();
            String pathName = qualifiedName.replace(".", "/").concat(".java");
            // 排除 test
            if (excludedScopes.contains("test") && (pathName.contains("test"))
            ) {
                continue;
            }
            if (excludedScopes.contains("test")) {
                PsiClass superClass = psiClass.getSuperClass();
                if (superClass != null && "com.yonyou.biz.mm.qic.ut.qic.mock.QICBaseTest".equals(superClass.getQualifiedName())) {
                    continue;
                }
            }
            // 排除 deprecated
            if (excludedScopes.contains("deprecated") && pathName.contains("deprecated")) {
                continue;
            }
            // 排除 resource
            if (excludedScopes.contains("resource") && pathName.contains("resource")) {
                continue;
            }
            // 排除 model
            if (excludedScopes.contains("model") && pathName.contains("model")) {
                continue;
            }

            // 排除类注解包含 @RemoteCall 的类
            if (excludedScopes.contains("RemoteCall") && psiClass.hasAnnotation("com.yonyou.cloud.middleware.rpc.RemoteCall")) {
                continue;
            }

            // 排除 DTO
            if (excludedScopes.contains("DTO") && (
                    className.endsWith("DTO")
                            || className.endsWith("Dto") || className.endsWith("dto") || pathName.contains(".dto"))
            ) {
                continue;
            }
            // 排除 Enum
            if (excludedScopes.contains("Enum") && (
                    className.endsWith("ENUM")
                            || className.endsWith("Enum"))
            ) {
                continue;
            }

            // 排除 Controller
            if (excludedScopes.contains("Controller") && (
                    className.endsWith("Controller")
                            || psiClass.hasAnnotation("org.springframework.stereotype.Controller")
                            || psiClass.hasAnnotation("org.springframework.web.bind.annotation.RestController"))
            ) {
                continue;
            }

            for (PsiMethod method : psiClass.getMethods()) {
                if (!method.isValid()) {
                    continue;
                }
                // 排除构造函数和 lambda 表达式生成的方法
                if (method.isConstructor() || method.getName().startsWith("lambda$")) {
                    continue;
                }

                // 查找该方法的引用
                Query<PsiReference> referencesQuery = ReferencesSearch.search(method, scope, false);
                List<PsiReference> references = (List<PsiReference>) referencesQuery.findAll();
                if (!references.isEmpty()) {
                    continue;
                };

                // 检查是否是接口方法的实现，并查看接口方法是否被引用
                PsiMethod[] superMethods = method.findSuperMethods();
                for (PsiMethod superMethod : superMethods) {
                    if (superMethod.getContainingClass().isInterface()) {
                        // 只要是接口方法的实现，就认为它是被使用的，不管有没有引用
                        return; // 跳过报告
                    }
                }
                // 真正未被使用
                String classMethodName = className + "." + method.getName();
                callback.onUnusedMethodFound(classMethodName);
            }
        }
        // 递归处理子包
        for (PsiPackage subPackage : psiPackage.getSubPackages(scope)) {
            processPackage(excludedScopes, subPackage, scope, callback);
        }
    }

    public static void cancel() {
        isCancelled = true;
        JOptionPane.showMessageDialog(null, "任务已取消");
    }

}