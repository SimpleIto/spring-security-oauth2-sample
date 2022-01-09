package top.simpleito.thirdpartlogin.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class JacksonJsr310Customizer implements Jackson2ObjectMapperBuilderCustomizer {
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(LOCAL_DATE_TIME_FORMATTER);
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(LOCAL_DATE_TIME_FORMATTER);
        builder.failOnEmptyBeans(false)
                .deserializerByType(LocalDateTime.class, localDateTimeDeserializer)
                .serializerByType(LocalDateTime.class, localDateTimeSerializer);
    }
}
