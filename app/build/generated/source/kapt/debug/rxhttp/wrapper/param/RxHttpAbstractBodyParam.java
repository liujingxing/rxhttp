package rxhttp.wrapper.param;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.param.AbstractBodyParam;
import rxhttp.wrapper.parse.Parser;

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
@SuppressWarnings("unchecked")
public class RxHttpAbstractBodyParam<P extends AbstractBodyParam<P>, R extends RxHttpAbstractBodyParam<P, R>> extends RxHttp<P, R> {

    //Controls the downstream callback thread
    private Scheduler observeOnScheduler;

    //Upload progress callback
    private Consumer<Progress> progressConsumer;

    protected RxHttpAbstractBodyParam(P param) {
        super(param);
    }

    public final R setUploadMaxLength(long maxLength) {
        param.setUploadMaxLength(maxLength);
        return (R) this;
    }

    public final R upload(Consumer<Progress> progressConsumer) {
        return upload(null, progressConsumer);
    }

    /**
     * @param progressConsumer   Upload progress callback
     * @param observeOnScheduler Controls the downstream callback thread
     */
    public final R upload(Scheduler observeOnScheduler, Consumer<Progress> progressConsumer) {
        this.progressConsumer = progressConsumer;
        this.observeOnScheduler = observeOnScheduler;
        return (R) this;
    }

    @Override
    public final <T> Observable<T> asParser(Parser<T> parser) {
        return asParser(parser, observeOnScheduler, progressConsumer);
    }

    @Override
    public final <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler,
                                            Consumer<Progress> progressConsumer) {
        if (progressConsumer == null) {
            return super.asParser(parser, scheduler, null);
        }
        ObservableCall observableCall;
        if (isAsync) {
            observableCall = new ObservableCallEnqueue(this, true);
        } else {
            observableCall = new ObservableCallExecute(this, true);
        }
        return observableCall.asParser(parser, scheduler, progressConsumer);
    }
}
