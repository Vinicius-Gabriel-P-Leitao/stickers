/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.core.pattern;

public class CallbackResult<T> {

    public enum Status {
        SUCCESS, FAILURE, WARNING, DEBUG
    }

    private Status status;
    private T data;
    private Exception error;
    private String warningMessage;

    private String debugMessage;

    public static <T> CallbackResult<T> success(T data) {
        CallbackResult<T> callbackResult = new CallbackResult<>();
        callbackResult.status = Status.SUCCESS;
        callbackResult.data = data;
        return callbackResult;
    }

    public static <T> CallbackResult<T> failure(Exception error) {
        CallbackResult<T> callbackResult = new CallbackResult<>();
        callbackResult.status = Status.FAILURE;
        callbackResult.error = error;
        return callbackResult;
    }

    public static <T> CallbackResult<T> warning(String warningMessage) {
        CallbackResult<T> callbackResult = new CallbackResult<>();
        callbackResult.status = Status.WARNING;
        callbackResult.warningMessage = warningMessage;
        return callbackResult;
    }

    public static <T> CallbackResult<T> debug(String debugMessage) {
        CallbackResult<T> callbackResult = new CallbackResult<>();
        callbackResult.status = Status.DEBUG;
        callbackResult.debugMessage = debugMessage;
        return callbackResult;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isFailure() {
        return status == Status.FAILURE;
    }

    public boolean isWarning() {
        return status == Status.WARNING;
    }

    public boolean isDebug() {
        return status == Status.DEBUG;
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public String getDebugMessage() {
        return debugMessage;
    }
}
