cmake_minimum_required(VERSION 3.22.1)
project("sticker")
add_library(${CMAKE_PROJECT_NAME} SHARED sticker.cpp)

set(FFMPEG_BASE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/vendor/ffmpeg)
if (ANDROID_ABI)
    set(FFMPEG_ARCH_DIR ${FFMPEG_BASE_DIR}/${ANDROID_ABI})
else ()
    message(FATAL_ERROR "ANDROID_ABI não está definido. Você está mesmo usando o NDK?")
endif ()

include_directories(${FFMPEG_ARCH_DIR}/include)
link_directories(${FFMPEG_ARCH_DIR}/lib)
target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        libavutil
        libavdevice
        libavcodec
        libavformat
        android
        atomic
        log
        m
)
