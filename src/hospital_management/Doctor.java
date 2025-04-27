package hospital_management;

public class Doctor {
    private int id;
    private String username;
    private String fullName;
    private String specialization;
    private String contact;
    private String availability;

    // Full constructor
    public Doctor(int id, String username, String fullName, 
                 String specialization, String contact, String availability) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.specialization = specialization;
        this.contact = contact;
        this.availability = availability;
    }

    // Simplified constructor
    public Doctor(int id, String fullName, String specialization, 
                 String contact, String availability) {
        this(id, "", fullName, specialization, contact, availability);
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getContact() { return contact; }
    public String getAvailability() { return availability; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setContact(String contact) { this.contact = contact; }
    public void setAvailability(String availability) { this.availability = availability; }

    @Override
    public String toString() {
        return "Doctor [" +
                "ID: " + id +
                ", Username: '" + username + '\'' +
                ", Name: '" + fullName + '\'' +
                ", Specialty: '" + specialization + '\'' +
                ", Contact: '" + contact + '\'' +
                ", Status: '" + availability + '\'' +
                ']';
        
    }
    
    
    

}