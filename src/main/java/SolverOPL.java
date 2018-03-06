import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.opl.*;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.*;

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
    public static String DEBUG = Logger.DIR + "%s_%s_DEBUG.txt";
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

    private Map<String, Map<Integer, VRouter>> backupVRouters;
    private Map<String, Map<Integer, List<VLink>>> backupVLinks;

    public SolverOPL(Network network) throws IloException {
        super(network);
        if (HYBRID) SEQ = true;
        isSeq = SEQ;
    }

    public SolverOPL(Network network, boolean isSeq) throws IloException {
        super(network);
        this.isSeq = isSeq;
    }

    private PRouter getRouter(int index) {
        IloTuple router = Vw.makeTuple(index);
        String name = router.getStringValue("name");
        PRouter pRouter = (PRouter) ParserOPL.getRouterByName(network, name);
        if (pRouter != null)
            return pRouter;
        int Bw = router.getIntValue("Bw");
        int Mw = router.getIntValue("Mw");
        int Lw = router.getIntValue("Lw");
        pRouter = new PRouter(name, Bw, Mw, Lw, network);
        network.addVertex(pRouter);
        network.addLocation(Lw, Bw, Mw);
        return pRouter;
    }

    private VRouter getVRouter(int req, int index) throws IloException {
        IloTuple v = Vv.get(req).makeTuple(index);
        String name = v.getStringValue("name");
        VRouter vRouter = (network.getRequests().size() >= req) ? (VRouter) ParserOPL.getRouterByName(network.getRequests().get(req - 1), name) : null;
        if (seqReq != null) {
            reqPointer = seqReq.getIndex();
            return (VRouter) ParserOPL.getRouterByName(seqReq, name);
        }
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
        vRouter = new VRouter(name, Bv, Mv, Lv);
        if (network.getRequests().size() < req)
            network.addRequest(new Request(VLink.class));
        Request request = network.getRequests().get(req - 1);
        reqPointer = request.getIndex();
        request.addVertex(vRouter);
        return vRouter;
    }

    private PLink getLink(int index) {
        IloTuple link = Ee.makeTuple(index);
        PRouter r1 = (PRouter) ParserOPL.getRouterByName(network, link.getStringValue("source"));
        PRouter r2 = (PRouter) ParserOPL.getRouterByName(network, link.getStringValue("target"));
        int Ce = link.getIntValue("Ce");

        if (network.containsEdge(r1, r2))
            return (PLink) network.getEdge(r1, r2);

        return new PLink(r1, r2, Ce, network);
    }

    private VLink getVLink(int req, int index) throws IloException {
        IloTuple dd = Ed.get(req).makeTuple(index);
        Request request = (seqReq != null) ? seqReq : network.getRequests().get(req - 1);

        VRouter r1 = (VRouter) ParserOPL.getRouterByName(request, dd.getStringValue("source"));
        VRouter r2 = (VRouter) ParserOPL.getRouterByName(request, dd.getStringValue("target"));

        if (request.containsEdge(r1, r2))
            return (VLink) request.getEdge(r1, r2);

        int Cd = dd.getIntValue("Cd");

        return new VLink(r1, r2, Cd, request);
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
        network.getRouters().stream().filter(r -> r.getRequests() != null).forEach(r -> {
            Set<Integer> reqs = new HashSet<>(r.getRequests().keySet());
            reqs.forEach(req -> r.removeRequest(req));
        });
        network.getLinks().stream().filter(l -> l.getRequests() != null).forEach(l -> {
            Set<Integer> reqs = new HashSet<>(l.getRequests().keySet());
            reqs.forEach(req -> l.removeRequest(req));
        });
    }

    @Override
    public boolean serveRequest(Request request) {
        Logger.log(request);
        network.addRequest(request);
        if (isSeq) {
            wr = new WriterOPL(network).writeSeq(request);
            seqReq = request;
        } else {
            wr = new WriterOPL(network).writeOPL();
        }
        try {
            setData(wr.getDataString());
            Logger.log("Solving...");
            debugWriter.println("\n" + "########## " + request.getIndex() + "\n");
            if (cplex.solve()) {
                Logger.log("Solved!");
                solveLoadBalancing();
                updateTime();
                if (!isSeq)
                    releaseAllRequests();
                toNetwork();
                network.countUsedCapacity();
                if (HYBRID) isSeq = true;
                return super.serveRequest(request);
            } else {
                super.releaseRequest(request);
                Logger.log("Can't solve.");
//                Logger.logF(Logger.DIR + "error.txt", wr.getDataString());
                if (HYBRID && isSeq) {
                    Logger.log("Trying full optimal...");
                    isSeq = false;
                    seqReq = null;
                    return serveRequest(request);
                } else if (HYBRID && !isSeq) {
                    isSeq = true;
                }
                updateTime();
                return false;
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        super.releaseRequest(request);
        return false;
    }

    private void solveLoadBalancing() throws IloException {
        cplex.addEq(model.getElement("OCCUPIED").asIntExpr(), cplex.getObjValue(), "Best");//.getExpr();
        cplex.getObjective().setExpr(model.getElement("MINIMUM").asIntExpr());
        cplex.getObjective().setSense(IloObjectiveSense.Maximize);
        cplex.setParam(IloCplex.DoubleParam.TiLim, 100);
        Logger.log("Load Balancing...");
        cplex.solve();
        Logger.log("Balanced.");
    }

    private void updateTime() {
        time = cplex.getCplexTime() - fullTime;
        Logger.log("TIME per REQ = " + time);
        fullTime = cplex.getCplexTime();
        Logger.log("FULL TIME = " + fullTime);
    }

    private void udpdateLocs() {
        if (locs == null) locs = new HashMap<>();
        for (int i = 1; i <= VRouter.LOC_MAX; i++) {
            final int location = i;
            int power = network.vertexSet().stream().filter(r -> ((PRouter) r).getLocation() == location).mapToInt(r -> ((PRouter) r).getSubstratePower()).max().getAsInt();
            int memory = network.vertexSet().stream().filter(r -> ((PRouter) r).getLocation() == location).mapToInt(r -> ((PRouter) r).getSubstrateMemory()).max().getAsInt();
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
        List<PLink> linksList = new ArrayList<>(network.getLinks());
        for (int r = 1; r <= dataElements.getElement("R").asInt(); r++) {
            for (int v = 0; v < Vv.get(r).getSize(); v++) {
                VRouter vRouter = getVRouter(r, v);

                for (int w = 0; w < Vw.getSize(); w++) {
                    PRouter pRouter = null;
                    if (!routersAdded) {
                        pRouter = getRouter(w);
                    }

                    IloTuple xvw = Xwv.makeTuple(x++);
                    if (Xvw.get(xvw) == 1) {
                        if (pRouter == null) {
                            pRouter = (PRouter) ParserOPL.getRouterByName(network, Vw.makeTuple(w).getStringValue("name"));
                        }
                        pRouter.serveRequest(reqPointer, vRouter);
                    }
                }
                routersAdded = true;
            }

            for (int d = 0; d < Ed.get(r).getSize(); d++) {

                VLink vLink = getVLink(r, d);

                for (int p = 0; p < Pst.getSize(); p++) {
                    IloTuple pst = Pst.makeTuple(p);
                    IloIntMap dep = pst.getIntMapValue("Dep");

                    IloTuple udp = Upd.makeTuple(u++);

                    if (Udp.get(udp) == 1) {
                        for (int e = 0; e < Ee.getSize(); e++) {
                            if (linksList.size() <= e) {
                                linksList.add(getLink(e));
                            }
                            PLink pLink = linksList.get(e);
                            if (dep.get(Ee.makeTuple(e)) == 1) {
                                pLink.serveRequest(reqPointer, vLink);
                                network.addUsedCapacity(vLink.getCapacity());
                            }
                        }
                    }
                }
            }
        }
        PLink.resetCounter();
//        new GraphVisualisation(network).start();
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
    }
}
