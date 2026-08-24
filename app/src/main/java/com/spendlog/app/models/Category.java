package com.spendlog.app.models;

import androidx.annotation.NonNull;

public class Category {

    private int id;
    private String name;
    private double monthlyLimit;

    public Category() {
    }

    public Category(int id, String name, double monthlyLimit) {
        this.id = id;
        this.name = name;
        this.monthlyLimit = monthlyLimit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    // el Spinner de la pantalla de registro muestra cada opcion con toString(),
    // asi nos ahorramos escribir un adapter propio
    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
