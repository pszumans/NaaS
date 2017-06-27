import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Allocation {

    public int getUsedCapacity() {
        return usedCapacity;
    }

    public void setUsedCapacity(int usedCapacity) {
        this.usedCapacity = usedCapacity;
    }

    private int reqIndex;
    private List<AllocationSet> allocationMap;
    private int usedCapacity;

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
            s.getPath().getEdgeList().forEach(l -> System.out.println("LLL: " + l));
        });
        return usedCapacity;
    }

    public Allocation release() {
        allocationMap.forEach(s -> s.getPath().releaseRequest(reqIndex));
        return this;
    }

    private class AllocationSet {

        private final Path path;
        private final PathEnds.Direction direction;
        private final vLink link;

        public Path getPath() {
            return path;
        }
        public PathEnds.Direction getDirection() {
            return direction;
        }
        public vLink getLink() {
            return link;
        }

        private AllocationSet(Path path, PathEnds.Direction direction, vLink link) {
            this.path = path;
            this.direction = direction;
            this.link = link;
        }
    }
}
