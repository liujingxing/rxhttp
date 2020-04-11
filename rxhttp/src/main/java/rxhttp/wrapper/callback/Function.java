package rxhttp.wrapper.callback;


import rxhttp.wrapper.annotations.NonNull;

/**
 * A functional interface that takes a value and returns another value, possibly with a
 * different type and allows throwing a checked exception.
 *
 * @param <T> the input value type
 * @param <R> the output value type
 */
public interface Function<T, R> {
    /**
     * Apply some calculation to the input value and return some other value.
     * @param t the input value
     * @return the output value
     * @throws Exception on error
     */
    @NonNull
    R apply(@NonNull T t) throws Exception;
}
