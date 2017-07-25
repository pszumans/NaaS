import java.util.*;

public class PathEndsComparator implements Comparator<Path> {

    private Network network;

    private int maxPower;
    private int minPower;
    private int maxMemory;
    private int minMemory;
    private int diffPower;
    private int diffMemory;

    public PathEndsComparator(Network network) {
        this.network = network;
        initCons(true);
    }

    private void initCons(boolean isGlobal) {
        Set<Locable> locables = isGlobal ? new HashSet<>(network.getLocations().values()) : network.vertexSet();
        maxPower = locables.stream().mapToInt(l -> l.getSubstratePower()).max().getAsInt();
        minPower = locables.stream().mapToInt(l -> l.getSubstratePower()).min().getAsInt();
        maxMemory = locables.stream().mapToInt(l -> l.getSubstrateMemory()).max().getAsInt();
        minMemory = locables.stream().mapToInt(l -> l.getSubstrateMemory()).min().getAsInt();
        diffPower = maxPower - minPower;
        diffMemory = maxMemory - minMemory;
    }

    @Override
    public int compare(Path p1, Path p2) {

//        int one = network.getLocations().get(p1.getSource().getLocation()).getSubstratePower()
//                + network.getLocations().get(p1.getSource().getLocation()).getSubstrateMemory()
//                + network.getLocations().get(p1.getTarget().getLocation()).getSubstratePower()
//                + network.getLocations().get(p1.getTarget().getLocation()).getSubstrateMemory();
//        int two = network.getLocations().get(p2.getSource().getLocation()).getSubstratePower()
//                + network.getLocations().get(p2.getSource().getLocation()).getSubstrateMemory()
//                + network.getLocations().get(p2.getTarget().getLocation()).getSubstratePower()
//                + network.getLocations().get(p2.getTarget().getLocation()).getSubstrateMemory();

        double one = count(network.getLocations().get(p1.getSource().getLocation()), network.getLocations().get(p1.getTarget().getLocation()));
        double two = count(network.getLocations().get(p2.getSource().getLocation()), network.getLocations().get(p2.getTarget().getLocation()));
//
//        System.out.println(p1 + " " + one + " " + p2 + " " + two + " " + (one-two));
//        if (p1.getSource().getName().equals("WUSb") && p1.getTarget().getName().equals("CUSa"))
//        System.out.println(p1 + " " + one + " " + p2 + " " + two + " " + (one-two));
//
//        if (two - one == 0) {
//            one = p1.getSource().getPower() + p1.getSource().getMemory() + p1.getTarget().getPower() + p1.getTarget().getMemory();
//            two = p2.getSource().getPower() + p2.getSource().getMemory() + p2.getTarget().getPower() + p2.getTarget().getMemory();
//        }
//
        if (two - one == 0) {
            initCons(false);
            one = count(p1.getSource(), p1.getTarget());
            two = count(p2.getSource(), p2.getTarget());
        }

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

    private double count(Locable locable1, Locable locable2) {
        double power1 = (diffPower == 0) ? Double.MAX_EXPONENT : (
                maxPower -
                        locable1.getSubstratePower()) / diffPower;
        double memory1 = (diffMemory == 0) ? Double.MAX_EXPONENT :(
                maxMemory -
                        locable1.getSubstrateMemory()) / diffMemory;
        double power2 = (diffPower == 0) ? Double.MAX_EXPONENT : (
                maxPower -
                        locable2.getSubstratePower()) / diffPower;
        double memory2 = (diffMemory == 0) ? Double.MAX_EXPONENT : (
                maxMemory -
                        locable2.getSubstrateMemory()) / diffMemory;
        return min(power1 + power2, memory1 + memory2);
    }

    private double min(double power, double memory) {
        return Math.min(power, memory);
    }
}