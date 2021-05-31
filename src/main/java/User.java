import annotations.Element;
import annotations.Table;

@Table(name = "Users")
public class User {
    public User(int login, String password, Car car) {
        this.login = login;
        this.password = password;
        this.car = car;
    }

    @Element(name = "Login")
    int login;
    @Element(name = "")
    Car car;
    @Element(name = "Password")
    String password;
}
