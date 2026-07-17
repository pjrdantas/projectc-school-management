package br.com.escola.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.catalog.application.command.CreateDisciplinaCommand;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.port.in.ComandoUseCase;
import br.com.escola.catalog.application.service.OutboxPublisherResult;
import br.com.escola.catalog.application.service.OutboxPublisherService;
import br.com.escola.catalog.application.service.OutboxPublisherSettings;
import br.com.escola.catalog.domain.valueobject.EscolaId;

@Testcontainers
@SpringBootTest(properties = {
        "management.health.redis.enabled=false",
        "catalog.outbox.publisher.enabled=false"
})
class OutboxPublisherIT {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka-native:3.8.0");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private ComandoUseCase commandUseCase;

    @Autowired
    private OutboxPublisherService publisherService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM disciplina");
        jdbcTemplate.update("DELETE FROM outbox_event");
        jdbcTemplate.update("DELETE FROM command_idempotency");
    }

    @Test
    void devePublicarEnvelopeNoKafkaEAguardarMetadata() throws Exception {
        String topic = "school.catalog.events.it." + UUID.randomUUID();
        String dltTopic = topic + ".DLT";
        criarDisciplina("publisher-main");

        OutboxPublisherResult result = publisherService.publicarLote(settings(topic, dltTopic, 3));
        JsonNode envelope = objectMapper.readTree(consume(topic));

        assertThat(result).isEqualTo(new OutboxPublisherResult(1, 1, 0, 0));
        assertThat(envelope.get("eventType").asText()).isEqualTo("subject-created");
        assertThat(envelope.get("eventVersion").asInt()).isEqualTo(1);
        assertThat(envelope.get("escolaId").asText()).isEqualTo(ESCOLA_ID.toString());
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM outbox_event", String.class))
                .isEqualTo("PUBLICADO");
        assertThat(jdbcTemplate.queryForObject("SELECT attempts FROM outbox_event", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT broker_topic FROM outbox_event", String.class))
                .isEqualTo(topic);
    }

    @Test
    void devePublicarNaDltQuandoTentativasJaForamEsgotadas() throws Exception {
        String topic = "school.catalog.events.it." + UUID.randomUUID();
        String dltTopic = topic + ".DLT";
        criarDisciplina("publisher-dlt");
        jdbcTemplate.update("UPDATE outbox_event SET status='RETRY', attempts=3");

        OutboxPublisherResult result = publisherService.publicarLote(settings(topic, dltTopic, 3));
        JsonNode envelope = objectMapper.readTree(consume(dltTopic));

        assertThat(result).isEqualTo(new OutboxPublisherResult(1, 0, 0, 1));
        assertThat(envelope.get("eventType").asText()).isEqualTo("subject-created");
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM outbox_event", String.class))
                .isEqualTo("DLT");
        assertThat(jdbcTemplate.queryForObject("SELECT broker_topic FROM outbox_event", String.class))
                .isEqualTo(dltTopic);
    }

    private void criarDisciplina(String key) {
        commandUseCase.criarDisciplina(
                new CreateDisciplinaCommand("Disciplina " + key, 40, null),
                key,
                new InternalRequestContext("corr-" + key, USUARIO_ID, new EscolaId(ESCOLA_ID)));
    }

    private OutboxPublisherSettings settings(String topic, String dltTopic, int maxAttempts) {
        return new OutboxPublisherSettings(
                10, maxAttempts, Duration.ofMinutes(1), Duration.ofMillis(100),
                Duration.ofSeconds(1), topic, dltTopic);
    }

    private String consume(String topic) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "catalog-it-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(List.of(topic));
            long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
            while (System.nanoTime() < deadline) {
                var records = consumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    return records.iterator().next().value();
                }
            }
        }
        throw new AssertionError("Evento nao recebido no topico " + topic);
    }
}

