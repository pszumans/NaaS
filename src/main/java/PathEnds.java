import lombok.Getter;
import lombok.Setter;
import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Szuman on 20.03.2017.
 */
@Getter @Setter
public class PathEnds extends Pair<pRouter, pRouter> {

    private List<Path> paths;

    public enum Direction {BOTH, SIMPLE, REVERSE}
    private Direction direction;
    private String name;

    public PathEnds(pRouter r1, pRouter r2) {
        super(r1, r2);
        direction = Direction.BOTH;
    }

    public PathEnds(PathEnds pathEnds) {
        this(pathEnds.getFirst(), pathEnds.getSecond());
        paths = pathEnds.getPaths();
    }

    public void restoreDirection() {
        direction = Direction.BOTH;
        paths.forEach(p -> p.setDirection(Direction.BOTH));
    }

    public void addPath(Path path) {
        paths.add(path);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        if (direction != Direction.BOTH)
//        if (direction == Direction.REVERSE)
            paths.forEach(p -> p.setDirection(direction));
    }

//    public void setDirection(vLink link, Map<vRouter, pRouter> list) {
////        boolean simple = list.contains(first);
////        boolean reverse = list.contains(second);
//        pRouter r1 = list.get(link.getSource());
//        pRouter r2 = list.get(link.getTarget());
//        boolean simple = r1 != null ? r1.equals(first) : false || r2 != null ? r2.equals(second) : false;
//        boolean reverse = r1 != null ? r1.equals(second) : false || r2 != null ? r2.equals(first) : false;
//        if (simple && !reverse)
//            setDirection(Direction.SIMPLE);
//        else if (!simple && reverse) {
//            setDirection(Direction.REVERSE);
//        }
//    }

    public void filterPaths(Predicate<Path> predicate) {
        paths = paths.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("%s %s %d", getFirst().getName(), getSecond().getName(), paths.size());
    }
}
