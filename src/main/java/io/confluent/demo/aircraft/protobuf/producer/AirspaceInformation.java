package io.confluent.demo.aircraft.protobuf.producer;

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

import io.confluent.demo.aircraft.protobuf.pojo.FlightState1;
import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.demo.aircraft.utils.ColouredSystemOutPrintln;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.*;
import org.opensky.api.OpenSkyApi;
import org.opensky.model.OpenSkyStates;
import org.opensky.model.StateVector;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * <h1>Produce Protobuf flight messages to Kafka and use Schema Registry for Protobuf format validation</h1>
 * The ProducerApp application connects to the OpenSKY network API on an infinite loop and produces flights location events to Kafka
 * <pre>
 * <h2>JSON SCHEMA</h2>
 * <b>CCLOUD PROD</b>:
 * mvn compile -DskipGenPOJO
 * mvn jar:jar -DskipGenPOJO
 * mvn exec:java -Dexec.mainClass="io.confluent.demo.flights.protobuf.producer.AirspaceInformation" -Dexec.args="./src/main/resources ccloud_prod.properties flights-protobuf opensky.properties OpenSKY-App 10"
 * java -cp ~/.m2/repository/org/apache/kafka/kafka-clients/2.5.0/kafka-clients-2.5.0.jar:~/.m2/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar:target/demo-flights-1.0-SNAPSHOT.jar io.confluent.demo.flights.protobuf.ProducerApp ./src/main/resources ccloud_prod.properties flights opensky.properties OpenSKY-App
 * </pre>
 *
 * @author David Araujo
 * @version 1.1
 * @since 2020-12-22
 */

/* AirspaceInformation domain/bounded context */
/* AirspaceInformation ubiquitous Language:
*
*
*  */
public class AirspaceInformation implements Runnable {

    String resourcesDir;
    String propertiesFile;
    String topicName;
    String openSkyPropsFile;
    String clientId;

    public AirspaceInformation(String resourcesDir,
                               String propertiesFile,
                               String topicName,
                               String openSkyPropsFile,
                               String clientId) {
        this.resourcesDir = resourcesDir;
        this.propertiesFile = propertiesFile;
        this.topicName = topicName;
        this.openSkyPropsFile = openSkyPropsFile;
        this.clientId = clientId;
    }

    /*
    * Airplanes seen by the OpenSky Network are associated with flight state information derived
    * from ADS-B and Mode S messages. The state of an aircraft is a summary of all tracking
    * information (mainly position, velocity, and identity) at a certain point in time.
    * These states of aircraft can be retrieved as state vectors in the form of a JSON object.
    */
    public void publishAirspaceInformation()
            throws IOException, ExecutionException, InterruptedException {

        // Load properties file that contains Kafka and Schema Registry properties
        final Properties producerProps = ClientsUtils.loadConfig(resourcesDir + "/" + propertiesFile);

        // Load properties file that contains the OpenSky credentials and flights coordinates
        final Properties openSkyProps = ClientsUtils.loadConfig(resourcesDir + "/" + openSkyPropsFile);

        // *** Set all the properties for the producer

        // Assign a client id to the producer
        if (clientId != null)
            producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);

        // Set the subject naming strategy to use
        //props.setProperty("value.subject.name.strategy", RecordNameStrategy.class.getName());

        // Auto register the schema
        producerProps.setProperty("auto.register.schemas", "true");

        // Key serializer String
        producerProps.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Value serializer KafkaProtobufSerializer
        producerProps.setProperty("value.serializer", "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer");

