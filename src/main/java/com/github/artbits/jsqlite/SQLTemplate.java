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

import java.util.Objects;
import java.util.Optional;

final class SQLTemplate {

    static <T> String create(Class<T> tClass) {
        StringBuffer columnsString = new StringBuffer("id integer primary key,");
        new Reflect<>(tClass).getDBColumnsWithType((column, type) -> {
            if (!Objects.equals(column, "id")) {
                columnsString.append(column).append(" ").append(type).append(",");
            }
        });
        columnsString.deleteCharAt(columnsString.length() - 1);
        String tableName = tClass.getSimpleName().toLowerCase();
        return $("create table %s (%s);", tableName, columnsString);
    }


    static String addTableColumn(String tableName, String column, String type) {
        return $("alter table %s add column %s %s;", tableName, column, type);
    }


    static <T> String drop(Class<T> tClass) {
        return $("drop table %s;", tClass.getSimpleName().toLowerCase());
    }


    static <T> String insert(T t) {
        StringBuffer columnsString = new StringBuffer();
        StringBuffer valueString = new StringBuffer();
        new Reflect<>(t).getDBColumnsWithValue((column, value) -> {
            if (!Objects.equals(column, "id")) {
                columnsString.append(column).append(",");
                valueString.append(value).append(",");
            }
        });
        columnsString.deleteCharAt(columnsString.length() - 1);
        valueString.deleteCharAt(valueString.length() - 1);
        String tableName = t.getClass().getSimpleName().toLowerCase();
        return $("insert into %s (%s) values (%s);", tableName, columnsString, valueString);
    }


    static <T> String update(T t, Options options) {
        String tableName = t.getClass().getSimpleName().toLowerCase();
        String whereString = (options.wherePredicate != null) ? $("where %s ", options.wherePredicate) : "";
        StringBuffer setString = new StringBuffer();
        new Reflect<>(t).getDBColumnsWithValue((column, value) -> {
            if (value != null && !Objects.equals(column, "id")) {
                setString.append(column).append(" = ").append(value).append(",");
            }
        });
        setString.deleteCharAt(setString.length() - 1);
        StringBuilder SQLBuilder = new StringBuilder();
        return SQLBuilder
                .append($("update %s set %s ", tableName, setString))
                .append(whereString)
                .append(";")
                .deleteCharAt(SQLBuilder.length() - 2)
                .toString();
    }


    static <T> String delete(Class<T> tClass, Options options) {
        String deleteString = $("delete from %s ", tClass.getSimpleName().toLowerCase());
        String whereString = (options.wherePredicate != null) ? $("where %s ", options.wherePredicate) : "";
        StringBuilder SQLBuilder = new StringBuilder();
        return SQLBuilder
                .append(deleteString)
                .append(whereString)
                .append(";")
                .deleteCharAt(SQLBuilder.length() - 2)
                .toString();
    }


    static <T> String query(String table, Options options) {
        if (options == null) {
            return $("select * from %s;", table);
        }
        String fromString = $("from %s ", table);
        String selectString = $("select %s ", Optional.ofNullable(options.selectColumns).orElse("*"));
        String whereString = (options.wherePredicate != null) ? $("where %s ", options.wherePredicate) : "";
        String groupString = (options.groupColumns != null) ? $("group by %s ", options.groupColumns) : "";
        String orderString = (options.orderColumns != null) ? $("order by %s ", options.orderColumns) : "";
        String limitString = (options.limitSize != null) ? $("limit %d ", options.limitSize) : "";
        String offsetString = (options.offsetSize != null) ? $("offset %d ", options.offsetSize) : "";
        StringBuilder SQLBuilder = new StringBuilder();
        return SQLBuilder
                .append(selectString)
                .append(fromString)
                .append(whereString)
                .append(groupString)
                .append(orderString)
                .append(limitString)
                .append(offsetString)
                .append(";")
                .deleteCharAt(SQLBuilder.length() - 2)
                .toString();
    }


    static <T> String query(Class<T> tClass, Options options) {
        return query(tClass.getSimpleName().toLowerCase(), options);
    }


    private static String $(String format, Object... objects) {
        return String.format(format, objects);
    }

}
