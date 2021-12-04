package com.example.adham.firebase1_authentication.models;

public class Item {
    private String Iname;
    private int Iimage;


    public Item(String iname, int image) {
        this.Iname = iname;
        this.Iimage = image;


    }

    public String GetItemName() {
        return this.Iname;

    }

    public int GetItemImage() {
        return this.Iimage;
    }


    public String SetIname(String name) {
        return this.Iname = name;

    }
    public int Iimage(int image) {
        return this.Iimage = image;

    }

}
