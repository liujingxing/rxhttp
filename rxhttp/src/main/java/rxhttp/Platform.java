package rxhttp;

import android.os.Build;
import android.util.Log;

import rxhttp.wrapper.utils.LogUtil;

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
            log(Log.INFO, tag, message);
        }

        @Override
        public void logd(String tag, String message) {
            log(Log.DEBUG, tag, message);
        }

        @Override
        public void loge(String tag, String message) {
            log(Log.ERROR, tag, message);
        }

        @Override
        public void logd(String tag, String message, Throwable e) {
            log(Log.DEBUG, tag, message + '\n' + Log.getStackTraceString(e));
        }

        public static void log(int priority, String tag, String content) {
            int p = 3072;
            int length = content.length();
            boolean isBlockPrint = LogUtil.isSegmentPrint();
            if (length <= p || !isBlockPrint) {
                Log.println(priority, tag, content);
                if (length > p) {
                    String tip = "Because of the logcat log printing limit, " + (length - p) + " bytes are omitted this time. " +
                        "If you want to print all the logs, call 'RxHttp.setDebug(true, true)' to turn on segment printing";
                    Log.w(tag, tip);
                }
            } else {
                int i = 0;
                while (content.length() > p) {
                    String logContent = content.substring(0, p);
                    Log.println(priority, tag, logContent);
                    Log.println(priority, tag, "<---------------------------------- Segment " + (++i) + " ---------------------------------->");
                    content = content.substring(p);
                }
                if (content.length() > 0)
                    Log.println(priority, tag, content);
            }
        }
    }
}
