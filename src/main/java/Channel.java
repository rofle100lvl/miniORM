import annotations.Id;
import annotations.Table;
import annotations.relationshipType.Element;

@Table("ch")
public class Channel {
    @Id
    int id;
    @Element
    String name;

    public Channel(String name) {
        this.name = name;
    }

    public Channel() {
    }

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
