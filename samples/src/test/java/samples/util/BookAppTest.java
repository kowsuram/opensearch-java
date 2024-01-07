package org.opensearch.client.samples.util;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.AvgAggregate;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.samples.SampleClient;
import org.opensearch.client.samples.model.Book;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
    @created January/03/2024 - 6:37 PM
    @project opensearch-java
    @author k.ramanjineyulu
*/
public class BookAppTest {

    public static final String BOOKS = "books";

    @BeforeAll
    @SneakyThrows
    public static void setup() {
        boolean acknowledged = SampleClient.create().indices().delete(d -> d.index(BOOKS)).acknowledged();
        createMapping();
        createDocuments();
    }

    public static void createMapping() {
        try {
            InputStream inputStream = BookAppTest.class.getClassLoader().getResourceAsStream("person_mappings.json");
            TypeMapping typeMapping = OpenSearchUtil.createTypeMapping(inputStream);
            CreateIndexRequest request = new CreateIndexRequest.Builder().index(BOOKS).mappings(typeMapping).build();
            CreateIndexResponse createIndexResponse = SampleClient.create().indices().create(request);
            assertTrue(createIndexResponse.acknowledged());
            Thread.sleep(1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @SneakyThrows
    public static void createDocuments() {
        InputStream inputStream = BookAppTest.class.getClassLoader().getResourceAsStream("testData/books.csv");
        List<String> books = IOUtils.readLines(inputStream, Charset.defaultCharset());
        List<BulkOperation> operations = new LinkedList<>();
        for (String book : books) {
            String[] split = book.split(",");
            Book _book = new Book().builder()
                    .bookName(split[0])
                    .author(split[1])
                    .price(Double.parseDouble(split[2]))
                    .publishedDate(new Date())
                    .noOfPages(Long.parseLong(split[3]))
                    .category(split[4])
                    .build();
            BulkOperation operation = new BulkOperation.Builder().create(c -> c.document(_book)).build();
            operations.add(operation);
        }
        BulkRequest bulkRequest = new BulkRequest.Builder().index(BOOKS).operations(operations).build();
        BulkResponse bulkResponse = SampleClient.create().bulk(bulkRequest);
        assertFalse(bulkResponse.errors());
        Thread.sleep(1000);
    }

    @Test
    @SneakyThrows
    public void termQueryByBookName() {
        Query query = new Query.Builder()
                .term(t -> t.queryName("sample-term-query").field("author").value(f -> f.stringValue("Roy Miller"))).build();
        String json = OpenSearchUtil.asJson(query);
        System.out.println("@@@@@@@@@@ termQueryByBookName -- Query @@@@@@@@@@  \n" + json);
        SearchResponse<Book> search = SampleClient.create().search(s -> s.query(query).size(1000), Book.class);
        List<Hit<Book>> hits = search.hits().hits();
        assertTrue(hits.size() > 0);
        for (Hit<Book> hit : hits) {
            Book book = hit.source();
            assertEquals("The Lizard on Wall", book.getBookName());
        }
    }


    @Test
    @SneakyThrows
    public void boolQuery_must_ByBookNameAndPrice() {
        ArrayList<Query> queries = Lists.newArrayList();
        queries.add(new TermQuery.Builder().field("author").value(f -> f.stringValue("Arthur Conan Doyle")).build().toQuery());
        queries.add(new TermQuery.Builder().field("price").value(f -> f.doubleValue(15.99)).build().toQuery());
        Query query = new Query.Builder().bool(b -> b.must(queries)).build();
        String json = OpenSearchUtil.asJson(query);
        System.out.println("@@@@@@@@@@ boolQuery_must_ByBookNameAndPrice -- Query @@@@@@@@@@  \n" + json);
        SearchResponse<Book> search = SampleClient.create().search(s -> s.query(query), Book.class);
        List<Hit<Book>> hits = search.hits().hits();
        assertTrue(hits.size() > 0);
        for (Hit<Book> hit : hits) {
            Book book = hit.source();
            assertEquals("The Adventures of Sherlock Holmes", book.getBookName());
        }
    }

    @Test
    @SneakyThrows
    public void boolQuery_must_not_ByBookNameAndPrice() {
        ArrayList<Query> queries = Lists.newArrayList();
        queries.add(new TermQuery.Builder().field("author").value(f -> f.stringValue("Arthur Conan Doyle")).build().toQuery());
        queries.add(new TermQuery.Builder().field("price").value(f -> f.doubleValue(12.5)).build().toQuery());
        Query query = new Query.Builder().bool(b -> b.mustNot(queries)).build();
        String json = OpenSearchUtil.asJson(query);
        System.out.println("@@@@@@@@@@ boolQuery_must_not_ByBookNameAndPrice -- Query @@@@@@@@@@  \n" + json);
        SearchResponse<Book> search = SampleClient.create().search(s -> s.query(query).size(1000), Book.class);
        HitsMetadata<Book> hits = search.hits();
        assertEquals(80l, hits.total().value());
    }

    @Test
    @SneakyThrows
    public void rangeQueryByPrice() {
        Query query = new Query.Builder().range(rg -> rg.field("price").gte(JsonData.of(12.5)).lte(JsonData.of(100))).build();
        String json = OpenSearchUtil.asJson(query);
        System.out.println("@@@@@@@@@@ rangeQueryByPrice -- Query @@@@@@@@@@  \n" + json);
        SearchResponse<Book> search = SampleClient.create().search(s -> s.query(query).size(1000), Book.class);
        HitsMetadata<Book> hits = search.hits();
        assertEquals(31l, hits.total().value());
    }

    @Test
    @SneakyThrows
    public void boolWithTermAndRangeQuery() {
        Query priceRangeQuery = new Query.Builder().range(rg -> rg.field("price").gte(JsonData.of(10)).lte(JsonData.of(50))).build();
        Query categoryQuery = new Query.Builder().term(t -> t.field("category").value(f -> f.stringValue("Mystery"))).build();
        SearchResponse<Book> response = SampleClient.create()
                .search(s -> s.query(Query.of(b -> b.bool(a -> a.must(priceRangeQuery, categoryQuery)))), Book.class);
        assertEquals(7, response.hits().total().value());
    }

    @Test
    @SneakyThrows
    public void fetchBooksByMultipleCatgs() {
        Query query = Query.of(q -> q
                .terms(ts -> ts.field("category")
                        .terms(t -> t.value(Lists.newArrayList(FieldValue.of("Fiction"), FieldValue.of("Classic"))))));
        System.out.println("@@@@@@@@@@@@@@ fetchBooksByMultipleCatgs -- " + OpenSearchUtil.asJson(query));
        SearchResponse<Book> response = SampleClient.create().search(s -> s.query(query), Book.class);
        assertEquals(21, response.hits().total().value());
    }

}
