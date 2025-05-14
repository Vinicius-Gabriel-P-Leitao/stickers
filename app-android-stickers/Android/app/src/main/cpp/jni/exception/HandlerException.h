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

#ifndef ANDROID_HANDLEREXCEPTION_H
#define ANDROID_HANDLEREXCEPTION_H

#include <jni.h>
#include <string>

class HandlerException {
public:
    static void throwException(JNIEnv *env, jclass exClass, const std::string &message);

private:
    static void logException(const std::string &message);
};


#endif //ANDROID_HANDLEREXCEPTION_H
