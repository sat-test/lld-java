/*
Low level design
Design OOD Pizza Store
Write program to calculate price.
-base, - size, - toppings
That was it. had to be creative and write efficient object oriented code.
*/

abstract class Pizza {
    String description = "Unknown Pizza";
    public String getDescription() {
        return description;
    }
    public abstract Double getCost();
}

class Margherita extends Pizza {
    public Margherita() {
        description = "Margherita Pizza";
    }
    
    @Override
    public Double getCost() {
        return 5.00;
    }
}

class Pepperoni extends Pizza {
    public Pepperoni() {
        description = "Pepperoni Pizza";
    }
    
    @Override
    public Double getCost() {
        return 7.00;
    }
}

abstract class ToppingDecorator extends Pizza {
    public abstract String getDescription();
}

class Cheese extends ToppingDecorator {
    private Pizza pizza;
    
    public Cheese(Pizza pizza) {
        this.pizza = pizza;
    }
    
    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Cheese";
    }
    
    @Override
    public Double getCost() {
        return pizza.getCost() + 1.50;
    }
}

class Olives extends ToppingDecorator {
    private Pizza pizza;
    
    public Olives(Pizza pizza) {
        this.pizza = pizza;
    }
    
    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Olives";
    }
    
    @Override
    public Double getCost() {
        return pizza.getCost() + 1.00;
    }
}

class Chicken extends ToppingDecorator {
    private Pizza pizza;
    
    public Chicken(Pizza pizza) {
        this.pizza = pizza;
    }
    
    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Chicken";
    }
    
    @Override
    public Double getCost() {
        return pizza.getCost() + 2.00;
    }
}

abstract class SizeDecorator extends Pizza {
    public abstract String getDescription();
}

class SmallSize extends SizeDecorator {
    private Pizza pizza;
    
    public SmallSize(Pizza pizza) {
        this.pizza = pizza;
    }
    
    @Override
    public String getDescription() {
        return pizza.getDescription() + " (Small)";
    }
    
    @Override
    public Double getCost() {
        return pizza.getCost() - 1.00;
    }
}

class MediumSize extends SizeDecorator {
    private Pizza pizza;
    
    public MediumSize(Pizza pizza) {
        this.pizza = pizza;
    }
    
    @Override
    public String getDescription() {
        return pizza.getDescription() + " (Medium)";
    }
    
    @Override
    public Double getCost() {
        return pizza.getCost() + 0.50;
    }
}

class LargeSize extends SizeDecorator {
    private Pizza pizza;
    
    public LargeSize(Pizza pizza) {
        this.pizza = pizza;
    }
    
    @Override
    public String getDescription() {
        return pizza.getDescription() + " (Large)";
    }
    
    @Override
    public Double getCost() {
        return pizza.getCost() + 2.00;
    }
}

public class PizzaStore {
    public static void main(String[] args) {
        // Create a base pizza
        Pizza pizza = new Margherita();

        // Add toppings
        pizza = new Cheese(pizza);
        pizza = new Olives(pizza);
        pizza = new Chicken(pizza);

        // Set size
        pizza = new LargeSize(pizza);

        // Print description and cost
        System.out.println("Order: " + pizza.getDescription());
        System.out.println("Total Cost: $" + pizza.getCost());
    }
}
