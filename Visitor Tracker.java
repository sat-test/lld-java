/*
Implement 2 APIs: one to track user visits, the other to return the first user to visit only once. Follow-up: return the first N users who visited only once.

Below is the Java implementation of two APIs:

trackVisit(String userId): Tracks a user visit by updating a data structure.
getFirstUniqueVisitor(): Returns the first user who visited only once.
Follow-up: getFirstNUniqueVisitors(int n): Returns the first N users who visited only once.

*/


class UniqueVisitorTracker {
    private Map<String, Integer> visitCountMap;
    private LinkedHashSet<String> uniqueVisitors;
    
    public UniqueVisitorTracker() {
        this.visitCountMap = new HashMap<>();
        this.uniqueVisitors = new LinkedHashSet<>();
    }
    
    public void trackVisit(String userId) {
        visitCountMap.put(userId, visitCountMap.getOrDefault(userId, 0) + 1);
        if(visitCountMap.get(userId) == 1) {
            uniqueVisitors.add(userId);
        } else {
            uniqueVisitors.remove(userId);
        }
    }
    
    public String getFirstUniqueVisitor() {
        return uniqueVisitors.isEmpty() ? null : uniqueVisitors.iterator().next();
    }
    
    public List<String> getFirstNUniqueVisitors(int n) {
        List<String> result = new ArrayList<>();
        Iterator<String> iterator = uniqueVisitors.iterator();
        while(iterator.hasNext() && result.size() < n) {
            result.add(iterator.next());
        }
        return result;
    }
}


public class Main {
    public static void main(String[] args) {
        UniqueVisitorTracker tracker = new UniqueVisitorTracker();
        tracker.trackVisit("user1");
        tracker.trackVisit("user2");
        tracker.trackVisit("user1");
        tracker.trackVisit("user3");
        tracker.trackVisit("user4");
        
        System.out.println("First unique visitor: " + tracker.getFirstUniqueVisitor()); // user2
        System.out.println("First 2 unique visitors: " + tracker.getFirstNUniqueVisitors(2)); // [user2, user3]

        tracker.trackVisit("user2");

        System.out.println("First unique visitor: " + tracker.getFirstUniqueVisitor()); // user3
        System.out.println("First 2 unique visitors: " + tracker.getFirstNUniqueVisitors(2)); // [user3, user4]
    }
}
