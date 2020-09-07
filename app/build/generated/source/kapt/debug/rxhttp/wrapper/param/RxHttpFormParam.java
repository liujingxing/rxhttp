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
import okhttp3.Headers;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.parse.Parser;

/**
 * Github
 * https://github.com/liujingxing/RxHttp
 * https://github.com/liujingxing/RxLife
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
 */
public class RxHttpFormParam extends RxHttpBodyParam<FormParam, RxHttpFormParam> {
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

  public RxHttpFormParam addAll(Map<String, ?> map) {
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

  public RxHttpFormParam addFile(String key, List<? extends File> fileList) {
    param.addFile(key,fileList);
    return this;
  }

  public RxHttpFormParam addFile(List<? extends UpFile> fileList) {
    param.addFile(fileList);
    return this;
  }

  public RxHttpFormParam addPart(Part part) {
    param.addPart(part);
    return this;
  }

  public RxHttpFormParam addPart(RequestBody requestBody) {
    param.addPart(requestBody);
    return this;
  }

  public RxHttpFormParam addPart(Headers headers, RequestBody requestBody) {
    param.addPart(headers, requestBody);
    return this;
  }

  public RxHttpFormParam addFormDataPart(String name, String fileName, RequestBody requestBody) {
    param.addFormDataPart(name, fileName, requestBody);
    return this;
  }

  public RxHttpFormParam setMultiForm() {
    param.setMultiForm();
    return this;
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
