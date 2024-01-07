package org.opensearch.client.samples.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.AverageAggregation;
import org.opensearch.client.opensearch._types.aggregations.AvgAggregate;
import org.opensearch.client.opensearch._types.aggregations.LongTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.samples.SampleClient;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
    @created January/05/2024 - 1:19 PM
    @project opensearch-java
    @author k.ramanjineyulu
*/
public class FruitsAppTest {

    @Test
    @SneakyThrows
    public void fruitsAggregationByPrice() {
        Aggregation averageAgg = new Aggregation.Builder().avg(a -> a.field("price_per_kg")).build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("fruits").aggregations("average_price", averageAgg).build();
        System.out.println("@@@@@@@@@@@@@@ fruitsAggregationByPrice -- " + OpenSearchUtil.asJson(searchRequest));
        SearchResponse<Object> response = SampleClient.create().search(s -> s.index("fruits").aggregations("average_price", averageAgg), Object.class);
        assertEquals(5, response.hits().total().value());
        Map<String, Aggregate> aggregations = response.aggregations();
        Aggregate averagePriceAgg = aggregations.get("average_price");
        if (averagePriceAgg.isAvg()) {
            AvgAggregate avg = averagePriceAgg.avg();
            String avgVal = new DecimalFormat("#.##").format(avg.value());
            assertEquals(3.15, Double.parseDouble(avgVal));
        }
    }

    @Test
    @SneakyThrows
    public void fruitsMinMaxAvgPriceAgg() {
        OpenSearchClient client = SampleClient.create();
        Aggregation avgAgg = new Aggregation.Builder().avg(a -> a.field("price_per_kg")).build();
        Aggregation minAgg = new Aggregation.Builder().min(mi -> mi.field("price_per_kg")).build();
        Aggregation maxAgg = new Aggregation.Builder().max(ma -> ma.field("price_per_kg")).build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("fruits").aggregations(Map.of("avg_price", avgAgg, "min_price", minAgg, "max_price", maxAgg)).build();
        System.out.println("@@@@@@@@@@@@@@ fruitsMinMaxAvgPriceAgg -- " + OpenSearchUtil.asJson(searchRequest));
        SearchResponse<Object> response = client.search(searchRequest, Object.class);
        assertEquals(5, response.hits().total().value());
        Map<String, Aggregate> aggregations = response.aggregations();
        assertEquals(2.55, aggregations.get("avg_price").avg().value());
        assertEquals(1.49, aggregations.get("min_price").min().value());
        assertEquals(3.29, aggregations.get("max_price").max().value());
    }


    @Test
    @SneakyThrows
    public void fruitsTermWithAggregationCombo() {
        OpenSearchClient client = SampleClient.create();
        SearchRequest searchRequest = new SearchRequest.Builder().index("fruits")
                .aggregations("yellow_fruits_max_price", magg -> magg.max(ma -> ma.field("price_per_kg")))
                .query(q -> q.term(t -> t.field("color").value(f -> f.stringValue("Yellow")))).build();
        System.out.println("@@@@@@@@@@@@@@ fruitsTermWithAggregationCombo -- " + OpenSearchUtil.asJson(searchRequest));
        SearchResponse<Object> response = client.search(searchRequest, Object.class);
        assertEquals(3, response.hits().total().value());
        Map<String, Aggregate> aggregations = response.aggregations();
        assertEquals(4.99, aggregations.get("yellow_fruits_max_price").max().value());
    }

    /**
     * {
     *     "aggs": {
     *         "fruit_varieties": {
     *             "nested": {
     *                 "path": "varieties"
     *             },
     *             "aggs": {
     *                 "average_price_per_variety": {
     *                     "avg": {
     *                         "field": "varieties.price_per_kg"
     *                     }
     *                 }
     *             }
     *         }
     *     }
     * }
     */
    @Test
    @SneakyThrows
    public void nestedAggregation() {
        OpenSearchClient client = SampleClient.create();
        Aggregation aggregation = new Aggregation.Builder().nested(nes -> nes.path("varieties"))
                .aggregations("average_price_per_variety", new AverageAggregation.Builder().field("varieties.price_per_kg").build()._toAggregation()).build();
        SearchRequest searchRequest = new SearchRequest.Builder().aggregations("fruit_varieties", aggregation).build();
        System.out.println("@@@@@@@@@@@@@@ nestedAggregation -- " + OpenSearchUtil.asJson(searchRequest));

        SearchResponse<Object> response = client.search(searchRequest, Object.class);
        double value = response.aggregations().get("fruit_varieties").nested().aggregations().get("average_price_per_variety").avg().value();
        String avgVal = new DecimalFormat("#.##").format(value);
        assertEquals(2.49d, Double.parseDouble(avgVal));
    }


    /**
     * {
     *     "aggs": {
     *         "price_by_color": {
     *             "terms": {
     *                 "field": "color"
     *             },
     *             "aggs": {
     *                 "avg_price": {
     *                     "avg": {
     *                         "field": "price_per_kg"
     *                     }
     *                 }
     *             }
     *         }
     *     }
     * }
     */

    @Test
    @SneakyThrows
    public void subAggregationTest_on_stringTerm() {
        OpenSearchClient client = SampleClient.create();
        Aggregation pricePerKgAgg = new Aggregation.Builder().avg(avg -> avg.field("price_per_kg")).build();
        Aggregation priceByColorAgg = new Aggregation.Builder().terms(term -> term.field("color")).aggregations("avg_price", pricePerKgAgg).build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("fruits").aggregations("price_by_color", priceByColorAgg).build();
        System.out.println("@@@@@@@@@@@@@@ subAggregationTest_on_stringTerm -- " + OpenSearchUtil.asJson(searchRequest));
        SearchResponse<Object> response = client.search(searchRequest, Object.class);
        Aggregate priceByColor = response.aggregations().get("price_by_color");
        List<StringTermsBucket> buckets = priceByColor.sterms().buckets().array();
        assertEquals(3, buckets.size());
    }

    /**
     * {
     *     "aggs": {
     *         "price_by_catg_agg": {
     *             "terms": {
     *                 "field": "catgCode"
     *             },
     *             "aggs": {
     *                 "price_average": {
     *                     "avg": {
     *                         "field": "price_per_kg"
     *                     }
     *                 }
     *             }
     *         }
     *     }
     * }
     */
    @Test
    @SneakyThrows
    public void subAggregationTest_on_longTerm() {
        OpenSearchClient client = SampleClient.create();
        Aggregation priceAvgAgg = new Aggregation.Builder().avg(avg -> avg.field("price_per_kg")).build();
        Aggregation priceByCatgCodeAgg = new Aggregation.Builder().terms(term -> term.field("catgCode"))
                .aggregations("price_average", priceAvgAgg)
                .build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("fruits").aggregations("price_by_catg_agg", priceByCatgCodeAgg).build();
        System.out.println("@@@@@@@@@@@@@@ subAggregationTest_on_longTerm -- " + OpenSearchUtil.asJson(searchRequest));
        SearchResponse<Object> response = client.search(searchRequest, Object.class);
        List<LongTermsBucket> priceByCatgAggs = response.aggregations().get("price_by_catg_agg").lterms().buckets().array();
        assertEquals(2, priceByCatgAggs.size());

    }


}
