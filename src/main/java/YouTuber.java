import annotations.Element;
import annotations.Id;
import annotations.Table;

@Table(name = "YouTubers")
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
                "id=" + id +
                ", name='" + name + '\'' +
                ", ch=" + ch +
                '}';
    }
}
