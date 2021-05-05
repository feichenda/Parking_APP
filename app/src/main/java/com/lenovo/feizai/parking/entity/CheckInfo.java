package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 2021/4/17 0017 下午 3:34:39
 * @annotation
 */
@Data
public class CheckInfo {
    Integer id;
    String merchant;
    String serialnumber;
    String carlicense;
    String intime;
    String outtime;
    String ordernumber;
    Float price;
    String state;
}
