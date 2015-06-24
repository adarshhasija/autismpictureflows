package com.adarshhasija.flows;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adarshhasija on 5/20/15.
 */
public class Item implements Parcelable {

    private String id;
    private String text;
    private byte[] image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(text);
        dest.writeByteArray(image);
    }


    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            //return new Item(in);
            Item item = new Item();
            item.id = in.readString();
            item.text = in.readString();
            item.image = in.createByteArray();
            return item;
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
  /*  private Item(Parcel in) {
        id = in.readString();
        text = in.readString();
        image = in.createByteArray();
    }   */
}
