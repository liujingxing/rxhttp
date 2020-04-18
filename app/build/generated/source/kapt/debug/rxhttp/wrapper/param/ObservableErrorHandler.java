package rxhttp.wrapper.param;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import rxhttp.wrapper.utils.LogUtil;

/**
 * User: ljx
 * Date: 2020/4/11
 * Time: 16:19
 */
public abstract class ObservableErrorHandler<T> extends Observable<T> {
    static {
        Consumer<? super Throwable> errorHandler = RxJavaPlugins.getErrorHandler();
        if (errorHandler == null) {
            /*
            RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃
            这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，
            这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃
            */
            RxJavaPlugins.setErrorHandler(LogUtil::log);
        }
    }
}
