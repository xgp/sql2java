# sql2java #

This is a fork of an abandoned project on SourceForge by the same name <http://sql2java.sourceforge.net/>. It is heavily modified from the original version. I started using this around 2005 as a quick & dirty way to generate Java beans and data access managers from a SQL database. I still use it, as I have found many ORM tools like Hibernate to be unweildy and more trouble than they're worth. Other things that looks interesting to me these days are JDBI <http://jdbi.org/> which puts nice Java idioms on top of SQL and JDBC, ActiveJDBC <https://code.google.com/p/activejdbc/> which walks a nice line between JDBC and the active record pattern, and Ebean <http://www.avaje.org/> which is an ORM, but feels right.

This differs from the original project in the following ways:
- Build uses Maven. Packaged as a Maven plugin.
- No more web widgets or factories. Just beans and managers.
- *Managers return Lists instead of arrays.
- Manager class is gone. BaseManager now takes a DataSource directly.
- Uses generics to make the code more concise (and requires Java 1.5)
- Not sure if anything but MySQL and HSQL support works anymore. PostgreSQL did work a while ago, but haven't checked in a bit.

To do in the future:
- Add an interface for a cache providing a few convenience methods on top of a *Manager: T get(Id), List<T> get(List<Id>), List<T> get(Key)
- Provide a optional runtime library with cache implementations. 
- The CodeWriter and Database classes are messy and fragile. Port to use SchemaCrawler <http://schemacrawler.sourceforge.net/>
- Generate a (SchemaName)Database.java factory with get*Manager() methods for all managers. This would be an easy entry point to extend as your applications DAO.
- Transactions?
- Do something with foreign key mappings that is sane against bad definitions?

### Using: ###
To try it out, copy src/test/config/test.properties into your project and edit to reflect your databases's properties.

Add the following to your POM file's build section:

    <build>
      <plugins>
        <plugin>
          <groupId>net.sourceforge</groupId>
          <artifactId>sql2java-maven-plugin</artifactId>
          <version>1.0-SNAPSHOT</version>
          <executions>
            <execution>
              <id>sql2java</id>
              <goals>
                <goal>sql2java</goal>
              </goals>
            </execution>
          </executions>
          <configuration>
            <outputDirectory>${project.basedir}/src/main/java</outputDirectory> <!-- Where you want the generated sources to go -->
            <propertiesFile>${project.basedir}/src/main/resources/sql2java.properties</propertiesFile> <!-- The location of the properties file -->
          </configuration>
          </plugin>
         ...
      </plugins>
    </build>

And run:

    mvn sql2java:sql2java compile

There is a log at target/velocity.log that will tell you if anything failed, and running Maven with the -e flag should be somewhat informative.

### Customizing: ###
The Velocity templates used by the code generator are in src/main/resources. If you add a new template, you must specify it in your properties file under mgrwriter.templates.perschema or mgrwriter.templates.pertable. 

### Dependencies: ###
- Runtime dependencies for the generated code are slf4j <http://www.slf4j.org> for logging, and whatever JDBC driver you need for your database.

### Feedback: ###
Please submit a pull request if you'd like to see something changed. 

Does anyone else use this? Let me know if you are a user (of this or some sql2java variant). The occasion of meeting users of old/obscure projects that I use has been known to produce free beer in San Francisco.
