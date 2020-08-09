```xml
<dependencies>

    <!-- RxJava2/RxJava3 二选一 -->
    <!-- <dependency>
        <groupId>io.reactivex.rxjava2</groupId>
        <artifactId>rxjava</artifactId>
        <version>2.2.8</version>
    </dependency> -->

    <dependency>
        <groupId>io.reactivex.rxjava3</groupId>
        <artifactId>rxjava</artifactId>
        <version>3.0.2</version>
    </dependency>

    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.8.1</version>
    </dependency>

    <dependency>
        <groupId>com.ljx.rxhttp</groupId>
        <artifactId>rxhttp</artifactId>
        <version>2.3.3-beta2</version>
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
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.ljx.rxhttp</groupId>
                        <artifactId>rxhttp-compiler</artifactId>
                        <version>2.3.3-beta2</version>
                    </path>
                </annotationProcessorPaths>

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