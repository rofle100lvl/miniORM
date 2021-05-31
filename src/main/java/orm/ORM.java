package orm;

import annotations.Element;
import annotations.NotNull;
import annotations.Table;
import annotations.Unique;
import exceptions.ConvertInstructionException;
import exceptions.NoTableAnnotationException;
import tools.CheckedFunction;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ORM<T> {
    private final DataSource dataSource;
    private final Class<T> clazz;

    private static final ConcurrentHashMap<Class<?>, String> typeConverter = new ConcurrentHashMap<>();
    private static final Map<Class<?>, CheckedFunction<String, Object, ConvertInstructionException>> toObjectConvertInstructions = new ConcurrentHashMap<>();
    private final Map<Class<?>, ORM<?>> ormClassesForReuse = new HashMap<>();

    static {
        addInstructionsForType(Integer.class, getWrappedParseNumFunction(Integer::parseInt), "INT");
        addInstructionsForType(Long.class, getWrappedParseNumFunction(Long::parseLong), "BIGINT");
        addInstructionsForType(Boolean.class, getWrappedParseNumFunction(Boolean::parseBoolean), "BOOLEAN");
        addInstructionsForType(Byte.class, getWrappedParseNumFunction(Byte::parseByte), "SMALLINT");
        addInstructionsForType(Float.class, getWrappedParseNumFunction(Float::parseFloat), "REAL");
        addInstructionsForType(Double.class, getWrappedParseNumFunction(Double::parseDouble), "DOUBLE");
        addInstructionsForType(Short.class, getWrappedParseNumFunction(Short::parseShort), "SMALLINT");
        addInstructionsForType(String.class, string -> string, "TEXT");
        addInstructionsForType(Character.class, string -> string.charAt(0), "CHARACTER [1]");

        addInstructionsForType(int.class, getWrappedParseNumFunction(Integer::parseInt), "INT");
        addInstructionsForType(long.class, getWrappedParseNumFunction(Long::parseLong), "BIGINT");
        addInstructionsForType(boolean.class, getWrappedParseNumFunction(Boolean::parseBoolean), "BOOLEAN");
        addInstructionsForType(byte.class, getWrappedParseNumFunction(Byte::parseByte), "SMALLINT");
        addInstructionsForType(float.class, getWrappedParseNumFunction(Float::parseFloat), "REAL");
        addInstructionsForType(double.class, getWrappedParseNumFunction(Double::parseDouble), "DOUBLE");
        addInstructionsForType(short.class, getWrappedParseNumFunction(Short::parseShort), "SMALLINT");
        addInstructionsForType(char.class, string -> string.charAt(0), "CHARACTER [1]");
    }

    private String createRequestToDB;
    private String insertRequestToDb;

    public ORM(DataSource dataSource, Class<T> clazz)
            throws NoTableAnnotationException {
        this.clazz = clazz;
        this.dataSource = dataSource;

        if (Objects.isNull(clazz.getAnnotation(Table.class))) throw new NoTableAnnotationException();
        createRequestsToDB();
    }

    public void insertObject(T object){
        
    }

    public void createTables() throws SQLException {
        runCommand(createRequestToDB);
        for (ORM<?> orm:
                ormClassesForReuse.values()) {
            orm.createTables();
        }
    }

    private void runCommand(String string) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(string);
            System.out.println(string);
        }
    }


    private void createRequestsToDB() throws NoTableAnnotationException {
        StringBuilder createRequest = new StringBuilder("CREATE TABLE IF NOT EXISTS " + clazz.getAnnotation(Table.class).name() + "(\n");
        StringBuilder insertRequest = new StringBuilder("INSERT INTO " + getTableName() +" (");

        // Add id column
        createRequest.append("\tid SERIAL PRIMARY KEY,\n");
        int k = 0;
        for (Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Element elementAnnotation = field.getAnnotation(Element.class);
            NotNull notNullAnnotation = field.getAnnotation(NotNull.class);
            Unique uniqueAnnotation = field.getAnnotation(Unique.class);
            if (Objects.nonNull(elementAnnotation)) {
                k++;
                if (typeConverter.containsKey(field.getType())) {
                    createRequest
                            .append("\t")
                            .append(elementAnnotation.name().equals("") ? field.getName() : elementAnnotation.name())
                            .append(" ")
                            .append(typeConverter.get(field.getType()))
                            .append(Objects.nonNull(notNullAnnotation) ? " NOT NULL" : "")
                            .append(Objects.nonNull(uniqueAnnotation) ? " UNIQUE" : "")
                            .append(",\n");
                    insertRequest
                            .append(field.getName())
                            .append(", ");
                } else {
                    ORM<?> orm = getORMForClass(field.getType());
                    createRequest.append("\t").append(orm.getTableName()).append("id").append(" INT,\n");
                    insertRequest
                            .append(orm.getTableName())
                            .append("id")
                            .append(", ");
                }
            }
            field.setAccessible(false);
        }
        insertRequest.replace(insertRequest.length() - 2, insertRequest.length() - 1, ")");
        if (k != 0){
           insertRequest.append("(");
           while(k != 0){
               insertRequest.append("?, ");
               k--;
           }
        }
        createRequest.replace(createRequest.length() - 2, createRequest.length() - 1, ");");
        insertRequest.replace(insertRequest.length() - 2, insertRequest.length() - 1, ");");


        createRequestToDB = createRequest.toString();
        insertRequestToDb = insertRequest.toString();
    }


    private String getTableName() {
        return clazz.getAnnotation(Table.class).name();
    }

    private <S> ORM<S> getORMForClass(Class<S> clazz) throws NoTableAnnotationException {
        if (ormClassesForReuse.containsKey(clazz)) return (ORM<S>) ormClassesForReuse.get(clazz);
        ORM<S> orm = new ORM<>(dataSource, clazz);
        ormClassesForReuse.put(clazz, orm);
        return orm;
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

    private static void addInstructionsForType(Class<?> clazz,
                                          CheckedFunction<String, Object, ConvertInstructionException> function,
                                          String dbType) {
        toObjectConvertInstructions.put(clazz, function);
        typeConverter.put(clazz, dbType);
    }
}
