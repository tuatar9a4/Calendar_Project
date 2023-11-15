package com.devd.calenderbydw.utils

import android.util.Log
import timber.log.Timber

class ReleaseTree  : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Release 빌드에서는 VERBOSE, DEBUG, INFO 레벨의 로그를 출력하지 않도록 설정합니다.
        return priority == Log.WARN || priority == Log.ERROR || priority == Log.ASSERT
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Crashlytics에 로그를 전송하는 코드를 추가합니다.
        // 이 코드를 사용하지 않는다면, 로그는 출력되지 않습니다.
        // 예시: FirebaseCrashlytics.getInstance().log(message)
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            // 릴리즈 모드에서 VERBOSE, DEBUG 로그는 출력하지 않도록 합니다.
            return
        }
        // 나머지 로그는 Android의 로그에 출력합니다.
        if (t == null) {
            Log.println(priority, tag, message)
        } else {
            Log.println(priority, tag, "$message\n${Log.getStackTraceString(t)}")
        }
    }
}