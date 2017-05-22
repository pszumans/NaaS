/**
 * Created by Szuman on 04.04.2017.
 */

import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Model {

    private IloCplex cplex;

    private int[] Bw;
    private int[] Mw;
    private int[] Lw;
    private int[] Ce;

    private int[] Bv;
    private int[] Mv;
    private int[][] Lv;
    private int[] Cd;

    private boolean[][] Dep;

//    private List<Integer> Bw;
//    private List<Integer> Mw;
//    private List<Integer> Lw;
//    private List<Integer> Ce;
//
//    private List<Integer> Bv;
//    private List<Integer> Mv;
//    private List<List<Integer>> Lv;
//    private List<Integer> Cd;
//
//    private List<List<Boolean>> Dep;

    private IloIntVar[][] Xvw;
    private IloIntVar[] Udp;
    private IloIntVar[] Zvw;
    private IloIntVar[] y;
    private IloIntVar[] z;
    private IloIntVar aux;
    private IloIntVar BandSum;

    private IloLinearIntExpr[] vRoutersToPh;
    private IloLinearIntExpr[] PhRouterForvR;
    private IloLinearIntExpr[] Power;
    private IloLinearIntExpr[] Memory;
    private IloLinearIntExpr[] Location;
    private IloLinearIntExpr[] LocaLoca;
    private IloLinearIntExpr[] UnsplittableP;
    private IloLinearIntExpr[] vRouterToLink1;
    private IloLinearIntExpr[] vRouterToLink2;
    private IloLinearIntExpr[] BandwidthLink;
    private IloLinearIntExpr[] BandwidthLimit;
    private IloLinearIntExpr[] SubstrateBandwidth;
    private IloLinearIntExpr[] Minimum;
    private IloLinearIntExpr[] Objective;

    private void test(Network network) {
        List<pRouter> pRouters = new ArrayList<>(network.vertexSet());
        List<pLink> pLinks = new ArrayList<>(network.edgeSet());
        int pRoutersCount = network.vertexSet().size();
        int pLinksCount = network.edgeSet().size();
        Bw = new int[pRoutersCount];
        Mw = new int[pRoutersCount];
        Lw = new int[pRoutersCount];
        Ce = new int[pLinksCount];

        List<vRouter> vRouters = new ArrayList<>();
        List<vLink> vLinks = new ArrayList<>();
        int vRoutersCount = 0;
        int vLinksCount = 0;

        List<Integer> BvList = new ArrayList<>();
        List<Integer> MvList = new ArrayList<>();
        List<List<Integer>> LvList = new ArrayList<>();
        List<Integer> CdList = new ArrayList<>();
        Set<Set<Boolean>> DepList = new LinkedHashSet<>();

        for (Request request : network.getRequests()) {
//            vRouters.addAll(new ArrayList<vRouter>(request.vertexSet()));
//            vLinks.addAll(new ArrayList<vLink>(request.edgeSet()));
//            vLinksCount += request.edgeSet().size();
        }

        network.getRequests().forEach(request -> {
            request.vertexSet().forEach(r -> {
                vRouter router = (vRouter) r;
                BvList.add(router.getPower());
                MvList.add(router.getMemory());
                LvList.add(router.getLocations());
            });
        });

        network.getPaths().forEach(pathEnds ->
            pathEnds.getPaths().forEach(p ->
                    p.getEdgeList().forEach(l -> {
//                        DepList.add()
                    }))
        );

        Bv = new int[vRoutersCount];
        Mv = new int[vRoutersCount];
        Lv = new int[vRoutersCount][];
        Cd = new int[vLinksCount];

        Dep = new boolean[pLinksCount][];

        IntStream.range(0, pRoutersCount).forEach(i -> {
            Bw[i] = pRouters.get(i).getPower();
            Mw[i] = pRouters.get(i).getMemory();
            Lw[i] = pRouters.get(i).getLocation();
        });
//        IntStream.range(0, pLinksCount).forEach(i -> {
//            Ce[i] = pLinks.get(i).getCapacity();
            Ce = pLinks.stream().mapToInt(l -> l.getCapacity()).toArray();
//        });
    }
}
