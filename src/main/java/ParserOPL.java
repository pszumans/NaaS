import org.jgrapht.graph.SimpleGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Szuman on 11.05.2017.
 */
public class ParserOPL {

    private final String[] PHYSICAL = {"Vw", "Ee"};
    private final String[] VIRTUAL = {"Vv", "Ed"};

    private Scanner sc;

    public Network getNetwork() {
        return network;
    }

    private Network network;
    private List<Request> requests;

    public ParserOPL(String filename) throws FileNotFoundException {
        sc = new Scanner(new BufferedReader(new FileReader(new File(filename))));
    }

    public ParserOPL parse() {
        requests = new ArrayList<>();
        network = new Network(requests);

        sc.skip(Pattern.compile("=|\\}|<|>"));

        String text;
        while (sc.hasNextLine()) {
//            if (arePaths)
//                break;
            if (sc.hasNext()) {
                text = sc.next();
//                if (text.matches("param*"))
                if (text.equals("Vw"))
                    parseRouter();
                else if (text.equals("Ee"))
                    parseLinks();
                else if (text.equals("Vw"))
                    parseVRouter();
                else if (text.equals("Vw"))
                    parseVLinks();

            }
            else
                sc.nextLine();
        }
        sc.close();
        pLink.resetCounter();
        return this;
    }

    private void parseRouter() {
        String name;// = sc.next();
        while (!(name = sc.next()).equals("}")) {
            int power = sc.nextInt();
            int memory = sc.nextInt();
            int location = sc.nextInt();
            network.addVertex(new pRouter(name, power, memory, location));
        }
    }

    private void parseLinks() {
        String r1;
        while (!(r1 = sc.next()).equals("}")) {
            String r2 = sc.next();
            pRouter router1 = (pRouter) getRouterByName(network, r1);
            pRouter router2 = (pRouter) getRouterByName(network, r1);
            int capacity = sc.nextInt();
            new pLink(router1, router2, capacity, network);
        }
    }

    private void parseVRouter() {
        Request request = new Request(vLink.class);
        requests.add(request);
        while (true) {
            String name = sc.next();
            if (name.equals("}")) {
                request = new Request(vLink.class);
                requests.add(request);
                name = sc.next();
            } else if (name.equals(";"))
                break;
            int power = sc.nextInt();
            int memory = sc.nextInt();
            List<Integer> locations = new ArrayList<>();
//            sc.next(); // {
            String location;// = sc.next();
            while (!(location = sc.next()).equals("}"))
                locations.add(Integer.valueOf(location));
//            sc.next(); // }
            request.addVertex(new vRouter(name, power, memory, locations));
        }
    }

    private void parseVLinks() {
        int i = 0;
        Request request = requests.get(i++);
        while (true) {
            String r1 = sc.next();
            if (r1.equals("}")) {
                request = new Request(vLink.class);
                requests.add(request);
                r1 = sc.next();
            }
            if (r1.equals(";"))
                break;
            String r2 = sc.next();
            vRouter router1 = (vRouter) getRouterByName(request, r1);
            vRouter router2 = (vRouter) getRouterByName(request, r2);
            int capacity = sc.nextInt();
            new vLink(router1, router2, capacity, request);
        }
    }

    public static Router getRouterByName(SimpleGraph<? extends Router, ? extends Link> graph, String routerName) {
        return graph.vertexSet().stream().filter(r -> r.getName().equals(routerName)).findAny().orElse(null);
    }

}
