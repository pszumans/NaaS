import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Szuman on 27.03.2017.
 */
public class Heuristic {

    private final Network network;
    private List<PathEnds> paths;
    private Map<Integer, List<Path>> pathsAllocated;

    public Heuristic(Network network) {
        this.network = network;
    }

    public void solve() {
        serveRequests();
    }

    private void serveRequests() {
//        Collections.sort(network.getRequests(), (r1, r2) -> r2.getLinks().stream().mapToInt(Link::getCapacity).sum() - r1.getLinks().stream().mapToInt(Link::getCapacity).sum());
        network.getRequests().forEach(r -> {
            boolean boo;// = serveRequest(r);
                r.setServed(boo = serveRequest(r));
                System.out.println("				STATUS R = " + r + " : " + boo);
        });
    }

    private boolean serveRequest(Request request) {
        if (pathsAllocated == null)
            pathsAllocated = new LinkedHashMap<>();
        paths = network.getActualPaths();
        Map<vRouter, pRouter> routersAllocation = new LinkedHashMap<>();
        List<vLink> linksAllocated = new ArrayList<>();
        pathsAllocated.put(request.getIndex(), new ArrayList<>());
//        List<Path> chosenPaths = new ArrayList<>();
        for (vLink link : request.getLinks()) {
//            System.out.println(link);
//            Path chosenPath = chooseBestPath(routersAllocation, chosenPaths, link);
            Path chosenPath = chooseBestPath(routersAllocation, pathsAllocated.get(request.getIndex()), link);
            if (chosenPath == null) {
//                releaseRequest(chosenPaths, request.getIndex());
                releaseRequest(request.getIndex());
                return false;
            }
//            chosenPaths.add(chosenPath);
            pathsAllocated.get(request.getIndex()).add(chosenPath);
            network.addUsedCapacity(chosenPath.serveRequest(request.getIndex(), link));
            linksAllocated.add(link);
//            System.out.println(chosenPath);
            updateWeights(chosenPath);
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
        }
        return true;
    }

    private void updateWeights(Path chosenPath) {
        chosenPath.getEdgeList().forEach(l -> l.updateWeight());
    }

//    private void releaseRequest(List<Path> paths, int request) {
//        paths.forEach(p -> network.removeUsedCapacity(p.releaseRequest(request)));
//    }

    private void releaseRequest(int request) {
        pathsAllocated.get(request).forEach(p -> network.removeUsedCapacity(p.releaseRequest(request)));
    }

    private Path chooseBestPath(Map<vRouter, pRouter> routersAllocation, List<Path> chosenPaths, vLink link) {
        List<PathEnds> properPaths = getProperPaths(link);
//        System.out.println("getProperPaths");
//        properPaths.forEach(p -> System.out.println(p.getPaths()));
        if (!chosenPaths.isEmpty())
            properPaths = excludeChosenPaths(properPaths, chosenPaths);
//        System.out.println("excludeChosenPaths");
//        properPaths.forEach(p -> System.out.println(p.getPaths()));
        if (properPaths.isEmpty())
            return null;
        if (!routersAllocation.isEmpty())
            properPaths = chooseFromAllocatedRouters(properPaths, routersAllocation, link);
//        System.out.println("chooseFromAllocatedRouters");
//        properPaths.forEach(p -> System.out.println(p.getPaths()));
        PathEnds selectedPathEnds = chooseBestPathEnds(properPaths);
        if (selectedPathEnds == null)
            return null;
//        System.out.println("MAPPATH: " + properPaths);
        Path bestPath = selectBestPath(selectedPathEnds);
//        System.out.println("CHOSEN_PATH: " + bestPath);
        return bestPath;
    }

    private Path selectBestPath(PathEnds pathEnds) {
        return pathEnds.getPaths().stream()
                .min(
                        (p1, p2) -> p1.getWeight() > p2.getWeight() ? 1
                                : p1.getWeight() < p2.getWeight() ? -1
                                : 0
                )
                .get();
    }

