package io.confluent.demo.aircraft.jsonschema.kstreams;

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

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

// mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.jsonschema.kstreams.QualityRules" -Dexec.args="./src/main/resources/ ccloud_prod.properties"
public class BasicStreamProcessing implements Runnable {

     String resourcesDir;
     String propertiesFile;
     String topicName;
     int threadNumber;

    public BasicStreamProcessing(String resourcesDir,
                                 String propertiesFile,
                                 String topicName,
                                 int threadNumber) {
        this.resourcesDir = resourcesDir;
        this.propertiesFile = propertiesFile;
        this.topicName = topicName;
        this.threadNumber = threadNumber;
    }

    /*
     */
    public void process() {

        Properties streamProps;
        String inputTopic;

        try {
            streamProps = ClientsUtils.loadConfig(resourcesDir + "/" + propertiesFile);

            streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstreams-basic-" + threadNumber);
            streamProps.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
            streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
            streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaJsonSchemaSerde.class);
            streamProps.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);

            streamProps.put("json.value.type", "io.confluent.demo.flights.jsonschema.pojo.FlightStateSchema1");

            if (threadNumber == 0)
                inputTopic = topicName;
            else
                inputTopic = topicName + threadNumber;

            String outputTopic = topicName + (threadNumber+1);

            System.out.println("thread: " + threadNumber + " ; input topic: " + inputTopic + " ; output topic:" + outputTopic);

            // Create a clean topic if it doesn't exist already
            AdminClient adminClient = AdminClient.create(streamProps);
            boolean outputTopicExists = adminClient.listTopics().names().get().contains(outputTopic);

            if (!outputTopicExists) {
                int partitions = new Integer(streamProps.getProperty("num.partitions"));
                int replication = new Integer(streamProps.getProperty("replication.factor"));
                NewTopic newTopic = new NewTopic(outputTopic, partitions, (short) replication);
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            }

            // Just copy messages to the output topic
            final StreamsBuilder builder = new StreamsBuilder();

            final KStream<String, Object> flights = builder.stream(inputTopic);
            final KStream<String, Object> passAlong = flights.
                    mapValues(( value -> value));
            //passAlong.print(Printed.toSysOut());
            passAlong.to(outputTopic);

            //builder.<String, FlightStateSchema1>stream(inputTopic).mapValues(value -> value).to(outputTopic);

            final KafkaStreams streams = new KafkaStreams(builder.build(), streamProps);
            streams.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) throws Exception {
        String resourcesDir = args[0];
        String propertiesFile = args[1];
        String topicName = args[2];
        int numberThreads = Integer.parseInt(args[3]);
        // run one kstreams thread
        if (numberThreads == 0) {
            BasicStreamProcessing basicStreamProcessing = new BasicStreamProcessing(resourcesDir, propertiesFile, topicName, 0);
            new Thread(basicStreamProcessing).start();
        }
        // run multiple kstreams threads
        else for (int i = 0; i < numberThreads; i++) {
            Thread.sleep(4000);
            BasicStreamProcessing basicStreamProcessing = new BasicStreamProcessing(resourcesDir, propertiesFile, topicName, i);
            new Thread(basicStreamProcessing).start();
        }
    }

    @Override
    public void run() {
        try {
            this.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
