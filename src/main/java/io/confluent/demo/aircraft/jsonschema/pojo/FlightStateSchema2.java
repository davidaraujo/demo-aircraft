
package io.confluent.demo.aircraft.jsonschema.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * FlightState
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "icao24",
    "callsign",
    "originCountry",
    "timePosition",
    "lastContact",
    "longitude",
    "latitude",
    "baroAltitude",
    "velocity",
    "trueTrack",
    "verticalRate",
    "geoAltitude",
    "squawk",
    "spi",
    "onGround"
})
public class FlightStateSchema2 {

    /**
     * Unique ICAO 24-bit address of the transponder in hex string representation.
     * 
     */
    @JsonProperty("icao24")
    @JsonPropertyDescription("Unique ICAO 24-bit address of the transponder in hex string representation.")
    private String icao24;
    /**
     * Callsign of the vehicle (8 chars). Can be null if no callsign has been received.
     * 
     */
    @JsonProperty("callsign")
    @JsonPropertyDescription("Callsign of the vehicle (8 chars). Can be null if no callsign has been received.")
    private String callsign;
    /**
     * Country name inferred from the ICAO 24-bit address.
     * 
     */
    @JsonProperty("originCountry")
    @JsonPropertyDescription("Country name inferred from the ICAO 24-bit address.")
    private String originCountry;
    /**
     * Unix timestamp (seconds) for the last position update. Can be null if no position report was received by OpenSky within the past 15s.
     * 
     */
    @JsonProperty("timePosition")
    @JsonPropertyDescription("Unix timestamp (seconds) for the last position update. Can be null if no position report was received by OpenSky within the past 15s.")
    private Double timePosition;
    /**
     * Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.
     * 
     */
    @JsonProperty("lastContact")
    @JsonPropertyDescription("Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.")
    private Integer lastContact;
    /**
     * WGS-84 longitude in decimal degrees. Can be null.
     * 
     */
    @JsonProperty("longitude")
    @JsonPropertyDescription("WGS-84 longitude in decimal degrees. Can be null.")
    private Double longitude;
    /**
     * Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.
     * 
     */
    @JsonProperty("latitude")
    @JsonPropertyDescription("Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.")
    private Double latitude;
    /**
     * Barometric altitude in meters. Can be null.
     * 
     */
    @JsonProperty("baroAltitude")
    @JsonPropertyDescription("Barometric altitude in meters. Can be null.")
    private Double baroAltitude;
    /**
     * Velocity over ground in m/s. Can be null.
     * 
     */
    @JsonProperty("velocity")
    @JsonPropertyDescription("Velocity over ground in m/s. Can be null.")
    private Double velocity;
    /**
     * True track in decimal degrees clockwise from north (north=0°). Can be null.
     * 
     */
    @JsonProperty("trueTrack")
    @JsonPropertyDescription("True track in decimal degrees clockwise from north (north=0\u00b0). Can be null.")
    private Double trueTrack;
    /**
     * Vertical rate in m/s. A positive value indicates that the airplane is climbing, a negative value indicates that it descends. Can be null.
     * 
     */
    @JsonProperty("verticalRate")
    @JsonPropertyDescription("Vertical rate in m/s. A positive value indicates that the airplane is climbing, a negative value indicates that it descends. Can be null.")
    private Double verticalRate;
    /**
     * Geometric altitude in meters. Can be null.
     * 
     */
    @JsonProperty("geoAltitude")
    @JsonPropertyDescription("Geometric altitude in meters. Can be null.")
    private Double geoAltitude;
    /**
     * The transponder code aka Squawk. Can be null.
     * 
     */
    @JsonProperty("squawk")
    @JsonPropertyDescription("The transponder code aka Squawk. Can be null.")
    private String squawk;
    /**
     * Whether flight status indicates special purpose indicator.
     * 
     */
    @JsonProperty("spi")
    @JsonPropertyDescription("Whether flight status indicates special purpose indicator.")
    private Boolean spi;
    /**
     * Boolean value which indicates if the position was retrieved from a surface position report.
     * 
     */
    @JsonProperty("onGround")
    @JsonPropertyDescription("Boolean value which indicates if the position was retrieved from a surface position report.")
    private Boolean onGround;

