/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

#include <jni.h>
#include <string>
#include <android/log.h>

#include "HandlerJavaException.hpp"

#define LOG_TAG_HANDLER "HandlerJavaException"
#define LOGEH(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_HANDLER, __VA_ARGS__)

void HandlerJavaException::throwNativeConversionException(JNIEnv *env, jclass exClass, const std::string &message) {

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }

    if (exClass == nullptr) {
        jclass fallback = env->FindClass("java/lang/RuntimeException");

        if (fallback != nullptr) {
            env->ThrowNew(fallback, "Classe NativeConversionException não encontrada");
            return;
        }

        env->FatalError("Falha crítica: nenhuma classe de exceção encontrada");
        return;
    }

    env->ThrowNew(exClass, message.c_str());
    logException(message);
}

void HandlerJavaException::logException(const std::string &message) {
    LOGEH("%s", message.c_str());
}
