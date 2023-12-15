package com.yyh.xfs.common.utils;

import com.yyh.xfs.common.constant.StatusCode;
import com.yyh.xfs.common.domain.Result;

/**
 * @author yyh
 * @date 2023-12-09
 */
public class ResultUtil {
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    public static <T> Result<T> successGet(T data) {
        return new Result<>(StatusCode.GET_SUCCESS, SUCCESS, data);
    }
    public static <T> Result<T> successGet(String msg, T data) {
        return new Result<>(StatusCode.GET_SUCCESS, msg, data);
    }
    public static <T> Result<T> successPost(T data) {
        return new Result<>(StatusCode.POST_SUCCESS, SUCCESS, data);
    }
    public static <T> Result<T> successPost(String msg, T data) {
        return new Result<>(StatusCode.POST_SUCCESS, msg, data);
    }

    public static <T> Result<T> successPut(T data) {
        return new Result<>(StatusCode.PUT_SUCCESS, SUCCESS, data);
    }
    public static <T> Result<T> successPut(String msg, T data) {
        return new Result<>(StatusCode.PUT_SUCCESS, msg, data);
    }
    public static <T> Result<T> successDelete(T data) {
        return new Result<>(StatusCode.DELETE_SUCCESS, SUCCESS, data);
    }
    public static <T> Result<T> successDelete(String msg, T data) {
        return new Result<>(StatusCode.DELETE_SUCCESS, msg, data);
    }
    public static <T> Result<T> errorGet(String msg){
        return new Result<>(StatusCode.GET_ERROR, msg, null);
    }
    public static <T> Result<T> errorGet(T data) {
        return new Result<>(StatusCode.GET_ERROR, ERROR, data);
    }

    public static <T> Result<T> errorPost(String msg){
        return new Result<>(StatusCode.POST_ERROR, msg, null);
    }
    public static <T> Result<T> errorPost(T data) {
        return new Result<>(StatusCode.POST_ERROR, ERROR, data);
    }

    public static <T> Result<T> errorPut(String msg){
        return new Result<>(StatusCode.PUT_ERROR, msg, null);
    }
    public static <T> Result<T> errorPut(T data) {
        return new Result<>(StatusCode.PUT_ERROR, ERROR, data);
    }

    public static <T> Result<T> errorDelete(String msg){
        return new Result<>(StatusCode.DELETE_ERROR, msg, null);
    }
    public static <T> Result<T> errorDelete(T data) {
        return new Result<>(StatusCode.DELETE_ERROR, ERROR, data);
    }


}
