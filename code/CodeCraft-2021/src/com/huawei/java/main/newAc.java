package com.huawei.java.main;

import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.Virtual;

import java.util.*;

public class newAc extends com.huawei.java.main.InputData {


    public static void inist(){
        int day=0;

        //初始化今日的需求c_cpu[i],c_mem[i],c_double[i];
        int[][] ck=new int[allTDayAppList.get(day).size()][3];
        List<Virtual> virtuals = new ArrayList<>();
        int i=0;
        for(Map.Entry<String,Integer> vs:allTDayAppList.get(day).entrySet()){
            List<Integer> list= hmForM_virtualType.get(vs.getValue());
            Virtual virtual = new Virtual(vs.getValue(), vs.getKey(), list);
            ck[i][2]=list.get(2);
            if(list.get(2)==1){
                ck[i][0]=list.get(0)/2;
                ck[i][1]=list.get(1)/2;
            }else {
                ck[i][0]=list.get(0);
                ck[i][1]=list.get(1);
            }
            i++;
        }


        LinkedHashMap<String,Integer> servers=buyServer(day,virtuals);
        //计算l_cpu[j] l_mem[j]  j<服务器数量;
        List<Integer> lCpu=new ArrayList<>();
        List<Integer> lMem=new ArrayList<>();
        for(Map.Entry<String,Integer> s:servers.entrySet()){
            List<Integer> list = hmForN_serverType.get(s.getKey());
            for(i=0;i<s.getValue();i++){
                lCpu.add(list.get(0)/2);//a
                lCpu.add(list.get(0)/2);//b
                lMem.add(list.get(1)/2);
                lMem.add(list.get(1)/2);
            }
        }

        //初始化xij
        int[][] x =new int[ck.length][lCpu.size()];


    }

    private static LinkedHashMap<String,Integer> buyServer(int day, List<Virtual> virtuals) {
        List<Server> serverList = new ArrayList<>(); //返回新增servers队列
        LinkedHashMap<String,Integer> todayPurScheme = new LinkedHashMap<>();
        //计算购买优先级
        List<String> purRecommend = new ArrayList();

        purRecommend = new ArrayList<>();
        TreeMap<Double,String> scores = new TreeMap<>();
        for (Map.Entry<String, List<Integer>> stype :  hmForN_serverType.entrySet()){
            double score = stype.getValue().get(2) + stype.getValue().get(3);
            scores.put(score,stype.getKey());
        }
        for (Map.Entry<Double, String> entry : scores.entrySet()) {
            purRecommend.add(entry.getValue());
        }

        //计算购买需求
        for(Virtual virtual:virtuals){
            boolean succeed=false;
            for(int i = 0;i<serverList.size();i++){
                Server servers=serverList.get(i);
                if(servers.canBeSet(virtual)){
                    if(!servers.setVirtual(virtual)){
                        System.err.println("处理每日申请 设置服务器时错误");
                        break;
                    }else {
                        succeed=true;
                        break;
                    }
                }
            }
            if(succeed ==false){
                //买新的服务器
                //购买策略推荐
                List<Integer> list=new ArrayList<>();
                for(int i=0;i<N;i++){
                    list=hmForN_serverType.get(purRecommend.get(i));
                    if(list.get(0)/2>=virtual.getRequiredCPU()/Math.pow(2,virtual.getIsDouble())
                            && list.get(1)/2>=virtual.getRequiredMem()/Math.pow(2,virtual.getIsDouble())){
                        int num=todayPurScheme.getOrDefault(purRecommend.get(i), 0)+1;
                        todayPurScheme.put(purRecommend.get(i),num);
                        Server newServer = new Server(purRecommend.get(i),list.get(0),list.get(1),T-day);
                        if(!newServer.setVirtual(virtual)){
                            System.err.println("处理每日申请->买新->设置服务器时错误");
                        }else {
                            succeed=true;
                            serverList.add(newServer);
                        }
                        break;
                    }
                }
            }

        }
        return todayPurScheme;
    }




}

