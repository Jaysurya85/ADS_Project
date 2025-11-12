import models.*;
import dataStructures.*;
import java.util.*;
import java.io.PrintWriter;

public class Scheduler {
	private RunwayMinHeap runways;
	private PairingHeap pendingFlights;
	private TimetableMinHeap timetable;
	private HashMap<Integer, Flight> activeFlights;
	private HashMap<Integer, HashSet<Integer>> airlineIndex;
	private HashMap<Integer, PairingHeapNode> handles;
	private int currentTime;
	private PrintWriter output;

	public Scheduler(PrintWriter output) {
		this.runways = new RunwayMinHeap();
		this.pendingFlights = new PairingHeap();
		this.timetable = new TimetableMinHeap();
		this.activeFlights = new HashMap<>();
		this.airlineIndex = new HashMap<>();
		this.handles = new HashMap<>();
		this.currentTime = 0;
		this.output = output;
	}

	public void initialize(int runwayCount) {

		// Checking for invalid scenarios
		if (runwayCount <= 0) {
			this.output.println("Invalid input. Please provide a valid number of runways.");
			return;
		}

		// Adding all the runways
		for (int i = 1; i <= runwayCount; i++) {
			this.runways.insert(new Runway(i, 0));
		}

		this.output.println(runwayCount + " Runways are now available");
	}

	public void submitFlight(int flightID, int airlineID, int submitTime, int priority, int duration) {

		// Updating the current time
		updateCurrentTime(submitTime);

		if (this.activeFlights.containsKey(flightID)) {
			this.output.println("Duplicate FlightID");
			return;
		}

		// Adding the flight in all the datastructures
		Flight flight = new Flight(flightID, airlineID, submitTime, priority, duration);
		this.activeFlights.put(flightID, flight);
		if (!this.airlineIndex.containsKey(airlineID)) {
			this.airlineIndex.put(airlineID, new HashSet<>());
		}
		this.airlineIndex.get(airlineID).add(flightID);
		PairingHeapNode node = this.pendingFlights.push(flight);
		this.handles.put(flightID, node);

		// Schedule this flight and updating the ETAs if required
		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println("Flight " + flightID + " scheduled - ETA: " + flight.getEndTime());
		printAllChangedETAs(updatedETAs);

	}

	public void cancelFlight(int flightID, int currentTime) {

		// Updating the current time
		updateCurrentTime(currentTime);

		if (!this.activeFlights.containsKey(flightID)) {
			this.output.println("Flight " + flightID + " does not exist");
			return;
		}
		Flight flight = this.activeFlights.get(flightID);

		if (flight.getLifeCycleState() == StatusType.IN_PROGRESS ||
				flight.getLifeCycleState() == StatusType.COMPLETED) {
			this.output.println("Cannot cancel. Flight " + flightID + " has already departed");
			return;
		}

		// Remove from all data structures
		this.activeFlights.remove(flightID);
		HashSet<Integer> airlineFlights = this.airlineIndex.get(flight.getAirlineID());
		if (airlineFlights != null) {
			airlineFlights.remove(flightID);
		}
		PairingHeapNode node = this.handles.get(flightID);
		if (node != null) {
			this.pendingFlights.erase(node);
			this.handles.remove(flightID);
		}

		// Updated the ETAs
		ArrayList<String> updatedETAs = rescheduleFlights();

		// Printing all the output
		this.output.println("Flight " + flightID + " has been canceled");
		printAllChangedETAs(updatedETAs);
	}

	public void reprioritize(int flightID, int currentTime, int newPriority) {

		// Updating the current time
		updateCurrentTime(currentTime);

		Flight flight = this.activeFlights.get(flightID);
		if (flight == null) {
			this.output.println("Flight " + flightID + " not found");
			return;
		}
		if (flight.getLifeCycleState() == StatusType.IN_PROGRESS ||
				flight.getLifeCycleState() == StatusType.COMPLETED) {
			this.output.println("Cannot reprioritize. Flight " + flightID + " has already departed");
			return;
		}

		int oldPriority = flight.getPriority();
		flight.setPriority(newPriority);
		PairingHeapNode node = this.handles.get(flightID);
		if (node != null) {
			if (newPriority > oldPriority) {
				// For increase increaseKey
				this.pendingFlights.increaseKey(node, newPriority);
			} else {
				// For decrease, remove and re-insert
				this.pendingFlights.erase(node);
				PairingHeapNode newNode = this.pendingFlights.push(flight);
				this.handles.put(flightID, newNode);
			}
		}

		// Rescheduling unsatisfied flights
		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println("Priority of Flight " + flightID + " has been updated to " + newPriority);
		printAllChangedETAs(updatedETAs);

	}

