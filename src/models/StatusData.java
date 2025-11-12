
package models;

public class StatusData {
	private StatusType type;
	private String rawCommand;

	public StatusData(StatusType type, String rawCommand) {
		this.type = type;
		this.rawCommand = rawCommand;
	}

	// Getters
	public StatusType getType() {
		return type;
	}

	public String getRawCommand() {
		return rawCommand;
	}

	public void setType(StatusType type) {
		this.type = type;
	}

	public void setRawCommand(String rawCommand) {
		this.rawCommand = rawCommand;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CommandData{type=").append(type);
		return sb.toString();
	}
}
