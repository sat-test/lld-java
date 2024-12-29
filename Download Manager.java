/*
Create the low-level design of a download manager capable of handling multiple downloads.

Here's a low-level design for a download manager that handles multiple downloads concurrently. 
We'll use the Observer Pattern to notify clients about the progress and status of downloads and 
the Thread Pool Design Pattern for managing multiple concurrent downloads.
*/

interface DownloadObserver {
    public void onProgress(String id, int progress);
    public void onComplete(String id);
    public void onError(String id, String message);
}

class DownloadTask {
    private final String id;
    private final String url;
    private int progress;
    private String status; // IN_PROGRESS, COMPLETED, FAILED
    private final List<DownloadObserver> observers;

    public DownloadTask(String id, String url) {
        this.id = id;
        this.url = url;
        this.progress = 0;
        this.status = "IN_PROGRESS";
        this.observers = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
    
    public int getProgress() {
        return progress;
    }

    public void registerObserver(DownloadObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        for (DownloadObserver observer : observers) {
            if (progress < 100) {
                observer.onProgress(id, progress);
            } else if ("COMPLETED".equals(status)) {
                observer.onComplete(id);
            } else if ("FAILED".equals(status)) {
                observer.onError(id, "Download failed");
            }
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        notifyObservers();
    }

    public void setStatus(String status) {
        this.status = status;
        notifyObservers();
    }
}

class DownloadWorker implements Runnable {
    private DownloadTask task;
    
    public DownloadWorker(DownloadTask task) {
        this.task = task;
    }
    
   public void run() {
        try {
            Random random = new Random();
            while (task.getStatus().equals("IN_PROGRESS") && task.getProgress() < 100) {
                Thread.sleep(500); // Simulate download time
                task.setProgress(task.getProgress() + random.nextInt(40) + 1);
            }
            task.setStatus("COMPLETED");
        } catch (Exception e) {
            task.setStatus("FAILED");
        }
    }
}

class DownloadManager {
    private ExecutorService executorService;
    private Map<String, DownloadTask> downloadTasks;
    
    public DownloadManager(int maxConcurrentDownloads) {
        this.executorService = Executors.newFixedThreadPool(maxConcurrentDownloads);
        this.downloadTasks = new HashMap<>();
    }
    
    public void addDownload(String url, DownloadObserver observer) {
        String id = "DL-" + System.currentTimeMillis();
        DownloadTask task = new DownloadTask(id, url);
        task.registerObserver(observer);
        downloadTasks.put(id, task);
        executorService.submit(new DownloadWorker(task));
    }
    
    public void cancelDownload(String id) {
        DownloadTask task = downloadTasks.get(id);
        if (task != null && "IN_PROGRESS".equals(task.getStatus())) {
            task.setStatus("FAILED");
        }
    }
    
    public String getDownloadStatus(String id) {
        DownloadTask task = downloadTasks.get(id);
        return task != null ? task.getStatus() : "Task not found";
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

public class Client implements DownloadObserver {
    public static void main(String[] args) {
        DownloadManager manager = new DownloadManager(3);

        Client client = new Client();
        manager.addDownload("http://example.com/file1.zip", client);
        manager.addDownload("http://example.com/file2.zip", client);
        manager.addDownload("http://example.com/file3.zip", client);

        try {
            Thread.sleep(2000); // Let downloads proceed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        manager.shutdown();
    }
    
    @Override
    public void onProgress(String id, int progress) {
        System.out.println("Download " + id + " is " + progress + "% complete.");
    }

    @Override
    public void onComplete(String id) {
        System.out.println("Download " + id + " completed successfully.");
    }

    @Override
    public void onError(String id, String message) {
        System.out.println("Download " + id + " failed with error: " + message);
    }
}
