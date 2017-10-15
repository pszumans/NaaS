import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        setSettings(args[0]);
    }

    private static void matchResults()  {
        String dir = "D:\\NaaS\\TEST\\logs\\" + Exec.SEED +"\\rand\\bigV\\";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir + "results" + Exec.SEED + ".csv"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Integer> list = Arrays.asList(20,25,30,35);
        for (int i = 0; i < 4; i++) {
            String template;
            if (i == 0)
                template = dir + "results_google_%s.csv";
            else if (i == 1){
                pw.println("HEURISTIC" + "\t" + "serviceRate" + "\t"
                        + "solverTime" + "\t"
                        + "requestsSize" + "\t"
                        + "usedCapacity" + "\t"
                        + "maxMinCapacity" + "\t"
                        + "capacityLoadRate" + "\t"
                        + "powerLoadRate" + "\t"
                        + "memoryLoadRate" + "\t"
                        + "loadRate");
                template = dir + "heuristic_" + "results_google_%s.csv";
            } else if (i == 2) {
                pw.println("DESCENDING");
                template = dir + "heuristic_desc_" + "results_google_%s.csv";
            } else {
                pw.println("ASCENDING");
                template = dir + "heuristic_asc_" + "results_google_%s.csv";
            }
            Scanner sc;
            for (Integer num : list) {
                String line;
                String numS = String.valueOf((double)num / 100).replace(".",",");
                try {
                    sc = new Scanner(new BufferedReader(new FileReader(new File(String.format(template, num)))));
                    line = numS + "\t" + sc.nextLine().replace(".",",");
                    sc.close();
                } catch (FileNotFoundException e) {
                    line = numS + "\t";
                }
                pw.println(line);
            }
        }
        pw.close();
        System.out.println("end");
    }

    private static void block(String... args) {
        if (args[0].equals("0.20")) {
            vLink.BIG_VARIANCE = true;
        } else if (args[0].equals("0.25")) {
            vLink.BIG_VARIANCE = false;
        } else if (args[0].equals("0.30")) {
            Request.FULL_RANDOM = true;
        } else {
            System.out.println("WRONG");
            return;
        }

        Log.DIR += ("run\\" + Exec.SEED + "\\" + ((Request.FULL_RANDOM) ? "full" : "rand") + "_" + ((vLink.BIG_VARIANCE) ? "bigV" : "smallV") + "\\" + ((Network.IS_HEURISTIC) ? "heuristic_" : ""));
//        Log.DIR += ("run" + "\\" + ((Request.FULL_RANDOM) ? "full" : "rand") + "\\" + ((vLink.BIG_VARIANCE) ? "bigV" : "smallV") + "\\" + Exec.SEED + ((Network.IS_HEURISTIC) ? "_heuristic_" : "_"));
        if (!Network.IS_HEURISTIC)
            Log.DIR += ((SolverOPL.HYBRID) ? "hybrid_" : (SolverOPL.SEQ) ? "seq_" : "");
        else
            Log.DIR += ((Heuristic.type == Heuristic.Type.DESCENDING) ? "desc_" : (Heuristic.type == Heuristic.Type.ASCENDING) ? "asc_" : "");

    }

    private static void setSettings(String arg) {
        //        String small = SolverOPL.DATADIR + "simple.dat";
//        String standard = SolverOPL.DATADIR + "NaaS.dat";
//        String google = SolverOPL.DATADIR + "google.dat";
//        String newGoogle = SolverOPL.DATADIR + "NewGoogle.dat";
//
//        SolverOPL.HYBRID = true;

//        Exec.SEED = 1;
//        matchResults();
//        Exec.SEED = 11;
//        matchResults();
//        Exec.SEED = 111;
//        matchResults();

//        Network.IS_HEURISTIC = true;
//        vLink.BIG_VARIANCE = true;
        Request.FULL_RANDOM = true;
//        Log.DIR += "heuristic_";


//        Network.IS_HEURISTIC = true;
//        Heuristic.type = Heuristic.Type.ASCENDING;
//        Heuristic.type = Heuristic.Type.DESCENDING;
//
        SolverOPL.SEQ = true;
//        SolverOPL.HYBRID = true;

//        Exec.SEED = Long.valueOf(args[1]);

//        if (args[1].equals("1"))
//            Exec.SEED = 7;
//        else if (args[1].equals("11"))
//            Exec.SEED = 77;
//        else if (args[1].equals("111"))
        Exec.SEED = 777;

        if (!arg.equals("0.20"))
            return;
//
//        Log.DIR += ("rate20\\" + Exec.SEED + "\\" + /*((Request.FULL_RANDOM) ? "full" : "rand") + "\\" + */((vLink.BIG_VARIANCE) ? "bigV" : "smallV") + "\\" + ((Network.IS_HEURISTIC) ? "heuristic_" : ""));
//        Log.DIR += ((Heuristic.type == Heuristic.Type.DESCENDING) ? "desc_" : (Heuristic.type == Heuristic.Type.ASCENDING) ? "asc_" : "");

//        block(args);

        Exec.CSV = Log.DIR + "%s_%s_CSV.csv";
        Exec.LOG = Log.DIR + "%s_%s_LOG.txt";
        SolverOPL.DEBUG = Log.DIR + "%s_%s_DEBUG.txt";
        Log.consoleLog = Log.DIR + "%s_%s_LOG.txt";

        String google = SolverOPL.DATADIR + "google.dat";

//        Arrays.asList(google).stream().forEach(f -> {
        try {
            new Exec(google)
//                        .run();
                    .singleLambda(
//                                0.2);
                            Double.valueOf(arg));
            Log.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        });
//        new Exec(filename)
//                .seq();
//                .run();
//                .lambda();
//                .singleLambda(Double.valueOf(args[0]), 1);
//        System.out.println(Runtime.getRuntime().availableProcessors());
//        Log.close();
    }

}
