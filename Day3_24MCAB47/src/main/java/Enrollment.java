import org.bson.types.ObjectId;

public class Enrollment {
    private ObjectId id;
    private ObjectId studentId;  // Reference to Student
    private ObjectId courseId;   // Reference to Course
    private Student student;      // Embedded Student Object
    private Course course;        // Embedded Course Object
    private boolean isEmbedded;   // Flag to distinguish between embedded vs referenced enrollments

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getStudentId() {
        return studentId;
    }

    public void setStudentId(ObjectId studentId) {
        this.studentId = studentId;
    }

    public ObjectId getCourseId() {
        return courseId;
    }

    public void setCourseId(ObjectId courseId) {
        this.courseId = courseId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean embedded) {
        isEmbedded = embedded;
    }
}

