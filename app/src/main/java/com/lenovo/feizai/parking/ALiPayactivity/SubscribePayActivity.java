package com.lenovo.feizai.parking.ALiPayactivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.customerfragment.FirstOrderInfoFragment;
import com.lenovo.feizai.parking.customerfragment.LengthenOrderInfoFragment;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 重要说明：
 * <p>
 * 本 Demo 只是为了方便直接向商户展示支付宝的整个支付流程，所以将加签过程直接放在客户端完成
 * （包括 OrderInfoUtil2_0_HK 和 OrderInfoUtil2_0）。
 * <p>
 * 在真实 App 中，私钥（如 RSA_PRIVATE 等）数据严禁放在客户端，同时加签过程务必要放在服务端完成，
 * 否则可能造成商户私密数据泄露或被盗用，造成不必要的资金损失，面临各种安全风险。
 * <p>
 * Warning:
 * <p>
 * For demonstration purpose, the assembling and signing of the request parameters are done on
 * the client side in this demo application.
 * <p>
 * However, in practice, both assembling and signing must be carried out on the server side.
 */
public class SubscribePayActivity extends AppCompatActivity {

    /**
     * 用于支付宝支付业务的入参 app_id。
     */
    public static final String APPID = "2021000117626664";

    /**
     * 用于支付宝账户登录授权业务的入参 pid。
     */
    public static final String PID = "2088621955441249";

    /**
     * 用于支付宝账户登录授权业务的入参 target_id。
     */
    public static final String TARGET_ID = "isgnkg2299@sandbox.com";

    /**
     * pkcs8 格式的商户私钥。
     * <p>
     * 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个，如果两个都设置了，本 Demo 将优先
     * 使用 RSA2_PRIVATE。RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议商户使用
     * RSA2_PRIVATE。
     * <p>
     * 建议使用支付宝提供的公私钥生成工具生成和获取 RSA2_PRIVATE。
     * 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1
     */
    public static final String RSA2_PRIVATE = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDruEPyh8z9Z7I+RXs2SMLOZ56SXL9PWyh+SSeefGhwTAQkRpOWtLohrDIjXESmhgWtagrl7y3ggU30ZsNswJ+kcizOo0CMRgfI6Z8RQlinWRWBARY+te0wQWBds9f07O1/HtrGkyaypDhdEI53EMJCb/PyxjE7XVoc95+ZPbXAuGxZ1CLS6dyrYLZy69ie1CWtUsB2Ndt0rqzG8bqw1eaXIQSZgI1vByEuuoAQmDpL8wmAEpWPV8T7hkQ8C/97lZMqI/0Segr3AIA66I1/9FLtVZ9+4DYvFK2BXR5XSbykK2j2Tk0q95jWJ9cMELgimBxVUOUG8u+iTnQZquWS+AXtAgMBAAECggEAZeKMg/0Mro8urNnNauZCRYLmOG8cRcmqFAO8uzUc00UYGTHkQyS0uy85sK0GA1A0jgMi2MXr93kMX9I3L/+GQUcUdPB9SuROIxpPpd8egJ7xu4PjwJFUByKK65V7h6MM+IA2odQiyt5OenNYkMBOpvbK46mkx/sOoyyo8EyLDdQUgUr2ZRZ69XqbQH7sAEKSpB0DUW95w24FUkUZvuSt675i8T13ISFxsCFvcfCFpN3bwGIvk8Oc+YKTAMn3UIVL7NdzBhywi3VaoWhu8SpSm+WqzH70pHFY9y0OHghr6PIJnSjhnCzq9PtcUACIedpOOsX5RQ4lyAwFWC2g+vZz4QKBgQD/w8s4Z2ZwughDAS6nF1lPWs0MgeOhgpHFTum433b+0Qnw6xgMRY66dXI24Tb9LsK7JiO/SkLY5V6B1VQ6jvn/MidSuKVuCvwXZ7bSu6n0skHk8AdnVTscPyQHZdYw8L1GjLpbs/dseezPTOTuDbIkQ8ReMi9aa4PoEF4UrZrdhQKBgQDr78DIXe5w7DULkYfea6F0XFcFdTaWpHBrlS6NFTpjI2pr/P42nekyl2HdhSmN96SlmGiR6XygtId+/RcKJ5tZGS/KbaSVL0YZ1PuqSUye09dhW7T7m/QR9qAs9Q+4VTd4wM00qnIBj3pzk6PDsqM3+s8ojbfIF6Qwagt06vjfSQKBgHFP4jnz/XuVv9FHtc6LVqMEnHGdHidQ/JoIsx2Xy1VrcVcOINJXArvzCo3ry3vx+o4FgLLgCKulOvCUfcOP0oxQQQGyEoClVzhbV7EXOV3Sl8UA3pbWOODzzFeGs5VtIr4d3PaBze1+Ov9AlvAsmy7b4yTEYRxTI9ZtYfERVNotAoGAKHjGDxjd0A0UxFlAf0zjMzyryMnkhU7L4giDJbxgeaUXmlKZoPDRkdYZ+Y/gIvQ6EBXmyjDNKhwNqiDsFNfadBw38HeCZfzn/4JjcUCFnjEknxTGJOsJoRM3Qr7+5VXo4BPin4glalMcZDsNOoKoMPAbHQ/CWNaFWbC3KmUsL9ECgYAwMC7s1pMMNBySUOdsYTPS7Pez2UGH6RwVWar0cJEFPWSuXe25tXpgtbol/sFjqeOEAJMGTybWL1gZYi4Pzo8nNgMLqqpexZJ73oN7gUuSvXMcKkL3iABHVEVby8uEsvcJBpTD817sUksYwvfhD9iZ3O1Z1NjSJQ3VVGXLLMiEqA==";
    public static final String RSA_PRIVATE = "";

    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Log.e("tag", payResult.getResult());
                        String[] split = payResult.getResult().split(",");

