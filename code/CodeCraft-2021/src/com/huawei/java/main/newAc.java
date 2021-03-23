package com.huawei.java.main;

import com.huawei.java.main.entity.Server;
import com.huawei.java.main.entity.Virtual;

import java.util.*;

public class newAc extends Base {
    public static List<Server> serverList = new ArrayList<>();
    public static LinkedHashMap<Integer, Virtual> virtualMap = new LinkedHashMap<>(); //id,虚拟机对象，全量存
    public static LinkedHashMap<Integer, Server> serverMap= new LinkedHashMap<>();; //id,服务器对象，全量存
    public static List<LinkedHashMap<String,Integer>> allDaypurchaseScheme = new ArrayList<>();; //每天的购买清单
    public static List<List<List<Integer>>> allDayMigration = new ArrayList<>();//每日迁移信息
    public static HashMap<Integer,Integer> myidToOut = new HashMap<>(); //服务器内部ID，与外部ID
    public static Set<Server> unfullServerList = new HashSet<>();
    public static Set<Server> unfullServerList_double = new HashSet<>();
    public static Set<Server> unfullServerList_single = new HashSet<>();


    public static HashMap<Integer,Server> serverMigrationAll;
    public static HashMap<Integer,HashSet<Server>> serverListMigration;
    public static double MigrationPercentageUpper=0.51;//a，b节点cpu,mem都小于该比例则进入待迁移list
    public static double stepForMigrationMap =0.01;
    public static List<Integer> generatedKeys;//从小到大,表示上限百分比
    public static double MigrationPercentageLower=0.01;//防止新节点进入迁移list
    public static int sumVirtual=0;




    public static void initialSeverList(){
        serverMigrationAll=new HashMap<>();
        serverListMigration =new HashMap<>();
        generatedKeys=new ArrayList<>();
        int setNum=(int)(MigrationPercentageUpper/ stepForMigrationMap);
        for (int i = 1; i <=setNum ; i++) {
            int key=(int) (i*stepForMigrationMap*100);
            generatedKeys.add(key);
            serverListMigration.put(key,new HashSet<>());
        }
    }

    public static void process(){
        long[] money={0,0};
        int m=0;
        initialSeverList();
        for (int day=0;day<1;day++){
            System.err.println(day);

            // 迁移
            migration();
            //处理添加
            processApplication(day);
            //根据今天的请求信息输出结果
           // printDayOutput(day);
            //删除操作
            executeDel(day);
            m+=allDayMigration.get(day).size();

            long[] a = calMoney(day);
            money[0]+=a[0];
            money[1]+=a[1];
            System.err.println("现有服务器数量:"+serverList.size());
        }
        long a=money[0]+money[1];
        System.err.println("硬件设备是:"+money[0]+";  软件设备是："+money[1]+";    总花费:"+a);
        System.err.println(m);
    }

