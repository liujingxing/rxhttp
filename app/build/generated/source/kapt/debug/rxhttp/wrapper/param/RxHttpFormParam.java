package rxhttp.wrapper.param;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import java.io.File;
import java.lang.Deprecated;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Map;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.ProgressT;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.parse.Parser;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
 */
public class RxHttpFormParam extends RxHttp<FormParam, RxHttpFormParam> {
  /**
   * 用于控制下游回调所在线程(包括进度回调)，仅当{@link progressConsumer}不为 null 时生效
   */
  private Scheduler observeOnScheduler;

  /**
   * 用于监听上传进度回调
   */
  private Consumer<Progress> progressConsumer;

  public RxHttpFormParam(FormParam param) {
    super(param);
  }

  public RxHttpFormParam add(String key, Object value) {
    param.add(key,value);
    return this;
  }

  public RxHttpFormParam addEncoded(String key, Object value) {
    param.addEncoded(key,value);
    return this;
  }

  public RxHttpFormParam add(String key, Object value, boolean isAdd) {
    if(isAdd) {
      param.add(key,value);
    }
    return this;
  }

  public RxHttpFormParam addAll(Map<? extends String, ?> map) {
    param.addAll(map);
    return this;
  }

  public RxHttpFormParam removeAllBody() {
    param.removeAllBody();
    return this;
  }

  public RxHttpFormParam removeAllBody(String key) {
    param.removeAllBody(key);
    return this;
  }

  public RxHttpFormParam set(String key, Object value) {
    param.set(key,value);
    return this;
  }

  public RxHttpFormParam setEncoded(String key, Object value) {
    param.setEncoded(key,value);
    return this;
  }

  public Object queryValue(String key) {
    return param.queryValue(key);
  }

  public List<Object> queryValues(String key) {
    return param.queryValues(key);
  }

  /**
   * @deprecated please user {@link #addFile(String,File)} instead
   */
  @Deprecated
  public RxHttpFormParam add(String key, File file) {
    param.add(key,file);
    return this;
  }

  public RxHttpFormParam addFile(String key, File file) {
    param.addFile(key,file);
    return this;
  }

  public RxHttpFormParam addFile(String key, String filePath) {
    param.addFile(key,filePath);
    return this;
  }

  public RxHttpFormParam addFile(String key, String value, String filePath) {
    param.addFile(key,value,filePath);
    return this;
  }

  public RxHttpFormParam addFile(String key, String value, File file) {
    param.addFile(key,value,file);
    return this;
  }

  public RxHttpFormParam addFile(UpFile file) {
    param.addFile(file);
    return this;
  }

  public RxHttpFormParam addFile(String key, List<File> fileList) {
    param.addFile(key,fileList);
    return this;
  }

  public RxHttpFormParam addFile(List<UpFile> fileList) {
    param.addFile(fileList);
    return this;
  }

  public RxHttpFormParam removeFile(String key) {
    param.removeFile(key);
    return this;
  }

  public RxHttpFormParam setMultiForm() {
    param.setMultiForm();
    return this;
  }

  public RxHttpFormParam setUploadMaxLength(long maxLength) {
    param.setUploadMaxLength(maxLength);
    return this;
  }

  public RxHttpFormParam upload(Consumer<Progress> progressConsumer) {
    return upload(progressConsumer, null);
  }

  /**
   * 监听上传进度
   * @param progressConsumer   进度回调
   * @param observeOnScheduler 用于控制下游回调所在线程(包括进度回调) ，仅当 progressConsumer 不为 null 时生效
   */
  public RxHttpFormParam upload(Consumer<Progress> progressConsumer, Scheduler observeOnScheduler) {
    this.progressConsumer = progressConsumer;
    this.observeOnScheduler = observeOnScheduler;
    return this;
  }

  @Override
  public <T> Observable<T> asParser(Parser<T> parser) {
        if (progressConsumer == null) {
            return super.asParser(parser);
        }
        doOnStart();
        Observable<Progress> observable = new ObservableUpload<T>(param, parser);
        if (scheduler != null)
            observable = observable.subscribeOn(scheduler);
        if (observeOnScheduler != null) {
            observable = observable.observeOn(observeOnScheduler);
        }
        return observable.doOnNext(progressConsumer)
            .filter(progress -> progress instanceof ProgressT)
            .map(progress -> ((ProgressT<T>) progress).getResult());
  }
}
