package rxhttp.wrapper.param

import rxhttp.wrapper.BodyParamFactory

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
open class RxHttpAbstractBodyParam<P : AbstractBodyParam<P>, R : RxHttpAbstractBodyParam<P, R>> 
protected constructor(
    param: P
) : RxHttp<P, R>(param), BodyParamFactory {

}