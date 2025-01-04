import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

class User {
    private final String userId;
    private final String name;
    private final String email;
    private final String mobile;

    public User(String userId, String name, String email, String mobile) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public String getUserId() {
        return userId;
    }
}

class FollowManager {
    private final Map<String, Set<String>> followers = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> followees = new ConcurrentHashMap<>();

    public void follow(String follower, String followee) {
        followers.computeIfAbsent(followee, k -> ConcurrentHashMap.newKeySet()).add(follower);
        followees.computeIfAbsent(follower, k -> ConcurrentHashMap.newKeySet()).add(followee);
    }

    public void unfollow(String follower, String followee) {
        Optional.ofNullable(followers.get(followee)).ifPresent(f -> f.remove(follower));
        Optional.ofNullable(followees.get(follower)).ifPresent(f -> f.remove(followee));
    }

    public Set<String> getFollowers(String userId) {
        return followers.getOrDefault(userId, Collections.emptySet());
    }
}

class Article {
    private final String articleId;
    private final String content;
    private final List<String> imageUrls;
    private final String writer;
    private final Timestamp publishedOn;
    private int likes;

    public Article(String content, List<String> imageUrls, String writer) {
        this.articleId = UUID.randomUUID().toString();
        this.content = content;
        this.imageUrls = imageUrls;
        this.writer = writer;
        this.publishedOn = Timestamp.valueOf(LocalDateTime.now());
        this.likes = 0;
    }

    public void incrementLikes() {
        likes++;
    }

    public void decrementLikes() {
        likes--;
    }

    public String getWriter() {
        return writer;
    }

    public Timestamp getPublishedOn() {
        return publishedOn;
    }

    @Override
    public String toString() {
        return "Article{" +
                "articleId='" + articleId + '\'' +
                ", content='" + content + '\'' +
                ", imageUrls=" + imageUrls +
                ", writer='" + writer + '\'' +
                ", publishedOn=" + publishedOn +
                ", likes=" + likes +
                '}';
    }
}

class ArticleStorage {
    private final List<Article> articles = Collections.synchronizedList(new ArrayList<>());

    public void storeArticle(Article article) {
        articles.add(article);
    }

    public List<Article> getArticlesByUser(String userId) {
        return articles.stream()
                .filter(article -> article.getWriter().equals(userId))
                .collect(Collectors.toList());
    }
}

class UserFeed {
    private final List<Article> articles = Collections.synchronizedList(new ArrayList<>());
    private final int limit;

    public UserFeed(int limit) {
        this.limit = limit;
    }

    public synchronized void addArticle(Article article) {
        articles.add(article);
        if (articles.size() > limit) {
            articles.remove(0);
        }
    }

    public synchronized List<Article> getFeed(int page, int pageSize) {
        return articles.stream()
                .sorted(Comparator.comparing(Article::getPublishedOn).reversed())
                .skip((long) page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }
}

class FeedWorker {
    private final Map<String, UserFeed> userFeeds = new ConcurrentHashMap<>();
    private final FollowManager followManager;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public FeedWorker(FollowManager followManager) {
        this.followManager = followManager;
    }

    public void createUserFeed(String userId, int limit) {
        userFeeds.put(userId, new UserFeed(limit));
    }

    public void processArticle(Article article) {
        executorService.submit(() -> publishArticleToFollowers(article));
    }

    private void publishArticleToFollowers(Article article) {
        Set<String> followers = followManager.getFollowers(article.getWriter());
        for (String follower : followers) {
            userFeeds.get(follower).addArticle(article);
        }
    }

    public List<Article> getFeed(String userId, int page, int pageSize) {
        return userFeeds.getOrDefault(userId, new UserFeed(0)).getFeed(page, pageSize);
    }
}

class MediumService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final FollowManager followManager = new FollowManager();
    private final ArticleStorage articleStorage = new ArticleStorage();
    private final FeedWorker feedWorker;

    public MediumService(int feedLimit) {
        this.feedWorker = new FeedWorker(followManager);
    }

    public void registerUser(User user, int feedLimit) {
        users.put(user.getUserId(), user);
        feedWorker.createUserFeed(user.getUserId(), feedLimit);
    }

    public void followUser(String follower, String followee) {
        followManager.follow(follower, followee);
    }

    public void unfollowUser(String follower, String followee) {
        followManager.unfollow(follower, followee);
    }

    public void publishArticle(Article article) {
        articleStorage.storeArticle(article);
        feedWorker.processArticle(article);
    }

    public List<Article> getUserFeed(String userId, int page, int pageSize) {
        return feedWorker.getFeed(userId, page, pageSize);
    }
}

public class Medium {
    public static void main(String[] args) {
        MediumService mediumService = new MediumService(10);

        User user1 = new User("1", "Amit", "amit@example.com", "1234567890");
        User user2 = new User("2", "Babu", "babu@example.com", "0987654321");
        User user3 = new User("3", "Calu", "calu@example.com", "0785753275");

        mediumService.registerUser(user1, 5);
        mediumService.registerUser(user2, 5);
        mediumService.registerUser(user3, 5);

        mediumService.followUser("2", "1");
        mediumService.followUser("3", "1");
        mediumService.followUser("3", "2");

        Article article1 = new Article("First Article", List.of("image1.jpg"), "1");
        Article article2 = new Article("Second Article", List.of("image2.jpg"), "2");
        mediumService.publishArticle(article1);
        mediumService.publishArticle(article2);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        List<Article> feedOfUser1 = mediumService.getUserFeed("1", 0, 5);
        System.out.println("Feed for user 1: " + feedOfUser1.size() + " articles : " + feedOfUser1);

        List<Article> feedOfUser2 = mediumService.getUserFeed("2", 0, 5);
        System.out.println("Feed for user 2: " + feedOfUser2.size() + " articles : " + feedOfUser2);

        List<Article> feedOfUser3 = mediumService.getUserFeed("3", 0, 5);
        System.out.println("Feed for user 3: " + feedOfUser3.size() + " articles : " + feedOfUser3);
    }
}
