package rxhttp.wrapper.coroutines

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
interface Await<T> {

    suspend fun await(): T
}

