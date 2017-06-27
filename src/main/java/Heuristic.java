import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Szuman on 27.03.2017.
 */
public class Heuristic {

    public static final boolean WEIGHTABLE_LINKS = false;
    private static final boolean RESTORABLE_PATHS = false;
    public static final boolean RESTORABLE_LOCATION = true;

    public static long TIME;

    private final Network network;
    private List<PathEnds> paths;
    private Map<Integer, List<Path>> pathsAllocated;
    private Map<vRouter, pRouter> routersAllocation;

    public Heuristic(Network network) {
        this.network = network;
    }

    public long solve() {
        long start = System.nanoTime();
        serveRequests();
        long end = System.nanoTime();
        TIME = (end - start) / 1000000;
        return TIME;
    }

    private void serveRequests() {
        pathsAllocated = new LinkedHashMap<>();
        network.getRequests().forEach(r -> {
            boolean boo;// = serveRequest(r);
//            if (r.getIndex() != 7) {
                r.setServed(boo = serveRequest(r));
                System.out.println("				STATUS R = " + r + " : " + boo);
//            }
        });
        System.out.println(pathsAllocated);
    }

    private boolean serveRequest1(Request request) {

        int temp = 0;
        int sum1 = 0;
        int sum2 = 0;

        if (pathsAllocated == null)
            pathsAllocated = new LinkedHashMap<>();
        if (paths == null || RESTORABLE_PATHS)
        paths = network.getActualPaths();
        routersAllocation = new LinkedHashMap<>();
        if (pathsAllocated.get(request.getIndex()) == null)
        pathsAllocated.put(request.getIndex(), new ArrayList<>());

        for (vLink link : request.getLinks()) {
            Path chosenPath = chooseBestPath(pathsAllocated.get(request.getIndex()), link);
            if (chosenPath == null) {
                releaseRequest(request.getIndex());
                return serveRequest(request);
//                return false;
            }
            pathsAllocated.get(request.getIndex()).add(chosenPath);
            temp = chosenPath.serveRequest(request.getIndex(), link);
            sum1 += temp;
            network.addUsedCapacity(temp);
            updateWeights(chosenPath); // aktualizuj wagę
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
            System.out.println(link + "-> ROUTERS ALLO: " + routersAllocation);
        }
/*
        List<Path> tempPaths = pathsAllocated.get(request.getIndex());
        releaseRequest(request.getIndex());
        pathsAllocated.put(request.getIndex(), new ArrayList<>());
        routersAllocation = new LinkedHashMap<>();
        List<vLink> links = new ArrayList<>(request.getLinks());
        Collections.reverse(links);
        for (vLink link : links) {
            Path chosenPath = chooseBestPath(pathsAllocated.get(request.getIndex()), link);
            if (chosenPath == null) {
                releaseRequest(request.getIndex());
                return serveRequest(request);
//                return false;
            }
            pathsAllocated.get(request.getIndex()).add(chosenPath);
            temp = chosenPath.serveRequest(request.getIndex(), link);
            sum2 += temp;
            network.addUsedCapacity(temp);
            updateWeights(chosenPath); // aktualizuj wagę
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
            System.out.println(link + "-> ROUTERS ALLO: " + routersAllocation);
        }
        if (sum1 < sum2) {
            releaseRequest(request.getIndex());
            pathsAllocated.put(request.getIndex(), tempPaths);
            int i = 0;
            for (vLink link : request.getLinks()) {
                network.addUsedCapacity(tempPaths.get(i++).serveRequest(request.getIndex(), link));
            }
        }
*/
        System.out.println("PATHS: " + paths);
        return true;
    }

    private boolean serveRequest(Request request) {

        if (paths == null || RESTORABLE_PATHS)
            paths = network.getActualPaths();

        List<vLink> links = new ArrayList<>(request.getLinks());
        Allocation all1 = allocateLinks(request.getIndex(), links);
        Collections.reverse(links);
        System.out.println("     ALL2");
        Allocation all2 = allocateLinks(request.getIndex(), links);

        if (all1.getUsedCapacity() > all2.getUsedCapacity()) {
            network.addUsedCapacity(all2.serve());
        } else {
            network.addUsedCapacity(all1.serve());
        }

        System.out.println("PATHS: " + paths);
        return true;
    }

    private Allocation allocateLinks(int reqIndex, List<vLink> links) {
        Allocation allocation = new Allocation(reqIndex);
        routersAllocation = new LinkedHashMap<>();
        if (pathsAllocated.get(reqIndex) == null)
            pathsAllocated.put(reqIndex, new ArrayList<>());
        for (vLink link : links) {
            Path chosenPath = chooseBestPath(pathsAllocated.get(reqIndex), link);
            if (chosenPath == null) {
                releaseRequest(reqIndex);
                return allocateLinks(reqIndex, links); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//                return false;
            }
            pathsAllocated.get(reqIndex).add(allocation.allocate(chosenPath, link));
//            updateWeights(chosenPath); // aktualizuj wagę
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
            System.out.println(link + "-> ROUTERS ALLO: " + routersAllocation);
        }
        return allocation.release();
    }

    private void updateWeights(Path chosenPath) {
        if (WEIGHTABLE_LINKS)
        chosenPath.getEdgeList().forEach(l -> l.updateWeight());
    }

//    private void releaseRequest(List<Path> paths, int request) {
//        paths.forEach(p -> network.removeUsedCapacity(p.releaseRequest(request)));
//    }

    private void releaseRequest(int request) {
//        pathsAllocated.get(request).forEach(p -> network.removeUsedCapacity(p.releaseRequest(request)));
        pathsAllocated.get(request).forEach(p -> p.releaseRequest(request));
    }

