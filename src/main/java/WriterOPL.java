import java.io.*;
import java.util.Arrays;

/**
 * Created by Szuman on 16.05.2017.
 */
public class WriterOPL extends PrintWriter {

    private final String SETS[] = { "Vw", "Ee" };
    private final String vSETS[] = { "V", "Vv", "E", "Ed" };

    private final String PARAMS[] = { "Bw", "Mw", "Lw" };
    private final String vPARAMS[] = { "Bv", "Mv", "L", "Lv" };

    private final String CAPACITY = "Ce";
    private final String vCAPACITY = "Cd";

    private Network network;

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

    public void writeLn(String text) {
        write(text + "\n");
    }

    private void writeRouters() {
        write("Routers = { ");
        network.getRouters().forEach(r -> write(r.getName() + " "));
        write("} ;\n\n");
        writeVw();
    }

    private void writeVw() {
        write("Vw = { ");
        network.getRouters().forEach(r -> write(r.toOPL()));
        write(" } ;\n\n");
    }

    private void writeLinks() {
        write("Ee = { ");
        network.getLinks().forEach(l -> write(l.toOPL()));
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
            req.vertexSet().forEach(r -> write(((Router)r).getName() + " "));
            write(" }\n");
        });
        write(" ];\n\n");
        writeVv();
    }

    private void writeVv() {
        write("Vv = [ ");
        network.getRequests().forEach(req -> {
            write("{ ");
            req.vertexSet().forEach(r -> write(((vRouter)r).toOPL() + " "));
            write(" }\n");
        });
        write(" ];\n\n");
    }

    private void writeVLinks() {
        write("Ed = [ ");
        network.getRequests().forEach(req -> {
            write("{ ");
            req.edgeSet().forEach(l -> write(((vLink)l).toOPL() + " "));
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




}
