package com.ms.datasyn.mq.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * @Auther: gina
 * @Date: 2025-02-26
 * @Description:
 */
public class DateTimeJsonDeserializer extends JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String dateString = jsonParser.readValueAs(String.class);
        DateTimeFormatter formatter= DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        return DateTime.parse(dateString, formatter);
    }
}
