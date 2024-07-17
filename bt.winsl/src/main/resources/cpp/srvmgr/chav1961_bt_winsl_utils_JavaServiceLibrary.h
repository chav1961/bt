/* DO NOT EDIT THIS FILE - it is machine generated */
#include "jni.h"
/* Header for class chav1961_bt_winsl_utils_JavaServiceLibrary */

#ifndef _Included_chav1961_bt_winsl_utils_JavaServiceLibrary
#define _Included_chav1961_bt_winsl_utils_JavaServiceLibrary
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    installService
 * Signature: (Lchav1961/bt/winsl/utils/JavaServiceDescriptor;)I
 */
JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_installService
  (JNIEnv *, jclass, jobject);

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    updateService
 * Signature: (Lchav1961/bt/winsl/utils/JavaServiceDescriptor;)I
 */
JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_updateService
  (JNIEnv *, jclass, jobject);

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    enumServices
 * Signature: (II)[Lchav1961/bt/winsl/utils/ServiceEnumDescriptor;
 */
JNIEXPORT jobjectArray JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_enumServices
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    queryService
 * Signature: (Ljava/lang/String;)Lchav1961/bt/winsl/utils/JavaServiceDescriptor;
 */
JNIEXPORT jobject JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_queryService
  (JNIEnv *, jclass, jstring);

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    removeService
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_removeService
  (JNIEnv *, jclass, jstring);

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    prepareService
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_getServiceRequest
(JNIEnv*, jclass);


/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    prepareService
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_prepareService
  (JNIEnv *, jclass, jstring, jobject);

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    unprepareService
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_unprepareService
  (JNIEnv *, jclass);

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    print2ServiceLog
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_print2ServiceLog
(JNIEnv*, jclass, jstring, jstring);


#ifdef __cplusplus
}
#endif
#endif
