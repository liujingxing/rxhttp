/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package rxhttp;


import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.DeferredScalarDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.cahce.CacheMode;
import rxhttp.wrapper.cahce.InternalCache;
import rxhttp.wrapper.exception.CacheReadFailedException;
import rxhttp.wrapper.param.Param;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.utils.LogUtil;

/**
 * 发送Http请求的观察者，管道中断时，请求还未执行完毕，会将请求cancel
 * User: ljx
 * Date: 2018/04/20
 * Time: 11:15
 */
public final class ObservableHttp<T> extends Observable<T> implements Callable<T> {
    private final Param param;
    private final Parser<T> parser;

    private Call mCall;
    private Request request;
    private InternalCache cache;

    ObservableHttp(@NonNull Param param, @NonNull Parser<T> parser) {
        this.param = param;
        this.parser = parser;
        cache = RxHttpPlugins.getCache();
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        HttpDisposable d = new HttpDisposable(observer);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        T value;
        try {
            value = ObjectHelper.requireNonNull(execute(param), "Callable returned null");
        } catch (Throwable e) {
            LogUtil.log(param.getUrl(), e);
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                observer.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
            return;
        }
        d.complete(value);
    }

    @Override
    public T call() throws Exception {
        return ObjectHelper.requireNonNull(execute(param), "The callable returned a null value");
    }


    //执行请求
    private T execute(Param param) throws Exception {
        if (request == null) { //防止失败重试时，重复构造okhttp3.Request对象
            request = param.buildRequest();
        }
        CacheMode cacheMode = param.getCacheMode();
        if (cacheModeIs(CacheMode.ONLY_CACHE, CacheMode.READ_CACHE_FAILED_REQUEST_NETWORK)) {
            //读取缓存
            Response cacheResponse = getCacheResponse(request, param.getCacheValidTime());
            if (cacheResponse != null) {
                return parser.onParse(cacheResponse);
            }
            if (cacheModeIs(CacheMode.ONLY_CACHE)) //仅读缓存模式下，缓存读取失败，直接抛出异常
                throw new CacheReadFailedException("Cache read failed");
        }
        Call call = mCall = HttpSender.newCall(request);
        Response networkResponse = null;
        try {
            networkResponse = call.execute();
            if (cache != null && cacheMode != CacheMode.ONLY_NETWORK) {
                //非ONLY_NETWORK模式下,请求成功，写入缓存
                networkResponse = cache.put(networkResponse, param.getCacheKey());
            }
        } catch (Exception e) {
            if (cacheModeIs(CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE)) {
                //请求失败，读取缓存
                networkResponse = getCacheResponse(request, param.getCacheValidTime());
            }
            if (networkResponse == null)
                throw e;
        }
        return parser.onParse(networkResponse);
    }

    private boolean cacheModeIs(CacheMode... cacheModes) {
        if (cacheModes == null || cache == null) return false;
        CacheMode cacheMode = param.getCacheMode();
        for (CacheMode mode : cacheModes) {
            if (mode == cacheMode) return true;
        }
        return false;
    }

    @Nullable
    private Response getCacheResponse(Request request, long validTime) throws IOException {
        if (cache == null) return null;
        Response cacheResponse = cache.get(request, param.getCacheKey());
        if (cacheResponse != null) {
            long receivedTime = cacheResponse.receivedResponseAtMillis();
            if (validTime != -1 && System.currentTimeMillis() - receivedTime > validTime)
                return null; //缓存过期，返回null
            return cacheResponse;
        }
        return null;
    }

    class HttpDisposable extends DeferredScalarDisposable<T> {

        /**
         * Constructs a DeferredScalarDisposable by wrapping the Observer.
         *
         * @param downstream the Observer to wrap, not null (not verified)
         */
        HttpDisposable(Observer<? super T> downstream) {
            super(downstream);
        }

        @Override
        public void dispose() {
            cancelRequest(mCall);
            super.dispose();
        }
    }


    //关闭请求
    private void cancelRequest(Call call) {
        if (call != null && !call.isCanceled())
            call.cancel();
    }
}
