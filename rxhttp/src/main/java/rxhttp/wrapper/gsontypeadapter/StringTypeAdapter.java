package com.tcc.base.http.gsontypeadapter;

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * 部分服务器框架会把数字数据返回null, 并且服务器处理不好,客户端可以处理
 */
public class StringTypeAdapter extends TypeAdapter<String> {
  
    @Override
    public void write(JsonWriter out, String value) throws IOException {
        try {
            if (value == null){
                out.nullValue();
                return;
            }
            out.value(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String read(JsonReader in) throws IOException {
        try {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                Log.e("TypeAdapter", "null is not a string");
                return "";
            }
        } catch (Exception e) {
            Log.e("TypeAdapter", "Not a String", e);
        }
        return in.nextString();
    }
}
