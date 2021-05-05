package com.lenovo.feizai.parking.entity;

import java.sql.Timestamp;

import lombok.Data;

/**
 * @author feizai
 * @date 2021/3/25 0025 下午 6:57:16
 * @annotation
 */
@Data
public class Order {
    Integer id;
    String orderNumber;
    String merchantName;
    String customerName;
    String space;
    String carLicense;
    Float price;
    Integer duration;
    Timestamp startDate;
    Timestamp endDate;
    String qrCode;
    String state;
    String orderType;
}
