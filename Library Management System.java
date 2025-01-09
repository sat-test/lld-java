enum Category {
    SCIENCE,
    FICTION,
    TECHNOLGY,
    SOCIAL,
    LANGUAGE,
    GENERAL_KNOWLEDGE,
    ASTROLOGY
}

class Book {
    private final String id;
    private final String name;
    private final String edition;
    private final String title;
    private final String description;
    private final String publisher;
    private final List<String> writers;
    private Set<Integer> allSNoSets;
    private Set<Integer> availableSNoSets;
    private Double price;
    private Double rentRate;
    
    public Book(String id, String name, String edition, String title, String description, String publisher, List<String> writers, Set<Integer> allSNoSets, Double price, Double rentRate) {
        this.id = id;
        this.name = name;
        this.edition = edition;
        this.title = title;
        this.decription = description;
        this.publisher = publisher;
        this.writers = writers;
        this.allSNoSets = allSNoSets;
        this.availableSNoSets = allSNoSets;
        this.price = price;
        this.rentRate = rentRate;
    }
    
    public Boolean isBookAvailable() {
        return availableSNoSets.size() != 0;
    }
    
    public void issueBook(Integer sNo) {
        availableSNoSets.remove(sNo);
    }
    
    public void returnBook(Integer sNo) {
        availableSNoSets.add(sNo);
    }
    
    public Set<Integer> availableSNoSets() {
        return availableSNoSets;
    }
    
    public Double getRentRate() {
        return rentRate;
    }
}

interface Filter<T> {
    public List<T> filter(List<T> lists, Predicate<T> condition);
} 

class BookByCondition implements Filter<Book> {
    
    @Override
    public List<Book> filter(List<Book> books, Predicate<Book> condition) {
        
    }
} 

class BookSelf {
    
    
}



public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
