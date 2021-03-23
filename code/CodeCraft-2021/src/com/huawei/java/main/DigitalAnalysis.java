package com.huawei.java.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DigitalAnalysis extends Base{
    public static int vtMaxCpu = -1;
    public static int vtMaxMem = -1;
    public static int needMaxCpu = -1;
    public static int needMaxMem = -1;
    public static int[][] serverType ;

    public static void insite(){
        int i=0;
        serverType = new int[N][4];
        for(Map.Entry<String, List<Integer>> st:hmForN_serverType.entrySet()){
            List<Integer> l=st.getValue();
            serverType[i][0]=l.get(0);
            serverType[i][1]=l.get(1);
            serverType[i][2]=l.get(2);
            serverType[i][3]=l.get(3);
            i++;
        }
        for(Map.Entry<String, List<Integer>> vs:hmForM_virtualType.entrySet()){
            int cpu=vs.getValue().get(0);
            int mem=vs.getValue().get(1);
            if(vs.getValue().get(2)==1){
                cpu=cpu/2;
                mem=mem/2;
            }
            vtMaxCpu=vtMaxCpu>cpu?vtMaxCpu:cpu;
            vtMaxMem=vtMaxMem>mem?vtMaxMem:mem;
        }
        for(i=0;i<1;i++){
            HashMap<Integer,List<Integer>> everyTDayActList=allTDayActList.get(i);
            int sumcpu=0;
            int summem=0;
            for (Map.Entry<Integer, List<Integer>> vs:everyTDayActList.entrySet()){
                //id cpu mem isdouble
                sumcpu+=vs.getValue().get(1);
                summem+=vs.getValue().get(2);
            }
            needMaxCpu=needMaxMem>sumcpu?needMaxMem:sumcpu;
            needMaxMem=needMaxMem>summem?needMaxMem:summem;
        }
        int[] c={needMaxCpu,needMaxMem};
        int[] p={vtMaxCpu,vtMaxMem};
        ac_buy(c,p);
    }

    public static List<Integer> ac_buy(int[] capacity, int[] limit){
        //capcity cpu,mem  || unallocated id,cpu,mem
        List<Integer> answer = new ArrayList();//j,
        double[] pheromone = new double[N];
        //初始化信息素。
        int maxAnt=300;//先试试50个
        double a=1.0,b=2.0,p=0.05,q=1000;//系数
        for(int i=0;i<N;i++){
            pheromone[i]=1.0;
        }
        //pheromone=fill(pheromone,1.0);
        //选择第一个节点
        int ant=0;
        int minMoney=100000000;
        List<List<Integer>> best=new ArrayList();
        List<Integer> bestChoice=new ArrayList();
        while(ant++<maxAnt){
            //选第一个
            int[] c = capacity.clone();
            List<Integer> next;
            double[] possibility;
            int money=0;
            List<Integer> choice=new ArrayList();
            while( true ) {
                next = new ArrayList<>();
                //寻找能去的下一个节点
                for(int i=0;i<N;i++){
                    if(limit[0]<serverType[i][0]/1 && limit[1]<serverType[i][1]/2){
                        next.add(i);
                    }
                }
                if (c[0]<0 && c[1]<0){ //安排完了，则退出。
                    break;
                }
                // 计算概率
                possibility = new double[next.size()];
                for (int i = 0; i < next.size(); i++) {
                    possibility[i] = Math.pow(pheromone[next.get(i)], a) *
                            Math.pow(1.0/(double) serverType[i][2],b);
                }
                //轮盘赌
                int pre = next.get(roulette(possibility));
                choice.add(pre);
                c[0]-=serverType[pre][0];
                c[1]-=serverType[pre][1];
                money+=serverType[pre][2]+serverType[pre][3];
            }
            // 更新信息素
            for(int i=0;i<N;i++){
                pheromone[i]*=(1-p) ;
            }
            for(int i=0;i<choice.size();i++){
                pheromone[choice.get(i)] += q/money;
            }
            if(minMoney>money){
                System.err.println(money);
                minMoney=money;
                best.add(choice);
                bestChoice.add(minMoney);
                answer=choice;
            }
        }
        return answer;
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
}
