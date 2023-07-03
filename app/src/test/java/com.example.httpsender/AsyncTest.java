package com.example.httpsender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static java.util.concurrent.TimeUnit.SECONDS;
import static okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST;

import com.example.httpsender.entity.Url;
import com.example.httpsender.entity.User;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.observers.TestObserver;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import rxhttp.RxHttpPlugins;
import rxhttp.wrapper.annotation.DefaultDomain;
import rxhttp.wrapper.entity.OkResponse;
import rxhttp.wrapper.entity.ParameterizedTypeImpl;
import rxhttp.wrapper.param.RxHttp;

/**
 * User: ljx
 * Date: 2020/6/20
 * Time: 09:14
 */
public class AsyncTest {

    @DefaultDomain
    public static String baseUrl = "";
    MockWebServer server = new MockWebServer();

    @Before
    public void setUp() throws Exception {
        RxHttpPlugins.init(null).setDebug(true);
        baseUrl = server.url("").toString();
        System.out.println("baseUrl="+baseUrl);
        Url.baseUrl = baseUrl;
    }

    @Test
    public void success() throws InterruptedException {
        TestObserver<String> observer = new TestObserver<>();
        RxHttp.postForm("/test")
            .toObservableString()
            .subscribe(observer);
        assertFalse(observer.await(1, SECONDS));  //等待1s后返回数据
        server.enqueue(new MockResponse()
            .setBody("{\"code\":100}")
            .throttleBody(1, 100, TimeUnit.MILLISECONDS)  //模拟网速慢，每100毫秒传递1个字节
        );
        assertTrue(observer.await(2, SECONDS)); //等待读取Body
        observer.assertComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/test", request.getPath());
        assertEquals("POST", request.getMethod());
    }

    @Test
    public void successList() throws InterruptedException {
        TestObserver<List<Integer>> observer = new TestObserver<>();
        RxHttp.postForm("/test")
            .toObservableList(int.class)
            .subscribe(observer);
        assertFalse(observer.await(1, SECONDS));  //等待1s后返回数据
        server.enqueue(new MockResponse()
            .setBody("[1,2,3,4]")
            .throttleBody(1, 100, TimeUnit.MILLISECONDS)  //模拟网速慢，每100毫秒传递1个字节
        );
        assertTrue(observer.await(2, SECONDS)); //等待读取Body
        observer.assertComplete();
    }

    @Test
    public void successOKResponse() throws InterruptedException {
        Type type = ParameterizedTypeImpl.get(OkResponse.class, User.class);
        TestObserver<OkResponse<String>> observer = new TestObserver<>();
        RxHttp.postForm("/test")
            .<OkResponse<String>>toObservable(type)
            .subscribe(observer);
        assertFalse(observer.await(1, SECONDS));  //等待1s后返回数据
        server.enqueue(new MockResponse()
            .setBody("{\"token\":\"abcdefg\"}")
            .throttleBody(1, 100, TimeUnit.MILLISECONDS)  //模拟网速慢，每100毫秒传递1个字节
        );
        assertTrue(observer.await(2, SECONDS)); //等待读取Body
        observer.assertComplete();
    }


    @Test
    public void failure() throws InterruptedException {
        TestObserver<String> observer = new TestObserver<>();
        RxHttp.postForm("/test")
            .toObservableString()
            .subscribe(observer);
        assertFalse(observer.await(1, SECONDS));
        server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST));
        observer.await(1, SECONDS);
        observer.assertError(IOException.class);
    }
}
