package rxhttp;

import android.os.Build;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

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

    public void loge(String tag, String message, Throwable e) {
        System.err.println(tag + ": " + message);
        e.printStackTrace();
    }

    public void loge(String tag, Throwable e) {
        e.printStackTrace();
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
        public void loge(String tag, String message, Throwable e) {
            log(Log.ERROR, tag, message);
            printThrowable(e, tag);
        }

        @Override
        public void loge(String tag, Throwable e) {
            printThrowable(e, tag);
        }

        private void log(int priority, String tag, String content) {
            int p = 3 * 1024;
            int i = 0;
            while (content.getBytes().length > p) {
                String logContent = new String(content.getBytes(), 0, p);
                //最后一个字符有可能是乱码(中文被截取一个字节等原因)，故移除
                logContent = logContent.substring(0, logContent.length() - 1);
                Log.println(priority, tag, logContent);
                if (!LogUtil.isSegmentPrint()) return;
                Log.v(tag, "<---------------------------------- Segment " + (++i) + " ---------------------------------->");
                content = content.substring(logContent.length());
            }
            if (content.length() > 0)
                Log.println(priority, tag, content);
        }

        private void printThrowable(Throwable ex, String tag) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.flush();
            String s = sw.toString();
            int position = 0;
            int len = s.length();
            while (position < len) {
                int index = s.indexOf('\n', position);
                if (index == -1) index = len;
                log(Log.ERROR, tag, s.substring(position, index));
                position = index + 1;
            }
        }
    }
}
