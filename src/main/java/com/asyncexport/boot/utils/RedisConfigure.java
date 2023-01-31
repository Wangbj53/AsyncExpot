package com.asyncexport.boot.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;

import org.joda.time.DateTime;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.joda.DateTimeFormatterFactory;

import java.util.TimeZone;

@Configuration
@EnableCaching
public class RedisConfigure {

    @Bean(name = "StringRedisTemplate")
    public RedisTemplate<String, String> normalRedisTemplate(final RedisConnectionFactory factory,
                                                             final ObjectMapper jodaMapper) {
        final StringRedisSerializer keySerializer = new StringRedisSerializer();

        final Jackson2JsonRedisSerializer<?> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        valueSerializer.setObjectMapper(jodaMapper);
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ObjectMapper jodaMapper() {
        final ObjectMapper om = new ObjectMapper();
        final JodaModule jodaModule = new JodaModule();
        final DateTimeFormatterFactory formatterFactory = new DateTimeFormatterFactory();
        formatterFactory.setPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        formatterFactory.setIso(DateTimeFormat.ISO.DATE);
        jodaModule.addSerializer(DateTime.class, new DateTimeSerializer(
                new JacksonJodaDateFormat(formatterFactory.createDateTimeFormatter())));
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.registerModule(jodaModule);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return om;
    }
}