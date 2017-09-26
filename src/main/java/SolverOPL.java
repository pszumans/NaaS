import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.opl.*;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;

/**
 * Created by Szuman on 13.05.2017.
 */
@Getter
@Setter
public class SolverOPL extends Solver {

    public static boolean SEQ;
    public static boolean HYBRID;

    private IloOplFactory oplF;
    private IloCplex cplex;
    private IloOplModel model;
    private IloOplDataElements dataElements;
    private IloOplDataSource dataSource;

    private WriterOPL wr;
    private Request seqReq;

    private Map<Integer, Network.Location> locs;// = new HashMap<>();

    //    private IloOplRunConfiguration RC;
    public static String DATADIR = "D:/Naas/TEST/";
    public static String DEBUG = Log.DIR + "%s_%s_DEBUG.txt";
    private FileOutputStream debugStream;
    private PrintWriter debugWriter;

    private IloTupleSet Vw;
    private IloTupleSet Ee;
    private IloTupleSetMap Vv;
    private IloTupleSet Xwv;
    private IloIntMap Xvw;
    private IloTupleSet Pst;
    private IloTupleSet Upd;
    private IloIntMap Udp;
    private IloTupleSetMap Ed;

    private int reqPointer;// = 0;
    private boolean isSeq;// = false;
    private int freeCapacity = Integer.MAX_VALUE;
    private IloOplModelSource modelSource;
    private IloOplModelDefinition def;
    private IloOplErrorHandler errHandler;
    private IloOplSettings settings;

    private Map<String, Map<Integer, vRouter>> backupVRouters;
    private Map<String, Map<Integer, List<vLink>>> backupVLinks;

    public SolverOPL(Network network) throws IloException {
        super(network);
        if (HYBRID) SEQ = true;
        isSeq = SEQ;
    }

    public SolverOPL(Network network, boolean isSeq) throws IloException {
        super(network);
        this.isSeq = isSeq;
//        init();
//        solve();
//        toNetwork();
    }

    private pRouter getRouter(int index) {
        IloTuple router = Vw.makeTuple(index);
        String name = router.getStringValue("name");
        pRouter pRouter = (pRouter) ParserOPL.getRouterByName(network, name);
        if (pRouter != null)
            return pRouter;
        int Bw = router.getIntValue("Bw");
        int Mw = router.getIntValue("Mw");
        int Lw = router.getIntValue("Lw");
        pRouter = new pRouter(name, Bw, Mw, Lw, network);
        network.addVertex(pRouter);
        network.addLocation(Lw, Bw, Mw);
        return pRouter;
    }

    private vRouter getVRouter(int req, int index) throws IloException {
        IloTuple v = Vv.get(req).makeTuple(index);
        String name = v.getStringValue("name");
        vRouter vRouter = (network.getRequests().size() >= req) ? (vRouter) ParserOPL.getRouterByName(network.getRequests().get(req - 1), name) : null;
        if (seqReq != null) {
            reqPointer = seqReq.getIndex();
            return (vRouter) ParserOPL.getRouterByName(seqReq, name);
        }
//        if (isSeq) req = reqPointer;
        if (vRouter != null) {
            reqPointer = network.getRequests().get(req - 1).getIndex();
            return vRouter;
        }
        int Bv = v.getIntValue("Bv");
        int Mv = v.getIntValue("Mv");
        IloIntSet L = v.getIntSetValue("Lv");
        Set<Integer> Lv = new HashSet<>();
        for (int l = 0; l < L.getSize(); l++)
            Lv.add(L.getValue(l));
        vRouter = new vRouter(name, Bv, Mv, Lv);
        if (network.getRequests().size() < req)
            network.addRequest(new Request(vLink.class));
        Request request = network.getRequests().get(req - 1);
        reqPointer = request.getIndex();
        request.addVertex(vRouter);
        return vRouter;
    }

    private pLink getLink(int index) {
        IloTuple link = Ee.makeTuple(index);
        pRouter r1 = (pRouter) ParserOPL.getRouterByName(network, link.getStringValue("source"));
        pRouter r2 = (pRouter) ParserOPL.getRouterByName(network, link.getStringValue("target"));
        int Ce = link.getIntValue("Ce");

        if (network.containsEdge(r1, r2))
            return (pLink) network.getEdge(r1, r2);

        return new pLink(r1, r2, Ce, network);
    }

    private vLink getVLink(int req, int index) throws IloException {
        IloTuple dd = Ed.get(req).makeTuple(index);
        Request request = (seqReq != null) ? seqReq : network.getRequests().get(req - 1);

        vRouter r1 = (vRouter) ParserOPL.getRouterByName(request, dd.getStringValue("source"));
        vRouter r2 = (vRouter) ParserOPL.getRouterByName(request, dd.getStringValue("target"));

        if (request.containsEdge(r1, r2))
            return (vLink) request.getEdge(r1, r2);

        int Cd = dd.getIntValue("Cd");

        return new vLink(r1, r2, Cd, request);
    }

