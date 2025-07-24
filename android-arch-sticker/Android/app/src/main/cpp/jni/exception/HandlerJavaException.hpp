/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_HANDLERJAVAEXCEPTION_HPP
#define ANDROID_HANDLERJAVAEXCEPTION_HPP

#include <jni.h>
#include <string>

class HandlerJavaException {
public:
    static void throwNativeConversionException(JNIEnv *env, jclass nativeMediaException, const std::string &message);

private:
    static void logException(const std::string &message);
};


#endif //ANDROID_HANDLERJAVAEXCEPTION_HPP
