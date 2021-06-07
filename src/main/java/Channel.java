import annotations.*;

import java.util.ArrayList;

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
