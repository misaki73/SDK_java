package com.huawei.java.main;

//import cn.hutool.core.io.FileUtil;

import java.io.*;
import java.util.*;
/**
 * 仅用于数据输入、输出
 * */
public class Base {
    public static Boolean isCommit=false;
    public static String path="E:/huaweiElite/training-1.txt";
    public static List<String> origenalDataList;//原始数据
    public static int N=-1;//可以采购的服务器类型数量
    public static HashMap<String,List<Integer>> hmForN_serverType;//(型号 | CPU核数,内存大小,硬件成本,每日能耗成本)
    public static int M=-1;//可以出售的虚拟机类型数量
    public static HashMap<String,List<Integer>> hmForM_virtualType;//(型号 | CPU核数,内存大小,是否双节点部署)
    public static int T=-1;//天数
    public static int vsMaxCpu = -1;
    public static int vsMaxMemory = -1;
   // public static double sumCpu=0;
   // public static double sumMem=0;
    /**
     * - 共T天，每天R条数据
     * - 每条数据为一个List：
     * -- size=1 表示del 虚拟机id； size=2表示 add ，虚拟机id，虚拟机型号
     * */


    //天 请求 （id cpu mem）
    public static List<HashMap<Integer,List<Integer>>> allTDayActList;
    /**
     * 天，（id : [id, cpu，mem]）* 请求
     * */
    public static List<HashMap<Integer,List<Integer>>> DayAddSingleList_rmDouble =new ArrayList<>();
    public static List<HashMap<Integer,List<Integer>>> DayAddDoubleList_rmDouble=new ArrayList<>();

    //天 请求 （id cpu mem）
    public static List<List<List<Integer>>> allTDayAddList= new ArrayList();

    // 天 [虚拟机id]*请求
    public static List<List<Integer>> allTDayDelList= new ArrayList();

