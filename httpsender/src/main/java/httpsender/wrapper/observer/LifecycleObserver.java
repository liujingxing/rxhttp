package httpsender.wrapper.observer;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.ObservableOperator;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 感知Activity、Fragment生命周期的观察者
 * User: ljx
 * Date: 2019/3/30
 * Time: 21:16
 */
public final class LifecycleObserver<T> implements Observer<T>, DefaultLifecycleObserver {

    private Disposable          mDisposable;
    private Observer<? super T> mObserver;

    private LifecycleObserver(LifecycleOwner lifecycleOwner, Observer<? super T> observer) {
        lifecycleOwner.getLifecycle().addObserver(this);
        this.mObserver = observer;
    }

    public static <T> ObservableTransformer<T, T> get(LifecycleOwner owner) {
        return observable -> observable
                .onTerminateDetach()
                .lift((ObservableOperator<T, T>) observer -> {
                    return new LifecycleObserver<>(owner, observer);
                });
    }

    public static <T> ObservableTransformer<T, T> getAndObserveOnMain(LifecycleOwner owner) {
        return observable -> observable
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .lift((ObservableOperator<T, T>) observer -> {
                    return new LifecycleObserver<>(owner, observer);
                });
    }

    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        mObserver.onSubscribe(d);
    }

    @Override
    public void onNext(T t) {
        mObserver.onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        mObserver.onError(e);
    }

    @Override
    public void onComplete() {
        mObserver.onComplete();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        final Disposable disposable = mDisposable;
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }
}
