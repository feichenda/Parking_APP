package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 2021/4/12 0012 下午 4:26:28
 * @annotation
 */
@Data
public class Comment {
    Integer id;
    String username;
    String avatar;
    String container;
    String commenttime;
    String merchantname;
}
