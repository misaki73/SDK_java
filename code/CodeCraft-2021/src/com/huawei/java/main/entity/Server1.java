package com.huawei.java.main.entity;

import java.util.ArrayList;

public class Server1 {
    private static int count = 0;
    private int id;//内部id
    private String typeName;
    private int cpu;
    private int mem;

    public Server1(String i_typeName, int i_maxCPU, int i_maxMemory) {
        id = count++;
        typeName = i_typeName;
        cpu = i_maxCPU;
        mem = i_maxMemory;
    }


}
