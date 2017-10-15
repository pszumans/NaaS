import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter @Setter
public class pRouter extends Router implements /*Comparable<pRouter>,*/ Visualisable, Locator {

    private int location; // geographical location
    private int substratePower = power;
    private int substrateMemory = memory;
    private Network network;

    private Map<Integer, List<Integer>> requests;
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

    public void setVRouters(Map<Integer, vRouter> vRouters) {
        vRouters.entrySet().forEach(e ->
                serveRequest(e.getKey(), e.getValue()));
//        this.vRouters = vRouters;
    }

    private boolean checkPower(int Bv) {
        return substratePower >= Bv;
    }

    private boolean checkMemory(int Mv) {
        return substrateMemory >= Mv;
    }

    private boolean checkLocation(Set<Integer> Lv) {
        return Lv.contains(location);
    }

    private boolean checkAll(int Bv, int Mv, Set<Integer> Lv) {
        return (checkPower(Bv) && checkMemory(Mv) && checkLocation(Lv));
    }

//    @Override
//    public int compareTo(pRouter r) {
//        return r.getSubstratePower() + r.getSubstrateMemory() - getSubstratePower() - getSubstrateMemory();
//    }

    public void serveRequest(int request, vRouter router) {
        if (requests == null)
            requests = new LinkedHashMap<>();
        if (vRouters == null)
            vRouters = new LinkedHashMap<>();
        if (vRouters.containsValue(router))
            return;
        if (!router.getLocations().contains(location))
            try {
                throw new WrongLocationException(getVisualText() + " --> " + request + ": " + router);
            } catch (WrongLocationException e) {
                e.printStackTrace();
                System.exit(1);
            }
        if (vRouters.containsKey(request))
            try {
                throw new RequestAllocatedException(getVisualText() + " TRY: " + request + ": " + router);
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
//        network.downdateLocation(location, router.getPower() + router.getMemory());
        network.getLocations().get(location).downdate(power, memory);
    }

    public void removeRequest(int request) {
        if (requests == null || !requests.containsKey(request)) return;
        int power = requests.get(request).get(0);
        int memory = requests.get(request).get(1);
        addPower(power);
        addMemory(memory);
        requests.remove(request);
        vRouters.remove(request);
        if (Heuristic.RESTORABLE_LOCATION)
//        network.updateLocation(location, power + memory);
        network.getLocations().get(location).update(power, memory);
    }

    public boolean isAllocated(vRouter router) {
        return vRouters.containsValue(router);
    }

    public boolean checkParameters(vRouter router) {
        boolean toReturn = (vRouters != null && vRouters.containsKey(router.getReqIndex())) ? false : checkAll(router.getPower(), router.getMemory(), router.getLocations());
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
        return String.format("%s(%d/%d %d/%d %d)", name, substratePower, power, substrateMemory, memory, location);
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

    private String getVRoutersText() {
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
