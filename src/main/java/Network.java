import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.VertexSetListener;
import org.jgrapht.graph.SimpleWeightedGraph;

@Getter @Setter
public class Network extends SimpleWeightedGraph/*<pRouter, pLink>*/ implements ListenableGraph {

	private List<Request> requests;
//	private Heuristic heuristic;
	private Solver solver;
	private List<PathEnds> paths;

	private int usedCapacity;
	private double solverTime;

	private List<Request> servedRequests;
	private Map<Integer, Location> locations;

	private double serviceRate = 1;
	private int reqCount;
	private int servedCount;
	
	public Network() {
        super(pLink.class);
        requests = new ArrayList<>();
        locations = new HashMap<>();
//        usedCapacity = 0;
    }

    public Network(List<Request> requests) {
        super(pLink.class);
        this.requests = requests;
		locations = new HashMap<>();
//		usedCapacity = 0;
    }

    public void setSolver(boolean isHeuristic) {
		solver = isHeuristic ? new Heuristic(this) :
				null;
//				new SolverOPL(this);
	}

	public void addUsedCapacity(int capacity) {
		usedCapacity += capacity;
	}

	public void removeUsedCapacity(int capacity) {
		usedCapacity -= capacity;
	}

	public Set<pRouter> getRouters() {
		return /*NaaSGraph.*/vertexSet();
	}

	public Set<pLink> getLinks() {
		return /*NaaSGraph.*/edgeSet();
	}

	private SimpleWeightedGraph getGraph(List<? extends Router> routers, List<? extends Link> links) {
		SimpleWeightedGraph graph = new SimpleWeightedGraph<>(links.get(0).getClass());
		routers.forEach(r -> graph.addVertex(r));
		links.forEach(l -> {
			graph.addEdge(l.getSource(),
					l.getTarget(), l);
			graph.setEdgeWeight(l, (double) 1 / l.getCapacity());
		});
		return graph;
	}

	public void addRequest(Request request){
		requests.add(request);
	}

	public void removeRequest(Request request) {
		requests.remove(request);
	}

	public boolean isRequestInService(Request request) {
		return requests.contains(request);
	}

	public void serveRequests() {
		if (solver == null)
			solver = new Heuristic(this);
		solver.solve();
		solverTime = solver.getTime();
//        System.out.println("TIME: " + heuristic.solve());
	}

	public void serveRequest(Request request) {
		if (solver == null)
			solver = new Heuristic(this);
		reqCount++;
		if (solver.serveRequest(request)) {
			servedCount++;
		}
		serviceRate = (double) servedCount / reqCount;
	}

	public void releaseRequest(Request request) {
		if (solver != null)
			solver.releaseRequest(request);
//		requests.remove(request);
	}

	public void addServedRequest(Request request) {
		if (servedRequests == null)
			servedRequests = new ArrayList<>();
//		if (isServed) {
//		requests.remove(request);
//		request.setServed(true);
		servedRequests.add(request);
//		}
	}

	public void addServedRequest(int req) {
		if (servedRequests == null)
			servedRequests = new ArrayList<>();
		Request request = requests.remove(req);
		request.setServed(true);
		servedRequests.add(request);
	}

	public void addRequestService(RequestService rs) {
		if (rs.isInput())
			requests.add(rs.getRequest());
		else
			solver.releaseRequest(rs.getRequest());

	}

	public List<PathEnds> getActualPaths() {
		setPaths();
		return paths;
	}

	private void setPaths() {
//	public void setPaths() {
		paths = new ArrayList<>();
		setPaths(Integer.MAX_VALUE);
	}

