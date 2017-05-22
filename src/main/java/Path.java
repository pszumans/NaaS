import org.jgrapht.Graph;
import org.jgrapht.graph.GraphWalk;

import java.util.*;

public class Path extends GraphWalk<pRouter, pLink> {

	private static int COUNTER = 0;
	private String name;
	private int index;

	public String getDelta() {
		StringBuilder sb = new StringBuilder();
		getEdgeList().forEach(l -> sb.append(l.getName() + " " + toString() + " 1\n"));
		return sb.toString();
	}

	public enum Direction {SIMPLE, REVERSE;};
	private Direction direction;

	public Path(Graph<pRouter, pLink> graph, List<pRouter> vertexList, double weight) {
		super(graph, vertexList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = Direction.SIMPLE;
	}

	public Path(Graph<pRouter, pLink> graph, pRouter startVertex, pRouter endVertex, List<pLink> edgeList, double weight) {
		super(graph, startVertex, endVertex, edgeList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = Direction.SIMPLE;
	}

	public Path(Graph<pRouter, pLink> graph, pRouter startVertex, pRouter endVertex, List<pRouter> vertexList, List<pLink> edgeList, double weight) {
		super(graph, startVertex, endVertex, vertexList, edgeList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = Direction.SIMPLE;
	}

	private int getLeastCapacity() {
		return getEdgeList().stream().mapToInt(pLink::getSubstrateCapacity).min().getAsInt();
	}

	public boolean checkCapacity(int capacity) {
		return getLeastCapacity() >= capacity;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	@Override
	public String toString() {
		return String.format("%s %s %d", getSource().getName(), getTarget().getName(), index, getLeastCapacity());
	}

	public void count() {
		index = ++COUNTER;
	}

	public static void resetCounter() {
		COUNTER = 0;
	}

	public int serveRequest(int request, vLink link) {
		getEdgeList().forEach(l -> l.serveRequest(request, link));
//		for (pLink l : getEdgeList()) {
//			l.serveRequest(request, link);
//			addedCapacity += link.getCapacity();
//		}
//		link.getRouters().forEach(vR -> {
		getSource().serveRequest(request, link.getSource());
		getTarget().serveRequest(request, link.getTarget());
		return link.getCapacity() * getEdgeList().size();
//			});
	}

	public int releaseRequest(int request) {
		int addedCapacity = 0;
//		getEdgeList().forEach(l -> l.removeRequest(request));
		for (pLink l : getEdgeList()) {
			addedCapacity += l.removeRequest(request);
		}
		getStartVertex().removeRequest(request);
		getEndVertex().removeRequest(request);
		return addedCapacity;
	}

	public pRouter getSource() {
		return direction.equals(Direction.SIMPLE) ? getStartVertex() : getEndVertex();
	}

	public pRouter getTarget() {
		return direction.equals(Direction.SIMPLE) ? getEndVertex() : getStartVertex();
	}
}