package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 2021/3/28 0028 下午 4:40:21
 * @annotation
 */
@Data
public class CollectionInfo {
    Integer id;
    String username;
    String address;
    String remark;
    Double longitude;
    Double latitude;
}
