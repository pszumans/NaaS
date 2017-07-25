import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Szuman on 16.05.2017.
 */
public class WriterOPL {

    private Network network;
    private PrintWriter pw;
    private StringBuilder dataText;

    private int cnt = 0;

    public int getCnt() {
        return cnt;
    }

    private List<Request> requests;

    public void setGraph(Network network) {
        this.network = network;
    }

    public WriterOPL(Network network) {
        this.network = network;
        requests = network.getRequests();
    }

    public WriterOPL(String filename, Network network) throws IOException {
        this(filename, false);
        this.network = network;
        requests = network.getRequests();
    }

    public WriterOPL(String filename, boolean append) throws IOException {
        pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename), append)));
    }

    public WriterOPL(String filename) throws IOException {
        pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename))));
    }

    public void write(String s) {
        if (pw == null)
            dataText.append(s);
        else
            pw.write(s);
    }

    public String getDataString() {
        return dataText.toString();
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
        write("R = " + requests.size());
        write(";\n\n");
    }


    private void writeVRouters() {
        write("vRouters = [ ");
        requests.forEach(req -> {
            write("{ ");
            req.vertexSet().forEach(r -> write(((Router) r).getName() + " "));
            write(" }\n");
        });
        write(" ];\n\n");
        writeVv();
    }

    private void writeVv() {
        write("Vv = [ ");
        requests.forEach(req -> {
            write("{ ");
            req.vertexSet().forEach(r -> write(((vRouter) r).toOPL() + " "));
            write(" }\n");
        });
        write(" ];\n\n");
    }

    private void writeVLinks() {
        write("Ed = [ ");
        requests.forEach(req -> {
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
                        write("<" + p.toOPL() + " ");
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

    public WriterOPL writeOPL() {
        if (pw == null) dataText = new StringBuilder();
        writeRouters();
        writeLinks();
        writeRequests();
        writePaths();
        if (pw != null) pw.close();
        return this;
    }

    public WriterOPL writeSeq() {
        if (pw == null) dataText = new StringBuilder();
        requests = Arrays.asList(network.getRequests().get(cnt++));
        writeRouters(false);
        writeLinks(false);
        writeRequests();
        writePaths();
        if (pw != null) pw.close();
        return this;
    }

    public WriterOPL writeSeq(Request request) {
        if (pw == null) dataText = new StringBuilder();
        requests = Arrays.asList(request);
        writeRouters(false);
        writeLinks(false);
        writeRequests();
        writePaths();
        if (pw != null) pw.close();
        return this;
    }

    private void writeRequests() {
        writeRequestsSize();
        writeVRouters();
        writeVLinks();
    }
/*
    private void writeRequest(Request request) {

//        write("R = " + network.getRequests().size());
        write("R = 1");
        write(";\n\n");

//        Request request = network.getRequests().get(0);
//        Request request = requests.get(index - 1);

        write("vRouters = [ ");
//        network.getRequests().forEach(request -> {
        write("{ ");
        request.vertexSet().forEach(r -> write(((Router) r).getName() + " "));
        write(" }\n");
//        });
        write(" ];\n\n");

        write("Vv = [ ");
//        network.getRequests().forEach(request -> {
        write("{ ");
        request.vertexSet().forEach(r -> write(((vRouter) r).toOPL() + " "));
        write(" }\n");
//        });
        write(" ];\n\n");

        write("Ed = [ ");
//        network.getRequests().forEach(request -> {
        write("{ ");
        request.edgeSet().forEach(l -> write(((vLink) l).toOPL() + " "));
        write(" }\n");
//        });
        write(" ];\n\n");

    }
    */
}