import java.util.Comparator;

/**
 * Created by Szuman on 13.03.2017.
 */
public class PathsComparator implements Comparator<Path> {

    private Network network;

    public PathsComparator(Network network) {
        this.network = network;
    }

    @Override
    public int compare(Path p1, Path p2) {
        return p1.getWeight() > p2.getWeight() ? 1
                : p1.getWeight() < p2.getWeight() ? -1
                : p1.getLeastCapacity() > p2.getLeastCapacity() ? 1
                : p1.getLeastCapacity() < p2.getLeastCapacity() ? -1
                :
//                0
                new PathEndsComparator(network).compare(p1,p2)
                ;
    }
}
