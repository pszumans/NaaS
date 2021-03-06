import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ilog.concert.IloException;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.VertexSetListener;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
public class Network extends SimpleWeightedGraph/*<PRouter, PLink>*/ implements ListenableGraph {

    private List<Request> requests;
    private List<PathEnds> paths;
    private Solver solver;

    private int usedCapacity;
//    private double solverTime;

    private int fullCapacity;
    private int fullPower;
    private int fullMemory;

    private double capacityLoadRate;
    private double powerLoadRate;
    private double memoryLoadRate;

    private List<Request> servedRequests;
    private Map<Integer, Location> locations;

    private double serviceRate;
    private int reqCount;
    private int servedCount;
    public static boolean IS_HEURISTIC;

    public Network() {
        super(PLink.class);
        requests = new ArrayList<>();
        locations = new HashMap<>();
//        usedCapacity = 0;
    }

    public Network(List<Request> requests) {
        super(PLink.class);
        this.requests = requests;
        locations = new HashMap<>();
//		usedCapacity = 0;
    }

    public void setSolver() {
        if (IS_HEURISTIC)
            solver = new Heuristic(this);
        else
//				null;
            try {
                solver = new SolverOPL(this);
            } catch (IloException e) {
                e.printStackTrace();
            }
    }

    public void setSolver(boolean isHeuristic) {
        if (isHeuristic)
            solver = new Heuristic(this);
        else
//				null;
            try {
                solver = new SolverOPL(this);
            } catch (IloException e) {
                e.printStackTrace();
            }
    }

    public void addUsedCapacity(int capacity) {
        usedCapacity += capacity;
    }

    public void removeUsedCapacity(int capacity) {
        usedCapacity -= capacity;
    }

    public Set<PRouter> getRouters() {
        return /*NaaSGraph.*/vertexSet();
    }

    public Set<PLink> getLinks() {
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

    public void addRequest(Request request) {
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
//        solverTime = solver.getTime();
//        System.out.println("TIME: " + heuristic.solve());
    }

    public boolean serveRequest(Request request) {
        boolean result;
        if (solver == null)
            solver = new Heuristic(this);
        reqCount++;
        if (result = solver.serveRequest(request)) {
            servedCount++;
        }
        serviceRate = (double) servedCount / reqCount;
        return result;
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
        if (paths == null)
            setPaths();
        return paths;
    }

    private void setPaths() {
//	public void setPaths() {
        paths = new ArrayList<>();
        setPaths(Integer.MAX_VALUE);
    }

    private void setPaths(int k) {
        KShortestPaths<PRouter, PLink> shortestPaths;
        try {
            shortestPaths = new KShortestPaths<>(/*NaaSGraph*/this, k);
            List<PRouter> routersList = new ArrayList<>(vertexSet());
            for (int i = 0; i < routersList.size() - 1; i++) {
                for (int j = i + 1; j < routersList.size(); j++) {
                    PRouter r1 = routersList.get(i);
                    PRouter r2 = routersList.get(j);
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
        Map<String, Map<Integer, VRouter>> vRouters = om.readValue(new File(vR), new TypeReference<Map<String, Map<Integer, VRouter>>>() {
        });
        Map<String, Map<Integer, List<VLink>>> vLinks = om.readValue(new File(vL), new TypeReference<Map<String, Map<Integer, VRouter>>>() {
        });
        vRouters.entrySet().forEach(r -> {
            PRouter router = (PRouter) ParserAMPL.getRouterByName(this, r.getKey());
            if (router != null)
                router.setVRouters(r.getValue());
        });
        vLinks.entrySet().forEach(l -> {
            PLink link = (PLink) ParserAMPL.getLinkByName(this, l.getKey());
            if (link != null)
                link.setVLinks(l.getValue());
        });
    }

    public int getMaxMinSubstrateCapacity() {
        return edgeSet().stream().mapToInt(l -> ((PLink) l).getSubstrateCapacity()).min().getAsInt();
    }

    @JsonCreator
    public static Network JsonParser(@JsonProperty("links") List<PLink> links) {
        Network network = new Network();
        links.forEach(l -> {
            PRouter r1 = (PRouter) l.getSource();
            PRouter r2 = (PRouter) l.getTarget();
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
        else {
            locations.put(location, new Location(location, power, memory));
            VRouter.LOC_MAX++;
        }
    }

    public void countUsedCapacity() {
        usedCapacity = getLinks().stream().mapToInt(l -> (l.getCapacity() - l.getSubstrateCapacity())).sum();
    }

    public double getLoadRate() {
		double loadRate = (getCapacityLoadRate() + getPowerLoadRate() + getMemoryLoadRate()) / 3;
		return loadRate;
    }

    public void countLoadRates() {
        countCapacityLoadRate();
        countPowerLoadRate();
        countMemoryLoadRate();
    }

    private void countCapacityLoadRate() {
        if (fullCapacity == 0)
            fullCapacity = edgeSet().stream().mapToInt(l -> ((PLink) l).getCapacity()).sum();
        capacityLoadRate = (double)usedCapacity / fullCapacity;
    }

    private void countPowerLoadRate() {
        if (fullPower == 0)
            fullPower = vertexSet().stream().mapToInt(r -> ((PRouter) r).getPower()).sum();
        int freePower = vertexSet().stream().mapToInt(r -> ((PRouter) r).getSubstratePower()).sum();
        powerLoadRate = 1 - (double)freePower / fullPower;
    }

    private void countMemoryLoadRate() {
        if (fullMemory == 0)
            fullMemory = vertexSet().stream().mapToInt(r -> ((PRouter) r).getMemory()).sum();
        int freeMemory = vertexSet().stream().mapToInt(r -> ((PRouter) r).getSubstrateMemory()).sum();
        memoryLoadRate = 1 - (double)freeMemory / fullMemory;
    }

    @Getter
    @Setter
    public class Location implements Locator, Serializable {
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
    }
}
