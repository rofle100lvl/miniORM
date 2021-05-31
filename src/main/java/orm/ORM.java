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

    public ORM(DataSource dataSource, Class<T> clazz)
            throws NoTableAnnotationException {
        this.clazz = clazz;
        this.dataSource = dataSource;

        if (Objects.isNull(clazz.getAnnotation(Table.class))) throw new NoTableAnnotationException();
    }

    public void createTables() throws NoTableAnnotationException, SQLException {
        runCommand(getCreateRequestToDB());
    }

    private void runCommand(String string) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(string);
        }
    }

    private String getCreateRequestToDB() throws NoTableAnnotationException, SQLException {
        StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + clazz.getAnnotation(Table.class).name() + "(\n");

        // Add id column
        stringBuilder.append("\tid SERIAL PRIMARY KEY,\n");

        for (Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);

            Element elementAnnotation = field.getAnnotation(Element.class);
            NotNull notNullAnnotation = field.getAnnotation(NotNull.class);
            Unique uniqueAnnotation = field.getAnnotation(Unique.class);

            if (Objects.nonNull(elementAnnotation)) {
                if (typeConverter.containsKey(field.getType())) {
                    stringBuilder
                            .append("\t")
                            .append(elementAnnotation.name().equals("") ? field.getName() : elementAnnotation.name())
                            .append(" ")
                            .append(typeConverter.get(field.getType()))
                            .append(Objects.nonNull(notNullAnnotation) ? " NOT NULL" : "")
                            .append(Objects.nonNull(uniqueAnnotation) ? " UNIQUE" : "")
                            .append(",\n");
                } else {
                    ORM<?> orm = getORMForClass(field.getType());
                    orm.createTables();
                    stringBuilder.append("\t").append(orm.getTableName()).append("id").append(" INT,\n");
                }
            }
            field.setAccessible(false);
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length() - 1, ");");
        return stringBuilder.toString();
    }

    private String getInsertRequestToDB(T object) {
        return object.toString();
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
