package com.hudyma.CarRental2024.constants;

public enum CarClass {

    PREMIUM("Преміум"),
    MINI("Міні"),
    ECONOMY("Економ"),
    ESTATE("Універсал"),
    SUV("Кросовер"),
    EXCLUSIVE("Ексклюзив");

    public final String str;

    CarClass(String str) {
        this.str = str;
    }
}
