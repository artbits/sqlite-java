/**
 * Copyright 2023 Zhang Guanhu
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.artbits.jsqlite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

final class Core implements DB {

    private static Connection connection;


    Core(String path) {
        try {
            Path databasePath = Paths.get(path);
            Path parentPath = databasePath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void tables(Class<?>... classes) {
        HashMap<String, HashMap<String, String>> tablesMap = new HashMap<>();
        String s = SQLTemplate.query("sqlite_master", new Options().where("type = ?", "table"));
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(s)) {
            DatabaseMetaData metaData = connection.getMetaData();
            while (result.next()) {
                HashMap<String, String> tableColumnTypeMap = new HashMap<>();
                String tableName = result.getString("name");
                try (ResultSet set = metaData.getColumns(null, null, tableName, null)) {
                    while (set.next()) {
                        String column = set.getString("COLUMN_NAME");
                        String type = set.getString("TYPE_NAME").toLowerCase();
                        tableColumnTypeMap.put(column, type);
                    }
                }
                tablesMap.put(tableName, tableColumnTypeMap);
            }
            for (Class<?> tClass : classes) {
                String tableName = tClass.getSimpleName().toLowerCase();
                HashMap<String, String> tableColumnTypeMap = tablesMap.getOrDefault(tableName, null);
                if (tableColumnTypeMap == null) {
                    statement.executeUpdate(SQLTemplate.create(tClass));
                } else {
                    new Reflect<>(tClass).getDBColumnsWithType((column, type) -> {
                        if (tableColumnTypeMap.getOrDefault(column, null) == null) {
                            try {
                                statement.executeUpdate(SQLTemplate.addTableColumn(tableName, column, type));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void drop(Class<?>... classes) {
        try (Statement statement = connection.createStatement()) {
            for (Class<?> tClass : classes) {
                statement.executeUpdate(SQLTemplate.drop(tClass));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> void insert(T t) {
        synchronized (this) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(SQLTemplate.insert(t));
                try (ResultSet result = statement.executeQuery("select last_insert_rowid()")) {
                    if (result.next()) {
                        new Reflect<>(t).setValue("id", result.getLong(1));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public <T> void update(T t, String predicate, Object... args) {
        synchronized (this) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(SQLTemplate.update(t, new Options().where(predicate, args)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public <T> void update(T t) {
        Object id = new Reflect<T>(t).getValue("id");
        update(t, "id = ?", id);
    }


    @Override
    public void delete(Class<?> tClass, String predicate, Object... args) {
        synchronized (this) {
            String sql = SQLTemplate.delete(tClass, new Options().where(predicate, args));
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void delete(Class<?> tClass, List<Long> ids) {
        StringBuilder builder = new StringBuilder(String.valueOf(ids));
        builder.deleteCharAt(0).deleteCharAt(builder.length() - 1);
        delete(tClass, "id in(?)", builder);
    }


    @Override
    public void delete(Class<?> tClass, Long... ids) {
        delete(tClass, Arrays.asList(ids));
    }


    @Override
    public void deleteAll(Class<?> tClass) {
        delete(tClass, null, (Object) null);
    }


    @Override
    public <T> List<T> find(Class<T> tClass, Consumer<Options> consumer) {
        Options options = (consumer != null) ? new Options() : null;
        Optional.ofNullable(consumer).ifPresent(c -> c.accept(options));
        String sql = SQLTemplate.query(tClass, options);
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                T t = Reflect.toEntity(tClass, options, resultSet);
                Optional.ofNullable(t).ifPresent(list::add);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> List<T> find(Class<T> tClass, List<Long> ids) {
        StringBuilder builder = new StringBuilder(String.valueOf(ids));
        builder.deleteCharAt(0).deleteCharAt(builder.length() - 1);
        return find(tClass, options -> options.where("id in(?)", builder));
    }


    @Override
    public <T> List<T> find(Class<T> tClass, Long... ids) {
        return find(tClass, Arrays.asList(ids));
    }


    @Override
    public <T> List<T> findAll(Class<T> tClass) {
        return find(tClass, (Consumer<Options>) null);
    }


    @Override
    public <T> T findOne(Class<T> tClass, String predicate, Object... args) {
        List<T> list = find(tClass, options -> options.where(predicate, args));
        return (!list.isEmpty()) ? list.get(0) : null;
    }


    @Override
    public <T> T findOne(Class<T> tClass, Long id) {
        return findOne(tClass, "id = ?", id);
    }

}
