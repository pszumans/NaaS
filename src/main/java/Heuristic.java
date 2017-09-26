import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Szuman on 27.03.2017.
 */
@Getter @Setter
public class Heuristic extends Solver implements Serializable{

    public static final boolean WEIGHTABLE_LINKS = false;
    //    private static final boolean RESTORABLE_PATHS = false;
    public static final boolean RESTORABLE_LOCATION = true;

    public enum Type {NORMAL, DESCENDING, ASCENDING;}
    public static Type type = Type.NORMAL;

    private List<PathEnds> paths;
    private Map<Integer, List<Path>> pathsAllocated;
    private Map<vRouter, pRouter> routersAllocation;

    private boolean stop;
    private boolean timeStarted;
    private long timestamp;

    public Heuristic(Network network) {
        super(network);
        paths = network.getActualPaths();
//        this.network = network;
    }

    @Override
    public void solve() {
        long start = System.nanoTime();
        serveRequests();
        long end = System.nanoTime();
        fullTime = (double) (end - start) / 1000000000;
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
            serveRequest(network.getRequests().get(i));
//                    )
//                pointer++
            ;
        }
//        Log.log(pathsAllocated);
//        Log.log(network.getRequests());
//        Log.log(network.getServedRequests());
    }

    //    private boolean serveRequest(Request request) {
    @Override
    public boolean serveRequest(Request request) {
        if (pathsAllocated == null)
            pathsAllocated = new LinkedHashMap<>();
        if (pathsAllocated.get(request.getIndex()) == null)
            pathsAllocated.put(request.getIndex(), new ArrayList<>());

        stop = false;
//            Log.logF(Log.DIR + "reqs2.txt", request.toString());
        if (!timeStarted) startTime();
//        if (paths == null || RESTORABLE_PATHS)
//            paths = network.getActualPaths();

        List<vLink> links = new ArrayList<>(request.getLinks());
        Collections.sort(links);

        Allocation all1, all2;
        if (type == Type.ASCENDING) {
            all1 = null;
        }
        else {
            all1 = allocateLinks(request.getIndex(), links);
            pathsAllocated.get(request.getIndex()).clear();
        }

        if (stop) {
            Log.log(request);
            updateTime();
            return false;
        }

        if (type == Type.DESCENDING) {
            all2 = null;
        } else {
            Collections.reverse(links);
            all2 = allocateLinks(request.getIndex(), links);
            pathsAllocated.get(request.getIndex()).clear();
        }
//        Allocation all2 = null; // 2 null lepiej, ale wciaz gorzej
        if (all1 == null && all2 == null) {
            Log.log(request);
            updateTime();
            return false;
        } else if (all1 != null && all2 != null) {
            List<Allocation> all = Arrays.asList(all1, all2);
            Collections.sort(all);
//            if (all1.getUsedCapacity() == all2.getUsedCapacity() && all1.getMaxMinCapacity() == all2.getMaxMinCapacity()) {
//                System.out.println(all.get(0).getAllocationMap());
//                System.out.println(all.get(0).getAllocationMap().size());
//                System.out.println(request);
//                if (all.get(0).equals(all1))
//                    System.out.println("JEDYNKA");
//                else
//                if (all.get(0).equals(all2))
//                    System.out.println("DWOJKA");
//                new Scanner(System.in).nextLine();
//            }
            network.addUsedCapacity(all.get(0).serve());
            pathsAllocated.put(request.getIndex(), all.get(0).getPaths());
        } else if (all2 == null) {
            network.addUsedCapacity(all1.serve());
            pathsAllocated.put(request.getIndex(), all1.getPaths());
        } else {
            network.addUsedCapacity(all2.serve());
            pathsAllocated.put(request.getIndex(), all2.getPaths());
        }

//        if ((all1 == null && all2 != null) || all1.getUsedCapacity() > ((all2 != null) ? all2.getUsedCapacity() : Integer.MAX_VALUE)) {
//            network.addUsedCapacity(all2.serve());
//        } else {
//            network.addUsedCapacity(all1.serve());
//        }

//        Log.log(paths.size() + "PATHS: " + paths);
//        network.addServedRequest(request);

        updateTime();

        return super.serveRequest(request);
//        return true;
    }

    private void startTime() {
        timestamp = System.nanoTime();
        timeStarted = true;
    }

    private void updateTime() {
        time = (double) (System.nanoTime() - timestamp) / 1000000000;
        timestamp = System.nanoTime();
        fullTime += time;
    }

    private Allocation allocateLinks(int reqIndex, List<vLink> links) {
//        if (pathsAllocated == null)
//            pathsAllocated = new LinkedHashMap<>();
        Allocation allocation = new Allocation(reqIndex);
        routersAllocation = new LinkedHashMap<>();
//        if (pathsAllocated.get(reqIndex) == null)
//            pathsAllocated.put(reqIndex, new ArrayList<>());
        for (vLink link : links) {
            Path chosenPath = chooseBestPath(
                    pathsAllocated.get(reqIndex)
                    , link);
            if (chosenPath == null) {
                releaseRequest(reqIndex);
                if (links.get(0).equals(link)) {
                    if (pathsAllocated.get(reqIndex).isEmpty())
                        stop = true;
//                    System.out.println("ALL_STOP");
                    return null;
                }
//                System.out.println("FAIL");
//                allocation.release();
                return allocateLinks(reqIndex, links);
            }
            Path path = allocation.allocate(chosenPath, link);
            pathsAllocated.get(reqIndex).add(path);
//            updateWeights(chosenPath); // aktualizuj wagę
//            System.out.println("PATHS_ALLOCATED #" + reqIndex + " (" + link.toOPL());
//            for (Path path1 : pathsAllocated.get(reqIndex)) {
//                System.out.println(path1.toOPL());
//                path1.getEdgeList().forEach(e -> System.out.print(" " + e.getSubstrateCapacity()));
//                System.out.println();
//            }
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
//            Log.log(link + "-> ROUTERS ALLO: " + routersAllocation);
        }
//        System.out.println("ALL_END");
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
//        if (pathsAllocated == null || !pathsAllocated.containsKey(request.getIndex())) {
//            network.getRouters().forEach(r -> r.removeRequest(request.getIndex()));
//            network.getLinks().forEach(r -> r.removeRequest(request.getIndex()));
//        } else
        pathsAllocated.get(request.getIndex()).forEach(p -> network.removeUsedCapacity(p.releaseRequest(request.getIndex())));
        super.releaseRequest(request);
    }

    private Path chooseBestPath(List<Path> chosenPaths, vLink link) {
        List<PathEnds> properPaths = paths.stream().map(PathEnds::new).collect(Collectors.toList());// = getProperPaths(link);
//        System.out.println("### " + link);
        properPaths = getProperPaths(excludeChosenPaths(properPaths, chosenPaths), link);

        if (properPaths.isEmpty())
            return null;

        Path bestPath = chooseBestPath(properPaths);

//        Log.log(network.getLocations());
        return bestPath;
    }

    private Path chooseBestPath(List<PathEnds> pathEnds) {
//        if (WEIGHTABLE_LINKS)
//        List<Path> paths = pathEnds.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList());
//        Collections.sort(paths, new PathsComparator(network));
//        for (int i = 0; i < 5 && i < paths.size(); i++) {
//            Path p = paths.get(i);
//            System.out.println(p.getSource() + " <--> " + p.getTarget() + " " + p.getWeight() + " " + p.getLeastCapacity());
//        }
//        new Scanner(System.in).nextLine();
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
//        System.out.println(properPaths.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList()).size());
//        properPaths.removeAll(chosenPaths.stream().map(p -> getPathEndsByPath(p)).collect(Collectors.toList()));
        properPaths.forEach(pE -> pE.filterPaths(p -> !chosenPaths.contains(p)));
//        System.out.println(properPaths.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList()).size());

        return properPaths;
//                .stream()
//                .filter(p -> !isPathUsed(p, chosenPaths))
//                .collect(Collectors.toList());
    }

    private boolean containsPath(PathEnds routers, List<Path> chosenPaths) {
        for (Path path:
             chosenPaths) {
            if (routers.getPaths().stream().anyMatch(p -> p.getName().equals(path.getName()) && p.getIndex() == path.getIndex()))
                return true;
        }
        return false;
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
//        Log.log(properRoutersAllocation.values().stream().anyMatch(r -> pathEnds.hasElement(r)));
        return /*list.isEmpty() ? false : */properRoutersAllocation.values().stream().anyMatch(r -> pathEnds.hasElement(r));
    }

    private List<PathEnds> getProperPaths(vLink link) {
//		return
        List<PathEnds> properPaths =
                paths.stream().filter(pathEnds ->
                        checkParameters(pathEnds, link) && pathEnds.getPaths().stream().anyMatch(p -> p.checkCapacity(link.getCapacity()))).collect(Collectors.toList());
        properPaths.forEach(k -> k.filterPaths(p -> p.checkCapacity(link.getCapacity())));
//        paths.forEach(p -> Log.log(p.getPaths()));
//        Log.log(properPaths);
        return properPaths;
    }

    private List<PathEnds> getProperPaths(List<PathEnds> properPaths, vLink link) {
//		return
        properPaths =
                properPaths.stream().filter(pathEnds ->
                        checkParameters(pathEnds, link) && pathEnds.getPaths().stream().anyMatch(p -> p.checkCapacity(link.getCapacity()))).collect(Collectors.toList());
        properPaths.forEach(k -> k.filterPaths(p -> p.checkCapacity(link.getCapacity())));
//        paths.forEach(p -> Log.log(p.getPaths()));
        return properPaths;
    }

    private boolean checkParameters(PathEnds routers, vLink link) {
        routers.restoreDirection();
        boolean simpleFirst = routersAllocation.containsKey(link.getSource()) ? routersAllocation.get(link.getSource()).equals(routers.getFirst()) : false;
        boolean simpleSecond = routersAllocation.containsKey(link.getTarget()) ? routersAllocation.get(link.getTarget()).equals(routers.getSecond()) : false;
        boolean reverseFirst = routersAllocation.containsKey(link.getTarget()) ? routersAllocation.get(link.getTarget()).equals(routers.getFirst()) : false;
        boolean reverseSecond = routersAllocation.containsKey(link.getSource()) ? routersAllocation.get(link.getSource()).equals(routers.getSecond()) : false;

        if (routersAllocation.containsKey(link.getSource()) && routersAllocation.containsKey(link.getTarget())) {
            if (simpleFirst && simpleSecond) {
                routers.setDirection(PathEnds.Direction.SIMPLE);
                return true;
            } else if (reverseFirst && reverseSecond) {
                routers.setDirection(PathEnds.Direction.REVERSE);
                return true;
            } else
                return false;
        }

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
//        if (!routersAllocation.isEmpty()) {
//            if (routersAllocation.containsKey(link.getSource()) && routersAllocation.containsKey(link.getTarget()))
//                return (simpleFirst && simpleSecond) || (reverseFirst && reverseSecond);
        if (routersAllocation.containsKey(link.getSource()) || routersAllocation.containsKey(link.getTarget()))
            result = result && (simpleFirst || simpleSecond || reverseFirst || reverseSecond);
//        }
        return result;
    }

    private PathEnds getPathEndsByPath(Path path) {
        return paths.stream().filter(pathEnds -> pathEnds.getPaths().contains(path)).findAny().get();
    }
}