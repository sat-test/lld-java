package Splitwise;

import java.util.*;

class User {
    private String userId;
    private String name;
    private double balance;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.balance = 0.0;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }
}

interface SplitStrategy {
    Map<String, Double> split(double amount, List<User> users, List<Double> values);
}

class EqualSplit implements SplitStrategy {
    @Override
    public Map<String, Double> split(double amount, List<User> users, List<Double> values) {
        Map<String, Double> splitAmounts = new HashMap<>();
        double splitAmount = amount / users.size();
        for (User user : users) {
            splitAmounts.put(user.getUserId(), splitAmount);
        }
        return splitAmounts;

    }
}

class ExactSplit implements SplitStrategy {

    @Override
    public Map<String, Double> split(double amount, List<User> users, List<Double> values) {
        Map<String, Double> splitAmounts = new HashMap<>();
        for (int i = 0; i < users.size(); i++) {
            splitAmounts.put(users.get(i).getUserId(), values.get(i));
        }
        return splitAmounts;
    }
}

class PercentageSplit implements SplitStrategy {

    @Override
    public Map<String, Double> split(double amount, List<User> users, List<Double> percentages) {
        Map<String, Double> splitAmounts = new HashMap<>();
        for (int i = 0; i < users.size(); i++) {
            double share = (percentages.get(i) / 100) * amount;
            splitAmounts.put(users.get(i).getUserId(), share);
        }
        return splitAmounts;
    }
}

enum SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE
}

class SplitStrategyFactory {
    public static SplitStrategy getSplitStrategy(SplitType type) {
        switch (type) {
            case EQUAL:
                return new EqualSplit();
            case EXACT:
                return new ExactSplit();
            case PERCENTAGE:
                return new PercentageSplit();
            default:
                throw new IllegalArgumentException("Invalid split type");
        }
    }
}

class Expense {
    private String expenseId;
    private User paidBy;
    private double totalAmount;
    private Map<String, Double> splits;

    public Expense(String expenseId, User paidBy, double totalAmount, Map<String, Double> splits) {
        this.expenseId = expenseId;
        this.paidBy = paidBy;
        this.totalAmount = totalAmount;
        this.splits = splits;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Map<String, Double> getSplits() {
        return splits;
    }
}

class SplitwiseGroup {
    private String id;
    private String name;
    private Map<String, User> users;
    private List<Expense> expenses;

    public SplitwiseGroup(String id, String name) {
        this.id = id;
        this.name = name;
        this.users = new HashMap<>();
        this.expenses = new ArrayList<>();
    }

    public void addUser(User user) {
        User copyUser = new User(user.getUserId(), user.getName());
        users.put(user.getUserId(), copyUser);
    }

    public void splitExpenses(String expenseId, String paidByUserId, double amount, List<String> userIds, SplitType splitType, List<Double> values) {
        User paidBy = users.get(paidByUserId);
        List<User> splitUsers = new ArrayList<>();
        for (String userId : userIds) {
            splitUsers.add(users.get(userId));
        }

        SplitStrategy splitStrategy = SplitStrategyFactory.getSplitStrategy(splitType);
        Map<String, Double> splitAmounts = splitStrategy.split(amount, splitUsers, values);

        Expense expense = new Expense(expenseId, paidBy, amount, splitAmounts);
        expenses.add(expense);

        for(Map.Entry<String, Double> entry : splitAmounts.entrySet()) {
            String userId = entry.getKey();
            double splitAmount = entry.getValue();
            User user = users.get(userId);

            if(user.getUserId().equals(paidByUserId)) {
                user.addBalance(amount - splitAmount);
            } else {
                user.addBalance(-splitAmount);
            }
        }
    }

    public void showExpenses() {
        for (Expense expense : expenses) {
            System.out.println("Expense: " + expense.getExpenseId() + ", Paid by: " + expense.getPaidBy().getName() + ", Amount: " + expense.getTotalAmount());
            System.out.println("Splits: ");
            for (Map.Entry<String, Double> entry : expense.getSplits().entrySet()) {
                System.out.println("User: " + users.get(entry.getKey()).getName() + ", Share: " + entry.getValue());
            }
            System.out.println();
        }
    }

