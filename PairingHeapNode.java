public class PairingHeapNode {
	private Flight flight;
	private PairingHeapNode child;
	private PairingHeapNode sibling;
	private PairingHeapNode prev;

	public PairingHeapNode(Flight flight) {
		this.flight = flight;
		this.child = null;
		this.sibling = null;
		this.prev = null;
	}

	public Flight getFlight() {
		return flight;
	}

	public PairingHeapNode getChild() {
		return child;
	}

	public PairingHeapNode getSibling() {
		return sibling;
	}

	public PairingHeapNode getPrev() {
		return prev;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public void setChild(PairingHeapNode child) {
		this.child = child;
	}

	public void setSibling(PairingHeapNode sibling) {
		this.sibling = sibling;
	}

	public void setPrev(PairingHeapNode prev) {
		this.prev = prev;
	}
}
