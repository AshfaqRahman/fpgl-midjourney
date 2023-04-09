package com.bdtopcoder.chatgpt.chatmodel;

import android.graphics.Bitmap;

public class Message {

    public static String SEND_BY_ME = "me";
    public static String SEND_BY_BOT = "bot";

    Bitmap bitmap;

    String message;
    String sentBy;

    public Message(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Message(Bitmap bitmap, String sentBy) {
        this.bitmap = bitmap;
        this.sentBy = sentBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
} // Message End Here =========
