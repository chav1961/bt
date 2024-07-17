// dllmain.cpp : Defines the entry point for the DLL application.

#include "pch.h"
#include "jni.h"
#include "chav1961_bt_winsl_utils_JavaServiceLibrary.h"
#include <malloc.h>
#include <tchar.h>
#include <strsafe.h>

#define RC_START 0
#define RC_PAUSE 1
#define RC_RESUME 2
#define RC_STOP 3
#define RC_UNKNOWN 4

const char* NO_ADMIN_RIGHTS = "You have no admin rights to call this method";
const char* SC_MANAGER_NOT_AVAILABLE = "SC manager is not available to manipulate service databases";
const char* SERVICE_CREATION_FAILURE_NO_SERVICE_NAME = "Service creation failure (no service name defined)";
const char* SERVICE_CREATION_FAILURE_START_DISPATCHER_FAILURE = "Service creation failure (StartServiceCtrlDispatcher failed)";
const char* SERVICE_UPDATE_FAILURE = "Service update failure";
const char* SERVICE_REMOVE_FAILURE = "Service remove failure";
const char* SERVICE_NOT_EXISTS = "Service not exists";
const char* SERVICE_STATUS_UNKNOWN = "Service status unknown";
const char* SERVICE_ALREADY_STARTED = "Service already started";
const char* SERVICE_ACCESS_FAILURE = "Service already started";
const TCHAR SERVICE_CREATION_FAILURE_FORMAT[] = TEXT("Service creation failure for [%1!s!]: %2!s!");
const TCHAR SERVICE_UPDATE_FAILURE_FORMAT[] = TEXT("Service update failure for [%1!s!]: %2!s!");
const TCHAR SERVICE_REMOVING_FAILURE_FORMAT[] = TEXT("Service removing failure for [%1!s!]: %2!s!");

JNIEnv                  *envRef;
jobject                 queueRef;
TCHAR                   SVCNAME[1024];
HANDLE                  ghSvcStopEvent = NULL;
SERVICE_STATUS          gSvcStatus;
SERVICE_STATUS_HANDLE   gSvcStatusHandle;


BOOL APIENTRY DllMain( HMODULE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
        case DLL_THREAD_ATTACH:
        case DLL_THREAD_DETACH:
        case DLL_PROCESS_DETACH:
            break;
    }
    return TRUE;
}

LPTSTR buildErrorMessage(DWORD errorMessageId) {
    LPTSTR messageBuffer = NULL;

    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS, NULL, errorMessageId, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR)&messageBuffer, 0, NULL);
    return messageBuffer;
}

LPTSTR buildErrorMessage(LPCTSTR format, ...) {
    LPTSTR result = NULL;
    va_list args = NULL;
    va_start(args, format);

    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_STRING, format, 0, 0, (LPTSTR)&result, 0, &args);
    
    va_end(args);

    return result;
}

BOOL IsElevated() {
    BOOL fRet = FALSE;
    HANDLE hToken = NULL;

    if (OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &hToken)) {
        TOKEN_ELEVATION Elevation;
        DWORD cbSize = sizeof(TOKEN_ELEVATION);
        if (GetTokenInformation(hToken, TokenElevation, &Elevation, sizeof(Elevation), &cbSize)) {
            fRet = Elevation.TokenIsElevated;
        }
    }
    if (hToken) {
        CloseHandle(hToken);
    }
    return fRet;
}

jint fireJavaException(JNIEnv* env, jclass clazz, const char* message) {
    env->ThrowNew(clazz, message);
    return -1;
}

jint fireEnvironmentException(JNIEnv* env, const char *message) {
    return fireJavaException(env, env->FindClass("chav1961/purelib/basic/exceptions/EnvironmentException"), message);
}

jint fireContentException(JNIEnv* env, const char* message) {
    return fireJavaException(env, env->FindClass("chav1961/purelib/basic/exceptions/ContentException"), message);
}

jint fireEnvironmentException(JNIEnv* env, jstring message) {
    jboolean isCopy;
    const char* content = env->GetStringUTFChars(message, &isCopy);
    int result = fireEnvironmentException(env, content);

    if (isCopy == JNI_TRUE) {
        env->ReleaseStringUTFChars(message, content);
    }
    return result;
}