	public void addRunways(int count, int currentTime) {

		// Updating the current time
		updateCurrentTime(currentTime);

		if (count <= 0) {
			this.output.println("Invalid input. Please provide a valid number of runways.");
			return;
		}

		// Adding new runways
		int currentRunwayCount = this.runways.getRunwayCount();
		for (int i = 0; i < count; i++) {
			this.runways.insert(new Runway(currentRunwayCount + i + 1, currentTime));
		}

		// Rescheduling the flights
		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println("Additional " + count + " Runways are now available");
		printAllChangedETAs(updatedETAs);

	}

	public void groundHold(int airlineLow, int airlineHigh, int currentTime) {

		// Updating the curren time
		updateCurrentTime(currentTime);

		if (airlineHigh < airlineLow) {
			this.output.println("Invalid input. Please provide a valid airline range.");
			return;
		}

		// Fetching all the flights with airlineID between low and high
		ArrayList<Integer> toRemove = new ArrayList<>();
		for (int airlineID = airlineLow; airlineID <= airlineHigh; airlineID++) {
			HashSet<Integer> flights = this.airlineIndex.get(airlineID);
			if (flights != null) {
				for (int flightID : flights) {
					Flight flight = this.activeFlights.get(flightID);
					if (flight != null &&
							(flight.getLifeCycleState() == StatusType.PENDING ||
									flight.getLifeCycleState() == StatusType.SCHEDULED)) {
						toRemove.add(flightID);
					}
				}
			}
		}

		// Deleting all the flights with those flightID
		for (int flightID : toRemove) {
			Flight flight = this.activeFlights.get(flightID);
			if (flight != null) {
				this.activeFlights.remove(flightID);
				HashSet<Integer> airlineFlights = this.airlineIndex.get(flight.getAirlineID());
				if (airlineFlights != null) {
					airlineFlights.remove(flightID);
				}
				PairingHeapNode node = this.handles.get(flightID);
				if (node != null) {
					this.pendingFlights.erase(node);
					this.handles.remove(flightID);
				}
				this.timetable.remove(flight);
			}
		}

		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println(
				"Flights of the airlines in the range [" + airlineLow + ", " + airlineHigh + "] have been grounded");
		printAllChangedETAs(updatedETAs);
	}

	public void printActive() {
		ArrayList<Flight> flights = new ArrayList<>();

		for (Flight flight : this.activeFlights.values()) {
			if (flight.getLifeCycleState() != StatusType.COMPLETED) {
				flights.add(flight);
			}
		}

		if (flights.isEmpty()) {
			this.output.println("No active flights");
			return;
		}

		flights.sort((a, b) -> Integer.compare(a.getFlightID(), b.getFlightID()));
		for (Flight flight : flights) {
			this.output.println(flight);
			// this.output.println("[flight" + flight.getFlightID() + ", airline" +
			// flight.getAirlineID() +
			// ", runway" + flight.getRunwayID() + ", start" + flight.getStartTime() +
			// ", ETA" + flight.getEndTime() + "]");
		}
	}

	public void printSchedule(int endTime1, int endTime2) {
		ArrayList<Flight> scheduledFlights = new ArrayList<>();

		for (Flight flight : this.activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.SCHEDULED &&
					flight.getStartTime() > this.currentTime &&
					flight.getEndTime() >= endTime1 &&
					flight.getEndTime() <= endTime2) {
				scheduledFlights.add(flight);
			}
		}

		if (scheduledFlights.isEmpty()) {
			this.output.println("There are no flights in that time period");
			return;
		}

		scheduledFlights.sort((a, b) -> {
			if (a.getEndTime() != b.getEndTime()) {
				return Integer.compare(a.getEndTime(), b.getEndTime());
			}
			return Integer.compare(a.getFlightID(), b.getFlightID());
		});

