package com.lenovo.feizai.parking.entity;

import lombok.Data;

@Data
public class Password {
    int id;
    String username;
    String oldpassword;
    String newpassword;
    String role;
}
