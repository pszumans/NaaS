import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class pRouter extends Router implements Comparable<pRouter>, Visualisable {

    private int location; // geographical location
    private int substratePower = power;
    private int substrateMemory = memory;
    private Network network;

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
    //    public pRouter(String name, int B, int M, int[] L) {
//        super(name, B, M, L);
//    }

    @Override
    public String getParam(int i) {
        StringBuilder sb = new StringBuilder(name + " ");
        if (i == 0)
            sb.append(substratePower + "\n");
        else if (i == 1)
            sb.append(substrateMemory + "\n");
        if (i == 2)
            sb.append(location + "\n");
        return sb.toString();
    }

    private Map<Integer, List<Integer>> requests;

    public void setVRouters(Map<Integer, vRouter> vRouters) {
        vRouters.entrySet().forEach(e ->
                serveRequest(e.getKey(), e.getValue()));
//        this.vRouters = vRouters;
    }

    public Map<Integer, vRouter> getVRouters() {
        return vRouters;
    }

    private Map<Integer, vRouter> vRouters;

    public pRouter(String name, int Bw, int Mw, int Lw) {
        super(name, Bw, Mw);
        location = Lw;
        substratePower = power;
        substrateMemory = memory;
    }

    public pRouter(String name, int Bw, int Mw, int Lw, Network network) {
        this(name, Bw, Mw, Lw);
        this.network = network;
    }

    public pRouter(String name) {
        super(name);
    }

    @Override
    public void setPower(int power) {
        super.setPower(power);
        substratePower = power;
    }

    @Override
    public void setMemory(int memory) {
        super.setMemory(memory);
        substrateMemory = memory;
    }

    public void addPower(int B) {
        substratePower += B;
    }

    public void removePower(int B) {
        substratePower -= B;
        if (substratePower < 0)
            try {
                throw new NegativeParameterException(getVisualText());
            } catch (NegativeParameterException e) {
                e.printStackTrace();
                System.exit(1);
            }
    }

    public void addMemory(int M) {
        substrateMemory += M;
    }

    public void removeMemory(int M) {
        substrateMemory -= M;
        if (substrateMemory < 0)
            try {
                throw new NegativeParameterException();
            } catch (NegativeParameterException e) {
                e.printStackTrace();
                System.exit(1);
            }
    }

    public int getSubstratePower() {
        return substratePower;
    }

    public void setSubstratePower(int substratePower) {
        this.substratePower = substratePower;
    }

    public int getSubstrateMemory() {
        return substrateMemory;
    }

    public void setSubstrateMemory(int substrateMemory) {
        this.substrateMemory = substrateMemory;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    private boolean checkPower(int Bv) {
        return substratePower >= Bv;
    }

    private boolean checkMemory(int Mv) {
        return substrateMemory >= Mv;
    }

    private boolean checkLocation(List<Integer> Lv) {
        return Lv.contains(location);
    }

    private boolean checkAll(int Bv, int Mv, List<Integer> Lv) {
        return (checkPower(Bv) && checkMemory(Mv) && checkLocation(Lv));
    }

    @Override
    public int compareTo(pRouter r) {
        return r.getSubstratePower() + r.getSubstrateMemory() - getSubstratePower() - getSubstrateMemory();
    }

    public void serveRequest(int request, vRouter router) {
        if (requests == null)
            requests = new LinkedHashMap<>();
        if (vRouters == null)
            vRouters = new LinkedHashMap<>();
        if (vRouters.containsValue(router))
            return;
        if (!router.getLocations().contains(location))
            try {
                throw new WrongLocationException(getVisualText() + " --> " + request + ":" + router);
            } catch (WrongLocationException e) {
                e.printStackTrace();
                System.exit(1);
            }
        if (vRouters.containsKey(request))
            try {
                throw new RequestAllocatedException(getVisualText() + " TRY: " + request + ":" + router);
            } catch (RequestAllocatedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        int power = router.getPower();
        int memory = router.getMemory();
        removePower(power);
        removeMemory(memory);
        requests.put(request, Arrays.asList(power, memory));
        vRouters.put(request, router);
        if (Heuristic.RESTORABLE_LOCATION)
        network.downdateLocation(location, router.getPower() + router.getMemory());
    }

    public void removeRequest(int request) {
        if (!requests.containsKey(request)) return;
        int power = requests.get(request).get(0);
        int memory = requests.get(request).get(1);
        addPower(power);
        addMemory(memory);
        requests.remove(request);
        vRouters.remove(request);
        if (Heuristic.RESTORABLE_LOCATION)
        network.updateLocation(location, power + memory);
    }

    public boolean isAllocated(vRouter router) {
        return vRouters.containsValue(router);
    }

    public boolean checkParameters(vRouter router) {
        boolean toReturn = checkAll(router.getPower(), router.getMemory(), router.getLocations());
//        if (!toReturn)
//            System.out.println("LOCATION CHECK: p = " + location + " v = " + router.getLocations());
        return toReturn;
    }

    @Override
    protected void locate(int... L) {
        location = L[0];
    }

    @Override
    public String toString() {
        return String.format("%s(B=%d, M=%d, L=%d)", name, substratePower, substrateMemory, location);
    }


    @JsonCreator
    public static pRouter JsonParser(@JsonProperty("name") String name, @JsonProperty("B") int B, @JsonProperty("M") int M, @JsonProperty("vRouters") Map<Integer,vRouter> vRouters, @JsonProperty("L") int L) {
        pRouter router = new pRouter(name, B, M, L);
        router.setVRouters(vRouters);
        return router;
    }

    @Override
    public String getVisualText() {
        return String.format("%s(%d/%d,%d/%d,%d)%s", name, substratePower, power, substrateMemory, memory, location, (vRouters != null && !vRouters.isEmpty() ? getVRoutersText() : ""));
    }

    public String getVRoutersText() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("[");
        vRouters.entrySet().forEach(e -> sb.append(e.getKey()).append(":").append(e.getValue().getName()).append("(")
                .append(e.getValue().getPower())
                .append(",")
                .append(e.getValue().getMemory())
                .append(",")
                .append(e.getValue().getLocations())
                .append("),\n"));
        sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toOPL() {
        return String.format("%s %d>\n", super.toOPL(), location);
    }

    public String toOPL(boolean isFull) {
        return String.format("<%s %d %d %d>\n", name, isFull ? power : substratePower, isFull ? memory : substrateMemory, location);
    }
}
