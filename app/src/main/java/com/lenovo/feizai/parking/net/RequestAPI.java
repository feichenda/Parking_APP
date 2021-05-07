package com.lenovo.feizai.parking.net;

import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.entity.AdImage;
import com.lenovo.feizai.parking.entity.CarInfo;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.entity.CollectionInfo;
import com.lenovo.feizai.parking.entity.Comment;
import com.lenovo.feizai.parking.entity.Customer;
import com.lenovo.feizai.parking.entity.Location;
import com.lenovo.feizai.parking.entity.Merchant;
import com.lenovo.feizai.parking.entity.MerchantChange;
import com.lenovo.feizai.parking.entity.MerchantProperty;
import com.lenovo.feizai.parking.entity.MerchantState;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.entity.ParkingInfo;
import com.lenovo.feizai.parking.entity.ParkingSpace;
import com.lenovo.feizai.parking.entity.Password;
import com.lenovo.feizai.parking.entity.User;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * @author feizai
 * @date 12/21/2020 021 10:02:37 PM
 */
public interface RequestAPI {
    public final static String baseURL = "http://172.21.58.82:8080/Parking_war/";
    public final static String baseImageURL = "http://172.21.58.82:8080/";

//    public final static String baseURL = "http://192.168.43.183:8080/Parking_war/";
//    public final static String baseImageURL = "http://192.168.43.183:8080/";

//    public final static String baseURL = "http://120.78.208.177:8080/Parking/";
//    public final static String baseImageURL = "http://120.78.208.177:8080/";

//    public final static String baseURL = "http://39.108.48.82:8080/Parking/";
//    public final static String baseImageURL = "http://39.108.48.82:8080/";

    @POST("api/user/login")
    Observable<BaseModel> login(@Body User user);

    @POST("api/user/register")
    Observable<BaseModel> register(@Body User user, @Query("phone") String phone);

    @GET("api/user/forget")
    Observable<BaseModel> forget(@Query("user") String user, @Query("phone") String phone, @Query("role") String role);

    @GET("api/user/find")
    Observable<BaseModel> find(@Query("user") String user, @Query("password") String password);

    @Multipart
    @POST("api/merchant/addmerchantinfo")
    Observable<BaseModel> addMerchantInfo(@Query("merchant_str") String merchant_str, @Query("location_str") String location_str, @Query("rates_str") String rates_str, @Query("parkingnumber_str") String parkingnumber_str, @Part MultipartBody.Part[] license, @Part MultipartBody.Part[] image);

    @POST("api/location/selectparking")
    Observable<BaseModel<MerchantProperty>> searchParking(@Body Location location);

    @GET("api/merchant/selectMerchant")
    Observable<BaseModel<MerchantProperty>> searchMerchant(@Query("name") String name);

    @GET("api/merchant/searchParkingSpace")
    Observable<BaseModel<ParkingSpace>> searchParkingSpace(@Query("merchantname") String merchantname);

    @GET("api/merchant/searchspacebyNameandserialnumber")
    Observable<BaseModel<Boolean>> searchSpaceByNameAndSerialnumber(@Query("merchantname") String merchantname, @Query("serialnumber") String serialnumber);

    @POST("api/customer/updateHomeAddressInfo")
    Observable<BaseModel> updatehomeaddressinfo(@Body Customer customer);

    @POST("api/customer/updateCompanyAddressInfo")
    Observable<BaseModel> updateCompanyAddressInfo(@Body Customer customer);

    @GET("api/customer/selectCustomerByUsername")
    Observable<BaseModel<Customer>> selectCustomerByUsername(@Query("name") String name);

    @Multipart
    @POST("api/customer/upadteCustomerAvatar")
    Observable<BaseModel<String>> upadteCustomerAvatar(@Query("username") String username, @Part MultipartBody.Part file);

    @GET("api/car/selectCarByUsername")
    Observable<BaseModel<CarInfo>> selectCarByUsername(@Query("username") String username);

    @GET("api/car/selectNoFreeCarByUsername")
    Observable<BaseModel<String>> selectNoFreeCarByUsername(@Query("username") String username);

    @GET("api/car/selectFreeCarByUsername")
    Observable<BaseModel<String>> selectFreeCarByUsername(@Query("username") String username);

    @GET("api/subscribe/selectIngOrderByUser")
    Observable<BaseModel<String>> selectIngOrderByUser(@Query("username") String username);

    @Multipart
    @POST("api/subscribe/addSubscribeOrder")
    Observable<BaseModel> addSubscribeOrder(@Query("order") String order, @Part MultipartBody.Part uploadFile);

    @Multipart
    @POST("api/subscribe/updateSubsrcibeOrder")
    Observable<BaseModel> updateSubsrcibeOrder(@Query("order") String order, @Part MultipartBody.Part uploadFile);

    @GET("api/subscribe/customerFindOrder")
    Observable<BaseModel<String>> customerFindOrder(@Query("customer") String customer, @Query("year") int year, @Query("month") int month, @Query("type") String type, @Query("index") int index);

    @GET("api/subscribe/merchantFindOrder")
    Observable<BaseModel<String>> merchantFindOrder(@Query("customer") String customer, @Query("year") int year, @Query("month") int month, @Query("type") String type, @Query("index") int index);

    @GET("api/collection/selectCollectionByCustomer")
    Observable<BaseModel<CollectionInfo>> selectCollectionByCustomer(@Query("username") String username);

    @POST("api/collection/addCollection")
    Observable<BaseModel> addCollection(@Body CollectionInfo collectionInfo);

    @POST("api/collection/updateCollection")
    Observable<BaseModel> updateCollection(@Body CollectionInfo collectionInfo);