    public static String toDat(int index) {
        return "NaaS" + index + ".dat";
    }

    @Override
    public void releaseRequest(Request request) {
        network.getRouters().forEach(r -> r.removeRequest(request.getIndex()));
        network.getLinks().forEach(r -> r.removeRequest(request.getIndex()));
        super.releaseRequest(request);
    }

    public void releaseAllRequests() {
//        if (HYBRID) {
//            backupVRouters = new HashMap<>();
//            backupVLinks = new HashMap<>();
//        }
        network.getRouters().stream().filter(r -> r.getRequests() != null).forEach(r -> {
//            Map<Integer,vRouter> vRouters = r.getVRouters();
//            if (HYBRID && vRouters != null) backupVRouters.put(r.getName(), new HashMap<>(vRouters));
            Set<Integer> reqs = new HashSet<>(r.getRequests().keySet());
            reqs.forEach(req -> r.removeRequest(req));
        });
        network.getLinks().stream().filter(l -> l.getRequests() != null).forEach(l -> {
//            Map<Integer,List<vLink>> vLinks = l.getVLinks();
//            if (HYBRID && vLinks != null) backupVLinks.put(l.getName(), new HashMap<>(vLinks));
            Set<Integer> reqs = new HashSet<>(l.getRequests().keySet());
            reqs.forEach(req -> l.removeRequest(req));
        });
    }


    private boolean isOverLoaded(Request request) {
//        boolean result = false;
//        while (!result) {
        if (locs == null) return false;
        Log.log(locs);
        return
//            result =
                request.vertexSet().stream().anyMatch(v -> ((vRouter) v).getPower() > locs.values().stream().filter(e -> ((vRouter) v).getLocations().contains(e.getIndex())).mapToInt(e -> e.getSubstratePower()).max().getAsInt())
                        || request.vertexSet().stream().anyMatch(v -> ((vRouter) v).getPower() > locs.values().stream().filter(e -> ((vRouter) v).getLocations().contains(e.getIndex())).mapToInt(e -> e.getSubstrateMemory()).max().getAsInt())
                        || request.edgeSet().stream().anyMatch(l -> ((vLink) l).getCapacity() > freeCapacity);

//        }
//        int powerNet = network.vertexSet().stream().filter(r -> getRouter().getLocation())
//        return false;
    }

