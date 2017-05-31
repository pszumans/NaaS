import java.util.Comparator;

public class PathsComparatorUpdate implements Comparator<Path> {

    @Override
    public int compare(Path p1, Path p2) {
//        double sum1 = p1.getEdgeList().stream().mapToInt(pLink::getSubstrateCapacity).sum();
//        double sum2 = p2.getEdgeList().stream().mapToInt(pLink::getSubstrateCapacity).sum();
//        double avg1 = p1.getEdgeList().stream().mapToInt(pLink::getSubstrateCapacity).average().getAsDouble() * p1.getLength();
//        double avg2 = p2.getEdgeList().stream().mapToInt(pLink::getSubstrateCapacity).average().getAsDouble() * p2.getLength();
//        return sum1 - avg1 > sum2 - avg2 ? 1
//                : sum1 - avg1 < sum2 - avg2 ? -1
//                : 0;
        return p1.getLeastCapacity() < p2.getLeastCapacity() ? 1
                : p2.getLeastCapacity() > p2.getLeastCapacity() ? -1
                : 0;
    }
}
