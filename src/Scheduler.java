
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
		if (runwayCount <= 0) {
			this.output.println("Invalid input. Please provide a valid number of runways.");
			return;
		}

		for (int i = 1; i <= runwayCount; i++) {
			this.runways.insert(new Runway(i, 0));
		}
		this.output.println(runwayCount + " Runways are now available");
	}

	public void submitFlight(int flightID, int airlineID, int submitTime, int priority, int duration) {
		updateCurrentTime(submitTime);

		if (activeFlights.containsKey(flightID)) {
			this.output.println("Duplicate FlightID");
			return;
		}

		Flight flight = new Flight(flightID, airlineID, submitTime, priority, duration);

		activeFlights.put(flightID, flight);

		if (!airlineIndex.containsKey(airlineID)) {
			airlineIndex.put(airlineID, new HashSet<>());
		}
		airlineIndex.get(airlineID).add(flightID);

		PairingHeapNode node = pendingFlights.push(flight);
		handles.put(flightID, node);

		// Schedule this flight
		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println("Flight " + flightID + " scheduled - ETA: " + flight.getEndTime());
		printAllChangedETAs(updatedETAs);

	}

	public void cancelFlight(int flightID, int currentTime) {
		updateCurrentTime(currentTime);

		if (!this.activeFlights.containsKey(flightID)) {
			this.output.println("Flight " + flightID + " does not exist");
			return;
		}

		Flight flight = activeFlights.get(flightID);

		if (flight.getLifeCycleState() == StatusType.IN_PROGRESS ||
				flight.getLifeCycleState() == StatusType.COMPLETED) {
			output.println("Cannot cancel. Flight " + flightID + " has already departed");
			return;
		}

		// Remove from all data structures
		activeFlights.remove(flightID);
		HashSet<Integer> airlineFlights = airlineIndex.get(flight.getAirlineID());
		if (airlineFlights != null) {
			airlineFlights.remove(flightID);
		}
		PairingHeapNode node = handles.get(flightID);
		if (node != null) {
			pendingFlights.erase(node);
			handles.remove(flightID);
		}
		ArrayList<String> updatedETAs = rescheduleFlights();
		output.println("Flight " + flightID + " has been canceled");
		printAllChangedETAs(updatedETAs);
	}

	public void reprioritize(int flightID, int currentTime, int newPriority) {
		updateCurrentTime(currentTime);

		Flight flight = activeFlights.get(flightID);
		if (flight == null) {
			output.println("Flight " + flightID + " not found");
			return;
		}

		if (flight.getLifeCycleState() == StatusType.IN_PROGRESS ||
				flight.getLifeCycleState() == StatusType.COMPLETED) {
			output.println("Cannot reprioritize. Flight " + flightID + " has already departed");
			return;
		}

		int oldPriority = flight.getPriority();
		flight.setPriority(newPriority);

		PairingHeapNode node = handles.get(flightID);
		if (node != null) {
			if (newPriority > oldPriority) {
				pendingFlights.increaseKey(node, newPriority);
			} else {
				// For decrease, remove and re-insert
				pendingFlights.erase(node);
				PairingHeapNode newNode = pendingFlights.push(flight);
				handles.put(flightID, newNode);
			}
		}

		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println("Priority of Flight " + flightID + " has been updated to " + newPriority);
		printAllChangedETAs(updatedETAs);

	}

	public void addRunways(int count, int currentTime) {
		updateCurrentTime(currentTime);

		if (count <= 0) {
			this.output.println("Invalid input. Please provide a valid number of runways.");
			return;
		}

		int currentRunwayCount = runways.getRunwayCount();
		for (int i = 0; i < count; i++) {
			runways.insert(new Runway(currentRunwayCount + i + 1, currentTime));
		}
		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println("Additional " + count + " Runways are now available");
		printAllChangedETAs(updatedETAs);

	}

	public void groundHold(int airlineLow, int airlineHigh, int currentTime) {
		updateCurrentTime(currentTime);

		if (airlineHigh < airlineLow) {
			this.output.println("Invalid input. Please provide a valid airline range.");
			return;
		}

		ArrayList<Integer> toRemove = new ArrayList<>();
		for (int airlineID = airlineLow; airlineID <= airlineHigh; airlineID++) {
			HashSet<Integer> flights = airlineIndex.get(airlineID);
			if (flights != null) {
				for (int flightID : flights) {
					Flight flight = activeFlights.get(flightID);
					if (flight != null &&
							(flight.getLifeCycleState() == StatusType.PENDING ||
									flight.getLifeCycleState() == StatusType.SCHEDULED)) {
						toRemove.add(flightID);
					}
				}
			}
		}

		for (int flightID : toRemove) {
			Flight flight = activeFlights.get(flightID);
			if (flight != null) {
				activeFlights.remove(flightID);
				HashSet<Integer> airlineFlights = airlineIndex.get(flight.getAirlineID());
				if (airlineFlights != null) {
					airlineFlights.remove(flightID);
				}
				PairingHeapNode node = handles.get(flightID);
				if (node != null) {
					pendingFlights.erase(node);
					handles.remove(flightID);
				}
				timetable.remove(flight);
			}
		}

		ArrayList<String> updatedETAs = rescheduleFlights();
		this.output.println(
				"Flights of the airlines in the range [" + airlineLow + ", " + airlineHigh + "] have been grounded");
		printAllChangedETAs(updatedETAs);
	}

	public void printActive() {
		ArrayList<Flight> flights = new ArrayList<>();

		for (Flight flight : activeFlights.values()) {
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
			output.println("[flight" + flight.getFlightID() + ", airline" + flight.getAirlineID() +
					", runway" + flight.getRunwayID() + ", start" + flight.getStartTime() +
					", ETA" + flight.getEndTime() + "]");
		}
	}

	public void printSchedule(int endTime1, int endTime2) {
		ArrayList<Flight> scheduledFlights = new ArrayList<>();

		for (Flight flight : activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.SCHEDULED &&
					flight.getStartTime() > currentTime &&
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
			output.println("[" + flight.getFlightID() + "]");
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
		if (newTime < currentTime)
			return;
		currentTime = newTime;

		// Need to complete all the flights between the old current time and the new
		// time
		settleCompletions();

		// Need to start all flights between the old current time and the new time
		promoteFlights();

		// Need to reschedule the unsatisfied flights
		rescheduleFlights();
	}

	private void settleCompletions() {
		ArrayList<Flight> completed = timetable.popAllCompleted(currentTime);

		for (Flight flight : completed) {
			flight.setLifeCycleState(StatusType.COMPLETED);
			output.println("Flight " + flight.getFlightID() + " has landed at time " + flight.getEndTime());

			activeFlights.remove(flight.getFlightID());
			handles.remove(flight.getFlightID());

			HashSet<Integer> airlineFlights = airlineIndex.get(flight.getAirlineID());
			if (airlineFlights != null) {
				airlineFlights.remove(flight.getFlightID());
			}
		}
	}

	private void promoteFlights() {
		for (Flight flight : activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.SCHEDULED &&
					flight.getStartTime() <= currentTime &&
					flight.getEndTime() > currentTime) {
				flight.setLifeCycleState(StatusType.IN_PROGRESS);
			}
		}
	}

	private ArrayList<String> rescheduleFlights() {
		ArrayList<Flight> toReschedule = new ArrayList<>();

		// Store OLD ETAs before rescheduling (only for flights that were already
		// scheduled)
		HashMap<Integer, Integer> oldEndTimes = new HashMap<>();

		// Collect all scheduled flights that haven't started yet
		for (Flight flight : activeFlights.values()) {
			if (flight.getLifeCycleState() == StatusType.SCHEDULED && flight.getStartTime() > currentTime) {
				toReschedule.add(flight);
				oldEndTimes.put(flight.getFlightID(), flight.getEndTime());
			}
		}

		// Collect all pending flights
		while (!pendingFlights.isEmpty()) {
			Flight f = pendingFlights.pop();
			toReschedule.add(f);
			handles.remove(f.getFlightID());
		}

		if (toReschedule.isEmpty())
			return new ArrayList<>();

		// Sort by priority (DESCENDING), then submitTime (ASCENDING), then flightID
		// (ASCENDING)
		toReschedule.sort((f1, f2) -> {
			if (f1.getPriority() != f2.getPriority()) {
				return Integer.compare(f2.getPriority(), f1.getPriority()); // Higher priority first
			}
			if (f1.getSubmitTime() != f2.getSubmitTime()) {
				return Integer.compare(f1.getSubmitTime(), f2.getSubmitTime()); // Earlier submit first
			}
			return Integer.compare(f1.getFlightID(), f2.getFlightID()); // Lower ID first
		});

		rebuildTimeTableAndRunway();

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

// import models.*;
// import dataStructures.*;
// import java.util.*;
// import java.io.PrintWriter;
//
// public class Scheduler {
// private RunwayMinHeap runways;
// private PairingHeap pendingFlights;
// private TimetableMinHeap timetable;
// private HashMap<Integer, Flight> activeFlights;
// private HashMap<Integer, HashSet<Integer>> airlineIndex;
// private HashMap<Integer, PairingHeapNode> handles;
// private int currentTime;
// private PrintWriter output;
//
// public Scheduler(PrintWriter output) {
// this.runways = new RunwayMinHeap();
// this.pendingFlights = new PairingHeap();
// this.timetable = new TimetableMinHeap();
// this.activeFlights = new HashMap<>();
// this.airlineIndex = new HashMap<>();
// this.handles = new HashMap<>();
// this.currentTime = 0;
// this.output = output;
// }
//
// public void initialize(int runwayCount) {
// if (runwayCount < 0) {
// this.output.println("Invalid input. Please provide a valid number of
// runways.");
// return;
// }
//
// for (int i = 0; i < runwayCount; i++) {
// this.runways.insert(new Runway(i + 1, 0));
// }
// this.output.println(runwayCount + " Runways are now available");
// }
//
// public void submitFlight(int flightID, int airlineID, int submitTime, int
// priority, int duration) {
// advanceTime(submitTime);
//
// if (activeFlights.containsKey(flightID)) {
// this.output.println("Duplicate FlightID");
// return;
// }
//
// Flight flight = new Flight(flightID, airlineID, submitTime, priority,
// duration);
//
// System.out.println("Submitting flight " + flight);
// activeFlights.put(flightID, flight);
//
// if (!airlineIndex.containsKey(airlineID)) {
// airlineIndex.put(airlineID, new HashSet<>());
// }
// airlineIndex.get(airlineID).add(flightID);
//
// PairingHeapNode node = pendingFlights.push(flight);
// handles.put(flightID, node);
//
// // Schedule this flight immediately
// rescheduleFlights();
//
// output.println("Flight " + flightID + " scheduled - ETA: " +
// flight.getEndTime());
// }
//
// public void cancelFlight(int flightID, int currentTime) {
// advanceTime(currentTime);
//
// if (!this.activeFlights.containsKey(flightID)) {
// this.output.println("Flight " + flightID + " does not exist");
// return;
// }
//
// Flight flight = activeFlights.get(flightID);
//
// if (flight.getLifeCycleState() == StatusType.IN_PROGRESS ||
// flight.getLifeCycleState() == StatusType.COMPLETED) {
// output.println("Cannot cancel flight " + flightID + " - already departed");
// return;
// }
//
// // Remove from all data structures
// activeFlights.remove(flightID);
// HashSet<Integer> airlineFlights = airlineIndex.get(flight.getAirlineID());
// if (airlineFlights != null) {
// airlineFlights.remove(flightID);
// }
// PairingHeapNode node = handles.get(flightID);
// if (node != null) {
// pendingFlights.erase(node);
// handles.remove(flightID);
// }
// output.println("Flight " + flightID + " has been canceled");
//
// rescheduleFlights();
// }
//
// public void reprioritize(int flightID, int currentTime, int newPriority) {
// advanceTime(currentTime);
//
// Flight flight = activeFlights.get(flightID);
// if (flight == null) {
// output.println("Flight " + flightID + " not found");
// return;
// }
//
// if (flight.getLifeCycleState() == StatusType.IN_PROGRESS ||
// flight.getLifeCycleState() == StatusType.COMPLETED) {
// output.println("Cannot reprioritize flight " + flightID + " - already
// departed");
// return;
// }
//
// flight.setPriority(newPriority);
// PairingHeapNode node = handles.get(flightID);
// if (node != null) {
// pendingFlights.increaseKey(node, newPriority);
// }
// this.output.println("Priority of Flight " + flightID + " has been updated to
// " + newPriority);
//
// rescheduleFlights();
// }
//
// public void addRunways(int count, int currentTime) {
// advanceTime(currentTime);
//
// if (count <= 0) {
// this.output.println("Invalid input. Please provide a valid number of
// runways.");
// return;
// }
//
// int currentRunwayCount = runways.getRunwayCount();
// for (int i = 0; i < count; i++) {
// runways.insert(new Runway(currentRunwayCount + i + 1, currentTime));
// }
// this.output.println("Additional " + count + " Runways are now available");
//
// rescheduleFlights();
// }
//
// public void groundHold(int airlineLow, int airlineHigh, int currentTime) {
// advanceTime(currentTime);
//
// if (airlineHigh < airlineLow) {
// this.output.println("Invalid input. Please provide a valid airline range.");
// return;
// }
//
// ArrayList<Integer> toCancel = new ArrayList<>();
// for (int airlineID = airlineLow; airlineID <= airlineHigh; airlineID++) {
// HashSet<Integer> flights = airlineIndex.get(airlineID);
// if (flights != null) {
// toCancel.addAll(new ArrayList<>(flights));
// }
// }
//
// for (int flightID : toCancel) {
// Flight flight = activeFlights.get(flightID);
// if (flight != null && (flight.getLifeCycleState() == StatusType.PENDING ||
// flight.getLifeCycleState() == StatusType.SCHEDULED)) {
// activeFlights.remove(flightID);
// HashSet<Integer> airlineFlights = airlineIndex.get(flight.getAirlineID());
// if (airlineFlights != null) {
// airlineFlights.remove(flightID);
// }
// PairingHeapNode node = handles.get(flightID);
// if (node != null) {
// pendingFlights.erase(node);
// handles.remove(flightID);
// }
// }
// }
// this.output.println(
// "Flights of the airlines in the range [" + airlineLow + ", " + airlineHigh +
// "] have been grounded");
//
// rescheduleFlights();
// }
//
// public void printActive() {
// ArrayList<Flight> flights = new ArrayList<>(activeFlights.values());
//
// if (flights.isEmpty()) {
// this.output.println("No active flights");
// return;
// }
//
// flights.sort((a, b) -> Integer.compare(a.getFlightID(), b.getFlightID()));
// for (Flight flight : flights) {
// output.println("[flight" + flight.getFlightID() + ", airline" +
// flight.getAirlineID() +
// ", runway" + flight.getRunwayID() + ", start" + flight.getStartTime() +
// ", ETA" + flight.getEndTime() + "]");
// }
// }
//
// public void printSchedule(int endTime1, int endTime2) {
// ArrayList<Flight> scheduledFlights = new ArrayList<>();
//
// for (Flight flight : activeFlights.values()) {
// if (flight.getLifeCycleState() == StatusType.SCHEDULED &&
// flight.getEndTime() >= endTime1 && flight.getEndTime() <= endTime2) {
// scheduledFlights.add(flight);
// }
// }
//
// if (scheduledFlights.isEmpty()) {
// this.output.println("There are no flights in that time period");
// return;
// }
//
// scheduledFlights.sort((a, b) -> Integer.compare(a.getFlightID(),
// b.getFlightID()));
// for (Flight flight : scheduledFlights) {
// output.println("[" + flight.getFlightID() + "]");
// }
// }
//
// public void tick(int time) {
// advanceTime(time);
// }
//
// // Internal functions
//
// private void advanceTime(int newTime) {
// if (newTime < currentTime)
// return;
// currentTime = newTime;
//
// settleCompletions();
// promoteFlights();
// }
//
// private void settleCompletions() {
// ArrayList<Flight> completed = timetable.popAllCompleted(currentTime);
//
// for (Flight flight : completed) {
// flight.setLifeCycleState(StatusType.COMPLETED);
// output.println("Flight " + flight.getFlightID() + " has landed at time " +
// flight.getEndTime());
//
// activeFlights.remove(flight.getFlightID());
// handles.remove(flight.getFlightID());
//
// HashSet<Integer> airlineFlights = airlineIndex.get(flight.getAirlineID());
// if (airlineFlights != null) {
// airlineFlights.remove(flight.getFlightID());
// }
// }
// }
//
// private void promoteFlights() {
// for (Flight flight : activeFlights.values()) {
// if (flight.getLifeCycleState() == StatusType.SCHEDULED &&
// flight.getStartTime() <= currentTime &&
// flight.getEndTime() > currentTime) {
// flight.setLifeCycleState(StatusType.IN_PROGRESS);
// }
// }
// }
//
// private void rescheduleFlights() {
// ArrayList<Flight> toReschedule = new ArrayList<>();
//
// // Store OLD ETAs before rescheduling (only for flights that were already
// // scheduled)
// HashMap<Integer, Integer> oldETAs = new HashMap<>();
//
// // Collect all scheduled flights that haven't started yet
// for (Flight flight : activeFlights.values()) {
// if (flight.getLifeCycleState() == StatusType.SCHEDULED &&
// flight.getStartTime() > currentTime) {
// toReschedule.add(flight);
// oldETAs.put(flight.getFlightID(), flight.getEndTime());
// }
// }
//
// // Collect all pending flights
// while (!pendingFlights.isEmpty()) {
// Flight f = pendingFlights.pop();
// toReschedule.add(f);
// handles.remove(f.getFlightID());
// }
//
// if (toReschedule.isEmpty())
// return;
//
// // Sort by priority (DESCENDING), then submitTime (ASCENDING), then flightID
// // (ASCENDING)
// toReschedule.sort((f1, f2) -> {
// if (f1.getPriority() != f2.getPriority()) {
// return Integer.compare(f2.getPriority(), f1.getPriority()); // Higher
// priority first
// }
// if (f1.getSubmitTime() != f2.getSubmitTime()) {
// return Integer.compare(f1.getSubmitTime(), f2.getSubmitTime()); // Earlier
// submit first
// }
// return Integer.compare(f1.getFlightID(), f2.getFlightID()); // Lower ID first
// });
//
// // Rebuild timetable: keep only in-progress flights
// TimetableMinHeap newTimetable = new TimetableMinHeap();
// for (Flight flight : activeFlights.values()) {
// if (flight.getLifeCycleState() == StatusType.IN_PROGRESS) {
// newTimetable.insert(flight);
// }
// }
// this.timetable = newTimetable;
//
// // Rebuild runway pool
// ArrayList<Runway> runwayList = runways.getAllRunways();
// runways.deleteAllRunways();
//
// // Initialize runway times to currentTime
// HashMap<Integer, Integer> runwayTimes = new HashMap<>();
// for (Runway runway : runwayList) {
// runwayTimes.put(runway.getRunwayID(), currentTime);
// }
//
// // Update runway times based on in-progress flights
// for (Flight flight : activeFlights.values()) {
// if (flight.getLifeCycleState() == StatusType.IN_PROGRESS) {
// int rid = flight.getRunwayID();
// runwayTimes.put(rid, flight.getEndTime());
// }
// }
//
// // Insert runways back with updated times
// for (Runway runway : runwayList) {
// runway.setNextFreeTime(runwayTimes.get(runway.getRunwayID()));
// runways.insert(runway);
// }
//
// ArrayList<String> endTimeUpdates = new ArrayList<>();
//
// // Greedy assignment
// for (Flight flight : toReschedule) {
// Runway runway = runways.extractMin();
// int startTime = Math.max(currentTime, runway.getNextFreeTime());
// int newEndTime = startTime + flight.getDuration();
//
// flight.setStartTime(startTime);
// flight.setEndTime(newEndTime);
// flight.setRunwayID(runway.getRunwayID());
// flight.setLifeCycleState(StatusType.SCHEDULED);
//
// runway.setNextFreeTime(newEndTime);
// runways.insert(runway);
//
// timetable.insert(flight);
//
// // Only report ETA changes for flights that were ALREADY scheduled
// if (oldETAs.containsKey(flight.getFlightID())) {
// int oldETA = oldETAs.get(flight.getFlightID());
// if (oldETA != newEndTime) {
// endTimeUpdates.add(flight.getFlightID() + ": " + newEndTime);
// }
// }
// }
//
// if (!endTimeUpdates.isEmpty()) {
// endTimeUpdates.sort((a, b) -> {
// int id1 = Integer.parseInt(a.split(":")[0]);
// int id2 = Integer.parseInt(b.split(":")[0]);
// return Integer.compare(id1, id2);
// });
// output.println("Updated ETAs: [" + String.join(", ", endTimeUpdates) + "]");
// }
// }
//
// public void quit() {
// output.println("Program Terminated!!");
// }
// }
