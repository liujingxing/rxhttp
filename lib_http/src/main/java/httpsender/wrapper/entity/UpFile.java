package httpsender.wrapper.entity;

import java.io.File;
import java.net.URI;

/**
 * User: ljx
 * Date: 2018/12/21
 * Time: 09:21
 */
public class UpFile extends File {

    private String value; //用于文件上传时对应的value值, 为空时,默认为文件名

    public UpFile(String pathname) {
        super(pathname);
    }

    public UpFile(String parent, String child) {
        super(parent, child);
    }

    public UpFile(File parent, String child) {
        super(parent, child);
    }

    public UpFile(URI uri) {
        super(uri);
    }

    public String getValue() {
        return value == null ? getName() : value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
