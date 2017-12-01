package com.jaredzhao;

public class UniqueIDSupplier {

    private static long currentID = -1;

    public static long getNextID(){
        currentID++;
        return currentID;
    }

}
