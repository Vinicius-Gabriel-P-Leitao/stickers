cmake_minimum_required(VERSION 3.22.1)
project("sticker")

include_directories(${CMAKE_SOURCE_DIR}/vendor/ffmpeg/include/${ANDROID_ABI})

add_library(${CMAKE_PROJECT_NAME} SHARED sticker.cpp)
add_definitions(-D__STDC_CONSTANT_MACROS -D__STDC_FORMAT_MACROS -D__STDC_LIMIT_MACROS)

add_library(avcodec SHARED IMPORTED)
set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/vendor/ffmpeg/lib/${ANDROID_ABI}/libavcodec.so
)

add_library(avformat SHARED IMPORTED)
set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/vendor/ffmpeg/lib/${ANDROID_ABI}/libavformat.so
)

add_library(avutil SHARED IMPORTED)
set_target_properties(
        avutil
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/vendor/ffmpeg/lib/${ANDROID_ABI}/libavutil.so
)

add_library(swscale SHARED IMPORTED)
set_target_properties(
        swscale
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/vendor/ffmpeg/lib/${ANDROID_ABI}/libswscale.so
)

add_library(swresample SHARED IMPORTED)
set_target_properties(
        swresample
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/vendor/ffmpeg/lib/${ANDROID_ABI}/libswresample.so
)

add_library(avdevice SHARED IMPORTED)
set_target_properties(
        avdevice
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/vendor/ffmpeg/lib/${ANDROID_ABI}/libavdevice.so
)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        avcodec
        avformat
        avutil
        swscale
        swresample
        avdevice
        android
        log
)