
import java.util.ArrayList;
import java.util.HashMap;

public class RunwayMinHeap {
	private ArrayList<Runway> heap;
	private HashMap<Integer, Integer> positionMap;

	public RunwayMinHeap() {
		this.heap = new ArrayList<>();
		this.positionMap = new HashMap<>();
	}

	// ================== External functions of Runway Min Heap====================

	public int getRunwayCount() {
		return this.heap.size();
	}

	public void insert(Runway runway) {
		this.heap.add(runway);
		int index = this.heap.size() - 1;
		this.positionMap.put(runway.getRunwayID(), index);
		heapifyUp(index);
	}

	public Runway extractMin() {
		if (this.heap.isEmpty())
			return null;

		Runway minRunway = this.heap.get(0);
		this.positionMap.remove(minRunway.getRunwayID());

		if (this.heap.size() == 1) {
			this.heap.clear();
			return minRunway;
		}

		this.heap.set(0, this.heap.get(this.heap.size() - 1));
		this.heap.remove(this.heap.size() - 1);
		this.positionMap.put(this.heap.get(0).getRunwayID(), 0);
		heapifyDown(0);

		return minRunway;
	}

	public ArrayList<Runway> getAllRunways() {
		return new ArrayList<>(this.heap);
	}

	public void deleteAllRunways() {
		this.heap.clear();
		this.positionMap.clear();
	}

	// ================== Internal functions of Runway Min Heap====================

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
		Runway temp = this.heap.get(i);
		this.heap.set(i, this.heap.get(j));
		this.heap.set(j, temp);

		this.positionMap.put(this.heap.get(i).getRunwayID(), i);
		this.positionMap.put(this.heap.get(j).getRunwayID(), j);
	}

	private void heapifyUp(int i) {
		while (i > 0 && compare(this.heap.get(i), this.heap.get(parentInd(i))) < 0) {
			swap(i, parentInd(i));
			i = parentInd(i);
		}
	}

	private void heapifyDown(int i) {
		int minIndex = i;
		int left = leftChildInd(i);
		int right = rightChildInd(i);

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

	private int compare(Runway runway1, Runway runway2) {
		if (runway1.getNextFreeTime() != runway2.getNextFreeTime()) {
			return Integer.compare(runway1.getNextFreeTime(), runway2.getNextFreeTime());
		}
		return Integer.compare(runway1.getRunwayID(), runway2.getRunwayID());
	}

}
