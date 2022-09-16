/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rxhttp.wrapper.entity;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import okhttp3.Headers;
import okhttp3.ResponseBody;

/**
 * User: ljx
 * Date: 2022/9/5
 * Time: 15:14
 */
public final class OkResponse<T> {

    private final okhttp3.Response rawResponse;
    private final @Nullable T body;
    private final @Nullable ResponseBody errorBody;

    private OkResponse(
        okhttp3.Response rawResponse, @Nullable T body, @Nullable ResponseBody errorBody) {
        this.rawResponse = rawResponse;
        this.body = body;
        this.errorBody = errorBody;
    }

    /**
     * Create a successful response from {@code rawResponse} with {@code body} as the deserialized
     * body.
     */
    public static <T> OkResponse<T> success(@Nullable T body, okhttp3.Response rawResponse) {
        Objects.requireNonNull(rawResponse, "rawResponse == null");
        if (!rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse must be successful response");
        }
        return new OkResponse<>(rawResponse, body, null);
    }

    /**
     * Create an error response from {@code rawResponse} with {@code body} as the error body.
     */
    public static <T> OkResponse<T> error(ResponseBody body, okhttp3.Response rawResponse) {
        Objects.requireNonNull(body, "body == null");
        Objects.requireNonNull(rawResponse, "rawResponse == null");
        if (rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse should not be successful response");
        }
        return new OkResponse<>(rawResponse, null, body);
    }

    /**
     * The raw response from the HTTP client.
     */
    public okhttp3.Response raw() {
        return rawResponse;
    }

    /**
     * HTTP status code.
     */
    public int code() {
        return rawResponse.code();
    }

    /**
     * HTTP status message or null if unknown.
     */
    public String message() {
        return rawResponse.message();
    }

    /**
     * HTTP headers.
     */
    public Headers headers() {
        return rawResponse.headers();
    }

    /**
     * Returns true if {@link #code()} is in the range [200..300).
     */
    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    /**
     * The deserialized response body of a {@linkplain #isSuccessful() successful} response.
     */
    @Nullable
    public T body() {
        return body;
    }

    /**
     * The raw response body of an {@linkplain #isSuccessful() unsuccessful} response.
     */
    @Nullable
    public ResponseBody errorBody() {
        return errorBody;
    }

    @Override
    public String toString() {
        return rawResponse.toString();
    }
}
