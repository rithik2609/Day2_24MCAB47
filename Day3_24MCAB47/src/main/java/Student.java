import org.bson.types.ObjectId;

public class Student {
    private ObjectId id;
    private String name;
    private int age;
    private String email;

    // Constructor with email validation
    public Student(ObjectId id, String name, int age, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty or null.");
        }
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be greater than 0.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty or null.");
        }

        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    // Getters and Setters with validation

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty or null.");
        }
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be greater than 0.");
        }
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty or null.");
        }
        this.email = email;
    }
}
