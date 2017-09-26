import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter @Setter
public class Allocation implements Comparable<Allocation> {//}, Comparator<Allocation> {

    private int reqIndex;
    private List<AllocationSet> allocationMap;
    private int usedCapacity;
    private int maxMinCapacity;
    private double maxMinCapacityRate;

    public Allocation(int reqIndex) {
        this.reqIndex = reqIndex;
    }


    public Path allocate(Path path, vLink link) {
        if (allocationMap == null) {
            allocationMap = new ArrayList<>();
        }
        allocationMap.add(new AllocationSet(path, path.getDirection(), link));
        usedCapacity += path.serveRequest(reqIndex, link);
        return path;
    }

    public int serve() {
        allocationMap.forEach(s -> {
            s.getPath().setDirection(s.getDirection());
            s.getPath().serveRequest(reqIndex, s.getLink());
//            Log.log(s.getLink() + " --> " + s.getPath());
        });
        return usedCapacity;
    }

    public List<Path> getPaths() {
        return allocationMap.stream().map(AllocationSet::getPath).collect(Collectors.toList());
    }

    public Allocation release() {
        maxMinCapacity = allocationMap.stream().mapToInt(a -> a.getPath().getLeastCapacity()).min().getAsInt();
        maxMinCapacityRate = allocationMap.stream().mapToDouble(a -> a.getPath().getLeastCapacityRate()).min().getAsDouble();
        allocationMap.forEach(s -> s.getPath().releaseRequest(reqIndex));
        return this;
    }

    @Override
    public int compareTo(Allocation a) {
        return usedCapacity < a.getUsedCapacity() ? -1
                : usedCapacity > a.getUsedCapacity() ? 1
                : a.getMaxMinCapacity() >= maxMinCapacity ? 1 : -1;
    }

//    @Override
//    public int compare(Allocation a1, Allocation a2) {
//        return a1.usedCapacity < a2.getUsedCapacity() ? -1
//                : a1.usedCapacity > a2.getUsedCapacity() ? 1
//                : a1.maxMinCapacityRate > a2.getMaxMinCapacityRate() ? -1
//                : a1.maxMinCapacityRate < a2.getMaxMinCapacityRate() ? 1
//                : a2.getMaxMinCapacity() - maxMinCapacity;
//    }

    @Getter @ToString
    private class AllocationSet {

        private final Path path;
        private final vLink link;
        private final PathEnds.Direction direction;

        private AllocationSet(Path path, PathEnds.Direction direction, vLink link) {
            this.path = path;
            this.direction = direction;
            this.link = link;
        }
    }
}
