
public class Platinum extends Customer {
    protected int bonusBucks;

    //Overloaded Constructor
    public Platinum(String fName, String lName, String guestID, double amountSpent, int bonusBucks){
        super(fName, lName, guestID, amountSpent);  // Calls the parent constructor
        this.bonusBucks = bonusBucks; 
    }

    //Accessor
    public int getBonus(){return bonusBucks;}
    //Muatator
    public void setBonus(int bonusBucks){ this.bonusBucks = bonusBucks; }

    
}
