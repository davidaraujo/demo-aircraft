/**
 * Copyright 2019 Confluent Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.demo.aircraft.jsonschema.consumer;

import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.demo.aircraft.utils.PrettyPrint;
import org.apache.kafka.clients.consumer.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author David Araujo
 * @version 1.1
 * @since 2020-12-22
 */
public class GenericJsonConsumerService implements Runnable {

    private final String resourcesDir;
    private final String propertiesFile;
    private final String topicName;
    private final String groupId;
    private final String clientId;

    public GenericJsonConsumerService(String resourcesDir,
                                      String propertiesFile,
                                      String topicName,
                                      String groupId,
                                      String clientId) {
        this.resourcesDir = resourcesDir;
        this.propertiesFile = propertiesFile;
        this.topicName = topicName;
        this.groupId = groupId;
        this.clientId = clientId;
    }

    public void getRecords() throws IOException {

        // ----------------------------- Load properties -----------------------------
        // Kafka and Schema Registry properties
        final Properties props = ClientsUtils.loadConfig(resourcesDir + "/" + propertiesFile);

        // ----------------------------- Set consumer properties -----------------------------
        // Key deserializer - String
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        // Value deserializer - KafkaJsonSchemaSerializer
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer");
        // Assign a group id and client id to the consumer
        if (groupId != null)
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        if (clientId != null)
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        // Set the offset to earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // ----------------------------- Create Kafka consumer subscribing from topic -----------------------------
        final Consumer<String, Object> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));

        // ----------------------------- Consume records from Kafka -----------------------------
        try {
            while (true) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, Object> record : records) {
                    PrettyPrint.consumerRecord(groupId, ((clientId == null)) ? "Unidentified": clientId, topicName, record.partition(), record.offset(), record.key().toString(), record.value().toString(), "jsonschema");
                }
            }
        } finally {
            consumer.close();
        }
    }

    /**
     * @param args resourcesDir propertiesFile topicName groupID clientID numberGroups numberClients
     * @param args [0] The resources directory
     * @param args [1] The properties filename
     * @param args [2] The name of the topic to subscribe
     * @param args [3] The group Id
     * @param args [4] The client Id
     * @param args [5] The number of consumer groups to run
     * @param args [6] The number of clients inside a group to run
     * @return Nothing.
     */
    public static void main(final String[] args) throws Exception {
        int numArgs = args.length;

        if (numArgs < 4) {
            System.out.println("Please provide command line arguments: resourcesDir propertiesFile topicName groupId clientId(optional) numberGroupThreads(optional) numberClientThreads(optional)");
            System.exit(1);
        }

        String resourcesDir = args[0];
        String propertiesFile = args[1];
        String topicName = args[2];
        String groupId = args[3] ;
        String clientId = ((numArgs >= 5)) ? args[4] : null;
        int numberGroupThreads = ((numArgs >= 6)) ? Integer.parseInt(args[5]) : 0;
        int numberClientThreads = ((numArgs == 7)) ? Integer.parseInt(args[6]) : 0;

        // run one consumer thread
        if (numArgs < 6) {
            GenericJsonConsumerService microService = new GenericJsonConsumerService(resourcesDir, propertiesFile, topicName, groupId, clientId);
            new Thread(microService).start();
        }
        // run multiple group and clients threads
        else for (int g = 0; g < numberGroupThreads; g++) {
            for (int c = 0; c < numberClientThreads; c++) {
                GenericJsonConsumerService microService = new GenericJsonConsumerService(resourcesDir, propertiesFile, topicName, groupId + "." + g, groupId + "." + g + "-" + clientId + "." + c);
                new Thread(microService).start();
            }
        }
    }

    @Override
    public void run() {
        try {
            this.getRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
