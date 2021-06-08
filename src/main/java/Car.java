import annotations.Id;
import annotations.Table;
import annotations.constraints.GreaterThan;
import annotations.relationshipType.Element;

@Table("cars")
public class Car {
    @Id
    int id;
    @Element
    @GreaterThan(55)
    int price;
    @Element
    String name;
    public Car(int price, String name) {
        this.price = price;
        this.name = name;
    }
    public Car() {
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", price=" + price +
                ", name='" + name + '\'' +
                '}';
    }
}
