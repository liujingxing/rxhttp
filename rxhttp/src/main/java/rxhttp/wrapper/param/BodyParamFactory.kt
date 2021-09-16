package rxhttp.wrapper.param

import rxhttp.CallFactory

/**
 * User: ljx
 * Date: 2021/9/16
 * Time: 23:48
 */
interface BodyParamFactory : CallFactory {
    val param: AbstractBodyParam<*>
}