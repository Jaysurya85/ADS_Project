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
		heap.add(flight);
		heapifyUp(heap.size() - 1);
	}

	public ArrayList<Flight> popAllCompleted(int currentTime) {
		ArrayList<Flight> completed = new ArrayList<>();
		while (!heap.isEmpty() && heap.get(0).getEndTime() <= currentTime) {
			completed.add(extractMin());
		}
		return completed;
	}

	public void clear() {
		this.heap.clear();
	}

	public boolean remove(Flight flight) {
		if (heap.isEmpty())
			return false;

		int index = -1;
		for (int i = 0; i < heap.size(); i++) {
			if (heap.get(i).getFlightID() == flight.getFlightID()) {
				index = i;
				break;
			}
		}
		if (index == -1)
			return false;

		int lastIndex = heap.size() - 1;
		if (index != lastIndex) {
			swap(index, lastIndex);
		}
		heap.remove(lastIndex);

		// restore heap property (both directions possible)
		if (index < heap.size()) {
			heapifyUp(index);
			heapifyDown(index);
		}

		return true;
	}

	// ================= Internal functions of Binary Heap =============

	private Flight extractMin() {
		if (heap.isEmpty())
			return null;

		Flight minFlight = heap.get(0);

		if (heap.size() == 1) {
			heap.clear();
			return minFlight;
		}

		heap.set(0, heap.get(heap.size() - 1));
		heap.remove(heap.size() - 1);
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
		Flight temp = heap.get(i);
		heap.set(i, heap.get(j));
		heap.set(j, temp);
	}

	private void heapifyUp(int i) {
		while (i > 0 && compare(heap.get(i), heap.get(parent(i))) < 0) {
			swap(i, parent(i));
			i = parent(i);
		}
	}

	private void heapifyDown(int i) {
		int minIndex = i;
		int left = leftChild(i);
		int right = rightChild(i);

		if (left < heap.size() && compare(heap.get(left), heap.get(minIndex)) < 0) {
			minIndex = left;
		}
		if (right < heap.size() && compare(heap.get(right), heap.get(minIndex)) < 0) {
			minIndex = right;
		}

		if (minIndex != i) {
			swap(i, minIndex);
			heapifyDown(minIndex);
		}
	}

}