    private Path chooseBestPath(List<Path> chosenPaths, vLink link) {
        List<PathEnds> properPaths = paths.stream().map(PathEnds::new).collect(Collectors.toList());// = getProperPaths(link);

//        if (link.getSource().getName().equals("V16")) {
//            System.out.println("1. PROPER_PATHS");
//            properPaths.forEach(pathEnds -> {
//                int sum = Network.locations.get(pathEnds.getFirst().getLocation()) + Network.locations.get(pathEnds.getSecond().getLocation());
//                System.out.print(sum + ": ");
//                pathEnds.getPaths().forEach(p -> System.out.print(p + ", "));
//                System.out.println();
//            });
//        }



//        if (!chosenPaths.isEmpty()) {
//            properPaths = excludeChosenPaths(properPaths, chosenPaths);
//        }


//        if (link.getSource().getName().equals("V16")) {
//            System.out.println("2. PROPER_PATHS");
//            properPaths.forEach(pathEnds -> {
//                int sum = Network.locations.get(pathEnds.getFirst().getLocation()) + Network.locations.get(pathEnds.getSecond().getLocation());
//                System.out.print(sum + ": ");
//                pathEnds.getPaths().forEach(p -> System.out.print(p + ", "));
//                System.out.println();
//            });
//        }
//        if (!routersAllocation.isEmpty()) {
//            properPaths = chooseFromAllocatedRouters(paths, routersAllocation, link);
//            properPaths = getProperPaths(chooseFromAllocatedRouters(excludeChosenPaths(properPaths, chosenPaths), link), link);
            properPaths = getProperPaths(excludeChosenPaths(properPaths, chosenPaths), link);
//        } else
//            properPaths = getProperPaths(link);



//        if (link.getSource().getName().equals("V16")) {
//            System.out.println("2. PROPER_PATHS");
//            properPaths.forEach(pathEnds -> {
//                int sum = Network.locations.get(pathEnds.getFirst().getLocation()) + Network.locations.get(pathEnds.getSecond().getLocation());
//                System.out.print(sum + ": ");
//                pathEnds.getPaths().forEach(p -> System.out.print(p + ", "));
//                System.out.println();
//            });
//        }



//        if (!chosenPaths.isEmpty()) {
//            properPaths = excludeChosenPaths(properPaths, chosenPaths);
//        }



//        if (link.getSource().getName().equals("V16")) {
//            System.out.println("3. PROPER_PATHS");
//            properPaths.forEach(pathEnds -> {
//                int sum = Network.locations.get(pathEnds.getFirst().getLocation()) + Network.locations.get(pathEnds.getSecond().getLocation());
//                System.out.print(sum + ": ");
//                pathEnds.getPaths().forEach(p -> System.out.print(p + ", "));
//                System.out.println();
//            });
//        }
//        System.out.println("PROPER_PATHS: " + properPaths);

        if (properPaths.isEmpty())
            return null;

        Path bestPath = chooseBestPath(properPaths);

        int sum = Network.locations.get(bestPath.getSource().getLocation()) + Network.locations.get(bestPath.getTarget().getLocation());
        System.out.println(bestPath + " weight: " + bestPath.getWeight() + " location: " + sum + " direction: " + bestPath.getDirection());
        System.out.println(Network.locations);
        return bestPath;
    }

    private Path chooseBestPath(List<PathEnds> pathEnds) {
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

    private PathEnds choseBestPathEnds(List<PathEnds> properPaths) {
        return properPaths.stream()
                .max(
                        (pair1, pair2) ->
                                pair2.getFirst().compareTo(pair1.getFirst()) + pair2.getSecond().compareTo(pair1.getSecond())
                ).orElse(null);
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
        pathEnds.setDirection(link, properRoutersAllocation);
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
        if (!reverseFirst && !reverseSecond)
        simple = (simpleFirst || routers.getFirst().checkParameters((link.getSource())))
                && (simpleSecond || routers.getSecond().checkParameters((link.getTarget())));
        if (!simpleFirst && !simpleSecond)
        reverse = (reverseFirst || routers.getFirst().checkParameters(link.getTarget()))
                && (reverseSecond || routers.getSecond().checkParameters(link.getSource()));
        if (simple && !reverse)
            routers.setDirection(PathEnds.Direction.SIMPLE);
        else if (!simple && reverse)
            routers.setDirection(PathEnds.Direction.REVERSE);
        boolean result = simple || reverse;
        if (!routersAllocation.isEmpty())
            result = result && (simpleFirst || simpleSecond || reverseFirst || reverseSecond);
        return result;
    }

//    private boolean checkParameters(PathEnds routers, vLink link) {
//        boolean simple = routers.getFirst().checkParameters((link.getSource()))
//                && routers.getSecond().checkParameters((link.getTarget()));
//        boolean reverse = routers.getFirst().checkParameters(link.getTarget())
//                && routers.getSecond().checkParameters(link.getSource());
//        if (simple && !reverse) {
//            routers.setDirection(PathEnds.Direction.SIMPLE);
//        }
//        else if (!simple && reverse) {
//            routers.setDirection(PathEnds.Direction.REVERSE);
//        }
//        if (link.getSource().getName().equals("V7") || link.getTarget().getName().equals("V7"))
//            System.out.println();
//        return simple || reverse;
//    }

    private PathEnds getPathEndsByPath(Path path) {
        return paths.stream().filter(pathEnds -> pathEnds.getPaths().contains(path)).findAny().get();
    }
}
