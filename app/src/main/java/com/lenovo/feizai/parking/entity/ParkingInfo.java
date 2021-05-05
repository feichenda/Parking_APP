package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 02/22/2021 022 9:10:44 PM
 * @annotation 商家详细信息实体类
 */
@Data
public class ParkingInfo {
    int id;
    String username;
    String merchantname;
    String merchantaddress;
    String merchantimage;
    String businesslicense;
    String phone;
    String linkman;
    String QQ;
}
