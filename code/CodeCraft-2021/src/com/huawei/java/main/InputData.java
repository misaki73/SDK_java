package com.huawei.java.main;



import java.io.*;
import java.util.*;



public class InputData {
    private static String path="E:/huaweiElite/training-2.txt";//地址

    public static int N=-1;//可以采购的服务器类型数量
    public static HashMap<String,List<Integer>> hmForN_serverType=new LinkedHashMap<>();;//(型号 | CPU核数,内存大小,硬件成本,每日能耗成本)
    public static HashMap<Integer, String> serverTypeid = new HashMap<>();


    public static int M=-1;//可以出售的虚拟机类型数量
    public static HashMap<String,List<Integer>> hmForM_virtualType=new LinkedHashMap<>();;//(型号 | CPU核数,内存大小,是否双节点部署)
    public static HashSet<String>  double_virtualType=new HashSet<>(); //(双节点虚拟机类型）
    public static int T=-1;//天数



    //add
    public static List<List<Integer>> allDayAppId = new ArrayList<>();//(每日申请ID）
    public static List<LinkedHashMap<String,Integer>> allTDayAppList = new ArrayList();
    public static List<LinkedHashMap<String,Integer>> allTDayAppList_double = new ArrayList(); //(双节点虚拟机 每日申请需求，虚拟机ID，以及类型）
    public static List<LinkedHashMap<String,Integer>> allTDayAppList_single = new ArrayList(); //(单节点虚拟机 每日申请需求，虚拟机ID，以及类型）
    //del
    public static List<Set<Integer>> allTDayDelList= new ArrayList(); //(每日删除ID)

    public static List<LinkedHashMap<Integer,String>> allTDayActList = new ArrayList() ; //(每日现存需求 虚拟机ID）



    public static void initial(){
        List<String>origenalDataList = readLogByList(path);
        int next=0;

        /* 输入服务器数据 */
        N=Integer.parseInt(origenalDataList.get(next++));
        for (int i = 0; i < N; i++) {
            String s=origenalDataList.get(next++);
            String[] strings = disgardBracket(s);
            List<Integer> tempList=new ArrayList<>();//CPU,核数,内存大小,硬件成本,每日能耗成本
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }
            hmForN_serverType.put(strings[0],tempList);
            serverTypeid.put(i,strings[0]);
        }

        /* 输入虚拟机数据 */
        M=Integer.parseInt(origenalDataList.get(next++));
        for (int i = 0; i < M; i++) {
            String s=origenalDataList.get(next++);
            String[] strings = disgardBracket(s);
            List<Integer> tempList=new ArrayList<>();//CPU ,核数,内存大小,是否双节点部署
            for (int j = 1; j < strings.length; j++) {
                tempList.add(Integer.parseInt(strings[j]));
            }
            hmForM_virtualType.put(strings[0],tempList);
            if(tempList.get(2)==1){
                double_virtualType.add(strings[0]);
            }
        }

        /* 每日申请删除数据 */
        T=Integer.parseInt(origenalDataList.get(next++));
        LinkedHashMap<Integer,String> everyDayActId = new LinkedHashMap();
        for (int i = 0; i < T; i++) {
            //add
            List<Integer> everyDayAppId = new ArrayList<>();//(每日申请ID）
            LinkedHashMap<String,Integer> everyDayAppList = new LinkedHashMap<>();
            LinkedHashMap<String,Integer> everyDayAppList_double = new LinkedHashMap<>(); //(双节点虚拟机 每日申请需求，虚拟机ID，以及类型）
            LinkedHashMap<String,Integer> everyDayAppList_single = new LinkedHashMap<>(); //(单节点虚拟机 每日申请需求，虚拟机ID，以及类型）
            //del
            HashSet<Integer> everyDayDelList= new HashSet<>(); //(每日删除ID)


            int R=Integer.parseInt(origenalDataList.get(next++));
            for (int j = 0; j < R; j++) {
                String s=origenalDataList.get(next++);
                String[] strings = disgardBracket(s);

                if (strings.length<3){//del
                    //虚拟机id
                    everyDayActId.remove(Integer.parseInt(strings[1]));
                    everyDayDelList.add(Integer.parseInt(strings[1]));
                }else {//add
                    //虚拟机id，虚拟机型号

                    int id=Integer.parseInt(strings[2]);
                    everyDayAppId.add(id);
                    everyDayActId.put(id,strings[1]);

                    /*保证 删除*/
                    if(everyDayDelList.contains(id)){
                        everyDayDelList.remove(id);
                    }
                    everyDayAppList.put(strings[1],id);
                    if(double_virtualType.contains(strings[1])){//是双节点
                        everyDayAppList_double.put(strings[1],id);
                    }else{//单节点
                        everyDayAppList_single.put(strings[1],id);
                    }
                }


            }

            LinkedHashMap<Integer,String> temp = new LinkedHashMap();
            temp = (LinkedHashMap<Integer, String>) everyDayActId.clone();
            //add
            allDayAppId.add(everyDayAppId);
            allTDayAppList_double.add(everyDayAppList_double);
            allTDayAppList_single.add(everyDayAppList_single);
            allTDayAppList.add(everyDayAppList);

            //del
            allTDayDelList.add(everyDayDelList);
            //统计现存的id
            allTDayActList.add(temp);

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
}
