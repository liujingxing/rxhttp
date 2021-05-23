package rxhttp.wrapper.parse

import rxhttp.wrapper.utils.getActualTypeParameters
import java.lang.reflect.Type

/**
 * This class stores multiple types in an array of [types]
 * User: ljx
 * Date: 2021/05/23
 * Time: 19:32
 */
abstract class TypeParser<T> : Parser<T> {

    @JvmField
    protected var types: Array<out Type>

    constructor() {
        types = getActualTypeParameters(javaClass)
    }

    constructor(vararg types: Type) {
        this.types = types
    }
}