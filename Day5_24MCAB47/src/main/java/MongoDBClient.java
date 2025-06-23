import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBClient {

    private static MongoDatabase database;

    // Singleton pattern for MongoDB connection
    public static MongoDatabase getDatabase() {
        if (database == null) {
            MongoClient client = MongoClients.create("mongodb://localhost:27017"); // Replace with your MongoDB URI
            database = client.getDatabase("employee_management"); // Your database name
        }
        return database;
    }
}
