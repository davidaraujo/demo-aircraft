package io.confluent.demo.aircraft.jsonschema.kstreams;

import io.confluent.demo.aircraft.jsonschema.pojo.FlightStateSchema1;
import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Collections;
import java.util.Properties;

// mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.jsonschema.streamprocessing.FilterByCountry" -Dexec.args="./src/main/resources/ ccloud_prod.properties usa"
// mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.jsonschema.streamprocessing.FilterByCountry" -Dexec.args="./src/main/resources/ ccloud_prod.properties canada"
public class FilterByCountry {

    static String resourcesDir;
    static String propertiesFile;
    static String country;

    public void run() throws Exception {

        String inputTopic;
        String outputTopic;
        Properties streamProps;
        streamProps = ClientsUtils.loadConfig(resourcesDir + "/" + propertiesFile);

        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstreams-filter-country");
        streamProps.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaJsonSchemaSerde.class);
        streamProps.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        //streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        streamProps.put("json.value.type", "io.confluent.demo.flights.jsonschema.pojo.FlightStateSchema1");

        inputTopic = streamProps.getProperty("input.topic.name");
        outputTopic = streamProps.getProperty("output.topic." + country + ".name");

        System.out.println("Output topic: " + outputTopic);

        // Create a new topic if it doesn't exist already
        AdminClient adminClient = AdminClient.create(streamProps);
        boolean topicExists = adminClient.listTopics().names().get().contains(outputTopic);

        if (!topicExists) {
            int partitions = new Integer(streamProps.getProperty("num.partitions"));
            int replication = new Integer(streamProps.getProperty("replication.factor"));
            NewTopic newTopic = new NewTopic(outputTopic, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }

        final StreamsBuilder builder = new StreamsBuilder();
        // Example to read and write to topic using kstreams without any manipulation
        //builder.<String, Message>stream(inputTopic).mapValues(value -> value).to(outputTopic);

        // Create a stream of flights filter by a specific country of origin
        final KStream<String, Object> flights = builder.stream(inputTopic);

        // if arg usa then filter by "United States"
        if (country.equals("usa")) country = "United States";

        final KStream<String, Object> filterByCountry = flights.
                filter((k, v) -> ((FlightStateSchema1) v).getOriginCountry().toLowerCase().equals(country.toLowerCase()));
        filterByCountry.print(Printed.toSysOut());
        filterByCountry.to(outputTopic);

        // 2nd filter from output topic above to generic topic
       // builder.<String, FlightStateSchema1>stream(outputTopic).mapValues(value -> value).to("generic");

        final KafkaStreams streams = new KafkaStreams(builder.build(), streamProps);
        streams.start();
    }

    public static void main(final String[] args) throws Exception {
        resourcesDir = args[0];
        propertiesFile = args[1];
        country = args[2];

        new FilterByCountry().run();
    }
}
