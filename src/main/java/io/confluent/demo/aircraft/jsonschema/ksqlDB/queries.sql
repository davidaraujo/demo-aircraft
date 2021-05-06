CREATE STREAM FLIGHTS_STREAM WITH (KAFKA_TOPIC = 'flights', VALUE_FORMAT = 'JSON_SR');

SELECT * FROM FLIGHTS_STREAM EMIT CHANGES;

SELECT * FROM FLIGHTS_STREAM WHERE callsign like 'BA%' EMIT CHANGES;

CREATE TABLE flight_origin_stats AS
  SELECT origincountry,
         count(*) as in_flight
  FROM  FLIGHTS_STREAM
  GROUP BY origincountry
  EMIT CHANGES;

SELECT * FROM flight_origin_stats EMIT CHANGES;