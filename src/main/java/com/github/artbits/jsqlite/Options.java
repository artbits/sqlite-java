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

import java.util.Arrays;

public final class Options {

    String selectColumns;
    String wherePredicate;
    String groupColumns;
    String orderColumns;
    Long limitSize;
    Long offsetSize;


    public final static String ASC = "asc";
    public final static String DESC = "desc";


    Options() { }


    public Options select(String... columns) {
        StringBuilder builder = new StringBuilder(Arrays.toString(columns));
        builder.deleteCharAt(0).deleteCharAt(builder.length() - 1);
        selectColumns = builder.toString();
        return this;
    }


    public Options where(String predicate) {
        wherePredicate = predicate.replace("&&", "and").replace("||", "or");
        return this;
    }


    public Options where(String predicate, Object... objects) {
        if (predicate != null) {
            predicate = predicate.replace("?", "%s").replace("&&", "and").replace("||", "or");
            wherePredicate = String.format(predicate, Arrays.stream(objects).map(o -> {
                if (o instanceof String || o instanceof Character) {
                    return String.format("'%s'", o);
                } else {
                    String s = String.valueOf(o);
                    switch (s) {
                        case "true": return "1";
                        case "false": return "0";
                        default: return s;
                    }
                }
            }).toArray());
        }
        return this;
    }


    public Options group(String columns) {
        groupColumns = columns;
        return this;
    }


    public Options order(String columns, String mode) {
        orderColumns = columns + " " + mode;
        return this;
    }


    public Options order(String columns) {
        orderColumns = columns;
        return this;
    }


    public Options limit(long size) {
        limitSize = size;
        return this;
    }


    public Options offset(long size) {
        offsetSize = size;
        return this;
    }

}
