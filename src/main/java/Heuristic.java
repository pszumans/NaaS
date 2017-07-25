import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Szuman on 27.03.2017.
 */
@Getter @Setter
public class Heuristic extends Solver {

    public static final boolean WEIGHTABLE_LINKS = false;
//    private static final boolean RESTORABLE_PATHS = false;
    public static final boolean RESTORABLE_LOCATION = true;

    private double time;

//    private final Network network;
//    private List<Request> requests;

    private List<PathEnds> paths;
    private Map<Integer, List<Path>> pathsAllocated;
    private Map<vRouter, pRouter> routersAllocation;

    public Heuristic(Network network) {
        super(network);
//        this.network = network;
    }

    @Override
    public void solve() {
        long start = System.nanoTime();
        serveRequests();
        long end = System.nanoTime();
        time = (double) (end - start) / 1000000000;
    }

    private void serveRequests() {
        if (paths == null)
            paths = network.getActualPaths();
        if (pathsAllocated == null)
            pathsAllocated = new LinkedHashMap<>();
        int size = network.getRequests().size();
        int pointer = 0;
        for (int i = 0; i < size; i++) {
//            if (!
                    serveRequest(network.getRequests().get(i))
//                    )
//                pointer++
                  ;
        }
//        System.out.println(pathsAllocated);
//        System.out.println(network.getRequests());
//        System.out.println(network.getServedRequests());
    }

//    private boolean serveRequest(Request request) {
    @Override
    public boolean serveRequest(Request request) {

//        if (paths == null || RESTORABLE_PATHS)
//            paths = network.getActualPaths();

        List<vLink> links = new ArrayList<>(request.getLinks());
        Allocation all1 = allocateLinks(request.getIndex(), links);
        Collections.reverse(links);
        Allocation all2 = allocateLinks(request.getIndex(), links);

        if (all1 == null && all2 == null)
            return false;

        if ((all1 == null && all2 != null) || all1.getUsedCapacity() > ((all2 != null) ? all2.getUsedCapacity() : Integer.MAX_VALUE)) {
            network.addUsedCapacity(all2.serve());
        } else {
            network.addUsedCapacity(all1.serve());
        }

//        System.out.println(paths.size() + "PATHS: " + paths);
//        network.addServedRequest(request);
        return super.serveRequest(request);
//        return true;
    }

    private Allocation allocateLinks(int reqIndex, List<vLink> links) {
        if (paths == null)
            paths = network.getActualPaths();
        if (pathsAllocated == null)
            pathsAllocated = new LinkedHashMap<>();
        Allocation allocation = new Allocation(reqIndex);
        routersAllocation = new LinkedHashMap<>();
        if (pathsAllocated.get(reqIndex) == null)
            pathsAllocated.put(reqIndex, new ArrayList<>());
        for (vLink link : links) {
            Path chosenPath = chooseBestPath(pathsAllocated.get(reqIndex), link);
            if (chosenPath == null) {
                releaseRequest(reqIndex);
                if (links.get(0).equals(link))
                    return null;
                return allocateLinks(reqIndex, links);
            }
            pathsAllocated.get(reqIndex).add(allocation.allocate(chosenPath, link));
//            updateWeights(chosenPath); // aktualizuj wagę
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
//            System.out.println(link + "-> ROUTERS ALLO: " + routersAllocation);
        }
        return allocation.release();
    }

    private void updateWeights(Path chosenPath) {
        if (WEIGHTABLE_LINKS)
        chosenPath.getEdgeList().forEach(l -> l.updateWeight());
    }

    private void releaseRequest(int request) {
        pathsAllocated.get(request).forEach(p -> p.releaseRequest(request));
    }

    @Override
    public void releaseRequest(Request request) {
//        if (pathsAllocated.containsKey(request.getIndex()))
            pathsAllocated.get(request.getIndex()).forEach(p -> network.removeUsedCapacity(p.releaseRequest(request.getIndex())));
            super.releaseRequest(request);
    }

    private Path chooseBestPath(List<Path> chosenPaths, vLink link) {
        List<PathEnds> properPaths = paths.stream().map(PathEnds::new).collect(Collectors.toList());// = getProperPaths(link);

        properPaths = getProperPaths(excludeChosenPaths(properPaths, chosenPaths), link);

        if (properPaths.isEmpty())
            return null;

        Path bestPath = chooseBestPath(properPaths);

//        System.out.println(network.getLocations());
        return bestPath;
    }

