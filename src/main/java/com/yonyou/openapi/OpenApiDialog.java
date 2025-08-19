package com.yonyou.openapi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.yonyou.losemethodparser.ProjectMethodManager;
import com.yonyou.methoddesign.ProjectManager;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

/**
 * 对话面板
 */
public class OpenApiDialog extends DialogWrapper {


    private final Project project;
    private JTextField appKeyInput;
    private JTextField appSecretInput;
    private JTextField signatureInput;
    private JTextField timestampInput;


    public OpenApiDialog(Project project) {
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
        JLabel appKeyLabel = new JLabel("AppKey:");
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 10);
        inputPanel.add(appKeyLabel, gbc);

        appKeyInput = new JTextField();
        appKeyInput.setText("87af21578d06474a9bb19bca800fc4a5");
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        inputPanel.add(appKeyInput, gbc);

        JLabel appSecretLabel = new JLabel("AppSecret:");
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 10);
        inputPanel.add(appSecretLabel, gbc);

        appSecretInput = new JTextField();
        appSecretInput.setText("d69c9c37968541bf6ad0d3d1a0b5cc3c066eafbe");
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        inputPanel.add(appSecretInput, gbc);

        JLabel signatureLabel = new JLabel("signature:");
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 10);
        inputPanel.add(signatureLabel, gbc);

        signatureInput = new JTextField();
        signatureInput.setText("");
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        inputPanel.add(signatureInput, gbc);

        JLabel timestampLabel = new JLabel("timestamp:");
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 10);
        inputPanel.add(timestampLabel, gbc);

        timestampInput = new JTextField();
        timestampInput.setText("");
        gbc.gridy = 3;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        inputPanel.add(timestampInput, gbc);

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
        JButton parseButton = new JButton("获取signature");
        parseButton.addActionListener(e -> {
            // 禁用按钮，防止重复点击
            parseButton.setEnabled(false);
            parseButton.setText("处理中...");
            try {
                String appKey = appKeyInput.getText();
                String anceAppSecret = appKeyInput.getText();
                genSignature(appKey, anceAppSecret);
            } catch (Exception ex) {
                Messages.showErrorDialog("获取signature失败：" + ex.getMessage(), "错误");
                ex.printStackTrace();
            } finally {
                // 恢复按钮状态
                parseButton.setEnabled(true);
                parseButton.setText("获取signature");
            }
        });
        southPanel.add(parseButton);
        return southPanel;
    }

    private void genSignature(String appKey, String anceAppSecret) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String s = "appKey" + appKey + "timestamp" + timestamp;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(anceAppSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(s.getBytes(StandardCharsets.UTF_8));
        String base64String = Base64.getEncoder().encodeToString(signData);
        String signature = URLEncoder.encode(base64String, "UTF-8");
        signatureInput.setText(signature);
        timestampInput.setText(timestamp);
    }

}