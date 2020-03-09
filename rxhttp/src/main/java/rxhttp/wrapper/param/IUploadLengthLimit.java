package rxhttp.wrapper.param;



/**
 * 文件上传长度限制接口
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
public interface IUploadLengthLimit {

    //检查长度逻辑自行实现
    void checkLength();
}
