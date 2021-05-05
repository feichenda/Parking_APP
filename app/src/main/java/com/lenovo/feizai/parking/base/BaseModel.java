package com.lenovo.feizai.parking.base;

import java.util.List;

import lombok.Data;

/**
 * @author feizai
 * @date 12/31/2020 031 11:45:15 AM
 */
@Data
public class BaseModel<T> {
    private int code;//默认成功的时候为200
    private String message;
    private T data;
    private List<T> datas;
}

