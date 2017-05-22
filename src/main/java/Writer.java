import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Writer extends PrintWriter {

    private final String SETS[] = { "Vw", "Ee" };
    private final String vSETS[] = { "V", "Vv", "E", "Ed" };

    private final String PARAMS[] = { "Bw", "Mw", "Lw" };
    private final String vPARAMS[] = { "Bv", "Mv", "L", "Lv" };

    private final String CAPACITY = "Ce";
    private final String vCAPACITY = "Cd";

    private Network graph;

    public void setGraph(Network graph) {
        this.graph = graph;
    }

    public Writer(String filename) throws IOException {
        this(filename, false);
    }

    public Writer(String filename, boolean append) throws IOException {
        super(new BufferedWriter(new FileWriter(new File(filename), append)));
    }

    public void writeLn(String text) {
        write(text + "\n");
    }

    public void writeOutputFile(String file) {
        write("param output := \"" + file + "\";\n\n");
    }

    public void writePaths(List<PathEnds> paths) {

        write("\nparam P := ");
        write("\n");
        paths.forEach(p -> write(p + "\n"));
        write(";\n");

        write("\nset Pst := ");
        write("\n");

        paths.forEach(pathEnds ->
                pathEnds.getPaths().forEach(p ->
                        write(p + "\n")));


        write(";\n\n");
        write("param Dep default 0 := ");
        write("\n");

        paths.forEach(pathEnds ->
                pathEnds.getPaths().forEach(p ->
                        write(p.getDelta())));
        write(";\n");
        flush();
    }

    public void writeOPL(Network g) {
        graph = g;
        int size = graph.edgeSet().size();
        graph.getActualPaths().forEach(
                pathEnds -> {
                    pathEnds.getPaths().forEach(p -> {
                        int[] temp = new int[size];
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
        close();
    }

    public void writeVirtual(Network graph) {
        setGraph(graph);
        writeVSets();
        writeVRoutersParams();
        writeVLinksCapacity();
    }

    public void writeData(Network graph) {
        setGraph(graph);
        writeSets();
        writeVSets();
        writeRoutersParams();
        writeLinksCapacity();
        writeVRoutersParams();
        writeVLinksCapacity();
        writePaths(graph.getPaths());
        close();
    }

    private void writeSets() {
        write("set " + SETS[0] + " :=");
        graph.getRouters().forEach(r -> write(" " + r.getName()));

        write(";\n");
        write("set " + SETS[1] + " :=");
        graph.getLinks().forEach(l -> write(" " + l.getSigned()));
        write(";\n");
    }

    private void writeVSets() {
        write("\nparam R := " + graph.getRequests().size() + ";\n\n");
        write("set " + vSETS[0] + " :=");
        graph.getRequests().forEach(r -> write(r.getRoutersNames()));
        write(";\n\n");

        graph.getRequests().forEach(r -> {
            write("set " + vSETS[0] + "[" + (r.getIndex() + 1) + "] :=");
            write(r.getRoutersNames() + ";\n");
        });
        write("\n\n");

        write("set " + vSETS[1] + " :=");
        graph.getRequests().forEach(r -> write(r.getLinksNames()));
        write(";\n\n");

        graph.getRequests().forEach(r -> {
            write("set " + vSETS[1] + "[" + r.getIndex() + "] :=");
            write(r.getLinksNames() + ";\n");
        });
        write("\n\n");
    }

    private void writeRoutersParams() {
        IntStream.range(0, PARAMS.length).forEach(i -> {
            write("param " + PARAMS[i] + " := \n");
            graph.getRouters().forEach(r -> write(r.getParam(i)));
            write(";\n\n");
        });
    }

    private void writeVRoutersParams() {
        IntStream.range(0, vPARAMS.length).forEach(i -> {
            write("param " + vPARAMS[i] + " := \n");
            graph.getRequests().forEach(r -> write(r.getRoutersParam((i))));
            write(";\n\n");
        });
    }

    private void writeLinksCapacity() {
        write("param " + CAPACITY + " := \n");
        graph.getLinks().forEach(l -> write(l.getParam()));
        write(";\n\n");
    }

    private void writeVLinksCapacity() {
        write("param " + vCAPACITY + " := \n");
        graph.getRequests().forEach(r -> write(r.getLinksParam()));

        write(";\n\n");
    }

    public void closend() {
        write("end;\n");
        close();
    }

    public void writeRun(String model, String data, String output) {
        write("model " + model + ";\n");
        write("data " + data + ";\n");
        write("option solver cplex;\n");
        write("solve;" + "\n");
        write("include " + output + ";");
        close();
    }

    public void writeBatch(String directory, String run) {
        write("cd " + directory + "\n");
        write("ampl " + run + "\n");
        write("pause");
        close();
    }

    public static void replaceInFile(String filename, String... strings) throws IOException {
        java.nio.file.Path filepath = java.nio.file.Paths.get(filename);
        String content = new String(Files.readAllBytes(filepath));
        for (int i = 0; i < strings.length; i += 2)
            content = content.replaceAll(strings[i], strings[i+1]);
        Files.write(filepath, content.getBytes());
    }
}
