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
//                : p1.getSource().getLocation() != p1.getTarget().getLocation() && p2.getSource().getLocation() == p2.getTarget().getLocation() ? 1
//                : p1.getSource().getLocation() == p1.getTarget().getLocation() && p2.getSource().getLocation() != p2.getTarget().getLocation() ? -1
//                : p1.getTransitCount() > p2.getTransitCount() ? 1
//                : p1.getTransitCount() < p2.getTransitCount() ? -1
                : p1.getLeastCapacityRate() < p2.getLeastCapacityRate() ? 1
                : p1.getLeastCapacityRate() > p2.getLeastCapacityRate() ? -1
                :

                new EndsComparator(network).compare(p1,p2)
                ;
    }
}
