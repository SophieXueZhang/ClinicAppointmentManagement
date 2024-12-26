package com.booktracker.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BookDialog extends JDialog {
    private JTextField titleField;
    private JTextField authorField;
    private JSpinner pagesSpinner;
    private boolean approved = false;

    public BookDialog(Frame owner, String title) {
        super(owner, title, true);
        initializeComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("书名:"), gbc);
        gbc.gridx = 1;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);
        
        // Author field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("作者:"), gbc);
        gbc.gridx = 1;
        authorField = new JTextField(20);
        formPanel.add(authorField, gbc);
        
        // Pages spinner
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("总页数:"), gbc);
        gbc.gridx = 1;
        pagesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        formPanel.add(pagesSpinner, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        okButton.addActionListener(e -> {
            if (validateInput()) {
                approved = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入书名", "输入错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public String getBookTitle() {
        return titleField.getText().trim();
    }
    
    public String getAuthor() {
        return authorField.getText().trim();
    }
    
    public int getTotalPages() {
        return (Integer) pagesSpinner.getValue();
    }

    public void setBookTitle(String title) {
        titleField.setText(title);
    }

    public void setAuthor(String author) {
        authorField.setText(author);
    }

    public void setTotalPages(int pages) {
        pagesSpinner.setValue(pages);
    }
}
