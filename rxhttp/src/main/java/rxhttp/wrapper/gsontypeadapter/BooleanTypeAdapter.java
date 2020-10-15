package com.tcc.base.http.gsontypeadapter;

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * 部分后台会把boolean数据返回null, 并且服务器处理不好,客户端可以处理
 *
 */
public class BooleanTypeAdapter extends TypeAdapter<Boolean> {
    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        try {
            if (value == null){
                value = false;
            }
            out.value(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean read(JsonReader in) throws IOException {
        try {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                Log.e("TypeAdapter", "null is not a boolean");
                return false;
            }
            if (in.peek() == JsonToken.NUMBER) {
                Double value = in.nextDouble();
                Log.e("TypeAdapter", value+ " is not a boolean");
                return value == Double.valueOf(1) ? true : false;//boolean 有时候服务器给的是1，表示true，0可能就是false
            }
            if (in.peek() == JsonToken.STRING) {
                String str = in.nextString();
                if (NumberUtils.isFloatOrDouble(str)){
                    return Double.valueOf(str)== Double.valueOf(1) ? true:false;
                } else {
                    Log.e("TypeAdapter", str + " is not a number");
                }
            }
        }catch(NumberFormatException e){
            Log.e("TypeAdapter", e.getMessage(), e);
        } catch (Exception e) {
            Log.e("TypeAdapter", e.getMessage(), e);
        }
        return false;
    }
}
