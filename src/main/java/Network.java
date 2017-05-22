import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.VertexSetListener;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Network extends SimpleWeightedGraph/*<pRouter, pLink>*/ implements ListenableGraph {

	public int getUsedCapacity() {
		return usedCapacity;
	}

	public void setUsedCapacity(int usedCapacity) {
		this.usedCapacity = usedCapacity;
	}

	public void addUsedCapacity(int capacity) {
		usedCapacity += capacity;
	}

	public void removeUsedCapacity(int capacity) {
		usedCapacity -= capacity;
	}

	private int usedCapacity;

    public List<PathEnds> getPaths() {
        return paths;
    }

    public void setPaths(List<PathEnds> paths) {
        this.paths = paths;
    }

	private List<PathEnds> paths;
	private List<Request> requests;

    public Network() {
        super(pLink.class);
        usedCapacity = 0;
    }

    public Network(List<Request> requests) {
        super(pLink.class);
        this.requests = requests;
        usedCapacity = 0;
    }

	public Network(SimpleWeightedGraph<pRouter, pLink> graph, List<Request> requests) {
		super(pLink.class);
//		requests.forEach(r -> Collections.sort(r.getLinks()));
		this.requests = requests;
	}

	public Network update(List<Request> requests) {
//        requests.forEach(r -> Collections.sort(r.getLinks()));
        this.requests = requests;
        return this;
    }

    public Network update() {
//        requests.forEach(r -> Collections.sort(r.getLinks()));
        return this;
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

	public List<Request> getRequests() {
		return requests;
	}

	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}

	public void addRequest(Request request){
		requests.add(request);
	}

	public void serveRequests() {
		new Heuristic(this).solve();
	}

	public List<PathEnds> getActualPaths() {
		setPaths();
		return paths;
	}

//	private void setPaths() {
	public void setPaths() {
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
		Map<String, Map<Integer, vLink>> vLinks = om.readValue(new File(vL), new TypeReference<Map<String, Map<Integer, vRouter>>>(){});
		vRouters.entrySet().forEach(r -> {
			pRouter router = (pRouter) Parser.getRouterByName(this, r.getKey());
			if (router != null)
				router.setVRouters(r.getValue());
		});
		vLinks.entrySet().forEach(l -> {
			pLink link = (pLink) Parser.getLinkByName(this, l.getKey());
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
}
