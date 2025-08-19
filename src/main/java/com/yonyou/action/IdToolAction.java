package com.yonyou.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.yonyou.idtool.IdToolDialog;
import org.jetbrains.annotations.NotNull;

/**
 * @author jinchenj
 * @description 雪花ID助手
 * @create:2025-06-2408:55:13
 */
public class IdToolAction extends AnAction {

    public IdToolAction() {
        super("雪花ID助手"); // 设置默认文本
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取当前项目", "错误");
            return;
        }
        IdToolDialog dialog = new IdToolDialog(project);
        dialog.pack();
        dialog.show();
    }

}
