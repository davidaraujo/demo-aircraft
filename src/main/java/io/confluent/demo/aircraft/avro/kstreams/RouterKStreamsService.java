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

package io.confluent.demo.aircraft.avro.kstreams;

import io.confluent.demo.aircraft.avro.pojo.AircraftState;
import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.demo.aircraft.utils.ColouredSystemOutPrintln;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
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

public class RouterKStreamsService {

    private final String resourcesDir;
    private final String confluentPropsFile;
    private final String kstreamsPropsFile;

    public RouterKStreamsService(String resourcesDir, String confluentPropsFile, String kstreamsPropsFile) {
        this.resourcesDir = resourcesDir;
        this.confluentPropsFile = confluentPropsFile;
        this.kstreamsPropsFile = kstreamsPropsFile;
    }

    public void run() throws Exception {

        String aircraftTopic;
        String onGroundTopic;
        String inFlightTopic;
        String unidentifiedTopic;

        // ----------------------------- Load properties -----------------------------
        final Properties props = ClientsUtils.loadConfig(resourcesDir + "/" + confluentPropsFile);
        final Properties kStreamsProps = ClientsUtils.loadConfig(resourcesDir + "/" + kstreamsPropsFile);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, kStreamsProps.getProperty("kstreams.router.application.id"));
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        //streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //streamProps.put("json.value.type", "io.confluent.demo.flights.jsonschema.pojo.FlightStateSchema1");

        aircraftTopic = kStreamsProps.getProperty("aircraft.topic.name");
        onGroundTopic = kStreamsProps.getProperty("aircraft_onground.topic.name");
        unidentifiedTopic = kStreamsProps.getProperty("aircraft_unidentified.topic.name");
        inFlightTopic = kStreamsProps.getProperty("aircraft_inflight.topic.name");

        // ----------------------------- Create the ouput Kafka topics -----------------------------
        AdminClient adminClient = AdminClient.create(props);
        int partitions = new Integer(props.getProperty("num.partitions"));
        int replication = new Integer(props.getProperty("replication.factor"));
        // Create an onGround topic if it doesn't exist
        boolean onGroundTopicExists = adminClient.listTopics().names().get().contains(onGroundTopic);
        if (!onGroundTopicExists) {
            NewTopic newTopic = new NewTopic(onGroundTopic, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }
        // Create an unidentified topic if it doesn't exist already
        boolean unidentifiedTopicExists = adminClient.listTopics().names().get().contains(unidentifiedTopic);
        if (!unidentifiedTopicExists) {
            NewTopic newTopic = new NewTopic(unidentifiedTopic, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }
        // Create an inFlight topic if it doesn't exist already
        boolean inFlightTopicExists = adminClient.listTopics().names().get().contains(inFlightTopic);
        if (!inFlightTopicExists) {
            NewTopic newTopic = new NewTopic(inFlightTopic, partitions, (short) replication);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }

        // ----------------------------- Create KStream -----------------------------
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Object> flights = builder.stream(aircraftTopic);

        //String pattern = "^[A-Z]{3}\\d{3}$"; // 3 upper case characters and 3 digits
        //String pattern = "[A-Z]{3}"; // 3 upper case characters and 3 digits
        // String pattern = "^[a-zA-Z]{1}";

        // RULE 1: if callsign is empty move record to unidentified topic
        final KStream<String, Object> ruleUnidentified = flights.
                filter((k, v) -> (
                        ((AircraftState) v).getCallsign().equals("")
                ));
        //ruleUnidentified.print(Printed.toSysOut());
        Printed un = Printed.toSysOut().withLabel(ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_RED + "Unidentified\n");
        ruleUnidentified.print(un);
        ruleUnidentified.to(unidentifiedTopic);

        // RULE 2: if onground is true move record to onground topic
        final KStream<String, Object> ruleOnGround = flights.
                filter((k, v) -> (
                        ((AircraftState) v).getOnGround()
                 ));
        //ruleOnGround.print(Printed.toSysOut());
        Printed on = Printed.toSysOut().withLabel(ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_YELLOW + "OnGround");
        ruleOnGround.print(on);
        ruleOnGround.to(onGroundTopic);

        // RULE 3: if onground is false move record to inflight topic
        final KStream<String, Object> ruleInFlight = flights.
                filter((k, v) -> (
                        !((AircraftState) v).getOnGround()
                ));
        //ruleInFlight.print(Printed.toSysOut());
        Printed in = Printed.toSysOut().withLabel(ColouredSystemOutPrintln.ANSI_BLACK + ColouredSystemOutPrintln.ANSI_BG_CYAN + "InFlight");
        ruleInFlight.print(in);
        ruleInFlight.to(inFlightTopic);

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }

    public static void main(final String[] args) throws Exception {
        int numArgs = args.length;
        if (numArgs < 3) {
            System.out.println("Please provide command line arguments: resourcesDir kafkaPropertiesFile kStreamsPropertiesFile");
            System.exit(1);
        }
        new RouterKStreamsService(args[0], args[1], args[2]).run();
    }
}
