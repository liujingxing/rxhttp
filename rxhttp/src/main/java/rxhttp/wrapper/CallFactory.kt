package rxhttp.wrapper

import okhttp3.Call
import rxhttp.wrapper.param.AbstractBodyParam

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:56
 */
interface CallFactory {

    fun newCall(): Call
}

interface BodyParamFactory : CallFactory {
    val param: AbstractBodyParam<*>
}



