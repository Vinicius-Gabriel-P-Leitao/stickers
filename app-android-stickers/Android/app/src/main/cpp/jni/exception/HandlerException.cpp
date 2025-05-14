/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */
#include <jni.h>
#include <string>
#include <android/log.h>

#include "HandlerException.h"

#define LOG_TAG_HANDLER "HandlerException"
#define LOGEH(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_HANDLER, __VA_ARGS__)

void HandlerException::throwException(JNIEnv *env, jclass exClass, const std::string &message) {

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

void HandlerException::logException(const std::string &message) {
    LOGEH("%s", message.c_str());
}
