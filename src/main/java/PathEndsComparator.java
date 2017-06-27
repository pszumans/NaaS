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


        if (two - one == 0) {
            one = p1.getSource().getPower() + p1.getSource().getMemory() + p1.getTarget().getPower() + p1.getTarget().getMemory();
            two = p2.getSource().getPower() + p2.getSource().getMemory() + p2.getTarget().getPower() + p2.getTarget().getMemory();
        }
//
//        if (two - one == 0) {
//            one = 0;
//            two = 0;
//            if (p1.getSource().isTransit())
//                two++;
//            if (p1.getTarget().isTransit())
//                two++;
//            if (p2.getSource().isTransit())
//                one++;
//            if (p2.getTarget().isTransit())
//                one++;
//        }

        return two - one;
    }
}