package models;

public class Flight {
	private int flightID;
	private int airlineID;
	private int submitTime;
	private int priority;
	private int duration;
	private int startTime;
	private int endTime;
	private StatusType lifeCycleState;
	private int runwayID;

	// Constructor
	public Flight(int flightID, int airlineID, int submitTime, int priority, int duration) {
		this.flightID = flightID;
		this.airlineID = airlineID;
		this.submitTime = submitTime;
		this.priority = priority;
		this.duration = duration;
		this.startTime = -1;
		this.endTime = -1;
		this.runwayID = -1;
		this.lifeCycleState = StatusType.PENDING;
	}

	public int getFlightID() {
		return flightID;
	}

	public int getAirlineID() {
		return airlineID;
	}

	public int getSubmitTime() {
		return submitTime;
	}

	public int getPriority() {
		return priority;
	}

	public int getDuration() {
		return duration;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public StatusType getLifeCycleState() {
		return lifeCycleState;
	}

	public int getRunwayID() {
		return runwayID;
	}

	public void setFlightID(int flightID) {
		this.flightID = flightID;
	}

	public void setAirlineID(int airlineID) {
		this.airlineID = airlineID;
	}

	public void setSubmitTime(int submitTime) {
		this.submitTime = submitTime;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int ETA) {
		this.endTime = ETA;
	}

	public void setLifeCycleState(StatusType state) {
		this.lifeCycleState = state;
	}

	public void setRunwayID(int runwayID) {
		this.runwayID = runwayID;
	}

	@Override
	public String toString() {
		return "[flight" + flightID + ", airline" + airlineID + ", runway" + runwayID + ", start" + startTime + ", ETA"
				+ endTime + "]";
	}
}