    private Path chooseBestPath(List<PathEnds> pathEnds) {
//        if (WEIGHTABLE_LINKS)
            return pathEnds.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList()).stream().min(new PathsComparator(network)).get();
//        return pathEnds.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList()).stream().min(
//                new PathsComparatorUpdate()).get();
    }

    private List<PathEnds> chooseFromAllocatedRouters(List<PathEnds> properPaths, vLink link) {
        List<PathEnds> filteredPaths = properPaths
                .stream()
                .filter(p -> isRouterAllocated(link, p))
                .collect(Collectors.toList());
        return (!filteredPaths.isEmpty()) ? filteredPaths : properPaths;
    }

    private List<PathEnds> excludeChosenPaths(List<PathEnds> properPaths, List<Path> chosenPaths) {
        properPaths.removeAll(chosenPaths.stream().map(p -> getPathEndsByPath(p)).collect(Collectors.toList()));
        // !!!!!!!!!!!!!!!!!!!!!

        return properPaths;
//                .stream()
//                .filter(p -> !isPathUsed(p, chosenPaths))
//                .collect(Collectors.toList());
    }

    private boolean isPathUsed(PathEnds routers, List<Path> chosenPaths) {
        return chosenPaths.stream()
                .anyMatch(p -> routers.getFirst().equals(p.getStartVertex()) && routers.getSecond().equals(p.getEndVertex())
                        || routers.getFirst().equals(p.getEndVertex()) && routers.getSecond().equals(p.getStartVertex()));
    }

    private boolean isRouterAllocated(vLink link, PathEnds pathEnds) {
        Map<vRouter, pRouter> properRoutersAllocation
                = routersAllocation.entrySet().stream()
                .filter(e -> link.containsRouter(e.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        if (properRoutersAllocation.isEmpty())
            return false;
//        !!!!!
//        pathEnds.setDirection(link, properRoutersAllocation);
//        System.out.println(properRoutersAllocation.values().stream().anyMatch(r -> pathEnds.hasElement(r)));
        return /*list.isEmpty() ? false : */properRoutersAllocation.values().stream().anyMatch(r -> pathEnds.hasElement(r));
    }

    private List<PathEnds> getProperPaths(vLink link) {
//		return
        List<PathEnds> properPaths =
                paths.stream().filter(pathEnds ->
                        checkParameters(pathEnds, link) && pathEnds.getPaths().stream().anyMatch(p -> p.checkCapacity(link.getCapacity()))).collect(Collectors.toList());
        properPaths.forEach(k -> k.filterPaths(p -> p.checkCapacity(link.getCapacity())));
//        paths.forEach(p -> System.out.println(p.getPaths()));
        System.out.println(properPaths);
        return properPaths;
    }

    private List<PathEnds> getProperPaths(List<PathEnds> properPaths, vLink link) {
//		return
        properPaths =
                properPaths.stream().filter(pathEnds ->
                        checkParameters(pathEnds, link) && pathEnds.getPaths().stream().anyMatch(p -> p.checkCapacity(link.getCapacity()))).collect(Collectors.toList());
        properPaths.forEach(k -> k.filterPaths(p -> p.checkCapacity(link.getCapacity())));
//        paths.forEach(p -> System.out.println(p.getPaths()));
        return properPaths;
    }

    private boolean checkParameters(PathEnds routers, vLink link) {
        routers.restoreDirection();
        boolean simpleFirst = routersAllocation.containsKey(link.getSource()) ? routersAllocation.get(link.getSource()).equals(routers.getFirst()) : false;
        boolean simpleSecond = routersAllocation.containsKey(link.getTarget()) ? routersAllocation.get(link.getTarget()).equals(routers.getSecond()) : false;
        boolean reverseFirst = routersAllocation.containsKey(link.getTarget()) ? routersAllocation.get(link.getTarget()).equals(routers.getFirst()) : false;
        boolean reverseSecond = routersAllocation.containsKey(link.getSource()) ? routersAllocation.get(link.getSource()).equals(routers.getSecond()) : false;
        boolean simple = false;
        boolean reverse = false;
//        if (simpleFirst && simpleSecond)
//            simple = true;
//        else
        if (!reverseFirst && !reverseSecond)
            simple = (simpleFirst || routers.getFirst().checkParameters((link.getSource())))
                    && (simpleSecond || routers.getSecond().checkParameters((link.getTarget())));
//        if (reverseFirst && reverseFirst)
//            reverse = true;
//        else
        if (!simpleFirst && !simpleSecond)
            reverse = (reverseFirst || routers.getFirst().checkParameters(link.getTarget()))
                    && (reverseSecond || routers.getSecond().checkParameters(link.getSource()));
        if (simple && !reverse)
            routers.setDirection(PathEnds.Direction.SIMPLE);
        else if (!simple && reverse)
            routers.setDirection(PathEnds.Direction.REVERSE);
        boolean result = simple || reverse;
        if (!routersAllocation.isEmpty()) {
            if (routersAllocation.containsKey(link.getSource()) && routersAllocation.containsKey(link.getTarget()))
                return (simple && simpleFirst && simpleSecond) || (reverse && reverseFirst && reverseSecond);
            result = result && (simpleFirst || simpleSecond || reverseFirst || reverseSecond);
        }
        return result;
    }

    private PathEnds getPathEndsByPath(Path path) {
        return paths.stream().filter(pathEnds -> pathEnds.getPaths().contains(path)).findAny().get();
    }
}
