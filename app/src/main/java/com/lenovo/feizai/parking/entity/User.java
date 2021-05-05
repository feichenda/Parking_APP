package com.lenovo.feizai.parking.entity;

import lombok.Data;

/**
 * @author feizai
 * @date 12/31/2020 031 11:49:27 AM
 */
@Data
public class User {
    int id;
    String username;
    String password;
    String role;
}