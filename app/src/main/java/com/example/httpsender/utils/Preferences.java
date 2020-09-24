package com.example.httpsender.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.httpsender.AppHolder;


/**
 * User: hqs
 * Date: 2016/5/10
 * Time: 19:07
 */
public class Preferences {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private Preferences() {
    }

    private static SharedPreferences getInstance() {
        if (sharedPreferences == null) {
            synchronized (Preferences.class) {
                if (sharedPreferences == null) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AppHolder.getInstance());
                }
            }
        }
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getEditor() {
        if (editor == null) {
            editor = getInstance().edit();
        }
        return editor;
    }

    public static String getValue(String key, String defaultValue) {
        return getInstance().getString(key, defaultValue);
    }

    public static void setValue(String key, String value) {
        getEditor().putString(key, value).commit();
    }

    public static int getValue(String key, int defaultValue) {
        return getInstance().getInt(key, defaultValue);
    }

    public static void setValue(String key, int value) {
        getEditor().putInt(key, value).commit();
    }

    public static void setFloat(String key, float value) {
        getEditor().putFloat(key, value).commit();
    }

    public static float getFloat(String key, float defaultValue) {
       return getInstance().getFloat(key, defaultValue);
    }

    public static boolean getValue(String key, boolean defaultValue) {
        return getInstance().getBoolean(key, defaultValue);
    }

    public static void setValue(String key, boolean value) {
        getEditor().putBoolean(key, value).commit();
    }

    public static long getValue(String key, long defaultValue) {
        return getInstance().getLong(key, defaultValue);
    }

    public static void setValue(String key, long value) {
        getEditor().putLong(key, value).commit();
    }
}
