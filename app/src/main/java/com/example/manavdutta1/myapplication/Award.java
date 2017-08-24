package com.example.manavdutta1.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by manavdutta1 on 8/15/17.
 */

public class Award implements Parcelable {
    private String category;
    private String typeDescription;
    private String description;
    private Date createdDt;
    private String recipient;
    private Long totalObligation;

    public Award(String category, String typeDescription, String description, Date cDt, String recip, Long totalMoney) {
        this.category = category;
        this.typeDescription = typeDescription;
        this.description = description;
        this.createdDt = cDt;
        this.recipient = recip;
        this.totalObligation = totalMoney;
    }

    public Award(Parcel in) {
        this.category = in.readString();
        this.typeDescription = in.readString();
        this.description = in.readString();
        this.createdDt = new Date(in.readLong());
        this.recipient = in.readString();
        this.totalObligation = in.readLong();
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }


    public Long getTotalObligation() {
        return totalObligation;
    }

    public void setTotalObligation(Long totalObligation) {
        this.totalObligation = totalObligation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDt() {
        return createdDt;
    }

    public void setCreatedDt(Date createdDt) {
        this.createdDt = createdDt;
    }

    private int mData;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.category);
        out.writeString(this.typeDescription);
        out.writeString(this.description);
        out.writeLong(this.createdDt.getTime());
        out.writeString(this.recipient);
        out.writeLong(this.totalObligation);
    }

    public static final Parcelable.Creator<Award> CREATOR
    = new Parcelable.Creator<Award>() {
        public Award createFromParcel(Parcel in) {
            return new Award(in);
        }

        public Award[] newArray(int size) {
            return new Award[size];
        }
    };
}

