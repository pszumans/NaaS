import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Szuman on 27.03.2017.
 */
public class Heuristic {

    public static final boolean WEIGHTABLE_LINKS = false;
    private static final boolean RESTORABLE_PATHS = true;
    public static final boolean RESTORABLE_LOCATION = true;


    private final Network network;
    private List<PathEnds> paths;
    private Map<Integer, List<Path>> pathsAllocated;

    public Heuristic(Network network) {
        this.network = network;
    }

    public long solve() {
        long start = System.nanoTime();
        serveRequests();
        long end = System.nanoTime();
        return (end - start) / 1000000;
    }

    private void serveRequests() {
//        Collections.sort(network.getRequests(), (r1, r2) -> r2.getLinks().stream().mapToInt(Link::getCapacity).sum() - r1.getLinks().stream().mapToInt(Link::getCapacity).sum());
        network.getRequests().forEach(r -> {
            boolean boo;// = serveRequest(r);
                r.setServed(boo = serveRequest(r));
                System.out.println("				STATUS R = " + r + " : " + boo);
        });
        System.out.println(pathsAllocated);
    }

    private boolean serveRequest(Request request) {
        if (pathsAllocated == null)
            pathsAllocated = new LinkedHashMap<>();
        if (paths == null || RESTORABLE_PATHS)
        paths = network.getActualPaths();
        Map<vRouter, pRouter> routersAllocation = new LinkedHashMap<>();
        List<vLink> linksAllocated = new ArrayList<>();
        if (pathsAllocated.get(request.getIndex()) == null)
        pathsAllocated.put(request.getIndex(), new ArrayList<>());
//        List<Path> chosenPaths = new ArrayList<>();
        for (vLink link : request.getLinks()) {
//            System.out.println(link);
//            Path chosenPath = chooseBestPath(routersAllocation, chosenPaths, link);
            Path chosenPath = chooseBestPath(routersAllocation, pathsAllocated.get(request.getIndex()), link);
            if (chosenPath == null) {
//                releaseRequest(chosenPaths, request.getIndex());
                releaseRequest(request.getIndex());
//                new GraphVisualisation(network).start();
//                new Scanner(System.in).nextLine();
                return serveRequest(request);
//                return false;
            }
//            chosenPaths.add(chosenPath);
            pathsAllocated.get(request.getIndex()).add(chosenPath);
            network.addUsedCapacity(chosenPath.serveRequest(request.getIndex(), link));
            linksAllocated.add(link);
//            System.out.println(chosenPath);
            updateWeights(chosenPath); // aktualizuj wagę
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
            System.out.println(link + "-> ROUTERS ALLO: " + routersAllocation);
        }
        return true;
    }

    private void updateWeights(Path chosenPath) {
        if (WEIGHTABLE_LINKS)
        chosenPath.getEdgeList().forEach(l -> l.updateWeight());
    }

//    private void releaseRequest(List<Path> paths, int request) {
//        paths.forEach(p -> network.removeUsedCapacity(p.releaseRequest(request)));
//    }

    private void releaseRequest(int request) {
        pathsAllocated.get(request).forEach(p -> network.removeUsedCapacity(p.releaseRequest(request)));
    }

