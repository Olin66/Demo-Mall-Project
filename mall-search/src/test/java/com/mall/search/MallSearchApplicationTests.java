package com.mall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.mall.search.config.MallElasticSearchConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class MallSearchApplicationTests {

    @Autowired
    private ElasticsearchClient client;

    @Test
    void test(){
        System.out.println(client);
    }

    @Test
    void index() throws IOException {
        Student s = new Student(1, "test", 19);
        IndexRequest.Builder<Student> indexReqBuilder = new IndexRequest.Builder<>();
        indexReqBuilder.index("student");
        indexReqBuilder.id(String.valueOf(s.id));
        indexReqBuilder.document(s);
        IndexResponse response = client.index(indexReqBuilder.build());
        System.out.println(response);
    }

    @Data
    static class Student{
        int id;
        String name;
        int age;

        public Student(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }

}
