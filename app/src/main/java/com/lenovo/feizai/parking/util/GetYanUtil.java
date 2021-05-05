package com.lenovo.feizai.parking.util;

import java.util.Random;

public class GetYanUtil {
    public static String getyan() {
        int data,i;
        char a[]={'0','1','2','3','4','5','6','7','8','9'};
        char temp[];
        temp=new char [4];
        String s = null;
        Random random=new Random();
        for(i=0;i<4;i++){
            data=random.nextInt(10);
            temp[i]=a[data];
        }
        s=String.valueOf(temp);
        return s;
    }
}
