import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
public class Heuristic extends Solver implements Serializable{

    public static final boolean WEIGHTABLE_LINKS = false;
    public static final boolean RESTORABLE_LOCATION = true;

    public enum Type {NORMAL, DESCENDING, ASCENDING;}
    public static Type type = Type.NORMAL;

    private List<PathEnds> paths;
    private Map<Integer, List<Path>> pathsAllocated;
    private Map<VRouter, PRouter> routersAllocation;

    private boolean stop;
    private boolean timeStarted;
    private long timestamp;

    public Heuristic(Network network) {
        super(network);
        paths = network.getActualPaths();
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
            serveRequest(network.getRequests().get(i));
            ;
        }
    }

    @Override
    public boolean serveRequest(Request request) {
        if (pathsAllocated == null)
            pathsAllocated = new LinkedHashMap<>();
        if (pathsAllocated.get(request.getIndex()) == null)
            pathsAllocated.put(request.getIndex(), new ArrayList<>());

        stop = false;
        if (!timeStarted) startTime();

        List<VLink> links = new ArrayList<>(request.getLinks());
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
            Logger.log(request);
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
        if (all1 == null && all2 == null) {
            Logger.log(request);
            updateTime();
            return false;
        } else if (all1 != null && all2 != null) {
            List<Allocation> all = Arrays.asList(all1, all2);
            Collections.sort(all);
            network.addUsedCapacity(all.get(0).serve());
            pathsAllocated.put(request.getIndex(), all.get(0).getPaths());
        } else if (all2 == null) {
            network.addUsedCapacity(all1.serve());
            pathsAllocated.put(request.getIndex(), all1.getPaths());
        } else {
            network.addUsedCapacity(all2.serve());
            pathsAllocated.put(request.getIndex(), all2.getPaths());
        }

        updateTime();

        return super.serveRequest(request);
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

    private Allocation allocateLinks(int reqIndex, List<VLink> links) {
        Allocation allocation = new Allocation(reqIndex);
        routersAllocation = new LinkedHashMap<>();
        for (VLink link : links) {
            Path chosenPath = chooseBestPath(
                    pathsAllocated.get(reqIndex)
                    , link);
            if (chosenPath == null) {
                releaseRequest(reqIndex);
                if (links.get(0).equals(link)) {
                    if (pathsAllocated.get(reqIndex).isEmpty())
                        stop = true;
                    return null;
                }
                return allocateLinks(reqIndex, links);
            }
            Path path = allocation.allocate(chosenPath, link);
            pathsAllocated.get(reqIndex).add(path);
            routersAllocation.put(link.getSource(), chosenPath.getSource());//;getStartVertex());
            routersAllocation.put(link.getTarget(), chosenPath.getTarget());//;getEndVertex());
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
        pathsAllocated.get(request.getIndex()).forEach(p -> network.removeUsedCapacity(p.releaseRequest(request.getIndex())));
        super.releaseRequest(request);
    }

    private Path chooseBestPath(List<Path> chosenPaths, VLink link) {
        List<PathEnds> properPaths = paths.stream().map(PathEnds::new).collect(Collectors.toList());
        properPaths = getProperPaths(excludeChosenPaths(properPaths, chosenPaths), link);
        if (properPaths.isEmpty())
            return null;

        return chooseBestPath(properPaths);
    }

    private Path chooseBestPath(List<PathEnds> pathEnds) {
        return pathEnds.stream().flatMap(pE -> pE.getPaths().stream()).collect(Collectors.toList()).stream().min(new PathsComparator(network)).get();
    }

    private List<PathEnds> excludeChosenPaths(List<PathEnds> properPaths, List<Path> chosenPaths) {
        properPaths.forEach(pE -> pE.filterPaths(p -> !chosenPaths.contains(p)));
        return properPaths;
    }

    private List<PathEnds> getProperPaths(List<PathEnds> properPaths, VLink link) {
        properPaths =
                properPaths.stream().filter(pathEnds ->
                        checkParameters(pathEnds, link) && pathEnds.getPaths().stream().anyMatch(p -> p.checkCapacity(link.getCapacity()))).collect(Collectors.toList());
        properPaths.forEach(k -> k.filterPaths(p -> p.checkCapacity(link.getCapacity())));
        return properPaths;
    }

    private boolean checkParameters(PathEnds routers, VLink link) {
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

        if (routersAllocation.containsKey(link.getSource()) || routersAllocation.containsKey(link.getTarget()))
            result = result && (simpleFirst || simpleSecond || reverseFirst || reverseSecond);

        return result;
    }

    private PathEnds getPathEndsByPath(Path path) {
        return paths.stream().filter(pathEnds -> pathEnds.getPaths().contains(path)).findAny().get();
    }
}