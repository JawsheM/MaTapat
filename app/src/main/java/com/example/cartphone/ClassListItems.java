package com.example.cartphone;

public class ClassListItems {
    public String productName;
    public int quantity;
    public float subTotal;
    public ClassListItems(String productName, int quantity, float subTotal){
        this.productName = productName;
        this.quantity = quantity;
        this.subTotal = subTotal;
    }
    public String getProductName(){
        return productName;
    }
    public int getquantity(){
        return quantity;
    }
    public float getsubTotal(){
        return subTotal;
    }

}
