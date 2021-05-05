package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 2021/3/18 0018 下午 9:59:38
 * @annotation
 */
@Data
public class MerchantProperty {
    ParkingInfo parkingInfo;
    Location location;
    ParkingNumber parkingNumber;
    Rates rates;
    MerchantState merchantState;
//    List<ParkingInfo> merchants;
//    List<Location> locations;
//    List<ParkingNumber> parkingNumbers;
}
