cmake_minimum_required(VERSION 3.22.1)
project("StickerLib")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden")

add_definitions(-D__STDC_CONSTANT_MACROS -D__STDC_FORMAT_MACROS -D__STDC_LIMIT_MACROS)

set(SHARED_SOURCES
        jni/service/WebpAnimationConverter.cpp
        jni/service/ProcessWebpToAvFrames.cpp
        jni/service/ProcessFramesToFormat.cpp
        jni/service/ProcessInputMedia.cpp
        jni/exception/HandlerJavaException.cpp
)

add_library(ConvertSticker SHARED jni/ConvertSticker.cpp ${SHARED_SOURCES})
target_include_directories(ConvertSticker PRIVATE
        ${CMAKE_SOURCE_DIR}/../include/ffmpeg/
        ${CMAKE_SOURCE_DIR}/../include/libwebp/
        ${CMAKE_SOURCE_DIR}/../include/fmt/
)

add_library(CropMedia SHARED jni/CropMedia.cpp ${SHARED_SOURCES})
target_include_directories(CropMedia PRIVATE
        ${CMAKE_SOURCE_DIR}/../include/ffmpeg/
        ${CMAKE_SOURCE_DIR}/../include/libwebp/
        ${CMAKE_SOURCE_DIR}/../include/fmt/
)

include_directories(${CMAKE_SOURCE_DIR}/../include/ffmpeg/)
include_directories(${CMAKE_SOURCE_DIR}/../include/libwebp/)
include_directories(${CMAKE_SOURCE_DIR}/../include/fmt/)

foreach (lib fmt webp sharpyuv webpmux webpdemux avcodec avformat avutil swscale swresample avdevice)
    if (NOT EXISTS "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/lib${lib}.a" AND
            NOT EXISTS "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/lib${lib}.so")
        message(FATAL_ERROR "Library ${lib} not found for ${ANDROID_ABI}")
    endif ()
endforeach ()

# Libs for fmt
add_library(fmt STATIC IMPORTED)
set_target_properties(
        fmt
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libfmt.a
)

# Libs for FFmpeg
add_library(avcodec SHARED IMPORTED)
set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavcodec.so
        IMPORTED_LINK_INTERFACE_LIBRARIES "avutil"
)

add_library(avformat SHARED IMPORTED)
set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavformat.so
        IMPORTED_LINK_INTERFACE_LIBRARIES "avcodec;avutil"
)

add_library(avutil SHARED IMPORTED)
set_target_properties(
        avutil
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavutil.so
)

add_library(swscale SHARED IMPORTED)
set_target_properties(
        swscale
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libswscale.so
        IMPORTED_LINK_INTERFACE_LIBRARIES "avutil"
)

add_library(swresample SHARED IMPORTED)
set_target_properties(
        swresample
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libswresample.so
        IMPORTED_LINK_INTERFACE_LIBRARIES "avutil"
)

add_library(avdevice SHARED IMPORTED)
set_target_properties(
        avdevice
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavdevice.so
        IMPORTED_LINK_INTERFACE_LIBRARIES "avcodec;avutil"
)

# Libs for libwebp
add_library(webp STATIC IMPORTED)
set_target_properties(
        webp
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libwebp.a
)

add_library(sharpyuv STATIC IMPORTED)
set_target_properties(
        sharpyuv
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libsharpyuv.a
)

add_library(webpmux STATIC IMPORTED)
set_target_properties(
        webpmux
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libwebpmux.a
        IMPORTED_LINK_INTERFACE_LIBRARIES "webp"
)

add_library(webpdemux STATIC IMPORTED)
set_target_properties(
        webpdemux
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libwebpdemux.a
        IMPORTED_LINK_INTERFACE_LIBRARIES "webp"
)

target_link_libraries(ConvertSticker PRIVATE avutil avcodec avformat swscale avdevice swresample webp sharpyuv webpmux webpdemux fmt android log)
target_link_libraries(CropMedia PRIVATE avutil avcodec avformat swscale avdevice swresample webp sharpyuv webpmux webpdemux fmt android log)

