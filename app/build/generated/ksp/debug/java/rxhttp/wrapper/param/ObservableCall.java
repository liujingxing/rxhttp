package rxhttp.wrapper.param;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rxhttp.wrapper.BodyParamFactory;
import rxhttp.wrapper.CallFactory;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.parse.Parser;
import rxhttp.wrapper.parse.StreamParser;
import rxhttp.wrapper.utils.LogUtil;

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 21:59
 */
public class ObservableCall<T> extends Observable<T> {

    private final Parser<T> parser;
    private final CallFactory callFactory;
    private boolean syncRequest = false;

    public ObservableCall(CallFactory callFactory, Parser<T> parser) {
        this.callFactory = callFactory;
        this.parser = parser;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        CallExecuteDisposable d = syncRequest ? new CallExecuteDisposable(observer, callFactory, parser) :
            new CallEnqueueDisposable(observer, callFactory, parser);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        d.run();
    }

    public ObservableCall<T> syncRequest() {
        syncRequest = true;
        return this;
    }

    public Observable<T> onUploadProgress(Consumer<Progress> progressConsumer) {
        return onUploadProgress(Schedulers.io(), progressConsumer);
    }

    public Observable<T> onUploadProgress(Scheduler scheduler, Consumer<Progress> progressConsumer) {
        if (!(parser instanceof StreamParser) && !(callFactory instanceof BodyParamFactory)) {
            throw new UnsupportedOperationException("parser is " + parser.getClass().getSimpleName() + ", callFactory is " + callFactory.getClass().getSimpleName());
        }
        return new ObservableProgress(this, scheduler, progressConsumer);
    }

    static class CallEnqueueDisposable<T> extends CallExecuteDisposable<T> implements Callback {

        CallEnqueueDisposable(Observer<? super T> downstream, CallFactory callFactory, Parser<T> parser) {
            super(downstream, callFactory, parser);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            try {
                T t = Objects.requireNonNull(parser.onParse(response), "The onParse function returned a null value.");
                if (!disposed) {
                    downstream.onNext(t);
                }
                if (!disposed) {
                    downstream.onComplete();
                }
            } catch (Throwable t) {
                onError(call, t);
                return;
            }
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            onError(call, e);
        }

        @Override
        public void run() {
            call = callFactory.newCall();
            call.enqueue(this);
        }
    }


    static class CallExecuteDisposable<T> implements Disposable {

        protected final Observer<? super T> downstream;
        protected final Parser<T> parser;
        protected final CallFactory callFactory;
        protected volatile boolean disposed;
        protected Call call;

        CallExecuteDisposable(Observer<? super T> downstream, CallFactory callFactory, Parser<T> parser) {
            this.downstream = downstream;
            this.callFactory = callFactory;
            this.parser = parser;
        }

        public void run() {
            call = callFactory.newCall();
            try {
                Response response = call.execute();
                T t = Objects.requireNonNull(parser.onParse(response), "The onParse function returned a null value.");
                if (!disposed) {
                    downstream.onNext(t);
                }
                if (!disposed) {
                    downstream.onComplete();
                }
            } catch (Throwable e) {
                onError(call, e);
                return;
            }
        }

        void onError(Call call, Throwable e) {
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
    }
}
