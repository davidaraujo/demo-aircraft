package io.confluent.demo.aircraft.protobuf.streamprocessing;

// import io.confluent.demo.pojo.jsonschema.Order;

import io.confluent.demo.aircraft.protobuf.pojo.FlightState1;
import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Collections;
import java.util.Properties;

// mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.protobuf.streamprocessing.FilterByCountry" -Dexec.args="./src/main/resources/ ccloud_prod.properties usa"
// mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.protobuf.streamprocessing.FilterByCountry" -Dexec.args="./src/main/resources/ ccloud_prod.properties canada"
public class FilterByCountry {

    static String resourcesDir;
    static String propertiesFile;
    static String country;

    final Serde<byte[]> byteArraySerde = Serdes.ByteArray();

    final Class<KafkaProtobufSerde> jsonSerde = KafkaProtobufSerde.class;

    public void run() throws Exception {

        String inputTopic;
        String outputTopic;
        Properties streamsConfiguration = new Properties();
        streamsConfiguration = ClientsUtils.loadConfig(resourcesDir + "/" + propertiesFile);

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstreams-filter-country");
        streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // TODO bug
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaProtobufSerializer.class);

        streamsConfiguration.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        //streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Class pojoClass = Class.forName("io.confluent.demo.aircraft.protobuf.pojo.FlightState1");
        pojoClass = Class.forName("io.confluent.demo.aircraft.protobuf.pojo.FlightState1");
        Class<?> innerPojoClass = pojoClass.getDeclaredClasses()[0];
        streamsConfiguration.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, innerPojoClass);

        //streamsConfiguration.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, "io.confluent.demo.flights.protobuf.pojo.FlightV1");


        inputTopic = streamsConfiguration.getProperty("input.topic.name");
        outputTopic = streamsConfiguration.getProperty("output.topic." + country + ".name");


        System.out.println("Output topic: " + outputTopic);

        // Create output topic if it doesn't exist already TODO bug
        AdminClient adminClient = AdminClient.create(streamsConfiguration);
        boolean topicExists = adminClient.listTopics().names().get().stream().anyMatch(topic -> outputTopic.equalsIgnoreCase(outputTopic));
        System.out.println("Output topic exists? " + topicExists);
        if (!topicExists) {
            int partitions = new Integer(streamsConfiguration.getProperty("output.topic." + country + ".partitions"));
            int replication = new Integer(streamsConfiguration.getProperty("output.topic." + country + ".replication.factor"));
            System.out.println("Creating new topic: " + outputTopic);
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
                filter((k, v) -> ((FlightState1.FlightState.Builder) v).getOriginCountry().toLowerCase().equals(country.toLowerCase()));
        filterByCountry.print(Printed.toSysOut());
        filterByCountry.to(outputTopic);

        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        streams.start();
    }

    public static void main(final String[] args) throws Exception {
        resourcesDir = args[0];
        propertiesFile = args[1];
        country = args[2];

        new FilterByCountry().run();
    }
}
