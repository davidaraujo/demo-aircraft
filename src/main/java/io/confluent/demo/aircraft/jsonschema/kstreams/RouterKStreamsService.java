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

package io.confluent.demo.aircraft.jsonschema.kstreams;

import io.confluent.demo.aircraft.jsonschema.pojo.AircraftState;
import io.confluent.demo.aircraft.utils.ClientsUtils;
import io.confluent.demo.aircraft.utils.ColouredSystemOutPrintln;
import io.confluent.demo.aircraft.utils.TopicAdmin;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Properties;

public class RouterKStreamsService {

    private final String resourcesDir;
    private final String confluentPropsFile;
    private final String applicationId;
    private final String aircraftTopic;
    private final String onGroundTopic;
    private final String inFlightTopic;
    private final String unidentifiedTopic;
    private final String clientId;

    public RouterKStreamsService(String resourcesDir, String confluentPropsFile, String applicationId,
                                 String aircraftTopic, String onGroundTopic, String inFlightTopic, String unidentifiedTopic,
                                 String clientId) {
        this.resourcesDir = resourcesDir;
        this.confluentPropsFile = confluentPropsFile;
        this.applicationId = applicationId;
        this.aircraftTopic = aircraftTopic;
        this.onGroundTopic = onGroundTopic;
        this.inFlightTopic = inFlightTopic;
        this.unidentifiedTopic = unidentifiedTopic;
        this.clientId = clientId;
    }

    public void run() throws Exception {
        // ----------------------------- Load properties -----------------------------
        final Properties props = ClientsUtils.loadConfig(resourcesDir + "/" + confluentPropsFile);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaJsonSchemaSerde.class);
        props.put("json.value.type", "io.confluent.demo.aircraft.jsonschema.pojo.AircraftState");
        props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        //streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // ----------------------------- Create Kafka topics  -----------------------------
        TopicAdmin.createTopic(props, onGroundTopic);
        TopicAdmin.createTopic(props, inFlightTopic);
        TopicAdmin.createTopic(props, unidentifiedTopic);

        // ----------------------------- Create KStream -----------------------------
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, Object> flights = builder.stream(aircraftTopic);

        //String pattern = "^[A-Z]{3}\\d{3}$"; // 3 upper case characters and 3 digits
        //String pattern = "[A-Z]{3}"; // 3 upper case characters and 3 digits
        // String pattern = "^[a-zA-Z]{1}";

        // ----------------------------- Stream processing based on 3 rules -----------------------------
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
        if (numArgs < 6) {
            System.out.println("Please provide command line arguments: resourcesDir kafkaPropertiesFile applicationID aircraftTopic onGroundTopic unidentifiedTopic clientID(optional)");
            System.exit(1);
        }
        // clientId is optional
        String clientId = ((numArgs >= 8)) ? args[7] : null;

        new RouterKStreamsService(args[0], args[1], args[2], args[3], args[4], args[5], args[6], clientId).run();
    }
}

