import exceptions.NoTableAnnotationException;
import exceptions.NotPreparedException;
import org.postgresql.ds.PGSimpleDataSource;
import orm.ORM;
import orm.ORMInterface;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws NoTableAnnotationException, SQLException, FileNotFoundException, IllegalAccessException, NotPreparedException {

        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("src/main/resources/config.properties")) {
            properties.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(properties.getProperty("db_url"));
        dataSource.setUser(properties.getProperty("db_user"));
        dataSource.setPassword(properties.getProperty("db_password"));

        ArrayList<Car> cars = new ArrayList<>();
        cars.add(new Car(123, "I love HENTAI"));
        cars.add(new Car(133, "I love ANIME"));
        User user = new User(123789, "o111111111111", cars, new YouTuber("Vlad", new Channel("A4")));

        ORM<User> userORM = new ORM<>(dataSource, User.class);
        userORM.prepare();
        userORM.createTables();
        // userORM.save(user);
        userORM.getObjects().forEach(System.out::println);
        userORM.update(user);
    }
}
