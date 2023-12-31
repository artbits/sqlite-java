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

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class Reflect<T> {

    private final Map<String, Field> fieldMap = new LinkedHashMap<>();
    private T t;

    Reflect(Class<?> tClass) {
        newInstance(tClass);
    }


    Reflect(T t) {
        this.t = t;
        newInstance(t.getClass());
    }


    private void newInstance(Class<?> tClass) {
        Class<?> clazz = tClass;
        while (clazz != null){
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                fieldMap.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
    }


    void setValue(String fieldName, Object value) {
        try {
            Field field = fieldMap.getOrDefault(fieldName, null);
            if (field != null) {
                field.set(t, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    Object getValue(String fieldName) {
        try {
            Field field = fieldMap.getOrDefault(fieldName, null);
            return (field != null) ? field.get(t) : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    Class<?> getType(String fieldName) {
        Field field = fieldMap.getOrDefault(fieldName, null);
        return field.getType();
    }


    String getDatabaseType(String fieldName) {
        switch (getType(fieldName).getSimpleName().toLowerCase()) {
            case "int":
            case "integer":
            case "byte":
            case "short":
            case "long": return "integer";
            case "float":
            case "double": return "real";
            case "char":
            case "character":
            case "string": return "text";
            case "boolean" : return "blob";
            default: throw new NullPointerException();
        }
    }


    Object getDBValue(String fieldName) {
        try {
            Field field = fieldMap.getOrDefault(fieldName, null);
            if (field != null && field.get(t) != null) {
                switch (getDatabaseType(fieldName)) {
                    case "text": return String.format("'%s'", field.get(t));
                    case "blob": return (Objects.equals(field.get(t), true)) ? 1 : 0;
                    default: return field.get(t);
                }
            }
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    void getDBColumnsWithValue(BiConsumer<String, Object> consumer) {
        for (Field field : fieldMap.values()) {
            consumer.accept(field.getName(), getDBValue(field.getName()));
        }
    }


    void getDBColumnsWithType(BiConsumer<String, String> consumer) {
        for (Field field : fieldMap.values()) {
            consumer.accept(field.getName(), getDatabaseType(field.getName()));
        }
    }


    T get() {
        return t;
    }


    static <T> T toEntity(Class<T> tClass, Options options, ResultSet resultSet) {
        try {
            Map<String, Boolean> columnsMap = new HashMap<>();
            if (options != null && options.selectColumns != null && !Objects.equals(options.selectColumns, "*")) {
                String[] columns = options.selectColumns.split(", ");
                for (String column : columns) {
                    columnsMap.put(column, true);
                }
            }
            T t = tClass.getConstructor(Consumer.class).newInstance((Consumer<T>) (c -> {}));
            Reflect<T> reflect = new Reflect<>(t);
            for (Field field : reflect.fieldMap.values()) {
                String name = field.getName();
                if (!columnsMap.isEmpty() && !columnsMap.getOrDefault(name, false)) {
                    continue;
                }
                String type = field.getType().getSimpleName().toLowerCase();
                switch (type) {
                    case "int":
                    case "integer":
                        reflect.setValue(name, resultSet.getInt(name));
                        break;
                    case "byte":
                        reflect.setValue(name, resultSet.getByte(name));
                        break;
                    case "short":
                        reflect.setValue(name, resultSet.getShort(name));
                        break;
                    case "long":
                        reflect.setValue(name, resultSet.getLong(name));
                        break;
                    case "float":
                        reflect.setValue(name, resultSet.getFloat(name));
                        break;
                    case "double":
                        reflect.setValue(name, resultSet.getDouble(name));
                        break;
                    case "char":
                    case "character":
                    case "string":
                        reflect.setValue(name, resultSet.getString(name));
                        break;
                    case "boolean" :
                        reflect.setValue(name, resultSet.getBoolean(name));
                        break;
                }
            }
            return reflect.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}