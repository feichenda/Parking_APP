package com.lenovo.feizai.parking.net;

import android.content.Context;
import android.text.TextUtils;

import com.lenovo.feizai.parking.entity.CarInfo;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.entity.CollectionInfo;
import com.lenovo.feizai.parking.entity.Comment;
import com.lenovo.feizai.parking.entity.Customer;
import com.lenovo.feizai.parking.entity.Location;
import com.lenovo.feizai.parking.entity.Merchant;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.entity.ParkingSpace;
import com.lenovo.feizai.parking.entity.Password;
import com.lenovo.feizai.parking.entity.User;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author feizai
 * @date 12/21/2020 021 10:05:39 PM
 */
public class RetrofitClient {
    private RequestAPI api;
    private Retrofit retrofit;
    private static OkHttpClient okHttpClient;
    public static String baseUrl = RequestAPI.baseURL;
    private static Context mContext;
    private static String mUrl;
    private static Map<String, String> mHeaders;

    private static final int DEFAULT_TIMEOUT = 60;

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create());

    private static OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
            .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    private RetrofitClient() {
    }

    private RetrofitClient(Context context, String url, Map<String, String> headers) {
        //url为空，则默认使用baseUrl
        if (TextUtils.isEmpty(url)) {
            url = baseUrl;
        }

        okHttpClient = httpBuilder.build();

        //创建retrofit
        retrofit = builder.client(okHttpClient).baseUrl(url).build();
        if (api == null) {
            api = create(RequestAPI.class);
        }
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    private <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }

    public static RetrofitClient getInstance(Context context) {
        if (context != null)
            mContext = context;
        return RetrofitClientHolder.sInstance;
    }

    public static RetrofitClient getInstance(Context context, String url) {
        if (context != null)
            mContext = context;
        if (url != null)
            mUrl = url;
        return RetrofitClientHolder.sInstance;
    }

    public static RetrofitClient getInstance(Context context, String url, Map<String, String> headers) {
        if (context != null)
            mContext = context;
        if (url != null)
            mUrl = url;
        if (headers != null)
            mHeaders = headers;
        return RetrofitClientHolder.sInstance;
    }

    private static class RetrofitClientHolder {
        private static final RetrofitClient sInstance = new RetrofitClient(mContext, mUrl, mHeaders);
    }

    public void destony() {
        retrofit = null;
        okHttpClient = null;
    }

    //处理线程调度的变换
    ObservableTransformer schedulersTransformer = new ObservableTransformer() {
        @Override
        public ObservableSource apply(Observable upstream) {
            return ((Observable) upstream).subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    };

    public static class HttpResponseFunc<T> implements Function<Throwable, Observable<T>> {
        @Override
        public Observable<T> apply(Throwable throwable) throws Exception {

            return Observable.error(ExceptionHandle.handleException(throwable));
        }
    }

    public void login(User user, Observer<?> observer) {
        api.login(user).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void register(User user, String phone, Observer<?> observer) {
        api.register(user, phone).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void forget(String user, String phone, String role, Observer<?> observer) {
        api.forget(user, phone, role).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void find(String user, String password, Observer<?> observer) {
        api.find(user, password).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addmerchantinfo(String merchant, String location, String rates, String parkingnumber, MultipartBody.Part[] license, MultipartBody.Part[] image, Observer<?> observer) {
        api.addMerchantInfo(merchant, location, rates, parkingnumber, license, image).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void searchParking(Location location, Observer<?> observer) {
        api.searchParking(location).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void searchMerchant(String merchantname, Observer<?> observer) {
        api.searchMerchant(merchantname).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void searchParkingSpace(String name, Observer<?> observer) {
        api.searchParkingSpace(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void searchSpaceByNameAndSerialnumber(String name, String serialnumber, Observer<?> observer) {
        api.searchSpaceByNameAndSerialnumber(name, serialnumber).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updatehomeaddressinfo(Customer customer, Observer<?> observer) {
        api.updatehomeaddressinfo(customer).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateCompanyAddressInfo(Customer customer, Observer<?> observer) {
        api.updateCompanyAddressInfo(customer).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectCustomerByUsername(String username, Observer<?> observer) {
        api.selectCustomerByUsername(username).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void upadteCustomerAvatar(String name, MultipartBody.Part file, Observer<?> observer) {
        api.upadteCustomerAvatar(name, file).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectCarByUsername(String name, Observer<?> observer) {
        api.selectCarByUsername(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectNoFreeCarByUsername(String name, Observer<?> observer) {
        api.selectNoFreeCarByUsername(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectFreeCarByUsername(String name, Observer<?> observer) {
        api.selectFreeCarByUsername(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectIngOrderByUser(String name, Observer<?> observer) {
        api.selectIngOrderByUser(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addSubscribeOrder(String order, MultipartBody.Part uploadFile, Observer<?> observer) {
        api.addSubscribeOrder(order, uploadFile).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateSubsrcibeOrder(String order, MultipartBody.Part uploadFile, Observer<?> observer) {
        api.updateSubsrcibeOrder(order, uploadFile).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void customerFindOrder(String name, int year, int month, String type, int index, Observer<?> observer) {
        api.customerFindOrder(name, year, month, type, index).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void merchantFindOrder(String name, int year, int month, String type, int index, Observer<?> observer) {
        api.merchantFindOrder(name, year, month, type, index).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectCollectionByCustomer(String name, Observer<?> observer) {
        api.selectCollectionByCustomer(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addCollection(CollectionInfo collectionInfo, Observer<?> observer) {
        api.addCollection(collectionInfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateCollection(CollectionInfo collectionInfo, Observer<?> observer) {
        api.updateCollection(collectionInfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void deleteCollection(CollectionInfo collectionInfo, Observer<?> observer) {
        api.deleteCollection(collectionInfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void deleteCar(CarInfo carInfo, Observer<?> observer) {
        api.deleteCar(carInfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void isCollection(String username, String merchant, Observer<?> observer) {
        api.isCollection(username, merchant).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addCarInfo(CarInfo carInfo, Observer<?> observer) {
        api.addCarInfo(carInfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateCarInfo(CarInfo carInfo, Observer<?> observer) {
        api.updateCarInfo(carInfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectMerchantByUsername(String name, Observer<?> observer) {
        api.selectMerchantByUsername(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void findMerchantState(String name, Observer<?> observer) {
        api.findMerchantState(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateParkingState(String name, String state, Observer<?> observer) {
        api.updateParkingState(name, state).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateParkingSpaceByNameAndSerialnumber(Map<String, String> spaces, Observer<?> observer) {
        api.updateParkingSpaceByNameAndSerialnumber(spaces).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void findOrderByNumber(String number, Observer<?> observer) {
        api.findOrderByNumber(number).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void changeParkingInfo(String json, MultipartBody.Part[] parts, Observer<?> observer) {
        api.changeParkingInfo(json, parts).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addComment(Comment comment, Observer<?> observer) {
        api.addComment(comment).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectCommentByMerchant(String merchant, int index, Observer<?> observer) {
        api.selectCommentByMerchant(merchant, index).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateCustomerPhone(String username, String phone, Observer<?> observer) {
        api.updateCustomerPhone(username, phone).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void changePassword(Password password, Observer<?> observer) {
        api.changePassword(password).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateUsername(String oldname, String newname, String role, Observer<?> observer) {
        api.updateUsername(oldname, newname, role).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectMerchantBaseInfo(String username, Observer<?> observer) {
        api.selectMerchantBaseInfo(username).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void upadteMerchantAvatar(String merchant, MultipartBody.Part file, Observer<?> observer) {
        api.upadteMerchantAvatar(merchant, file).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateMerchantPhone(String merchant, String phone, Observer<?> observer) {
        api.updateMerchantPhone(merchant, phone).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void isMerchantChange(String oldname, Observer<?> observer) {
        api.isMerchantChange(oldname).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateMerchantChange(String json, MultipartBody.Part[] parts, Observer<?> observer) {
        api.updateMerchantChange(json, parts).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectParkingInfo(String merchant, Observer<?> observer) {
        api.selectParkingInfo(merchant).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateParkingInfoLink(String merchant, String phone, String linkname, String QQ, Observer<?> observer) {
        api.updateParkingInfoLink(merchant, phone, linkname, QQ).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateCustomerQQ(String customer, String QQ, Observer<?> observer) {
        api.updateCustomerQQ(customer, QQ).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateMerchantQQ(String merchant, String QQ, Observer<?> observer) {
        api.updateMerchantQQ(merchant, QQ).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addCheckInfoByQRCode(String json, Observer<?> observer) {
        api.addCheckInfoByQRCode(json).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectCheckInfoByCar(String merchant, String car, Observer<?> observer) {
        api.selectCheckInfoByCar(merchant, car).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void isPay(String merchant, String car, Observer<?> observer) {
        api.isPay(merchant, car).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updatePayByMoney(CheckInfo info, Observer<?> observer) {
        api.updatePayByMoney(info).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updateByNowCode(String order, String checkinfo, Observer<?> observer) {
        api.updateByNowCode(order, checkinfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addCheckInfo(CheckInfo checkInfo, Observer<?> observer) {
        api.addCheckInfo(checkInfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void clearing(String merchant, String car, Observer<?> observer) {
        api.clearing(merchant, car).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void updataByScanQRCode(String order, String checkinfo, Observer<?> observer) {
        api.updataByScanQRCode(order, checkinfo).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void cancelOrderByNumber(String order, Observer<?> observer) {
        api.cancelOrderByNumber(order).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectUsingSpace(ParkingSpace space, Observer<?> observer) {
        api.selectUsingSpace(space).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void isExistParkingInfo(String name, Observer<?> observer) {
        api.isExistParkingInfo(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void isSubscribing(String name, String car, Observer<?> observer) {
        api.isSubscribing(name, car).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void selectParkingInfoByName(String name, Observer<?> observer) {
        api.selectParkingInfoByName(name).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }

    public void addchengmerchantinfo(String oldname, String merchant, String location, String rates, String parkingnumber, MultipartBody.Part[] license, MultipartBody.Part[] image, Observer<?> observer) {
        api.addchengmerchantinfo(oldname, merchant, location, rates, parkingnumber, license, image).compose(schedulersTransformer).onErrorResumeNext(new HttpResponseFunc<>()).subscribe(observer);
    }
}
