/*
Implement an unbounded set with expiration.

Below is an implementation of an unbounded set with expiration in Java. This set will allow you to:
Add elements with an expiration time.
Automatically remove expired elements when certain operations are performed.
Check for element existence.
*/

class ExpiringSet<E> {
    private ConcurrentHashMap<E, Long> map;
    private ScheduledExecutorService cleaner;
    
    public ExpiringSet() {
        this.map = new ConcurrentHashMap<E, Long>();
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        startCleanerTask();
    }
    
    public void add(E element, long expirationMillis) {
        long expirationTime = System.currentTimeMillis() + expirationMillis;
        map.put(element, expirationTime);
    }
    
    public boolean contains(E element) {
        cleanup();
        return map.containsKey(element);
    }
    
    public boolean remove(E element) {
        cleanup();
        return map.remove(element) != null;
    }
    
    public int size() {
        cleanup();
        return map.size();
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        for (Map.Entry<E, Long> entry : map.entrySet()) {
            if (entry.getValue() <= now) {
                map.remove(entry.getKey());
            }
        }
    }
    
    private void startCleanerTask() {
        cleaner.scheduleAtFixedRate(() -> {
            cleanup();
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    public void stopCleaner() {
        cleaner.shutdownNow();
    }
}

public class Main {
    public static void main(String[] args) {
        ExpiringSet<String> expiringSet = new ExpiringSet<>();

        expiringSet.add("user1", 300); // Expires in 3 seconds
        expiringSet.add("user2", 500); // Expires in 5 seconds

        System.out.println("Contains user1? " + expiringSet.contains("user1")); // true
        System.out.println("Contains user2? " + expiringSet.contains("user2")); // true
        
        try{
            Thread.sleep(400); // Wait for 4 seconds
        } catch (Exception e) {
            
        }
        

        System.out.println("Contains user1? " + expiringSet.contains("user1")); // false (expired)
        System.out.println("Contains user2? " + expiringSet.contains("user2")); // true

        expiringSet.stopCleaner();
    }
}
