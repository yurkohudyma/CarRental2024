package com.hudyma.CarRental2024.constants;

public enum OrderStatus {

    REQUESTED ("Замовлено"),
    CONFIRMED ("Підтверджено"),
    PAID ("Сплачено"),
    DECLINED ("Відхилено"),
    CANCELLED ("Скасовано"),
    COMPLETE ("Завершено");

    public final String str;


    OrderStatus(String str) {
        this.str = str;
    }
}
