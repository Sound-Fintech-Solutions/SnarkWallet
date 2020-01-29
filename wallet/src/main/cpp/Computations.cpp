//
// Created by Dmitry on 5/22/2019.
//

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include "gcs.cpp"

#include <algorithm>
extern "C" {


JNIEXPORT jboolean JNICALL
Java_org_bitcoinj_core_neutrino_GCSFilter_intFrom(JNIEnv *env, jobject thiz, jbyteArray arr, jint p,
                                                  jobjectArray arr2, jint N, jbyteArray key, jint a) {

    int len = env->GetArrayLength (arr);
    int len2 = env->GetArrayLength (arr2);
    int keylen = env->GetArrayLength(key);

    uint8_t *buf = new uint8_t[len];
    uint8_t *buf2 = new unsigned char[len2];
    uint8_t *buf3= new unsigned char[keylen];
    env->GetByteArrayRegion (arr, 0, len, reinterpret_cast<jbyte*>(buf));
    env->GetByteArrayRegion (key, 0, keylen, reinterpret_cast<jbyte*>(buf3));

//    jbyteArray dim = (jbyteArray)env->GetObjectArrayElement(arr2, 0);
//    env->GetByteArrayRegion (array, 0, len2, reinterpret_cast<jbyte*>(buf2[0]));
//    env->GetByteArrayRegion (arr2, 0, len2, reinterpret_cast<jbyte*>(buf2));


    int l1 = env -> GetArrayLength(arr2);
    jbyteArray dim = (jbyteArray)env->GetObjectArrayElement(arr2, 0);
    int l = env -> GetArrayLength(dim);

    jbyte **localArray = new jbyte*[l1];
    uint64_t* hashes = new uint64_t[l1];
    for(int i=0; i<l1; ++i){
        jbyteArray oneDim= (jbyteArray)env->GetObjectArrayElement(arr2, i);
        jbyte *element=env->GetByteArrayElements(oneDim, 0);
        int l2 = env -> GetArrayLength(oneDim);
        localArray[i] = new jbyte[l2];
        for(int j=0; j<l2; ++j) {
            localArray[i][j]= element[j];
        }
        hashes[i] = gcs_hash(localArray[i], l2, N, p, buf3);
    }

    GolombDecoder *ty = new GolombDecoder(buf, len, p);



    uint64_t h = hashes[4];

    sort(hashes, hashes+l1);

    uint64_t value = 0;
    int idx = 0;
    for (int i = 0; i < N; i++) {
//    while (!ty->eof()) {
        unsigned int diff = ty->next();
        value += diff;
        while (true) {
            if (idx == l1) {
                return false;
            } else if (value == hashes[idx]) {
                return true;
            } else if (hashes[idx] > value) {
                break;
            }
            idx++;
        }
    }

//    while (!ty->eof()) {
//        unsigned int diff = ty->next();
//        value += diff;
//
//        if (value == hashes[4])
//            return true;
//        else if (value > hashes[4])
//            return false;
//    }

    return false;


}

}
