package rxhttp.wrapper.param

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer
import rxhttp.wrapper.BodyParamFactory
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.AbstractBodyParam
import rxhttp.wrapper.parse.Parser

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
@Suppress("UNCHECKED_CAST", "UPPER_BOUND_VIOLATED_BASED_ON_JAVA_ANNOTATIONS")
open class RxHttpAbstractBodyParam<P : AbstractBodyParam<P>, R : RxHttpAbstractBodyParam<P, R>> 
protected constructor(
    param: P
) : RxHttp<P, R>(param), BodyParamFactory {
    //Controls the downstream callback thread
    private var observeOnScheduler: Scheduler? = null

    //Upload progress callback
    private var progressConsumer: Consumer<Progress>? = null
    
    fun setUploadMaxLength(maxLength: Long): R {
        param.setUploadMaxLength(maxLength)
        return this as R
    }

    fun upload(progressConsumer: Consumer<Progress>) = upload(null, progressConsumer)

    /**
     * @param progressConsumer   Upload progress callback
     * @param observeOnScheduler Controls the downstream callback thread
     */
    fun upload(observeOnScheduler: Scheduler?, progressConsumer: Consumer<Progress>): R {
        this.progressConsumer = progressConsumer
        this.observeOnScheduler = observeOnScheduler
        return this as R
    }

    override fun <T> asParser(parser: Parser<T>): Observable<T> =
        asParser(parser, observeOnScheduler, progressConsumer)

    override fun <T> asParser(
        parser: Parser<T>,
        scheduler: Scheduler?,
        progressConsumer: Consumer<Progress>?
    ): Observable<T> {
        if (progressConsumer == null) {
            return super.asParser(parser, scheduler, null)
        }
        val observableCall: ObservableCall = if (isAsync) {
            ObservableCallEnqueue(this, true)
        } else {
            ObservableCallExecute(this, true)
        }
        return observableCall.asParser(parser, scheduler, progressConsumer)
    }
}
