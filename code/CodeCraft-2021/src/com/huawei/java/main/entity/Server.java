package com.huawei.java.main.entity;

import java.util.ArrayList;
import java.util.List;

public class Server {
    private static int count = 0;
    private int id;//内部id
    private String typeName;
    private int maxCPU;
    private int maxMEM;
    private int aCpuUse;
    private int aMemUse;
    private int bCpuUse;
    private int bMemUse;
    private int priority;
    private List<Integer> virtualList;//id


    public Server(String i_typeName, int i_maxCPU, int i_maxMemory, int i_priority) {
        id = count++;
        typeName = i_typeName;
        maxCPU = i_maxCPU;
        maxMEM = i_maxMemory;
        priority = i_priority;
        virtualList=new ArrayList();
        aCpuUse=0;
        aMemUse =0;
        bCpuUse=0;
        bMemUse =0;
    }

    public Server(Server value) {
        id = value.getId();
        typeName = value.getTypeName();
        maxCPU = value.getMaxCPU();
        maxMEM = value.getMaxMEM();
        priority = value.getPriority();
        virtualList= new ArrayList<>(value.getVirtualList());
        aCpuUse= value.getaCpuUse();
        aMemUse = value.getaMemUse();
        bCpuUse= value.getbCpuUse();
        bMemUse = value.getbMemUse();
    }

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        Server.count = count;
    }

    public int getId() {
        return id;
    }
//
//    public void setId(int id) {
//        this.id = id;
//    }

    public String getTypeName() {
        return typeName;
    }

//    public void setTypeName(String typeName) {
//        this.typeName = typeName;
//    }

    public int getMaxCPU() {
        return maxCPU;
    }

//    public void setMaxCPU(int maxCPU) {
//        this.maxCPU = maxCPU;
//    }

    public int getMaxMEM() {
        return maxMEM;
    }

//    public void setMaxMEM(int maxMEM) {
//        this.maxMEM = maxMEM;
//    }

    public int getaCpuUse() {
        return aCpuUse;
    }

    public void setaCpuUse(int aCpuUse) {
        this.aCpuUse = aCpuUse;
    }

    public int getaMemUse() {
        return aMemUse;
    }

    public void setaMemUse(int aMemUse) {
        this.aMemUse = aMemUse;
    }

    public int getbCpuUse() {
        return bCpuUse;
    }

    public void setbCpuUse(int bCpuUse) {
        this.bCpuUse = bCpuUse;
    }

    public int getbMemUse() {
        return bMemUse;
    }

    public void setbMemUse(int bMemUse) {
        this.bMemUse = bMemUse;
    }

    public int getPriority() {
        return priority;
    }

