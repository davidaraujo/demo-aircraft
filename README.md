
# Aircraft location demo

This demo consists of multiple microservice applications producing and consuming aircraft location events to Kafka. To structure the data representing these events we will use one of the 3 schema types supported on the Confluent Schema Registry:
* [Avro](http://avro.apache.org/)
* [Protocol Buffers](https://developers.google.com/protocol-buffers)
* [JSON Schema](https://json-schema.org/)

The aircraft location data is provided by the [OpenSKY Network](https://opensky-network.org/):

*Airplanes seen by the OpenSky Network are associated with flight state information derived
from ADS-B and Mode S messages. The state of an aircraft is a summary of all tracking
information (mainly position, velocity, and identity) at a certain point in time.
These states of aircraft can be retrieved as state vectors in the form of a JSON object.*

![Alt text](aircraft-radar-blue.png?raw=true "Order")

We'll have 4 applications running:
* **TrackingService app** 
    *  Produces aircraft location events to topic A.
* **RouterService app (KStreams)**
    * Consumes aircraft location events from topics A and depending on its positioning produces to topic B (aircraft on the ground), C (aircraft in flight) or D (aircraft unidentified).
* **OnGroundService**
  * Consumes aircraft location events from topics B.
* **InFlightService**
  * Consumes aircraft location events from topics C.
* **UnidentifiedService**
  * Consumes aircraft location events from topics D. 

## Running as Avro

Serialized messages using [this Avro schema](src/main/java/io/confluent/demo/aircraft/avro/schemas/AircraftState.avsc).

Replace $CCLOUD_PROPS with your Kafka properties file name inside the `src/main/resource` folder as exemplified [here](/src/main/resources/ccloud_example.properties).

### TrackingService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.avro.producer.TrackingService" -Dexec.args="./src/main/resources $CCLOUD_PROPS opensky.properties tracking.aviation.aircraft.avro TrackingService.avro"
```
### RouterService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.avro.kstreams.RouterKStreamsService" -Dexec.args="./src/main/resources $CCLOUD_PROPS RouterService.avro tracking.aviation.aircraft.avro tracking.aviation.aircraft_onground.avro tracking.aviation.aircraft_inflight.avro tracking.aviation.aircraft_unidentified.avro Kstreams.avro.client.1"
```
### OnGroundService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.avro.consumer.GenericAvroConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_onground.avro OnGroundService.avro OnGroundConsumer.avro.client.1"
```
### InFlightService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.avro.consumer.GenericAvroConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_inflight.avro InFlightService.avro InFlightConsumer.avro.client.1"
```
### UnidentifiedService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.avro.consumer.GenericAvroConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_unidentified.avro UnidentifiedService.avro UnidentifiedConsumer.avro.client.1"
```

## Running as Protobuf

Serialized messages using [this Protobuf schema](src/main/java/io/confluent/demo/aircraft/protobuf/schemas/AircraftState.proto).

Replace $CCLOUD_PROPS with your Kafka properties file name inside the `src/main/resource` folder as exemplified [here](/src/main/resources/ccloud_example.properties).

### TrackingService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.protobuf.producer.TrackingService" -Dexec.args="./src/main/resources $CCLOUD_PROPS opensky.properties tracking.aviation.aircraft.protobuf TrackingService.protobuf"
```
### RouterService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.protobuf.kstreams.RouterKStreamsService" -Dexec.args="./src/main/resources $CCLOUD_PROPS RouterService.protobuf tracking.aviation.aircraft.protobuf tracking.aviation.aircraft_onground.protobuf tracking.aviation.aircraft_inflight.protobuf tracking.aviation.aircraft_unidentified.protobuf Kstreams.protobuf.client.1"
```
### OnGroundService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.protobuf.consumer.GenericProtobufConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_onground.protobuf OnGroundService.protobuf OnGroundConsumer.protobuf.client.1"
```
### InFlightService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.protobuf.consumer.GenericProtobufConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_inflight.protobuf InFlightService.protobuf InFlightConsumer.protobuf.client.1"
```
### UnidentifiedService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.protobuf.consumer.GenericProtobufConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_unidentified.protobuf UnidentifiedService.protobuf UnidentifiedConsumer.protobuf.client.1"
```

## Running as JSON

Serialized messages using [this JSON schema](src/main/java/io/confluent/demo/aircraft/jsonschema/schemas/AircraftState.json).

Replace $CCLOUD_PROPS with your Kafka properties file name inside the `src/main/resource` folder as exemplified [here](/src/main/resources/ccloud_example.properties).

### TrackingService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.jsonschema.producer.TrackingService" -Dexec.args="./src/main/resources $CCLOUD_PROPS opensky.properties tracking.aviation.aircraft.json TrackingService.json"
```
### RouterService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.jsonschema.kstreams.RouterKStreamsService" -Dexec.args="./src/main/resources $CCLOUD_PROPS RouterService.json tracking.aviation.aircraft.json tracking.aviation.aircraft_onground.json tracking.aviation.aircraft_inflight.json tracking.aviation.aircraft_unidentified.json Kstreams.json.client.1"
```
### OnGroundService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.jsonschema.consumer.GenericJsonConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_onground.json OnGroundService.json OnGroundConsumer.json.client.1"
```
### InFlightService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.jsonschema.consumer.GenericJsonConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_inflight.json InFlightService.json InFlightConsumer.json.client.1"
```
### UnidentifiedService
```bash
mvn exec:java -Dexec.mainClass="io.confluent.demo.aircraft.jsonschema.consumer.GenericJsonConsumerService" -Dexec.args="./src/main/resources $CCLOUD_PROPS tracking.aviation.aircraft_unidentified.json UnidentifiedService.json UnidentifiedConsumer.json.client.1"
```

## Read more
* [Schema Registry Overview](https://docs.confluent.io/platform/current/schema-registry/index.html)
* [Manage Schemas in Confluent Cloud](https://docs.confluent.io/cloud/current/client-apps/schemas-manage.html#cloud-schemas-manage)
* [Schema Registry API](https://docs.confluent.io/platform/current/schema-registry/develop/api.html)
* [Broker-Side Schema Validation on Confluent Cloud](https://docs.confluent.io/cloud/current/client-apps/schemas-manage.html#using-broker-side-schema-validation-on-ccloud)

## License
This project is licensed under the [Apache 2.0 License](./LICENSE).
