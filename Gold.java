
public class Gold extends Customer {
    protected Double discountPercent;

    //default constructor
    public Gold (){
        super();
        discountPercent = 0.00;
    }
    public Gold(String fName, String lName, String guestID, double amountSpent, double discountPercent) {
        super(fName, lName, guestID, amountSpent);  // Calls the parent constructor
        this.discountPercent = discountPercent;
    }
    // Accssor 
    public Double getDiscountPercent() {return discountPercent;}
    //Mutator
    public void setDiscountPercent(Double discountPercent){this.discountPercent = discountPercent; }
}
