package com.lenovo.feizai.parking.net;

import android.net.ParseException;

import androidx.annotation.Nullable;

import com.google.gson.JsonParseException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.net.ConnectException;

import retrofit2.HttpException;

/**
 * @author feizai
 * @date 12/22/2020 022 11:02:46 AM
 */
public class ExceptionHandle {

    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int Method_Not_Allowed = 405;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;

    public static ResponeThrowable handleException(Throwable e) {
        ResponeThrowable ex;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ResponeThrowable(e, ERROR.HTTP_ERROR);
            switch (httpException.code()) {
                case UNAUTHORIZED:ex.message = "HTTP 401";break;
                case FORBIDDEN:ex.message = "HTTP 403";break;
                case NOT_FOUND:ex.message = "HTTP 404";break;
                case Method_Not_Allowed:ex.message = "HTTP 405";break;
                case REQUEST_TIMEOUT:ex.message = "HTTP 408";break;
                case GATEWAY_TIMEOUT:ex.message = "HTTP 504";break;
                case INTERNAL_SERVER_ERROR:ex.message = "HTTP 500";break;
                case BAD_GATEWAY:ex.message = "HTTP 502";break;
                case SERVICE_UNAVAILABLE:ex.message = "HTTP 503";break;
                default:
                    ex.message = "网络错误";
                    break;
            }
            return ex;
        } else if (e instanceof ServerException) {
            ServerException resultException = (ServerException) e;
            ex = new ResponeThrowable(resultException, resultException.code);
            ex.message = resultException.message;
            return ex;
        } else if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            ex = new ResponeThrowable(e, ERROR.PARSE_ERROR);
            ex.message = "解析错误";
            return ex;
        } else if (e instanceof ConnectException) {
            ex = new ResponeThrowable(e, ERROR.NETWORD_ERROR);
            ex.message = "连接失败";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ResponeThrowable(e, ERROR.SSL_ERROR);
            ex.message = "证书验证失败";
            return ex;
        } else if (e instanceof ConnectTimeoutException){
            ex = new ResponeThrowable(e, ERROR.TIMEOUT_ERROR);
            ex.message = "连接超时";
            return ex;
        } else if (e instanceof java.net.SocketTimeoutException) {
            ex = new ResponeThrowable(e, ERROR.TIMEOUT_ERROR);
            ex.message = "连接超时";
            return ex;
        }else if (e instanceof java.net.NoRouteToHostException){
            ex = new ResponeThrowable(e, ERROR.No_Route_To_Host);
            ex.message = "没有找到主机";
            return ex;
        }
        else {
            ex = new ResponeThrowable(e, ERROR.UNKNOWN);
            ex.message = "未知错误";
            return ex;
        }
    }

    /**
     * 约定异常
     */
    public class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1001;
        /**
         * 网络错误
         */
        public static final int NETWORD_ERROR = 1002;
        /**
         * 协议出错
         */
        public static final int HTTP_ERROR = 1003;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1005;

        /**
         * 连接超时
         */
        public static final int TIMEOUT_ERROR = 1006;

        /**
         * 连接超时
         */
        public static final int No_Route_To_Host = 1007;
    }

    public static class ResponeThrowable extends Exception {
        private int code;
        private String message;

        public ResponeThrowable(Throwable throwable, int code) {
            super(throwable);
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        @Nullable
        @Override
        public String getMessage() {
            return message;
        }
    }

    public class ServerException extends RuntimeException {
        public int code;
        public String message;
    }
}
