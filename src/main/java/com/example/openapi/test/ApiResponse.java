package com.example.openapi.test;

/**
 * 通用API响应结构
 * @param <T> 响应数据类型
 */
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;
    
    public ApiResponse() {
    }
    
    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return code == 0 || (code >= 200 && code < 300);
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
