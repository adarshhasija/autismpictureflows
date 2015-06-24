package com.adarshhasija.flows;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adarshhasija on 5/21/15.
 */
public class Flow implements Parcelable {

    private String id;
    private String text;
    private byte[] image;
    private String[] items;
    private boolean writeAccess;

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

    public String[] getItems() {
        return items;
    }

    public void setItems(String[] items) {
        this.items = items;
    }

    public boolean isWriteAccess() {
        return writeAccess;
    }

    public void setWriteAccess(boolean writeAccess) {
        this.writeAccess = writeAccess;
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
        dest.writeArray(items);
    }


    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Flow> CREATOR = new Parcelable.Creator<Flow>() {
        public Flow createFromParcel(Parcel in) {
            //return new Item(in);
            Flow flow = new Flow();
            flow.id = in.readString();
            flow.text = in.readString();
            flow.image = in.createByteArray();
            flow.items = in.createStringArray();
            return flow;
        }

        public Flow[] newArray(int size) {
            return new Flow[size];
        }
    };
}
