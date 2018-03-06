import lombok.Getter;
import lombok.Setter;
import org.jgrapht.Graph;
import org.jgrapht.graph.GraphWalk;

import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class Path extends GraphWalk<PRouter, PLink> implements Serializable {

	private static int COUNTER = 0;
	private String name;
	private int index;

	private PathEnds.Direction direction;

	public Path(Graph<PRouter, PLink> graph, List<PRouter> vertexList, double weight) {
		super(graph, vertexList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = PathEnds.Direction.BOTH;
	}

	public Path(Graph<PRouter, PLink> graph, PRouter startVertex, PRouter endVertex, List<PLink> edgeList, double weight) {
		super(graph, startVertex, endVertex, edgeList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = PathEnds.Direction.BOTH;
	}

	public Path(Graph<PRouter, PLink> graph, PRouter startVertex, PRouter endVertex, List<PRouter> vertexList, List<PLink> edgeList, double weight) {
		super(graph, startVertex, endVertex, vertexList, edgeList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = PathEnds.Direction.BOTH;
	}

	public int getLeastCapacity() {
		return getEdgeList().stream().mapToInt(PLink::getSubstrateCapacity).min().getAsInt();
	}

	public double getLeastCapacityRate() {
		return getEdgeList().stream().mapToDouble(l -> (double)l.getSubstrateCapacity()/l.getCapacity()).min().getAsDouble();
	}

	public boolean checkCapacity(int capacity) {
		return getLeastCapacity() >= capacity;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getSource()).append(" ")
				.append(getSource().getVRouters() != null ? getSource().getVRouters() : "")
				.append(" ").append(getTarget())
				.append(getTarget().getVRouters() != null ? getTarget().getVRouters() : "")
				.append(" ").append(edgeList);
		return sb.toString();
	}

	public String toOPL() {
//		return String.format("%s %s %d", getSource().getName(), getTarget().getName(), index);
		return String.format("%s %s %d", getStartVertex().getName(), getEndVertex().getName(), index);
	}

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		edgeList.forEach(l -> sb.append(l));
		return sb.toString();
	}

	public void count() {
		index = ++COUNTER;
	}

	public static void resetCounter() {
		COUNTER = 0;
	}

	public int serveRequest(int request, VLink link) {
		getEdgeList().forEach(l -> l.serveRequest(request, link));
		if (direction == PathEnds.Direction.BOTH)
			checkReverse(link);
		getSource().serveRequest(request, link.getSource());
		getTarget().serveRequest(request, link.getTarget());
		return link.getCapacity() * getEdgeList().size();
	}

	private void checkReverse(VLink link) {
		boolean shouldReverse = shouldReverse() != shouldReverse(link);
		if (shouldReverse)
			setDirection(PathEnds.Direction.REVERSE);
	}

	private boolean reverseParameters(int power1, int memory1, int power2, int memory2, int max) {
		int maxPower = max;
		int	minPower = power1 >= power2 ? power2 : power1;
		int maxMemory = max;
		int	minMemory = memory1 >= memory2 ? memory2 : memory1;
		double diffPower = maxPower - minPower + Double.MIN_VALUE;
		double diffMemory = maxMemory - minMemory + Double.MIN_VALUE;
		double p1 = (maxPower - power1) / diffPower;
		double m1 = (maxMemory - memory1) / diffMemory;
		double p2 = (maxPower - power2) / diffPower;
		double m2 = (maxMemory - memory2) / diffMemory;
		double min1 = Math.min(p1, m1);
		double min2 = Math.min(p2, m2);
		return min1 > min2;
	}

	private boolean shouldReverse() {
		return shouldReverse(getSource(), getTarget());
	}

	private boolean shouldReverse(VLink link) {
		return shouldReverse(link.getSource(), link.getTarget());
	}

	private boolean shouldReverse(VRouter r1, VRouter r2) {
		return reverseParameters(r1.getPower(), r1.getMemory(), r2.getPower(), r2.getMemory(), 40);
	}

	private boolean shouldReverse(PRouter r1, PRouter r2) {
		return reverseParameters(r1.getSubstratePower(), r1.getSubstrateMemory(), r2.getSubstratePower(), r2.getSubstrateMemory(), 200);
	}

	public int releaseRequest(int request) {
		int addedCapacity = 0;
		for (PLink l : getEdgeList()) {
			addedCapacity += l.removeRequest(request);
			if (Heuristic.WEIGHTABLE_LINKS)
				l.updateWeight();
		}
		getStartVertex().removeRequest(request);
		getEndVertex().removeRequest(request);
		return addedCapacity;
	}

	public PRouter getSource() {
		return direction.equals(PathEnds.Direction.SIMPLE) || direction.equals(PathEnds.Direction.BOTH) ? getStartVertex() : getEndVertex();
	}

	public PRouter getTarget() {
		return direction.equals(PathEnds.Direction.SIMPLE) || direction.equals(PathEnds.Direction.BOTH) ? getEndVertex() : getStartVertex();
	}

	public String getDelta() {
		StringBuilder sb = new StringBuilder();
		getEdgeList().forEach(l -> sb.append(l.getName() + " " + toString() + " 1\n"));
		return sb.toString();
	}
}