    /**
     * Unique ICAO 24-bit address of the transponder in hex string representation.
     * 
     */
    @JsonProperty("icao24")
    public String getIcao24() {
        return icao24;
    }

    /**
     * Unique ICAO 24-bit address of the transponder in hex string representation.
     * 
     */
    @JsonProperty("icao24")
    public void setIcao24(String icao24) {
        this.icao24 = icao24;
    }

    /**
     * Callsign of the vehicle (8 chars). Can be null if no callsign has been received.
     * 
     */
    @JsonProperty("callsign")
    public String getCallsign() {
        return callsign;
    }

    /**
     * Callsign of the vehicle (8 chars). Can be null if no callsign has been received.
     * 
     */
    @JsonProperty("callsign")
    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    /**
     * Country name inferred from the ICAO 24-bit address.
     * 
     */
    @JsonProperty("originCountry")
    public String getOriginCountry() {
        return originCountry;
    }

    /**
     * Country name inferred from the ICAO 24-bit address.
     * 
     */
    @JsonProperty("originCountry")
    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    /**
     * Unix timestamp (seconds) for the last position update. Can be null if no position report was received by OpenSky within the past 15s.
     * 
     */
    @JsonProperty("timePosition")
    public Double getTimePosition() {
        return timePosition;
    }

    /**
     * Unix timestamp (seconds) for the last position update. Can be null if no position report was received by OpenSky within the past 15s.
     * 
     */
    @JsonProperty("timePosition")
    public void setTimePosition(Double timePosition) {
        this.timePosition = timePosition;
    }

    /**
     * Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.
     * 
     */
    @JsonProperty("lastContact")
    public Integer getLastContact() {
        return lastContact;
    }

    /**
     * Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.
     * 
     */
    @JsonProperty("lastContact")
    public void setLastContact(Integer lastContact) {
        this.lastContact = lastContact;
    }

    /**
     * WGS-84 longitude in decimal degrees. Can be null.
     * 
     */
    @JsonProperty("longitude")
    public Double getLongitude() {
        return longitude;
    }

