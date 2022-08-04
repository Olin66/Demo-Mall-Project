package com.mall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.mall.common.to.es.SkuEsModel;
import com.mall.search.constant.EsConstant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class MallSearchApplicationTests {

    @Autowired
    private ElasticsearchClient client;

    @Test
    void test() throws IOException {
        SearchResponse<SkuEsModel> response = client.search(s -> s.index(EsConstant.PRODUCT_INDEX).query(q -> q.matchAll(m -> m)), SkuEsModel.class);
        for (Hit<SkuEsModel> hit : response.hits().hits()) System.out.println(hit.source());
    }

    @Test
    void searchTest() throws IOException {
        SearchRequest request = SearchRequest.of(s ->
                s.index(EsConstant.PRODUCT_INDEX)
                        .query(q -> q.bool(b -> b
                                .must(m -> m.match(item ->
                                        item.field("skuTitle").query("白色")))
                                .filter(f -> f.term(t -> t.field("catalogId").value(225)))
                                .filter(f -> f.terms(t -> t.field("brandId").terms(v -> v.value(List.of(FieldValue.of(9), FieldValue.of(10), FieldValue.of(2))))))
                                .filter(f -> f.term(t -> t.field("hasStock").value(true)))
                                .filter(f -> f.range(r -> r.field("skuPrice").gte(JsonData.of(6000)).lte(JsonData.of(8000))))
                                .filter(f -> f.nested(n -> n.path("attrs")
                                        .query(qq -> qq.bool(bb -> bb
                                                .must(mm -> mm.term(t -> t.field("attrs.attrId").value(15)))
                                                .must(mm -> mm.terms(t -> t.field("attrs.attrValue").terms(v -> v.value(List.of(FieldValue.of("海思（Hisilicon）"), FieldValue.of("以官网信息为准"))))))))))
                        )).sort(ss -> ss.field(f -> f.field("skuPrice").order(SortOrder.Desc)))
                        .highlight(h -> h.fields("skuTitle", f -> f.preTags("<b style='color:red'>").postTags("</b>")))
                        .from(0).size(1));
        SearchResponse<SkuEsModel> response = client.search(request, SkuEsModel.class);
        for (Hit<SkuEsModel> hit : response.hits().hits()) System.out.println(hit.source());
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

    @Test
    void search() throws IOException {
        SearchResponse<Bank> search = client.search(s -> s
                        .index("bank")
                        .query(q -> q
                                .term(t -> t
                                        .field("balance")
                                        .value(39225)
                                )),
                Bank.class);

        for (Hit<Bank> hit : search.hits().hits()) {
            System.out.println(hit.source());
        }
    }

    @Test
    void pageSearch() throws IOException {
        SearchResponse<Bank> search = client.search(s -> s.index("bank").query(q -> q.matchAll(m -> m)).from(4).size(2), Bank.class);
        search.hits().hits().forEach(hit -> System.out.println(hit.source()));
    }

    @Test
    void sortSearch() throws IOException {
        SearchResponse<Bank> search = client.search(s -> s
                .index("bank")
                .query(q -> q
                        .matchAll(m -> m))
                .sort(sort -> sort
                        .field(f -> f
                                .field("balance")
                                .order(SortOrder.Desc)
                        )), Bank.class);
        search.hits().hits().forEach(hit -> System.out.println(hit.source()));
    }

    @Test
    void filterSearch() throws IOException {
        SearchResponse<Bank> search = client.search(s -> s
                        .index("bank")
                        .query(q -> q
                                .bool(b -> b
                                        .must(must -> must
                                                .match(m -> m
                                                        .field("age")
                                                        .query(30)
                                                )
                                        )
                                        .must(must -> must
                                                .match(m -> m
                                                        .field("gender")
                                                        .query("M")
                                                )
                                        )
                                )
                        )
                , Bank.class);
        search.hits().hits().forEach(hit -> System.out.println(hit.source()));
    }

    @Test
    void rangeSearch() throws IOException {
        SearchResponse<Bank> search = client.search(s -> s
                        .index("bank")
                        .query(q -> q
                                .range(r -> r
                                        .field("age")
                                        .gte(JsonData.of(30))
                                        .lt(JsonData.of(40))))
                , Bank.class);
        search.hits().hits().forEach(hit -> System.out.println(hit.source()));
    }

    @Test
    void fuzzySearch() throws IOException {
        SearchResponse<Bank> search = client.search(s -> s
                        .index("bank")
                        .query(q -> q
                                .fuzzy(f -> f
                                        .field("email")
                                        .value("@netplode.com")
                                        .fuzziness("1"))
                        )
                , Bank.class);
        search.hits().hits().forEach(hit -> System.out.println(hit.source()));
    }

    @Test
    void maxSearch() throws IOException {
        SearchResponse<Bank> response10 = client.search(s -> s
                        .index("bank")
                        .aggregations("maxAge", a -> a
                                .max(m -> m.field("age"))
                        )
                , Bank.class);
        System.out.println(response10.took());
        assert response10.hits().total() != null;
        System.out.println(response10.hits().total().value());
        response10.hits().hits().forEach(e -> {
            assert e.source() != null;
            System.out.println(e.source());
        });
        for (Map.Entry<String, Aggregate> entry : response10.aggregations().entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().max().value());
        }
    }

    @Test
    void groupSearch() throws IOException {
        SearchResponse<Bank> response11 = client.search(s -> s
                        .index("bank")
                        .size(100)
                        .aggregations("ageGroup", a -> a
                                .terms(t -> t
                                        .field("age")
                                )
                        )
                , Bank.class);
        System.out.println(response11.took());
        assert response11.hits().total() != null;
        System.out.println(response11.hits().total().value());
        response11.hits().hits().forEach(e -> {
            assert e.source() != null;
            System.out.println(e.source());
        });
        Aggregate aggregate = response11.aggregations().get("ageGroup");
        LongTermsAggregate lterms = aggregate.lterms();
        Buckets<LongTermsBucket> buckets = lterms.buckets();
        for (LongTermsBucket b : buckets.array()) {
            System.out.println(b.key() + " : " + b.docCount());
        }
    }

    @Data
    static class Bank {
        Integer account_number;
        Integer balance;
        String firstname;
        String lastname;
        Integer age;
        String gender;
        String address;
        String employer;
        String email;
        String city;
        String state;
    }

    @Data
    static class Student {
        Integer id;
        String name;
        Integer age;

        public Student() {

        }

        public Student(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }

}
