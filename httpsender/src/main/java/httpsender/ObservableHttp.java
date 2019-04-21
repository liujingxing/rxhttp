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

package httpsender;


import java.util.concurrent.Callable;

import httpsender.wrapper.param.Param;
import httpsender.wrapper.parse.Parser;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.DeferredScalarDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Response;

/**
 * 发送Http请求的观察者，管道中断时，请求还未执行完毕，会将请求cancel
 * User: ljx
 * Date: 2018/04/20
 * Time: 11:15
 */
public final class ObservableHttp<T> extends Observable<T> implements Callable<T> {
    private final Param     param;
    private final Parser<T> parser;

    private Call mCall;

    ObservableHttp(@NonNull Param param, @NonNull Parser<T> parser) {
        this.param = param;
        this.parser = parser;
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
        Call call = mCall = Sender.newCall(param);
        Response response = call.execute();
        return parser.onParse(response);
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
