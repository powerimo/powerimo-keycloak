package org.powerimo.keycloak.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.powerimo.keycloak.KcEvent;
import org.powerimo.keycloak.MessageSerializer;
import org.powerimo.keycloak.PowerimoKeycloakException;

import java.nio.charset.StandardCharsets;

/**
 * Default JSON serializer. ObjectMapper is used for serialization and deserialization.
 * Two constructors are available:
 * <ul>
 *   <li>Without arguments. Default ObjectMapper will be created with one registered module (JavaTimeModule).</li>
 *   <li>ObjectMapper. Custom ObjectMapper for serialization and deserialization.</li>
 * </ul>
 * Additionally, getObjectMapper method could be used for customizing.
 */
@Getter
public class DefaultJsonSerializer implements MessageSerializer {
    private final ObjectMapper objectMapper;

    /**
     * Creates instance with predefined ObjectMapper for serialization\deserialization
     */
    public DefaultJsonSerializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Creates instance with a custom ObjectMapper
     * @param objectMapper custom ObjectMapper
     */
    public DefaultJsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T deserialize(String str, Class<T> clazz) {
        try {
            return objectMapper.readValue(str, clazz);
        } catch (Exception ex) {
            throw new PowerimoKeycloakException("Exception on converting JSON to " + clazz, ex);
        }
    }

    @Override
    public KcEvent deserializeEvent(String s) {
        return deserialize(s, KcEvent.class);
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            var s =  objectMapper.writeValueAsString(obj);
            return s.getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new PowerimoKeycloakException("Exception on convert object to JSON", ex);
        }
    }
}
