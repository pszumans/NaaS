import java.util.Comparator;
import java.util.List;

public class PathEndsComparator implements Comparator<Path> {
    @Override
    public int compare(Path p1, Path p2) {
//        int one = p2.getSource().compareTo(p1.getSource());
//        int two = p2.getTarget().compareTo(p1.getTarget());
//        return one + two;
        int one = Network.locations.get(p1.getSource().getLocation()) + (Network.locations.get(p1.getTarget().getLocation()));
        int two = Network.locations.get(p2.getSource().getLocation()) + (Network.locations.get(p2.getTarget().getLocation()));
//        System.out.println(p1 + " " + one + " " + p2 + " " + two + " " + (one-two));
        return two - one;
    }
}