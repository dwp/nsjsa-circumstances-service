package uk.gov.dwp.jsa.circumstances.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.vladmihalcea.hibernate.type.util.ObjectMapperSupplier;

public class ServiceObjectMapperProvider implements ObjectMapperSupplier {
    @Override
    public ObjectMapper get() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        objectMapper.setDateFormat(new StdDateFormat());
        return objectMapper;
    }
}
