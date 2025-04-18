
public class Customer {
    protected String fName;
    protected String lName;
    protected String guestID;
    protected double amountSpent;

    // Default constructor
    public Customer() {
        this.fName = "";
        this.lName = "";
        this.guestID = "";
        this.amountSpent = 0.0;
    }

    public Customer(String fName, String lName, String guestID, double amountSpent) {
        this.fName = fName;  
        this.lName = lName;  
        this.guestID = guestID;
        this.amountSpent = amountSpent;
    }

    // Accessors
    public String getFname() { return fName; }
    public String getLname() { return lName; }
    public String getGuestID() { return guestID; }
    public double getASpent() { return amountSpent; }

    // Mutators
    public void setFname(String fName) { this.fName = fName; }
    public void setLname(String lName) { this.lName = lName; }
    public void setGuestID(String guestID) { this.guestID = guestID; }
    public void setASpent(double amountSpent) { this.amountSpent = amountSpent; }
}