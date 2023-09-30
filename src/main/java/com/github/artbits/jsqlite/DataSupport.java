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

import java.util.Optional;
import java.util.function.Consumer;

public class DataSupport<T> {

    Long id;
    Long createdAt;
    Long updatedAt;


    public DataSupport(Consumer<T> consumer) {
        Optional.of(consumer).ifPresent(c -> c.accept((T) this));
    }


    public final T set(Consumer<T> consumer) {
        consumer.accept((T) this);
        return (T) this;
    }


    public final long id() {
        return (id != null) ? id : 0L;
    }


    public final long createdAt() {
        return (createdAt != null) ? createdAt : 0L;
    }


    public final long updatedAt() {
        return (updatedAt != null) ? updatedAt : 0L;
    }


    public final String toJson() {
        return new JsonObject(this).toString();
    }


    public final void printJson() {
        System.out.println(toJson());
    }

}
