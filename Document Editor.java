/*
Low level design for a real-time collaborative document editing platform like Google Docs.
*/

class User {
    private String userId;
    
    public User(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }
}


interface DocumentObserver {
    void update(Document document, String eventType);
}


class Document {
    private String content;
    private List<User> collaborators;
    private List<String> versionHistory;
    private Map<User, Integer> cursorPositions;
    private Map<User, DocumentObserver> observers;

    public Document() {
        this.content = "";
        this.collaborators = new ArrayList<>();
        this.versionHistory = new ArrayList<>();
        this.cursorPositions = new HashMap<>();
        this.observers = new HashMap<>();
    }

    public synchronized void editDocument(String newText) {
        versionHistory.add(content);
        content = newText;
        notifyObservers("edit");
    }

    public synchronized void addCollaborator(User user, DocumentObserver observer) {
        collaborators.add(user);
        cursorPositions.put(user, 0);
        observers.put(user, observer);
    }

    public synchronized void removeCollaborator(User user) {
        collaborators.remove(user);
        cursorPositions.remove(user);
        observers.remove(user);
    }

    public synchronized void updateCursorPosition(User user, int newPosition) {
        cursorPositions.put(user, newPosition);
        notifyObservers("cursor_update");
    }

    private void notifyObservers(String eventType) {
        for (DocumentObserver observer : observers.values()) {
            observer.update(this, eventType);
        }
    }

    public synchronized String getContent() {
        return content;
    }

    public synchronized int getCursorPosition(User user) {
        return cursorPositions.getOrDefault(user, 0);
    }
}


class CursorPositionObserver implements DocumentObserver {
    private User user;

    public CursorPositionObserver(User user) {
        this.user = user;
    }

    @Override
    public void update(Document document, String eventType) {
        if ("cursor_update".equals(eventType)) {
            System.out.println("Cursor position for " + user.getUserId() + ": " + document.getCursorPosition(user));
        } else if ("edit".equals(eventType)) {
            System.out.println("Document edited. New content: " + document.getContent());
        }
    }
}


class CollaborativeEditor {
    private Document document;

    public CollaborativeEditor(Document document) {
        this.document = document;
    }

    public void addCollaborator(User user) {
        CursorPositionObserver observer = new CursorPositionObserver(user);
        document.addCollaborator(user, observer);
    }

    public void removeCollaborator(User user) {
        document.removeCollaborator(user);
    }

    public void editDocument(String newText) {
        document.editDocument(newText);
    }
}



public class RealTimeCollaborativeEditor {
    public static void main(String[] args) {
        // Create users
        User user1 = new User("user1");
        User user2 = new User("user2");

        // Create a document
        Document document = new Document();

        // Create a collaborative editor
        CollaborativeEditor collaborativeEditor = new CollaborativeEditor(document);

        // Add collaborators to the document
        collaborativeEditor.addCollaborator(user1);
        collaborativeEditor.addCollaborator(user2);

        // Edit the document
        collaborativeEditor.editDocument("This is the edited document content.");

        // Update cursor position for user1
        document.updateCursorPosition(user1, 10);

        // Update cursor position for user2
        document.updateCursorPosition(user2, 15);
    }
}
