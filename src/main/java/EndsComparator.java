import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

public class EndsComparator implements Comparator<Path> {

    private Network network;

    private int maxPower;
    private int maxMemory;
    private double diffPower;
    private double diffMemory;
    private double max;
    private boolean isGlobal;

    public EndsComparator() {
    }

    public EndsComparator(Network network) {
        this.network = network;
        isGlobal = true;
        initCons();
    }

    private void initCons() {
        Set<Locator> locators = isGlobal ? new HashSet<>(network.getLocations().values()) : network.vertexSet();
        initCons(getMax(locators, Locator::getSubstratePower), getMin(locators, Locator::getSubstratePower),
                getMax(locators, Locator::getSubstrateMemory), getMin(locators, Locator::getSubstrateMemory));
    }

    private int getMax(Set<Locator> locators, ToIntFunction<Locator> function) {
        return locators.stream().mapToInt(function).max().getAsInt();
    }

    private int getMin(Set<Locator> locators, ToIntFunction<Locator> function) {
        return locators.stream().mapToInt(function).min().getAsInt();
    }

    private void initCons(int maxPower, int minPower, int maxMemory, int minMemory) {
        this.maxPower = maxPower;
        this.maxMemory = maxMemory;
        diffPower = maxPower - minPower + Double.MIN_VALUE;
        diffMemory = maxMemory - minMemory + Double.MIN_VALUE;
    }

    @Override
    public int compare(Path p1, Path p2) {

        double one = 0, two = 0;

        if (!isGlobal) {
            isGlobal = true;
            initCons();
        }
        one = count(network.getLocations().get(p1.getSource().getLocation()), network.getLocations().get(p1.getTarget().getLocation()));
        two = count(network.getLocations().get(p2.getSource().getLocation()), network.getLocations().get(p2.getTarget().getLocation()));

        if (two == one) {
            if (isGlobal) {
                isGlobal = false;
                initCons();
            }
            one = count(p1.getSource(), p1.getTarget());
            double temp = max;
            two = count(p2.getSource(), p2.getTarget());
            if (two == one) {
                one = temp;
                two = max;
            }
        }

        return (two < one) ? 1 : (two == one) ? 0 : -1;
    }

    private double count(Locator locator1, Locator locator2) {
        int power1 = locator1.getSubstratePower();
        int memory1 = locator1.getSubstrateMemory();
        int power2 = locator2.getSubstratePower();
        int memory2 = locator2.getSubstrateMemory();
        return count(power1, memory1, power2, memory2);
    }

    private double count(int power1, int memory1, int power2, int memory2) {
        double power = //(diffPower == 0) ? Double.MAX_EXPONENT :
                (2 * maxPower -
                        (power1 + power2)) / diffPower;
        double memory = //(diffMemory == 0) ? Double.MAX_EXPONENT :
                (2 * maxMemory -
                        (memory1 + memory2)) / diffMemory;
        if (!isGlobal)
            max = Math.max(power, memory);
        return min(power, memory);
    }

    private double min(double power, double memory) {
        return Math.min(power, memory);
    }

//    public int compare(VLink link1, VLink link2) {
//        List<VRouter> links = Arrays.asList(link1.getSource(), link1.getTarget(), link2.getSource(), link2.getTarget());
//        int minPower = links.stream().mapToInt(VRouter::getPower).min().getAsInt();
//        int minMemory = links.stream().mapToInt(VRouter::getMemory).min().getAsInt();
//        initCons(40, minPower, 40, minMemory);
//        double one = count(link1);
//        double temp = max;
//        double two = count(link2);
//        if (two - one == 0) {
//            one = temp;
//            two = max;
//        }
//        return (two <= one) ? 1
//                : (two == one) ? 0
//                : -1;
//    }

    private double count(VLink link) {
        VRouter source = link.getSource();
        VRouter target = link.getTarget();
        return count(source.getPower(), source.getMemory(), target.getPower(), target.getMemory());
    }
}