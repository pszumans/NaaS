import java.util.Comparator;
import java.util.List;

/**
 * Created by Szuman on 13.03.2017.
 */
public class RoutersComparator implements Comparator<List<pRouter>> {
    @Override
    public int compare(List<pRouter> list1, List<pRouter> list2) {
        int one = list2.get(0).compareTo(list1.get(0));
        int two = list2.get(1).compareTo(list1.get(1));
        return one + two;
    }
}
