package com.zzhow.magicshare.result;

/**
 * 统一的结果
 *
 * @author ZZHow
 * @date 2025/01/14
 */
public class Result<T> {
    private Integer code; // 代码：1 成功，0 和其它数字为失败
    private String message; // 信息
    private T data; // 数据

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> success(String message, T object) {
        return new Result<>(0, message, object);
    }

    public static <T> Result<T> success(T object) {
        return new Result<>(0, "success", object);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(1, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> loginSuccessful(T object) {
        return new Result<>(0, "Login successful", object);
    }

    public static <T> Result<T> loginFailed(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(1, "Unauthorized", null);
    }

    public static <T> Result<T> resourceNotFound() {
        return new Result<>(1, "Resource Not Found", null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
