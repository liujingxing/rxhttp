package rxhttp.wrapper.param

import rxhttp.IRxHttp

/**
 * User: ljx
 * Date: 2021/9/16
 * Time: 23:48
 */
interface BodyParamFactory : IRxHttp {
    val param: AbstractBodyParam<*>
}