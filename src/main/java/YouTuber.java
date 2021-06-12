import annotations.Id;
import annotations.Table;
import annotations.relationshipType.Element;
@Table("yt")
public class YouTuber {
    @Id
    int id;
    @Element
    String name;
    @Element
    Channel ch;

    public YouTuber(String name, Channel ch) {
        this.name = name;
        this.ch = ch;
    }

    public YouTuber() {
    }

    @Override
    public String toString() {
        return "YouTuber{" +
                "name='" + name + '\'' +
                ", ch=" + ch +
                '}';
    }
}
