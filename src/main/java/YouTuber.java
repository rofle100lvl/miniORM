import annotations.Element;
import annotations.Table;

@Table(name = "YouTubers")
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
}
