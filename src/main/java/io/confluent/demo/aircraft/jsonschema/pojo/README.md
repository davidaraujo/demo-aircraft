To register the exact same JSON Schema that was defined, inject the original .json schema
with the @Schema annotation above the class definition:

import io.confluent.kafka.schemaregistry.annotations.Schema;

@Schema(value="JSON SCHEMA GOES HERE",refs = {})

--------------------------------------------------------

flight.json notes:

removed $id property and fields sensors and position_source
   "sensors": {
      "type": "array",
      "description": "IDs of the receivers which contributed to this state vector. Is null if no filtering for sensor was used in the request."
    },
    "position_source": {
      "type": "integer",
      "description": "Origin of this state’s position: 0 = ADS-B, 1 = ASTERIX, 2 = MLAT",
      "enum": [
        0,
        1,
        2
      ]
    }
   