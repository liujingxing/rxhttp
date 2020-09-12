package rxhttp.wrapper.param;

import android.content.Context;
import android.net.Uri;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.IOUtil;

/**
 * User: ljx
 * Date: 2019-05-19
 * Time: 18:18
 */
public interface IPart<P extends Param<P>> extends IFile<P> {

    P addPart(@NonNull Part part);

    default P addPart(@Nullable MediaType contentType, byte[] content) {
        return addPart(RequestBody.create(contentType, content));
    }

    default P addPart(@Nullable MediaType contentType, byte[] content,
                      int offset, int byteCount) {
        return addPart(RequestBody.create(contentType, content, offset, byteCount));
    }

    default P addPart(Context context, Uri uri, @Nullable MediaType contentType) {
        byte[] byteArray = IOUtil.toByeArray(uri, context);
        return addPart(RequestBody.create(contentType, byteArray));
    }

    default P addPart(Context context, Uri uri, @Nullable MediaType contentType, int offset, int byteCount) {
        byte[] byteArray = IOUtil.toByeArray(uri, context);
        return addPart(RequestBody.create(contentType, byteArray, offset, byteCount));
    }

    default P addPart(@NonNull RequestBody body) {
        return addPart(Part.create(body));
    }

    default P addPart(@Nullable Headers headers, @NonNull RequestBody body) {
        return addPart(Part.create(headers, body));
    }

    default P addFormDataPart(@NonNull String name, @Nullable String fileName, @NonNull RequestBody body) {
        return addPart(Part.createFormData(name, fileName, body));
    }

    @Override
    default P addFile(@NonNull UpFile file) {
        if (!file.exists())
            throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' does not exist");
        if (!file.isFile())
            throw new IllegalArgumentException("File '" + file.getAbsolutePath() + "' is not a file");

        RequestBody requestBody = RequestBody.create(BuildUtil.getMediaType(file.getName()), file);
        return addPart(Part.createFormData(file.getKey(), file.getValue(), requestBody));
    }

}
