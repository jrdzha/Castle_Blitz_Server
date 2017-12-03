package com.jaredzhao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Iterator;

public class MongoDBAccessor {
    public static MongoClient mongoClient = new MongoClient("mongo.jaredzhao.com", 27017);
    //MongoCredential mongoCredential = MongoCredential.createCredential()
    public static MongoDatabase mongoDatabase = mongoClient.getDatabase("castleblitz");
    public static MongoCollection<Document> collection = mongoDatabase.getCollection("players");

    public static void insertDocument(Document document){
        collection.insertOne(document);
    }

    public static Iterator queryDocument(BasicDBObject query){
        return collection.find(query).iterator();
    }
}
