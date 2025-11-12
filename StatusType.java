public enum StatusType {
	PENDING("PENDING"),
	SCHEDULED("SCHEDULED"),
	IN_PROGRESS("IN_PROGRESS"),
	COMPLETED("COMPLETED");

	private String label;

	private StatusType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return this.label;
	}
}
