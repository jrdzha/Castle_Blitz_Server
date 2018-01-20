package com.jaredzhao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {

    public Socket socket;
    public BlockingQueue<String> inputQueue, outputQueue;
    public int lastPing = (int)(System.currentTimeMillis() / 1000);
    public String uniqueID;

    public Client(Socket socket){
        uniqueID = "--------";
        this.socket = socket;
        inputQueue = new LinkedBlockingQueue<>();
        outputQueue = new LinkedBlockingQueue<>();
    }

    public String getAddressAndPort(){
        return String.valueOf(socket.getRemoteSocketAddress());
    }

    public boolean loggedIn(){
        return !uniqueID.equals("--------");
    }

    public void update(){
        if(inputQueue.size() != 0){
            String[] input = inputQueue.poll().split("\\.");
            if(input[0].equals("LOGIN")){
                Iterator iterator = MongoDBAccessor.queryDocument(new BasicDBObject("username", input[1]));
                if(iterator.hasNext()){
                    Document document = (Document)iterator.next();
                    if(input[2].equals(document.getString("password"))){
                        uniqueID = document.getString("id");
                        MongoDBAccessor.collection.updateOne(Filters.eq("id", uniqueID), Updates.push("login-history", new BasicDBObject("time-stamp", new Date().toString())));
                        outputQueue.add("LOGIN.OK");
                    } else {
                        outputQueue.add("LOGIN.FAIL");
                    }
                } else {
                    outputQueue.add("LOGIN.FAIL");
                }
            } else if(input[0].equals("REGISTER")){
                Iterator iterator = MongoDBAccessor.queryDocument(new BasicDBObject("username", input[1]));
                if(!iterator.hasNext()) {
                    uniqueID = UniqueIDSupplier.getNextID();
                    List<BasicDBObject> loginHistory = new ArrayList<>();
                    loginHistory.add(new BasicDBObject("time-stamp", new Date().toString()));
                    List<BasicDBObject> unlockedCharacters = new ArrayList<>();
                    unlockedCharacters.add(new BasicDBObject("character", "Knight"));
                    unlockedCharacters.add(new BasicDBObject("character", "Dwarf"));
                    MongoDBAccessor.insertDocument(
                            new Document("id", uniqueID)
                                    .append("account-created-on", new Date().toString())
                                    .append("username", input[1])
                                    .append("password", input[2])
                                    .append("login-history", loginHistory)
                                    .append("rank", 1)
                                    .append("level", 0)
                                    .append("xp", 0)
                                    .append("gold", 0)
                                    .append("shards", 0)
                                    .append("unlocked-characters", unlockedCharacters));
                    outputQueue.add("REGISTER.OK");
                } else {
                    outputQueue.add("USERNAME.EXISTS");
                }
            } else if(input[0].equals("REQUEST") && loggedIn()){
                Iterator iterator = MongoDBAccessor.queryDocument(new BasicDBObject("id", uniqueID));
                if(iterator.hasNext()) {
                    Document document = (Document) iterator.next();
                    if(input[1].equals("STATS")) {
                        String unlockedCharacters = "";
                        List<Document> unlockedCharactersList = (List<Document>) document.get("unlocked-characters");
                        for (Document character : unlockedCharactersList) {
                            unlockedCharacters = unlockedCharacters + "." + character.getString("character");
                        }
                        outputQueue.add("ID." + uniqueID);
                        outputQueue.add("RANK." + document.getInteger("rank"));
                        outputQueue.add("LEVEL." + document.getInteger("level"));
                        outputQueue.add("XP." + document.getInteger("xp"));
                        outputQueue.add("GOLD." + document.getInteger("gold"));
                        outputQueue.add("SHARDS." + document.getInteger("shards"));
                        outputQueue.add("UNLOCKED-CHARACTERS" + unlockedCharacters); //unlockedCharacters already has a leading "."
                    }
                }
            }
        }
    }
}