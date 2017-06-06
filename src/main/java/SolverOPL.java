//import ilog.concert.*;
//import ilog.cplex.IloCplex;
//import ilog.opl.*;
//
//import java.util.*;
//
///**
// * Created by Szuman on 13.05.2017.
// */
//public class SolverOPL {
//
//    private IloOplDataElements dataElements;
//    private IloOplModel model;
////    private IloOplRunConfiguration RC;
//    private String DATADIR = "D:/Naas";
//
//    private IloTupleSet Vw;
//    private IloTupleSet Ee;
//    private IloTupleSetMap Vv;
//    private IloTupleSetMap Ed;
//
//    private Network network;
//
//    public SolverOPL() throws IloException {
//
//
//            solve();
//            toNetwork();
//
//
//    }
//
//    private pRouter addRouter(int index) {
//        IloTuple router = Vw.makeTuple(index);
//        String name = router.getStringValue("name");
//        int Bw = router.getIntValue("Bw");
//        int Mw = router.getIntValue("Mw");
//        int Lw = router.getIntValue("Lw");
//        pRouter pRouter = new pRouter(name, Bw, Mw, Lw);
//        network.addVertex(pRouter);
//        return pRouter;
//    }
//
//    private vRouter addVRouter(int req, int index) throws IloException {
//        IloTuple v = Vv.get(req).makeTuple(index);
////                    System.out.println(v);
//        String vname = v.getStringValue("name");
//        int Bv = v.getIntValue("Bv");
//        int Mv = v.getIntValue("Mv");
//        IloIntSet L = v.getIntSetValue("Lv");
//        List<Integer> Lv = new ArrayList<>();
//        for (int l = 0; l < L.getSize(); l++)
//            Lv.add(L.getValue(l));
//        vRouter vRouter = new vRouter(vname, Bv, Mv, Lv);
//        if (network.getRequests().size() < req)
//            network.addRequest(new Request(vLink.class));
//        Request request = network.getRequests().get(req - 1);
//        request.addVertex(vRouter);
//        return vRouter;
//    }
//
//    private pLink addLink(int index) {
//        IloTuple link = Ee.makeTuple(index);
//        pRouter r1 = (pRouter) ParserOPL.getRouterByName(network, link.getStringValue("source"));
//        pRouter r2 = (pRouter) ParserOPL.getRouterByName(network, link.getStringValue("target"));
//        int Ce = link.getIntValue("Ce");
//
//        return new pLink(r1, r2, Ce, network);
//    }
//
//    private vLink addVLink(int req, int index) throws IloException {
//        IloTuple dd = Ed.get(req).makeTuple(index);
//        vRouter r1 = (vRouter) ParserOPL.getRouterByName(network.getRequests().get(req - 1), dd.getStringValue("source"));
//        vRouter r2 = (vRouter) ParserOPL.getRouterByName(network.getRequests().get(req - 1), dd.getStringValue("target"));
//        int Cd = dd.getIntValue("Cd");
//        Request request = network.getRequests().get(req - 1);
//        return new vLink(r1, r2, Cd, request);
//    }
//
//    private void solve() throws IloException {
//        IloOplFactory.setDebugMode(false);
//        IloOplFactory oplF = new IloOplFactory();
//        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
//        IloOplSettings settings = oplF.createOplSettings(errHandler);
//
//        // make master model
//        IloCplex cplex = oplF.createCplex();
//        cplex.setOut(null);
//
//        IloOplRunConfiguration RC = oplF.createOplRunConfiguration(
//                DATADIR +
//                        "/NaaS.mod", DATADIR +
//                        "/NaaS.dat");
//        RC.setCplex(cplex);
//        model =
//        RC.getOplModel();
//        model.generate();
//        dataElements = RC.getOplModel()
//                .makeDataElements();
//
////        masterCplex.clearModel();
////
////        IloOplRunConfiguration masterRC = oplF.createOplRunConfiguration(
////                masterRC0.getOplModel().getModelDefinition(),
////                masterDataElements);
////        masterRC.setCplex(masterCplex);
////        masterRC.getOplModel().generate();
////
//
//        int status;
//
//        System.out.println("Solve master.");
//        if (cplex.solve()) {
//            double curr = cplex.getObjValue();
////            String str = masterCplex.
//            int win = RC.getOplModel().getElement("OCCUPIED").asInt();
//            IloIntMap y = RC.getOplModel().getElement("y").asIntMap();
//            System.out.println("FULL CAPACITY: "  + win);
//            int aux = RC.getOplModel().getElement("aux").asInt();
//            System.out.println("AUX: " + aux);
////            System.out.println(occ);
//            System.out.println("OBJECTIVE: " + curr);
//            double time = cplex.getCplexTime();
//            System.out.println("TIME: " + time);
//
//            status = 0;
//        } else {
//            System.out.println("No solution!");
//            status = 1;
//        }
//    }
//
//    private void toNetwork() throws IloException {
//        network = new Network();
//
//        Vw = dataElements.getElement("Vw").asTupleSet();
//        Vv = dataElements.getElement("Vv").asTupleSetMap();
//        Ee = dataElements.getElement("Ee").asTupleSet();
//        Ed = dataElements.getElement("Ed").asTupleSetMap();
//        IloTupleSet Xwv = model.getElement("Xwv").asTupleSet();
//        IloIntMap Xvw = model.getElement("Xvw").asIntMap();
//        IloTupleSet Pst = dataElements.getElement("Pst").asTupleSet();
//        IloTupleSet Upd = model.getElement("Upd").asTupleSet();
//        IloIntMap Udp = model.getElement("Udp").asIntMap();
//
//        int x = 0;
//        boolean routersAdded = false;
//        List<pLink> linksList = new ArrayList<>();
//        for (int r = 1; r <= model.getElement("R").asInt(); r++) {
//            for (int v = 0; v < Vv.get(r).getSize(); v++) {
//                vRouter vRouter = addVRouter(r, v);
//
//                for (int w = 0; w < Vw.getSize(); w++) {
//                    pRouter pRouter = null;
//                    if (!routersAdded) {
//                        pRouter = addRouter(w);
//                    }
//
//                    IloTuple xvw = Xwv.makeTuple(x++);
//                    if (Xvw.get(xvw) ==  1) {
//                        if (pRouter == null) {
//                            pRouter = (pRouter) ParserOPL.getRouterByName(network, Vw.makeTuple(w).getStringValue("name"));
//                        }
//                        pRouter.serveRequest(1, vRouter);
//                    }
//                }
//                routersAdded = true;
//            }
//
//            int u = 0;
//            for (int d = 0; d < Ed.get(r).getSize(); d++) {
//
//                vLink vLink = addVLink(r, d);
//
//                for (int p = 0; p < Pst.getSize(); p++) {
//                    IloTuple pp = Pst.makeTuple(p);
//                    IloIntMap dep = pp.getIntMapValue("Dep");
//
//                    IloTuple udp = Upd.makeTuple(u++);
//
//                    if (Udp.get(udp) == 1) {
//                        System.out.println(dep);
////                            int e = 0;
//                        for (int e = 0; e < Ee.getSize(); e++) {
////                                pLink pLink = null;
//                            if (linksList.size() <= e) {
//                                linksList.add(addLink(e));
//                            }
//                            pLink pLink = linksList.get(e);
//                            if (dep.get(Ee.makeTuple(e)) == 1) {
//                                pLink.serveRequest(r, vLink);
//                                System.out.println("PUK PUK: " + pLink);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        System.out.println(network);
//        new GraphVisualisation(network).start();
//
////            IloIntMap y = masterRC0.getOplModel().getElement("y").asIntMap();
////            System.out.println(y);
//
//
//
//    }
//}