		for (Flight flight : scheduledFlights) {
			this.output.println("[" + flight.getFlightID() + "]");
		}
	}

	public void tick(int time) {
		updateCurrentTime(time);
	}

	public void quit() {
		this.output.print("Program Terminated!!");
		this.output.flush();
	}

	// Internal functions

	private void updateCurrentTime(int newTime) {
		if (newTime < this.currentTime)
			return;
		this.currentTime = newTime;

		// Need to complete all the flights between the old current time and the new
		// time
		settleCompletions();

		// Need to start all flights between the old current time and the new time
		promoteFlights();

		// Need to reschedule the unsatisfied flights
		rescheduleFlights();
	}

	private void settleCompletions() {
		ArrayList<Flight> completed = this.timetable.popAllCompleted(currentTime);

		for (Flight flight : completed) {
			flight.setLifeCycleState(StatusType.COMPLETED);
			this.output.println("Flight " + flight.getFlightID() + " has landed at time " + flight.getEndTime());

			this.activeFlights.remove(flight.getFlightID());
			this.handles.remove(flight.getFlightID());

			HashSet<Integer> airlineFlights = this.airlineIndex.get(flight.getAirlineID());
			if (airlineFlights != null) {
				airlineFlights.remove(flight.getFlightID());
			}
		}
	}

	private void promoteFlights() {
		for (Flight flight : this.activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.SCHEDULED &&
					flight.getStartTime() <= currentTime &&
					flight.getEndTime() > currentTime) {
				flight.setLifeCycleState(StatusType.IN_PROGRESS);
			}
		}
	}

	private ArrayList<String> rescheduleFlights() {

		ArrayList<Flight> toReschedule = new ArrayList<>();
		HashMap<Integer, Integer> oldEndTimes = new HashMap<>();

		// Collect all scheduled flights that haven't started yet
		for (Flight flight : this.activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.SCHEDULED && flight.getStartTime() > this.currentTime) {
				toReschedule.add(flight);
				oldEndTimes.put(flight.getFlightID(), flight.getEndTime());
			}
		}

		// Collect all pending flights
		while (!this.pendingFlights.isEmpty()) {
			Flight flight = this.pendingFlights.pop();
			toReschedule.add(flight);
			handles.remove(flight.getFlightID());
		}

		if (toReschedule.isEmpty())
			return new ArrayList<>();

		toReschedule.sort((flight1, flight2) -> {
			if (flight1.getPriority() != flight2.getPriority()) {
				return Integer.compare(flight2.getPriority(), flight1.getPriority());
			}
			if (flight1.getSubmitTime() != flight2.getSubmitTime()) {
				return Integer.compare(flight1.getSubmitTime(), flight2.getSubmitTime());
			}
			return Integer.compare(flight1.getFlightID(), flight2.getFlightID());
		});

		// Rebuilding the time table and runways
		rebuildTimeTableAndRunway();

		// Greedily assigning according to the new ETAs
		ArrayList<String> endTimeUpdates = greedyAssignment(toReschedule, oldEndTimes);

		return endTimeUpdates;
	}

	private void rebuildTimeTableAndRunway() {

		// Rebuild timetable: keep only in-progress flights
		TimetableMinHeap newTimetable = new TimetableMinHeap();
		for (Flight flight : this.activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.IN_PROGRESS) {
				newTimetable.insert(flight);
			}
		}
		this.timetable = newTimetable;

		// Rebuild runway pool
		ArrayList<Runway> runwayList = this.runways.getAllRunways();
		this.runways.deleteAllRunways();

		// Initialize runway times to currentTime
		HashMap<Integer, Integer> runwayTimes = new HashMap<>();
		for (Runway runway : runwayList) {
			runwayTimes.put(runway.getRunwayID(), this.currentTime);
		}

		// Update runway times based on in-progress flights
		for (Flight flight : this.activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.IN_PROGRESS) {
				int rid = flight.getRunwayID();
				runwayTimes.put(rid, flight.getEndTime());
			}
		}

		// Insert runways back with updated times
		for (Runway runway : runwayList) {
			runway.setNextFreeTime(runwayTimes.get(runway.getRunwayID()));
			this.runways.insert(runway);
		}
	}

	private ArrayList<String> greedyAssignment(List<Flight> toReschedule, HashMap<Integer, Integer> oldEndTimes) {

		ArrayList<String> endTimeUpdates = new ArrayList<>();

		for (Flight flight : toReschedule) {
			Runway runway = this.runways.extractMin();
			int startTime = Math.max(this.currentTime, runway.getNextFreeTime());
			int newEndTime = startTime + flight.getDuration();

			flight.setStartTime(startTime);
			flight.setEndTime(newEndTime);
			flight.setRunwayID(runway.getRunwayID());
			flight.setLifeCycleState(StatusType.SCHEDULED);

			runway.setNextFreeTime(newEndTime);
			this.runways.insert(runway);

			this.timetable.insert(flight);

			// Only report ETA changes for flights that were ALREADY scheduled
			if (oldEndTimes.containsKey(flight.getFlightID())) {
				int oldEndTime = oldEndTimes.get(flight.getFlightID());
				if (oldEndTime != newEndTime) {
					endTimeUpdates.add(flight.getFlightID() + ": " + newEndTime);
				}
			}
		}
		return endTimeUpdates;
	}

	private void printAllChangedETAs(ArrayList<String> endTimeUpdates) {

		if (!endTimeUpdates.isEmpty()) {
			endTimeUpdates.sort((a, b) -> {
				int id1 = Integer.parseInt(a.split(":")[0]);
				int id2 = Integer.parseInt(b.split(":")[0]);
				return Integer.compare(id1, id2);
			});
			this.output.println("Updated ETAs: [" + String.join(", ", endTimeUpdates) + "]");
		}
	}
}
