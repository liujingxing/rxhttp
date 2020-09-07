package rxhttp.wrapper.param;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.param.BodyParam;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
 */
@SuppressWarnings("unchecked")
public class RxHttpBodyParam<P extends BodyParam<P>, R extends RxHttpBodyParam<P, R>> extends RxHttp<P, R> {

  //Controls the downstream callback thread
  protected Scheduler observeOnScheduler;

  //Upload progress callback
  protected Consumer<Progress> progressConsumer;

  protected RxHttpBodyParam(P param) {
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
}
