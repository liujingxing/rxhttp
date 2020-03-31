/*
 * Copyright (C) 2013 Square, Inc.
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
package rxhttp.wrapper.cahce;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import rxhttp.wrapper.annotations.Nullable;

/**
 * RxHttp's internal cache interface. Applications shouldn't implement this: instead use {@link CacheManager}.
 */
public interface InternalCache {
    @Nullable
    Response get(Request request, String key) throws IOException;

    @Nullable
    Response put(Response response, String key) throws IOException;


    void remove(String key) throws IOException;

    void removeAll() throws IOException;

    long size() throws IOException;
}
