package dataStructures;

import java.util.ArrayList;
import java.util.HashMap;
import models.*;

public class RunwayMinHeap {
	private ArrayList<Runway> heap;
	private HashMap<Integer, Integer> positionMap;

	public RunwayMinHeap() {
		this.heap = new ArrayList<>();
		this.positionMap = new HashMap<>();
	}

	public int getRunwayCount() {
		return this.heap.size();
	}

	public void insert(Runway runway) {
		heap.add(runway);
		int index = heap.size() - 1;
		positionMap.put(runway.getRunwayID(), index);
		heapifyUp(index);
	}

	public Runway extractMin() {
		if (heap.isEmpty())
			return null;

		Runway minRunway = heap.get(0);
		positionMap.remove(minRunway.getRunwayID());

		if (heap.size() == 1) {
			heap.clear();
			return minRunway;
		}

		heap.set(0, heap.get(heap.size() - 1));
		heap.remove(heap.size() - 1);
		positionMap.put(heap.get(0).getRunwayID(), 0);
		heapifyDown(0);

		return minRunway;
	}

	public ArrayList<Runway> getAllRunways() {
		return new ArrayList<>(heap);
	}

	public void deleteAllRunways() {
		heap.clear();
		positionMap.clear();
	}

	private int parentInd(int i) {
		return (i - 1) / 2;
	}

	private int leftChildInd(int i) {
		return 2 * i + 1;
	}

	private int rightChildInd(int i) {
		return 2 * i + 2;
	}

	private void swap(int i, int j) {
		Runway temp = heap.get(i);
		heap.set(i, heap.get(j));
		heap.set(j, temp);

		positionMap.put(heap.get(i).getRunwayID(), i);
		positionMap.put(heap.get(j).getRunwayID(), j);
	}

	private void heapifyUp(int i) {
		while (i > 0 && compare(heap.get(i), heap.get(parentInd(i))) < 0) {
			swap(i, parentInd(i));
			i = parentInd(i);
		}
	}

	private void heapifyDown(int i) {
		int minIndex = i;
		int left = leftChildInd(i);
		int right = rightChildInd(i);

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

	private int compare(Runway runway1, Runway runway2) {
		if (runway1.getNextFreeTime() != runway2.getNextFreeTime()) {
			return Integer.compare(runway1.getNextFreeTime(), runway2.getNextFreeTime());
		}
		return Integer.compare(runway1.getRunwayID(), runway2.getRunwayID());
	}

}