                        for (int i = 0; i < split.length; i++) {
                            if (split[i].contains("\"trade_no\"") && flag == false) {//提取订单号
                                String[] split1 = split[i].split("\"");
                                order.setOrderNumber(split1[3]);
                            }
                            if (split[i].contains("timestamp") && flag == false) {//提取支付时间
                                String[] split1 = split[i].split("\"");
                                order.setStartDate(Timestamp.valueOf(split1[3]));
                            }
                        }

                        FragmentManager fragmentmanager = getSupportFragmentManager();
                        FragmentTransaction ft = fragmentmanager.beginTransaction();
                        FirstOrderInfoFragment firstOrderInfoFragment = new FirstOrderInfoFragment();
                        LengthenOrderInfoFragment lengthenOrderInfoFragment = new LengthenOrderInfoFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("order", GsonUtil.GsonString(order));
                        if (flag) {
                            lengthenOrderInfoFragment.setArguments(bundle);
                            ft.replace(R.id.container, lengthenOrderInfoFragment);
                        }else {
                            firstOrderInfoFragment.setArguments(bundle);
                            ft.replace(R.id.container, firstOrderInfoFragment);
                        }
                        ft.commitAllowingStateLoss();
                    } else {
                        EventBus.getDefault().postSticky(new MessageEvent("failed"));
                        finish();
                    }
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

    private Order order;
    private float price;
    private boolean flag;//用来区分是否为延长预约的支付，true为延长的订单支付，false为第一次预约的订单支付

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        StatusBarUtil.setStatusBarMode(this,true, R.color.color_ffffff);
        Intent intent = getIntent();
        String json = intent.getStringExtra("json");
        price = intent.getFloatExtra("price", 0);
        flag = intent.getBooleanExtra("flag", false);
        order = GsonUtil.GsonToBean(json, Order.class);
        payV2(order);

    }

    /**
     * 支付宝支付业务示例
     */
    public void payV2(Order order) {
        if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
            showAlert(this, getString(R.string.error_missing_appid_rsa_private));
            return;
        }

        /*
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo 的获取必须来自服务端；
         */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, order.getMerchantName() + "-停车位:" + order.getSpace() + "预约费用", String.valueOf(price), rsa2);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
        final String orderInfo = orderParam + "&" + sign;

        final Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(SubscribePayActivity.this);

                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    private static void showAlert(Context ctx, String info) {
        MaterialDialog dialog = new MaterialDialog(ctx, MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "Message");
        dialog.message(null, info, null);
        dialog.positiveButton(R.string.confirm,null,materialDialog -> {
            return null;
        });
        dialog.show();
    }
}