    @Override
    public boolean serveRequest(Request request) {
//        int maxNet = network.getLinks().stream().mapToInt(l -> l.getSubstrateCapacity()).sum();//getMaxMinSubstrateCapacity();
//        int maxReq = request.getLinks().stream().mapToInt(vLink::getCapacity).sum();//.getAsInt();
//        Log.log("REQ: " + maxReq + " vs  NET: " + freeCapacity + " ");
//        Log.log(network.getLocations());
//        if (isOverLoaded(request)) {
//            Log.log("OVERLOAD - don't wanna solve");
//            return false;
//        }
        Log.log(request);
        network.addRequest(request);
        if (isSeq) {
            wr = new WriterOPL(network).writeSeq(request);
            seqReq = request;
        } else {
            wr = new WriterOPL(network).writeOPL();
//            releaseAllRequests();
        }
        try {
            setData(wr.getDataString());
            Log.log("Solving...");
            debugWriter.println("\n" + "########## " + request.getIndex() + "\n");
            if (cplex.solve()) {
                Log.log("Solved!");
                solveLoadBalancing();
                updateTime();
//                freeCapacity = network.getLinks().stream().mapToInt(l -> l.getSubstrateCapacity()).max().getAsInt();//getMaxMinSubstrateCapacity();
                if (!isSeq)
                    releaseAllRequests();
                toNetwork();
                network.countUsedCapacity();
                if (HYBRID) isSeq = true;
//                udpdateLocs();
                return super.serveRequest(request);
            } else {
                super.releaseRequest(request);
                Log.log("Can't solve.");
//                Log.logF(Log.DIR + "error.txt", wr.getDataString());
                if (HYBRID && isSeq) {
                    Log.log("Trying full optimal...");
//                    new GraphVisualisation(network).start();
                    isSeq = false;
                    seqReq = null;
                    return serveRequest(request);
                } else if (HYBRID && !isSeq) {
                    isSeq = true;
//                    getFromBackup();
                }
                updateTime();
                return false;
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        super.releaseRequest(request);
        return false;
//        new GraphVisualisation(network);
    }

    private void solveLoadBalancing() throws IloException {
//        System.out.println("SOL = " + cplex.getObjValue());
//        System.out.println(cplex.getObjective().getExpr());
        cplex.addEq(model.getElement("OCCUPIED").asIntExpr(), cplex.getObjValue(), "Best");//.getExpr();
        cplex.getObjective().setExpr(model.getElement("MINIMUM").asIntExpr());
        cplex.getObjective().setSense(IloObjectiveSense.Maximize);
        cplex.setParam(IloCplex.DoubleParam.TiLim, 100);
//        System.out.println(cplex.getObjective().getExpr());
        Log.log("Load Balancing...");
        cplex.solve();
        Log.log("Balanced.");
//        Log.log("MAX_MIN = " + cplex.getObjValue());
    }

    private void updateTime() {
        time = cplex.getCplexTime() - fullTime;
        Log.log("TIME per REQ = " + time);
        fullTime = cplex.getCplexTime();
        Log.log("FULL TIME = " + fullTime);
    }

    private void udpdateLocs() {
        if (locs == null) locs = new HashMap<>();
        for (int i = 1; i <= vRouter.LOC_MAX; i++) {
            final int location = i;
            int power = network.vertexSet().stream().filter(r -> ((pRouter) r).getLocation() == location).mapToInt(r -> ((pRouter) r).getSubstratePower()).max().getAsInt();
            int memory = network.vertexSet().stream().filter(r -> ((pRouter) r).getLocation() == location).mapToInt(r -> ((pRouter) r).getSubstrateMemory()).max().getAsInt();
            if (locs.containsKey(i)) {
                locs.get(i).setSubstratePower(power);
                locs.get(i).setSubstrateMemory(memory);
            } else
                locs.put(location, network.new Location(location, power, memory));
        }
    }

    private void init() throws IloException {
        if (oplF != null) {
            end();
        } else {
            IloOplFactory.setDebugMode(true);
            try {
                File log = new File(DEBUG);
                if (!log.exists())
                    log.createNewFile();
                debugStream = new FileOutputStream(log);
                debugWriter = new PrintWriter(debugStream, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        oplF = new IloOplFactory();
        cplex = oplF.createCplex();
        cplex.setOut(debugStream);
//        cplex.setOut(System.out);
        cplex.setParam(IloCplex.IntParam.Threads, 1);
        cplex.setParam(IloCplex.DoubleParam.TiLim, 1000);
        errHandler = oplF.createOplErrorHandler();
        settings = oplF.createOplSettings(errHandler);
        modelSource = oplF.createOplModelSource("D:/NaaS/NaaS.mod");
        def = oplF.createOplModelDefinition(modelSource, settings);
        model = oplF.createOplModel(def, cplex);
    }

    public void setData() throws IloException {
        if (isSeq)
            wr = new WriterOPL(network).writeSeq();
        else
            wr = new WriterOPL(network).writeOPL();
        setData(wr.getDataString());
    }

    public void setData(String text) throws IloException {
        init();
//            text = "D:\\NaaS\\NaaS.dat";
        if (text.endsWith(".dat"))
            dataSource = oplF.createOplDataSource(text);//,filename.replace(".dat", ""));
        else
            dataSource = oplF.createOplDataSourceFromString(text, "");
        model.addDataSource(dataSource);
        model.generate();
        dataElements = model.makeDataElements();
    }

    private void getDataElements() {
        Vw = dataElements.getElement("Vw").asTupleSet();
        Vv = dataElements.getElement("Vv").asTupleSetMap();
        Ee = dataElements.getElement("Ee").asTupleSet();
        Ed = dataElements.getElement("Ed").asTupleSetMap();
        Xwv = model.getElement("Xwv").asTupleSet();
        Xvw = model.getElement("Xvw").asIntMap();
        Pst = dataElements.getElement("Pst").asTupleSet();
        Upd = model.getElement("Upd").asTupleSet();
        Udp = model.getElement("Udp").asIntMap();
    }

    public void toNetwork() throws IloException {

        getDataElements();

        int x = 0;
        int u = 0;
        boolean routersAdded = false;
        List<pLink> linksList = new ArrayList<>(network.getLinks());
        for (int r = 1; r <= dataElements.getElement("R").asInt(); r++) {
//            reqPointer++;
//            Log.log("REQ: " + reqPointer + "/" + dataElements.getElement("R").asInt());
            for (int v = 0; v < Vv.get(r).getSize(); v++) {
                vRouter vRouter = getVRouter(r, v);

                for (int w = 0; w < Vw.getSize(); w++) {
                    pRouter pRouter = null;
                    if (!routersAdded) {
                        pRouter = getRouter(w);
                    }

                    IloTuple xvw = Xwv.makeTuple(x++);
                    if (Xvw.get(xvw) == 1) {
                        if (pRouter == null) {
                            pRouter = (pRouter) ParserOPL.getRouterByName(network, Vw.makeTuple(w).getStringValue("name"));
                        }
                        pRouter.serveRequest(reqPointer, vRouter);
                    }
                }
                routersAdded = true;
            }

            for (int d = 0; d < Ed.get(r).getSize(); d++) {

                vLink vLink = getVLink(r, d);

                for (int p = 0; p < Pst.getSize(); p++) {
                    IloTuple pst = Pst.makeTuple(p);
                    IloIntMap dep = pst.getIntMapValue("Dep");

                    IloTuple udp = Upd.makeTuple(u++);

                    if (Udp.get(udp) == 1) {
                        for (int e = 0; e < Ee.getSize(); e++) {
                            if (linksList.size() <= e) {
                                linksList.add(getLink(e));
                            }
                            pLink pLink = linksList.get(e);
                            if (dep.get(Ee.makeTuple(e)) == 1) {
//                                Log.log(pLink.getName() + " " + pLink.getVisualText());
                                pLink.serveRequest(reqPointer, vLink);
                                network.addUsedCapacity(vLink.getCapacity());
//                                Log.log(pLink.getName() + " " + pLink.getVisualText());
                            }
                        }
                    }
                }
            }
        }
        pLink.resetCounter();
//        new GraphVisualisation(network).start();
    }

    private void clearMemory() {
        if (dataElements != null) {
            dataSource.end();
            dataElements.end();
            model.end();
        }
    }

    public void end() {
        dataSource.end();
        dataElements.end();
        modelSource.end();
        model.end();
        errHandler.end();
        def.end();
        settings.end();
        cplex.end();
        oplF.end();
//        debugWriter.close();
    }

//    private void serveSeqReq(Request request) throws IloException {
//
//        getDataElements();
//
//        int x = 0;
//        int u = 0;
////        boolean routersAdded = false;
//        List<pLink> linksList = new ArrayList<>(network.getLinks());
////            reqPointer++;
////            Log.log("REQ: " + reqPointer + "/" + dataElements.getElement("R").asInt());
//        for (int v = 0; v < Vv.get(1).getSize(); v++) {
//            vRouter vRouter = (vRouter) ParserOPL.getRouterByName(request, Vv.get(1).makeTuple(v).getStringValue("name"));
//
//            for (int w = 0; w < Vw.getSize(); w++) {
//                IloTuple xvw = Xwv.makeTuple(x++);
//                if (Xvw.get(xvw) == 1) {
////                        if (pRouter == null) {
//                    pRouter pRouter = (pRouter) ParserOPL.getRouterByName(network, Vw.makeTuple(w).getStringValue("name"));
////                 g       }
//                    pRouter.serveRequest(request.getIndex(), vRouter);
//                }
//            }
////                routersAdded = true;
//            }
//
//            for (int d = 0; d < Ed.get(1).getSize(); d++) {
//
//                IloTuple dd = Ed.get(1).makeTuple(d);
////        if (isSeq) req = reqPointer;
//                vRouter r1 = (vRouter) ParserOPL.getRouterByName(request, dd.getStringValue("source"));
//                vRouter r2 = (vRouter) ParserOPL.getRouterByName(request, dd.getStringValue("target"));
//
//                vLink vLink = (vLink) request.getEdge(r1, r2);
//
//                for (int p = 0; p < Pst.getSize(); p++) {
//
//                    IloTuple pst = Pst.makeTuple(p);
//                    IloIntMap dep = pst.getIntMapValue("Dep");
//
//                    IloTuple udp = Upd.makeTuple(u++);
//
//                    if (Udp.get(udp) == 1) {
//                        for (int e = 0; e < Ee.getSize(); e++) {
//                            pLink pLink = linksList.get(e);
//                            if (dep.get(Ee.makeTuple(e)) == 1) {
////                                Log.log(pLink.getName() + " " + pLink.getVisualText());
//                                pLink.serveRequest(request.getIndex(), vLink);
//                                network.addUsedCapacity(vLink.getCapacity());
////                                Log.log(pLink.getName() + " " + pLink.getVisualText());
//                            }
//                        }
//                    }
//                }
//            }
//        }

//    public void solve() {
//
//        Log.log("Solve.");
//        try {
//            if (wr == null) setData();
////            long start = System.nanoTime();
//            if (cplex.solve()) {
////                time = System.nanoTime() - start;
//                Log.log("TIME per sol ~= " + (cplex.getCplexTime() - time));
//                time = cplex.getCplexTime();
//                Log.log("FULL TIME " + time);
//                toNetwork();
//            } else {
//                Log.log("No solution!");
//                return;
//            }
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
//
//        if (isSeq && network.getRequests().size() > wr.getCnt()) {
//            try {
//                setData(wr.writeSeq().getDataString());
//                solve();
//            } catch (IloException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
