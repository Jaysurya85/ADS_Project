public class CommandData {
	private CommandType type;
	private int[] params;
	private String rawCommand;

	public CommandData(CommandType type, int[] params, String rawCommand) {
		this.type = type;
		this.params = params;
		this.rawCommand = rawCommand;
	}

	// Getters
	public CommandType getType() {
		return type;
	}

	public int[] getParams() {
		return params;
	}

	public String getRawCommand() {
		return rawCommand;
	}

	// Convenience methods for specific parameters
	public int getParam(int index) {
		if (params != null && index >= 0 && index < params.length) {
			return params[index];
		}
		return -1;
	}

	public int getParamCount() {
		return params != null ? params.length : 0;
	}

	// Setters
	public void setType(CommandType type) {
		this.type = type;
	}

	public void setParams(int[] params) {
		this.params = params;
	}

	public void setRawCommand(String rawCommand) {
		this.rawCommand = rawCommand;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CommandData{type=").append(type);
		if (params != null && params.length > 0) {
			sb.append(", params=[");
			for (int i = 0; i < params.length; i++) {
				sb.append(params[i]);
				if (i < params.length - 1)
					sb.append(", ");
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}
}