    /**
     * 限制参数
     * */
    public static int maxServerNum=100000;
    /**
     * 初始化以及清洗数据
     * */
    public static void initial_standard1(){
//        origenalDataList = readLogByList(path + "training-2.txt");
        hmForM_virtualType =new LinkedHashMap<>();
        hmForM_virtualType =new LinkedHashMap<>();
        hmForN_serverType=new LinkedHashMap<>();
        allTDayActList=new ArrayList<>();
//        everyTDayActList=new ArrayList<>();
        allTDayAddList= new ArrayList();
        allTDayDelList =new ArrayList<>();
        allTDayActList =new ArrayList<>();
        DayAddSingleList_rmDouble =new ArrayList<>();
        DayAddDoubleList_rmDouble = new ArrayList<>();

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
            List<Integer> tempList=new ArrayList<>();//CPU核数,内存大小,是否双节点部署
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }
            List<Integer> tempList1=new ArrayList<>();
            for (int j = 1; j < strings.length-1; j++) {//CPU核数,内存大小
                tempList1.add(Integer.parseInt(strings[j]));
            }
            vsMaxCpu= tempList.get(0)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(0) / Math.pow(2, tempList.get(2))) : vsMaxCpu;
            vsMaxMemory= tempList.get(1)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(1) / Math.pow(2, tempList.get(2))) : vsMaxCpu;
            //sumCpu += tempList.get(0);
           // sumMem += tempList.get(1);

            hmForM_virtualType.put(strings[0],tempList);

        }
        T=Integer.parseInt(in.nextLine());
        for (int i = 0; i < T; i++) {
            int R=Integer.parseInt(in.nextLine());
            //天，请求，（cpu，mem，id）
            List<List<Integer>> addSingleList=new ArrayList<>();
            List<List<Integer>> addDoubleList=new ArrayList<>();
            //天，（id : cpu，mem）* 请求
            HashMap<Integer,List<Integer>> hmSingle=new HashMap<>();
            HashMap<Integer,List<Integer>> hmDouble=new HashMap<>();
            List<Integer> tempdelList=new ArrayList<>();
            HashMap<Integer,List<Integer>> everyTDayActList=new HashMap<>();
            List<List<Integer>> everyTDayAddList=new ArrayList<>();
            for (int j = 0; j < R; j++) {
                String s=in.nextLine();
                String[] strings = disgardBracket(s);
                List<Integer> tempLH=new ArrayList<>();
                List<Integer> tempAddOnly=new ArrayList<>();
                if (strings.length<3){//del
                    //虚拟机id
                    tempLH.add(Integer.valueOf(strings[1]));
                    tempdelList.add(Integer.parseInt(strings[1]));
                }else {
                    //虚拟机id，cpu,mem
                    int id =Integer.valueOf(strings[2]);
                    int cpuReq=hmForM_virtualType.get(strings[1]).get(0);
                    int memReq=hmForM_virtualType.get(strings[1]).get(1);
                    int isDouble=hmForM_virtualType.get(strings[1]).get(2);
                    if (isDouble==1){
                        List<Integer> l_temp=new ArrayList<>();
                        l_temp.add(id);
                        l_temp.add(cpuReq);
                        l_temp.add(memReq);
                        hmDouble.put(id,l_temp);
                    }else {
                        List<Integer> l_temp=new ArrayList<>();
                        l_temp.add(id);
                        l_temp.add(cpuReq);
                        l_temp.add(memReq);
                        hmSingle.put(id,l_temp);
                    }
                    tempLH.add(id);
                    tempLH.add(cpuReq);
                    tempLH.add(memReq);
                    tempAddOnly.add(id);
                    tempAddOnly.add(cpuReq);
                    tempAddOnly.add(memReq);
                    everyTDayAddList.add(tempAddOnly);

                }
               // everyTDayActList.add(tempLH);

            }
            allTDayAddList.add(everyTDayAddList);
            allTDayActList.add(everyTDayActList);
            allTDayDelList.add(tempdelList);
            DayAddDoubleList_rmDouble.add(hmDouble);
            DayAddSingleList_rmDouble.add(hmSingle);
        }
    }

    public static void initial(){
        origenalDataList = readLogByList(path);
        hmForM_virtualType =new LinkedHashMap<>();
        hmForM_virtualType =new LinkedHashMap<>();
        hmForN_serverType=new LinkedHashMap<>();

//        everyTDayActList=new ArrayList<>();
        allTDayAddList= new ArrayList();
        allTDayDelList =new ArrayList<>();
        allTDayActList =new ArrayList<>();
        DayAddSingleList_rmDouble =new ArrayList<>();
        DayAddDoubleList_rmDouble = new ArrayList<>();

        int next=0;
        N=Integer.parseInt(origenalDataList.get(next++));
        for (int i = 0; i < N; i++) {
            String s=origenalDataList.get(next++);
            String[] strings = disgardBracket(s);
            List<Integer> tempList=new ArrayList<>();//CPU,核数,内存大小,硬件成本,每日能耗成本
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }
            List<Integer> tempList1=new ArrayList<>();
            for (int j = 1; j < strings.length-1; j++) {//CPU核数,内存大小
                tempList1.add(Integer.parseInt(strings[j]));
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
            vsMaxCpu= tempList.get(0)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(0) / Math.pow(2, tempList.get(2))) : vsMaxCpu;
            vsMaxMemory= tempList.get(1)/Math.pow(2,tempList.get(2)) > vsMaxCpu ? (int) (tempList.get(1) / Math.pow(2, tempList.get(2))) : vsMaxCpu;
           // sumCpu += tempList.get(0);
           // sumMem += tempList.get(1);

            hmForM_virtualType.put(strings[0],tempList);
        }

        HashMap<Integer,List<Integer>> nowActList=new HashMap<>();
        T=Integer.parseInt(origenalDataList.get(next++));
        for (int i = 0; i < T; i++) {
            int R=Integer.parseInt(origenalDataList.get(next++));
            //天，请求，（cpu，mem，id）
            List<List<Integer>> addSingleList=new ArrayList<>();
            List<List<Integer>> addDoubleList=new ArrayList<>();
            //天，（id : cpu，mem）* 请求
            HashMap<Integer,List<Integer>> hmSingle=new HashMap<>();
            HashMap<Integer,List<Integer>> hmDouble=new HashMap<>();
            List<Integer> tempdelList=new ArrayList<>();
            List<List<Integer>> everyTDayAddList=new ArrayList<>();
            for (int j = 0; j < R; j++) {
                String s=origenalDataList.get(next++);
                String[] strings = disgardBracket(s);
                List<Integer> tempLH=new ArrayList<>();
                List<Integer> tempAddOnly=new ArrayList<>();
                if (strings.length<3){//del
                    //虚拟机id
                    nowActList.remove(Integer.valueOf(strings[1]));
                    tempLH.add(Integer.valueOf(strings[1]));
                    tempdelList.add(Integer.parseInt(strings[1]));
                }else {
                    //虚拟机id，cpu,mem
                    int id =Integer.valueOf(strings[2]);
                    int cpuReq=hmForM_virtualType.get(strings[1]).get(0);
                    int memReq=hmForM_virtualType.get(strings[1]).get(1);
                    int isDouble=hmForM_virtualType.get(strings[1]).get(2);
                    if (isDouble==1){
                        List<Integer> l_temp=new ArrayList<>();
                        l_temp.add(id);
                        l_temp.add(cpuReq);
                        l_temp.add(memReq);
                        l_temp.add(isDouble);
                        hmDouble.put(id,l_temp);
                        nowActList.put(id,l_temp);
                    }else {
                        List<Integer> l_temp=new ArrayList<>();
                        l_temp.add(id);
                        l_temp.add(cpuReq);
                        l_temp.add(memReq);
                        l_temp.add(isDouble);
                        hmSingle.put(id,l_temp);
                        nowActList.put(id,l_temp);
                    }
                    tempLH.add(id);
                    tempLH.add(cpuReq);
                    tempLH.add(memReq);
                    tempAddOnly.add(id);
                    tempAddOnly.add(cpuReq);
                    tempAddOnly.add(memReq);
                    everyTDayAddList.add(tempAddOnly);
                }

            }
            HashMap<Integer,List<Integer>> everyTDayActList=new HashMap<>(nowActList);
            allTDayAddList.add(everyTDayAddList);
            allTDayActList.add(everyTDayActList);
            allTDayDelList.add(tempdelList);
            DayAddDoubleList_rmDouble.add(hmDouble);
            DayAddSingleList_rmDouble.add(hmSingle);
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

}
