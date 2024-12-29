/*
Design an inventory management system with queuing for incoming requests.
*/


enum Category {
    ELECTRONICS,
    GROCERIES,
    FASHION,
    HOME_APPLIANCES,
    BOOKS
}

class Item {
    private String id;
    private String name;
    private Integer quantity;
    private Double price;
    private Category category;
    
    public Item(String id, String name, Integer quantity, Double price, Category category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

class Inventory {
    private Map<String, Item> items;
    
    public Inventory() {
        items = new HashMap<>();
    }
    
    public void addItem(Item item) {
        items.put(item.getId(), item);
    }
    
    public Boolean addItemQuantity(String itemId, Integer quantity) {
        if(items.containsKey(itemId)) {
            Item item = items.get(itemId);
            item.setQuantity(item.getQuantity() + quantity);
            return true;
        }
        return false;
    }
    
    public boolean removeItemQuantity(String itemId, int quantity) {
        if (items.containsKey(itemId)) {
            Item item = items.get(itemId);
            if (item.getQuantity() >= quantity) {
                item.setQuantity(item.getQuantity() - quantity);
                return true;
            }
        }
        return false;
    }
    
    public Item getItem(String itemId) {
        return items.get(itemId);
    }
    
    public Map<String, Item> getItemsByCategory(Category category) {
        Map<String, Item> result = new HashMap<>();
        for (Item item : items.values()) {
            if (item.getCategory() == category) {
                result.put(item.getId(), item);
            }
        }
        return result;
    }
}

class NotificationService {
    
    public void notifyUser(String userId, String message) {
        System.out.println("Notification sent to User " + userId + ": " + message);
    }
    
    public void notifyAdmin(String message) {
        System.out.println("Notification sent to Admin: " + message);
    }
}

interface InventoryCommand {
    void execute();
}

class AddItemCommand implements InventoryCommand {
    private Item item;
    private Inventory inventory;
    private NotificationService notificationService;
    
    public AddItemCommand(Item item, Inventory inventory, NotificationService notificationService) {
        this.item = item;
        this.inventory = inventory;
        this.notificationService = notificationService;
    }
    
    @Override
    public void execute() {
        inventory.addItem(item);
        notificationService.notifyAdmin("Item added to inventory: " + item.getName());
    }
}

class AddItemQuantityCommand implements InventoryCommand {
    private String itemId;
    private int quantity;
    private Inventory inventory;
    private NotificationService notificationService;
    
    public AddItemQuantityCommand(String itemId, int quantity, Inventory inventory, NotificationService notificationService) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.inventory = inventory;
        this.notificationService = notificationService;
    }
    
    @Override
    public void execute() {
        Boolean success = inventory.addItemQuantity(itemId, quantity);
        if(success) {
            notificationService.notifyAdmin(quantity + " Item Quantity added to inventory: " + itemId);
        } else {
            notificationService.notifyAdmin("Failed to add quantity for item: " + itemId);
        }
    }
}

class RemoveItemQuantityCommand implements InventoryCommand {
    private String itemId;
    private int quantity;
    private Inventory inventory;
    private NotificationService notificationService;
    
    public RemoveItemQuantityCommand(String itemId, int quantity, Inventory inventory, NotificationService notificationService) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.inventory = inventory;
        this.notificationService = notificationService;
    }
    
    @Override
    public void execute() {
        Boolean success = inventory.removeItemQuantity(itemId, quantity);
        if (success) {
            notificationService.notifyAdmin("Item removed from inventory: " + itemId);
        } else {
            notificationService.notifyAdmin("Failed to remove item (Insufficient stock): " + itemId);
        }
    }
}

class InventoryProcessor extends Thread {
    private final BlockingQueue<InventoryCommand> commandQueue;
    private volatile boolean running = true;
    
    public InventoryProcessor(BlockingQueue<InventoryCommand> commandQueue) {
        this.commandQueue = commandQueue;
    }
    
    @Override
    public void run() {
        while(running) {
            try {
                InventoryCommand command = commandQueue.take();
                command.execute();
            } catch (InterruptedException e) {
                if (!running) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    public void stopProcessing() {
        running = false;
        this.interrupt(); 
    }
}

class InventoryManager {
    private final LinkedBlockingQueue<InventoryCommand> commandQueue;
    private final InventoryProcessor processor;
    
    public InventoryManager() {
        this.commandQueue = new LinkedBlockingQueue<>();
        this.processor = new InventoryProcessor(commandQueue);
        this.processor.start();
    }
    
    public void addCommand(InventoryCommand command) {
        try {
            commandQueue.put(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to add command to the queue: " + e.getMessage());
        }
    }
    
    public void stopProcessing() {
        processor.stopProcessing();
    }
}


public class Main {
    public static void main(String[] args) {
        Inventory inventory = new Inventory();
        NotificationService notificationService = new NotificationService();
        InventoryManager manager = new InventoryManager();

        // Add items
        Item item1 = new Item("1", "Laptop", 10, 800.0, Category.ELECTRONICS);
        Item item2 = new Item("2", "Apple", 50, 1.0, Category.GROCERIES);

        manager.addCommand(new AddItemCommand(item1, inventory, notificationService));
        manager.addCommand(new AddItemCommand(item2, inventory, notificationService));

        // Remove items
        manager.addCommand(new RemoveItemQuantityCommand("2", 50, inventory, notificationService));
        manager.addCommand(new RemoveItemQuantityCommand("2", 1, inventory, notificationService));

        // Wait for processing
        try {
            Thread.sleep(2000); // Simulate delay for processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Stop processing commands
        manager.stopProcessing();

        // Query by category
        System.out.println("Items in ELECTRONICS:");
        inventory.getItemsByCategory(Category.ELECTRONICS).forEach((id, item) -> System.out.println(item.getName()));

        System.out.println("Items in GROCERIES:");
        inventory.getItemsByCategory(Category.GROCERIES).forEach((id, item) -> System.out.println(item.getName()));
    }
}
