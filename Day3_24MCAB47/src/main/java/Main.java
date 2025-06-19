import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Scanner;

public class Main {

    // Initialize the scanner object
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // MongoDB collections
        MongoCollection<Document> studentsCollection = MongoDBUtils.getCollection("students");
        MongoCollection<Document> coursesCollection = MongoDBUtils.getCollection("courses");
        MongoCollection<Document> enrollmentsCollection = MongoDBUtils.getCollection("enrollments");

        // User Input for student
        System.out.println("Enter student details:");
        System.out.print("Name: ");
        String studentName = scanner.nextLine();
        System.out.print("Age: ");
        int studentAge = Integer.parseInt(scanner.nextLine());
        System.out.print("Email: ");
        String studentEmail = scanner.nextLine();

        // Check if the student already exists in the database by email
        Document existingStudentDoc = studentsCollection.find(new Document("email", studentEmail)).first();

        Student student;
        if (existingStudentDoc != null) {
            // Student already exists, use the existing student data
            ObjectId studentId = existingStudentDoc.getObjectId("_id");
            String existingStudentName = existingStudentDoc.getString("name");
            int existingStudentAge = existingStudentDoc.getInteger("age");
            student = new Student(studentId, existingStudentName, existingStudentAge, studentEmail);
            System.out.println("Student already exists: " + student.getName());
        } else {
            // New student, create a new student record
            student = new Student(new ObjectId(), studentName, studentAge, studentEmail);
            studentsCollection.insertOne(new Document("_id", student.getId())
                    .append("name", student.getName())
                    .append("age", student.getAge())
                    .append("email", student.getEmail()));
            System.out.println("Student added: " + student.getName());
        }

        // User Input for course
        System.out.println("\nEnter course details:");
        System.out.print("Course Title: ");
        String courseTitle = scanner.nextLine();
        System.out.print("Course Description: ");
        String courseDescription = scanner.nextLine();

        // Check if the course already exists
        Document existingCourseDoc = coursesCollection.find(new Document("title", courseTitle)).first();
        Course course;
        if (existingCourseDoc != null) {
            // Course already exists, use the existing course data
            ObjectId courseId = existingCourseDoc.getObjectId("_id");
            String existingCourseTitle = existingCourseDoc.getString("title");
            String existingCourseDescription = existingCourseDoc.getString("description");
            course = new Course(courseId, existingCourseTitle, existingCourseDescription);
            System.out.println("Course already exists: " + course.getTitle());
        } else {
            // New course, create a new course record
            course = new Course(new ObjectId(), courseTitle, courseDescription);
            coursesCollection.insertOne(new Document("_id", course.getId())
                    .append("title", course.getTitle())
                    .append("description", course.getDescription()));
            System.out.println("Course added: " + course.getTitle());
        }

        // Check for existing enrollment before inserting new enrollments
        Document existingEnrollment = enrollmentsCollection.find(new Document("studentId", student.getId())
                .append("courseId", course.getId())).first();

        if (existingEnrollment != null) {
            // Enrollment already exists for this student and course, so do not insert a new enrollment
            System.out.println("This student is already enrolled in the course: " + course.getTitle());
        } else {
            // Create and insert Embedded Enrollment
            Document embeddedEnrollment = new Document("_id", new ObjectId())
                    .append("student", new Document("_id", student.getId())
                            .append("name", student.getName())
                            .append("age", student.getAge()))
                    .append("course", new Document("_id", course.getId())
                            .append("title", course.getTitle())
                            .append("description", course.getDescription()))
                    .append("isEmbedded", true);

            // Create and insert Referenced Enrollment
            Document referencedEnrollment = new Document("_id", new ObjectId())
                    .append("studentId", student.getId())
                    .append("courseId", course.getId())
                    .append("isEmbedded", false);

            // Insert enrollments
            enrollmentsCollection.insertOne(embeddedEnrollment);
            enrollmentsCollection.insertOne(referencedEnrollment);
            System.out.println("Enrollments added.");
        }

        // Query and print both types of enrollments
        System.out.println("\nAll enrollments:");
        MongoCursor<Document> cursor = enrollmentsCollection.find().iterator();
        while (cursor.hasNext()) {
            Document enrollment = cursor.next();
            if ((Boolean) enrollment.get("isEmbedded")) {
                System.out.println("Embedded Enrollment: " + enrollment.toJson());
            } else {
                System.out.println("Referenced Enrollment: " + enrollment.toJson());
            }
        }

        // User Input for updating student details (e.g., name)
        System.out.println("\nDo you want to update a student's name? (yes/no)");
        String updateResponse = scanner.nextLine();
        if (updateResponse.equalsIgnoreCase("yes")) {
            System.out.print("Enter student's email to update: ");
            String studentEmailToUpdate = scanner.nextLine();

            System.out.print("Enter new student name: ");
            String newName = scanner.nextLine();

            // Update the student's name in MongoDB
            studentsCollection.updateOne(new Document("email", studentEmailToUpdate),
                    new Document("$set", new Document("name", newName)));
            System.out.println("Student's name updated to: " + newName);

            // Query and print updated enrollments
            System.out.println("\nUpdated enrollments after name change:");
            cursor = enrollmentsCollection.find().iterator();
            while (cursor.hasNext()) {
                Document enrollment = cursor.next();
                if ((Boolean) enrollment.get("isEmbedded")) {
                    System.out.println("Embedded Enrollment: " + enrollment.toJson());
                } else {
                    System.out.println("Referenced Enrollment: " + enrollment.toJson());
                }
            }
        }

        // Create an index on the student's name for better querying
        studentsCollection.createIndex(new Document("name", 1));
    }
}
