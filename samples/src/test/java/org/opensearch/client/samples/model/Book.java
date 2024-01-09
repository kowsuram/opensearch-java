package org.opensearch.client.samples.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.json.stream.JsonGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opensearch.client.json.JsonpDeserializable;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;

import java.lang.annotation.Annotation;
import java.util.Date;

/*
    @created January/03/2024 - 6:59 PM
    @project opensearch-java
    @author k.ramanjineyulu
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book  {
    @JsonProperty
    private String author;
    @JsonProperty
    private String bookName;
    @JsonProperty
    private String publisher;
    @JsonProperty
    private String category;
    @JsonProperty
    private long noOfPages;
    @JsonProperty
    private double price;
    @JsonProperty
    private Date publishedDate;
}
