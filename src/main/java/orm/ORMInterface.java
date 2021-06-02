package orm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface ORMInterface<T> {
    void save(T object) throws SQLException;
    List<T> getObjects() throws SQLException;
    List<T> getObjects(String ... fetch) throws SQLException;
    void remove(T object) throws SQLException;
}
