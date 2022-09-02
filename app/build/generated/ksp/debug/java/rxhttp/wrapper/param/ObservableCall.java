package rxhttp.wrapper.param;

import androidx.annotation.NonNull;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rxhttp.wrapper.BodyParamFactory;
import rxhttp.wrapper.CallFactory;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.ProgressT;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.utils.LogUtil;

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 21:59
 */
public class ObservableCall extends Observable<Progress> {

    private CallFactory callFactory;
    private boolean syncRequest = false;
    private boolean callbackUploadProgress = false;

    public ObservableCall(CallFactory callFactory) {
        this.callFactory = callFactory;
    }

    @Override
    public void subscribeActual(Observer<? super Progress> observer) {
        CallExecuteDisposable d = syncRequest ? new CallExecuteDisposable(observer, callFactory, callbackUploadProgress) :
            new CallEnqueueDisposable(observer, callFactory, callbackUploadProgress);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        d.run();
    }

    public void syncRequest() {
        syncRequest = true;
    }

    public void enableUploadProgressCallback() {
        callbackUploadProgress = true;
    }

    private static class CallEnqueueDisposable extends CallExecuteDisposable implements Callback {

        /**
         * Constructs a DeferredScalarDisposable by wrapping the Observer.
         *
         * @param downstream             the Observer to wrap, not null (not verified)
         * @param callFactory
         * @param callbackUploadProgress
         */
        CallEnqueueDisposable(Observer<? super Progress> downstream, CallFactory callFactory, boolean callbackUploadProgress) {
            super(downstream, callFactory, callbackUploadProgress);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!disposed) {
                downstream.onNext(new ProgressT<>(response));
            }
            if (!disposed) {
                downstream.onComplete();
            }
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            LogUtil.log(call.request().url().toString(), e);
            Exceptions.throwIfFatal(e);
            if (!disposed) {
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        @Override
        public void run() {
            call.enqueue(this);
        }
    }


    private static class CallExecuteDisposable implements Disposable, ProgressCallback {

        protected final Call call;
        protected final Observer<? super Progress> downstream;
        protected volatile boolean disposed;

        /**
         * Constructs a DeferredScalarDisposable by wrapping the Observer.
         *
         * @param downstream the Observer to wrap, not null (not verified)
         */
        CallExecuteDisposable(Observer<? super Progress> downstream, CallFactory callFactory, boolean callbackUploadProgress) {
            if (callFactory instanceof BodyParamFactory && callbackUploadProgress) {
                ((BodyParamFactory) callFactory).getParam().setProgressCallback(this);
            }
            this.downstream = downstream;
            this.call = callFactory.newCall();
        }

        @Override
        public void onProgress(int progress, long currentSize, long totalSize) {
            if (!disposed) {
                downstream.onNext(new Progress(progress, currentSize, totalSize));
            }
        }

        public void run() {
            Response value;
            try {
                value = call.execute();
            } catch (Throwable e) {
                LogUtil.log(call.request().url().toString(), e);
                Exceptions.throwIfFatal(e);
                if (!disposed) {
                    downstream.onError(e);
                } else {
                    RxJavaPlugins.onError(e);
                }
                return;
            }
            if (!disposed) {
                downstream.onNext(new ProgressT<>(value));
            }
            if (!disposed) {
                downstream.onComplete();
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
    }
}
