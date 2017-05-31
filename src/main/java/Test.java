import java.io.FileNotFoundException;

public class Test {

    private String filename;

    public Test(String filename) {
        this.filename = filename;
    }

    public void run() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
        network.serveRequests();
        new GraphVisualisation(network).start();
        System.out.println(network.getUsedCapacity());
    }
}
