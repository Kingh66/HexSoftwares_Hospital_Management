package hospital_management;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Appointment {
    private int appointmentId;
    private Timestamp appointmentDate;
    private String patientName;
    private String doctorName;
    private String specialization;
    private String status;
    private String notes;

     public Appointment(int appointmentId, Timestamp appointmentDate, 
                      String patientName, String doctorName, 
                      String specialization, String status, String notes) {
        this.appointmentId = appointmentId;
        this.appointmentDate = appointmentDate;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.status = status; 
        this.notes = notes;// Now properly set from parameter
    }
     
     public String getNotes() { return notes; }

    // Getters
    public int getAppointmentId() { return appointmentId; }
    public Timestamp getAppointmentDate() { return appointmentDate; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialization() { return specialization; }
    public String getStatus() { return status; }
    
    // Formatted date for display
    public String getFormattedTime() {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm").format(appointmentDate);
    }
    
    public String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(appointmentDate);
    }
    
    public String getFormattedDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appointmentDate);
    }

    @Override
    public String toString() {
        return "Appointment ID: " + appointmentId + 
             "\nPatient: " + patientName +
             "\nDoctor: " + doctorName +
             "\nSpecialization: " + specialization + 
             "\nTime: " + getFormattedTime() +
             "\nStatus: " + status;
    }
}