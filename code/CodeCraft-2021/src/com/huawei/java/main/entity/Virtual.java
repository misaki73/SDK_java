//package entity;
package com.huawei.java.main.entity;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Virtual {
    private int id;
    private String typeName;
    private int requiredCPU;
    private int requiredMem;
    private int isDouble;
    private boolean isDel;
    private int deployedServerId;//-1默认，服务器内部id
    private int deployedServerNode;//0默认，1-a,2-b,3-ab(双节点)

    public Virtual(int id, String typeName, int requiredCPU, int requiredMem, int isDouble,
                   int deployedServerId,int deployedServerNode) {
        this.id = id;
        this.typeName = typeName;
        this.requiredCPU = requiredCPU;
        this.requiredMem = requiredMem;
        this.isDouble = isDouble;
        this.deployedServerId = deployedServerId;
        this.isDel=false;
        if (!(deployedServerNode==1 || deployedServerNode==2 || deployedServerNode==3)){
            System.err.println("虚拟机新建出错");
            throw new NullPointerException();
        }
        if (isDouble==1){
            this.deployedServerNode=0;
        }else {
            if (deployedServerNode==3){
                System.err.println("虚拟机新建出错");
                throw new NullPointerException();
            }
            this.deployedServerNode=deployedServerNode;
        }
    }
    public Virtual(int id, String typeName, List<Integer> list){
        this.id = id;
        this.typeName = typeName;
        this.requiredCPU = list.get(0);
        this.requiredMem = list.get(1);
        this.isDouble = list.get(2);
        deployedServerId=-1;
        this.isDel=false;
        if (isDouble==1){
            this.deployedServerNode=3;
        }else {
            this.deployedServerNode=0;//待指派给a或b节点
        }
    }
    public Virtual(List<Integer> list,int i){
        this.id = list.get(0);
        this.requiredCPU = list.get(1);
        this.requiredMem = list.get(2);
        this.isDouble = i;
        deployedServerId=-1;
        this.isDel=false;
        if (isDouble==1){
            this.deployedServerNode=3;
        }else {
            this.deployedServerNode=0;//待指派给a或b节点
        }
    }

    public Virtual(int id, String typeName, int requiredCPU, int requiredMem, int isDouble) {
        this.id = id;
        this.typeName = typeName;

        this.requiredCPU = requiredCPU;
        this.requiredMem = requiredMem;
        this.isDouble = isDouble;
        deployedServerId=-1;
        this.isDel=false;
        if (isDouble==1){
            this.deployedServerNode=3;
        }else {
            this.deployedServerNode=0;//待指派给a或b节点
        }
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

    public int getRequiredCPU() {
        return requiredCPU;
    }

//    public void setRequiredCPU(int requiredCPU) {
//        this.requiredCPU = requiredCPU;
//    }

    public int getRequiredMem() {
        return requiredMem;
    }

//    public void setRequiredMem(int requiredMem) {
//        this.requiredMem = requiredMem;
//    }

    public int getIsDouble() {
        return isDouble;
    }

//    public void setIsDouble(int isDouble) {
//        this.isDouble = isDouble;
//    }

    public int getDeployedServerId() {
        return deployedServerId;
    }

    public boolean getIsDel() {
        return isDel;
    }

    public void setIsDel(boolean isDel) {
        this.isDel = isDel;
    }

    //-TODO
    public void setDeployedServerId(int deployedServerId) {
        this.deployedServerId = deployedServerId;
    }

    public int getDeployedServerNode() {
        return deployedServerNode;
    }

    public void setDeployedServerNode(int deployedServerNode) {
        this.deployedServerNode = deployedServerNode;
    }
}