        // Create a new topic if it doesn't exist already
        AdminClient adminClient = AdminClient.create(producerProps);
        boolean topicExists = adminClient.listTopics().names().get().contains(topicName);
        if (!topicExists) {
            int partitions = new Integer(producerProps.getProperty("num.partitions"));
            int replication = new Integer(producerProps.getProperty("replication.factor"));
            NewTopic newTopic = new NewTopic(topicName, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }

        // Create a producer
        Producer<String, Object> producer = new KafkaProducer<>(producerProps);

        String key = null;
        Object value = null;

        // Authentication on the OpenSKY network API
        OpenSkyApi api = new OpenSkyApi((String)openSkyProps.get("username"), (String)openSkyProps.get("password"));

        // Configure the geo coordinates to get airspace information from
        OpenSkyApi.BoundingBox coordinates = new OpenSkyApi.BoundingBox(
                Double.parseDouble((String)openSkyProps.get("minLatitude")),
                Double.parseDouble((String)openSkyProps.get("maxLatitude")),
                Double.parseDouble((String)openSkyProps.get("minLongitude")),
                Double.parseDouble((String)openSkyProps.get("maxLongitude")));

        // Infinite loop getting aircraft data from OpenSKY and producing those events to Kafka
        while (true) {

            // Get new flights for every loop run and store it in a collection
            OpenSkyStates os;
            try {
                os = api.getStates(0, null, coordinates);
            }
            catch (Exception e) {
                System.out.println("No connection to OpenSKY server, trying again ...");
                continue;
            }

            Collection states = os.getStates();
            Iterator it = states.iterator();
            FlightState1.FlightState.Builder flight;

            // Iterate over the collection of flights and produce events to Kafka
            while (it.hasNext()) {
                try {
                    StateVector state = (StateVector) it.next();

                    // 1 - Set the key as the call sign id that represents the flight number (so we get an ordered sequence of a flight id)
                    key = state.getCallsign();

                    flight = FlightState1.FlightState.newBuilder();
                    flight.setBaroAltitude(state.getBaroAltitude());
                    flight.setCallsign(state.getCallsign().trim());
                    flight.setGeoAltitude(state.getGeoAltitude());
                    flight.setBaroAltitude(state.getBaroAltitude());
                    flight.setIcao24(state.getIcao24());
                    flight.setLastContact(state.getLastContact().intValue());
                    flight.setLatitude(state.getLatitude());
                    flight.setLongitude(state.getLongitude());
                    //flight.setOnGround(state.isOnGround());
                    flight.setOriginCountry(state.getOriginCountry());
                    flight.setSpi(state.isSpi());
                    //????? flight.setSquawk(state.getSquawk());
                    flight.setTimePosition(state.getLastPositionUpdate());
                    flight.setTrueTrack(state.getHeading());
                    flight.setVelocity(state.getVelocity());
                    flight.setVerticalRate(state.getVerticalRate());

                    // 2 - Set value as a Protobuf flight
                    value = flight.build();
                    Object copyValue = value;

                    // Produce the event to the topic in Kafka
                    producer.send(new ProducerRecord<>(topicName, key, value), new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata m, Exception e) {
                            if (e != null) {
                                e.printStackTrace();
                            } else {
                                // Print the message to the terminal and use the color blue to highlight that the version 1 of the schema is being used
                                System.out.print(ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_GREEN +
                                        "Producing message to topic " + m.topic() + " partition " + m.partition() + " [" + m.partition() + " ] @ offset " + m.offset() + ColouredSystemOutPrintln.ANSI_RESET);
                                System.out.print("\n" + ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_BLUE);
                                System.out.println(copyValue.toString() + ColouredSystemOutPrintln.ANSI_RESET);
                                //System.out.println(FlightPrettyPrint.printFlightV1((FlightState1.FlightState.Builder) copyValue) + ColouredSystemOutPrintln.ANSI_RESET);
                            }
                        }
                    });
                    // wait 1000 milliseconds between each message produce
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    System.out.println(ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_RED);
                    System.out.println(ex.toString());
                    System.out.println(ex.getCause().getCause());
                    System.out.println(ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_RED);
                    System.out.println(">>> Skipping bad record.");
                    continue;
                }
            }
           // producer.flush();
           // producer.close();
        }
    }

    public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {

        int numArgs = args.length;

        if (numArgs < 4) {
            System.out.println("Please provide command line arguments: resourcesDir kafkaPropertiesFile topicName openSkyPropertiesFile clientID(optional) numberThreads(optional)");
            System.exit(1);
        }

        String resourcesDir = args[0];
        String propertiesFile = args[1];
        String topicName = args[2];
        String openSkyPropsFile = args[3];
        String clientId = ((numArgs >= 5)) ? args[4] : null;
        // get number of producer threads to run, if parameter not passed run 1 thread
        int numberThreads = ((numArgs == 6)) ? Integer.parseInt(args[5]) : 0;

        // run one producer thread
        if (numberThreads == 0) {
            io.confluent.demo.aircraft.protobuf.producer.AirspaceInformation airspaceInformation = new io.confluent.demo.aircraft.protobuf.producer.AirspaceInformation(resourcesDir, propertiesFile, topicName, openSkyPropsFile, clientId);
            new Thread(airspaceInformation, clientId).start();
        }
        // run multiple producer threads
        else for (int i = 0; i < numberThreads; i++) {
            io.confluent.demo.aircraft.protobuf.producer.AirspaceInformation airspaceInformation = new io.confluent.demo.aircraft.protobuf.producer.AirspaceInformation(resourcesDir, propertiesFile, topicName, openSkyPropsFile, clientId + "-" + i);
            new Thread(airspaceInformation, clientId).start();
        }
    }

    @Override
    public void run() {
        try {
            this.publishAirspaceInformation();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
