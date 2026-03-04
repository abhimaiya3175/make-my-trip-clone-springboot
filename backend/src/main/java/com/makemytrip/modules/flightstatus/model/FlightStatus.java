package com.makemytrip.modules.flightstatus.model;

public class FlightStatus {
    private String flightId;
    private String flightName;
    private String status;
    private String departureTime;
    private String arrivalTime;
    private String gate;
    private String terminal;
    private int delayMinutes;

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getFlightName() { return flightName; }
    public void setFlightName(String flightName) { this.flightName = flightName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }
    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }
    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }
}
