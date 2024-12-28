/*
Given N lockers and the location of a person, determine the nearest locker where the person can drop a package (location is represented by coordinates (x, y)).
*/

class Package {
    private String id;
    private String description;

    public Package(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}


class Locker {
    private String id;
    private int x;
    private int y;
    private boolean isAvailable;
    private Package storedPackage;

    public Locker(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isAvailable = true;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void storePackage(Package pkg) {
        this.storedPackage = pkg;
        this.isAvailable = false;
    }

    public Package retrievePackage() {
        Package pkg = this.storedPackage;
        this.storedPackage = null;
        this.isAvailable = true;
        return pkg;
    }
}

class User {
    private String id;
    private int x;
    private int y;

    public User(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

class NotificationService {
    public void sendNotification(String userId, String message) {
        System.out.println("Notification sent to User " + userId + ": " + message);
    }
}

class LockerManager {
    private List<Locker> lockers;
    private NotificationService notificationService;

    public LockerManager(NotificationService notificationService) {
        this.lockers = new ArrayList<>();
        this.notificationService = notificationService;
    }

    public void addLocker(Locker locker) {
        lockers.add(locker);
    }

    public Locker findNearestLocker(int userX, int userY) {
        Locker nearestLocker = null;
        double minDistance = Double.MAX_VALUE;

        for (Locker locker : lockers) {
            if (locker.isAvailable()) {
                double distance = calculateDistance(userX, userY, locker.getX(), locker.getY());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestLocker = locker;
                }
            }
        }

        return nearestLocker;
    }

    public void storePackage(Package pkg, int userX, int userY, User user) {
        Locker nearestLocker = findNearestLocker(userX, userY);

        if (nearestLocker != null) {
            nearestLocker.storePackage(pkg);
            System.out.println("Package stored in locker: " + nearestLocker.getId());
            notificationService.sendNotification(user.getId(), "Your package has been stored in locker: " + nearestLocker.getId());
        } else {
            System.out.println("No available locker nearby.");
            notificationService.sendNotification(user.getId(), "Failed to store your package. No lockers are available nearby.");
        }
    }

    public Package retrievePackage(String lockerId, User user) {
        for (Locker locker : lockers) {
            if (locker.getId().equals(lockerId) && !locker.isAvailable()) {
                Package pkg = locker.retrievePackage();
                notificationService.sendNotification(user.getId(), "You have successfully retrieved your package from locker: " + lockerId);
                return pkg;
            }
        }
        System.out.println("No package found in locker: " + lockerId);
        notificationService.sendNotification(user.getId(), "Failed to retrieve package. No package found in locker: " + lockerId);
        return null;
    }

    private double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}

public class Main {
    public static void main(String[] args) {
        // Initialize NotificationService
        NotificationService notificationService = new NotificationService();

        // Initialize LockerManager
        LockerManager lockerManager = new LockerManager(notificationService);

        // Add lockers
        lockerManager.addLocker(new Locker("L1", 0, 0));
        lockerManager.addLocker(new Locker("L2", 10, 10));
        lockerManager.addLocker(new Locker("L3", 5, 5));

        // Create a user
        User user = new User("U1", 2, 2);

        // Create a package
        Package pkg = new Package("P1", "Electronics");

        // Store the package
        lockerManager.storePackage(pkg, user.getX(), user.getY(), user);

        // Retrieve the package
        Package retrievedPkg = lockerManager.retrievePackage("L1", user);
        if (retrievedPkg != null) {
            System.out.println("Package retrieved: " + retrievedPkg.getDescription());
        }
    }
}


