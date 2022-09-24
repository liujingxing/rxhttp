package rxhttp.wrapper.param;

import okhttp3.MultipartBody.Part;

/**
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
public interface IPart<P extends Param<P>> {

    P addPart(Part part);
}
