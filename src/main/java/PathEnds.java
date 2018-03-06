import lombok.Getter;
import lombok.Setter;
import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class PathEnds extends Pair<PRouter, PRouter> {

    private List<Path> paths;

    public enum Direction {BOTH, SIMPLE, REVERSE}

    private Direction direction;
    private String name;

    public PathEnds(PRouter r1, PRouter r2) {
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
            paths.forEach(p -> p.setDirection(direction));
    }

    public void filterPaths(Predicate<Path> predicate) {
        paths = paths.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("%s %s %d", getFirst().getName(), getSecond().getName(), paths.size());
    }
}
