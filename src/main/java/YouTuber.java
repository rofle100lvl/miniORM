import annotations.Table;
import annotations.relationshipType.Element;
@Table("yt")
public class YouTuber {

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
