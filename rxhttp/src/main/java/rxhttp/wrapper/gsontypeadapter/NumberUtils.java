package com.tcc.base.http.gsontypeadapter;

import android.text.TextUtils;

import java.util.regex.Pattern;

public class NumberUtils {
    public static boolean isIntOrLong(String str){
        return TextUtils.isDigitsOnly(str);
    }

    public static boolean isFloatOrDouble(String str){
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }
}
