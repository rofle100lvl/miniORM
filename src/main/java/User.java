import annotations.Element;
import annotations.Table;

@Table(name = "Users")
public class User {
    public User(int login, String password, Car car, YouTuber youTuber) {
        this.login = login;
        this.password = password;
        this.car = car;
        this.youTuber = youTuber;
    }

    public User() {
    }

    @Element(name = "Login")
    int login;
    @Element(name = "")
    Car car;
    @Element(name = "Password")
    String password;
    @Element(name = "youtuber")
    YouTuber youTuber;
}
