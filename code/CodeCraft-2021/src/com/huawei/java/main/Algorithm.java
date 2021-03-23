package com.huawei.java.main;

import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.Virtual;

import java.util.*;

public class Algorithm extends com.huawei.java.main.Base{
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

    public static void computeAssignment(){

        setvirtualMap();
        for(int day=0;day<1;day++){
            setPurRecommend(day);

            processApplication(day);

            //根据今天的请求信息输出结果
            printDayOutput(day);

            //删除操作
         //  executeDel(day);

            if(day==134){
                System.out.println();
            }
            //计算money
            money+=calMoney(day);
        }
        System.out.println(money);
    }
    public static long calMoney(int day){
        long money=0;
        LinkedHashMap<String,Integer> todayPurScheme = allDaypurchaseScheme.get(day);
      //  System.out.println("(purchase, "+todayPurScheme.size()+")");
        for (Map.Entry<String, Integer> entry : todayPurScheme.entrySet()) {
            List<Integer> list = hmForN_serverType.get(entry.getKey());
            money +=list.get(2)*entry.getValue();
        }
        for(Server server:serverList){
            if(server.getVirtualList()!=null){
                money+=hmForN_serverType.get(server.getTypeName()).get(3);
            }
        }
        return money;
    }

    public static void executeDel(int day){
        for (int i = 0; i < allTDayDelList.get(day).size(); i++) {
            int vsid = allTDayDelList.get(day).get(i);
            Virtual virtual = virtualMap.get(vsid);

            int serverid = virtualMap.get(vsid).getDeployedServerId();
            Server server = (Server)serverMap.get(virtual.getDeployedServerId());

            try {
                server.delVirtual((Virtual)virtualMap.get(allTDayDelList.get(day).get(i)));
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }
    }

    public static void printDayOutput(int day){
        //输出购买信息
        LinkedHashMap<String,Integer> todayPurScheme = allDaypurchaseScheme.get(day);
        System.out.println("(purchase, "+todayPurScheme.size()+")");
        for (Map.Entry<String, Integer> entry : todayPurScheme.entrySet()) {
            System.out.println("("+entry.getKey()+", "+entry.getValue()+")");
        }

        //输出迁移信息
        System.out.println("(migration, 0)");

        //输出今天请求结果
        List<List<Integer>> todayAppLits = allTDayAddList.get(day);
        for (int i=0;i<todayAppLits.size(); i++){
            int id=todayAppLits.get(i).get(0);
            int id_outer=myidToOut.get(virtualMap.get(id).getDeployedServerId());//根据虚拟机当前部署的服务器的内部id获取其外部id
            System.out.print("("+id_outer);
            if(virtualMap.get(id).getDeployedServerNode()==1){
                System.out.print(", A");
            }else if(virtualMap.get(id).getDeployedServerNode()==2){
                System.out.print(", B");
            }
            System.out.println(")");
        }
    }
    public static void setPurRecommend(int day){
        purRecommend = new ArrayList<>();
        TreeMap<Double,String> scores = new TreeMap<>();
        for (Map.Entry<String, List<Integer>> stype :  hmForN_serverType.entrySet()){
            double score = stype.getValue().get(2) + stype.getValue().get(3);
            scores.put(score,stype.getKey());
        }
        for (Map.Entry<Double, String> entry : scores.entrySet()) {
            purRecommend.add(entry.getValue());
        }

    }

    public static void setvirtualMap(){

        for(int day=0;day<T;day++){
            for(Map.Entry<Integer,List<Integer>> temp:DayAddDoubleList_rmDouble.get(day).entrySet()){
                //String name=allTDayAppNameList.get(day).get(i);
                Virtual v=new Virtual(temp.getValue(),1);
                virtualMap.put(v.getId(),v);

            }
            for(Map.Entry<Integer,List<Integer>> temp:DayAddSingleList_rmDouble.get(day).entrySet()){
                //String name=allTDayAppNameList.get(day).get(i);
                Virtual v=new Virtual(temp.getValue(),0);
                virtualMap.put(v.getId(),v);

            }
        }

    }

    /* 处理每日申请购买*/
    public static void processApplication(int day){
        LinkedHashMap<String,Integer> todayPurScheme = new LinkedHashMap<>();
        int servernum = 0 ;
        int len=serverList.size();
        List<List<Integer>> list_ = allTDayAddList.get(day);
        for(List<Integer> l:list_){
            int vsid=l.get(0);
            boolean succeed=false;
            Virtual virtual=virtualMap.get(vsid);

            for(int i = 0;i<serverList.size();i++){
                Server servers=serverList.get(i);
                if(servers.canBeSet(virtual)){

                    if(!servers.setVirtual(virtual)){
                        System.err.println("处理每日申请 设置服务器时错误");
                        break;
                    }else {
                        succeed=true;
                        servernum = i;
                        break;
                    }
                }
            }
//            System.out.println("XX");
            if(succeed ==false){

                //买新的服务器
                //购买策略推荐
                List<Integer> list=new ArrayList<>();
                for(int i=0;i<N;i++){
                //    System.out.println(purRecommend.get(i));
                    list=hmForN_serverType.get(purRecommend.get(i));
                    if(list.get(0)/2>=virtual.getRequiredCPU()/Math.pow(2,virtual.getIsDouble())
                    && list.get(1)/2>=virtual.getRequiredMem()/Math.pow(2,virtual.getIsDouble())){
                        int num=todayPurScheme.getOrDefault(purRecommend.get(i), 0)+1;
                        todayPurScheme.put(purRecommend.get(i),num);
                        Server newServer = new Server(purRecommend.get(i),list.get(0),list.get(1),1);
                        serverMap.put(newServer.getId(),newServer);
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
  //          System.out.println("XX");
        }

        /**/



        allDaypurchaseScheme.add(todayPurScheme);
        int count = 0;
        HashMap<String,Integer> outId=new HashMap<>();
        for (Map.Entry<String, Integer> entry : todayPurScheme.entrySet()) {
            for (int j = 0; j < entry.getValue(); j++) {
                for(int i=0;i<serverList.size()-len;i++) {
                    Server ser =serverList.get(len+i);
                    if(entry.getKey().equals(ser.getTypeName())){
                        if(!myidToOut.containsKey(ser.getId())) {
                            myidToOut.put(ser.getId(),len+count);
                            break;
                        }
                    }
                }
                count++;
            }
        }
        /*将新增加的服务器 id 与 判断器 id 对应*/
    }
    /*今日购买策略推荐*/

    /*首日购买清单*/
/*   public static void firstDayBuy(){
        //计算总需求
        virtualMap=new LinkedHashMap<>();
        cpus=new ArrayList<>();
        mems=new ArrayList<>();
        int dayAllCpu=0;int dayAllMem=0;
        for (int i = 0; i < allTDayActList.size(); i++) {
            List<List<String>> dayAct = allTDayActList.get(i);
            for (int j = 0; j < dayAct.size(); j++) {
                List<String> listforthisDay = dayAct.get(j);
                if (listforthisDay.size()>=2){//add
                    List<Integer> listforthisTypeVirtual = hmForM_virtualType.get(listforthisDay.get(1));
                    Virtual addedVirtual=new Virtual(Integer.parseInt(listforthisDay.get(0)),listforthisDay.get(1),
                            listforthisTypeVirtual.get(0),listforthisTypeVirtual.get(1),listforthisTypeVirtual.get(2));
                    virtualMap.put(addedVirtual.getId(),addedVirtual);
                    dayAllCpu+=addedVirtual.getRequiredCPU();
                    dayAllMem+=addedVirtual.getRequiredMem();
                }else {//del
                    Virtual virtual = virtualMap.get(Integer.parseInt(listforthisDay.get(0)));
                    virtual.setIsDel(true);
                    dayAllCpu-=virtual.getRequiredCPU();
                    dayAllMem-=virtual.getRequiredMem();
                }
            }
            cpus.add(dayAllCpu);mems.add(dayAllMem);
        }
        //计算 能满足所有虚拟机存量需求的 服务器类型
        HashMap <String, List<Integer>> bigServers=new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : hmForN_serverType.entrySet()) {
            if(entry.getValue().get(0)/2 >= vsMaxCpu && entry.getValue().get(0)/2 >= vsMaxMemory){
                bigServers.put(entry.getKey(),entry.getValue());
            }
        }

        //计算购买量
        int range=10;
        List<Integer> cpus_1 = new ArrayList<>(cpus);
        List<Integer> mems_1 = new ArrayList<>(mems);
        List<Integer> l_cpus = new ArrayList();
        List<Integer> l_mems = new ArrayList();
        Collections.sort(cpus_1);
        Collections.sort(mems_1);
        l_cpus.add(cpus_1.get(0));
        l_mems.add(mems_1.get(0));
        for (int i = 1; i < cpus_1.size() / range; i++) {
            l_cpus.add(cpus_1.get(i * range) - cpus_1.get((i - 1) * range));
            l_mems.add(mems_1.get(i * range) - mems_1.get(i - 1) * range);
        }
        if (cpus_1.size() % range != 0) {
            l_cpus.add(cpus_1.get(cpus_1.size()) - cpus_1.get(cpus_1.size() - cpus_1.size() % range));
            l_mems.add(mems_1.get(cpus_1.size()) - mems_1.get(cpus_1.size() - cpus_1.size() % range));
        }
    }
    */



}
