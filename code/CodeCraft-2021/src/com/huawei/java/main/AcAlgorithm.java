package com.huawei.java.main;

import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.Virtual;

import java.util.*;

public class AcAlgorithm extends com.huawei.java.main.InputData {
    public static LinkedHashMap<String, List<Double>>InitList = new LinkedHashMap<>(); //每台虚拟服务器能选择的服务器类型
    public static double p=0.99; //蚁群算法系数
    public static double Q=1000; //蚁群算法系数
    public static List<Double> moneys = new ArrayList<>();


    public static double ac(){
        double minMoney=1000000000;

        int maxGen=1000;
        setInitList();
        LinkedHashMap<Integer,String> todayApp= (LinkedHashMap<Integer, String>) allTDayActList.get(0);
        int[][] minx = new int[todayApp.size()][N];
        double[][] possibility=new double[todayApp.size()][N];


        for(int gen=0;gen<maxGen;gen++){
            //初始化表格 行：服务器，列：虚拟机id
            System.out.println(gen);
            int[][] x=new int[todayApp.size()][N]; //x
            int i=0;
            for (Map.Entry<Integer,String> id : todayApp.entrySet()){ //p
                for(int j=0;j<N;j++) {
                    possibility[i][j] = (double) InitList.get(id.getValue()).get(j);
                }
                i++;
            }
            //随机选择
            for (i=0;i<x.length;i++){
                int j=roulette(possibility[i]);
                x[i][j]=1;
            }
            //计算money
            double money=getMoney(x,todayApp);

            if(money<0){
                System.err.println("money 字节不够用");
            }
            //更新概率
            for(i=0;i<x.length;i++){
                for(int j=0;j<x[0].length;j++){
                    possibility[i][j] = ((1.0-p)+Q/money)*possibility[i][j];
                }
            }
            //更新最新的money
            if(minMoney>money){
                minMoney=money;
                minx=x;
                moneys.add(minMoney);
            }

        }
        System.out.println(minMoney);
        return minMoney;
    }

    public static double getMoney(int[][] x, LinkedHashMap<Integer,String> todayApp) {
        double money = 0.0;
        for (int j = 0; j < x[0].length; j++) {
            String name = serverTypeid.get(j);
            List<Server> serverList = new ArrayList<>();
            int i = 0;
            for (Map.Entry<Integer,String> v : todayApp.entrySet()) {
                if (x[i][j] == 1) {
                    boolean success = false;
                    Virtual virtual = new Virtual(v.getKey(), v.getValue(), hmForM_virtualType.get(v.getValue()));
                    for (int k = 0; k < serverList.size(); k++) {
                        Server servers = serverList.get(k);
                        if (servers.canBeSet(virtual)) {
                            if (!servers.setVirtual(virtual)) {
                                System.err.println("处理每日申请 设置服务器时错误");
                                break;
                            }
                        }
                    }
                    if(success ==false){
                        List<Integer> list=hmForN_serverType.get(name);
                        money+=(double) (list.get(2)+list.get(3));
                        Server newServer = new Server(name,list.get(0),list.get(1),0);
                        newServer.setVirtual(virtual);
                        serverList.add(newServer);
                    }
                }
                i++;
            }
        }
        return money;
    }

    public static int roulette(double[] p){
        int j=-1;
        double sum = 0.0;
        for (int i =0; i< p.length;i++){
            sum+=p[i];
        }
        double now=0.0;
        double r = Math.random() * sum;
        for (int i = 0; i < p.length; i++) {
            now += p[i];
            if (r <= now) {
                return i;
            }

        }
        return j;
    }

    public static void setInitList(){
        for (Map.Entry<String, List<Integer>> virtual : hmForM_virtualType.entrySet()){
            List<Integer> virList = virtual.getValue();
            List<Double> temp = new ArrayList<>();
            int i=0;
            for (Map.Entry<String, List<Integer>> server : hmForN_serverType.entrySet()){
                List<Integer> list = server.getValue();

                if(list.get(0)/2>=virList.get(0)/Math.pow(2,virList.get(2))
                        && list.get(1)/2>=virList.get(1)/Math.pow(2,virList.get(2))){
                    temp.add(1/(double)Math.pow((list.get(2)+list.get(3))/100,5));
                }else{
                    temp.add(0.0);
                }
            }
            InitList.put(virtual.getKey(),temp);
        }
    }
}
