package com.base_project.java_postgresql.models.dto.responses.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BaseResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object errors;

    public static <T> BaseResponse<T> success(T data, String message){
        return new BaseResponse<>(true, message, data, null);
    }

    public static BaseResponse<Object> fail(String message, Object errors){
        return new BaseResponse<>(false, message, null, errors);
    }

}