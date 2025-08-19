package com.yonyou.idtool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.yonyou.util.HttpRequestUtil;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 对话面板
 */
public class IdToolDialog extends DialogWrapper {


    private final Project project;
    private JTextField idCountText;
    private JTextArea idTextArea;


    public IdToolDialog(Project project) {
        super(project);
        this.project = project;
        setTitle("OpenApi助手");
        init();
    }

    /**
     * 中间内容面板
     * @return
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setPreferredSize(new Dimension(600, 300)); // 增加整体高度以容纳内容

        // 创建输入面板
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // 输入框
        JLabel idCountLabel = new JLabel("ID数量:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 10);
        inputPanel.add(idCountLabel, gbc);

        idCountText = new JTextField();
        idCountText.setText("");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        inputPanel.add(idCountText, gbc);

        JLabel idLabel = new JLabel("ID:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 10);
        inputPanel.add(idLabel, gbc);

        idTextArea = new JTextArea();
        idTextArea.setText("");
        idTextArea.setRows(5);
        idTextArea.setLineWrap(true); // 自动换行（可选）
        idTextArea.setWrapStyleWord(true);
        JBScrollPane scrollPane = new JBScrollPane(idTextArea);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        inputPanel.add(scrollPane, gbc);

        dialogPanel.add(inputPanel, BorderLayout.CENTER);
        return dialogPanel;
    }

    /**
     * 底部按钮面板
     * @return
     */
    @Override
    protected JComponent createSouthPanel() {
        JPanel southPanel = new JPanel(new FlowLayout());
        JButton parseButton = new JButton("获取雪花ID");
        parseButton.addActionListener(e -> {
            // 禁用按钮，防止重复点击
            parseButton.setEnabled(false);
            parseButton.setText("处理中...");
            try {
                genSignature();
            } catch (Exception ex) {
                Messages.showErrorDialog("获取雪花ID失败：" + ex.getMessage(), "错误");
                ex.printStackTrace();
            } finally {
                // 恢复按钮状态
                parseButton.setEnabled(true);
                parseButton.setText("获取雪花ID");
            }
        });
        southPanel.add(parseButton);
        return southPanel;
    }

    private void genSignature() throws Exception {
        String count = idCountText.getText();
        String url = "https://yonbip.diwork.com/iuap-yonbuilder-businessflow/common/snowflakeUuid?count=" + count;
        String resp = HttpRequestUtil.doGet(url, null);
        JSONObject respJSON = new JSONObject(resp);
        JSONArray data = respJSON.getJSONArray("data");
        StringBuilder sb = new StringBuilder();
        for (Object item : data) {
            sb.append(item).append("\n");
        }
        idTextArea.setText(sb.toString());
    }

}