package com.vinicius.sticker.core.exception;

public class NativeConversionException extends RuntimeException {
    public NativeConversionException(String message) {
        super(message);
    }

    public NativeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
