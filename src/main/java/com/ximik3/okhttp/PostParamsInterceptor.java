package com.ximik3.okhttp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * Created by volodymyr.kukhar on 6/1/17.
 */

public class PostParamsInterceptor implements Interceptor {
    protected Function<RequestParams, Request> interceptor;

    public PostParamsInterceptor(@NonNull Function<RequestParams, Request> interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(interceptor.apply(parse(chain.request())));
    }

    /**
     * Main method. It parses request to fetch body params from there
     *
     * @param request to parse
     * @return request + body params pair
     */
    protected RequestParams parse(Request request) throws IOException {
        return new RequestParams(request, parse(request.body()));
    }

    /**
     * @param body to parse
     * @return body parameters map
     */
    @NonNull
    public static Map<String, String> parse(@Nullable RequestBody body) throws IOException {
        return body == null ? emptyMap()
                : body instanceof MultipartBody ? parse((MultipartBody) body)
                : body instanceof FormBody ? parse((FormBody) body)
                : emptyMap();
    }

    //region Multipart Body

    /**
     * @param body multipart body to parse
     * @return body parameters map
     */
    @NonNull
    public static Map<String, String> parse(@NonNull MultipartBody body) throws IOException {
        Map<String, String> params = new HashMap<>(body.parts().size());
        for (MultipartBody.Part part : body.parts())
            params.putAll(parse(part));
        return params;
    }

    /**
     * @param part to parse
     * @return singleton map for current part (name, value) for usual entry, (name, filename) for files
     */
    @NonNull
    public static Map<String, String> parse(MultipartBody.Part part) throws IOException {
        Map<String, String> headerParams = paramsFrom(headersFrom(part).values("Content-Disposition"));
        if (headerParams.containsKey("name"))
            // Special case when we have file we map entry like Entry(name, filename)
            if (headerParams.containsKey("filename"))
                return singletonMap(headerParams.get("name"), headerParams.get("filename"));
                // otherwise we read body and return Entry(name, body)
            else
                return singletonMap(headerParams.get("name"), bodyBuffer(bodyFrom(part)).readUtf8());
        return emptyMap();
    }

    /**
     * Header value string example: [form-data;  name="file";  filename="filename.jpeg"]
     * possible keys: "name", "filename"
     *
     * @param headerValues values joined with semicolon
     * @return values map
     */
    public static Map<String, String> paramsFrom(List<String> headerValues) {
        Map<String, String> map = new HashMap<>(3);
        for (String value : headerValues) {
            String[] params = value.split(";");
            for (String keyValue : params) {
                String[] entry = keyValue.trim().split("=");
                if (entry.length > 1)
                    map.put(entry[0], entry[1].replace('\"', ' ').trim());
            }

        }
        return map;
    }

    /**
     * @param part to get {@link RequestBody} from
     * @return body from part or empty text body
     */
    @NonNull
    public static RequestBody bodyFrom(MultipartBody.Part part) {
        try {
            Field body = MultipartBody.Part.class.getDeclaredField("body");
            body.setAccessible(true);
            return ((RequestBody) body.get(part));
        } catch (Exception e) {
            return RequestBody.create(MediaType.parse("text"), "");
        }
    }

    /**
     * @param part to get {@link RequestBody} from
     * @return body from part or empty text body
     */
    @NonNull
    public static Headers headersFrom(MultipartBody.Part part) {
        try {
            Field body = MultipartBody.Part.class.getDeclaredField("headers");
            body.setAccessible(true);
            return ((Headers) body.get(part));
        } catch (Exception e) {
            return Headers.of(emptyMap());
        }
    }

    /**
     * Converts {@link RequestBody} to {@link Buffer}
     *
     * @param body to convert
     * @return buffer from body
     */
    @NonNull
    public static Buffer bodyBuffer(@NonNull RequestBody body) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Sink sink = Okio.sink(os);
        BufferedSink bufferedSink = Okio.buffer(sink);
        body.writeTo(bufferedSink);
        return bufferedSink.buffer();
    }
    //endregion

    //region Form Body

    /**
     * @param body form body to parse
     * @return key value parameter pairs map
     */
    @NonNull
    public static Map<String, String> parse(@NonNull FormBody body) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < body.size(); i++)
            map.put(body.name(i), body.value(i));

        return map;
    }
    //endregion

    public static class RequestParams {
        private final Request request;
        private final Map<String, String> params;

        public RequestParams(Request request, Map<String, String> postParams) {
            this.request = request;
            this.params = postParams;
        }

        public Request request() {
            return request;
        }

        public Map<String, String> postParams() {
            return params;
        }
    }

}
