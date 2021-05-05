package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 2021/4/8 0008 下午 3:19:31
 * @annotation
 */
@Data
public class MerchantChange {
    Integer id;
    String username;
    String oldmerchantname;
    String newmerchantname;
    String merchantaddress;
    Double longitude;
    Double latitude;
    String city;
    Float onehour;
    Float otherone;
    String businesslicense;
    String auditstate;
    String remark;
}
