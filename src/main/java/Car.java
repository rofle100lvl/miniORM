import annotations.*;

@Table(name = "Cars")
public class Car {
    public Car(int price, String name) {
        this.price = price;
        this.name = name;
    }

    int price;

    @Element(name = "name")
    String name;

}
