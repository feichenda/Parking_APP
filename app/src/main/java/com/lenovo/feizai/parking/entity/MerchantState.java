package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 2021/4/5 0005 下午 2:37:35
 * @annotation
 */
@Data
public class MerchantState {
    int id;
    String merchantname;
    String operatingstate;
    String auditstate;
    String remark;
}
