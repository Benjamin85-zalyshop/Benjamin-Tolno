package com.sr;

import android.content.Context;

public class SrPrinter {
    private static SrPrinter instance;

    public static SrPrinter getInstance(Context context) {
        if (instance == null) {
            instance = new SrPrinter();
        }
        return instance;
    }

    public void setAlignment(int alignment) {}
    public void setTextBold(boolean bold) {}
    public void setTextSize(float size) {}
    public void printText(String text) {}
    public void nextLine(int lines) {}
}
