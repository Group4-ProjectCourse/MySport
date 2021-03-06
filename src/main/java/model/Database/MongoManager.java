package model.Database;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import eu.dozd.mongo.MongoMapper;
import eu.dozd.mongo.annotation.Entity;
import eu.dozd.mongo.annotation.Id;
import model.Tools.Tags.Legacy;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoManager {

    static {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
    }


    public void addActivity(LocalDate date, Activity activity){
        try(MongoClient client = getClient()) {
            MongoCollection<Day> collection = getCollection(client, date);
            if (collection.find(Filters.eq("_id", date.getDayOfMonth())).first() == null)
                insertDay(collection, date.getDayOfMonth(), date.getDayOfYear());
            collection.updateOne(Filters.eq("_id", date.getDayOfMonth()),
                    Updates.addToSet("activities", activity
            ));
        }
    }

    public UpdateResult addParticipant(LocalDate date, String sport, int id, boolean isLeader){
        String table = isLeader ? "leaders" : "members";
        try(MongoClient client = getClient()) {
            return getCollection(client, date).updateOne(
                    BasicDBObject.parse("{ _id: "+date.getDayOfMonth()+", " +
                            "\"activities._id\": \"" + sport + "\" }"),
                    BasicDBObject.parse("{ $push: {\"activities.$." + table + "\": " + id + "}}")
            );
        }
    }

    public UpdateResult changeTime(LocalDate date, String sport, int startTime, int newStart, int newEnd){
        UpdateResult result;
        try(MongoClient client = getClient()) {
            result = getCollection(client, date).updateOne(
                    BasicDBObject.parse("{ _id: " + date.getDayOfMonth() + ", " +
                            "\"activities._id\": \"" + sport + "\", " +
                            "\"activities.start\": " + startTime + " }"),
                    BasicDBObject.parse("{ $set: { \"activities.$.start\": " + newStart + ", \"activities.$.end\": " + newEnd + " } }")
            );
        }

        return result;
    }

    public UpdateResult removeParticipant(LocalDate date, String sport, int id, boolean isLeader){
        try(MongoClient client = getClient()) {
            return getCollection(client, date).updateOne(
                    BasicDBObject.parse("{ _id: " + date.getDayOfMonth() + ", " +
                            "\"activities._id\": \"" + sport + "\" }"),
                    BasicDBObject.parse("{ $pull: { \"activities.$." + (isLeader ? "leaders" : "members") + "\": " + id +" } }")
            );
        }
    }

    public UpdateResult removeActivity(LocalDate date, String sport, int startTime){
        try(MongoClient client = getClient()) {
            return getCollection(client, date).updateOne(
                    BasicDBObject.parse("{ _id: " + date.getDayOfMonth() + " }"),
                    BasicDBObject.parse("{ $pull: { \"activities.$._id\": \"" + sport + "\", " +
                            "\"activities.$.start\": \"" + startTime + "\" } }")
            );
        }
    }

    public long removeDay(LocalDate date){
        try(MongoClient client = getClient()) {
            return getCollection(client, date).deleteOne(
                    BasicDBObject.parse("{ _id: " + date.getDayOfMonth() + " }")
            ).getDeletedCount();
        }
    }


    private void insertDay(MongoCollection<Day> collection, int dayOfMonth, int dayOfYear){
        collection.insertOne(new Day(dayOfMonth, dayOfYear, new ArrayList<>(5)));
    }


    @Legacy(reason = "Codec needed for converting Document to Day")
    public int getParticipantsCount(LocalDate date, String sport){
        int sum = 0;
        try(MongoClient client = getClient()){
            MongoCollection<Day> collection = getCollection(client, date);
            Day day = collection.find(
                    BasicDBObject.parse("{ _id: "+ date.getDayOfMonth()+", " +
                            "\"activities._id\": \"" + sport + "\" }")
            ).first();
            for(Object o :  day.getActivities()){
                Document activity = (Document) o;
                if(activity.getString("_id").equals(sport)){
                    sum = activity.getList("members", Integer.class).size() + activity.getList("leaders", Integer.class).size();
                    break;
                }
            }
        }

        return sum;
    }

    public Day getDay(){
        return getDay(LocalDate.now());
    }

    public Day getDay(LocalDate date){
        ArrayList<Activity> activities = new ArrayList<>(3);
        try(MongoClient client = getClient()){
            MongoCollection<Day> collection = getCollection(client, date);
            FindIterable<Day> day = collection.find(
                    BasicDBObject.parse("{ _id: "+ date.getDayOfMonth()+" }")
            );
            if(day.first() == null)
                return null;
            for(Object a : day.first().getActivities()){
                Document document = (Document) a;
                activities.add(new Activity(
                        document.getString("_id"),
                        document.getString("location"),
                        document.getInteger("rating"),
                        document.getInteger("start"),
                        document.getInteger("end"),
                        new ArrayList<>(document.getList("leaders", Integer.class)),
                        new ArrayList<>(document.getList("members", Integer.class))
                ));
            }
        }
        return new Day(date.getDayOfMonth(), date.getDayOfYear(), activities);
    }

    private MongoCollection<Day> getCollection(MongoClient client, LocalDate date){
        return getDatabase(client, String.valueOf(date.getYear())).getCollection(date.getMonth().name(), Day.class);
    }

    private MongoDatabase getDatabase(MongoClient client, String year){
        return client.getDatabase(year);
    }

    private static MongoClient getClient(){
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(MongoMapper.getProviders()),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(new ConnectionString("mongodb+srv://admin:azino777@mysport-4hkzu.mongodb.net/test?retryWrites=true&w=majority"))
                .build();

        return MongoClients.create(settings);
    }

    @Entity
    public static final class Day {
        @Id
        private int day;
        private int dayOfYear;
        private ArrayList<Activity> activities;

        public Day(){

        }

        public Day(int day, int dayOfYear, ArrayList<Activity> activities){
            this.dayOfYear = dayOfYear;
            this.day = day;
            this.activities = activities;
        }

        public void removeActivity(String name){
            for(Activity a : activities){
                if(a.name.equals(name)){
                    activities.remove(a);
                    break;
                }
            }
        }

        public ArrayList<Activity> getActivities() {
            return activities;
        }

        public void setActivities(ArrayList<Activity> activities) {
            this.activities = activities;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        @Override
        public String toString() {
            return "day: " + day + " activities count: " + activities.size();
        }

        public int getDayOfYear() {
            return dayOfYear;
        }

        public void setDayOfYear(int dayOfYear) {
            this.dayOfYear = dayOfYear;
        }
    }

    @Entity
    public static final class Activity {
        @Id
        private String name;
        private String location;
        private int rating;
        private int start; //in minutes (e.g: 15:30 is (15 * 60 + 30) = 930, then to convert back 930 div 60 + 930 mod 60)
        private int end; //in minutes (ee.g: 16:45 is (16 * 60 + 45) = 1005, then to convert back 1005 div 60 + 1005 mod 60)
        private ArrayList<Integer> leaders;
        private ArrayList<Integer> members;

        public Activity(){

        }

        public Activity(String name, String location, int rating, int start, int end, ArrayList<Integer> leaders, ArrayList<Integer> members){
            this.name = name;
            this.location = location;
            this.rating = rating;
            this.start = start;
            this.end = end;
            this.leaders = leaders;
            this.members = members;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public int getStart() {
            return start;
        }

        public int getStartHour(){
            return start / 60;
        }

        public int getStartMin(){
            return start % 60;
        }

        public int getEndHour(){
            return end / 60;
        }

        public int getEndMin(){
            return end % 60;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public ArrayList<Integer> getLeaders() {
            return leaders;
        }

        public void setLeaders(ArrayList<Integer> leaders) {
            this.leaders = leaders;
        }

        public ArrayList<Integer> getMembers() {
            return members;
        }

        public void setMembers(ArrayList<Integer> members) {
            this.members = members;
        }

        @Override
        public String toString() {
            return "Location: " + location + " Rating: " + rating + " Starts: " + start / 60 + ":" + start % 60 +
                    " Ends: " + end / 60 + ":" + end % 60 + " Leaders count: " + leaders.size() + " Members count: " + members.size();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

