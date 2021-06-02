import annotations.Element;
import annotations.NotNull;
import annotations.Table;

@Table(name = "Channels")
public class Channel {
    @Element
    String name;

    public Channel() {
    }

    public Channel(String name) {
        this.name = name;
    }
}
