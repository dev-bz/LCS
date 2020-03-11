cmake_minimum_required(VERSION 3.14)
project(LCS)
add_library(hello-jni SHARED hello-jni.c)
INSTALL(TARGETS hello-jni DESTINATION ${PROJECT_SOURCE_DIR}/arm64-v8a)