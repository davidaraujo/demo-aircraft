package io.confluent.demo.aircraft.jsonschema.kstreams;

import io.confluent.demo.aircraft.jsonschema.pojo.FlightStateSchema1;
import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Collections;
import java.util.Properties;
import java.util.regex.Pattern;

// mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.jsonschema.kstreams.QualityRules" -Dexec.args="./src/main/resources/ ccloud_prod.properties"
public class QualityRules {

    static String resourcesDir;
    static String propertiesFile;
    static String country;

    /*
    QUALITY RULES:
    1) IF verticalRate < 0 THEN move message to dlq
    2) IF squawk == null THEN squawk="N/A"
    */
    public void run() throws Exception {

        String inputTopic;
        String cleanTopic;
        String dlqTopic;

        Properties streamProps;
        streamProps = ClientsUtils.loadConfig(resourcesDir + "/" + propertiesFile);

        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstreams-quality-rules");
        streamProps.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaJsonSchemaSerde.class);
        streamProps.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        //streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // topic-success ; topic-dlq ; topic-error

        streamProps.put("json.value.type", "io.confluent.demo.flights.jsonschema.pojo.FlightStateSchema1");

        inputTopic = streamProps.getProperty("input.topic.name");
        cleanTopic = streamProps.getProperty("clean.topic.name");
        dlqTopic = streamProps.getProperty("dlq.topic.name");

        // Create a clean topic if it doesn't exist already
        AdminClient adminClient = AdminClient.create(streamProps);
        boolean cleanTopicExists = adminClient.listTopics().names().get().contains(cleanTopic);

        if (!cleanTopicExists) {
            int partitions = new Integer(streamProps.getProperty("num.partitions"));
            int replication = new Integer(streamProps.getProperty("replication.factor"));
            NewTopic newTopic = new NewTopic(cleanTopic, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }

        // Create a clean topic if it doesn't exist already
        boolean dlqTopicExists = adminClient.listTopics().names().get().contains(dlqTopic);

        if (!dlqTopicExists) {
            int partitions = new Integer(streamProps.getProperty("num.partitions"));
            int replication = new Integer(streamProps.getProperty("replication.factor"));
            NewTopic newTopic = new NewTopic(dlqTopic, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }

        // Create a stream of flights
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Object> flights = builder.stream(inputTopic);

        String pattern = "^[A-Z]{3}\\d{3}$"; // 3 upper case characters and 3 digits

        // *** FILTERING
        final KStream<String, Object> rule1 = flights.
                filter((k, v) -> (
                        // FILTER rule 1 - move to DLQ if origin country not "United States"
                            !((FlightStateSchema1) v).getOriginCountry().equals("United States") ||
                        // FILTER rule 2 - move to DLQ if call sign does starts with "C"
                            ((FlightStateSchema1) v).getCallsign().startsWith("C") ||
                        // FITLER rule 3 - move to DLQ if call sign does not match "[a-zA-Z].*?\b"
                        !Pattern.matches(pattern, ((FlightStateSchema1) v).getCallsign())
                 ));

        rule1.print(Printed.toSysOut());
        rule1.to(dlqTopic);

        // *** TRANSFORMATIONS
        final KStream<String, Object> rule2 = flights.
                map((k, v) -> {
                    // TRANSFORM rule 1
                    String upperOriginCountry = ((FlightStateSchema1) v).getOriginCountry().toUpperCase();
                    ((FlightStateSchema1) v).setOriginCountry(upperOriginCountry);

                    // TRANSFORM rule 2
                    if (((FlightStateSchema1) v).getSquawk() == null) {
                        ((FlightStateSchema1) v).setSquawk("N/A");
                    }

                    // TRANSFORM rule 3
                    String icao = ((FlightStateSchema1) v).getIcao24();
                    ((FlightStateSchema1) v).setIcao24(icao.substring(3));

                    return KeyValue.pair(k,v);
                });
        rule2.print(Printed.toSysOut());
        rule2.to(cleanTopic);

        // *** DEDUP
        // https://stackoverflow.com/questions/55803210/how-to-handle-duplicate-messages-using-kafka-streaming-dsl-functions

        final KafkaStreams streams = new KafkaStreams(builder.build(), streamProps);
        streams.start();
    }

    public static void main(final String[] args) throws Exception {
        resourcesDir = args[0];
        propertiesFile = args[1];



        new QualityRules().run();
    }
}
