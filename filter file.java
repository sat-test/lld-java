// "static void main" must be defined in a public class.
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class File {
    String name;
    Integer size;
    String createdAt;
    String modifiedAt;

    public File(String name, Integer size, String createdAt, String modifiedAt) {
        this.name = name;
        this.size = size;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public String getName() {
        return name;
    }

    public Integer getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "{ name: " + name + ", size: " + size + ", createdAt: " + createdAt + ", modifiedAt: " + modifiedAt + " }";
    }
}


interface Filter<T> {
    public List<File> filter(List<T> files, Predicate<T> condition);
}

class FileByCondition implements Filter<File> {
    
    @Override
    public List<File> filter(List<File> files, Predicate<File> condition) {
        List<File> res = new ArrayList<>();
        for(File file : files) {
            if(condition.test(file)) {
                res.add(file);
            }
        }
        return res;
    }
}


class FileManager {
    
    List<File> files;
    FileByCondition fileByCondition;
    
    public FileManager() {
        this.files = new ArrayList<>();
        this.fileByCondition = new FileByCondition();
    }
    
    public void addFile(File file) {
        files.add(file);
    }
    
    public List<File> getFileByName(String name) {
        Predicate<File> condition = file -> file.getName().equals(name);
        return fileByCondition.filter(files, condition);
    }
    
    public List<File> getFileBySize(Integer mini, Integer maxi) {
        Predicate<File> condition = file -> file.getSize() >= mini && file.getSize() <= maxi;
        return fileByCondition.filter(files, condition);
    }
}

public class Main {
    public static void main(String[] args) {
        
        FileManager fileManager = new FileManager();
        fileManager.addFile(new File("abc", 10, "2024", "2024"));
        fileManager.addFile(new File("def", 15, "2024", "2024"));
        fileManager.addFile(new File("efg", 5, "2024", "2024"));
        fileManager.addFile(new File("hni", 20, "2024", "2024"));
        
        List<File> filter1 = fileManager.getFileByName("abc");
        System.out.println("filter by name ==> " + filter1.toString());
        
        List<File> filter2 = fileManager.getFileBySize(10, 15);
        System.out.println("filter by size ==> " + filter2.toString());
    }
}
