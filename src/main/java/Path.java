import lombok.Getter;
import lombok.Setter;
import org.jgrapht.Graph;
import org.jgrapht.graph.GraphWalk;

import java.util.*;

@Getter @Setter
public class Path extends GraphWalk<pRouter, pLink> {

	private static int COUNTER = 0;
	private String name;
	private int index;

//	public enum Direction {SIMPLE, REVERSE, BOTH}

	private PathEnds.Direction direction;

	public Path(Graph<pRouter, pLink> graph, List<pRouter> vertexList, double weight) {
		super(graph, vertexList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = PathEnds.Direction.BOTH;
	}

	public Path(Graph<pRouter, pLink> graph, pRouter startVertex, pRouter endVertex, List<pLink> edgeList, double weight) {
		super(graph, startVertex, endVertex, edgeList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = PathEnds.Direction.BOTH;
	}

	public Path(Graph<pRouter, pLink> graph, pRouter startVertex, pRouter endVertex, List<pRouter> vertexList, List<pLink> edgeList, double weight) {
		super(graph, startVertex, endVertex, vertexList, edgeList, weight);
		name = getStartVertex() + " " + getEndVertex();
		index = ++COUNTER;
		direction = PathEnds.Direction.BOTH;
	}

	public int getLeastCapacity() {
		return getEdgeList().stream().mapToInt(pLink::getSubstrateCapacity).min().getAsInt();
	}

	public boolean checkCapacity(int capacity) {
		return getLeastCapacity() >= capacity;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getSource()).append(" ")
				.append(getSource().getVRouters())
				.append(" ").append(getTarget())
				.append(getTarget().getVRouters())
				.append(" ").append(edgeList);
		return sb.toString();
//		return String.format("%s %d", toOPL(), getLeastCapacity());
	}

	public String toOPL() {
//		return String.format("%s %s %d", getSource().getName(), getTarget().getName(), index);
		return String.format("%s %s %d", getStartVertex().getName(), getEndVertex().getName(), index);
	}

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
//		vertexList.forEach(r -> sb.append(r));
		edgeList.forEach(l -> sb.append(l));
		return sb.toString();
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
		if (direction == PathEnds.Direction.BOTH)
			checkParameters(link);
		getSource().serveRequest(request, link.getSource());
		getTarget().serveRequest(request, link.getTarget());
		return link.getCapacity() * getEdgeList().size();
//			});
	}

	private void checkParameters(vLink link) { //!!!!!!!!!!!!!!!!!!!!!!!!!
	    int first = link.getSource().getPower() + link.getSource().getMemory();
	    int second = link.getTarget().getPower() + link.getTarget().getMemory();
//		boolean reverse = getSource().getNetwork().getLocations().get(getSource().getLocation())
//				< getTarget().getNetwork().getLocations().get(getTarget().getLocation())
//                && first > second;
// 		boolean reverse = PathEndsComparator.count(getSource().getNetwork().getLocations().get(getSource().getLocation()), getSource().getNetwork().getLocations().get(getSource().getLocation()))
//				< PathEndsComparator.count(getTarget().getNetwork().getLocations().get(getTarget().getLocation()), getTarget().getNetwork().getLocations().get(getTarget().getLocation()))
//                && first > second;
//		if (reverse)
//			setDirection(PathEnds.Direction.REVERSE);
	}

	public int releaseRequest(int request) {
		int addedCapacity = 0;
//		getEdgeList().forEach(l -> l.removeRequest(request));
		for (pLink l : getEdgeList()) {
			addedCapacity += l.removeRequest(request);
			if (Heuristic.WEIGHTABLE_LINKS)
			l.updateWeight();
		}
		getStartVertex().removeRequest(request);
		getEndVertex().removeRequest(request);
		return addedCapacity;
	}

	public pRouter getSource() {
		return direction.equals(PathEnds.Direction.SIMPLE) || direction.equals(PathEnds.Direction.BOTH) ? getStartVertex() : getEndVertex();
	}

	public pRouter getTarget() {
		return direction.equals(PathEnds.Direction.SIMPLE) || direction.equals(PathEnds.Direction.BOTH) ? getEndVertex() : getStartVertex();
	}

	public String getDelta() {
		StringBuilder sb = new StringBuilder();
		getEdgeList().forEach(l -> sb.append(l.getName() + " " + toString() + " 1\n"));
		return sb.toString();
	}
}