	private void setPaths(int k) {
        KShortestPaths<pRouter, pLink> shortestPaths;
        try {
            shortestPaths = new KShortestPaths<>(/*NaaSGraph*/this, k);
            List<pRouter> routersList = new ArrayList<>(vertexSet());
            for (int i = 0; i < routersList.size() - 1; i++) {
                for (int j = i + 1; j < routersList.size(); j++) {
                    pRouter r1 = routersList.get(i);
                    pRouter r2 = routersList.get(j);
                    PathEnds key = new PathEnds(r1, r2);
                    key.setPaths(shortestPaths.getPaths(r1, r2)
							.stream()
							.map(p -> new Path(p.getGraph(), p.getStartVertex(), p.getEndVertex(), p.getVertexList(), p.getEdgeList(), p.getWeight()))
							.collect(Collectors.toList()));
					paths.add(key);
                    Path.resetCounter();
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
	}

	public void parseServedRequests(String vR, String vL) throws IOException {
		ObjectMapper om = new ObjectMapper();
		Map<String, Map<Integer, vRouter>> vRouters = om.readValue(new File(vR), new TypeReference<Map<String, Map<Integer, vRouter>>>(){});
		Map<String, Map<Integer, List<vLink>>> vLinks = om.readValue(new File(vL), new TypeReference<Map<String, Map<Integer, vRouter>>>(){});
		vRouters.entrySet().forEach(r -> {
			pRouter router = (pRouter) ParserAMPL.getRouterByName(this, r.getKey());
			if (router != null)
				router.setVRouters(r.getValue());
		});
		vLinks.entrySet().forEach(l -> {
			pLink link = (pLink) ParserAMPL.getLinkByName(this, l.getKey());
			if (link != null)
				link.setVLinks(l.getValue());
		});
	}

	public int getMaxSubstrateCapacity() {
		return edgeSet().stream().mapToInt(l -> ((pLink)l).getSubstrateCapacity()).min().getAsInt();
	}

	@JsonCreator
	public static Network JsonParser(@JsonProperty("links") List<pLink> links) {
		Network network = new Network();
		links.forEach(l -> {
			pRouter r1 = (pRouter) l.getSource();
			pRouter r2 = (pRouter) l.getTarget();
			network.addVertex(r1);
			network.addVertex(r2);
			network.addEdge(r1, r2, l);
		});
		return network;
	}

    @Override
    public void addGraphListener(GraphListener graphListener) {

    }

    @Override
    public void addVertexSetListener(VertexSetListener vertexSetListener) {

    }

    @Override
    public void removeGraphListener(GraphListener graphListener) {

    }

    @Override
    public void removeVertexSetListener(VertexSetListener vertexSetListener) {

    }

	public void addLocation(int location, int power, int memory) {
		if (locations.containsKey(location))
			locations.get(location).addValues(power, memory);
		else
			locations.put(location, new Location(location, power, memory));
	}

	@Getter @Setter
	public class Location implements Locable {
		private final int index;
		private int power;
		private int memory;

		private int substratePower;
		private int substrateMemory;

		public Location(int index, int power, int memory) {
			this.index = index;
			this.power = substratePower = power;
			this.memory = substrateMemory = memory;
		}

		public void update(int powerToAdd, int memoryToAdd) {
			substratePower += powerToAdd;
			substrateMemory += memoryToAdd;
		}

		public void downdate(int powerToRemove, int memoryToRemove) {
			substratePower -= powerToRemove;
			substrateMemory -= memoryToRemove;
		}

		public void addValues(int power, int memory) {
			this.power = substratePower += power;
			this.memory = substrateMemory += memory;
		}

		@Override
		public String toString() {
			return String.format("%d %d/%d %d/%d", index, substratePower, power, substrateMemory, memory);
		}

		//			this.substrateMemory = substrateMemory;
		//		public void setSubstrateMemory(int substrateMemory) {
		//
		//		}
		//			return substrateMemory;
		//		public int getSubstrateMemory() {
		//
		//		}
		//			this.substratePower = substratePower;
		//		public void setSubstratePower(int substratePower) {
		//
		//		}
		//			return substratePower;
		//		public int getSubstratePower() {
		//
		//		}
		//			this.memory = memory;
		//		public void setMemory(int memory) {
		//
		//		}
		//			return memory;
		//		public int getMemory() {
		//
		//		}
		//			this.power = power;
		//		public void setPower(int power) {
		//
		//		}
		//			return power;
		//		public int getPower() {
		//
		//		}
		//			this.index = index;
		//		public void setIndex(int index) {
		//
		//		}
		//			return index;
//		public int getIndex() {

//		}
	}
}
