package dataStructures;

import models.*;
import java.util.ArrayList;

public class TimetableMinHeap {
	private ArrayList<Flight> heap;

	public TimetableMinHeap() {
		this.heap = new ArrayList<>();
	}

	// ============= External functions of Timetable Min Heap====================

	public void insert(Flight flight) {
		this.heap.add(flight);
		heapifyUp(this.heap.size() - 1);
	}

	public ArrayList<Flight> popAllCompleted(int currentTime) {
		ArrayList<Flight> completed = new ArrayList<>();
		while (!this.heap.isEmpty() && this.heap.get(0).getEndTime() <= currentTime) {
			completed.add(extractMin());
		}
		return completed;
	}

	public void clear() {
		this.heap.clear();
	}

	public boolean remove(Flight flight) {
		if (this.heap.isEmpty())
			return false;

		int index = -1;
		for (int i = 0; i < this.heap.size(); i++) {
			if (this.heap.get(i).getFlightID() == flight.getFlightID()) {
				index = i;
				break;
			}
		}
		if (index == -1)
			return false;

		int lastIndex = this.heap.size() - 1;
		if (index != lastIndex) {
			swap(index, lastIndex);
		}
		this.heap.remove(lastIndex);

		// restore heap property (both directions possible)
		if (index < this.heap.size()) {
			heapifyUp(index);
			heapifyDown(index);
		}

		return true;
	}

	// ================= Internal functions of Binary Heap =============

	private Flight extractMin() {
		if (this.heap.isEmpty())
			return null;

		Flight minFlight = this.heap.get(0);

		if (this.heap.size() == 1) {
			this.heap.clear();
			return minFlight;
		}

		this.heap.set(0, this.heap.get(this.heap.size() - 1));
		this.heap.remove(this.heap.size() - 1);
		heapifyDown(0);

		return minFlight;
	}

	private int compare(Flight flight1, Flight flight2) {
		if (flight1.getEndTime() != flight2.getEndTime()) {
			return Integer.compare(flight1.getEndTime(), flight2.getEndTime());
		}
		return Integer.compare(flight1.getFlightID(), flight2.getFlightID());
	}

	private int parent(int i) {
		return (i - 1) / 2;
	}

	private int leftChild(int i) {
		return 2 * i + 1;
	}

	private int rightChild(int i) {
		return 2 * i + 2;
	}

	private void swap(int i, int j) {
		Flight temp = this.heap.get(i);
		this.heap.set(i, this.heap.get(j));
		this.heap.set(j, temp);
	}

	private void heapifyUp(int i) {
		while (i > 0 && compare(this.heap.get(i), this.heap.get(parent(i))) < 0) {
			swap(i, parent(i));
			i = parent(i);
		}
	}

	private void heapifyDown(int i) {
		int minIndex = i;
		int left = leftChild(i);
		int right = rightChild(i);

		if (left < this.heap.size() && compare(this.heap.get(left), this.heap.get(minIndex)) < 0) {
			minIndex = left;
		}
		if (right < this.heap.size() && compare(this.heap.get(right), this.heap.get(minIndex)) < 0) {
			minIndex = right;
		}

		if (minIndex != i) {
			swap(i, minIndex);
			heapifyDown(minIndex);
		}
	}

}
