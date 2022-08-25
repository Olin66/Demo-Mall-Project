package com.mall.mallthirdparty;

import com.aliyun.oss.OSS;
import com.mall.mallthirdparty.component.SmsComponent;
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

    @Autowired
    SmsComponent smsComponent;

    @Test
    void test1() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("D:\\BaiduNetdiskDownload\\gmall\\Doc\\API网关.jpg");
        String bucketName = "snowcharm";
        String objectName = "test.png";
        oss.putObject(bucketName, objectName, inputStream);
    }

    @Test
    void test2(){
    }

    @Test
    void test3(){
        smsComponent.sendSmsCode("15305979533", "123456");
    }

}
