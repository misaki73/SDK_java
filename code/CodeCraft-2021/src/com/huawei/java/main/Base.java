package com.huawei.java.main;

//import cn.hutool.core.io.FileUtil;

import java.io.*;
import java.util.*;
/**
 * 仅用于数据输入、输出
 * */
public class Base {
    public static Boolean isCommit=false;
    public static String path="E:/huaweiElite/";
    public static List<String> origenalDataList;//原始数据
    public static int N=-1;//可以采购的服务器类型数量
    public static HashMap<String,List<Integer>> hmForN_serverType;//(型号 | CPU核数,内存大小,硬件成本,每日能耗成本)
    public static int M=-1;//可以出售的虚拟机类型数量
    public static HashMap<String,List<Integer>> hmForM_virtualType;//(型号 | CPU核数,内存大小,是否双节点部署)
    public static int T=-1;//天数
    public static int vsMaxCpu = -1;
    public static int vsMaxMemory = -1;
    /**
     * - 共T天，每天R条数据
     * - 每条数据为一个List：
     * -- size=1 表示del 虚拟机id； size=2表示 add ，虚拟机id，虚拟机型号
     * */
    public static List<List<List<String>>> allTDayActList;
    public static List<List<String>> everyTDayActList;//临时

    public static List<List<Integer>> allTDayAppList = new ArrayList();
    public static List<List<String>> allTDayAppNameList =new ArrayList();
    public static List<List<Integer>> allTDayDelList= new ArrayList();

