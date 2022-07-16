package com.mall.mallthirdparty;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class MallThirdPartyApplicationTests {

    @Autowired
    OSS oss;

    @Test
    void test() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("D:\\BaiduNetdiskDownload\\gmall\\Doc\\API网关.jpg");
        String bucketName = "snowcharm";
        String objectName = "test.png";
        oss.putObject(bucketName, objectName, inputStream);
    }

}
