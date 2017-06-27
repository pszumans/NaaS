import java.io.FileNotFoundException;
import java.util.Scanner;

public class Test {

    private String filename;

    public Test(String filename) {
        this.filename = filename;
    }

    public void run() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
        network.serveRequests();
        GraphVisualisation gV = new GraphVisualisation(network);
        gV.start();
//        new Scanner(System.in).nextLine();
//        gV.saveToFile("C:\\Users\\Szuman\\Desktop\\graph.png");
        System.out.println(network.getUsedCapacity());
        System.out.println(network.getMaxSubstrateCapacity());
        System.out.println(Network.locations);
    }

    public void seq() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
        Request.resetCounter();
        network.getRequests().replaceAll(r -> Request.getRandomRequest(3,3));
        network.serveRequests();
        GraphVisualisation gV = new GraphVisualisation(network);
        gV.start();
//        gV.saveToFile("C:\\Users\\Szuman\\Desktop\\graph.png");
        System.out.println(network.getUsedCapacity());
        System.out.println(network.getMaxSubstrateCapacity());
        System.out.println(Network.locations);

    }
}
