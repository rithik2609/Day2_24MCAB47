import org.bson.types.ObjectId;

public class Course {
    private ObjectId id;
    private String title;
    private String description;

    public Course(ObjectId id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
