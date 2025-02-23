package com.mall.product;

//import com.aliyun.oss.*;

import com.mall.product.dao.AttrGroupDao;
import com.mall.product.service.CategoryService;
import com.mall.product.service.SkuSaleAttrValueService;
import com.mall.product.vo.pojo.SkuSaleAttr;
import com.mall.product.vo.pojo.SpuAttrGroup;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    public void test1() {
        List<SpuAttrGroup> groups = attrGroupDao.getAttrGroupWithAttrsBySpuId(100L, 225L);
        System.out.println(groups);
    }

    @Test
    public void test2(){
        List<SkuSaleAttr> attrs = skuSaleAttrValueService.getSaleAttrsBySpuId(13L);
        System.out.println(attrs);
    }

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void redisTest() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world_" + UUID.randomUUID());
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Autowired
    RedissonClient redissonClient;

    @Test
    void redissonTest(){
        System.out.println(redissonClient);
    }

//    @Autowired
//    BrandService brandService;
//
//    @Autowired
//    OSS ossClient;
//
//    @Test
//    void test1(){
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("Huawei");
//        brandService.save(brandEntity);
//    }
//
//    @Test
//    void test2(){
//        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        // 填写Bucket名称，例如examplebucket。
//        String bucketName = "snowcharm";
//        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
//        String objectName = "优秀共青团员干部.png";
//        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
//        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//        String filePath= "D:\\1A\\重要资料\\优秀共青团干部.png";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        try {
//            InputStream inputStream = new FileInputStream(filePath);
//            // 创建PutObject请求。
//            ossClient.putObject(bucketName, objectName, inputStream);
//        } catch (OSSException oe) {
//            System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                    + "but was rejected with an error response for some reason.");
//            System.out.println("Error Message:" + oe.getErrorMessage());
//            System.out.println("Error Code:" + oe.getErrorCode());
//            System.out.println("Request ID:" + oe.getRequestId());
//            System.out.println("Host ID:" + oe.getHostId());
//        } catch (ClientException ce) {
//            System.out.println("Caught an ClientException, which means the client encountered "
//                    + "a serious internal problem while trying to communicate with OSS, "
//                    + "such as not being able to access the network.");
//            System.out.println("Error Message:" + ce.getMessage());
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (ossClient != null) {
//                ossClient.shutdown();
//            }
//        }
//    }
//
//    @Test
//    void test3() throws FileNotFoundException {
//        InputStream inputStream = new FileInputStream("D:\\BaiduNetdiskDownload\\gmall\\Doc\\API网关.jpg");
//        String bucketName = "snowcharm";
//        String objectName = "gateway.png";
//        ossClient.putObject(bucketName, objectName, inputStream);
//    }

}
