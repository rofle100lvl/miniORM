import exceptions.NoTableAnnotationException;
import exceptions.NotPreparedException;
import org.postgresql.ds.PGSimpleDataSource;
import orm.ORM;
import orm.ORMInterface;
import orm.ShadowFiend;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws NoTableAnnotationException, SQLException, FileNotFoundException, IllegalAccessException, NotPreparedException {
        User user = new User(123,
                "gofgdvno",
                new Car(60, "yergd"),
                new YouTuber("Vfdlad",
                        new Channel("Кобяgfdgdefков")));

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
        ShadowFiend<User> sf = new ShadowFiend<>(dataSource, User.class);
        sf.prepare();
        sf.createTables();

        for (int i = 0; i++ < 10;) sf.save(user);

        System.out.println(user.id);
        sf.remove(user);


        List<User> users = sf.getObjects();
        users.forEach(System.out::println);

    }
}