    public void showExpensesForUser(String userId) {
        User user = users.get(userId);
        System.out.println("Expenses for User: " + user.getName());
        for (Expense expense : expenses) {
            if (expense.getSplits().containsKey(userId)) {
                System.out.println("Expense ID: " + expense.getExpenseId() + ", Amount: " + expense.getSplits().get(userId));
            }
        }
        System.out.println("Balance: " + user.getBalance());
    }

    public Map<String, Double> showAmountSettlementDetailsForUser(String userId) {
        Map<String, Double> settlements = new HashMap<>();
        User currentUser = users.get(userId);
        if (currentUser == null) {
            throw new IllegalArgumentException("User not found in group");
        }

        for (Expense expense : expenses) {
            String paidByUserId = expense.getPaidBy().getUserId();
            Map<String, Double> splits = expense.getSplits();

            // If the user is the payer, calculate how much others owe them
            if (paidByUserId.equals(userId)) {
                for (Map.Entry<String, Double> entry : splits.entrySet()) {
                    String splitUserId = entry.getKey();
                    double splitAmount = entry.getValue();

                    if (!splitUserId.equals(userId)) {
                        settlements.put(splitUserId, settlements.getOrDefault(splitUserId, 0.0) + splitAmount);
                    }
                }
            } else if (splits.containsKey(userId)) {
                // If the user is part of the split, calculate what they owe
                double splitAmount = splits.get(userId);
                settlements.put(paidByUserId, settlements.getOrDefault(paidByUserId, 0.0) - splitAmount);
            }
        }

        return settlements;
    }

}

class SplitwiseService {

    Map<String, User> users;
    Map<String, SplitwiseGroup> splitwiseGroups;

    public SplitwiseService() {
        users = new HashMap<>();
        splitwiseGroups = new HashMap<>();
    }

    public void createGroup(String groupId, String groupName) {
        SplitwiseGroup splitwiseGroup = new SplitwiseGroup(groupId, groupName);
        splitwiseGroups.put(groupId, splitwiseGroup);
    }

    public void addUser(String userId, String name) {
        User user = new User(userId, name);
        users.put(userId, user);
    }

    public void addUserToGroup(String groupId, String userId) {
        User user = users.get(userId);
        splitwiseGroups.get(groupId).addUser(user);
    }

    public void splitExpenses(String groupId, String expenseId, String paidByUserId, double amount, List<String> userIds, SplitType splitType, List<Double> values) {
        splitwiseGroups.get(groupId).splitExpenses(expenseId, paidByUserId, amount, userIds, splitType, values);
    }

    public void showExpenses(String groupId) {
        splitwiseGroups.get(groupId).showExpenses();
    }

    public void showExpensesForUser(String groupId, String userId) {
        splitwiseGroups.get(groupId).showExpensesForUser(userId);
    }

    public Map<String, Double> showAmountSettlementDetailsForUser(String groupId, String userId) {
        return splitwiseGroups.get(groupId).showAmountSettlementDetailsForUser(userId);
    }
}

public class Splitwise {

    public static void main(String[] args) {
        SplitwiseService service = new SplitwiseService();

// Users
        service.addUser("U1", "Alice");
        service.addUser("U2", "Bob");
        service.addUser("U3", "Charlie");

// Group
        service.createGroup("G1", "Trip");
        service.addUserToGroup("G1", "U1");
        service.addUserToGroup("G1", "U2");
        service.addUserToGroup("G1", "U3");

// Expenses
        List<String> userIds = Arrays.asList("U1", "U2", "U3");
        List<Double> percentages = Arrays.asList(50.0, 30.0, 20.0);
        service.splitExpenses("G1", "E1", "U1", 300.0, userIds, SplitType.PERCENTAGE, percentages);

// Show Settlement Details
        Map<String, Double> settlementDetails = service.showAmountSettlementDetailsForUser("G1", "U2");
        settlementDetails.forEach((k, v) -> System.out.println("User " + k + " owes: " + v));

    }
}
