package rxhttp.wrapper.param;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rxhttp.IRxHttp;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.ProgressT;
import rxhttp.wrapper.utils.LogUtil;

/**
 * User: ljx
 * Date: 2018/04/20
 * Time: 11:15
 */
final class ObservableCallEnqueue extends ObservableCall {

    private IRxHttp iRxHttp;
    private boolean callbackUploadProgress;

    ObservableCallEnqueue(IRxHttp iRxHttp) {
        this(iRxHttp, false);
    }

    ObservableCallEnqueue(IRxHttp iRxHttp, boolean callbackUploadProgress) {
        this.iRxHttp = iRxHttp;
        this.callbackUploadProgress = callbackUploadProgress;
    }

    @Override
    public void subscribeActual(Observer<? super Progress> observer) {
        HttpDisposable d = new HttpDisposable(observer, iRxHttp, callbackUploadProgress);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        d.run();
    }


    private static class HttpDisposable implements Disposable, Callback, ProgressCallback {

        private volatile boolean disposed;

        private final Call call;
        private final Observer<? super Progress> downstream;

        /**
         * Constructs a DeferredScalarDisposable by wrapping the Observer.
         *
         * @param downstream the Observer to wrap, not null (not verified)
         */
        HttpDisposable(Observer<? super Progress> downstream, IRxHttp iRxHttp, boolean callbackUploadProgress) {
            if (iRxHttp instanceof RxHttpAbstractBodyParam && callbackUploadProgress) {
                RxHttpAbstractBodyParam<?, ?> bodyParam = (RxHttpAbstractBodyParam) iRxHttp;
                bodyParam.getParam().setProgressCallback(this);
            }
            this.downstream = downstream;
            this.call = iRxHttp.newCall();
        }

        @Override
        public void onProgress(Progress p) {
            if (!disposed) {
                downstream.onNext(p);
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (!disposed) {
                downstream.onNext(new ProgressT<>(response));
            }
            if (!disposed) {
                downstream.onComplete();
            }
        }

        @Override
        public void onFailure(Call call, IOException e) {
            LogUtil.log(call.request().url().toString(), e);
            Exceptions.throwIfFatal(e);
            if (!disposed) {
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        @Override
        public void dispose() {
            disposed = true;
            call.cancel();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        public void run() {
            call.enqueue(this);
        }
    }
}