    private Path chooseBestPath(Map<vRouter, pRouter> routersAllocation, List<Path> chosenPaths, vLink link) {
        List<PathEnds> properPaths;// = paths;// = getProperPaths(link);
        if (!routersAllocation.isEmpty()) {
//            properPaths = chooseFromAllocatedRouters(paths, routersAllocation, link);
            properPaths = getProperPaths(chooseFromAllocatedRouters(paths, routersAllocation, link), link, routersAllocation.values());
        } else
            properPaths = getProperPaths(link);
//        System.out.println("getProperPaths");
//        properPaths.forEach(p -> System.out.println(p.getPaths()));
        if (!chosenPaths.isEmpty())
            properPaths = excludeChosenPaths(properPaths, chosenPaths);
//        System.out.println("excludeChosenPaths");
//        properPaths.forEach(p -> System.out.println(p.getPaths()));
        if (properPaths.isEmpty())
            return null;


//        if (!routersAllocation.isEmpty())
//            properPaths = chooseFromAllocatedRouters(properPaths, routersAllocation, link);


        //        System.out.println("chooseFromAllocatedRouters");
//        properPaths.forEach(p -> System.out.println(p.getPaths()));


//        PathEnds selectedPathEnds = chooseBestPathEnds(properPaths);
//        System.out.println(selectedPathEnds);
//        if (selectedPathEnds == null)
//            return null;
//        System.out.println("MAPPATH: " + properPaths);


//        Path bestPath = selectBestPath(selectedPathEnds);
        Path bestPath = getBestPath(properPaths);

        List<Path> pathEnds = properPaths.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList());
        Collections.sort(pathEnds, new PathsComparator());
//        System.out.println("properPaths: " + properPaths);
//        properPaths.forEach(p -> {System.out.println("PATH: " + p + " -> " + (Network.locations.get(p.getFirst().getLocation()) + Network.locations.get(p.getSecond().getLocation())) + " -> ");
//            p.getPaths().forEach(pp -> System.out.print(pp.getSource().getName() + " " + pp.getTarget().getName() + " W: " + pp.getWeight() + "; "));});
//        System.out.println();
        pathEnds.forEach(p -> System.out.print(p + " W: " + p.getWeight() + " ; "));
        System.out.println();
//        System.out.println("#########sortedPaths: " + pathEnds);
        int sum = Network.locations.get(bestPath.getSource().getLocation()) + Network.locations.get(bestPath.getTarget().getLocation());
        System.out.println(bestPath + " weight: " + bestPath.getWeight() + " location: " + sum);
        System.out.println(Network.locations);
//        System.out.println("CHOSEN_PATH: " + bestPath);
        return bestPath;
    }

    private Path getBestPath(List<PathEnds> pathEnds) {
//        if (WEIGHTABLE_LINKS)
            return pathEnds.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList()).stream().min(new PathsComparator()).get();
//        return pathEnds.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList()).stream().min(
//                new PathsComparatorUpdate()).get();
    }

    private Path selectBestPath(PathEnds pathEnds) {
        if (WEIGHTABLE_LINKS)
        return pathEnds.getPaths().stream()
                .min(
                        new PathsComparator()
                )
                .get();
        return pathEnds.getPaths().stream().min(
            new PathsComparatorUpdate()).get();
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

    private List<PathEnds> getProperPaths(List<PathEnds> properPaths, vLink link, Collection<pRouter> routersAllocation) {
//		return
        properPaths =
                properPaths.stream().filter(pathEnds ->
                        checkParameters(pathEnds, link, routersAllocation) && pathEnds.getPaths().stream().anyMatch(p -> p.checkCapacity(link.getCapacity()))).collect(Collectors.toList());
        properPaths.forEach(k -> k.filterPaths(p -> p.checkCapacity(link.getCapacity())));
//        paths.forEach(p -> System.out.println(p.getPaths()));
        return properPaths;
    }

    private boolean checkParameters(PathEnds routers, vLink link, Collection<pRouter> routersAllocation) {
        boolean simple = (routersAllocation.contains(routers.getFirst()) ||
                routers.getFirst().checkParameters((link.getSource())))
                && (routersAllocation.contains(routers.getSecond()) ||
                routers.getSecond().checkParameters((link.getTarget())));
        boolean reverse = (routersAllocation.contains(routers.getFirst()) ||
                routers.getFirst().checkParameters(link.getTarget()))
                && (routersAllocation.contains(routers.getSecond()) ||
                routers.getSecond().checkParameters(link.getSource()));
        if (simple && !reverse)
            routers.setCapability(PathEnds.Capability.SIMPLE);
        else if (!simple && reverse)
            routers.setCapability(PathEnds.Capability.REVERSE);
        return simple || reverse;
    }

    private boolean checkParameters(PathEnds routers, vLink link) {
        boolean simple = routers.getFirst().checkParameters((link.getSource()))
                && routers.getSecond().checkParameters((link.getTarget()));
        boolean reverse = routers.getFirst().checkParameters(link.getTarget())
                && routers.getSecond().checkParameters(link.getSource());
        if (simple && !reverse) {
            routers.setCapability(PathEnds.Capability.SIMPLE);
        }
        else if (!simple && reverse) {
            routers.setCapability(PathEnds.Capability.REVERSE);
        }
        if (link.getSource().getName().equals("V7") || link.getTarget().getName().equals("V7"))
            System.out.println();
        return simple || reverse;
    }

}
