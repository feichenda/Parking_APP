package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 01/11/2021 011 8:08:19 PM
 */
@Data
public class Customer {
    int id;
    String username;
    String phone;
    String QQ;
    String company;
    double company_longitude;
    double company_latitude;
    String home;
    double home_longitude;
    double home_latitude;
    String avatar;
}
