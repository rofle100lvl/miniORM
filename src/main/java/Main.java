import exceptions.NoTableAnnotationException;
import org.postgresql.ds.PGSimpleDataSource;
import orm.ORM;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws NoTableAnnotationException, SQLException {
//        User user = new User(123, "govno", new Car(60, "Bugatti"));

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/db");
        dataSource.setUser("postgres");
        dataSource.setPassword("rak830");

        ORM<User> userORM = new ORM<>(dataSource, User.class);
        userORM.createTables();
        Iterator<Integer> iterator = Arrays.asList(1, 2, 3).iterator();
    }
}
