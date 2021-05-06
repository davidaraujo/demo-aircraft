package io.confluent.demo.aircraft.jsonschema.consumer;

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

import io.confluent.demo.aircraft.jsonschema.pojo.FlightStateSchema1;
import io.confluent.demo.aircraft.jsonschema.pojo.FlightStateSchema2;
import io.confluent.demo.aircraft.jsonschema.pojo.FlightStateSchema3;
import io.confluent.demo.aircraft.jsonschema.utils.FlightPrettyPrint;
import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.demo.aircraft.utils.ColouredSystemOutPrintln;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializerConfig;
import org.apache.kafka.clients.consumer.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * <h1>Consume JSON flight messages from Kafka and use Schema Registry for JSON Schema format validation</h1>
 * The ConsumerApp application subscribes to the topic in Kafka with flights location events
 * <pre>
 * mvn compile -DskipGenPOJO
 * mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.jsonschema.consumer.AirTrafficControl" -Dexec.args="./src/main/resources ccloud_prod.properties 1 flights client1 group1 20"
 * </pre>
 *
 * @author David Araujo
 * @version 1.1
 * @since 2020-12-22
 */

/* AirTrafficControl domain/bounded context */
/* AirTrafficControl ubiquitous Language:
 *  */
public class AirTrafficControl  implements Runnable {

    private String resourcesDir;
    private String propertiesFile;
    private String schemaVersion;
    private String topicName;
    private String clientId;
    private String groupId;

    public AirTrafficControl(String resourcesDir,
                             String propertiesFile,
                             String schemaVersion,
                             String topicName,
                             String clientId,
                             String groupId) {
        this.resourcesDir = resourcesDir;
        this.propertiesFile = propertiesFile;
        this.schemaVersion = schemaVersion;
        this.topicName = topicName;
        this.clientId = clientId;
        this.groupId = groupId;
    }

