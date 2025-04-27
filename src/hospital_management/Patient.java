package hospital_management;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;

public class Patient {
    private int id;
    private String username;
    private String fullName;
    private Date dob;
    private String gender;
    private String contact;
    private String email;
    private Date registrationDate;

    // Default constructor (protected for framework use)
    protected Patient(int aInt, String string, Date date, String string1, String string2, String string3, Date date1) {}

    // Main constructor
    public Patient(int id, String username, String fullName, Date dob, String gender, 
                  String contact, String email, Date registrationDate) {
        this.id = id;
        this.username = username != null ? username : "";
        this.fullName = fullName != null ? fullName : "";
        this.dob = dob;
        this.gender = gender != null ? gender : "Not specified";
        this.contact = contact != null ? contact : "";
        this.email = email != null ? email : "";
        this.registrationDate = registrationDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { 
        return fullName.isEmpty() ? "Unknown" : fullName; 
    }
    public Date getDob() { return dob; }
    public String getGender() { 
        return gender.isEmpty() ? "N/A" : gender; 
    }
    public String getContact() { 
        return contact.isEmpty() ? "N/A" : contact; 
    }
    public String getEmail() { 
        return email.isEmpty() ? "N/A" : email; 
    }
    public Date getRegistrationDate() { return registrationDate; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setDob(Date dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setContact(String contact) { this.contact = contact; }
    public void setEmail(String email) { this.email = email; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    // Calculated properties
    public int getAge() {
        if (dob == null) return 0;
        LocalDate birthDate = dob.toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }

    // Formatting methods
     public String getFormattedDob() {
        return dob != null ? 
            new SimpleDateFormat("MMM dd, yyyy").format(dob) : "N/A";
    }

    public String getFormattedRegistrationDate() {
        return registrationDate != null ? 
            new SimpleDateFormat("MMM dd, yyyy").format(registrationDate) : "N/A";
    }

    @Override
    public String toString() {
        return String.format(
            "Patient [ID: %d, Name: '%s', Age: %d, Gender: '%s', Contact: '%s', Email: '%s']",
            id, getFullName(), getAge(), getGender(), getContact(), getEmail()
        );
    }
}