jint fireContentException(JNIEnv* env, jstring message) {
    jboolean isCopy;
    const char* content = env->GetStringUTFChars(message, &isCopy);
    int result = fireContentException(env, content);

    if (isCopy == JNI_TRUE) {
        env->ReleaseStringUTFChars(message, content);
    }
    return result;
}


DWORD extractStringFromJava(JNIEnv* env, jstring value, LPTSTR buffer, int size) {
    if (value != NULL) {
        jboolean isCopy;
        int curSize = size - 1;
        const char* content = env->GetStringUTFChars(value, &isCopy);
        DWORD len;

#if UNICODE
        len = MultiByteToWideChar(CP_UTF8, 0, content, (int)strlen(content), buffer, curSize);
#else
        len = strncpy_s(buffer, curSize, content, strlen(content));
#endif
        buffer[len] = 0;
        if (isCopy == JNI_TRUE) {
            env->ReleaseStringUTFChars(value, content);
        }
        return len;
    }
    else {
        buffer[0] = 0;
        return 0;
    }
}


jstring placeString2Java(JNIEnv* env, LPTSTR buffer) {
    if (buffer == NULL) {
        return NULL;
    }
    else {
#if UNICODE
        int size = (int)_tcslen(buffer);
        int len = 4 * size;
        LPSTR temp = (LPSTR)_malloca(len);
        *(temp + WideCharToMultiByte(CP_UTF8, 0, buffer, size, temp, len, NULL, NULL)) = 0;
        return env->NewStringUTF(temp);
#else
        return env->NewStringUTF(buffer);
#endif
    }
}


DWORD extractStringFromServiceDescriptor(JNIEnv* env, jclass clazz, const char* fieldName, jobject obj, LPTSTR buffer, int size) {
    jfieldID field = env->GetFieldID(clazz, fieldName, "Ljava/lang/String;");
    jstring jstr = (jstring)env->GetObjectField(obj, field);

    if (jstr != NULL) {
        return extractStringFromJava(env, jstr, buffer, size);
    }
    else {
        return 0;
    }
}

DWORD extractDWORDFromServiceDescriptor(JNIEnv* env, jclass clazz, const char* fieldName, jobject obj) {
    jfieldID field = env->GetFieldID(clazz, fieldName, "J");
    return (DWORD)env->GetLongField(obj, field);
}


void storeString2ServiceDescriptor(JNIEnv* env, jclass clazz, const char* fieldName, jobject obj, jstring value) {
    jfieldID field = env->GetFieldID(clazz, fieldName, "Ljava/lang/String;");
    env->SetObjectField(obj, field, value);
}

void storeDWORD2ServiceDescriptor(JNIEnv* env, jclass clazz, const char* fieldName, jobject obj, jlong value) {
    jfieldID field = env->GetFieldID(clazz, fieldName, "J");
    env->SetLongField(obj, field, value);
}


BOOL CmdAddServiceParameterRegistry(LPTSTR szServiceName, LPTSTR lpszServiceDescription, DWORD dwServiceDescriptionLen) {
    HKEY key, subkey;
    BOOL result = FALSE;

    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("SYSTEM\\CurrentControlSet\\Services"), 0, KEY_READ, &key)) {
        if (RegOpenKeyEx(key, szServiceName, 0, KEY_ALL_ACCESS, &subkey)) {
            if (RegSetValueEx(subkey, TEXT("Type"), 0, REG_SZ, (const BYTE *)lpszServiceDescription, dwServiceDescriptionLen)) {
                result = TRUE;
            }
            RegCloseKey(subkey);
        }
        RegCloseKey(key);
    }
    return result;
}


void CmdRemoveServiceParameterRegistry(LPTSTR szServiceName) {
    HKEY key;

    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("SYSTEM\\CurrentControlSet\\Services"), 0, KEY_READ, &key)) {
        RegDeleteKey(key, szServiceName);
        RegCloseKey(key);
    }
}

jstring buildErrorMessage(JNIEnv* env, LPCTSTR format, DWORD errorMessageId, LPTSTR service) {
    LPTSTR err = buildErrorMessage(GetLastError());
    LPTSTR msg = buildErrorMessage(format, service, err);
    jstring result = placeString2Java(env, msg);

    LocalFree(err);
    LocalFree(msg);
    return result;
}

