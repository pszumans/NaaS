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
//    private IloOplFactory oplF;
//    private IloCplex cplex;
//    private IloOplModel model;
//    private IloOplDataElements dataElements;
//
//    private WriterOPL wr;
//    private Request seqReq;
//
//    //    private IloOplRunConfiguration RC;
//    public static String DATADIR = "D:/Naas/TEST1/";
//
//    public static int FULL;
//    public static double TIME;
//
//    private IloTupleSet Vw;
//    private IloTupleSet Ee;
//    private IloTupleSetMap Vv;
//    private IloTupleSet Xwv;
//    private IloIntMap Xvw;
//    private IloTupleSet Pst;
//    private IloTupleSet Upd;
//    private IloIntMap Udp;
//
//    private IloTupleSetMap Ed;
//    private int reqCounter = 0;
//    private boolean isSeq = false;
//
//    public Network getNetwork() {
//        return network;
//    }
//
//    private Network network;
//
//    public SolverOPL() throws IloException {
//        solve();
//        toNetwork();
//    }
//
//    public SolverOPL(Network network) throws IloException {
//        this.network = network;
////        init();
////        setData(DATADIR + "NaaS.dat");
//    }
//
//    public SolverOPL(Network network, boolean isSeq) throws IloException {
//        this.network = network;
//        this.isSeq = isSeq;
////        init();
////        solve();
////        toNetwork();
//    }
//
//    private pRouter getRouter(int index) {
//        IloTuple router = Vw.makeTuple(index);
//        String name = router.getStringValue("name");
//        pRouter pRouter = (pRouter) ParserOPL.getRouterByName(network, name);
//        if (pRouter != null)
//            return pRouter;
//        int Bw = router.getIntValue("Bw");
//        int Mw = router.getIntValue("Mw");
//        int Lw = router.getIntValue("Lw");
//        pRouter = new pRouter(name, Bw, Mw, Lw, network);
//        network.addVertex(pRouter);
//        network.addLocation(Lw, Bw + Mw);
//        return pRouter;
//    }
//
//    private vRouter getVRouter(int req, int index) throws IloException {
//        IloTuple v = Vv.get(req).makeTuple(index);
//        String name = v.getStringValue("name");
//        vRouter vRouter = (network.getRequests().size() >= req) ? (vRouter) ParserOPL.getRouterByName(network.getRequests().get(req - 1), name) : null;
//        if (isSeq)
//            return (vRouter) ParserOPL.getRouterByName(seqReq, name);
////        if (isSeq) req = reqCounter;
//        if (vRouter != null)
//            return vRouter;
//        int Bv = v.getIntValue("Bv");
//        int Mv = v.getIntValue("Mv");
//        IloIntSet L = v.getIntSetValue("Lv");
//        List<Integer> Lv = new ArrayList<>();
//        for (int l = 0; l < L.getSize(); l++)
//            Lv.add(L.getValue(l));
//        vRouter = new vRouter(name, Bv, Mv, Lv);
//        if (network.getRequests().size() < req)
//            network.addRequest(new Request(vLink.class));
//        Request request = network.getRequests().get(req - 1);
//        request.addVertex(vRouter);
//        return vRouter;
//    }
//
//    private pLink getLink(int index) {
//        IloTuple link = Ee.makeTuple(index);
//        pRouter r1 = (pRouter) ParserOPL.getRouterByName(network, link.getStringValue("source"));
//        pRouter r2 = (pRouter) ParserOPL.getRouterByName(network, link.getStringValue("target"));
//        int Ce = link.getIntValue("Ce");
//
//        if (network.containsEdge(r1, r2))
//            return (pLink) network.getEdge(r1, r2);
//
//        return new pLink(r1, r2, Ce, network);
//    }
//
//    private vLink getVLink(int req, int index) throws IloException {
//        IloTuple dd = Ed.get(req).makeTuple(index);
////        if (isSeq) req = reqCounter;
//        vRouter r1 = (vRouter) ParserOPL.getRouterByName(network.getRequests().get(req - 1), dd.getStringValue("source"));
//        vRouter r2 = (vRouter) ParserOPL.getRouterByName(network.getRequests().get(req - 1), dd.getStringValue("target"));
//
//        if (isSeq) return (vLink) seqReq.getEdge(r1, r2);
//
//        Request request = network.getRequests().get(req - 1);
//
//        if (request.containsEdge(r1, r2))
//            return (vLink) request.getEdge(r1, r2);
//
//        int Cd = dd.getIntValue("Cd");
//
//        return new vLink(r1, r2, Cd, request);
//    }
//
//    public static String toDat(int index) {
//        return "NaaS" + index + ".dat";
//    }
//
//    public void solve(int index) throws IloException {
////        IloOplFactory.setDebugMode(false);
////        IloOplFactory oplF = new IloOplFactory();
////        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
////        IloOplSettings settings = oplF.createOplSettings(errHandler);
////
////        IloCplex cplex = oplF.createCplex();
////        cplex.setOut(null);
////
////        IloOplRunConfiguration RC = oplF.createOplRunConfiguration(
////                DATADIR +
////                        "NaaS.mod", DATADIR +
////                        toDat(index));
////        RC.setCplex(cplex);
////        model =
////                RC.getOplModel();
////        model.generate();
////        dataElements = RC.getOplModel()
////                .makeDataElements();
////
////        oplF.createOplDataSourceFromString("","");
//
//        System.out.println("Solve master.");
//        if (cplex.solve()) {
//            double curr = cplex.getObjValue();
//            int win = model.getElement("OCCUPIED").asInt();
//            IloIntMap y = model.getElement("y").asIntMap();
//            System.out.println("CAPACITY: "  + win);
//            System.out.println(y);
//            int aux = model.getElement("aux").asInt();
////            double aux = model.getElement("aux").asNum();
//            System.out.println("AUX: " + aux);
//            System.out.println("OBJECTIVE: " + curr);
//            double time = cplex.getCplexTime();
//            System.out.println("TIME: " + time);
//
//            FULL += win;
//            TIME = time;
//            System.out.println("FULL = " + FULL + "; TIME = " + TIME);
//        } else {
//            System.out.println("No solution!");
//        }
//
//        toNetwork();
//    }
//
//    public void serveRequest(Request request) throws IloException {
//        network.addRequest(request);
////        WriterOPL wr = null;
//        if (isSeq) {
//            wr = new WriterOPL(network).writeSeq(request);
//            seqReq = request;
//        }
//        else
//            wr = new WriterOPL(network).writeOPL();
//        setData(wr.getDataString());
//        solve();
//        toNetwork();
//        new GraphVisualisation(network);
//    }
//
//    private void init() throws IloException {
//        IloOplFactory.setDebugMode(true);
//        if (oplF == null)
//            oplF = new IloOplFactory();
//        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
//        IloOplSettings settings = oplF.createOplSettings(errHandler);
//
//        // make master model
//        cplex = oplF.createCplex();
//        cplex.setOut(System.out);
//
//        IloOplModelSource modelSource = oplF.createOplModelSource("D:/NaaS/NaaS.mod");
//        IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource, settings);
//        model = oplF.createOplModel(def, cplex);
//
//    }
//
//    public void setData() throws IloException {
//        if (isSeq)
//            wr = new WriterOPL(network).writeSeq();
//        else
//            wr = new WriterOPL(network).writeOPL();
//        setData(wr.getDataString());
//    }
//
//    public void setData(String text) throws IloException {
//        init();
//        IloOplDataSource ds;
//        if (text.endsWith(".dat"))
//            ds = oplF.createOplDataSource(text);//,filename.replace(".dat", ""));
//        else
//            ds = oplF.createOplDataSourceFromString(text, "");
//        model.addDataSource(ds);
//        model.generate();
//        dataElements = model.makeDataElements();
//    }
//
//    public void solve() throws IloException {
////        IloOplFactory.setDebugMode(false);
////        IloOplFactory oplF = new IloOplFactory();
////        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
////        IloOplSettings settings = oplF.createOplSettings(errHandler);
////
////         make master model
////        cplex = oplF.createCplex();
////        cplex.setOut(null);
//
////        IloOplRunConfiguration RC = oplF.createOplRunConfiguration(
////                DATADIR +
////                        "NaaS.mod", DATADIR +
////                        "NaaS.dat");
////        RC.setCplex(cplex);
////        model =
////                RC.getOplModel();
////        model.generate();
//////        dataElements = RC.getOplModel()
//////                .makeDataElements();
////
////        IloOplDataSource ds = oplF.createOplDataSourceFromString("","");
////        model.addDataSource(ds);
//
////        RC = oplF.createOplRunConfiguration("modPath");
//
////        IloCP cp = oplF.createCP();
////        IloOplModel opl=oplF.createOplModel(def,cp);
//
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
////        System.out.println(dataElements.getElement("Vw").asTupleSet().makeFirst().getIntValue(2)
////        .makeFirst().getStringValue("name")
////        );
//
//
//        System.out.println("Solve master.");
//        if (cplex.solve()) {
////            double curr = cplex.getObjValue();
////            int win = model.getElement("OCCUPIED").asInt();
////            IloIntMap y = model.getElement("y").asIntMap();
////            System.out.println("FULL CAPACITY:");
////            System.out.println(y);
////            IloTupleSet ww = model.getElement("Vw").asTupleSet();
////            System.out.println(ww);
////            IloTupleSetMap w = model.getElement("Vv").asTupleSetMap();
////            System.out.println(w);
////            System.out.println(model.getElement("R").asInt());
////            IloTupleSet wv = model.getElement("Vv").asTupleSet();
////            System.out.println(wv);
////            int aux = model.getElement("aux").asInt();
////            System.out.println("AUX: " + aux);
////            System.out.println(occ);
////            System.out.println("OBJECTIVE: " + curr);
//            double time = cplex.getCplexTime();
////            System.out.println("TIME: " + time);
//        } else {
//            System.out.println("No solution!");
//        }
//
////        toNetwork();
//
////        if (isSeq && network.getRequests().size() > wr.getCnt()) {
////            if (wr == null)
////                wr = new WriterOPL(network);
////            setData(wr.writeSeq().getDataString());
////            System.out.println(wr.getDataString());
////            solve();
////        }
//    }
//
//    private void getDataElements() {
//        Vw = dataElements.getElement("Vw").asTupleSet();
//        Vv = dataElements.getElement("Vv").asTupleSetMap();
//        Ee = dataElements.getElement("Ee").asTupleSet();
//        Ed = dataElements.getElement("Ed").asTupleSetMap();
//        Xwv = model.getElement("Xwv").asTupleSet();
//        Xvw = model.getElement("Xvw").asIntMap();
//        Pst = dataElements.getElement("Pst").asTupleSet();
//        Upd = model.getElement("Upd").asTupleSet();
//        Udp = model.getElement("Udp").asIntMap();
//    }
//
//    public void toNetwork() throws IloException {
//        if (network == null)
//            network = new Network();
//
//        getDataElements();
//
//        reqCounter = 0;
//
//        int x = 0;
//        int u = 0;
//        boolean routersAdded = false;
//        List<pLink> linksList = new ArrayList<>(network.getLinks());
//        for (int r = 1; r <= dataElements.getElement("R").asInt(); r++) {
//            reqCounter++;
//            System.out.println("REQ: " + reqCounter + "/" + dataElements.getElement("R").asInt());
//            for (int v = 0; v < Vv.get(r).getSize(); v++) {
//                vRouter vRouter = getVRouter(r, v);
//
//                for (int w = 0; w < Vw.getSize(); w++) {
//                    pRouter pRouter = null;
//                    if (!routersAdded) {
//                        pRouter = getRouter(w);
//                    }
//
//                    IloTuple xvw = Xwv.makeTuple(x++);
//                    if (Xvw.get(xvw) ==  1) {
//                        if (pRouter == null) {
//                            pRouter = (pRouter) ParserOPL.getRouterByName(network, Vw.makeTuple(w).getStringValue("name"));
//                        }
//                        pRouter.serveRequest(reqCounter, vRouter);
//                    }
//                }
//                routersAdded = true;
//            }
//
//            for (int d = 0; d < Ed.get(r).getSize(); d++) {
//
//                vLink vLink = getVLink(r, d);
//
//                for (int p = 0; p < Pst.getSize(); p++) {
//                    IloTuple pst = Pst.makeTuple(p);
//                    IloIntMap dep = pst.getIntMapValue("Dep");
//
//                    IloTuple udp = Upd.makeTuple(u++);
//
//                    if (Udp.get(udp) == 1) {
//                        for (int e = 0; e < Ee.getSize(); e++) {
//                            if (linksList.size() <= e) {
//                                linksList.add(getLink(e));
//                            }
//                            pLink pLink = linksList.get(e);
//                            if (dep.get(Ee.makeTuple(e)) == 1) {
////                                System.out.println(pLink.getName() + " " + pLink.getVisualText());
//                                pLink.serveRequest(r, vLink);
//                                network.addUsedCapacity(vLink.getCapacity());
////                                System.out.println(pLink.getName() + " " + pLink.getVisualText());
//                            }
//                        }
//                    }
//                }
//            }
//        }
////        System.out.println(network);
//        network.setSolverTime(TIME);
////        network.addServedRequest(0);
//        pLink.resetCounter();
////        System.out.println(network);
//        new GraphVisualisation(network).start();
//
//    }
//
////    private void serveSeqReq(Request request) throws IloException {
////
////        getDataElements();
////
////        int x = 0;
////        int u = 0;
//////        boolean routersAdded = false;
////        List<pLink> linksList = new ArrayList<>(network.getLinks());
//////            reqCounter++;
//////            System.out.println("REQ: " + reqCounter + "/" + dataElements.getElement("R").asInt());
////        for (int v = 0; v < Vv.get(1).getSize(); v++) {
////            vRouter vRouter = (vRouter) ParserOPL.getRouterByName(request, Vv.get(1).makeTuple(v).getStringValue("name"));
////
////            for (int w = 0; w < Vw.getSize(); w++) {
////                IloTuple xvw = Xwv.makeTuple(x++);
////                if (Xvw.get(xvw) == 1) {
//////                        if (pRouter == null) {
////                    pRouter pRouter = (pRouter) ParserOPL.getRouterByName(network, Vw.makeTuple(w).getStringValue("name"));
//////                        }
////                    pRouter.serveRequest(request.getIndex(), vRouter);
////                }
////            }
//////                routersAdded = true;
////            }
////
////            for (int d = 0; d < Ed.get(1).getSize(); d++) {
////
////                IloTuple dd = Ed.get(1).makeTuple(d);
//////        if (isSeq) req = reqCounter;
////                vRouter r1 = (vRouter) ParserOPL.getRouterByName(request, dd.getStringValue("source"));
////                vRouter r2 = (vRouter) ParserOPL.getRouterByName(request, dd.getStringValue("target"));
////
////                vLink vLink = (vLink) request.getEdge(r1, r2);
////
////                for (int p = 0; p < Pst.getSize(); p++) {
////
////                    IloTuple pst = Pst.makeTuple(p);
////                    IloIntMap dep = pst.getIntMapValue("Dep");
////
////                    IloTuple udp = Upd.makeTuple(u++);
////
////                    if (Udp.get(udp) == 1) {
////                        for (int e = 0; e < Ee.getSize(); e++) {
////                            pLink pLink = linksList.get(e);
////                            if (dep.get(Ee.makeTuple(e)) == 1) {
//////                                System.out.println(pLink.getName() + " " + pLink.getVisualText());
////                                pLink.serveRequest(request.getIndex(), vLink);
////                                network.addUsedCapacity(vLink.getCapacity());
//////                                System.out.println(pLink.getName() + " " + pLink.getVisualText());
////                            }
////                        }
////                    }
////                }
////            }
////        }
//}
