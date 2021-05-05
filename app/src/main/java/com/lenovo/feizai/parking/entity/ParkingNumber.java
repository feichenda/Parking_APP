package com.lenovo.feizai.parking.entity;


import lombok.Data;

/**
 * @author feizai
 * @date 02/22/2021 022 9:14:25 PM
 * @annotation
 */
@Data
public class ParkingNumber {
    int id;
    String merchantname;
    int allnumber;
    int usednumber;
    int subscribenumber;
    int unusednumber;
}
