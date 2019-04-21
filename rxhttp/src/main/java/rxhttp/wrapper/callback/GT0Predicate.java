package rxhttp.wrapper.callback;

import java.util.Collection;

import io.reactivex.functions.Predicate;

/**
 * 筛选size大于0的Collection集合
 * User: ljx
 * Date: 2018/12/29
 * Time: 14:58
 */
public class GT0Predicate implements Predicate<Collection> {
    @Override
    public boolean test(Collection c) throws Exception {
        return c.size() > 0;
    }
}
