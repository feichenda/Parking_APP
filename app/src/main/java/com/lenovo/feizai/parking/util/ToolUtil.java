package com.lenovo.feizai.parking.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.baidu.mapapi.model.LatLng;
import com.lenovo.feizai.parking.entity.Location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feizai
 * @date 01/14/2021 014 9:36:03 PM
 */
public class ToolUtil {
    /**
     * 验证邮箱
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码
     *
     * @param mobileNumber
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber) {
        boolean flag = false;
        try {
            Pattern regex = Pattern.compile("^((13[0-9])|(14[579])|(15[0-35-9])|(16[2567])|(17[0-35-8])|(18[0-9])|(19[0-35-9]))\\d{8}$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 车牌号验证
     *
     * @param carLicense
     * @return
     */
    public static boolean checkCarLicense(String carLicense) {
        boolean flag = false;
        try {
            String pattern = "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]" +
                    "{1}(([A-HJ-Z]{1}[A-HJ-NP-Z0-9]{5})|([A-HJ-Z]{1}(([DF]{1}[A-HJ-NP-Z0-9]{1}[0-9]{4})|([0-9]{5}[DF]" +
                    "{1})))|([A-HJ-Z]{1}[A-D0-9]{1}[0-9]{3}警)))|([0-9]{6}使)|((([沪粤川云桂鄂陕蒙藏黑辽渝]{1}A)|鲁B|闽D|蒙E|蒙H)[0-9]{4}领)" +
                    "|(WJ[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼·•]{1}[0-9]{4}[TDSHBXJ0-9]{1})" +
                    "|([VKHBSLJNGCE]{1}[A-DJ-PR-TVY]{1}[0-9]{5})";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(carLicense);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    public static List<String> arrayToList(String[] array) {
        List<String> list = new ArrayList<>();
        for (String s : array) {
            list.add(s);
        }
        return list;
    }

    private static final double EARTH_RADIUS = 6378.137;

    /**
     * 根据经纬度，计算两点间的距离
     *
     * @param location1 第一个点的经纬度
     * @param location2 第一个点的经纬度
     * @return 返回距离 单位千米
     */
    public static double getDistance(Location location1, Location location2) {
        // 纬度
        double lat1 = Math.toRadians(location1.getLatitude());
        double lat2 = Math.toRadians(location2.getLatitude());
        // 经度
        double lng1 = Math.toRadians(location1.getLongitude());
        double lng2 = Math.toRadians(location2.getLongitude());
        // 纬度之差
        double a = lat1 - lat2;
        // 经度之差
        double b = lng1 - lng2;
        // 计算两点距离的公式
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘地球半径, 返回单位: 千米
        s = s * EARTH_RADIUS;
        BigDecimal bigDecimal = new BigDecimal(s);
        double f1 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }

    /**
     * 根据经纬度，计算两点间的距离
     *
     * @param location1 第一个点的经纬度
     * @param location2 第一个点的经纬度
     * @return 返回距离 单位千米
     */
    public static double getDistance(Location location1, LatLng location2) {
        // 纬度
        double lat1 = Math.toRadians(location1.getLatitude());
        double lat2 = Math.toRadians(location2.latitude);
        // 经度
        double lng1 = Math.toRadians(location1.getLongitude());
        double lng2 = Math.toRadians(location2.longitude);
        // 纬度之差
        double a = lat1 - lat2;
        // 经度之差
        double b = lng1 - lng2;
        // 计算两点距离的公式
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘地球半径, 返回单位: 千米
        s = s * EARTH_RADIUS;
        BigDecimal bigDecimal = new BigDecimal(s);
        double f1 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }

    //把bitmap图保存成文件
    public static File savePhotoToSDCard(Bitmap photoBitmap, String path) {
        boolean flag = false;
        File photoFile = null;
        if (checkSDCardAvailable()) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            photoFile = new File(path, "code.jpg");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
                        fileOutputStream.flush();
                        flag = true;
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (flag)
            return photoFile;
        else
            return null;
    }

    //检查是否有SD卡
    public static boolean checkSDCardAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getUsername(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        return username;
    }

    public static String getRole(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String role = preferences.getString("role", "");
        return role;
    }

    public static String getUsernameAvatar(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String avatar = preferences.getString("avatar", "");
        return avatar;
    }

    public static String getPhone(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String phone = preferences.getString("phone", "");
        return phone;
    }

    public static String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
    }

    /**
     * 将时间转换成显示的时间格式，参考微信
     *
     * @param addTime
     * @return
     */
    public static String getShowTime(String addTime) {
        //当前日历时间
        Calendar currentCalendar = Calendar.getInstance();
        //发布日历时间
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(strToDateLong(addTime));
        //当前时间单位
        int cYear = currentCalendar.get(Calendar.YEAR);
        int cMonth = currentCalendar.get(Calendar.MONTH);
        int cDate = currentCalendar.get(Calendar.DAY_OF_MONTH);
        int cHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int cMinuter = currentCalendar.get(Calendar.MINUTE);
        //发布时间单位
        int sYear = startCalendar.get(Calendar.YEAR);
        int sMonth = startCalendar.get(Calendar.MONTH);
        int sDate = startCalendar.get(Calendar.DAY_OF_MONTH);
        int sHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        int sMinuter = startCalendar.get(Calendar.MINUTE);
        if (cYear > sYear) {
            return (cYear - sYear) + "年前";
        } else if (cMonth > sMonth) {
            return (cMonth - sMonth) + "个月前";
        } else if (cDate > sDate) {
            return (cDate - sDate) > 1 ? (cDate - sDate) + "天前" : "昨天";
        } else if (cHour > sHour) {
            return (cHour - sHour) + "小时前";
        } else if (cMinuter >= sMinuter) {
            return (cMinuter - sMinuter) < 1 ? "刚刚" : (cMinuter - sMinuter) + "分钟前";
        }
        return "";
    }

    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date strtodate = formatter.parse(strDate, new ParsePosition(0));
        return strtodate;
    }

