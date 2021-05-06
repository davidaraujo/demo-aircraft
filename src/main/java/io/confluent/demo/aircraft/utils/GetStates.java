package io.confluent.demo.aircraft.utils;

import org.json.JSONObject;
import org.opensky.api.OpenSkyApi;
import org.opensky.model.OpenSkyStates;
import org.opensky.model.StateVector;


import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class GetStates {

    // mvn exec:java -Dexec.mainClass="io.confluent.demo.airtraffic.GetStates"


    public static void main(final String[] args) throws IOException {

        OpenSkyApi api = new OpenSkyApi("david.araujo", "jsondemo");
        // no filter on icao24 or sensor serial number
        OpenSkyStates os = api.getStates(0, null, null);

        Collection states = os.getStates();

        Iterator it = states.iterator();

        while (it.hasNext()) {
            StateVector state = (StateVector) it.next();
            System.out.println("state 0: " + state.toString());
            String stateVectorStr = state.toString().substring(11).replace("=", ":");
            JSONObject json = new JSONObject(stateVectorStr);
            System.out.println("state: " + json);
        }

       // JSONObject json = new JSONObject(os.getStates()); // Convert text to object
       // System.out.println(json.toString(4));
    }
}