    public static void migration() {
        int n = 0;
        int start = 0, end = unfullServerList.size() - 1;
        //先分配再删除
        Stack<Server> unfullServer = new Stack<>();
        TreeMap<Integer,Server> scores = new TreeMap<>();
        List<List<Integer>> unallocated_double = new ArrayList();
        List<List<Integer>> unallocated_single = new ArrayList();
        HashMap<Integer,Server> befor_server = new HashMap<>();//存下释放前的server
        for(Server s:unfullServerList){
            scores.put(s.getaCpuUse()+s.getbCpuUse()+s.getaMemUse()+s.getbMemUse(),s);
        }
        for (Map.Entry<Integer,Server> entry : scores.entrySet()) {
            Server s=new Server(entry.getValue());
            unfullServer.push(s);
            if(n+s.getVirtualList().size()<5 * sumVirtual / 1000){
                n+=s.getVirtualList().size();
                for (int i = 0; i < s.getVirtualList().size(); i++) {
                    Virtual v = virtualMap.get(s.getVirtualList().get(i));
                    if(v.getDeployedServerId()==-1){
                        System.err.println("xxxx");
                    }
                    List<Integer> l = new ArrayList<>();
                    l.add(v.getId());
                    l.add(v.getRequiredCPU());
                    l.add(v.getRequiredMem());
                    befor_server.put(v.getId(),s); //存下服务器和对应的虚拟服务器
                    if (v.getIsDouble() == 0) {
                        unallocated_single.add(l);
                    } else {
                        unallocated_double.add(l);
                    }
                  //   if(!s.delVirtual_test(v)){// 先不删除
                  //      System.err.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                  //  }

                }

            }
        }

        // serverid vid,a or b;
        List<List<Integer>> set = new ArrayList<>();//serverid virtualid node
        Set<Server> unfull_double = new HashSet<>();
        while(!unfullServer.empty()){
            Server s=unfullServer.pop();
            unfull_double.add(s);
        }
        Set<Server> unfull_single = new HashSet<>(unfull_double);
        while (unallocated_double.size()!=0) {//分配双节点

            for (Server server : unfull_double) {
                int[] capacity = new int[2];// 分配双
                capacity[0] = Math.min(server.getRemainedCpuA(), server.getRemainedCpuB());
                capacity[1] = Math.min(server.getRemainedMemA(), server.getRemainedMemB());
                if (Math.min(capacity[0], capacity[1]) != 0) { // 只要不等于0就可以试试看反正就跑一边而已
                    List<Integer> numbers = ac_double(capacity, unallocated_double);
                    Collections.sort(numbers);
                    for (Integer i : numbers) {//将分配结果匹配到对应的server上;
                        List<Integer> l = new ArrayList<>();
                        l.add(server.getId());
                        l.add(unallocated_double.get(i).get(0));
                        Virtual v=virtualMap.get(l.get(1));
                        if(v.getDeployedServerId()!=l.get(0)) {//不在原来的位置上，部署在新的服务器上
                            if(!server.setVirtual_test(v)){
                                System.err.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                            }
                            Server befor = befor_server.get(l.get(1));
                            if(!befor.delVirtual_test(v)) { //删除原来服务器上虚拟机
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            }
                            set.add(l);
                            if(virtualMap.get(l.get(1)).getDeployedServerId()==-1){
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            }
                        }
                    }
                    int count = 0;
                    for (Integer i : numbers) {//删除未分配的点
                        unallocated_double.remove(i - count);
                        count++;
                    }
                    if(unallocated_double.size()==0){
                        break;
                    }
                }
            }
            break;
        }
        while (unallocated_single.size()!=0){
            for(Server server:unfull_single){
                int[] capacity = new int[2];
                int flag=0;
                capacity[0]=server.getRemainedCpuA();
                capacity[1]=server.getRemainedMemA();
                if(Math.min(capacity[0],capacity[1])!=0){
                    List<Integer> numbers=ac_single(capacity, unallocated_single);
                    Collections.sort(numbers);
                    for(Integer i:numbers){//将分配结果匹配到对应的server上;
                        List<Integer> l=new ArrayList<>();
                        l.add(server.getId());
                        l.add(unallocated_single.get(i).get(0));
                        l.add(1);
                        Virtual v=virtualMap.get(l.get(1));
                        if(virtualMap.get(l.get(1)).getDeployedServerId()!=l.get(0) || virtualMap.get(l.get(1)).getDeployedServerNode()!=1){
                            //不在同一个位置，删除原来服务器上vs
                            Server befor=befor_server.get(l.get(1));
                            if(!befor.delVirtual_test(v)){
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            };
                            if(!server.setVirtual_test(v,1)){
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            };
                            set.add(l);
                            if(virtualMap.get(l.get(1)).getDeployedServerId()==-1){
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            }
                        }

                    }
                    int count=0;
                    for(Integer i:numbers){//删除未分配的点
                        unallocated_single.remove(i-count);
                        count++;
                    }
                    if(unallocated_single.size()==0){
                        break;
                    }
                }
                //b 节点
                //int[] capacity=new int[2];
                capacity[0]=server.getRemainedCpuB();
                capacity[1]=server.getRemainedMemB();
                if(Math.min(capacity[0],capacity[1])!=0){
                    List<Integer> numbers=ac_single(capacity, unallocated_single);
                    Collections.sort(numbers);
                    for(Integer i:numbers){//将分配结果匹配到对应的server上;
                        List<Integer> l=new ArrayList<>();
                        l.add(server.getId());
                        l.add(unallocated_single.get(i).get(0));
                        l.add(2);
                        Virtual v=virtualMap.get(l.get(1));

                        if(virtualMap.get(l.get(1)).getDeployedServerId()!=l.get(0) || virtualMap.get(l.get(1)).getDeployedServerNode()!=2) {
                            //不在同一位置，删除原来的
                            Server befor=befor_server.get(l.get(1));
                            if(!befor.delVirtual_test(v)){
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            };
                            if(!server.setVirtual_test(v,2)){
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            };
                            set.add(l);
                            if(virtualMap.get(l.get(1)).getDeployedServerId()==-1){
                                System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                            }
                        }
                    }
                    int count=0;
                    for(Integer i:numbers){//删除未分配的点
                        unallocated_single.remove(i-count);
                        count++;
                    }
                    if(unallocated_single.size()==0){
                        break;
                    }
                }
            }
            break;
        }
        if(unallocated_double.size()==0 && unallocated_single.size()==0){
            allDayMigration.add(set);
            for(List<Integer> l:set){
                Virtual v = virtualMap.get(l.get(1));
                Server s_berfor = serverMap.get(v.getDeployedServerId());//释放
                if(s_berfor==null){
                    System.err.println("xxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                }
                if(!s_berfor.delVirtual(v)){
                    System.err.println("xxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                }
            }

            //重新分配
            for(List<Integer> l:set){

                Server s_after=serverMap.get(l.get(0));
                Virtual v = virtualMap.get(l.get(1));
                if(l.size()==2){//添加
                    if(!s_after.setVirtual(v)){
                        System.err.println("xxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    }
                }else{
                    if(!s_after.setVirtual(v,l.get(2))){
                        System.err.println("xxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                    }
                }
            }
        }else {
            set = new ArrayList<>();
            allDayMigration.add(set);
        }
    }



    public static boolean moveToMigrationMap(Server s){//s添加或移除服务器后调用该方法
//        System.err.println(s.getId()+": \n"+s.getRemainedCpuAPercentage()+","+s.getRemainedCpuBPercentage()+","+
//                s.getRemainedMemAPercentage()+","+s.getRemainedMemBPercentage());
        HashSet<Server> hs=new HashSet<>();
        for (int i = 0; i < generatedKeys.size(); i++) {
            int key=generatedKeys.get(i);
            if (key>=s.MaxRemainedPercentage()*100) {
                hs = serverListMigration.get(key);
                break;
            }
        }
        if (s.MinUsedPercentage()<MigrationPercentageLower){
            hs.remove(s);
            if (serverMigrationAll.containsKey(s.getId())){
                serverMigrationAll.remove(s.getId());
                for (int i = 0; i < generatedKeys.size(); i++) {
                    int key=generatedKeys.get(i);
                    HashSet<Server> hs_temp = serverListMigration.get(key);
                    if (hs_temp.remove(s)){
                        break;
                    }
                }
            }
            return false;
        }
        if (s.MaxUsedPercentage()<MigrationPercentageUpper){
            if (serverMigrationAll.containsKey(s.getId())){
                serverMigrationAll.remove(s.getId());
                for (int i = 0; i < generatedKeys.size(); i++) {
                    int key=generatedKeys.get(i);
                    HashSet<Server> hs_temp = serverListMigration.get(key);
                    if (hs_temp.remove(s)){
                        break;
                    }
                }
            }
            hs.add(s);
            serverMigrationAll.put(s.getId(),s);
            return true;
        }
        if (serverMigrationAll.containsKey(s.getId())){
            serverMigrationAll.remove(s.getId());
            for (int i = 0; i < generatedKeys.size(); i++) {
                int key=generatedKeys.get(i);
                HashSet<Server> hs_temp = serverListMigration.get(key);
                if (hs_temp.remove(s)){
                    break;
                }
            }
        }
        hs.remove(s);
        return false;
    }




    public static long[] calMoney(int day){
        long[] money={0,0};
        int num=serverList.size();
        int n=0;
        LinkedHashMap<String,Integer> todayPurScheme = allDaypurchaseScheme.get(day);
       // System.out.println("(purchase, "+todayPurScheme.size()+")");
        for (Map.Entry<String, Integer> entry : todayPurScheme.entrySet()) {
            List<Integer> list = hmForN_serverType.get(entry.getKey());
            money[0] +=list.get(2)*entry.getValue();
            n+=entry.getValue();
        }
        for(Server server:serverList){
            if(server.getVirtualList()!=null && server.getVirtualList().size()!=0 ){
                money[1]+=hmForN_serverType.get(server.getTypeName()).get(3);
                num--;
            }
        }
        System.err.println("今日购买的服务器数："+n);
        System.err.println("今天空出来的服务器数："+num);
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
                sumVirtual--;
                unfullServerList.add(server);
                unfullServerList_double.add(server);
                unfullServerList_single.add(server);
              //  moveToMigrationMap(server);

            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }
    }

    public static void printDayOutput(int day) {
        //输出购买信息
        //输出购买信息
        LinkedHashMap<String, Integer> todayPurScheme = allDaypurchaseScheme.get(day);
        System.out.println("(purchase, " + todayPurScheme.size() + ")");
        for (Map.Entry<String, Integer> entry : todayPurScheme.entrySet()) {
            System.out.println("(" + entry.getKey() + ", " + entry.getValue() + ")");
        }

        //输出迁移信息
        List<List<Integer>> todayMirgration = allDayMigration.get(day);
        System.out.println("(migration, "+todayMirgration.size()+")");
        for (int i = 0; i < todayMirgration.size(); i++){
            int id = todayMirgration.get(i).get(0);
            int id_outer = myidToOut.get(id);
            int vsid = todayMirgration.get(i).get(1);
            System.out.print("(" + vsid+", "+id_outer);
            if (virtualMap.get(vsid).getDeployedServerNode() == 1) {
                System.out.print(", A");
            } else if (virtualMap.get(vsid).getDeployedServerNode() == 2) {
                System.out.print(", B");
            }
            System.out.println(")");
        }

        //输出今天请求结果
        List<List<Integer>> todayAppLits = allTDayAddList.get(day);
        for (int i = 0; i < todayAppLits.size(); i++) {
            int id = todayAppLits.get(i).get(0);
            int id_outer = myidToOut.get(virtualMap.get(id).getDeployedServerId());//根据虚拟机当前部署的服务器的内部id获取其外部id
            System.out.print("(" + id_outer);
            if (virtualMap.get(id).getDeployedServerNode() == 1) {
                System.out.print(", A");
            } else if (virtualMap.get(id).getDeployedServerNode() == 2) {
                System.out.print(", B");
            }
            System.out.println(")");
        }
    }



    public static void processApplication(int day){
        int len=serverList.size(); //当前server有多少 ！
        LinkedHashMap<String,Integer> todayPurScheme = new LinkedHashMap<>();//今日购买清单


        /* 双节点*/
        List<List<Integer>> unallocated_double = new ArrayList();//待分配的虚拟机有哪些
        int cpu=0,mem=0;
        for(Map.Entry<Integer,List<Integer>> temp:DayAddDoubleList_rmDouble.get(day).entrySet()){ //今日需要分配的双节点有
            unallocated_double.add(temp.getValue());
            cpu+=temp.getValue().get(1);
            mem+=temp.getValue().get(2);
        }
        List<String> purRecommend = setRecommend(day,cpu,mem);//今日购买推荐
        Set<Server> unfullServer_temp = new HashSet<>(unfullServerList_double);
        while (true) {
            List<Server> fullServers = new ArrayList<>();
            for(Server server:unfullServer_temp){
                int[] capacity=new int[2];
                capacity[0]=Math.min(server.getRemainedCpuA(),server.getRemainedCpuB());
                capacity[1]=Math.min(server.getRemainedMemA(),server.getRemainedMemB());
                if(Math.min(capacity[0],capacity[1])!=0){ // 只要不等于0就可以试试看反正就跑一边而已
                    List<Integer> numbers=ac_double(capacity, unallocated_double);
                    Collections.sort(numbers);
                    for(Integer i:numbers){//将分配结果匹配到对应的server上;
                        Virtual v=new Virtual(unallocated_double.get(i),1);//重新写了个Virtual的构造
                        virtualMap.put(v.getId(),v);
                        if(!server.setVirtual(v)){
                            System.err.println("xxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                        }
                        sumVirtual++;
                        //moveToMigrationMap(server);
                    }
                    int count=0;
                    for(Integer i:numbers){//删除未分配的点
                        unallocated_double.remove(i-count);
                        count++;
                    }
                }else{
                    fullServers.add(server);
                }
            }
            unfullServer_temp = new HashSet<>(); // 跑完一遍就清空
            for(Server server:fullServers){
                unfullServerList_double.remove(server);
            }
            if(unallocated_double.size()==0){
                break;
            }
            //不够买新
            int maxCpu=max(unallocated_double,1)/2;
            int maxMem=max(unallocated_double,2)/2;
            for(int i=0;i<N;i++){
                List<Integer> list=hmForN_serverType.get(purRecommend.get(i));
                if(list.get(0)/2>maxCpu && list.get(1)/2>maxMem){
                    int num=todayPurScheme.getOrDefault(purRecommend.get(i), 0)+1;
                    todayPurScheme.put(purRecommend.get(i),num);
                    Server newServer = new Server(purRecommend.get(i),list.get(0),list.get(1),1);
                    serverMap.put(newServer.getId(),newServer);
                    serverList.add(newServer);
                    unfullServerList.add(newServer);
                    unfullServerList_double.add(newServer);
                    unfullServerList_single.add(newServer);
                    unfullServer_temp.add(newServer);
                    break;
                }
            }
        }

        /* 单节点 */
        unfullServer_temp = new HashSet<>(unfullServerList_single);
        List<List<Integer>> unallocated_single = new ArrayList();
        cpu=0;mem=0;

        for(Map.Entry<Integer,List<Integer>> temp:DayAddSingleList_rmDouble.get(day).entrySet()){
            unallocated_single.add(temp.getValue());
            cpu+=temp.getValue().get(1);
            mem+=temp.getValue().get(2);
        }
        purRecommend = setRecommend(day,cpu,mem);
        while (true) {
            Set<Server> fullServers = new HashSet<>();
            for(Server server:unfullServer_temp){
                //a 节点
                int flag=0;
                int[] capacity=new int[2];
                capacity[0]=server.getRemainedCpuA();
                capacity[1]=server.getRemainedMemA();
                if(Math.min(capacity[0],capacity[1])!=0){
                    List<Integer> numbers=ac_single(capacity, unallocated_single);
                    Collections.sort(numbers);
                    for(Integer i:numbers){//将分配结果匹配到对应的server上;
                        Virtual v=new Virtual(unallocated_single.get(i),0);//重新写了个Virtual的构造
                        virtualMap.put(v.getId(),v);
                        if(!server.setVirtual(v,1)){
                            System.err.println("xxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                        };// 安排在了a节点
                        //moveToMigrationMap(server);
                        sumVirtual++;
                    }
                    int count=0;
                    for(Integer i:numbers){//删除未分配的点
                        unallocated_single.remove(i-count);
                        count++;
                    }
                }else{
                    flag+=1;
                    if(unfullServerList_double.contains(server)) {
                        unfullServerList_double.remove(server);
                    }
                }
                //b 节点
                //int[] capacity=new int[2];
                capacity[0]=server.getRemainedCpuB();
                capacity[1]=server.getRemainedMemB();
                if(Math.min(capacity[0],capacity[1])!=0){
                    List<Integer> numbers=ac_single(capacity, unallocated_single);
                    Collections.sort(numbers);
                    for(Integer i:numbers){//将分配结果匹配到对应的server上;
                        Virtual v=new Virtual(unallocated_single.get(i),0);//重新写了个Virtual的构造
                        virtualMap.put(v.getId(),v);
                        if(!server.setVirtual(v,2)){
                            System.err.println("xxXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                        };// 安排在了b节点
                        //moveToMigrationMap(server);
                        sumVirtual++;
                    }
                    int count=0;
                    for(Integer i:numbers){//删除未分配的点
                        unallocated_single.remove(i-count);
                        count++;
                    }
                }else{
                    flag+=1;
                    if(unfullServerList_double.contains(server)) {
                        unfullServerList_double.remove(server);
                    }
                    if(flag==2){
                        fullServers.add(server);
                    }
                }
            }
            unfullServer_temp = new HashSet<>();//跑完一遍就清空
            for(Server server:fullServers){
                unfullServerList_single.remove(server);
                unfullServerList.remove(server);
            }
            if(unallocated_single.size()==0){
                break;
            }
            //不够买新
            int maxCpu=max(unallocated_single,1);
            int maxMem=max(unallocated_single,2);
            List<Integer> list=new ArrayList<>();
            for(int i=0;i<N;i++){
                list=hmForN_serverType.get(purRecommend.get(i));
                if(list.get(0)/2>maxCpu && list.get(1)/2>maxMem){
                    int num=todayPurScheme.getOrDefault(purRecommend.get(i), 0)+1;
                    todayPurScheme.put(purRecommend.get(i),num);
                    Server newServer = new Server(purRecommend.get(i),list.get(0),list.get(1),1);
                    serverMap.put(newServer.getId(),newServer);
                    serverList.add(newServer);
                    unfullServerList.add(newServer);
                    unfullServerList_double.add(newServer);
                    unfullServerList_single.add(newServer);
                    unfullServer_temp.add(newServer);

                    break;
                }
            }
        }

        /*内部id 和 外部id的映射 */
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
    }

    /**
     * 迁移执行
     * */
    // -TODO 更新unfullServerList
    public static void migrationExecute(){
        int maxSuccessMigrationCount=virtualMap.size()/1000*5;
        int successMigrationCount=0;
        int left=0,right=generatedKeys.size()-1;
        while (successMigrationCount<maxSuccessMigrationCount && left<right) {
            HashSet<Server> leftServerSet = serverListMigration.get(generatedKeys.get(left));
            HashSet<Server> rightServerSet = serverListMigration.get(generatedKeys.get(right));
            List<Server> leftS=new ArrayList<>(leftServerSet);
            List<Server> rightS=new ArrayList<>(rightServerSet);
            boolean isContinueOuter=false;
            for (int i = 0; i < leftS.size(); i++) {
                Server serverOut = leftS.get(i);
                leftServerSet.remove(serverOut);
                serverMigrationAll.remove(serverOut.getId());

                List<Integer> virtualOutList = serverOut.getVirtualList();
                while (!virtualOutList.isEmpty()){//为该serverOut所有服务器重新分配
                    int virtualId=virtualOutList.get(0);
                    virtualOutList.remove(0);
                    Virtual v=virtualMap.get(virtualId);
                    serverOut.delVirtual(v);
                    moveToMigrationMap(serverOut);

                    boolean migraSuc=false;
                    for (int j = 0; j < rightS.size(); j++) {
                        Server serverIn = rightS.get(j);
                        rightServerSet.remove(serverIn);
                        serverMigrationAll.remove(serverIn.getId());

                        migraSuc=serverIn.setVirtual(v);
                        moveToMigrationMap(serverIn);//此时才重新判断是否进入待迁移map
                        if (migraSuc){
                            //-TODO 创建对应的migration记录用于输出
                            successMigrationCount++;
                            if (successMigrationCount<maxSuccessMigrationCount){
                                isContinueOuter=true;
                            }
                            break;
                        }
                    }
                    if (isContinueOuter){
                        break;
                    }
                    if (!migraSuc){
                        serverOut.setVirtual(v);
                        moveToMigrationMap(serverOut);
                        right--;
                    }

                }
                if (isContinueOuter){
                    break;
                }
            }
            left++;
        }
    }



    public static int max(List<List<Integer>> unallocated, int j){
        int max = 0;
        for(List<Integer> list:unallocated){
            max = max > list.get(j)?max :list.get(j);
        }
        return max;
    }
    public static double[] setsum(){
        double sumC=0,sumM=0;
        Iterator<Map.Entry<Integer, Virtual>> iterator = virtualMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer, Virtual> next = iterator.next();
            Virtual value = next.getValue();
            sumC+=value.getRequiredCPU();
            sumC+=value.getRequiredMem();
        }
        double[] ans=new double[2];
        ans[0]=sumC;ans[1]=sumM;
        return ans;
    }

    public static List<String> setRecommend(int day,int cpu,int mem){
        List<String> purRecommend = new ArrayList();
        TreeMap<Double,String> scores = new TreeMap<>();
        for (Map.Entry<String, List<Integer>> stype :  hmForN_serverType.entrySet()){
            double score = (double)(stype.getValue().get(2)/(T-day) + stype.getValue().get(3))
                    *Math.max(cpu/stype.getValue().get(0)+1,mem/stype.getValue().get(1)+1);
            scores.put(score,stype.getKey());
        }
        for (Map.Entry<Double, String> entry : scores.entrySet()) {
            purRecommend.add(entry.getValue());
        }
        return purRecommend;
    }


    public static List<Integer> ac_single(int[] capacity, List<List<Integer>> unallocated){
        //capcity cpu,mem  || unallocated id,cpu,mem
        List<Integer> answer = new ArrayList();//j,
        int len=unallocated.size()<500?unallocated.size():500;
        double[] pheromone = new double[len];
        //初始化信息素。
        int maxAnt=100;//先试试50个
        double a=1.0,b=1,p=0.05,q=100;//系数
        for(int i=0;i<len;i++){
            pheromone[i]=1.0;
        }
        //pheromone=fill(pheromone,1.0);
        //选择第一个节点
        int ant=0;
        int c_m=capacity[0]+capacity[1];
        double maxUse=0;
        List<List<Integer>> best=new ArrayList();
        List<Double> bestUse=new ArrayList();
        while(ant++<maxAnt && (double)maxUse <(double) c_m*0.98 && answer.size()<len){
            boolean[] mark = new boolean[len];
            //选第一个
            int[] c = capacity.clone();
            List<Integer> next;
            int pre=len;
            double[] possibility;
            double used=0;
            List<Integer> choice=new ArrayList();
            while( true ) {
                next = new ArrayList<>();
                //寻找能去的下一个节点
                for (int i = 0; i < len; i++) {
                    if (!mark[i] && c[0] >= unallocated.get(i).get(1) && c[1] >= unallocated.get(i).get(2)){
                        next.add(i);
                    }
                }
                if (next == null || next.size()==0){ //如果没有能到达的下一个节点，则退出。
                    break;
                }
                // 计算概率
                possibility = new double[next.size()];
                for (int i = 0; i < next.size(); i++) {
                    possibility[i] = Math.pow(pheromone[next.get(i)], a) *
                            Math.pow(unallocated.get(next.get(i)).get(1) + unallocated.get(next.get(i)).get(2),b);
                }
                //轮盘赌
                pre = next.get(roulette(possibility));
                mark[pre]=true;
                choice.add(pre);
                int cpu=unallocated.get(pre).get(1);
                int mem=unallocated.get(pre).get(2);
                c[0]-=cpu;
                c[1]-=mem;
                used+=cpu+mem;
            }
            if(choice.size()==0){
                return answer;
            }
            // 更新信息素
            for(int i=0;i<len;i++){
                pheromone[i]*=(1-p) ;
            }
            for(int i=0;i<choice.size();i++){
                pheromone[choice.get(i)] += used/q;
            }
            if(maxUse<used){
                maxUse=used;
                best.add(choice);
                bestUse.add(maxUse);
                answer=choice;
            }
        }
        return answer;
    }



    public static List<Integer> ac_double(int[] capacity, List<List<Integer>> unallocated){
        //capcity cpu,mem  || unallocated id,cpu,mem
        List<Integer> answer = new ArrayList();//j,
        int len=unallocated.size()<500?unallocated.size():500;
        double[] pheromone = new double[len];
        //初始化信息素。
        int maxAnt=100;//先试试50个
        double a=1.0,b=1,p=0.05,q=100;//系数
        for(int i=0;i<len;i++){
            pheromone[i]=1.0;
        }
        //pheromone=fill(pheromone,1.0);
        //选择第一个节点
        int ant=0;
        int c_m=capacity[0]+capacity[1];
        double maxUse=0;
        List<List<Integer>> best=new ArrayList();
        List<Double> bestUse=new ArrayList();
        while(ant++<maxAnt && (double)maxUse <(double) c_m*0.98 && answer.size()<len){
            boolean[] mark = new boolean[len];
            //选第一个
            int[] c = capacity.clone();
            List<Integer> next;
            int pre=len;
            double[] possibility;
            double used=0;
            List<Integer> choice=new ArrayList();
            while( true ) {
                next = new ArrayList<>();
                //寻找能去的下一个节点
                for (int i = 0; i < len; i++) {
                    if (!mark[i] && c[0] >= unallocated.get(i).get(1)/2 && c[1] >= unallocated.get(i).get(2)/2){
                        next.add(i);
                    }
                }
                if (next == null || next.size()==0){ //如果没有能到达的下一个节点，则退出。
                    break;
                }
                // 计算概率
                possibility = new double[next.size()];
                for (int i = 0; i < next.size(); i++) {
                    possibility[i] = Math.pow(pheromone[next.get(i)], a) *
                            Math.pow(unallocated.get(next.get(i)).get(1)/2 + unallocated.get(next.get(i)).get(2)/2,b);
                }
                //轮盘赌
                pre = next.get(roulette(possibility));
                mark[pre]=true;
                choice.add(pre);
                int cpu=unallocated.get(pre).get(1)/2;
                int mem=unallocated.get(pre).get(2)/2;
                c[0]-=cpu;
                c[1]-=mem;
                used+=cpu+mem;
            }
            if(choice.size()==0){
                return answer;
            }
            // 更新信息素
            for(int i=0;i<len;i++){
                    pheromone[i]*=(1-p) ;
            }
            for(int i=0;i<choice.size();i++){
                pheromone[choice.get(i)] += used/q;
            }
            if(maxUse<used){
                maxUse=used;
                best.add(choice);
                bestUse.add(maxUse);
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
    public static double[][] fill(double[][] p,double a){
        for(int i=0;i<p.length;i++){
            for(int j=0;j<p[0].length;j++){
                p[i][j]=a;
            }
        }
        return p;
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
                        moveToMigrationMap(servers);
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
                            moveToMigrationMap(newServer);
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