    public static int compareToCurrentTime(String strDate) {
        Date date = strToDateLong(strDate);
        //当前时间
        long currentTime = System.currentTimeMillis();
        long argTime = date.getTime();
        if (argTime > currentTime) {
            return 0;
        } else {
            return 1;
        }
    }

    public static int getDuration(String start, String end) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long nd = 1000 * 24 * 60 * 60;//1天
        long nh = 1000 * 60 * 60;//1小时
        long nm = 1000 * 60; //1分钟
        try {
            // long ns = 1000;
            // 获得两个时间的毫秒时间差异
            long diff = simpleFormat.parse(end).getTime() - simpleFormat.parse(start).getTime();
            // 计算差多少分钟
            long min = diff / nm;
            System.out.println(min + "min");
            return (int) min;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getDetailDuration(String start, String end) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long nd = 1000 * 24 * 60 * 60;//1天
        long nh = 1000 * 60 * 60;//1小时
        long nm = 1000 * 60; //1分钟
        long ns = 1000;
        long second = 0;
        long min = 0;
        long hour = 0;
        long day = 0;
        try {
            // long ns = 1000;
            // 获得两个时间的毫秒时间差异
            long diff = simpleFormat.parse(end).getTime() - simpleFormat.parse(start).getTime();
            // 计算差多少分钟
            day = diff / nd;// 计算差多少天
            hour = diff % nd / nh;// 计算差多少小时
            min = diff % nd % nh / nm;// 计算差多少分钟
            second = diff % nd % nh % nm / ns;// 计算差多少秒
            if (day > 0)
                return day + "天" + hour + "时" + min + "分" + second + "秒";
            if (hour > 0)
                return hour + "时" + min + "分" + second + "秒";
            if (min > 0)
                return min + "分" + second + "秒";
            if (second > 0)
                return second + "秒";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return day + "天" + hour + "时" + min + "分" + second + "秒";
    }

    public static String positiveTime(String startTime) {//正计时
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long nd = 1000 * 24 * 60 * 60;//1天
        long nh = 1000 * 60 * 60;//1小时
        long nm = 1000 * 60; //1分钟
        long ns = 1000; //1秒钟
        long second = 0;
        long min = 0;
        long hour = 0;
        long day = 0;
        try {
            // 获得两个时间的毫秒时间差异
            long diff = simpleFormat.parse(ToolUtil.getDate()).getTime() - simpleFormat.parse(startTime).getTime();

            day = diff / nd;// 计算差多少天
            hour = diff % nd / nh;// 计算差多少小时
            min = diff % nd % nh / nm;// 计算差多少分钟
            second = diff % nd % nh % nm / ns;// 计算差多少秒
            if (day > 0)
                return day + "天" + hour + "时" + min + "分" + second + "秒";
            if (hour > 0)
                return hour + "时" + min + "分" + second + "秒";
            if (min > 0)
                return min + "分" + second + "秒";
            if (second > 0)
                return second + "秒";
            return day + "天" + hour + "时" + min + "分" + second + "秒";
        } catch (Exception e) {
            e.printStackTrace();
            return day + "天" + hour + "时" + min + "分" + second + "秒";
        }
    }

    public static String countdownTime(String startTime) {//倒计时
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long nd = 1000 * 24 * 60 * 60;//1天
        long nh = 1000 * 60 * 60;//1小时
        long nm = 1000 * 60; //1分钟
        long ns = 1000; //1秒钟
        long second = 0;
        long min = 0;
        long hour = 0;
        long day = 0;
        try {
            // 获得两个时间的毫秒时间差异
            long diff = simpleFormat.parse(startTime).getTime() - simpleFormat.parse(ToolUtil.getDate()).getTime();

            day = diff / nd;// 计算差多少天
            hour = diff % nd / nh;// 计算差多少小时
            min = diff % nd % nh / nm;// 计算差多少分钟
            second = diff % nd % nh % nm / ns;// 计算差多少秒
            if (day > 0)
                return day + "天" + hour + "时" + min + "分" + second + "秒";
            if (hour > 0)
                return hour + "时" + min + "分" + second + "秒";
            if (min > 0)
                return min + "分" + second + "秒";
            if (second > 0)
                return second + "秒";
            return day + "天" + hour + "时" + min + "分" + second + "秒";
        } catch (Exception e) {
            e.printStackTrace();
            return day + "天" + hour + "时" + min + "分" + second + "秒";
        }
    }

    public static String timeStampToString(Timestamp timestamp) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleFormat.format(timestamp);
    }
}
