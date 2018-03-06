import lombok.Getter;
import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

@Getter
public class ParserOPL {

    private final String[] PHYSICAL = {"Vw", "Ee"};
    private final String[] VIRTUAL = {"Vv", "Ed"};

    private Scanner sc;

    private Network network;
    private List<Request> requests;

    public ParserOPL(String filename) throws FileNotFoundException {
        sc = new Scanner(new BufferedReader(new FileReader(new File(filename))));
    }

    public ParserOPL parse() {
        requests = new ArrayList<>();
        network = new Network(requests);

        sc.useDelimiter("(\\s*>?\\s*//.*\\s*)+<?\\s*|\\s*<?\\s*>?\\s+(\\s*=?\\s*\\[?\\s*\\{?\\s*<?\\s*>?\\s*]?\\s*)?");

        String text;
        while (
                sc.hasNext()) {
            text = sc.next();
            if (text.equals("Vw"))
                parseRouters();
            else if (text.equals("Ee"))
                parseLinks();
            else if (text.equals("Vv"))
                parseVRouters();
            else if (text.equals("Ed"))
                parseVLinks();
            else if (text.equals("Pst"))
                break;
            else if (sc.hasNextLine())
                sc.nextLine()
                        ;
        }
        sc.close();
        PLink.resetCounter();
        return this;
    }

    private void parseRouters() {
        String name;
        while (!(name = sc.next()).equals("}")) {
            int power = 0;
            int memory = 0;
            int location = 0;
            try {
                power = sc.nextInt();
                memory = sc.nextInt();
                location = sc.nextInt();
            } catch (java.util.InputMismatchException e) {
                System.out.println(sc.next());
            }
            network.addVertex(new PRouter(name, power, memory, location, network));
            network.addLocation(location, power, memory);
        }
    }

    private void parseLinks() {
        String r1;
        while (!(r1 = sc.next()).equals("}")) {
            String r2 = sc.next();
            PRouter router1 = (PRouter) getRouterByName(network, r1);
            PRouter router2 = (PRouter) getRouterByName(network, r2);
            int capacity = sc.nextInt();
            new PLink(router1, router2, capacity, network);
        }
    }

    private void parseVRouters() {
        Request request = new Request(VLink.class);
        String name = sc.next();
        while (true) {
            int power = sc.nextInt();
            int memory = sc.nextInt();
            Set<Integer> locations = new HashSet<>();
            String location;// = sc.next();
            while (!(location = sc.next()).equals("}"))
                locations.add(Integer.valueOf(location));
            request.addVertex(new VRouter(name, power, memory, locations));
            if ((name = sc.next()).equals("}")) {
                requests.add(request);
                request = new Request(VLink.class);
                if ((name = sc.next()).equals(";")) {
                    break;
                }
            }
        }
    }

    private void parseVLinks() {
        int i = 0;
        Request request = requests.get(i);
        String r1 = sc.next();
        while (true) {
            String r2 = sc.next();
            VRouter router1 = (VRouter) getRouterByName(request, r1);
            VRouter router2 = (VRouter) getRouterByName(request, r2);
            int capacity = sc.nextInt();
            request.addLink(new VLink(router1, router2, capacity, request));
            if ((r1 = sc.next()).equals("}")) {
                if ((r1 = sc.next()).equals(";")) {
                    break;
                }
                request = requests.get(++i);
            }
        }
    }

    public static Router getRouterByName(SimpleGraph<? extends Router, ? extends Link> graph, String routerName) {
        return graph.vertexSet().stream().filter(r -> r.getName().equals(routerName)).findAny().orElse(null);
    }

}
