import annotations.Element;
import annotations.Id;
import annotations.NotNull;
import annotations.Table;

@Table(name = "Channels")
public class Channel {
    @Element
    String name;

    public Channel() {
    }

    @Id
    int id;

    public Channel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
