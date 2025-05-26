cmake_minimum_required(VERSION 3.22.1)
project("sticker")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden")

file(GLOB_RECURSE SOURCES
        "jni/*.cpp"
)

include_directories(${CMAKE_SOURCE_DIR}/../include/ffmpeg/)
include_directories(${CMAKE_SOURCE_DIR}/../include/libwebp/)
include_directories(${CMAKE_SOURCE_DIR}/../include/fmt/)

add_library(${CMAKE_PROJECT_NAME} SHARED ${SOURCES})
add_library(${CMAKE_PROJECT_SERVICE_WEBP} SHARED ${SOURCES})

add_definitions(-D__STDC_CONSTANT_MACROS -D__STDC_FORMAT_MACROS -D__STDC_LIMIT_MACROS)

# Libs do fmt
add_library(fmt STATIC IMPORTED)
set_target_properties(
        fmt
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libfmt.a
)

# Libs do ffmepg
add_library(avcodec SHARED IMPORTED)
set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavcodec.so
)

add_library(avformat SHARED IMPORTED)
set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavformat.so
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
)

add_library(swresample SHARED IMPORTED)
set_target_properties(
        swresample
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libswresample.so
)

add_library(avdevice SHARED IMPORTED)
set_target_properties(
        avdevice
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavdevice.so
)

# Libs do libwebp
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
)

add_library(webpdemux STATIC IMPORTED)
set_target_properties(
        webpdemux
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libwebpdemux.a
)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        ${CMAKE_PROJECT_SERVICE_WEBP}
        avutil
        avcodec
        avformat
        swscale
        avdevice
        swresample
        webpdemux
        sharpyuv
        webpmux
        webp
        fmt
        android
        log
)