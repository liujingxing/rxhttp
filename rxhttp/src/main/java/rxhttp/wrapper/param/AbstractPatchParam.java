package rxhttp.wrapper.param;


import rxhttp.wrapper.utils.BuildUtil;
import io.reactivex.annotations.NonNull;
import okhttp3.Request;

/**
 * Patch请求继承本类
 * User: ljx
 * Date: 2019/1/19
 * Time: 11:36
 */
public abstract class AbstractPatchParam extends AbstractParam implements PatchRequest {

    public AbstractPatchParam(@NonNull String url) {
        super(url);
    }

    @Override
    public final Request buildRequest() {
        return BuildUtil.buildPatchRequest(this);
    }
}
