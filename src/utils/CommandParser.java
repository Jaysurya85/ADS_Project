package utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import models.*;

public class CommandParser {

	public CommandData parse(String line) {
		if (line == null || line.trim().isEmpty()) {
			return new CommandData(CommandType.UNKNOWN, null, line);
		}
		line = line.trim();
		try {
			if (line.startsWith("Initialize")) {
				Pattern pattern = Pattern.compile("Initialize\\((\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int runwayCount = Integer.parseInt(matcher.group(1));
					return new CommandData(CommandType.INITIALIZE, new int[] { runwayCount }, line);
				}
			}

			else if (line.startsWith("SubmitFlight")) {
				Pattern pattern = Pattern
						.compile("SubmitFlight\\((\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int flightID = Integer.parseInt(matcher.group(1));
					int airlineID = Integer.parseInt(matcher.group(2));
					int submitTime = Integer.parseInt(matcher.group(3));
					int priority = Integer.parseInt(matcher.group(4));
					int duration = Integer.parseInt(matcher.group(5));
					return new CommandData(CommandType.SUBMIT_FLIGHT,
							new int[] { flightID, airlineID, submitTime, priority, duration }, line);
				}
			}

			else if (line.startsWith("CancelFlight")) {
				Pattern pattern = Pattern.compile("CancelFlight\\((\\d+),\\s*(\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int flightID = Integer.parseInt(matcher.group(1));
					int currentTime = Integer.parseInt(matcher.group(2));
					return new CommandData(CommandType.CANCEL_FLIGHT,
							new int[] { flightID, currentTime }, line);
				}
			}

			else if (line.startsWith("Reprioritize")) {
				Pattern pattern = Pattern.compile("Reprioritize\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int flightID = Integer.parseInt(matcher.group(1));
					int currentTime = Integer.parseInt(matcher.group(2));
					int newPriority = Integer.parseInt(matcher.group(3));
					return new CommandData(CommandType.REPRIORITIZE,
							new int[] { flightID, currentTime, newPriority }, line);
				}
			}

			else if (line.startsWith("AddRunways")) {
				Pattern pattern = Pattern.compile("AddRunways\\((\\d+),\\s*(\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int count = Integer.parseInt(matcher.group(1));
					int currentTime = Integer.parseInt(matcher.group(2));
					return new CommandData(CommandType.ADD_RUNWAYS,
							new int[] { count, currentTime }, line);
				}
			}

			else if (line.startsWith("GroundHold")) {
				Pattern pattern = Pattern.compile("GroundHold\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int airlineLow = Integer.parseInt(matcher.group(1));
					int airlineHigh = Integer.parseInt(matcher.group(2));
					int currentTime = Integer.parseInt(matcher.group(3));
					return new CommandData(CommandType.GROUND_HOLD,
							new int[] { airlineLow, airlineHigh, currentTime }, line);
				}
			}

			else if (line.startsWith("PrintActive")) {
				return new CommandData(CommandType.PRINT_ACTIVE, null, line);
			}

			else if (line.startsWith("PrintSchedule")) {
				Pattern pattern = Pattern.compile("PrintSchedule\\((\\d+),\\s*(\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int time1 = Integer.parseInt(matcher.group(1));
					int time2 = Integer.parseInt(matcher.group(2));
					return new CommandData(CommandType.PRINT_SCHEDULE,
							new int[] { time1, time2 }, line);
				}
			}

			else if (line.startsWith("Tick")) {
				Pattern pattern = Pattern.compile("Tick\\((\\d+)\\)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int time = Integer.parseInt(matcher.group(1));
					return new CommandData(CommandType.TICK, new int[] { time }, line);
				}
			}

			else if (line.startsWith("Quit")) {
				return new CommandData(CommandType.QUIT, null, line);
			}

		} catch (Exception e) {
			System.err.println("Error parsing command: " + line);
			System.err.println("Error: " + e.getMessage());
		}

		return new CommandData(CommandType.UNKNOWN, null, line);
	}

}
