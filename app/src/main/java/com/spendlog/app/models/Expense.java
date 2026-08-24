package com.spendlog.app.models;

public class Expense {

    private int id;
    private double amount;
    private int categoryId;
    // se llena con el JOIN, no vive en la tabla expenses
    private String categoryName;
    private String description;
    private String date;
    // Double y no double: en null significa que el gasto se guardo sin ubicacion
    private Double latitude;
    private Double longitude;

    public Expense() {
    }

    public Expense(int id, double amount, int categoryId, String categoryName, String description, String date) {
        this.id = id;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // el historial pone el pin solo en los gastos que traen las dos coordenadas
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}
