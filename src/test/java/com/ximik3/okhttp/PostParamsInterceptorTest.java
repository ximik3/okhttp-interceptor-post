package com.ximik3.okhttp; 

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.ximik3.okhttp.PostParamsInterceptor.parse;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by volodymyr.kukhar on 6/1/17.
 */
public class PostParamsInterceptorTest {
    String multipartExample =
            "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[attributes][email]\"\n" +
                    "                                                          Content-Length: 27\n" +
                    "                                                          \n" +
                    "                                                          katya.idyartova@teamvoy.com\n" +
                    "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[attributes][login]\"\n" +
                    "                                                          Content-Length: 27\n" +
                    "                                                          \n" +
                    "                                                          katya.idyartova@teamvoy.com\n" +
                    "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[attributes][password]\"\n" +
                    "                                                          Content-Length: 8\n" +
                    "                                                          \n" +
                    "                                                          12345678\n" +
                    "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[attributes][password_confirmation]\"\n" +
                    "                                                          Content-Length: 8\n" +
                    "                                                          \n" +
                    "                                                          12345678\n" +
                    "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[relationships][profile][data][attributes][first_name]\"\n" +
                    "                                                          Content-Length: 5\n" +
                    "                                                          \n" +
                    "                                                          John\n" +
                    "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[relationships][profile][data][attributes][last_name]\"\n" +
                    "                                                          Content-Length: 5\n" +
                    "                                                          \n" +
                    "                                                          Dou\n" +
                    "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[relationships][profile][data][type]\"\n" +
                    "                                                          Content-Length: 8\n" +
                    "                                                          \n" +
                    "                                                          profiles\n" +
                    "                                                          --laU6BErZtR\n" +
                    "                                                          Content-Disposition: form-data; name=\"data[type]\"\n" +
                    "                                                          Content-Length: 5\n" +
                    "                                                          \n" +
                    "                                                          users\n" +
                    "                                                          --laU6BErZtR--";

    String multipartFileExample =
            "--6510397a-5a05-4908-8a4a-dedab4cbaf87\n" +
                    "Content-Disposition: form-data; name=\"param1\"\n" +
                    "Content-Length: 6\n" +
                    "\n" +
                    "value1\n" +
                    "--6510397a-5a05-4908-8a4a-dedab4cbaf87\n" +
                    "Content-Disposition: form-data; name=\"param2\"\n" +
                    "Content-Length: 6\n" +
                    "\n" +
                    "value2\n" +
                    "--6510397a-5a05-4908-8a4a-dedab4cbaf87\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"filename.jpeg\"\n" +
                    "Content-Type: application/json; charset=utf-8\n" +
                    "Content-Length: 18\n" +
                    "\n" +
                    "{ \"some\": \"json\" }\n" +
                    "--6510397a-5a05-4908-8a4a-dedab4cbaf87--";

    String formBodyExample = "user%5Blogin%5D=user%40mail.com&user%5Bpassword%5D=12345678";

    @Test
    public void parseMultipart() throws Exception {
        MultipartBody multipartBody = new MultipartBody.Builder()
                .addFormDataPart("param1", "")
                .addFormDataPart("param2", "value2")
                .addFormDataPart("file", "filename.jpeg", RequestBody.create(MediaType.parse("application/json"), "{ \"some\": \"json\" }"))
                .build();
        Map<String, String> params = parse(multipartBody);
        assertThat(params.get("param1"), is(""));
        assertThat(params.get("param2"), is("value2"));
        assertThat(params.get("file"), is("filename.jpeg"));
    }

    @Test
    public void parseFormData() throws Exception {
        FormBody formBody = new FormBody.Builder().add("param", "value[@$%11]")
                .addEncoded("%5Bencoded%5D", "%5Bvalue%5D")
                .build();
        Map<String, String> params = parse(formBody);
        assertThat(params.get("param"), is("value[@$%11]"));
        assertThat(params.get("[encoded]"), is("[value]"));
    }


}
