import annotations.*;

@Table(name = "Cars")
public class Car {
    public Car(int price, String name) {
        this.price = price;
        this.name = name;
    }

    public Car() {
    }

    @Id
    int id;

    int price;

    @Element(name = "name")
    String name;

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", price=" + price +
                ", name='" + name + '\'' +
                '}';
    }
}
