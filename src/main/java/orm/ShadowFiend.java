package orm;

import annotations.*;
import exceptions.ConvertInstructionException;
import exceptions.NotPreparedException;
import tools.CheckedFunction;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShadowFiend<T> implements ORMInterface<T> {
    private final DataSource dataSource;
    private final Class<? super T> clazz;

    private static final ConcurrentHashMap<Class<?>, String> typeConverter = new ConcurrentHashMap<>();
    private static final Map<Class<?>, CheckedFunction<String, Object, ConvertInstructionException>> toObjectConvertInstructions = new HashMap<>();
    private final Map<Class<?>, ShadowFiend<?>> ormInstancesForClasses = new HashMap<>();

    // Flags
    private boolean iSPrepared = false;

    // Strings with requests to db
    private String createTableRequestToDB;
    private String insertRequestToDb;

    public ShadowFiend(DataSource dataSource, Class<? super T> clazz) {
        this.dataSource = dataSource;
        this.clazz = clazz;
    }

    @Override
    public void save(T object) throws SQLException {
        try {
            insert(object, null);
        } catch (IllegalAccessException ignored) { }
    }

    @Override
    public List<T> getObjects() throws SQLException {
        try {
            return getSelectToTableRequest().stream().map(o -> (T) o).collect(Collectors.toList());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ConvertInstructionException e) {
            e.printStackTrace();
        } return null;
    }

    @Override
    public List<T> getObjects(String... fetch) throws SQLException {
        try {
            return getObjWithFetch(fetch).stream().map(o -> (T) o).collect(Collectors.toList());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ConvertInstructionException e) {
            e.printStackTrace();
        } return null;
    }

    @Override
    public void remove(T object) throws SQLException {
        try {
            deleteFromDB(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void prepare() {
        getCreateTableRequest(null);
        getInsertToTableRequest(null);
    }

    public void createTables() throws NotPreparedException, SQLException {
        if (!iSPrepared) throw new NotPreparedException();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableRequestToDB);
        }
        for (ShadowFiend<?> sf:
             ormInstancesForClasses.values()) {
            sf.createTables();
        }
    }

    // Private methods

    private void deleteFromDB(Object o) throws SQLException, IllegalAccessException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (Field field:
                 clazz.getDeclaredFields()) {
                if (Objects.nonNull(field.getAnnotation(Id.class))) {
                    field.setAccessible(true);
                    statement.execute("DELETE FROM " + getTableName() + " WHERE id = " + field.get(o));
                    field.setAccessible(false);
                }
            }
        }
    }

    private ArrayList<Object> getSelectToTableRequest() throws SQLException, InstantiationException, IllegalAccessException, ConvertInstructionException {
        ArrayList<Object> objects = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT * FROM " + getTableName());
            ResultSet resultSet = statement.getResultSet();

            while (resultSet.next()) {

                Object object = clazz.newInstance();

                for (Field field :
                        clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (Objects.nonNull(field.getAnnotation(Element.class))) {
                        if (typeConverter.containsKey(field.getType())) {
                            String value = resultSet.getString(field.getAnnotation(Element.class).name());
                            field.set(object, toObjectConvertInstructions.get(field.getType()).apply(value));
                        } else {
                            ShadowFiend<?> orm = getORMForClass(field.getType());
                            String id = resultSet.getString(Constants.id);
                            field.set(object, orm.getObjWithFetch(getTableName() + "_" + Constants.id + " = " + id).get(0));
                        }
                    }
                    if (Objects.nonNull(field.getAnnotation(Id.class))) {
                        field.set(object, resultSet.getInt(Constants.id));
                    }
                }
                objects.add(object);
            }
        }
        return objects;
    }

    private ArrayList<Object> getObjWithFetch(String... fetch) throws SQLException, InstantiationException, IllegalAccessException, ConvertInstructionException {
        ArrayList<Object> objects = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + getTableName() + " WHERE " + String.join(" AND ", fetch));

            while (resultSet.next()) {

                Object object = clazz.newInstance();

                for (Field field :
                        clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    Element elementAnnotation = field.getAnnotation(Element.class);
                    if (Objects.nonNull(field.getAnnotation(Element.class))) {
                        if (typeConverter.containsKey(field.getType())) {
                            String value = resultSet.getString(elementAnnotation.name().equals("") ? field.getName() : elementAnnotation.name());
                            field.set(object, toObjectConvertInstructions.get(field.getType()).apply(value));
                        } else {
                            ShadowFiend<?> orm = getORMForClass(field.getType());
                            String id = resultSet.getString(Constants.id);
                            field.set(object, orm.getObjWithFetch(getTableName() + "_" + Constants.id + " = " + id).get(0));
                        }
                    }
                }
                objects.add(object);
            }
        }
        return objects;
    }

    private void insert(Object object, Integer identifier) throws SQLException, IllegalAccessException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertRequestToDb)) {
            int index = 1;
            Field idField = null;
            if (Objects.nonNull(identifier)) {
                preparedStatement.setInt(index++, identifier);
            }
            LinkedList<Object> nonPrimaryElements = new LinkedList<>();
            for (Field field:
                 clazz.getDeclaredFields()) {
                field.setAccessible(true);

                Element elementAnnotation = field.getAnnotation(Element.class);
                Id idAnnotation = field.getAnnotation(Id.class);

                if (Objects.nonNull(elementAnnotation)) {
                    if (typeConverter.containsKey(field.getType())) {
                        preparedStatement.setObject(index++, field.get(object));
                    }
                    else {
                        nonPrimaryElements.add(field.get(object));
                    }
                }
                if (Objects.nonNull(idAnnotation)) idField = field;
                field.setAccessible(false);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            if (Objects.nonNull(idField)) {
                idField.setAccessible(true);
                idField.set(object, resultSet.getInt(Constants.id));
                idField.setAccessible(false);
            }

            for (Object element:
                 nonPrimaryElements) {
                ormInstancesForClasses.get(element.getClass()).insert(element, resultSet.getInt(Constants.id));
            }
        }
    }

    private void getInsertToTableRequest(String ownerTable) {
        StringBuilder insertRequest = new StringBuilder("INSERT INTO " + getTableName() +" (");

        if(Objects.nonNull(ownerTable)){
            insertRequest
                    .append(ownerTable +"_id")
                    .append(", ");
        }
        int countOfColumns = Objects.nonNull(ownerTable) ?  1 : 0;
        for (Field field:
             clazz.getDeclaredFields()) {
            Element elementAnnotation = field.getAnnotation(Element.class);
            if (Objects.nonNull(elementAnnotation)) {
                if (typeConverter.containsKey(field.getType())) {
                    countOfColumns++;
                    insertRequest
                            .append(field.getName())
                            .append(", ");
                } else {
                    ShadowFiend<?> orm = getORMForClass(field.getType());
                    orm.getInsertToTableRequest(getTableName());
                }
            }
        }
        insertRequest.replace(insertRequest.length() - 2, insertRequest.length() - 1, ")")
                .append(" VALUES ");

        if (countOfColumns != 0) {
            insertRequest.append("(");
            while(countOfColumns-- != 0) {
                insertRequest.append("?, ");
            }
        }

        insertRequest.replace(insertRequest.length() - 2, insertRequest.length() - 1, ") RETURNING id;");
        insertRequestToDb = insertRequest.toString();
        iSPrepared = true;
    }

    private void getCreateTableRequest(String ownerTableName) {
        StringBuilder createRequest = new StringBuilder("CREATE TABLE IF NOT EXISTS " + clazz.getAnnotation(Table.class).name() + "(\n");

        // Add id column
        createRequest.append("\tid SERIAL PRIMARY KEY,\n");

        // Add other columns
        for (Field field:
                clazz.getDeclaredFields()) {
            Element elementAnnotation = field.getAnnotation(Element.class);
            NotNull notNullAnnotation = field.getAnnotation(NotNull.class);
            Unique uniqueAnnotation = field.getAnnotation(Unique.class);
            if (Objects.nonNull(elementAnnotation)) {
                if (typeConverter.containsKey(field.getType())) {
                    createRequest
                            .append("\t")
                            .append(elementAnnotation.name().equals("") ? field.getName() : elementAnnotation.name())
                            .append(" ")
                            .append(typeConverter.get(field.getType()))
                            .append(Objects.nonNull(notNullAnnotation) ? " NOT NULL" : "")
                            .append(Objects.nonNull(uniqueAnnotation) ? " UNIQUE" : "")
                            .append(",\n");
                } else {
                    ShadowFiend<?> orm = getORMForClass(field.getType());
                    orm.getCreateTableRequest(getTableName());
                }
            }
        }
        if (Objects.nonNull(ownerTableName)) {
            createRequest
                    .append("\t")
                    .append(ownerTableName + "_id")
                    .append(" INT REFERENCES " )
                    .append(ownerTableName)
                    .append(" (id) ")
                    .append("ON DELETE CASCADE  ");
        }

        createRequest.replace(createRequest.length() - 2, createRequest.length() - 1, ");");

        createTableRequestToDB = createRequest.toString();
        iSPrepared = true;
    }

    private String getTableName() {
        return clazz.getAnnotation(Table.class).name();
    }

    private <S> ShadowFiend<S> getORMForClass(Class<S> clazz) {
        if (ormInstancesForClasses.containsKey(clazz))
            return (ShadowFiend<S>) ormInstancesForClasses.get(clazz);
        ShadowFiend<S> orm = new ShadowFiend<S>(dataSource, clazz);
        ormInstancesForClasses.put(clazz, orm);
        return orm;
    }

    private static void addInstructionsForType(Class<?> clazz,
                                               CheckedFunction<String, Object, ConvertInstructionException> function,
                                               String dbType) {
        toObjectConvertInstructions.put(clazz, function);
        typeConverter.put(clazz, dbType);
    }

    private static CheckedFunction<String, Object, ConvertInstructionException> getWrappedParseNumFunction(Function<String, Object> parseFunction) {
        return string -> {
            try {
                return parseFunction.apply(string);
            } catch (NumberFormatException e) {
                throw new ConvertInstructionException();
            }
        };
    }

    static {
        addInstructionsForType(Integer.class, getWrappedParseNumFunction(Integer::parseInt), Constants.intDB);
        addInstructionsForType(Long.class, getWrappedParseNumFunction(Long::parseLong), Constants.bigIntDB);
        addInstructionsForType(Boolean.class, getWrappedParseNumFunction(Boolean::parseBoolean), Constants.booleanDB);
        addInstructionsForType(Byte.class, getWrappedParseNumFunction(Byte::parseByte), Constants.smallIntDB);
        addInstructionsForType(Float.class, getWrappedParseNumFunction(Float::parseFloat), Constants.realDB);
        addInstructionsForType(Double.class, getWrappedParseNumFunction(Double::parseDouble), Constants.doubleDB);
        addInstructionsForType(Short.class, getWrappedParseNumFunction(Short::parseShort), Constants.smallIntDB);
        addInstructionsForType(String.class, string -> string, Constants.textDB);
        addInstructionsForType(Character.class, string -> string.charAt(0), Constants.charDB);

        addInstructionsForType(int.class, getWrappedParseNumFunction(Integer::parseInt), Constants.intDB);
        addInstructionsForType(long.class, getWrappedParseNumFunction(Long::parseLong), Constants.bigIntDB);
        addInstructionsForType(boolean.class, getWrappedParseNumFunction(Boolean::parseBoolean), Constants.booleanDB);
        addInstructionsForType(byte.class, getWrappedParseNumFunction(Byte::parseByte), Constants.smallIntDB);
        addInstructionsForType(float.class, getWrappedParseNumFunction(Float::parseFloat), Constants.realDB);
        addInstructionsForType(double.class, getWrappedParseNumFunction(Double::parseDouble), Constants.doubleDB);
        addInstructionsForType(short.class, getWrappedParseNumFunction(Short::parseShort), Constants.smallIntDB);
        addInstructionsForType(char.class, string -> string.charAt(0), Constants.charDB);
    }

    private static final class Constants {
        static String intDB = "INT";
        static String bigIntDB = "BIGINT";
        static String booleanDB = "BOOLEAN";
        static String smallIntDB = "SMALLINT";
        static String realDB = "REAL";
        static String doubleDB = "DOUBLE";
        static String textDB = "TEXT";
        static String charDB = "CHARACTER [1]";

        static String id = "id";
    }
}
