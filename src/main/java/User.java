import annotations.Element;
import annotations.Id;
import annotations.Table;

@Table(name = "Users")
public class User {
    public User(int login, String password, Car car, YouTuber youTuber) {
        this.login = login;
        this.password = password;
        this.car = car;
        this.carr = car;
        this.youTuber = youTuber;
    }

    public User() {
    }

    @Id
    int id;
    @Element(name = "Login")
    int login;
    @Element(name = "")
    Car car;
    @Element(name = "")
    Car carr;
    @Element(name = "Password")
    String password;
    @Element(name = "youtuber")
    YouTuber youTuber;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login=" + login +
                ", car=" + car +
                ", carr=" + carr +
                ", password='" + password + '\'' +
                ", youTuber=" + youTuber +
                '}';
    }
}
