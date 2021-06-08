import annotations.Id;
import annotations.Table;
import annotations.constraints.MaxLength;
import annotations.constraints.MinLength;
import annotations.relationshipType.OneToMany;
import annotations.relationshipType.Element;

import java.util.ArrayList;

@Table("users")
public class User {
    @Id
    int id;
    @Element
    int login;
    @OneToMany
    ArrayList<Car> cars;
    @Element
    @MaxLength(15)
    @MinLength(11)
    String password;
    @Element
    YouTuber youTuber;
    public User(int login, String password, ArrayList<Car> cars, YouTuber youTuber) {
        this.login = login;
        this.password = password;
        this.cars = cars;
        this.youTuber = youTuber;
    }
    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login=" + login +
                ", cars=" + cars +
                ", password='" + password + '\'' +
                ", youTuber=" + youTuber +
                '}';
    }
}
