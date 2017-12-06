package com.jaredzhao;

import com.mongodb.BasicDBObject;

import java.util.Iterator;
import java.util.Random;

public class UniqueIDSupplier {

    private static int length = 8;
    private static String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static Random random = new Random();

    public static String getNextID(){
        char[] text;
        Iterator iterator;
        do {
            text = new char[length];
            for (int i = 0; i < length; i++) {
                text[i] = characters.charAt(random.nextInt(characters.length()));
            }
            iterator = MongoDBAccessor.queryDocument(new BasicDBObject("id", new String(text)));
        } while(iterator.hasNext());
        return new String(text);
    }

}
