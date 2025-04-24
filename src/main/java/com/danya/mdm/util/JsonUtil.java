package com.danya.mdm.util;

import com.danya.mdm.exception.MdmJsonConvertException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new MdmJsonConvertException("Ошибка конвертирования объекта в json: ", e);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new MdmJsonConvertException("Ошибка конвертирования json в объект: ", e);
        }
    }
}
