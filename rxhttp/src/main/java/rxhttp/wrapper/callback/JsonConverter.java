package rxhttp.wrapper.callback;


import okhttp3.MediaType;

/**
 * User: ljx
 * Date: 2021-10-15
 * Time: 22:54
 */
public interface JsonConverter extends IConverter {

     MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
}
