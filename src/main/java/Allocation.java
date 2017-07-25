import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Getter @Setter
public class Allocation {

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
            System.out.println(s.getLink() + " --> " + s.getPath());
        });
        return usedCapacity;
    }

    public Allocation release() {
        allocationMap.forEach(s -> s.getPath().releaseRequest(reqIndex));
        return this;
    }

    @Getter
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
