package com.danya.mdm;

import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.repository.MdmMessageRepository;
import com.danya.mdm.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@Import(AbstractTest.KafkaTopicConfig.class)
public abstract class AbstractTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    @BeforeAll
    static void beforeAll() {
        if (!postgres.isRunning()) postgres.start();
        if (!kafka.isRunning()) kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    protected KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    protected WireMockServer wireMockServer;

    @Autowired
    protected ObjectMapper objectMapper;

    @SpyBean
    protected MdmMessageRepository mdmMessageRepository;

    @SpyBean
    protected MdmMessageOutboxRepository mdmMessageOutboxRepository;

    @SpyBean
    protected ValidationService validationService;

    @BeforeEach
    void resetStubs() {
        wireMockServer.resetAll();
    }

    @TestConfiguration
    static class KafkaTopicConfig {
        @Bean
        public NewTopic phoneChangeTopic() {
            return TopicBuilder
                    .name("phone-change-topic")
                    .partitions(1)
                    .replicas(1)
                    .build();
        }
    }
}