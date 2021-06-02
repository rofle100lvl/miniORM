import annotations.*;

@Table(name = "Cars")
public class Car {
    public Car(int price, String name) {
        this.price = price;
        this.name = name;
    }

    public Car() {
    }

    int price;

    @Element(name = "name")
    String name;

}
