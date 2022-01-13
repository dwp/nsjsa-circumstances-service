package uk.gov.dwp.jsa.circumstances.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.dwp.jsa.circumstances.service.config.ServiceObjectMapperProvider;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableTransactionManagement
@ComponentScan(value = "uk.gov.dwp.jsa")
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ServiceObjectMapperProvider objectMapperProvider = new ServiceObjectMapperProvider();
        return objectMapperProvider.get();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
