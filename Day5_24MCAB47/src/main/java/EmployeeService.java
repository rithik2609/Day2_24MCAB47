import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import com.mongodb.client.MongoCursor;


import java.util.List;
public class EmployeeService {

    private  MongoCollection<Document> employeeCollection;

    public EmployeeService() {
        this.employeeCollection = MongoDBClient.getDatabase().getCollection("employees");
    }
    public boolean updateEmployee(String email, Map<String, Object> updatedFields) {
        Document updateDoc = new Document();
        updatedFields.forEach((key, value) -> updateDoc.append(key, value));

        UpdateResult result = employeeCollection.updateOne(
                Filters.eq("email", email),
                new Document("$set", updateDoc)
        );
        return result.getMatchedCount() > 0;
    }
    // Method to add an employee
    public boolean addEmployee(Employee employee) {
        // Ensure email is unique
        Document existingEmployee = employeeCollection.find(Filters.eq("email", employee.getEmail())).first();
        if (existingEmployee != null) {
            return false;  // Email already exists
        }

        // Insert new employee
        Document newEmployee = new Document("name", employee.getName())
                .append("email", employee.getEmail())
                .append("department", employee.getDepartment())
                .append("skills", employee.getSkills())
                .append("joiningDate", employee.getJoiningDate());
        employeeCollection.insertOne(newEmployee);
        return true;
    }
    public boolean deleteEmployeeByEmail(String email) {
        DeleteResult result = employeeCollection.deleteOne(Filters.eq("email", email));
        return result.getDeletedCount() > 0;
    }

    public boolean deleteEmployeeById(String employeeId) {
        DeleteResult result = employeeCollection.deleteOne(Filters.eq("_id", new ObjectId(employeeId)));
        return result.getDeletedCount() > 0;
    }
    public List<Employee> searchEmployees(String name, String department, String skill) {
        Bson nameFilter = name != null ? Filters.regex("name", name, "i") : Filters.exists("name");
        Bson departmentFilter = department != null ? Filters.eq("department", department) : Filters.exists("department");
        Bson skillFilter = skill != null ? Filters.in("skills", skill) : Filters.exists("skills");

        MongoCursor<Document> cursor = employeeCollection.find(Filters.and(nameFilter, departmentFilter, skillFilter)).iterator();

        List<Employee> employees = new ArrayList<>();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Employee employee = new Employee(
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("department"),
                    (List<String>) doc.get("skills"),
                    doc.getDate("joiningDate")
            );
            employees.add(employee);
        }

        return employees;
    }
    public List<Employee> getEmployeesWithPagination(int page, int pageSize) {
        int skipCount = (page - 1) * pageSize;

        MongoCursor<Document> cursor = employeeCollection.find()
                .skip(skipCount)
                .limit(pageSize)
                .iterator();

        List<Employee> employees = new ArrayList<>();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Employee employee = new Employee(
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("department"),
                    (List<String>) doc.get("skills"),
                    doc.getDate("joiningDate")
            );
            employees.add(employee);
        }

        return employees;
    }
    public void departmentStatistics() {
        AggregateIterable<Document> result = employeeCollection.aggregate(Arrays.asList(
                Aggregates.group("$department", Accumulators.sum("count", 1))
        ));

        for (Document doc : result) {
            System.out.println("Department: " + doc.getString("_id") + " | Count: " + doc.getInteger("count"));
        }
    }

    }


