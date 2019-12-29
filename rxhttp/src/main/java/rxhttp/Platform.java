package rxhttp;

import android.os.Build;
import android.util.Log;

/**
 * User: ljx
 * Date: 2019-12-29
 * Time: 16:07
 */
public class Platform {
    private static final Platform PLATFORM = findPlatform();

    public static Platform get() {
        return PLATFORM;
    }

    private Platform() {
    }

    private static Platform findPlatform() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                return new Android();
            }
        } catch (ClassNotFoundException ignored) {
        }
        return new Platform();
    }

    //是否Android平台
    public boolean isAndroid() {
        return false;
    }

    public boolean sdkLessThan(int api) {
        return false;
    }

    public void logi(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    public void logd(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    public void loge(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    public void logd(String tag, String message, Throwable e) {
        System.out.println(tag + ": " + message);
    }

    static final class Android extends Platform {
        Android() {
            super();
        }

        @Override
        public boolean isAndroid() {
            return true;
        }

        @Override
        public boolean sdkLessThan(int api) {
            return Build.VERSION.SDK_INT < api;
        }

        @Override
        public void logi(String tag, String message) {
            Log.i(tag, message);
        }

        @Override
        public void logd(String tag, String message) {
            Log.d(tag, message);
        }

        @Override
        public void loge(String tag, String message) {
            Log.e(tag, message);
        }

        @Override
        public void logd(String tag, String message, Throwable e) {
            Log.e(tag, message, e);
        }
    }
}
