import annotations.Element;
import annotations.Id;
import annotations.Table;
import annotations.ToManyElement;

import java.util.ArrayList;

@Table(name = "Users")
public class User {
    public User(int login, String password, ArrayList<Car> cars, YouTuber youTuber) {
        this.login = login;
        this.password = password;
        this.cars = cars;
        this.youTuber = youTuber;
    }

    public User() {
    }

    @Id
    int id;
    @Element(name = "Login")
    int login;
    @ToManyElement(name = "")
    ArrayList<Car> cars;
    @Element(name = "Password")
    String password;
    @Element(name = "youtuber")
    YouTuber youTuber;

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