    /**
     * 限制参数
     * */
    public static int maxServerNum=100000;
//    public static void main(String[] argv){
////        initial_standard1();
//        initial();
////        initial_standard();
////        System.err.println("ok");
//        compute();
//
//    }
    /**
     * 初始化以及清洗数据
     * */
    public static void initial_standard1(){
//        origenalDataList = readLogByList(path + "training-2.txt");
        hmForM_virtualType =new LinkedHashMap<>();
        hmForN_serverType=new LinkedHashMap<>();
        allTDayActList=new ArrayList<>();
        everyTDayActList=new ArrayList<>();
        allTDayAppList =new ArrayList<>();
        allTDayDelList =new ArrayList<>();
        allTDayActList =new ArrayList<>();
        allTDayAppNameList = new ArrayList<>();
        Scanner in=new Scanner(System.in);
        N=Integer.parseInt(in.nextLine());
        for (int i = 0; i < N; i++) {
            String s=in.nextLine();
            String[] strings = disgardBracket(s);
            List<Integer> tempList=new ArrayList<>();//CPU,核数,内存大小,硬件成本,每日能耗成本
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }
            hmForN_serverType.put(strings[0],tempList);
        }
        M=Integer.parseInt(in.nextLine());
        for (int i = 0; i < M; i++) {
            String s=in.nextLine();
            String[] strings = disgardBracket(s);
            List<Integer> tempList=new ArrayList<>();//CPU ,核数,内存大小,是否双节点部署
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }
            vsMaxCpu= tempList.get(0)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(0) / Math.pow(2, tempList.get(2))) : vsMaxCpu;
            vsMaxMemory= tempList.get(1)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(1) / Math.pow(2, tempList.get(2))) : vsMaxCpu;

            hmForM_virtualType.put(strings[0],tempList);
        }
        T=Integer.parseInt(in.nextLine());
        for (int i = 0; i < T; i++) {
            int R=Integer.parseInt(in.nextLine());
            List<Integer> tempappList = new ArrayList<>();
            HashSet<Integer> tempdelList = new HashSet<>();
            List<String> tempName = new ArrayList<>();
            everyTDayActList=new ArrayList<>();
            for (int j = 0; j < R; j++) {
                String s=in.nextLine();
                String[] strings = disgardBracket(s);
                List<String> tempLH=new ArrayList<>();
                if (strings.length<3){//del
                    //虚拟机id
                    tempLH.add(strings[1]);
                    tempdelList.add(Integer.parseInt(strings[1]));
                }else {
                    //虚拟机id，虚拟机型号
                    tempLH.add(strings[2]);
                    tempLH.add(strings[1]);
                    tempName.add(strings[1]);
                    tempappList.add(Integer.parseInt(strings[2]));
                    int id = Integer.parseInt(strings[2]);
                    if(tempdelList.contains(id)){
                        tempdelList.remove(id);
                    }
                }
                everyTDayActList.add(tempLH);
            }
            allTDayAppList.add(tempappList);
            allTDayDelList.add(new ArrayList(tempdelList));
            allTDayAppNameList.add(tempName);
            allTDayActList.add(everyTDayActList);
        }
    }

    public static void initial(){
        origenalDataList = readLogByList(path + "training-2.txt");
        hmForM_virtualType =new LinkedHashMap<>();
        hmForN_serverType=new LinkedHashMap<>();
        allTDayActList=new ArrayList<>();
        everyTDayActList=new ArrayList<>();
        allTDayAppList =new ArrayList<>();
        allTDayDelList =new ArrayList<>();
        allTDayActList =new ArrayList<>();
        allTDayAppNameList = new ArrayList<>();

        int next=0;
        N=Integer.parseInt(origenalDataList.get(next++));
        for (int i = 0; i < N; i++) {
            String s=origenalDataList.get(next++);
            String[] strings = disgardBracket(s);
            List<Integer> tempList=new ArrayList<>();//CPU,核数,内存大小,硬件成本,每日能耗成本
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }

            vsMaxCpu= tempList.get(0)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(0) / Math.pow(2, tempList.get(2))) : vsMaxCpu;
            vsMaxMemory= tempList.get(1)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(1) / Math.pow(2, tempList.get(2))) : vsMaxCpu;
            hmForN_serverType.put(strings[0],tempList);
        }
        M=Integer.parseInt(origenalDataList.get(next++));
        for (int i = 0; i < M; i++) {
            String s=origenalDataList.get(next++);
            String[] strings = disgardBracket(s);
            List<Integer> tempList=new ArrayList<>();//CPU ,核数,内存大小,是否双节点部署
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }
            hmForM_virtualType.put(strings[0],tempList);
        }
        T=Integer.parseInt(origenalDataList.get(next++));
        for (int i = 0; i < T; i++) {
            int R=Integer.parseInt(origenalDataList.get(next++));
            everyTDayActList=new ArrayList<>();
            List<Integer> tempappList = new ArrayList<>();
            HashSet<Integer> tempdelList = new HashSet<>();
            List<String> tempName = new ArrayList<>();


            for (int j = 0; j < R; j++) {
                String s=origenalDataList.get(next++);
                String[] strings = disgardBracket(s);
                List<String> tempLH=new ArrayList<>();

                if (strings.length<3){//del
                    //虚拟机id
                    tempLH.add(strings[1]);
                    tempdelList.add(Integer.parseInt(strings[1]));
                }else {
                    //虚拟机id，虚拟机型号
                    tempLH.add(strings[2]);
                    int id = Integer.parseInt(strings[2]);
                    if(tempdelList.contains(id)){
                        tempdelList.remove(id);
                    }
                    tempLH.add(strings[1]);
                    tempName.add(strings[1]);
                    tempappList.add(Integer.parseInt(strings[2]));
                }
                everyTDayActList.add(tempLH);

            }
            allTDayAppList.add(tempappList);
            allTDayDelList.add(new ArrayList(tempdelList));
            allTDayAppNameList.add(tempName);
            allTDayActList.add(everyTDayActList);
        }
    }
    public static String[] disgardBracket(String inputString){
        return inputString.substring(1, inputString.length() - 1).split(", ");
    }
    public static List<String> readLogByList(String path) {
        List<String> lines = new ArrayList<String>();
        String tempstr = null;
        try {
            File file = new File(path);
            if(!file.exists()) {
                throw new FileNotFoundException();
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "utf-8"));
            while((tempstr = br.readLine()) != null) {
                lines.add(tempstr.toString());
            }
        } catch(IOException ex) {
//            System.out.println(ex.getStackTrace());
        }
        return lines;
    }

    /**
     * 核心运算代码
     * */
    public static StringBuilder sb;
    public static String outputPath="E:\\huaweiElite\\testData.txt";
    public static void outputString(String s){
        System.out.println(s);
//        sb.append(s+"\n");
    }

    public static List<String> serverPool;
    public static int cpuRemain=0,memRemain=0,serverCountBefore=0;
    public static void compute(){
        sb=new StringBuilder();

        generateEasyServerPool();
        //每一天
        for (int i = 0; i < allTDayActList.size(); i++) {
            List<List<String>> requestForiDay = allTDayActList.get(i);
            List<String> assignList=new ArrayList<>();
            List<String> purchaseType=new ArrayList<>();
            List<String> purchaseNumber=new ArrayList<>();
            //每一天---每一条请求
            for (int j = 0; j < requestForiDay.size(); j++) {
                List<String> strings = requestForiDay.get(j);
                if (strings.size()<2){//del

                }else {//add 0:虚拟机id；(本版本不考虑删除所以不适用)1：虚拟机型号
                    List<Integer> thisVirtualTypelist = hmForM_virtualType.get(strings.get(1));
                    int needCpu= thisVirtualTypelist.get(0);
                    int needMem=thisVirtualTypelist.get(1);
                    int isDouble=thisVirtualTypelist.get(2);
                    if (needCpu<cpuRemain && needMem<memRemain){
                        cpuRemain-=needCpu;memRemain-=needMem;
                    }else {//买新服务器
                        int selectedServerIndex= (int) (Math.random()*serverPool.size());
                        String selectServerType = serverPool.get(selectedServerIndex);
                        List<Integer> thisServerTypeList = hmForN_serverType.get(selectServerType);
                        cpuRemain=thisServerTypeList.get(0)- (isDouble==0?needCpu*2:needCpu);
                        memRemain=thisServerTypeList.get(1)- (isDouble==0?needMem*2:needMem);

                        purchaseType.add(selectServerType);purchaseNumber.add(String.valueOf(1));
                    }
                    if (isDouble==1){
                        assignList.add((serverCountBefore+purchaseType.size()-1)+"");
//                        assignList.add((serverCountBefore+purchaseType.size()-1)+""+"  debug:"+cpuRemain+"--"+memRemain);
                    }else {
                        assignList.add((serverCountBefore+purchaseType.size()-1)+", A");
//                        assignList.add((serverCountBefore+purchaseType.size()-1)+", A"+"  debug:"+cpuRemain+"--"+memRemain);
                    }
                }
            }
            LinkedHashMap<String,Integer> lhm=new LinkedHashMap();
            for (int j = 0; j < purchaseType.size(); j++) {
                if (lhm.containsKey(purchaseType.get(j))){
                    lhm.put(purchaseType.get(j),lhm.get(purchaseType.get(j))+1);
                }else {
                    lhm.put(purchaseType.get(j),1);
                }
            }
            outputString("(purchase, "+lhm.size() +")");
//            System.out.println("(purchase, "+purchaseNumber.size()+")");
            Iterator<Map.Entry<String, Integer>> iterator_lhm = lhm.entrySet().iterator();
            while (iterator_lhm.hasNext()) {
                Map.Entry<String, Integer> next = iterator_lhm.next();
                outputString("("+next.getKey()+", "+next.getValue()+")");
//                System.out.println("("+purchaseType.get(j)+", "+purchaseNumber.get(j)+")");
            }
            outputString("(migration, 0)");
//            System.out.println("(migration, 0)");
            for (int j = 0; j < assignList.size(); j++) {
                outputString("("+assignList.get(j)+")");
//                System.out.println("("+assignList.get(j)+")");
            }
            serverCountBefore+=purchaseNumber.size();
        }

//        FileUtil.writeString(sb.toString(),outputPath,"utf-8");
    }
    public static void generateEasyServerPool(){
        serverPool=new ArrayList<>();
        Iterator<Map.Entry<String, List<Integer>>> iterator = hmForM_virtualType.entrySet().iterator();
        int virtualMaxCpu=-1,virtualMaxMem=-1;
        while (iterator.hasNext()){
            Map.Entry<String, List<Integer>> next = iterator.next();
            List<Integer> value = next.getValue();
            int isDouble=value.get(2);
            int cpu=value.get(0);
            int mem=value.get(1);
            virtualMaxCpu=Math.max(virtualMaxCpu,cpu);
            virtualMaxMem=Math.max(virtualMaxMem,mem);
        }
        Iterator<Map.Entry<String, List<Integer>>> iterator1 = hmForN_serverType.entrySet().iterator();
        int j=0;
        while (iterator1.hasNext()){
            Map.Entry<String, List<Integer>> next1 = iterator1.next();
            List<Integer> value = next1.getValue();
            if (value.get(0)/2>=virtualMaxCpu && value.get(1)/2>=virtualMaxMem){
//                System.out.println(j+"  "+next1.getKey()+value.toString());
                serverPool.add(next1.getKey());
            }
            j++;
        }
    }

}