    @POST("api/collection/deleteCollection")
    Observable<BaseModel> deleteCollection(@Body CollectionInfo collectionInfo);

    @POST("api/car/deleteCar")
    Observable<BaseModel> deleteCar(@Body CarInfo carInfo);

    @GET("api/collection/isCollection")
    Observable<BaseModel> isCollection(@Query("username") String username, @Query("merchant") String merchant);

    @POST("api/car/addCarInfo")
    Observable<BaseModel> addCarInfo(@Body CarInfo carInfo);

    @POST("api/car/updateCarInfo")
    Observable<BaseModel> updateCarInfo(@Body CarInfo carInfo);

    @GET("api/merchant/selectMerchantByUsername")
    Observable<BaseModel<MerchantProperty>> selectMerchantByUsername(@Query("username") String username);

    @GET("api/subscribe/findSubscribeOrderByLicense")
    Observable<BaseModel> findSubscribeOrderByLicense(@Query("license") String license, @Query("merchant") String merchant);

    @GET("api/merchant/findMerchantState")
    Observable<BaseModel<MerchantState>> findMerchantState(@Query("merchant") String merchant);

    @GET("api/merchant/updateParkingState")
    Observable<BaseModel> updateParkingState(@Query("merchant") String merchant, @Query("state") String state);

    @POST("api/merchant/updateParkingSpaceByNameAndSerialnumber")
    Observable<BaseModel> updateParkingSpaceByNameAndSerialnumber(@Body Map<String, String> map);

    @GET("api/subscribe/findOrderByNumber")
    Observable<BaseModel<String>> findOrderByNumber(@Query("ordernumber") String ordernumber);

    @Multipart
    @POST("api/merchant/changeParkingInfo")
    Observable<BaseModel> changeParkingInfo(@Query("change") String change, @Part MultipartBody.Part[] image);

    @POST("api/comment/addComment")
    Observable<BaseModel> addComment(@Body Comment comment);

    @GET("api/comment/selectCommentByMerchant")
    Observable<BaseModel<Comment>> selectCommentByMerchant(@Query("merchant") String merchant, @Query("index") int index);

    @GET("api/customer/updateCustomerPhone")
    Observable<BaseModel<String>> updateCustomerPhone(@Query("username") String username, @Query("phone") String phone);

    @POST("api/user/changepassword")
    Observable<BaseModel> changePassword(@Body Password password);

    @GET("api/user/updateUsername")
    Observable<BaseModel<String>> updateUsername(@Query("oldname") String oldname, @Query("newname") String newname, @Query("role") String role);

    @GET("api/merchant/selectMerchantBaseInfo")
    Observable<BaseModel<Merchant>> selectMerchantBaseInfo(@Query("username") String username);

    @Multipart
    @POST("api/merchant/upadteMerchantAvatar")
    Observable<BaseModel<String>> upadteMerchantAvatar(@Query("username") String username, @Part MultipartBody.Part file);

    @GET("api/merchant/updateMerchantPhone")
    Observable<BaseModel<String>> updateMerchantPhone(@Query("username") String username, @Query("phone") String phone);

    @GET("api/merchant/isMerchantChange")
    Observable<BaseModel<MerchantChange>> isMerchantChange(@Query("oldname") String oldname);

    @Multipart
    @POST("api/merchant/updateMerchantChange")
    Observable<BaseModel> updateMerchantChange(@Query("change") String change, @Part MultipartBody.Part[] image);

    @GET("api/merchant/selectParkingInfo")
    Observable<BaseModel<ParkingInfo>> selectParkingInfo(@Query("merchant") String merchant);

    @GET("api/merchant/updateParkingInfoLink")
    Observable<BaseModel> updateParkingInfoLink(@Query("merchant") String merchant, @Query("phone") String phone, @Query("linkname") String linkname, @Query("QQ") String QQ);

    @GET("api/customer/updateCustomerQQ")
    Observable<BaseModel> updateCustomerQQ(@Query("customer") String customer, @Query("QQ") String QQ);

    @GET("api/merchant/updateMerchantQQ")
    Observable<BaseModel> updateMerchantQQ(@Query("merchant") String merchant, @Query("QQ") String QQ);

    @GET("api/check/addCheckInfoByQRCode")
    Observable<BaseModel> addCheckInfoByQRCode(@Query("orderNumber") String orderNumber);

    @GET("api/check/selectCheckInfoByCar")
    Observable<BaseModel<CheckInfo>> selectCheckInfoByCar(@Query("merchant") String merchant, @Query("car") String car);

    @GET("api/check/isPay")
    Observable<BaseModel<CheckInfo>> isPay(@Query("merchant") String merchant, @Query("car") String car);

    @POST("api/check/updatePayByMoney")
    Observable<BaseModel> updatePayByMoney(@Body CheckInfo checkInfo);

    @GET("api/check/updateByNowCode")
    Observable<BaseModel> updateByNowCode(@Query("orderjson") String orderjson, @Query("checkinfojson") String checkinfojson);

    @POST("api/check/addCheckInfo")
    Observable<BaseModel> addCheckInfo(@Body CheckInfo checkInfo);

    @GET("api/check/clearing")
    Observable<BaseModel<CheckInfo>> clearing(@Query("merchant") String merchant, @Query("car") String car);

    @GET("api/check/updataByScanQRCode")
    Observable<BaseModel> updataByScanQRCode(@Query("orderjson") String orderjson, @Query("checkinfojson") String checkinfojson);

    @POST("api/subscribe/cancelOrderByNumber")
    Observable<BaseModel> cancelOrderByNumber(@Query("orderjson") String orderjson);

    @POST("api/check/selectUsingSpace")
    Observable<BaseModel<CheckInfo>> selectUsingSpace(@Body ParkingSpace space);
}

