package rxhttp.wrapper.param;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.parse.Parser;

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 21:59
 */
abstract class ObservableCall extends Observable<Progress> {

    public <T> Observable<T> asParser(Parser<T> parser) {
        return asParser(parser, null, null);
    }

    public <T> Observable<T> asParser(Parser<T> parser, Consumer<Progress> progressConsumer) {
        return asParser(parser, null, progressConsumer);
    }

    public <T> Observable<T> asParser(Parser<T> parser, Scheduler scheduler, Consumer<Progress> progressConsumer) {
        return new ObservableParser<>(this, parser, scheduler, progressConsumer);
    }
}
