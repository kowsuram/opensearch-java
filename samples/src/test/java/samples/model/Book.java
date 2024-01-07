package org.opensearch.client.samples.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/*
    @created January/03/2024 - 6:59 PM
    @project opensearch-java
    @author k.ramanjineyulu
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {
    private String author;
    private String bookName;
    private String publisher;
    private String category;
    private long noOfPages;
    private double price;
    private Date publishedDate;
}
