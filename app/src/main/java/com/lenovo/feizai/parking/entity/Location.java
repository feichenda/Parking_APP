package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 02/22/2021 022 9:14:25 PM
 * @annotation
 */
@Data
public class Location {
    int id;
    String merchantname;
    double longitude;
    double latitude;
    String city;
}