void putQueueValue(JNIEnv* env, jobject queue, int value) {
    jclass intDesc = env->FindClass("java/lang/Integer");
    jmethodID valueOf = env->GetStaticMethodID(intDesc, "valueOf", "(I)Ljava/lang/Integer;");
    jobject intValue = env->CallStaticObjectMethod(intDesc, valueOf, value);

    jclass queueDesc = env->FindClass("java/util/concurrent/ArrayBlockingQueue");
    jmethodID put = env->GetMethodID(queueDesc, "put", "(Ljava/lang/Object;)V");
    env->CallVoidMethod(queue, put, intValue);
}

VOID SvcReportEvent(LPTSTR szFunction) {
    HANDLE hEventSource = RegisterEventSource(NULL, SVCNAME);
    LPCTSTR lpszStrings[2];
    TCHAR Buffer[1024];

    if (hEventSource != NULL) {
        StringCchPrintf(Buffer, 80, TEXT("%s failed with %d"), szFunction, GetLastError());

        lpszStrings[0] = SVCNAME;
        lpszStrings[1] = Buffer;

        ReportEvent(hEventSource,        // event log handle
            EVENTLOG_ERROR_TYPE, // event type
            0,                   // event category
            11111,           // event identifier
            NULL,                // no security identifier
            2,                   // size of lpszStrings array
            0,                   // no binary data
            lpszStrings,         // array of strings
            NULL);               // no binary data

        DeregisterEventSource(hEventSource);
    }
}


/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    installService
 * Signature: (Lchav1961/bt/winsl/utils/JavaServiceDescriptor;)I
 */
extern "C" JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_installService(JNIEnv* env, jclass clazz, jobject obj) {
    if (IsElevated()) {
        SC_HANDLE schSCManager = OpenSCManager(
            NULL,                    // local computer
            NULL,                    // ServicesActive database 
            SC_MANAGER_ALL_ACCESS);  // full access rights 

        if (schSCManager == NULL) {
            return fireEnvironmentException(env, SC_MANAGER_NOT_AVAILABLE);
        }
        else {
            // Create the service
            jclass desc = env->FindClass("chav1961/bt/winsl/utils/JavaServiceDescriptor");
            TCHAR SVCNAME[1024], DISPLAYNAME[1024], PATH[2048], ORDER_GROUP[1024], DEPENDENCIES[1024], START_NAME[1024], PASSWORD[100];

            DWORD svcLen = extractStringFromServiceDescriptor(env, desc, "lpServiceName", obj, SVCNAME, sizeof(SVCNAME) / sizeof(SVCNAME[0]));
            DWORD displayLen = extractStringFromServiceDescriptor(env, desc, "lpDisplayName", obj, DISPLAYNAME, sizeof(DISPLAYNAME) / sizeof(DISPLAYNAME[0]));
            DWORD dwDesiredAccess = extractDWORDFromServiceDescriptor(env, desc, "dwDesiredAccess", obj);
            DWORD dwServiceType = extractDWORDFromServiceDescriptor(env, desc, "dwServiceType", obj);
            DWORD dwStartType = extractDWORDFromServiceDescriptor(env, desc, "dwStartType", obj);
            DWORD dwErrorControl = extractDWORDFromServiceDescriptor(env, desc, "dwErrorControl", obj);
            DWORD pathLen = extractStringFromServiceDescriptor(env, desc, "lpBinaryPathName", obj, PATH, sizeof(PATH) / sizeof(PATH[0]));
            DWORD loadOrderGroupLen = extractStringFromServiceDescriptor(env, desc, "lpLoadOrderGroup", obj, ORDER_GROUP, sizeof(ORDER_GROUP) / sizeof(ORDER_GROUP[0]));
            DWORD tagId = 0;
            DWORD dependenciesLen = extractStringFromServiceDescriptor(env, desc, "lpDependencies", obj, DEPENDENCIES, sizeof(DEPENDENCIES) / sizeof(DEPENDENCIES[0]));
            DWORD serviceStartNameLen = extractStringFromServiceDescriptor(env, desc, "lpServiceStartName", obj, START_NAME, sizeof(START_NAME) / sizeof(START_NAME[0]));
            DWORD passwordLen = extractStringFromServiceDescriptor(env, desc, "lpPassword", obj, PASSWORD, sizeof(PASSWORD) / sizeof(PASSWORD[0]));
            
            SC_HANDLE schService = CreateService(
                schSCManager,               // SCM database 
                SVCNAME,                    // name of service 
                DISPLAYNAME,                // service name to display 
                dwDesiredAccess,            // desired access 
                dwServiceType,              // service type 
                dwStartType,                // start type 
                dwErrorControl,             // error control type 
                PATH,                       // path to service's binary 
                loadOrderGroupLen ? ORDER_GROUP : NULL,     // load ordering group 
                NULL,//&tagId,                                     // no tag identifier 
                dependenciesLen ? DEPENDENCIES : NULL,      // dependencies 
                serviceStartNameLen ? START_NAME : NULL,    // typed or LocalSystem account 
                passwordLen ? PASSWORD : NULL               // password 
            );

            if (schService == NULL) {
                CloseServiceHandle(schSCManager);
                return fireContentException(env, buildErrorMessage(env, SERVICE_CREATION_FAILURE_FORMAT, GetLastError(), SVCNAME));
            }
            else {
                CloseServiceHandle(schService);
                CloseServiceHandle(schSCManager);
                return tagId;
            }
        }
    }
    else {
        return fireEnvironmentException(env, NO_ADMIN_RIGHTS);
    }
}

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    updateService
 * Signature: (Lchav1961/bt/winsl/utils/JavaServiceDescriptor;)I
 */
