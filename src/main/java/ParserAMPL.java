import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ParserAMPL {

    public Network getGraph() {
        return graph;
    }

    public void setGraph(Network graph) {
        this.graph = graph;
    }

    Network graph;

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    private List<Request> requests;

    private final String CAPACITY_KEY = "Ce";
    private final String vCAPACITY_KEY = "Cd";

    private final String[] PHYSICAL = {"Bw", "Mw", "Lw"};
    private final String[] VIRTUAL = {"Bv", "Mv", "Lv"};//, "L"};

    public static boolean arePaths = false;

    private Scanner sc;

    public ParserAMPL(String filename) throws FileNotFoundException {
        sc = new Scanner(new BufferedReader(new FileReader(new File(filename))));
    }

    public String scanResult() {
        String result = sc.next();
        sc.close();
        return result;
    }

    public void parse() throws FileNotFoundException {

//        graph = new SimpleWeightedGraph<>(pLink.class);
        requests = new ArrayList<>();
        graph = new Network(requests);

        String data;
        while (sc.hasNextLine()) {
            if (arePaths)
                break;
            if (sc.hasNext()) {
                data = sc.next();
                if (data.matches("param*"))
                    addData(sc.next());
            }
            else
                sc.nextLine();
        }
        sc.close();
        pLink.resetCounter();
    }

    private void addData(String dataKey) {
//        if (dataKey.equals("P")) {
//            arePaths = true;
//            return;
//        }

        sc.nextLine();

        if (Arrays.asList(PHYSICAL).contains(dataKey))
            ParserAMPLouters(Arrays.asList(PHYSICAL).indexOf(dataKey), false);
        else if (Arrays.asList(VIRTUAL).contains(dataKey))
            ParserAMPLouters(Arrays.asList(VIRTUAL).indexOf(dataKey), true);
        else if (dataKey.equals(CAPACITY_KEY))
            parseCapacity(false);
        else if (dataKey.equals(vCAPACITY_KEY))
            parseCapacity(true);
    }

    private void setParameter(String routerName, int KEY, int param, int req) {
        while (req > requests.size())
            requests.add(new Request(vLink.class));
        Request request = requests.get(req - 1);
        vRouter router = (vRouter) getRouterByName(request, routerName);
        if (router == null) {
            router = new vRouter(routerName);
            requests.get(req - 1).addVertex(router);
        }
        if (KEY == 0)
            router.setPower(param);
        else if (KEY == 1)
            router.setMemory(param);
        else if (KEY == 2)
            router.addLocation(param);
    }

    private void setParameter(String routerName, int KEY, int param) {
        pRouter router = (pRouter) getRouterByName(graph, routerName);
        if (router == null) {
            router = new pRouter(routerName);
            graph.addVertex(router);
        }
        if (KEY == 0)
            router.setPower(param);
        else if (KEY == 1)
            router.setMemory(param);
        else if (KEY == 2)
            router.setLocation(param);
    }

    private void ParserAMPLouters(int KEY, boolean isVirtual) {
        int cnt = 0;
        int req = -1;
        while (true) {
            if (isVirtual) {// && KEY != 3) {
                String temp = sc.next();
                if (temp.equals(";"))
                    break;
                else if (temp.charAt(0) == '#') {
                    sc.nextLine();
                    continue;
                }
                req = Integer.parseInt(temp);
                if (req > cnt)
                    cnt = req;
            }
            String name = sc.next();
            if (name.charAt(0) == '#') {
                sc.nextLine();
                continue;
            } else if (name.equals(";"))
                break;
            int data;// = -1;
            if (sc.hasNext(Pattern.compile("\\d+;"))) {
                data = Integer.parseInt(sc.next().replace(";", ""));
            } else {
                data = sc.nextInt();
            }
            if (isVirtual) {
                if (KEY == 2)
                    data = sc.nextInt();
                setParameter(name, KEY, data, req);
            }
            else
                setParameter(name, KEY, data);
        }
        if (sc.hasNextLine())
            sc.nextLine();
    }

    private void addLink(String r1, String r2, int capacity, int req) {
        while (req > requests.size())
            requests.add(new Request(vLink.class));
        Request request = requests.get(req - 1);
        vRouter vR1 = (vRouter) getRouterByName(request, r1);
        vRouter vR2 = (vRouter) getRouterByName(request, r2);
        vLink link = new vLink(vR1, vR2, capacity, request);
        request.addLink(link);
    }

    private void addLink(String r1, String r2, int capacity) {
        pRouter pR1 = (pRouter) getRouterByName(graph, r1);
        pRouter pR2 = (pRouter) getRouterByName(graph, r2);
        new pLink(pR1, pR2, capacity, graph);
    }

    private void parseCapacity(boolean isVirtual) {
        int cnt = 0;
        int req = -1;
        while (true) {
            if (isVirtual) {
                String temp = sc.next();
                if (temp.equals(";"))
                    break;
                else if (temp.charAt(0) == '#') {
                    sc.nextLine();
                    continue;
                }
                req = Integer.parseInt(temp);
                if (req > cnt)
                    cnt = req;
            }
            String r1 = sc.next();
            if (r1.charAt(0) == '#') {
                sc.nextLine();
                continue;
            } else if (r1.equals(";"))
                break;
            String r2 = sc.next();
            int capacity;
            if (sc.hasNext(Pattern.compile("\\d+;"))) {
                capacity = Integer.parseInt(sc.next().replace(";", ""));
                if (isVirtual)
                    addLink(r1, r2, capacity, req);
                else
                    addLink(r1, r2, capacity);
                // loadData(new Object[]{sr1, sr2, capacity}, Writer.LINKS_KEY);
                break;
            } else {
                capacity = sc.nextInt();
            }
            if (isVirtual)
                addLink(r1, r2, capacity, req);
            else
                addLink(r1, r2, capacity);
            // loadData(new Object[]{sr1, sr2, capacity}, Writer.LINKS_KEY);
        }
        if (sc.hasNextLine())
            sc.nextLine();

    }

    public static Router getRouterByName(SimpleGraph<? extends Router, ? extends Link> graph, String routerName) {
        return graph.vertexSet().stream().filter(r -> r.getName().equals(routerName)).findAny().orElse(null);
    }

    public static Link getLinkByName(SimpleGraph<? extends Router, ? extends Link> graph, String linkName) {
        return graph.edgeSet().stream().filter(l -> l.getName().equals(linkName)).findAny().orElse(null);
    }
}
