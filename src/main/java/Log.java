import lombok.Getter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Log {

    public static String DIR = "D:\\NaaS\\Exec\\logs\\";

    public static String consoleLog = DIR + "%s_%s_LOG.csv";
    private static PrintWriter consoleWriter;
    public static PrintWriter resultsWriter;

    public static Map<String, LogWriter> out;

    private static void put(String filename) throws IOException {
        out.put(filename, new LogWriter(filename));
    }

    public static void logF(String filename, String msg) {
        if (out == null)
            out = new HashMap<>();
        if (!out.containsKey(filename))
            try {
                put(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        out.get(filename).log(msg);
//        log(msg);
    }

    public static void log(String filename, String msg) throws IOException {
        logF(filename, msg);
        log(msg);
    }

    public static void log(String filename, String... msgs) throws IOException {
        String msg;
        if (filename.endsWith(".csv"))
            msg = toCSV(filename, msgs);
        else {
            msg = String.join(" ", msgs);
            logF(filename, msg);
        }
        log(msg);
    }

    public static String toCSV(String filename, String... msgs) {
        if (out == null)
            out = new HashMap<>();
        if (!out.containsKey(filename)) {
            try {
                put(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            out.get(filename).log(Exec.Pattern);
        }
        out.get(filename).log(msgs);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msgs.length; i++) {
            sb.append(Exec.Pattern[i]).append(" = ").append(msgs).append(" ");
        }
        return sb.toString().replace(".", ",");
    }

    public static String toCSV(String filename, Object... msgs) {
        String[] strings = new String[msgs.length];
        IntStream.range(0 , strings.length).forEach(i -> strings[i] = String.valueOf(msgs[i]));
        return toCSV(filename, strings);
    }

    public static void log(Object obj) {
        log(String.valueOf(obj));
    }

    public static void log(String msg) {
        if (consoleWriter == null)
            try {
                consoleWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(consoleLog))), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        String message = new SimpleDateFormat("[HH:mm:ss] ").format(new Date().getTime()) + msg;
        System.out.println(message);
        consoleWriter.println(message);
    }

    public static void toResults(String name, double lambda, String msg) {
        if (resultsWriter == null)
            try {
                resultsWriter = new PrintWriter(new FileOutputStream(new File(Log.DIR + "results_" + name + "_" + (int)(lambda * 100) + ".csv")), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        String message = new SimpleDateFormat("[HH:mm:ss] ").format(new Date().getTime()) + msg;
        System.out.println(message);
        resultsWriter.println(msg);
    }

    public static void close() {
        if (consoleWriter != null)
            consoleWriter.close();
        if (resultsWriter != null)
            resultsWriter.close();
        if (out != null)
            out.values().forEach(s -> s.close());
    }

    public static void flush() {
        close();
        consoleWriter = null;
        out = null;
    }

    @Getter
    private static class LogWriter extends PrintWriter {

        public LogWriter(String filename) throws IOException {
            this(filename, true);
//            pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename))));
        }

        public LogWriter(String filename, boolean append) throws IOException {
            super(new BufferedWriter(new FileWriter(new File(filename))), append);
//            pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(filename))));
        }

        public void log(String msg) {
            println(getTime() + msg);
        }

        private void log(String[] msgs) {
            String msg = String.join("\t", msgs).replace(".",",");
            println(msg);
        }

        private String getTime() {
            return new SimpleDateFormat("[HH:mm:ss] ").format(new Date().getTime());
        }

    }
}
