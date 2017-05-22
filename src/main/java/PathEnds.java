import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Szuman on 20.03.2017.
 */
public class PathEnds extends Pair<pRouter, pRouter> {

    public List<Path> getPaths() {
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

    public void addPath(Path path) {
        paths.add(path);
    }

    private List<Path> paths;

    public enum Capability {BOTH, SIMPLE, REVERSE}
    private Capability capability;
    public PathEnds(pRouter r1, pRouter r2) {
        super(r1, r2);
        capability = Capability.BOTH;
    }

    public Capability getCapability() {
        return capability;
    }

    public void setCapability(Capability capability) {
        this.capability = capability;
        if (capability == Capability.REVERSE)
            paths.forEach(p -> p.setDirection(Path.Direction.REVERSE));
    }

    public void setCapability(vLink link, Map<vRouter, pRouter> list) {
//        boolean simple = list.contains(first);
//        boolean reverse = list.contains(second);
        pRouter r1 = list.get(link.getSource());
        pRouter r2 = list.get(link.getTarget());
        boolean simple = r1 != null ? r1.equals(first) : false || r2 != null ? r2.equals(second) : false;
        boolean reverse = r1 != null ? r1.equals(second) : false || r2 != null ? r2.equals(first) : false;
        if (simple && !reverse)
            capability = Capability.SIMPLE;
        else if (!simple && reverse) {
            capability = Capability.REVERSE;
            paths.forEach(p -> p.setDirection(Path.Direction.REVERSE));
        }
    }

    public void filterPaths(Predicate<Path> predicate) {
        paths = paths.stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("%s %s %d", getFirst().getName(), getSecond().getName(), paths.size());
    }
}