    private PathEnds chooseBestPathEnds(List<PathEnds> properPaths) {
        return properPaths.stream()
                .max(
                        (pair1, pair2) ->
                                pair2.getFirst().compareTo(pair1.getFirst()) + pair2.getSecond().compareTo(pair1.getSecond())
                ).orElse(null);
    }

    private List<PathEnds> chooseFromAllocatedRouters(List<PathEnds> properPaths, Map<vRouter, pRouter> routersAllocation, vLink link) {
        List<PathEnds> filteredPaths = properPaths
                .stream()
                .filter(p -> isRouterAllocated(link, routersAllocation, p))
                .collect(Collectors.toList());
        return (!filteredPaths.isEmpty()) ? filteredPaths : properPaths;
    }

    private List<PathEnds> excludeChosenPaths(List<PathEnds> properPaths, List<Path> chosenPaths) {
        return properPaths
                .stream()
                .filter(p -> !isPathUsed(p, chosenPaths))
                .collect(Collectors.toList());
    }

    private boolean isPathUsed(PathEnds routers, List<Path> chosenPaths) {
        return chosenPaths.stream()
                .anyMatch(p -> routers.getFirst().equals(p.getStartVertex()) && routers.getSecond().equals(p.getEndVertex())
                        || routers.getFirst().equals(p.getEndVertex()) && routers.getSecond().equals(p.getStartVertex()));
    }

    private boolean isRouterAllocated(vLink link, Map<vRouter, pRouter> routersAllocation, PathEnds pathEnds) {
        Map<vRouter, pRouter> properRoutersAllocation
                = routersAllocation.entrySet().stream()
                .filter(e -> link.containsRouter(e.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        if (properRoutersAllocation.isEmpty())
            return false;
        pathEnds.setCapability(link, properRoutersAllocation);
        return /*list.isEmpty() ? false : */properRoutersAllocation.values().stream().anyMatch(r -> pathEnds.hasElement(r));
    }

    private List<PathEnds> getProperPaths(vLink link) {
//		return
        List<PathEnds> properPaths =
                paths.stream().filter(pathEnds ->
                        checkParameters(pathEnds, link) && pathEnds.getPaths().stream().anyMatch(p -> p.checkCapacity(link.getCapacity()))).collect(Collectors.toList());
        properPaths.forEach(k -> k.filterPaths(p -> p.checkCapacity(link.getCapacity())));
//        paths.forEach(p -> System.out.println(p.getPaths()));
        return properPaths;
    }

    private boolean checkParameters(PathEnds routers, vLink link) {
        boolean simple = routers.getFirst().checkParameters((link.getSource()))
                && routers.getSecond().checkParameters((link.getTarget()));
        boolean reverse = routers.getFirst().checkParameters(link.getTarget())
                && routers.getSecond().checkParameters(link.getSource());
        if (simple && !reverse)
            routers.setCapability(PathEnds.Capability.SIMPLE);
        else if (!simple && reverse)
            routers.setCapability(PathEnds.Capability.REVERSE);
        return simple || reverse;
//		System.out.println("		CHECK");
//		System.out.println(String.format("%s -> %s : %b", routers.getSource(), link.getRouter(0), routers.getSource().checkParameters(((vRouter) link.getRouter(0)))));
//		System.out.println(String.format("%s -> %s : %b", routers.getTarget(), link.getRouter(1), routers.getTarget().checkParameters(((vRouter) link.getRouter(1)))));
//		System.out.println(String.format("%s -> %s : %b", routers.getSource(), link.getRouter(1), routers.getSource().checkParameters(((vRouter) link.getRouter(1)))));
//		System.out.println(String.format("%s -> %s : %b", routers.getTarget(), link.getRouter(0), routers.getTarget().checkParameters(((vRouter) link.getRouter(0)))));
//		return (routers.getSource().checkParameters(((vRouter) link.getRouter(0)))
//				&& routers.getTarget().checkParameters(((vRouter) link.getRouter(1))))
//				|| (routers.getSource().checkParameters((vRouter) link.getRouter(1))
//				&& routers.getTarget().checkParameters((vRouter) link.getRouter(0)));
    }

}
