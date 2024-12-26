package com.booktracker.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReadingSessionDialog extends JDialog {
    private JSpinner currentPageSpinner;
    private JLabel startTimeLabel;
    private JLabel durationLabel;
    private Timer timer;
    private LocalDateTime startTime;
    private boolean approved = false;

    public ReadingSessionDialog(Frame owner, String bookTitle, int totalPages, int currentPage) {
        super(owner, "阅读会话 - " + bookTitle, true);
        this.startTime = LocalDateTime.now();
        initializeComponents(totalPages, currentPage);
        pack();
        setLocationRelativeTo(owner);
    }

    private void initializeComponents(int totalPages, int currentPage) {
        setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Start time
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("开始时间:"), gbc);
        gbc.gridx = 1;
        startTimeLabel = new JLabel(startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        formPanel.add(startTimeLabel, gbc);
        
        // Duration
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("已读时间:"), gbc);
        gbc.gridx = 1;
        durationLabel = new JLabel("00:00:00");
        formPanel.add(durationLabel, gbc);
        
        // Current page spinner
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("当前页数:"), gbc);
        gbc.gridx = 1;
        currentPageSpinner = new JSpinner(new SpinnerNumberModel(currentPage, 1, totalPages, 1));
        formPanel.add(currentPageSpinner, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton finishButton = new JButton("完成阅读");
        JButton cancelButton = new JButton("取消");
        
        finishButton.addActionListener(e -> {
            approved = true;
            stopTimer();
            dispose();
        });
        
        cancelButton.addActionListener(e -> {
            stopTimer();
            dispose();
        });
        
        buttonPanel.add(finishButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Start timer
        startTimer();
    }
    
    private void startTimer() {
        timer = new Timer(1000, e -> updateDuration());
        timer.start();
    }
    
    private void stopTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
    
    private void updateDuration() {
        long seconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        durationLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public int getCurrentPage() {
        return (Integer) currentPageSpinner.getValue();
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return LocalDateTime.now();
    }
}
