import java.io.*;

public class gatorAirTrafficScheduler {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java gatorAirTrafficScheduler <input_file>");
			System.exit(1);
		}

		String inputFile = args[0];
		String outputFile = inputFile.replace(".txt", "_output_file.txt");

		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

			Scheduler scheduler = new Scheduler(writer);
			CommandParser parser = new CommandParser();

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("//")) {
					continue;
				}

				// Parse the command
				CommandData cmd = parser.parse(line);

				// Execute based on command type
				executeCommand(cmd, scheduler, writer);
			}

		} catch (FileNotFoundException e) {
			System.err.println("Input file not found: " + inputFile);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error reading/writing file: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void executeCommand(CommandData cmd, Scheduler scheduler, PrintWriter writer) {
		try {
			switch (cmd.getType()) {
				case INITIALIZE:
					int runwayCount = cmd.getParam(0);
					scheduler.initialize(runwayCount);
					break;

				case SUBMIT_FLIGHT:
					int flightID = cmd.getParam(0);
					int airlineID = cmd.getParam(1);
					int submitTime = cmd.getParam(2);
					int priority = cmd.getParam(3);
					int duration = cmd.getParam(4);
					scheduler.submitFlight(flightID, airlineID, submitTime, priority, duration);
					break;

				case CANCEL_FLIGHT:
					flightID = cmd.getParam(0);
					int currentTime = cmd.getParam(1);
					scheduler.cancelFlight(flightID, currentTime);
					break;

				case REPRIORITIZE:
					flightID = cmd.getParam(0);
					currentTime = cmd.getParam(1);
					int newPriority = cmd.getParam(2);
					scheduler.reprioritize(flightID, currentTime, newPriority);
					break;

				case ADD_RUNWAYS:
					int count = cmd.getParam(0);
					currentTime = cmd.getParam(1);
					scheduler.addRunways(count, currentTime);
					break;

				case GROUND_HOLD:
					int airlineLow = cmd.getParam(0);
					int airlineHigh = cmd.getParam(1);
					currentTime = cmd.getParam(2);
					scheduler.groundHold(airlineLow, airlineHigh, currentTime);
					break;

				case PRINT_ACTIVE:
					scheduler.printActive();
					break;

				case PRINT_SCHEDULE:
					int t1 = cmd.getParam(0);
					int t2 = cmd.getParam(1);
					scheduler.printSchedule(t1, t2);
					break;

				case TICK:
					int time = cmd.getParam(0);
					scheduler.tick(time);
					break;

				case QUIT:
					scheduler.quit();
					break;

				case UNKNOWN:
				default:
					writer.println("Unknown command: " + cmd.getRawCommand());
					break;
			}

		} catch (Exception e) {
			writer.println("Error executing command: " + cmd.getRawCommand());
			writer.println("Error message: " + e.getMessage());
			e.printStackTrace(writer);
		}
	}
}
