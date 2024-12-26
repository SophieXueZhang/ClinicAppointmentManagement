package com.booktracker.exception;

/**
 * 图书追踪应用的自定义异常类
 * 用于封装应用中的所有业务异常
 */
public class BookTrackerException extends RuntimeException {
    
    /**
     * 创建一个新的BookTrackerException实例
     * @param message 异常信息
     */
    public BookTrackerException(String message) {
        super(message);
    }

    /**
     * 创建一个新的BookTrackerException实例
     * @param message 异常信息
     * @param cause 原始异常
     */
    public BookTrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
