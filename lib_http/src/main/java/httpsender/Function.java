package httpsender;

import httpsender.wrapper.param.Param;

/**
 * User: ljx
 * Date: 2019/1/21
 * Time: 17:26
 */
public interface Function {

    /**
     * 有可能在子线程调用
     *
     * @param p Param
     * @return Param
     */
    Param apply(Param p);
}
