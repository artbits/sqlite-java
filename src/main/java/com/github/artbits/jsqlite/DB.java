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

import java.util.List;
import java.util.function.Consumer;

public interface DB extends AutoCloseable {
    @Override
    void close();
    void tables(Class<?>... classes);
    void drop(Class<?>... classes);
    <T> void insert(T t);
    <T> void update(T t, String predicate, Object... args);
    <T> void update(T t);
    void delete(Class<?> tClass, String predicate, Object... args);
    void delete(Class<?> tClass, List<Long> ids);
    void delete(Class<?> tClass, Long... ids);
    void deleteAll(Class<?> tClass);
    <T> List<T> find(Class<T> tClass, Consumer<Options> consumer);
    <T> List<T> find(Class<T> tClass, List<Long> ids);
    <T> List<T> find(Class<T> tClass, Long... ids);
    <T> List<T> findAll(Class<T> tClass);
    <T> T findOne(Class<T> tClass, String predicate, Object... args);
    <T> T findOne(Class<T> tClass, Long id);

    static DB connect(String path) {
        return new Core(path);
    }
}
