package com.huawei.java.main;

import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.Virtual;

import java.util.*;

public class AcAlgorithm1 extends com.huawei.java.main.InputData {
    public static List<Server> serverList = new ArrayList<>();
    public static LinkedHashMap<Integer, Virtual> virtualMap = new LinkedHashMap<>(); //id,虚拟机对象，全量存
    public static LinkedHashMap<Integer, Server> serverMap= new LinkedHashMap<>();; //id,服务器对象，全量存
    public static List<Integer> cpus;
    public static List<Integer> mems;
    //    public static HashMap<Integer,Integer> vStoS =new HashMap<>(); //虚拟ID，服务器ID
    public static HashMap<Integer,Integer> myidToOut = new HashMap<>(); //服务器内部ID，与外部ID
    public static List<LinkedHashMap<String,Integer>> allDaypurchaseScheme = new ArrayList<>();; //每天的购买清单
    public static List<String> purRecommend = new ArrayList(); //购买推荐
    //    public static HashMap<Integer,Integer> vidtoAB = new HashMap();
    public static long money = 0;


    public static void ac(){

        for(int day=0;day<1;day++){
            setPurRecommend(day);
            processApplication(day);

            //根据今天的请求信息输出结果
           // printDayOutput(day);

            //删除操作
         //   executeDel(day);

            //计算money
          //  money+=calMoney(day);
        }
        System.out.println(money);
    }
    public static void setPurRecommend(int day){
        purRecommend = new ArrayList<>();
        TreeMap<Double,String> scores = new TreeMap<>();
        for (Map.Entry<String, List<Integer>> stype :  hmForN_serverType.entrySet()){
            double score = stype.getValue().get(2)/(T-day) + stype.getValue().get(3);
            scores.put(score,stype.getKey());
        }
        for (Map.Entry<Double, String> entry : scores.entrySet()) {
            purRecommend.add(entry.getValue());
        }

    }
    public static void processApplication(int day) {
        LinkedHashMap<String, Integer> todayPurScheme = new LinkedHashMap<>();
        int servernum = 0;
        int len = serverList.size();
        for (Integer vsid : allDayAppId.get(day)) {
            boolean succeed = false;
            Virtual virtual = new Virtual(vsid, allTDayActList.get(day).get(vsid), hmForM_virtualType.get(allTDayActList.get(vsid)));

            for (int i = 0; i < serverList.size(); i++) {
                Server servers = serverList.get(i);
                if (servers.canBeSet(virtual)) {

                    if (!servers.setVirtual(virtual)) {
                        System.err.println("处理每日申请 设置服务器时错误");
                        break;
                    } else {
                        succeed = true;
                        servernum = i;
                        break;
                    }
                }
            }
//            System.out.println("XX");
            if (succeed == false) {

                //买新的服务器
                //购买策略推荐
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < N; i++) {
                    list = hmForN_serverType.get(purRecommend.get(i));
                    if (list.get(0) / 2 >= virtual.getRequiredCPU() / Math.pow(2, virtual.getIsDouble())
                            && list.get(1) / 2 >= virtual.getRequiredMem() / Math.pow(2, virtual.getIsDouble())) {
                        int num = todayPurScheme.getOrDefault(purRecommend.get(i), 0) + 1;
                        todayPurScheme.put(purRecommend.get(i), num);
                        Server newServer = new Server(purRecommend.get(i), list.get(0), list.get(1), T - day);
                        serverMap.put(newServer.getId(), newServer);
                        if (!newServer.setVirtual(virtual)) {
                            System.err.println("处理每日申请->买新->设置服务器时错误");
                        } else {
                            succeed = true;
                            serverList.add(newServer);
                        }
                        break;
                    }
                }
            }
            //          System.out.println("XX");
        }
    }
}