extern "C" JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_updateService(JNIEnv* env, jclass clazz, jobject obj) {
    if (IsElevated()) {
        SC_HANDLE schSCManager = OpenSCManager(
            NULL,                    // local computer
            NULL,                    // ServicesActive database 
            SC_MANAGER_ALL_ACCESS);  // full access rights 

        if (schSCManager == NULL) {
            return fireEnvironmentException(env, SC_MANAGER_NOT_AVAILABLE);
        }
        else {
            // Update the service
            jclass desc = env->FindClass("chav1961/bt/winsl/utils/JavaServiceDescriptor");
            TCHAR SVCNAME[1024], DISPLAYNAME[1024], PATH[2048], ORDER_GROUP[1024], DEPENDENCIES[1024], START_NAME[1024], PASSWORD[100];

            DWORD svcLen = extractStringFromServiceDescriptor(env, desc, "lpServiceName", obj, SVCNAME, sizeof(SVCNAME) / sizeof(SVCNAME[0]));
            DWORD displayLen = extractStringFromServiceDescriptor(env, desc, "lpDisplayName", obj, DISPLAYNAME, sizeof(DISPLAYNAME) / sizeof(DISPLAYNAME[0]));
            DWORD dwDesiredAccess = extractDWORDFromServiceDescriptor(env, desc, "dwDesiredAccess", obj);
            DWORD dwServiceType = extractDWORDFromServiceDescriptor(env, desc, "dwServiceType", obj);
            DWORD dwStartType = extractDWORDFromServiceDescriptor(env, desc, "dwStartType", obj);
            DWORD dwErrorControl = extractDWORDFromServiceDescriptor(env, desc, "dwErrorControl", obj);
            DWORD pathLen = extractStringFromServiceDescriptor(env, desc, "lpBinaryPathName", obj, PATH, sizeof(PATH) / sizeof(PATH[0]));
            DWORD loadOrderGroupLen = extractStringFromServiceDescriptor(env, desc, "lpLoadOrderGroup", obj, ORDER_GROUP, sizeof(ORDER_GROUP) / sizeof(ORDER_GROUP[0]));
            DWORD tagId = 0;
            DWORD dependenciesLen = extractStringFromServiceDescriptor(env, desc, "lpDependencies", obj, DEPENDENCIES, sizeof(DEPENDENCIES) / sizeof(DEPENDENCIES[0]));
            DWORD serviceStartNameLen = extractStringFromServiceDescriptor(env, desc, "lpServiceStartName", obj, START_NAME, sizeof(START_NAME) / sizeof(START_NAME[0]));
            DWORD passwordLen = extractStringFromServiceDescriptor(env, desc, "lpPassword", obj, PASSWORD, sizeof(PASSWORD) / sizeof(PASSWORD[0]));

            SC_HANDLE schService = OpenService(
                        schSCManager,				// handle to service control manager database 
                        SVCNAME,		            // pointer to name of service to start 
                        SERVICE_ALL_ACCESS			// type of access to service
            );

            if (schService) {
                BOOL retCode1 = ChangeServiceConfig(
                    schService,					            // SCManager database
                    dwServiceType,                          // service type
                    dwStartType,				            // start type
                    dwErrorControl,                         // error control type
                    PATH,                                   // service start command
                    loadOrderGroupLen ? ORDER_GROUP : NULL, // load ordering group
                    &tagId,                                 // tag identifier
                    dependenciesLen ? DEPENDENCIES : NULL,	// dependencies
                    serviceStartNameLen ? START_NAME : NULL,// account
                    passwordLen ? PASSWORD : NULL,			// password
                    DISPLAYNAME
                );

                if (retCode1) {
                    SERVICE_FAILURE_ACTIONS_FLAG failureActions;
                    failureActions.fFailureActionsOnNonCrashFailures = TRUE;
                    BOOL retCode2 = ChangeServiceConfig2(schService, SERVICE_CONFIG_FAILURE_ACTIONS, &failureActions);
                    
                    CloseServiceHandle(schService);
                    CloseServiceHandle(schSCManager);
                    if (retCode2) {
                        return CmdAddServiceParameterRegistry(SVCNAME, DISPLAYNAME, displayLen);
                    }
                    else {
                        return fireContentException(env, buildErrorMessage(env, SERVICE_UPDATE_FAILURE_FORMAT, GetLastError(), SVCNAME));
                    }
                }
                else {
                    CloseServiceHandle(schService);
                    CloseServiceHandle(schSCManager);
                    return fireContentException(env, buildErrorMessage(env, SERVICE_UPDATE_FAILURE_FORMAT, GetLastError(), SVCNAME));
                }
            }
            else {
                CloseServiceHandle(schSCManager);
                return fireContentException(env, buildErrorMessage(env, SERVICE_UPDATE_FAILURE_FORMAT, GetLastError(), SVCNAME));
            }
        }
    } 
    else {
        return fireEnvironmentException(env, NO_ADMIN_RIGHTS);
    }
}


