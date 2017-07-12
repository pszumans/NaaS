import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Test {

    private String filename;

    public Test(String filename) {
        this.filename = filename;
    }

    public void run() throws FileNotFoundException {
        Network network = new ParserOPL(filename).parse().getNetwork();
        /*
        try {
            RequestService req = new ObjectMapper().readValue(
//                    new File("C:\\Users\\Szuman\\Desktop\\req.json")
                    "{\"input\":false,\"data\":\"p\"}"
                    , RequestService.class);//.getRequest();
            System.out.println(req.getRequest());
            System.out.println(req.getReqName());
//            new ObjectMapper().writeValue(System.out, network.getRequests().get(0).getLinks());
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
//        List<List<RequestService>> list = new ArrayList<>();
//        RequestService rs = new RequestService(3,3);
//        RequestService rs2 = new RequestService(3,3);
//        RequestService sr = new RequestService(1);
//        list.add(Arrays.asList(rs, rs2, sr));
//        list.forEach(l -> IntStream.range(0, l.size()).forEach(i -> {network.addRequestService(l.get(i));
        network.serveRequests();
//        }));
//        network.addRequestService(rs);
//        network.serveRequests();
//        network.addRequest(Request.getRandomRequest(3,3));
//        network.serveRequests();
        GraphVisualisation gV = new GraphVisualisation(network);
        gV.start();
//        new Scanner(System.in).nextLine();
//        gV.saveToFile("C:\\Users\\Szuman\\Desktop\\graph.png");
        System.out.println(network.getUsedCapacity());
        System.out.println(network.getMaxSubstrateCapacity());
        System.out.println(network.getLocations());
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
        System.out.println(network.getLocations());
    }
}