    /**
     * WGS-84 longitude in decimal degrees. Can be null.
     * 
     */
    @JsonProperty("longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.
     * 
     */
    @JsonProperty("latitude")
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Unix timestamp (seconds) for the last update in general. This field is updated for any new, valid message received from the transponder.
     * 
     */
    @JsonProperty("latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Barometric altitude in meters. Can be null.
     * 
     */
    @JsonProperty("baroAltitude")
    public Double getBaroAltitude() {
        return baroAltitude;
    }

    /**
     * Barometric altitude in meters. Can be null.
     * 
     */
    @JsonProperty("baroAltitude")
    public void setBaroAltitude(Double baroAltitude) {
        this.baroAltitude = baroAltitude;
    }

    /**
     * Velocity over ground in m/s. Can be null.
     * 
     */
    @JsonProperty("velocity")
    public Double getVelocity() {
        return velocity;
    }

    /**
     * Velocity over ground in m/s. Can be null.
     * 
     */
    @JsonProperty("velocity")
    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

    /**
     * True track in decimal degrees clockwise from north (north=0°). Can be null.
     * 
     */
    @JsonProperty("trueTrack")
    public Double getTrueTrack() {
        return trueTrack;
    }

    /**
     * True track in decimal degrees clockwise from north (north=0°). Can be null.
     * 
     */
    @JsonProperty("trueTrack")
    public void setTrueTrack(Double trueTrack) {
        this.trueTrack = trueTrack;
    }

    /**
     * Vertical rate in m/s. A positive value indicates that the airplane is climbing, a negative value indicates that it descends. Can be null.
     * 
     */
    @JsonProperty("verticalRate")
    public Double getVerticalRate() {
        return verticalRate;
    }

    /**
     * Vertical rate in m/s. A positive value indicates that the airplane is climbing, a negative value indicates that it descends. Can be null.
     * 
     */
    @JsonProperty("verticalRate")
    public void setVerticalRate(Double verticalRate) {
        this.verticalRate = verticalRate;
    }

    /**
     * Geometric altitude in meters. Can be null.
     * 
     */
    @JsonProperty("geoAltitude")
    public Double getGeoAltitude() {
        return geoAltitude;
    }

    /**
     * Geometric altitude in meters. Can be null.
     * 
     */
    @JsonProperty("geoAltitude")
    public void setGeoAltitude(Double geoAltitude) {
        this.geoAltitude = geoAltitude;
    }

    /**
     * The transponder code aka Squawk. Can be null.
     * 
     */
    @JsonProperty("squawk")
    public String getSquawk() {
        return squawk;
    }

    /**
     * The transponder code aka Squawk. Can be null.
     * 
     */
    @JsonProperty("squawk")
    public void setSquawk(String squawk) {
        this.squawk = squawk;
    }

    /**
     * Whether flight status indicates special purpose indicator.
     * 
     */
    @JsonProperty("spi")
    public Boolean getSpi() {
        return spi;
    }

    /**
     * Whether flight status indicates special purpose indicator.
     * 
     */
    @JsonProperty("spi")
    public void setSpi(Boolean spi) {
        this.spi = spi;
    }

    /**
     * Boolean value which indicates if the position was retrieved from a surface position report.
     * 
     */
    @JsonProperty("onGround")
    public Boolean getOnGround() {
        return onGround;
    }

    /**
     * Boolean value which indicates if the position was retrieved from a surface position report.
     * 
     */
    @JsonProperty("onGround")
    public void setOnGround(Boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(FlightStateSchema2 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("icao24");
        sb.append('=');
        sb.append(((this.icao24 == null)?"<null>":this.icao24));
        sb.append(',');
        sb.append("callsign");
        sb.append('=');
        sb.append(((this.callsign == null)?"<null>":this.callsign));
        sb.append(',');
        sb.append("originCountry");
        sb.append('=');
        sb.append(((this.originCountry == null)?"<null>":this.originCountry));
        sb.append(',');
        sb.append("timePosition");
        sb.append('=');
        sb.append(((this.timePosition == null)?"<null>":this.timePosition));
        sb.append(',');
        sb.append("lastContact");
        sb.append('=');
        sb.append(((this.lastContact == null)?"<null>":this.lastContact));
        sb.append(',');
        sb.append("longitude");
        sb.append('=');
        sb.append(((this.longitude == null)?"<null>":this.longitude));
        sb.append(',');
        sb.append("latitude");
        sb.append('=');
        sb.append(((this.latitude == null)?"<null>":this.latitude));
        sb.append(',');
        sb.append("baroAltitude");
        sb.append('=');
        sb.append(((this.baroAltitude == null)?"<null>":this.baroAltitude));
        sb.append(',');
        sb.append("velocity");
        sb.append('=');
        sb.append(((this.velocity == null)?"<null>":this.velocity));
        sb.append(',');
        sb.append("trueTrack");
        sb.append('=');
        sb.append(((this.trueTrack == null)?"<null>":this.trueTrack));
        sb.append(',');
        sb.append("verticalRate");
        sb.append('=');
        sb.append(((this.verticalRate == null)?"<null>":this.verticalRate));
        sb.append(',');
        sb.append("geoAltitude");
        sb.append('=');
        sb.append(((this.geoAltitude == null)?"<null>":this.geoAltitude));
        sb.append(',');
        sb.append("squawk");
        sb.append('=');
        sb.append(((this.squawk == null)?"<null>":this.squawk));
        sb.append(',');
        sb.append("spi");
        sb.append('=');
        sb.append(((this.spi == null)?"<null>":this.spi));
        sb.append(',');
        sb.append("onGround");
        sb.append('=');
        sb.append(((this.onGround == null)?"<null>":this.onGround));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.verticalRate == null)? 0 :this.verticalRate.hashCode()));
        result = ((result* 31)+((this.icao24 == null)? 0 :this.icao24 .hashCode()));
        result = ((result* 31)+((this.latitude == null)? 0 :this.latitude.hashCode()));
        result = ((result* 31)+((this.timePosition == null)? 0 :this.timePosition.hashCode()));
        result = ((result* 31)+((this.velocity == null)? 0 :this.velocity.hashCode()));
        result = ((result* 31)+((this.spi == null)? 0 :this.spi.hashCode()));
        result = ((result* 31)+((this.lastContact == null)? 0 :this.lastContact.hashCode()));
        result = ((result* 31)+((this.trueTrack == null)? 0 :this.trueTrack.hashCode()));
        result = ((result* 31)+((this.squawk == null)? 0 :this.squawk.hashCode()));
        result = ((result* 31)+((this.geoAltitude == null)? 0 :this.geoAltitude.hashCode()));
        result = ((result* 31)+((this.onGround == null)? 0 :this.onGround.hashCode()));
        result = ((result* 31)+((this.callsign == null)? 0 :this.callsign.hashCode()));
        result = ((result* 31)+((this.originCountry == null)? 0 :this.originCountry.hashCode()));
        result = ((result* 31)+((this.baroAltitude == null)? 0 :this.baroAltitude.hashCode()));
        result = ((result* 31)+((this.longitude == null)? 0 :this.longitude.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FlightStateSchema2) == false) {
            return false;
        }
        FlightStateSchema2 rhs = ((FlightStateSchema2) other);
        return ((((((((((((((((this.verticalRate == rhs.verticalRate)||((this.verticalRate!= null)&&this.verticalRate.equals(rhs.verticalRate)))&&((this.icao24 == rhs.icao24)||((this.icao24 != null)&&this.icao24 .equals(rhs.icao24))))&&((this.latitude == rhs.latitude)||((this.latitude!= null)&&this.latitude.equals(rhs.latitude))))&&((this.timePosition == rhs.timePosition)||((this.timePosition!= null)&&this.timePosition.equals(rhs.timePosition))))&&((this.velocity == rhs.velocity)||((this.velocity!= null)&&this.velocity.equals(rhs.velocity))))&&((this.spi == rhs.spi)||((this.spi!= null)&&this.spi.equals(rhs.spi))))&&((this.lastContact == rhs.lastContact)||((this.lastContact!= null)&&this.lastContact.equals(rhs.lastContact))))&&((this.trueTrack == rhs.trueTrack)||((this.trueTrack!= null)&&this.trueTrack.equals(rhs.trueTrack))))&&((this.squawk == rhs.squawk)||((this.squawk!= null)&&this.squawk.equals(rhs.squawk))))&&((this.geoAltitude == rhs.geoAltitude)||((this.geoAltitude!= null)&&this.geoAltitude.equals(rhs.geoAltitude))))&&((this.onGround == rhs.onGround)||((this.onGround!= null)&&this.onGround.equals(rhs.onGround))))&&((this.callsign == rhs.callsign)||((this.callsign!= null)&&this.callsign.equals(rhs.callsign))))&&((this.originCountry == rhs.originCountry)||((this.originCountry!= null)&&this.originCountry.equals(rhs.originCountry))))&&((this.baroAltitude == rhs.baroAltitude)||((this.baroAltitude!= null)&&this.baroAltitude.equals(rhs.baroAltitude))))&&((this.longitude == rhs.longitude)||((this.longitude!= null)&&this.longitude.equals(rhs.longitude))));
    }

}
