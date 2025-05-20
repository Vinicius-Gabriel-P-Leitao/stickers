package com.vinicius.sticker.core.exception;

public class StickerSizeFileLimitException extends IllegalStateException {
    private final String stickerPackIdentifier;

    private final String fileName;

    public StickerSizeFileLimitException(String message) {
        super(message);
        this.stickerPackIdentifier = null;
        this.fileName = null;
    }

    public StickerSizeFileLimitException(String message, String stickerPackIdentifier, String fileName) {
        super(message);
        this.stickerPackIdentifier = stickerPackIdentifier;
        this.fileName = fileName;
    }

    public String getStickerPackIdentifier() {
        return stickerPackIdentifier;
    }

    public String getFileName() {
        return fileName;
    }
}
