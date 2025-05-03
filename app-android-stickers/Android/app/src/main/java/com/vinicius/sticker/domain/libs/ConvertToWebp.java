package com.vinicius.sticker.domain.libs;

public class ConvertToWebp {
    static {
        System.loadLibrary("sticker");
    }

    public native String convertToWebp(String inputPath, String command);
}