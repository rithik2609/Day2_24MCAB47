import com.mongodb.client.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoDBUtils {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    static {
        mongoClient = MongoClients.create("mongodb://localhost:27017"); // or use MongoDB Atlas connection string
        database = mongoClient.getDatabase("studentEnrollmentDB");
    }

    public static MongoCollection<Document> getCollection(String name) {
        return database.getCollection(name);
    }
}
