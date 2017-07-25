import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Szuman on 28.03.2017.
 */
public class SolverAMPL {
    
    private final String noInteger = "NO INTEGER FEASIBLE SOLUTION";
    private final String feasible = "INTEGER OPTIMAL SOLUTION FOUND";
    private final String infeasible = "NO PRIMAL FEASIBLE SOLUTION";
    
    private final String console = "C:\\Users\\Szuman\\Documents\\amplide.mswin64\\ampl.exe";// --cover
    private final String AMPLdirectory = "C:\\Users\\Szuman\\Documents\\amplide.mswin64\\";// --cover

    private final String batch = "C:\\Users\\Szuman\\Desktop\\NaaS.bat";

    private final String folder = "C:/Users/Szuman/Desktop/NaaS/";

    private final String outputToJson = "C:\\Users\\Szuman\\Desktop\\output.run";

    private final String runFile = folder + "NaaS.run";
    private final String modelFile = folder + "NaaS.mod";
    private final String dataPattern = folder + "NaaS_pattern.dat";
    private final String dataFile = folder + "NaaS.dat";
    private final String output = folder + "output/output";
    private final String txt = ".txt";
    private final String outputAll = folder + "outputAll.txt";
    private final String outputFile = folder + "output.txt";
    private final String dataFil = folder + "data/data";
    private final String dat = ".dat";
    private final String updateFile = folder + "update.txt";
    private final String resultFile = folder + "result.txt";

    private Network network;
    private List<Request> requests;

    public SolverAMPL(Network network) {
        this.network = network;
    }

    public SolverAMPL() throws FileNotFoundException {
        ParserAMPL parser = new ParserAMPL(dataPattern);
        parser.parse();
        network = parser.getGraph();
    }

    private void solve(String dataFile) throws IOException, InterruptedException {
        new Writer(runFile).writeRun(modelFile, dataFile, outputToJson);
        new Writer(batch).writeBatch(AMPLdirectory, runFile);
        Process p = Runtime.getRuntime().exec(console);
        consolePrint(p);
        p.waitFor();
    }

    private void solve(int i) throws IOException, InterruptedException {
        String dataFile = makeDataFile(i);
        new Writer(dataFile).writeData(network);
        new Writer(runFile).writeRun(modelFile, dataFile, outputToJson);
        new Writer(batch).writeBatch(AMPLdirectory, runFile);
        Process p = Runtime.getRuntime().exec("cmd /c start " + batch);
        p.waitFor();
    }

    public void sequenceSolve() throws IOException, InterruptedException {
        initResultFile();
        int i = 0;
        while (isSolved()) {
            Request random = Request.getRandom(3,2, 1);
            network.setRequests(Arrays.asList(random));
            requests.add(random);
            solve(i++);
        }
        network.setRequests(requests);
        new Writer(dataFile).writeData(network);
        solve(dataFile);
    }

    private void initResultFile() throws IOException {
        new Writer(resultFile).write("");
    }

    private boolean isSolved() {
        boolean isSolved = true;
        String result;
        try {
            result = new ParserAMPL(resultFile).scanResult();
        } catch (FileNotFoundException e) {
            return true;
        }
        if (result.equals("solved"))
            isSolved = true;
        else if (result.equals("infeasible"))
            isSolved = false;
        return isSolved;
    }

    private void consolePrint(Process p) throws IOException {

        String line;

        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = bri.readLine()) != null) {
            System.out.println(line);
        }
        bri.close();
        while ((line = bre.readLine()) != null) {
            System.out.println(line);
        }
        bre.close();
        // System.out.println("Done.");
    }

    private String makeDataFile(int i) {
        return dataFil + i + dat;
    }

    private String getOutputFile(int i) {
        return output + i + txt;
    }
}