//    public void setPriority(int priority) {
//        this.priority = priority;
//    }

    public List<Integer> getVirtualList() {
        return virtualList;
    }

    public boolean canBeSet(Virtual v){
        if (virtualList.contains(v.getId())) {
            return false;//该服务器已经部署了这个虚拟机
        }
        int isDouble = v.getIsDouble();
        if (isDouble==1){//双节点
            int requiredCPU = v.getRequiredCPU()/2;
            int requiredMem = v.getRequiredMem()/2;
            if ((maxCPU/2-aCpuUse)<requiredCPU || (maxMEM/2-aMemUse)<requiredMem
                    || (maxCPU/2-bCpuUse)<requiredCPU || (maxMEM/2-bMemUse)<requiredMem){
                return false;
            }
            return true;
        }else {
            int requiredCPU = v.getRequiredCPU();
            int requiredMem = v.getRequiredMem();
            if ((maxCPU/2-aCpuUse)>=requiredCPU && (maxMEM/2-aMemUse)>=requiredMem){
                return true;
            }
            if ((maxCPU/2-bCpuUse)>=requiredCPU && (maxMEM/2-bMemUse)>=requiredMem){
                return true;
            }
            return false;
        }
    }


    public boolean setVirtual(Virtual v){
        if (virtualList.contains(v.getId())) {
            return false;//该服务器已经部署了这个虚拟机
        }
        int isDouble = v.getIsDouble();
        if (isDouble==1){//双节点
            int requiredCPU = v.getRequiredCPU()/2;
            int requiredMem = v.getRequiredMem()/2;
            if ((maxCPU/2-aCpuUse)<requiredCPU || (maxMEM/2-aMemUse)<requiredMem
                    || (maxCPU/2-bCpuUse)<requiredCPU || (maxMEM/2-bMemUse)<requiredMem){
                return false;
            }
            aCpuUse+=requiredCPU;
            aMemUse+=requiredMem;
            bCpuUse+=requiredCPU;
            bMemUse+=requiredMem;
            v.setDeployedServerId(id);
            v.setDeployedServerNode(3);
            this.virtualList.add(v.getId());
            return true;
        }else {
            int requiredCPU = v.getRequiredCPU();
            int requiredMem = v.getRequiredMem();
            if ((maxCPU/2-aCpuUse)>=requiredCPU && (maxMEM/2-aMemUse)>=requiredMem){
                aCpuUse+=requiredCPU;
                aMemUse+=requiredMem;
                this.virtualList.add(v.getId());
                v.setDeployedServerId(id);
                v.setDeployedServerNode(1);
                return true;
            }
            if ((maxCPU/2-bCpuUse)>=requiredCPU && (maxMEM/2-bMemUse)>=requiredMem){
                bCpuUse+=requiredCPU;
                bMemUse+=requiredMem;
                this.virtualList.add(v.getId());
                v.setDeployedServerId(id);
                v.setDeployedServerNode(2);
                return true;
            }
            return false;
        }
    }
    /**
     * 仅支持单节点虚拟机
     * 指定部署a节点传入1；b节点传入2.
     * */
    public boolean setVirtual(Virtual v,int a1b2){
        if (virtualList.contains(v.getId())) {
            return false;//该服务器已经部署了这个虚拟机
        }
        int isDouble = v.getIsDouble();
        if (isDouble==1){//双节点

            return false;
        }else {
            if (!(a1b2==1 || a1b2==2)){
                return false;
            }
            int requiredCPU = v.getRequiredCPU();
            int requiredMem = v.getRequiredMem();
            if (a1b2==1){//a
                if ((maxCPU/2-aCpuUse)>=requiredCPU && (maxMEM/2-aMemUse)>=requiredMem){
                    aCpuUse+=requiredCPU;
                    aMemUse+=requiredMem;
                    this.virtualList.add(v.getId());
                    v.setDeployedServerId(id);
                    v.setDeployedServerNode(1);
                    return true;
                }
            }
            if (a1b2==2){//b
                if ((maxCPU/2-bCpuUse)>=requiredCPU && (maxMEM/2-bMemUse)>=requiredMem){
                    bCpuUse+=requiredCPU;
                    bMemUse+=requiredMem;
                    this.virtualList.add(v.getId());
                    v.setDeployedServerId(id);
                    v.setDeployedServerNode(2);
                    return true;
                }
            }
            return false;
        }
    }
    //-TODO 需要做迁移版
    public boolean delVirtual(Virtual v){
        if (!virtualList.contains(v.getId())) {
            return false;//该服务器不存在该虚拟机
        }
        int isDouble = v.getIsDouble();
        if (isDouble==1){
            int requiredCPU = v.getRequiredCPU()/2;
            int requiredMem = v.getRequiredMem()/2;
            aCpuUse-=requiredCPU;
            aMemUse-=requiredMem;
            bCpuUse-=requiredCPU;
            bMemUse-=requiredMem;
        }else {
            int requiredCPU = v.getRequiredCPU();
            int requiredMem = v.getRequiredMem();
            int deployedServerNode = v.getDeployedServerNode();
            if (deployedServerNode==1){
                aCpuUse-=requiredCPU;
                aMemUse-=requiredMem;
            }else if (deployedServerNode==2){
                bCpuUse-=requiredCPU;
                bMemUse-=requiredMem;
            }else {
                System.err.println("虚拟机移除出错");
                throw new NullPointerException();
            }
        }
        v.setDeployedServerId(-1);
        v.setDeployedServerNode(0);
        Boolean b= this.virtualList.remove(new Integer(v.getId()));
        return true;
    }

    public boolean setVirtual_test(Virtual v){
        if (virtualList.contains(v.getId())) {
            return false;//该服务器已经部署了这个虚拟机
        }
        int isDouble = v.getIsDouble();
        if (isDouble==1){//双节点
            int requiredCPU = v.getRequiredCPU()/2;
            int requiredMem = v.getRequiredMem()/2;
            if ((maxCPU/2-aCpuUse)<requiredCPU || (maxMEM/2-aMemUse)<requiredMem
                    || (maxCPU/2-bCpuUse)<requiredCPU || (maxMEM/2-bMemUse)<requiredMem){
                return false;
            }
            aCpuUse+=requiredCPU;
            aMemUse+=requiredMem;
            bCpuUse+=requiredCPU;
            bMemUse+=requiredMem;
         //   this.virtualList.add(v.getId());
            return true;
        }else {
            int requiredCPU = v.getRequiredCPU();
            int requiredMem = v.getRequiredMem();
            if ((maxCPU/2-aCpuUse)>=requiredCPU && (maxMEM/2-aMemUse)>=requiredMem){
                aCpuUse+=requiredCPU;
                aMemUse+=requiredMem;
            //    this.virtualList.add(v.getId());
                return true;
            }
            if ((maxCPU/2-bCpuUse)>=requiredCPU && (maxMEM/2-bMemUse)>=requiredMem){
                bCpuUse+=requiredCPU;
                bMemUse+=requiredMem;
          //      this.virtualList.add(v.getId());
                return true;
            }
            return false;
        }
    }
    /**
     * 仅支持单节点虚拟机
     * 指定部署a节点传入1；b节点传入2.
     * */
    public boolean setVirtual_test(Virtual v,int a1b2){
        if (virtualList.contains(v.getId())) {
           return false;//该服务器已经部署了这个虚拟机
        }
        int isDouble = v.getIsDouble();
        if (isDouble==1){//双节点

            return false;
        }else {
            if (!(a1b2==1 || a1b2==2)){
                return false;
            }
            int requiredCPU = v.getRequiredCPU();
            int requiredMem = v.getRequiredMem();
            if (a1b2==1){//a
                if ((maxCPU/2-aCpuUse)>=requiredCPU && (maxMEM/2-aMemUse)>=requiredMem){
                    aCpuUse+=requiredCPU;
                    aMemUse+=requiredMem;
                //    this.virtualList.add(v.getId());
                    return true;
                }
            }
            if (a1b2==2){//b
                if ((maxCPU/2-bCpuUse)>=requiredCPU && (maxMEM/2-bMemUse)>=requiredMem){
                    bCpuUse+=requiredCPU;
                    bMemUse+=requiredMem;
             //       this.virtualList.add(v.getId());
                    return true;
                }
            }
            return false;
        }
    }
    //-TODO 需要做迁移版
    public boolean delVirtual_test(Virtual v){
        if (!virtualList.contains(v.getId())) {
            return false;//该服务器不存在该虚拟机
        }
        int isDouble = v.getIsDouble();
        if (isDouble==1){
            int requiredCPU = v.getRequiredCPU()/2;
            int requiredMem = v.getRequiredMem()/2;
            aCpuUse-=requiredCPU;
            aMemUse-=requiredMem;
            bCpuUse-=requiredCPU;
            bMemUse-=requiredMem;
        }else {
            int requiredCPU = v.getRequiredCPU();
            int requiredMem = v.getRequiredMem();
            int deployedServerNode = v.getDeployedServerNode();
            if (deployedServerNode==1){
                aCpuUse-=requiredCPU;
                aMemUse-=requiredMem;
            }else if (deployedServerNode==2){
                bCpuUse-=requiredCPU;
                bMemUse-=requiredMem;
            }else {
                System.err.println("虚拟机移除出错");
                throw new NullPointerException();
            }
        }
        Boolean b= this.virtualList.remove(new Integer(v.getId()));
        return true;
    }

    public int getRemainedCpuA(){
        return maxCPU/2-getaCpuUse();
    }
    public int getRemainedCpuB(){
        return maxCPU/2-getbCpuUse();
    }
    public int getRemainedMemA(){
        return maxMEM/2-getaMemUse();
    }
    public int getRemainedMemB(){
        return maxMEM/2-getbMemUse();
    }

    public double getRemainedCpuAPercentage(){
        return (double)getRemainedCpuA()/(double)maxCPU/2;
    }
    public double getRemainedCpuBPercentage(){
        return (double)getRemainedCpuB()/(double)maxCPU/2;
    }
    public double getRemainedMemAPercentage(){
        return (double)getRemainedMemA()/(double)maxMEM/2;
    }
    public double getRemainedMemBPercentage(){
        return (double)getRemainedMemB()/(double)maxMEM/2;
    }
    public double MaxRemainedPercentage(){
        double max1=Math.max(getRemainedCpuAPercentage(),getRemainedCpuBPercentage());
        max1= Math.max(max1,getRemainedMemAPercentage());
        return Math.max(max1,getRemainedMemBPercentage());
    }
    public double MinRemainedPercentage(){
        double min1=Math.min(getRemainedCpuAPercentage(),getRemainedCpuBPercentage());
        min1= Math.min(min1,getRemainedMemAPercentage());
        return Math.min(min1,getRemainedMemBPercentage());
    }

    public double getUsedCpuAPercentage(){
        return (double)getaCpuUse()/(double)maxCPU/2;
    }
    public double getUsedCpuBPercentage(){
        return (double)getbCpuUse()/(double)maxCPU/2;
    }
    public double getUsedMemAPercentage(){
        return (double)getaMemUse()/(double)maxMEM/2;
    }
    public double getUsedMemBPercentage(){
        return (double)getbMemUse()/(double)maxMEM/2;
    }

    public double MaxUsedPercentage(){
        double max1=Math.max(getUsedCpuAPercentage(),getUsedCpuBPercentage());
        max1= Math.max(max1,getUsedMemAPercentage());
        return Math.max(max1,getUsedMemBPercentage());
    }
    public double MinUsedPercentage(){
        double min1=Math.min(getUsedCpuAPercentage(),getUsedCpuBPercentage());
        min1= Math.min(min1,getUsedMemAPercentage());
        return Math.min(min1,getUsedMemBPercentage());
    }

//    public void setVirtualList(List<Integer> virtualList) {
//        this.virtualList = virtualList;
//    }
}
