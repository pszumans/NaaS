import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Szuman on 16.05.2017.
 */
public class WriterOPL extends PrintWriter {

    private final String SETS[] = {"Vw", "Ee"};
    private final String vSETS[] = {"V", "Vv", "E", "Ed"};

    private final String PARAMS[] = {"Bw", "Mw", "Lw"};
    private final String vPARAMS[] = {"Bv", "Mv", "L", "Lv"};

    private final String CAPACITY = "Ce";
    private final String vCAPACITY = "Cd";

    private Network network;

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    private List<Request> requests;

    public void setGraph(Network network) {
        this.network = network;
    }

    public WriterOPL(String filename, Network network) throws IOException {
        this(filename, false);
        this.network = network;
    }

    public WriterOPL(String filename, boolean append) throws IOException {
        super(new BufferedWriter(new FileWriter(new File(filename), append)));
    }

    public WriterOPL(String filename) throws IOException {
        super(new BufferedWriter(new FileWriter(new File(filename))));
    }

    public void writeLn(String text) {
        write(text + "\n");
    }

    private void writeRouters() {
        writeRouters(true);
    }

    private void writeRouters(boolean isFull) {
        write("Routers = { ");
        network.getRouters().forEach(r -> write(r.getName() + " "));
        write("} ;\n\n");
        writeVw(isFull);
    }

//    private void writeVw() {
//        writeVw(true);
//    }

    private void writeVw(boolean isFull) {
        write("Vw = { ");
        network.getRouters().forEach(r -> write(r.toOPL(isFull)));
        write(" } ;\n\n");
    }

    private void writeLinks() {
        writeLinks(true);
    }

    private void writeLinks(boolean isFull) {
        write("Ee = { ");
        network.getLinks().forEach(l -> write(l.toOPL(isFull)));
        write(" } ;\n\n");
    }

    private void writeRequestsSize() {
        write("R = " + network.getRequests().size());
        write(";\n\n");
    }

    private void writeVRouters() {
        write("vRouters = [ ");
        network.getRequests().forEach(req -> {
            write("{ ");
            req.vertexSet().forEach(r -> write(((Router) r).getName() + " "));
            write(" }\n");
        });
        write(" ];\n\n");
        writeVv();
    }

    private void writeVv() {
        write("Vv = [ ");
        network.getRequests().forEach(req -> {
            write("{ ");
            req.vertexSet().forEach(r -> write(((vRouter) r).toOPL() + " "));
            write(" }\n");
        });
        write(" ];\n\n");
    }

    private void writeVLinks() {
        write("Ed = [ ");
        network.getRequests().forEach(req -> {
            write("{ ");
            req.edgeSet().forEach(l -> write(((vLink) l).toOPL() + " "));
            write(" }\n");
        });
        write(" ];\n\n");
    }

    private void writePaths() {
        write("Pst = { \n");
        network.getActualPaths().forEach(
                pathEnds -> {
                    pathEnds.getPaths().forEach(p -> {
                        int[] temp = new int[network.edgeSet().size()];
                        write("<" + p + " ");
                        p.getEdgeList().forEach(l -> {
//                            System.out.println(l.getIndex());
                            temp[l.getIndex() - 1] = 1;
//                            write(l.getName().replace(" ", "_") + " ");
//                            write("Dep[" + l.getIndex() + "] = 1 ");
//                            write(l.getIndex() + " ");
                        });
                        write(Arrays.toString(temp));
                        write(">\n");
                    });
                });
        write(" } ;");
    }

    public void writeOPL() {
        writeRouters();
        writeLinks();
        writeRequestsSize();
        writeVRouters();
        writeVLinks();
        writePaths();
        close();
    }

    public void writeSeq(int index) {
        writeRouters(false);
        writeLinks(false);
//        writeRequestsSize();
//        writeVRouters();
//        writeVLinks();
        writeRequest(index);
        writePaths();
        close();
    }

    private void writeRequest(int index) {

//        write("R = " + network.getRequests().size());
        write("R = 1");
        write(";\n\n");

//        Request req = network.getRequests().get(index - 1);
        Request req = requests.get(index - 1);

        write("vRouters = [ ");
//        network.getRequests().forEach(req -> {
        write("{ ");
        req.vertexSet().forEach(r -> write(((Router) r).getName() + " "));
        write(" }\n");
//        });
        write(" ];\n\n");

        write("Vv = [ ");
//        network.getRequests().forEach(req -> {
        write("{ ");
        req.vertexSet().forEach(r -> write(((vRouter) r).toOPL() + " "));
        write(" }\n");
//        });
        write(" ];\n\n");

        write("Ed = [ ");
//        network.getRequests().forEach(req -> {
        write("{ ");
        req.edgeSet().forEach(l -> write(((vLink) l).toOPL() + " "));
        write(" }\n");
//        });
        write(" ];\n\n");

    }
}