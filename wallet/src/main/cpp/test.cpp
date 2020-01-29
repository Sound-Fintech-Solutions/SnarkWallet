//
// Created by Dmitry on 5/22/2019.
//
#include <jni.h>

JNIEXPORT jint JNICALL Java_com_example_Computations_intFromJni () {
    jint b = 12;
    return b;
}