/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    enumServices
 * Signature: (II)[Lchav1961/bt/winsl/utils/ServiceEnumDescriptor;
 */
extern "C" JNIEXPORT jobjectArray JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_enumServices(JNIEnv* env, jclass clazz, jint from, jint to) {
    return NULL;
    SC_HANDLE schSCManager = OpenSCManager(
        NULL,                    // local computer
        NULL,                    // ServicesActive database 
        SC_MANAGER_ENUMERATE_SERVICE);  // full access rights 

    if (schSCManager == NULL) {
        fireEnvironmentException(env, SC_MANAGER_NOT_AVAILABLE);
        return NULL;
    }
    else {
        DWORD   pcbBytesNeeded, lpServicesReturned, lpResumeHandle = 0;

        if (EnumServicesStatusEx(schSCManager, SC_ENUM_PROCESS_INFO, SERVICE_WIN32, SERVICE_ACTIVE, NULL, 0, &pcbBytesNeeded, &lpServicesReturned, &lpResumeHandle, NULL) == 0 && GetLastError() == ERROR_MORE_DATA) {
            LPENUM_SERVICE_STATUS_PROCESS   serviceStateList = (LPENUM_SERVICE_STATUS_PROCESS)GlobalAlloc(GMEM_FIXED, pcbBytesNeeded);
            jobjectArray result = NULL;

            if (EnumServicesStatusEx(schSCManager, SC_ENUM_PROCESS_INFO, SERVICE_WIN32, SERVICE_ACTIVE, (LPBYTE)serviceStateList, pcbBytesNeeded, &pcbBytesNeeded, &lpServicesReturned, &lpResumeHandle, NULL)) {
                jclass desc = env->FindClass("chav1961/bt/winsl/utils/JavaServiceDescriptor");
                
                if (lpServicesReturned > to) {
                    result = env->NewObjectArray(to - from, desc, NULL);
                    for (int index = from; index < to; index++) {
                        jstring name = placeString2Java(env, serviceStateList[index].lpServiceName);
                        env->SetObjectArrayElement(result, index, Java_chav1961_bt_winsl_utils_JavaServiceLibrary_queryService(env, clazz, name));
                    }
                }
                else if (lpServicesReturned < from) {
                    result = env->NewObjectArray(0, desc, NULL);
                }
                else {
                    DWORD size = (to - from) - lpServicesReturned;
                    result = env->NewObjectArray(size, desc, NULL);
                    for (int index = from; index < to; index++) {
                        jstring name = placeString2Java(env, serviceStateList[index].lpServiceName);
                        env->SetObjectArrayElement(result, index-from, Java_chav1961_bt_winsl_utils_JavaServiceLibrary_queryService(env, clazz, name));
                    }
                }
            }
            GlobalFree(serviceStateList);
            return result;
        }
        else {
            fireEnvironmentException(env, SERVICE_REMOVE_FAILURE);
            return NULL;
        }

    }
}


/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    queryService
 * Signature: (Ljava/lang/String;)Lchav1961/bt/winsl/utils/JavaServiceDescriptor;
 */
extern "C" JNIEXPORT jobject JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_queryService(JNIEnv* env, jclass clazz, jstring name) {
    SC_HANDLE schSCManager = OpenSCManager(
        NULL,                    // local computer
        NULL,                    // ServicesActive database 
        SC_MANAGER_ENUMERATE_SERVICE);  // full access rights 

    if (schSCManager == NULL) {
        fireEnvironmentException(env, SC_MANAGER_NOT_AVAILABLE);
        return NULL;
    }
    else {
        // Query the service
        TCHAR SVCNAME[1024];
        QUERY_SERVICE_CONFIG config;

        if (extractStringFromJava(env, name, SVCNAME, sizeof(SVCNAME) / sizeof(SVCNAME[0])) > 0) {
            SC_HANDLE schService = OpenService  (
                schSCManager,				// handle to service control manager database 
                SVCNAME,		            // pointer to name of service to start 
                SERVICE_QUERY_CONFIG		// type of access to service
            );

            if(schService == NULL) {
                CloseServiceHandle(schSCManager);
                return NULL;
            }
            else {
                DWORD size;
                jobject result = NULL;
                BOOL retCode = QueryServiceConfig(schService, &config, sizeof(config), &size);

                if (!retCode) {
                    jclass desc = env->FindClass("chav1961/bt/winsl/utils/JavaServiceDescriptor");
                    jmethodID constr = env->GetMethodID(desc, "<init>", "()V");
                    result = env->NewObject(desc, constr);

                    storeString2ServiceDescriptor(env, desc, "lpServiceName", result, placeString2Java(env, SVCNAME));
                    storeString2ServiceDescriptor(env, desc, "lpDisplayName", result, placeString2Java(env, config.lpDisplayName));
                    storeDWORD2ServiceDescriptor(env, desc, "dwDesiredAccess", result, SERVICE_QUERY_CONFIG);
                    storeDWORD2ServiceDescriptor(env, desc, "dwServiceType", result, config.dwServiceType);
                    storeDWORD2ServiceDescriptor(env, desc, "dwStartType", result, config.dwStartType);
                    storeDWORD2ServiceDescriptor(env, desc, "dwErrorControl", result, config.dwErrorControl);
                    storeString2ServiceDescriptor(env, desc, "lpBinaryPathName", result, placeString2Java(env, config.lpBinaryPathName));
                    storeString2ServiceDescriptor(env, desc, "lpLoadOrderGroup", result, placeString2Java(env, config.lpLoadOrderGroup));
                    storeString2ServiceDescriptor(env, desc, "lpDependencies", result, placeString2Java(env, config.lpDependencies));
                    storeString2ServiceDescriptor(env, desc, "lpServiceStartName", result, placeString2Java(env, config.lpServiceStartName));
                }
                else {
                    fireEnvironmentException(env, SC_MANAGER_NOT_AVAILABLE);
                }
                CloseServiceHandle(schService);
                CloseServiceHandle(schSCManager);
                return result;
            }
        }
        else {
            CloseServiceHandle(schSCManager);
            return NULL;
        }
    }
}


/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    removeService
 * Signature: (Ljava/lang/String;)I
 */
extern "C" JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_removeService(JNIEnv * env, jclass clazz, jstring name) {
    if (IsElevated()) {
        SC_HANDLE schSCManager = OpenSCManager(
            NULL,                    // local computer
            NULL,                    // ServicesActive database 
            SC_MANAGER_ALL_ACCESS);  // full access rights 

        if (schSCManager == NULL) {
            return fireEnvironmentException(env, SC_MANAGER_NOT_AVAILABLE);
        }
        else {
            // Remove the service
            TCHAR SVCNAME[1024];

            extractStringFromJava(env, name, SVCNAME, sizeof(SVCNAME) / sizeof(SVCNAME[0]));
            SC_HANDLE schService = OpenService(
                schSCManager,				// handle to service control manager database 
                SVCNAME,	                // pointer to name of service to start 
                SERVICE_ALL_ACCESS			// type of access to service
            );

            if (schService) {
                BOOL retCode = DeleteService(schService);

                if (retCode) {
                    CloseServiceHandle(schService);
                    CloseServiceHandle(schSCManager);
                    CmdRemoveServiceParameterRegistry(SVCNAME);
                    return 0;
                }
                else {
                    CloseServiceHandle(schService);
                    CloseServiceHandle(schSCManager);
                    return fireContentException(env, buildErrorMessage(env, SERVICE_REMOVING_FAILURE_FORMAT, GetLastError(), SVCNAME));
                }
            }
            else {
                CloseServiceHandle(schSCManager);
                return fireContentException(env, buildErrorMessage(env, SERVICE_REMOVING_FAILURE_FORMAT, GetLastError(), SVCNAME));
            }
        }
    }
    else {
        return fireEnvironmentException(env, NO_ADMIN_RIGHTS);
    }
}

VOID ReportSvcStatus(SERVICE_STATUS_HANDLE handle, DWORD dwCurrentState, DWORD dwWin32ExitCode, DWORD dwWaitHint) {
    static DWORD dwCheckPoint = 1;
    SERVICE_STATUS gSvcStatus;

    gSvcStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
    gSvcStatus.dwServiceSpecificExitCode = 0;
    gSvcStatus.dwCurrentState = dwCurrentState;
    gSvcStatus.dwWin32ExitCode = dwWin32ExitCode;
    gSvcStatus.dwWaitHint = dwWaitHint;

    if (dwCurrentState == SERVICE_START_PENDING)
        gSvcStatus.dwControlsAccepted = 0;
    else gSvcStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;

    if ((dwCurrentState == SERVICE_RUNNING) || (dwCurrentState == SERVICE_STOPPED))
        gSvcStatus.dwCheckPoint = 0;
    else gSvcStatus.dwCheckPoint = dwCheckPoint++;

    SetServiceStatus(handle, &gSvcStatus);
}

VOID ReportSvcStatus(DWORD dwCurrentState, DWORD dwWin32ExitCode, DWORD dwWaitHint) {
    static DWORD dwCheckPoint = 1;

    gSvcStatus.dwCurrentState = dwCurrentState;
    gSvcStatus.dwWin32ExitCode = dwWin32ExitCode;
    gSvcStatus.dwWaitHint = dwWaitHint;

    if (dwCurrentState == SERVICE_START_PENDING)
        gSvcStatus.dwControlsAccepted = 0;
    else gSvcStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;

    if ((dwCurrentState == SERVICE_RUNNING) ||
        (dwCurrentState == SERVICE_STOPPED))
        gSvcStatus.dwCheckPoint = 0;
    else gSvcStatus.dwCheckPoint = dwCheckPoint++;

    SetServiceStatus(gSvcStatusHandle, &gSvcStatus);
}

VOID WINAPI service_ctrl(DWORD dwCtrlCode) {
    switch (dwCtrlCode)     {
        case SERVICE_CONTROL_STOP:
        case SERVICE_CONTROL_SHUTDOWN:
            ReportSvcStatus(SERVICE_STOP_PENDING, NO_ERROR, 0);
            SetEvent(ghSvcStopEvent);
            putQueueValue(envRef, queueRef, RC_STOP);
            ReportSvcStatus(gSvcStatus.dwCurrentState, NO_ERROR, 0);
            break;
        case SERVICE_CONTROL_PAUSE:
            putQueueValue(envRef, queueRef, RC_PAUSE);
            break;
        case SERVICE_CONTROL_CONTINUE:
            putQueueValue(envRef, queueRef, RC_RESUME);
            break;
        case SERVICE_CONTROL_INTERROGATE:
            putQueueValue(envRef, queueRef, RC_PAUSE);
            break;
    }
}


VOID WINAPI SvcMain() {
    gSvcStatusHandle = RegisterServiceCtrlHandler(SVCNAME, service_ctrl);

    if (!gSvcStatusHandle) {
        SvcReportEvent((LPTSTR)TEXT("RegisterServiceCtrlHandler"));
        return;
    }

    gSvcStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
    gSvcStatus.dwServiceSpecificExitCode = 0;
    ReportSvcStatus(SERVICE_START_PENDING, NO_ERROR, 3000);

    ghSvcStopEvent = CreateEvent(
        NULL,    // default security attributes
        TRUE,    // manual reset event
        FALSE,   // not signaled
        NULL);   // no name

    if (ghSvcStopEvent == NULL) {
        ReportSvcStatus(SERVICE_STOPPED, GetLastError(), 0);
        return;
    }

    ReportSvcStatus(SERVICE_RUNNING, NO_ERROR, 0);
    while (1) {
        WaitForSingleObject(ghSvcStopEvent, INFINITE);
        ReportSvcStatus(SERVICE_STOPPED, NO_ERROR, 0);
        return;
    }
}

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    prepareService
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_getServiceRequest(JNIEnv*, jclass) {
    return 0;
}

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    prepareService
 * Signature: ()I
 */
extern "C" JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_prepareService(JNIEnv * env, jclass clazz, jstring name, jobject queue) {
    if (extractStringFromJava(env, name, SVCNAME, sizeof(SVCNAME) / sizeof(SVCNAME[0])) > 0) {
        queueRef = queue;
        envRef = env;
         
        SERVICE_TABLE_ENTRY DispatchTable[] = {
            { SVCNAME, (LPSERVICE_MAIN_FUNCTION)SvcMain},
            { NULL, NULL }
        };

        if (!StartServiceCtrlDispatcher(DispatchTable)) {
            SvcReportEvent((LPTSTR)TEXT("StartServiceCtrlDispatcher"));
            return fireEnvironmentException(env, SERVICE_CREATION_FAILURE_START_DISPATCHER_FAILURE);
        }
        else {
            return 0;
        }
    }
    else {
        return fireEnvironmentException(env, SERVICE_CREATION_FAILURE_NO_SERVICE_NAME);
    }
}

/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    unprepareService
 * Signature: ()I
 */
extern "C" JNIEXPORT jint JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_unprepareService(JNIEnv* env, jclass clazz) {
    SetEvent(ghSvcStopEvent);
    CloseHandle(ghSvcStopEvent);
    return 0;
}


/*
 * Class:     chav1961_bt_winsl_utils_JavaServiceLibrary
 * Method:    print2ServiceLog
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_chav1961_bt_winsl_utils_JavaServiceLibrary_print2ServiceLog(JNIEnv* env, jclass clazz, jstring service, jstring message) {
    TCHAR   SVCNAME[1024];

    if (extractStringFromJava(env, service, SVCNAME, sizeof(SVCNAME) / sizeof(SVCNAME[0])) > 0) {
        HANDLE  hEventSource = RegisterEventSource(NULL, SVCNAME);

        if (hEventSource > 0) {
            TCHAR MESSAGE[1024];
            LPCTSTR  lpszStrings[] = { MESSAGE };

            extractStringFromJava(env, service, SVCNAME, sizeof(SVCNAME) / sizeof(SVCNAME[0]));
            ReportEvent(hEventSource, // handle of event source
                EVENTLOG_ERROR_TYPE,  // event type
                0,                    // event category
                0,                    // event ID
                NULL,                 // current user's SID
                1,                    // strings in lpszStrings
                0,                    // no bytes of raw data
                lpszStrings,          // array of error strings
                NULL                  // no raw data
            );
            DeregisterEventSource(hEventSource);
        }
    }
}
