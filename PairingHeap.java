package dataStructures;

import models.*;

public class PairingHeap {
	private PairingHeapNode root;

	public PairingHeap() {
		this.root = null;
	}

	// ================== Public Functions of Pairing Heap====================

	public boolean isEmpty() {
		return this.root == null;
	}

	public int compare(Flight flight1, Flight flight2) {
		if (flight1.getPriority() != flight2.getPriority()) {
			return Integer.compare(flight2.getPriority(), flight1.getPriority());
		}
		if (flight1.getSubmitTime() != flight2.getSubmitTime()) {
			return Integer.compare(flight2.getSubmitTime(), flight1.getSubmitTime());
		}
		return Integer.compare(flight2.getFlightID(), flight1.getFlightID());
	}

	public PairingHeapNode push(Flight flight) {
		PairingHeapNode node = new PairingHeapNode(flight);
		this.root = meld(this.root, node);
		return node;
	}

	public Flight pop() {
		if (this.root == null)
			return null;

		Flight maxFlight = this.root.getFlight();
		this.root = mergePairs(this.root.getChild());
		if (this.root != null) {
			this.root.setPrev(null);
		}
		return maxFlight;
	}

	public void increaseKey(PairingHeapNode node, int newPriority) {
		if (node == null)
			return;
		node.getFlight().setPriority(newPriority);

		if (node == this.root)
			return;

		if (node.getPrev() != null) {
			if (node.getPrev().getChild() == node) {
				node.getPrev().setChild(node.getSibling());
			} else {
				node.getPrev().setSibling(node.getSibling());
			}
		}
		if (node.getSibling() != null) {
			node.getSibling().setPrev(node.getPrev());
		}

		node.setSibling(null);
		node.setPrev(null);
		this.root = meld(this.root, node);
	}

	public void erase(PairingHeapNode node) {
		if (node == null)
			return;

		if (node == this.root) {
			pop();
			return;
		}

		if (node.getPrev() != null && node.getPrev().getChild() == node) {
			node.getPrev().setChild(node.getSibling());
		} else if (node.getPrev() != null && node.getPrev().getChild() != node) {
			node.getPrev().setSibling(node.getSibling());
		}
		if (node.getSibling() != null) {
			node.getSibling().setPrev(node.getPrev());
		}

		PairingHeapNode mergedChildren = mergePairs(node.getChild());
		if (mergedChildren != null) {
			this.root = meld(this.root, mergedChildren);
		}
	}

	// ================== Internal functions of Pairing Heap====================

	private PairingHeapNode meld(PairingHeapNode a, PairingHeapNode b) {
		if (a == null)
			return b;
		if (b == null)
			return a;

		if (compare(a.getFlight(), b.getFlight()) > 0) {
			b.setPrev(a);
			b.setSibling(a.getChild());
			if (a.getChild() != null) {
				a.getChild().setPrev(b);
			}
			a.setChild(b);
			a.setSibling(null);
			a.setPrev(null);
			return a;
		} else {
			a.setPrev(b);
			a.setSibling(b.getChild());
			if (b.getChild() != null) {
				b.getChild().setPrev(a);
			}
			b.setChild(a);
			b.setSibling(null);
			b.setPrev(null);
			return b;
		}
	}

	private PairingHeapNode mergePairs(PairingHeapNode node) {
		if (node == null || node.getSibling() == null) {
			return node;
		}

		// First pass: merge pairs left to right
		PairingHeapNode nextPair = node.getSibling().getSibling();
		PairingHeapNode merged = meld(node, node.getSibling());
		PairingHeapNode rest = mergePairs(nextPair);

		// Second pass: merge result with rest
		return meld(merged, rest);
	}

}
