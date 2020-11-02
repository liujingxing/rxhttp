package com.example.httpsender.param;

import java.io.File;

import okhttp3.RequestBody;
import rxhttp.wrapper.annotation.Param;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.param.FormParam;
import rxhttp.wrapper.param.Method;
import rxhttp.wrapper.utils.BuildUtil;

/**
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 */
@Param(methodName = "postBodyForm")
public class PostBodyParam extends FormParam {

    private RequestBody mRequestBody;

    public PostBodyParam(String url) {
        super(url, Method.POST);
    }

    public PostBodyParam setRequestBody(String key, File file) {
        return setRequestBody(new UpFile(key, file));
    }

    public PostBodyParam setRequestBody(UpFile upFile) {
        mRequestBody = RequestBody.create(BuildUtil.getMediaType(upFile.getFilename()), upFile.getFile());
        return this;
    }

    @Override
    public RequestBody getRequestBody() {
        return mRequestBody != null ? mRequestBody : super.getRequestBody();
    }
}
