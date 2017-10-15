import java.util.*;
import java.util.function.ToIntFunction;

public class EndsComparator implements Comparator<Path> {

    private Network network;

    private int maxPower;
    //    private int minPower;
    private int maxMemory;
    //    private int minMemory;
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

//        if (maxPower > network.getRouters().stream().mapToInt(pRouter::getPower).max().getAsInt()
//                && maxMemory > network.getRouters().stream().mapToInt(pRouter::getMemory).max().getAsInt()) {
        if (!isGlobal) {
            isGlobal = true;
            initCons();
        }
        one = count(network.getLocations().get(p1.getSource().getLocation()), network.getLocations().get(p1.getTarget().getLocation()));
//        double t = max;
        two = count(network.getLocations().get(p2.getSource().getLocation()), network.getLocations().get(p2.getTarget().getLocation()));

//        if (two == one) {
//            one = t;
//            two = max;
//        }
//

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
//                System.out.println(p1.getSource() + " " + p1.getTarget());
//                System.out.println(p2.getSource() + " " + p2.getTarget());
//                System.out.println(one + " " + two);
//                new Scanner(System.in).nextLine();
            }
        }

//        if (two - one == 0) {
//            one = count(network.getLocations().get(p1.getSource().getLocation()), network.getLocations().get(p1.getTarget().getLocation()));
//            two = count(network.getLocations().get(p2.getSource().getLocation()), network.getLocations().get(p2.getTarget().getLocation()));
//        }

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
//        System.out.println("MAXP = " + maxPower);
//        System.out.println("MinP = " + minPower);
//        System.out.println("MAXM = " + maxMemory);
//        System.out.println("MinM = " + minMemory);
//        System.out.println("L1 P = " + locable1.getSubstratePower());
//        System.out.println("L1 M = " + locable1.getSubstrateMemory());
//        System.out.println("L2 P = " + locable2.getSubstratePower());
//        System.out.println("L2 M = " + locable2.getSubstrateMemory());
//        System.out.println("power = " + power);
//        System.out.println("p2 = " + p2);
//        System.out.println("memory = " + memory);
//        System.out.println("m2 = " + m2);
//        new Scanner(System.in).nextLine();
        if (!isGlobal)
            max = Math.max(power, memory);
        return min(power, memory);
    }

    private double min(double power, double memory) {
        return Math.min(power, memory);
    }

//    public int compare(vLink link1, vLink link2) {
//        List<vRouter> links = Arrays.asList(link1.getSource(), link1.getTarget(), link2.getSource(), link2.getTarget());
//        int minPower = links.stream().mapToInt(vRouter::getPower).min().getAsInt();
//        int minMemory = links.stream().mapToInt(vRouter::getMemory).min().getAsInt();
//        initCons(40, minPower, 40, minMemory);
//        double one = count(link1);
//        double temp = max;
//        double two = count(link2);
//        if (two - one == 0) {
//            one = temp;
//            two = max;
//        }
//        return (two <= one) ? 1
////                : (two == one) ? 0
//                : -1;
//    }

    private double count(vLink link) {
        vRouter source = link.getSource();
        vRouter target = link.getTarget();
        return count(source.getPower(), source.getMemory(), target.getPower(), target.getMemory());
    }
}