    public void getAirspaceInformation() throws IOException {

        // Load properties file that contains Kafka and Schema Registry properties - cp_local.properties; ccloud_devel.properties; ccloud_prod.properties
        final Properties consumerProps = ClientsUtils.loadConfig(resourcesDir + "/" + propertiesFile);

        // *** Set all the properties for the consumer

        // Key deserializer String
        consumerProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // Value deserializer KafkaJsonSchemaSerializer
        consumerProps.setProperty("value.deserializer", "io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer");

        // Set validation for JSONSchema
        consumerProps.setProperty("json.fail.invalid.schema", "true");

        // Choose a POJO based on the schema version
        if (schemaVersion.equals("1"))
            consumerProps.put(KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE, "io.confluent.demo.flights.jsonschema.pojo.FlightStateSchema1");
        else if (schemaVersion.equals("2"))
            consumerProps.put(KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE, "io.confluent.demo.flights.jsonschema.pojo.FlightStateSchema2");
        else if (schemaVersion.equals("3"))
            consumerProps.put(KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE, "io.confluent.demo.flights.jsonschema.pojo.FlightStateSchema3");

        // Assign a client and group id to the consumer
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Set the offset to earliest
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create a consumer
        final Consumer<String, Object> consumer = new KafkaConsumer<String, Object>(consumerProps);
        consumer.subscribe(Arrays.asList(topicName));

        Long total_count = 0L;

        try {
            while (true) {

                ConsumerRecords<String, Object> records = consumer.poll(10);

                for (ConsumerRecord<String, Object> record : records) {

                    String key = record.key();
                    Object value = record.value();
                    total_count += 1;

                    System.out.print(ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_GREEN +
                           "Consumer group " + groupId + " client " + clientId + " consuming message " + total_count + ColouredSystemOutPrintln.ANSI_RESET);

                    // color the records depending on the schema name (for demo to highlight different schema versions)
                    if (schemaVersion.equals("1"))
                        System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_BLUE);
                    else if (schemaVersion.equals("2"))
                        System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_PURPLE);
                    else if (schemaVersion.equals("3"))
                        System.out.print("\n" + ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_YELLOW);
                    else
                        System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_BLUE);

                    if (schemaVersion.equals("1"))
                        System.out.println(new FlightPrettyPrint().printFlightV1((FlightStateSchema1) value) + ColouredSystemOutPrintln.ANSI_RESET);
                    else if (schemaVersion.equals("2"))
                        System.out.println(new FlightPrettyPrint().printFlightV2((FlightStateSchema2) value) + ColouredSystemOutPrintln.ANSI_RESET);
                    else if (schemaVersion.equals("3"))
                        System.out.println(new FlightPrettyPrint().printFlightV3((FlightStateSchema3) value) + ColouredSystemOutPrintln.ANSI_RESET);
                }
            }
        } finally {
            consumer.close();
        }
    }

    /**
     * This is the main method which consumers records from Kafka.
     *
     * @param args resourcesDir propertiesFile serializationType schemaVersion topicName clientID groupID
     * @param args [0] The resources directory
     * @param args [1] The properties filename
     * @param args [2] The version of the schema to use
     * @param args [3] The name of the topic to subscribe
     * @param args [4] The client Id
     * @param args [5] The group Id
     * @param args [6] The number of consumer groups threads to run
     * @return Nothing.
     */
    public static void main(final String[] args) throws Exception {

        int numArgs = args.length;

        if (numArgs < 6) {
            System.out.println("Please provide command line arguments: resourcesDir propertiesFile schema topicName clientId groupId numberThreads(optional) groups|consumers");
            System.exit(1);
        }

        String resourcesDir = args[0];
        String propertiesFile = args[1];
        String schemaVersion = args[2];
        String topicName = args[3];
        String clientId = args[4];
        String groupId = args[5];
        // get number of threads to run, if parameter not passed run 1 thread
        int numberThreads = ((numArgs >= 7)) ? Integer.parseInt(args[6]) : 0;
        String groupOrconsumer = ((numArgs == 8)) ? args[7] : "";

        // run one producer thread
        if (numberThreads == 0) {
            io.confluent.demo.aircraft.jsonschema.consumer.AirTrafficControl airTrafficControl = new io.confluent.demo.aircraft.jsonschema.consumer.AirTrafficControl(resourcesDir, propertiesFile, schemaVersion, topicName, clientId, groupId);
            new Thread(airTrafficControl).start();
        }
        // run multiple consumer groups or consumers inside the group threads
        else for (int i = 0; i < numberThreads; i++) {
            io.confluent.demo.aircraft.jsonschema.consumer.AirTrafficControl airTrafficControl = new io.confluent.demo.aircraft.jsonschema.consumer.AirTrafficControl(resourcesDir, propertiesFile, schemaVersion, topicName, (groupOrconsumer.equals("consumers")) ? clientId + "-" + i :  clientId, (groupOrconsumer.equals("groups")) ? groupId + "-" + i : groupId);

            String consumer = (groupOrconsumer.equals("consumers")) ? clientId + "-" + i :  clientId;
            String group = (groupOrconsumer.equals("groups")) ? groupId + "-" + i : groupId;

            System.out.println("group = " + group);
            System.out.println("consumer = " + group);

            new Thread(airTrafficControl).start();
        }

        // run multiple consumers threads inside a consumer group
        /*
        else for (int i = 0; i < numberThreads; i++) {

            System.out.println("New consumer thread " + i + "...");
            io.confluent.demo.flights.jsonschema.consumer.AirTrafficControl airTrafficControl = new io.confluent.demo.flights.jsonschema.consumer.AirTrafficControl(resourcesDir, propertiesFile, schemaVersion, topicName, clientId + "-" + i, groupId );
            new Thread(airTrafficControl, clientId).start();
        }*/
    }

    @Override
    public void run() {
        try {
            this.getAirspaceInformation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
