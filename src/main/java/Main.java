import exceptions.NoTableAnnotationException;
import exceptions.NotPreparedException;
import org.postgresql.ds.PGSimpleDataSource;
import orm.ShadowFiend;

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

//        ORM<User> userORM = new ORM<>(dataSource, User.class);
//        userORM.prepare();
        ArrayList<Car> cars = new ArrayList<>();
        cars.add(new Car(44, "I love HENTAI"));
        cars.add(new Car(13, "I love ANIME"));
        User user = new User(123789, "old", cars, new YouTuber("Vlad", new Channel("A4")));

        ShadowFiend<User> sf = new ShadowFiend<>(dataSource, User.class);
        sf.prepare();
        sf.createTables();

        sf.save(user);

        System.out.println(user.id);


        List<User> users = sf.getObjects();
        users.forEach(System.out::println);

    }
}
