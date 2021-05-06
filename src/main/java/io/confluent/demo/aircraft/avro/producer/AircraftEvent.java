package io.confluent.demo.aircraft.avro.producer;

import io.confluent.demo.aircraft.avro.pojo.AircraftState;
import org.opensky.model.StateVector;

public class AircraftEvent {
    
    public static AircraftState create(StateVector state) {
       /* FlightState event = new FlightState();
                            new FlightState(state.getIcao24(),
                            state.getCallsign(),
                            state.getOriginCountry(),
                            new Double(0),
                            state.getLastContact().longValue(),
                            state.getLongitude(),
                            state.getLatitude(),
                            state.getBaroAltitude(),
                            state.getVelocity(),
                            new Double(0),
                            state.getVerticalRate(),
                            state.getGeoAltitude(),
                            state.getSquawk(),
                            false,
                            false,
                            "");*/
        AircraftState event = new AircraftState();
        
        if (state.getBaroAltitude() != null)
            event.setBaroAltitude(state.getBaroAltitude());

        if (state.getCallsign() != null)
            event.setCallsign(state.getCallsign().trim());
        else
            event.setCallsign("");

        if (state.getGeoAltitude() != null)
            event.setGeoAltitude(state.getGeoAltitude());

        if (state.getBaroAltitude() != null)
            event.setBaroAltitude(state.getBaroAltitude());

        if (state.getIcao24() != null)
            event.setIcao24(state.getIcao24());

        if (state.getLastContact() != null)
            event.setLastContact(state.getLastContact().intValue());

        if (state.getLatitude() != null)
            event.setLatitude(state.getLatitude());

        if (state.getLongitude() != null)
            event.setLongitude(state.getLongitude());
        //event.setOnGround(state.isOnGround());

        if (state.getOriginCountry() != null)
            event.setCountryOrigin(state.getOriginCountry());

        event.setSpi(state.isSpi());

        if (state.getSquawk() != null)
            event.setSquawk(state.getSquawk());

        if (state.getLastPositionUpdate() != null)
            event.setTimePosition(state.getLastPositionUpdate());

        if (state.getHeading() != null)
            event.setTrueTrack(state.getHeading());

        if (state.getVelocity() != null)
            event.setVelocity(state.getVelocity());

        if (state.getVerticalRate() != null)
            event.setVerticalRate(state.getVerticalRate());
        // if version 2 or 3

        event.setOnGround(state.isOnGround());
        
        return event;
    }
}
