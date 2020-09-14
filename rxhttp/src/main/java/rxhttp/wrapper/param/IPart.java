package rxhttp.wrapper.param;

import android.content.Context;
import android.net.Uri;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import rxhttp.wrapper.annotations.NonNull;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.entity.UpFile;
import rxhttp.wrapper.entity.UriRequestBody;
import rxhttp.wrapper.utils.BuildUtil;
import rxhttp.wrapper.utils.KotlinExtensions;

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

    default P addPart(Context context, Uri uri) {
        return addPart(Part.create(new UriRequestBody(context, uri)));
    }

    default P addPart(Context context, String name, Uri uri) {
        return addPart(KotlinExtensions.asPart(uri, context, name));
    }

    default P addPart(Context context, Uri uri, @Nullable MediaType contentType) {
        return addPart(Part.create(new UriRequestBody(context, uri, contentType)));
    }

    default P addPart(Context context, String name, Uri uri, @Nullable MediaType contentType) {
        return addPart(KotlinExtensions.asPart(uri, context, name, contentType));
    }

    default P addParts(Context context, Map<String, ? extends Uri> uriMap) {
        for (Entry<String, ? extends Uri> entry : uriMap.entrySet()) {
            addPart(context, entry.getKey(), entry.getValue());
        }
        return (P) this;
    }

    default P addParts(Context context, List<? extends Uri> uris) {
        for (Uri uri : uris) {
            addPart(context, uri);
        }
        return (P) this;
    }

    default P addParts(Context context, String name, List<? extends Uri> uris) {
        for (Uri uri : uris) {
            addPart(context, name, uri);
        }
        return (P) this;
    }

    default P addParts(Context context, List<? extends Uri> uris, @Nullable MediaType contentType) {
        for (Uri uri : uris) {
            addPart(context, uri, contentType);
        }
        return (P) this;
    }

    default P addParts(Context context, String name, List<? extends Uri> uris, @Nullable MediaType contentType) {
        for (Uri uri : uris) {
            addPart(context, name, uri, contentType);
        }
        return (P) this;
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
