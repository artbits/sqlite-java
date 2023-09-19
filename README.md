[![](https://jitpack.io/v/artbits/sqlite-java.svg)](https://jitpack.io/#artbits/sqlite-java)
[![](https://img.shields.io/badge/JDK-8%20%2B-%23DD964D)](https://jdk.java.net/)
[![](https://img.shields.io/badge/license-Apache--2.0-%234377BF)](#license)


## SQLite-Java
``SQLite-Java`` is a Java ORM for SQLite databases. Using ``SQLite-JDBC`` as the driver at the bottom. It provides simple and efficient APIs without writing a large number of SQL statements. You only need to know the basics of SQL to get started.


## Features
 + Support for automatic table creation and addition columns.
 + Provide APIs for adding, deleting, modifying, and querying.
 + APIs are simple, elegant and efficient to use.


## Download
Gradle:
```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.artbits:sqlite-java:1.0.0'
}
```
Maven:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://www.jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.artbits</groupId>
    <artifactId>sqlite-java</artifactId>
    <version>1.0.0</version>
</dependency>
```


## Usage
Let Java classes be mapped into database tables.
```java
public class User {
    public Long id;
    public String name;
    public Integer age;
    public Boolean vip;

    public User(Consumer<User> consumer) {
        consumer.accept(this);
    }
}

public class Book {
    public Long id;
    public String name;
    public String author;
    public Double price;

    public Book(Consumer<Book> consumer) {
        consumer.accept(this);
    }
}
```

Connect to the database and load tables (automatically add tables and columns).
```java
DB db = DB.connect("database/example.db");
db.tables(User.class, Book.class);
```

Insert data.
```java
// No need to set ID, ID will increase automatically when inserting data.
User user = new User(u -> {u.name = "Lake"; u.age = 25; u.vip = true;});
db.insert(user);
System.out.printf("userId = %d", user.id);
```

Update data.
```java
// Update data by id.
db.update(new User(u -> {
    u.id = 11L;
    u.vip = false;
}));

// Update data by condition.
db.update(new User(u -> u.vip = true), "age < ?", 50);
```

Delete data.
```java
// Delete all data in this table.
db.deleteAll(User.class);

// Delete data by IDs.
db.delete(User.class, 1L, 2L, 3L);

// Delete data by ID list.
db.delete(User.class, Arrays.asList(1L, 2L, 3L));

// Delete data by condition.
db.delete(User.class, "name = ?", "Lake");
```

Query data.
```java
// Find one by ID.
User user1 = db.findOne(User.class, 1L);

// Find one by condition.
User user2 = db.findOne(User.class, "name = ?", "Lake");

// Find all.
List<User> users1 = db.findAll(User.class);

// Find many by IDs.
List<User> users2 = db.find(User.class, 1L, 2L, 3L);

// Find many by ID list.
List<User> users3 = db.find(User.class, Arrays.asList(1L, 2L, 3L));

// Find many by custom option rules. Options APIs are optional, choose according to actual needs.
List<User> users4 = db.find(User.class, options -> options
        .select("name", "age")
        .where("age <= ?", 50)
        .order("age", Options.DESC)
        .limit(5)
        .offset(1));
```



## Links
+ Thanks: 
    + [SQLite-JDBC](https://github.com/xerial/sqlite-jdbc)
    + [QuickIO](https://github.com/artbits/quickio)


## License
```
Copyright 2023 Zhang Guanhu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.