package com.hudyma.CarRental2024.constants;

public enum CarPropulsion {
    HYDROGEN ("Водень"),
    DIESEL("Дизель"),
    BENZINE("Бензин"),
    ELECTRIC("Електро"),
    HYBRID("Гібрид");

    public final String str;

    CarPropulsion(String str) {
        this.str = str;
    }
}
