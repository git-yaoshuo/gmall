package com.yaoshuo.gmall.enums;

public enum PaymentStatus {

    UNPAID("支付中"),
    PAID("已支付"),
    PAY_FAIL("支付失败"),
    ClOSED("已关闭");

    PaymentStatus( String name) {
        this.name = name;
    }

    private String name ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
