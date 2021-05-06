/**
 * Ref: https://opensky-network.org/apidoc/java.html
 */

package io.confluent.demo.aircraft.utils;

import org.opensky.api.OpenSkyApi;
import org.opensky.model.OpenSkyStates;
import org.opensky.model.StateVector;

import java.util.Collection;
import java.util.Properties;

public class OpenSkyNetwork {

    private Properties props;
    private OpenSkyApi api;
    private OpenSkyStates states;

    public OpenSkyNetwork(Properties props) {
        this.props = props;
    }

    public void connect() {
        try {
            api = new OpenSkyApi((String) props.get("username"), (String) props.get("password"));
        } catch (Exception e) {
            System.out.println("OpenSKY authentication failed.");
        }
    }

    public Collection<StateVector> getAircraftLocation() {

        // Configure the coordinates to get air traffic data from
        OpenSkyApi.BoundingBox coordinates = new OpenSkyApi.BoundingBox(
                Double.parseDouble((String) props.get("minLatitude")),
                Double.parseDouble((String) props.get("maxLatitude")),
                Double.parseDouble((String) props.get("minLongitude")),
                Double.parseDouble((String) props.get("maxLongitude")));

        // Infinite loop in case the open sky network does not respond and we need to retry
        while (true) {
            try {
                return api.getStates(0, null, coordinates).getStates();
            } catch (Exception e) {

                System.out.println(
                        ColouredSystemOutPrintln.ANSI_WHITE + ColouredSystemOutPrintln.ANSI_BG_RED +
                        "No answer from the OpenSKY network, retrying ...");
                continue;
            }
        }
    }
}
