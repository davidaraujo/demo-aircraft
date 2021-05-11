package io.confluent.demo.aircraft.protobuf.producer;

import io.confluent.demo.aircraft.protobuf.pojo.AircraftState;
import org.opensky.model.StateVector;

public class AircraftEvent {
    
    public static AircraftState.Aircraft create(StateVector state) {

        AircraftState.Aircraft.Builder event = AircraftState.Aircraft.newBuilder();
        
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

        event.setOnGround(state.isOnGround());
        
        return event.build();
    }
}
