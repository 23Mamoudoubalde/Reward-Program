### **📌 Overview**

This Java program models a Customer Rewards Program using object-oriented programming principles such as inheritance, encapsulation, and polymorphism.
The project simulates different customer types—Standard, Gold, and Platinum—each with unique attributes and behaviors. It allows you to create customer profiles and store reward-related data such as amount spent, discount percentages, and bonus bucks.

### 🗂️ Project Structure

## 1. Customer.java

The base class representing a general customer.

Contains attributes like first name, last name, guest ID, and total amount spent.

Provides accessors and mutators for managing customer data.

## 2. Gold.java

Inherits from Customer.

Adds a discountPercent field specific to Gold members.

Includes constructors, an accessor, and a mutator for the discount percentage.

## 3. Platinum.java

Inherits from Customer.

Introduces bonusBucks, a reward system for Platinum members.

Includes a constructor, accessor, and mutator for managing bonus bucks.

## 4. Main.java

The driver class that creates instances of different customer types.

Demonstrates the functionality of the program.

Displays customer information and allows testing of class methods and inheritance.

## 🧠 Key Concepts Demonstrated

**🧬 Inheritance:**

Gold and Platinum classes extend the Customer class, inheriting shared properties.

**🔐 Access Level:**

Class fields are private or protected with public getter and setter methods.

**🔄 Constructor Overloading:**

Multiple constructors support flexible object creation.

**🎭 Polymorphism (if applied in Main.java):**
Demonstrates treating subclass objects as base class types for generalized behavior.
