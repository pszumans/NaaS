import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import ilog.concert.IloException;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Szuman on 17.03.2017.
 */
public class Main {

//    public static void test() throws IOException {
//        Parser p = new Parser("C:\\Users\\Szuman\\Desktop\\NaaS\\NaaS.dat");
////        Parser p = new Parser("C:\\Users\\Szuman\\Desktop\\NaaS\\data'.dat");
//        System.out.println(1);
//        p.parse();
//        System.out.println(2);
////        Network n = new Network(p.getRouters(), p.getLinks(), p.getVRouters(), p.getVLinks());
////        Network n = new Network(p.getGraph(), p.getVRouters(), p.getVLinks());
////        Network n = p.getGraph().update(p.getRequests());
//        Network n = p.getGraph();//.update();
//        System.out.println(3);
////        n.serveRequests();
////        System.out.println(n.edgeSet().stream().mapToInt(l -> ((pLink)l).getSubstrateCapacity()).min().getAsInt());
////        System.out.println(n.getUsedCapacity());
////        System.out.println(n.getMaxSubstrateCapacity());
//        //        n.getRequests().forEach(r -> System.out.println(r.isServed()));
////        n.getRequests().forEach(r -> System.out.println(r));
//        new Writer("C:\\Users\\Szuman\\Desktop\\TXT.txt").writeOPL(n);
////        new GraphVisualisation(n).start();
////        Writer w = new Writer("C:\\Users\\Szuman\\Desktop\\l.txt");
////        w.writeData(n);
////        n.serveRequests();
////        n.getRouters().forEach(l -> System.out.println(l));
////        n.getLinks().forEach(l -> System.out.println(l));
////        IntStream.range(0, n.reqStatus.size()).forEach(i -> System.out.println("#" + i + ": " + n.reqStatus.get(i)));
////        Deser.graph = n;
//    }
//
//    public static void test1() {
//        try {
//            ObjectMapper om = new ObjectMapper();
//            om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
////            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
////                    false);
//            SimpleGraph graph = new SimpleGraph(vLink.class);
//            vRouter r1 = new vRouter("R1", 10,10, Arrays.asList(1));
//            vRouter r2 = new vRouter("R2", 10,10, Arrays.asList(1));
//            graph.addVertex(r1);
//            graph.addVertex(r2);
////            om.writeValue(new File("C:\\Users\\Szuman\\Desktop\\l.txt"),
//////              new Request(vLink.class));
////                    new vLink(
////                            r1
////                            , r2
////                            ,10
////                            , graph
////                    )
////            );
////            vLink l = om.readValue(new File("C:\\Users\\Szuman\\Desktop\\r.txt"), vLink.class);
////            Request[] r = om.readValue(new File("C:\\Users\\Szuman\\Desktop\\l.txt"), Request[].class);
////            Deser r = om.readValue(new File("C:\\Users\\Szuman\\Desktop\\l.txt"), Deser.class);
////            vRouter l = om.readValue(new File("C:\\Users\\Szuman\\Desktop\\Request.txt"), vRouter.class);
////            om.writeValue(new File("C:\\Users\\Szuman\\Desktop\\l.txt"), l);
////            System.out.println(l);
////            System.out.println(Arrays.asList(r));
////            r.forEach(tr -> System.out.println(tr));
//            String filename = "C:\\Users\\Szuman\\Desktop\\r.txt";
//            String vR = "C:\\Users\\Szuman\\Desktop\\vRouters.txt";
//            String vL = "C:\\Users\\Szuman\\Desktop\\vLinks.txt";
//            Writer.replaceInFile(vR, "'", "\"", ", ]", "]", "},\\s+}", "}}", ",\\s+}", "}");
//            Writer.replaceInFile(vL, "'", "\"", ", ]", "]", "},\\s+}", "}}", ",\\s+}", "}");
//            Map<String, Map<Integer, vRouter>> vRouters = om.readValue(new File(vR), new TypeReference<Map<String, Map<Integer, vRouter>>>(){});
//            Map<String, Map<Integer, vLink>> vLinks = om.readValue(new File(vL), new TypeReference<Map<String, Map<Integer, vLink>>>(){});
////  Network n = om.readValue(new File("C:\\Users\\Szuman\\Desktop\\r.txt"), Network.class);
////            System.out.println(n.getLinks());
////            pRouter p = new pRouter("W1");
////            p.setVRouters(vRouters.get("W1"));
////            System.out.println(p);
//            System.out.println(vRouters);
//            System.out.println(vLinks);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void test2() throws IOException, InterruptedException {
//        String console = "C:\\Users\\Szuman\\Documents\\amplide.mswin64\\ampl.exe";
//        Process p = Runtime.getRuntime().exec("cmd /c start " + console);
//        String line;
//        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//        while ((line = bri.readLine()) != null) {
//            System.out.println(line);
//        }
//        bri.close();
//        while ((line = bre.readLine()) != null) {
//            System.out.println(line);
//        }
//        bre.close();
//        p.waitFor();
//    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String filename = "C:\\Users\\Szuman\\Desktop\\NaaS\\NaaS.dat";
        new Test(filename).run();
    }
}
