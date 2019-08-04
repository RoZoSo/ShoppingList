package de.codeyourapp.shoppinglist;

public class ShoppingMemo {

    // main vars of this class
    private String product;
    private int quantity;
    private long id;
    private boolean checked;


    // Constructor of entered data
    public ShoppingMemo(String product, int quantity, long id, boolean checked) {
        this.product = product;
        this.quantity = quantity;
        this.id = id;
        this.checked = checked;
    }


    // set and get of product
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    // set and get of quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // set and get of id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // set and get of checked
    public boolean isChecked() {
        return checked;
    }

    public void setChecked (boolean checked) {
        this.checked = checked;
    }

    // output of quantity and product
    @Override
    public String toString() {
        String output = quantity + " x " + product;

        return output;
    }
}