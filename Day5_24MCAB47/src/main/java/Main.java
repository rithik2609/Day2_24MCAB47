import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        EmployeeService service = new EmployeeService();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\nEmployee Management System:");
            System.out.println("1. Add Employee");
            System.out.println("2. Update Employee");
            System.out.println("3. Delete Employee");
            System.out.println("4. Search Employees");
            System.out.println("5. Display Department Statistics");
            System.out.println("6. Exit");

            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine();  // Consume newline character

            switch (option) {
                case 1: // Add Employee
                    System.out.print("Enter Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter Department: ");
                    String department = scanner.nextLine();
                    System.out.print("Enter Skills (comma separated): ");
                    String skills = scanner.nextLine();
                    List<String> skillsList = Arrays.asList(skills.split(","));

                    // Handling date parsing
                    Date joiningDate = null;
                    while (joiningDate == null) {
                        System.out.print("Enter Joining Date (yyyy-MM-dd): ");
                        String joiningDateString = scanner.nextLine();
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            joiningDate = sdf.parse(joiningDateString);
                        } catch (Exception e) {
                            System.out.println("Invalid date format. Please use yyyy-MM-dd.");
                        }
                    }

                    Employee newEmployee = new Employee(name, email, department, skillsList, joiningDate);
                    boolean isAdded = service.addEmployee(newEmployee);
                    System.out.println("Employee added: " + isAdded);
                    break;

                case 2: // Update Employee
                    System.out.print("Enter Employee Email to Update: ");
                    String updateEmail = scanner.nextLine();

                    System.out.print("Enter field to update (name, department, skills): ");
                    String field = scanner.nextLine();
                    System.out.print("Enter new value for " + field + ": ");
                    String value = scanner.nextLine();
                    Map<String, Object> updatedFields = new HashMap<>();
                    updatedFields.put(field, value);

                    boolean isUpdated = service.updateEmployee(updateEmail, updatedFields);
                    System.out.println("Employee updated: " + isUpdated);
                    break;

                case 3: // Delete Employee
                    System.out.print("Enter Employee Email to Delete: ");
                    String deleteEmail = scanner.nextLine();
                    boolean isDeletedByEmail = service.deleteEmployeeByEmail(deleteEmail);
                    System.out.println("Employee deleted: " + isDeletedByEmail);
                    break;

                case 4: // Search Employees
                    System.out.print("Enter Name to Search: ");
                    String searchName = scanner.nextLine();
                    List<Employee> employees = service.searchEmployees(searchName, null, null);

                    if (employees.isEmpty()) {
                        System.out.println("No employees found.");
                    } else {
                        // Display full details for each employee
                        for (Employee employee : employees) {
                            System.out.println("\nEmployee Details:");
                            System.out.println("Name: " + employee.getName());
                            System.out.println("Email: " + employee.getEmail());
                            System.out.println("Department: " + employee.getDepartment());
                            System.out.println("Skills: " + String.join(", ", employee.getSkills()));
                            System.out.println("Joining Date: " + employee.getJoiningDate());
                        }
                    }
                    break;

                case 5: // Department Statistics
                    service.departmentStatistics();
                    break;

                case 6: // Exit
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
        System.out.println("Goodbye!");
    }
}
