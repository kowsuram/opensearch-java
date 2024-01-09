package org.opensearch.client.samples.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

/*
    @created December/22/2023 - 11:48 AM
    @project opensearch-java
    @author k.ramanjineyulu
*/
public class OpenSearchUtil {

    public static TypeMapping createTypeMapping(InputStream inputStream) throws FileNotFoundException {
        return TypeMapping._DESERIALIZER.deserialize(Json.createParser(inputStream), new JacksonJsonpMapper(new ObjectMapper()));
    }

    public static <T extends JsonpSerializable> String asJson(T t) {
        JacksonJsonpMapper mapper = new JacksonJsonpMapper();
        Writer writer = new StringWriter();
        JsonGenerator generator = mapper.jsonProvider().createGenerator(writer);
        mapper.serialize(t, generator);
        generator.close();
        return writer.toString();
    }


}
