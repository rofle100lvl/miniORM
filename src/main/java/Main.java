import exceptions.NoTableAnnotationException;
import org.postgresql.ds.PGSimpleDataSource;
import orm.ORM;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws NoTableAnnotationException, SQLException, FileNotFoundException, IllegalAccessException {
        User user = new User(123,
                "govno",
                new Car(60, "Bugatti"),
                new YouTuber("Vlad",
                        new Channel("Кобяков")));

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

        ORM<User> userORM = new ORM<>(dataSource, User.class);
        userORM.createTables();
    }
}
