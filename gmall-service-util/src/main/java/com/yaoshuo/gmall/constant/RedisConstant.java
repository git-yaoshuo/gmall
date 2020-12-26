package com.yaoshuo.gmall.constant;

import java.io.Serializable;

public class RedisConstant implements Serializable {

    public static final int CONNECTION_TIME_OUT = 10 * 1000;
    public static final long MAX_WAIT_MILLIS = 10 * 1000L;

    public static final String REDIS_PREFIX_SKU = "sku:";
    public static final String REDIS_SUFFIX_SKU_INFO = ":skuInfo";
    public static final long REDIS_STRING_SKUINFO_EXPIRE_TIME = 24 * 60 * 60L;

    public static final String REDIS_SUFFIX_LOCK = ":lock";
    public static final long REDIS_STRING_LOCK_EXPIRE_TIME = 10 * 1000L;

    public static final String REDIS_PREFIX_USER = "user:";
    public static final String REDIS_SUFFIX_USER_INFO = ":userInfo";
    public static final int REDIS_USER_LOGIN_TIME_OUT = 24 * 60 * 60;

    public static final String REDIS_PREFIX_CART = "cart:";
    public static final String REDIS_SUFFIX_CART_SKU_INFO = ":cart_skuInfo";
    public static final String REDIS_SUFFIX_CHECKDE_CART_SKU_INFO = ":checkedCart_skuInfo";

    public static final String REDIS_PREFIX_ORDER = "order:";
    public static final String REDIS_SUFFIX_ORDER_TRADE_NO = ":tradeNo";



}
