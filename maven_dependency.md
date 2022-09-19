## 注意

- RxHttp支持`RxJava2和RxJava3`，依赖时需要选择对应的注解处理器，如不需要支持RxJava，则可不选择注解处理器

```xml
<dependencies>

    <!--必须-->
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.10.0</version>
    </dependency>

    <!--必须-->
    <dependency>
        <groupId>com.github.liujingxing.rxhttp</groupId>
        <artifactId>rxhttp</artifactId>
        <version>2.9.5</version>
    </dependency>

    <!-- 非必须 RxJava2/RxJava3 二选一或都不选 -->
    <!-- <dependency>
        <groupId>io.reactivex.rxjava2</groupId>
        <artifactId>rxjava</artifactId>
        <version>2.2.8</version>
    </dependency> -->

    <dependency>
        <groupId>io.reactivex.rxjava3</groupId>
        <artifactId>rxjava</artifactId>
        <version>3.1.5</version>
    </dependency>

</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <!--必须-->
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.github.liujingxing.rxhttp</groupId>
                        <artifactId>rxhttp-compiler</artifactId>
                        <version>2.9.5</version>
                    </path>
                </annotationProcessorPaths>

                <!--RxJava注解处理器，非必须-->
                <annotationProcessors>
                    <!--以下两个注解处理器，分别对应RxJava2/RxJava3，二选一即可 -->
                    <!-- <annotationProcessor>
                        com.rxhttp.compiler.maven.AnnotationRxJava2Processor
                    </annotationProcessor> -->
                    <annotationProcessor>
                        com.rxhttp.compiler.maven.AnnotationRxJava3Processor
                    </annotationProcessor>
                </annotationProcessors>
            </configuration>
        </plugin>
    </plugins>
</build>
```
