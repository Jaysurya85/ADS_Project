public class Runway {
	private int runwayID;
	private int nextFreeTime;

	public Runway(int runwayID) {
		this.runwayID = runwayID;
		this.nextFreeTime = 0;
	}

	public Runway(int runwayID, int nextFreeTime) {
		this.runwayID = runwayID;
		this.nextFreeTime = nextFreeTime;
	}

	public int getRunwayID() {
		return runwayID;
	}

	public int getNextFreeTime() {
		return nextFreeTime;
	}

	public void setRunwayID(int runwayID) {
		this.runwayID = runwayID;
	}

	public void setNextFreeTime(int nextFreeTime) {
		this.nextFreeTime = nextFreeTime;
	}

	@Override
	public String toString() {
		return "Runway " + runwayID + " (Next Free: " + nextFreeTime + ")";
	}
}
