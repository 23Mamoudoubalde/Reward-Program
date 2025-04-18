import java.io.*;
import java.util.Scanner;

public class Main {
    // Arrays to store regular and preferred customers
    private static Customer[] regularCustomers = new Customer[0];
    private static Customer[] preferredCustomers = new Customer[0]; 


    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Prompt for file names
            System.out.println("Enter regular customers file:");
            String regularFile = scanner.nextLine();
            System.out.println("Enter preferred customers file:");
            String preferredFile = scanner.nextLine();
            System.out.println("Enter orders file:");
            String ordersFile = scanner.nextLine();

            // Read customer from files
            readCustomers(regularFile, false);
            readCustomers(preferredFile, true);

            // Process orders and update customer spending
            processOrders(ordersFile);

            // Write updated customer data to output files
            writeCustomers("customer.dat", "preferred.dat");

            scanner.close();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    // Read customer data from a file and stores it into appropriate array
    private static void readCustomers(String filename, boolean isPreferred) throws IOException {
        File file = new File(filename);
        
        if (!file.exists()) {
            if (isPreferred) {
                // It's okay if preferred customer file doesn't exist
                return;
            } else {
                System.out.println("File not found: " + filename);
                return;
            }
        }
    
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        
        try {
            while ((line = reader.readLine()) != null) {
                String[] data = line.trim().split("\\s+"); 
        
                // Ensure the line contains sufficient data
                if (data.length < 4) {
                    System.out.println("Skipping invalid line (too few elements): " + line);
                    continue;
                }
        
                // Parse customer ID and names
                String id = data[0];
                String firstName = data[1];
                String lastName = data[2];
                
                // Amount spent
                double amount;
                try {
                    amount = Double.parseDouble(data[3]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount format: " + data[3]);
                    continue;
                }
        
                if (isPreferred) {
                    if (data.length < 5) {
                        System.out.println("Skipping invalid preferred customer (missing discount/bonus): " + line);
                        continue;
                    }
                    
                    // Determine if this is Gold or Platinum customer
                    if (data[4].contains("%")) {
                        // Gold customer
                        double discount;
                        try {
                            discount = Double.parseDouble(data[4].replace("%", "").trim());
                            preferredCustomers = resizeArray(preferredCustomers, 
                                new Gold(firstName, lastName, id, amount, discount));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid discount format: " + data[4]);
                            continue;
                        }
                    } else {
                        // Platinum customer
                        int bonus;
                        try {
                            bonus = Integer.parseInt(data[4].trim());
                            preferredCustomers = resizeArray(preferredCustomers,
                                new Platinum(firstName, lastName, id, amount, bonus));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid bonus format: " + data[4]);
                            continue;
                        }
                    }
                } else {
                    // Regular customer
                    regularCustomers = resizeArray(regularCustomers, 
                        new Customer(firstName, lastName, id, amount));
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading customer data: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    // Process orders data from a file and updates customer spending
   // Process orders data from a file and update customer spending
private static void processOrders(String filename) throws IOException {
    File file = new File(filename);
    
    if (!file.exists()) {
        System.out.println("Error: Orders file not found.");
        return;
    }

    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    
    try {
        while ((line = reader.readLine()) != null) {
            String[] data = line.trim().split("\\s+");
            
            if (data.length != 5) {
                System.out.println("Skipping invalid order (incorrect number of fields): " + line);
                continue;
            }

            String id = data[0];
            String size = data[1];
            String drinkType = data[2]; 
            double pricePerInch;
            int quantity;

            Customer customer = findCustomer(id);
            if (customer == null) {
                System.out.println("Skipping invalid order (Customer ID not found): " + id);
                continue;
            }

            if (!size.equals("S") && !size.equals("M") && !size.equals("L")) {
                System.out.println("Skipping invalid order (Invalid cup size): " + size);
                continue;
            }

            if (!drinkType.equalsIgnoreCase("frap") && 
                !drinkType.equalsIgnoreCase("tea") && 
                !drinkType.equalsIgnoreCase("latte")) {
                System.out.println("Skipping invalid order (Invalid drink type): " + drinkType);
                continue;
            }

            try {
                pricePerInch = Double.parseDouble(data[3]);
                quantity = Integer.parseInt(data[4]);

                if (quantity <= 0) {
                    System.out.println("Skipping invalid order (Quantity must be positive): " + quantity);
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid order (Invalid number format in price/quantity): " + line);
                continue;
            }

            // Calculate the drink price
            double drinkPrice = calculateDrinkPrice(size, drinkType, pricePerInch);
            double totalPrice = quantity * drinkPrice;
            double originalAmount = customer.getASpent();
            double discountedPrice = totalPrice;

            //  Apply Gold discount before updating spending
            if (customer instanceof Gold) {
                Gold goldCustomer = (Gold) customer;
                double discountPercent = goldCustomer.getDiscountPercent();
                discountedPrice = totalPrice * (1 - discountPercent / 100.0);
            }

            // Apply Platinum bonus bucks before updating spending
            else if (customer instanceof Platinum) {
                Platinum platinumCustomer = (Platinum) customer;
                int bonusBucksToUse = platinumCustomer.getBonus();
                int bucksUsed = (int) Math.min(bonusBucksToUse, totalPrice);
                
                // Deduct bonus bucks but do not lower spending count for future rewards
                discountedPrice = totalPrice - bucksUsed;
                
                platinumCustomer.setBonus(platinumCustomer.getBonus() - bucksUsed);
            }


            // Update spending with discounted price
            customer.setASpent(originalAmount + discountedPrice);

            //  Check if a Regular customer should be upgraded to Gold
            if (customer.getASpent() >= 50 && !(customer instanceof Gold || customer instanceof Platinum)) {
                customer = upgradeToGold(customer);
                updateGoldDiscount((Gold) customer);
            }

            // Apply Gold discount FIRST before checking for Platinum upgrade
            if (customer instanceof Gold && customer.getASpent() >= 200) {
                customer = upgradeToPlatinum((Gold) customer);
            }

            //  If Platinum, update bonus bucks correctly
            if (customer instanceof Platinum) {
                updatePlatinumBonus((Platinum) customer, originalAmount);
            }
        }
    } finally {
        reader.close();
    }
}


    // Calculate the price of a drink based on size, type, and personalization
    private static double calculateDrinkPrice(String size, String drinkType, double pricePerInch) {
        final double FRAPPUCCINO_PRICE = 0.20;
        final double TEA_PRICE = 0.12;
        final double LATTE_PRICE = 0.15;
        final double PI = Math.PI;
        
        double diameter = 0, height = 0, ounces = 0, pricePerOunce = 0;
        
        // Set cup dimensions based on size
        switch (size) {
            case "S":
                diameter = 4;
                height = 4.5;
                ounces = 12;
                break;
            case "M":
                diameter = 4.5;
                height = 5.75;
                ounces = 20;
                break;
            case "L":
                diameter = 5.5;
                height = 7;
                ounces = 32;
                break;
        }

        // Set price per ounce based on drink type
        switch (drinkType.toLowerCase()) {
            case "latte":
                pricePerOunce = LATTE_PRICE;
                break;
            case "tea":
                pricePerOunce = TEA_PRICE;
                break;
            case "frap":
                pricePerOunce = FRAPPUCCINO_PRICE;
                break;
        }

        // Calculate the surface area of cylinder (2πrh)
        double radius = diameter / 2.0;
        double surfaceArea = 2 * PI * radius * height;
        
        // Calculate personalization cost and base price
        double personalizationCost = surfaceArea * pricePerInch;
        double basePrice = ounces * pricePerOunce;

        return personalizationCost + basePrice;
    }

    // Search for a customer by ID
    private static Customer findCustomer(String id) {
        // Check preferred customers first
        for (Customer c : preferredCustomers) {
            if (c != null && c.getGuestID().equals(id)) {
                return c;
            }
        }
        
        // Then check regular customers
        for (Customer c : regularCustomers) {
            if (c != null && c.getGuestID().equals(id)) {
                return c;
            }
        }
        
        return null;
    }

    // Update a Gold customer's discount percentage based on total spending
    private static void updateGoldDiscount(Gold customer) {
        double spent = customer.getASpent();
        double newDiscount = 0.0;

        // Determine the correct discount 
        if (spent >= 150 && spent < 200 && customer.getDiscountPercent() < 15.0) {
            customer.setDiscountPercent(15.0);
        } else if (spent >= 100 && spent < 150 && customer.getDiscountPercent() < 10.0) {
            customer.setDiscountPercent(10.0);
        } else if (customer.getDiscountPercent() < 5.0) {
            customer.setDiscountPercent(5.0);
        }


        //  update the discount if it changes
        if (customer.getDiscountPercent() < newDiscount) {
            customer.setDiscountPercent(newDiscount);
        }
    }


    // Update a Platinum customer's bonus bucks based on spending

    private static void updatePlatinumBonus(Platinum customer, double previousSpent) {
        double currentSpent = customer.getASpent();

        if (currentSpent < 200) {
            return; // No bonus bucks if spending is below $200
        }

        // Ensure previousSpent starts at 200 if customer just reached Platinum
        previousSpent = Math.max(previousSpent, 200);
        
        int previousBonusBucks = (int) Math.floor((previousSpent - 200) / 5);
        int currentBonusBucks = (int) Math.floor((currentSpent - 200) / 5);

        // Add only new bonus bucks earned
        int newBonusBucks = currentBonusBucks - previousBonusBucks;

        if (newBonusBucks > 0) {
            customer.setBonus(customer.getBonus() + newBonusBucks);
        }
    }



    // Upgrade a regular customer to Gold status
    private static Customer upgradeToGold(Customer customer) {
        if (preferredCustomers == null) {
        preferredCustomers = new Customer[1]; 
        preferredCustomers[0] = new Gold(customer.getFname(), customer.getLname(),
                                        customer.getGuestID(), customer.getASpent(), 5.0);
        } else {
        preferredCustomers = resizeArray(preferredCustomers, new Gold(
                                        customer.getFname(), customer.getLname(),
                                        customer.getGuestID(), customer.getASpent(), 5.0));
        }

        
        regularCustomers = removeCustomer(regularCustomers, customer);
        return preferredCustomers[preferredCustomers.length - 1];
    }

    // Upgrade a Gold customer to Platinum status
    private static Customer upgradeToPlatinum(Gold goldCustomer) {
        int bonusBucks = (int) ((goldCustomer.getASpent() - 200) / 5); 

        Platinum platinumCustomer = new Platinum(
            goldCustomer.getFname(),
            goldCustomer.getLname(), 
            goldCustomer.getGuestID(),
            goldCustomer.getASpent(),
            bonusBucks
        );

        // Replace the Gold customer in the preferredCustomers array
        for (int i = 0; i < preferredCustomers.length; i++) {
            if (preferredCustomers[i].getGuestID().equals(goldCustomer.getGuestID())) {
                preferredCustomers[i] = platinumCustomer;
                break;
            }
        }
        
        return platinumCustomer;
    }


    // Write updated customer data back to files
    private static void writeCustomers(String regularFile, String preferredFile) throws IOException {
        // Write regular customers
        BufferedWriter regWriter = new BufferedWriter(new FileWriter(regularFile));
        for (Customer c : regularCustomers) {
            regWriter.write(String.format("%s %s %s %.2f%n", 
                c.getGuestID(), c.getFname(), c.getLname(), c.getASpent()));
        }
        regWriter.close();

        // Write preferred customers
        BufferedWriter prefWriter = new BufferedWriter(new FileWriter(preferredFile));
        for (Customer c : preferredCustomers) {
            if (c instanceof Gold) {
                prefWriter.write(String.format("%s %s %s %.2f %.0f%%%n",
                    c.getGuestID(), c.getFname(), c.getLname(), c.getASpent(), 
                    ((Gold) c).getDiscountPercent()));
            }
            else if (c instanceof Platinum) {
                prefWriter.write(String.format("%s %s %s %.2f %d%n",
                    c.getGuestID(), c.getFname(), c.getLname(), c.getASpent(), 
                    ((Platinum) c).getBonus()));
            }
        }
        prefWriter.close();
    }

    // Helper method to resize Customer arrays
    private static Customer[] resizeArray(Customer[] array, Customer newCustomer) {
        int newSize = array.length + 1; 
        Customer[] newArray = new Customer[newSize]; 
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = newCustomer;
        return newArray;
    }


    // Helper method to remove a customer from an array
    private static Customer[] removeCustomer(Customer[] array, Customer customer) {
        if (array.length == 0) return new Customer[0];
        
        Customer[] newArray = new Customer[array.length - 1];
        int index = 0;
        
        for (Customer c : array) {
            if (!c.getGuestID().equals(customer.getGuestID())) {
                if (index < newArray.length) {
                    newArray[index++] = c;
                }
            }
        }
        
        return newArray;
    }
}