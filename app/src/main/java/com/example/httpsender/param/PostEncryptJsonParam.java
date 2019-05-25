package com.example.httpsender.param;




import org.json.JSONObject;

import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.param.AbstractPostParam;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * User: ljx
 * Date: 2019/1/25
 * Time: 19:32
 */
@Param(methodName = "postEncryptJson")
public class PostEncryptJsonParam extends AbstractPostParam {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

    public PostEncryptJsonParam(String url) {
        super(url);
    }

    /**
     * @return 根据自己的业务需求返回对应的RequestBody
     */
    @Override
    public RequestBody getRequestBody() {
        //我们要发送Post请求，参数以加密后的json形式发出
        //第一步，将参数转换为Json字符串
        String json = new JSONObject(this).toString();
        //第二步，加密
        byte[] encryptByte = encrypt(json, "HttpSender");
        //第三部，创建RequestBody并返回
        return RequestBody.create(MEDIA_TYPE_JSON, encryptByte);
    }

    /**
     * @param content  要加密的字符串
     * @param password 密码
     * @return 加密后的字节数组
     */
    private byte[] encrypt(String content, String password) {
        //加码代码省略
        return null;
    }
}
