package rxhttp.wrapper.entity;

import java.io.File;
import java.net.URI;

/**
 * User: ljx
 * Date: 2018/12/21
 * Time: 09:21
 */
public class UpFile extends File {

    private String key;
    private String value; //用于文件上传时对应的value值, 为空时,默认为文件名

    public UpFile(String key, String pathname) {
        super(pathname);
        this.key = key;
    }

    public UpFile(String key, String parent, String child) {
        super(parent, child);
        this.key = key;
    }

    public UpFile(String key, File parent, String child) {
        super(parent, child);
        this.key = key;
    }

    public UpFile(String key, URI uri) {
        super(uri);
        this.key = key;
    }


    public String getKey() {
        return key;
    }

    public String getValue() {
        return value == null ? getName() : value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
