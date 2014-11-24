# sql2java #

This is a fork of an abandoned project on SourceForge by the same name <http://sql2java.sourceforge.net/>. It is heavily modified from the original version. I started using this around 2005 as a quick & dirty way to generate Java beans and data access managers from a SQL database. I still use it, as I have found many ORM tools like Hibernate to be unweildy and more trouble than they're worth. 

This differs from the original project in the following ways:
- Build uses Maven. Packaged as a Maven plugin.
- New Maven plugin (sqlfile) for sourcing a SQL DDL before generation.
- sql2java-lib runtime library is now a mandatory dependency.
- No more web widgets or factories. Just beans and managers.
- *Managers return Lists instead of arrays.
- Manager class is gone. BaseManager now takes a DataSource directly.
- Generates a (SchemaName)Database.java factory with get*Manager() methods for all managers. Intended as an easy entry point to extend as your application's DAO.
- Management of transactions from the (SchemaName)Database.java class.
- Uses generics to make the code more concise (and requires Java 1.5)
- Not sure if anything but MySQL and HSQL support works anymore. PostgreSQL did work a while ago, but haven't checked in a bit.

To do in the future:
- Add an interface for a cache providing a few convenience methods on top of a *Manager: T get(Id), List<T> get(List<Id>), List<T> get(Key). Add optional runtime library with cache implementations. 
- The CodeWriter and Database classes are messy and fragile. Port to use SchemaCrawler <http://schemacrawler.sourceforge.net/>. Also look at jOOQ <http://www.jooq.org/> and see what they're doing.
- Do something with foreign key mappings that is sane against bad definitions.
- Do something better with compound primary keys (despite thinking they're a bad design decision).
- Move the properties defined in the file into the Maven plugin definition. Allow a list of tables to be specified for generation.

### Using: ###
To try it out, copy src/test/config/test.properties into your project and edit to reflect your databases's properties.

Add the following to your POM file's build section:

    <build>
      <plugins>
        <plugin>
          <groupId>com.github.xgp</groupId>
          <artifactId>sql2java-maven-plugin</artifactId>
          <version>${sql2java.version}</version>
          <executions>
            <execution>
              <id>sql2java</id>
              <goals>
                <goal>sql2java</goal>
              </goals>
            </execution>
          </executions>
          <configuration>
            <outputDirectory>${project.build.directory}/generated-sources/sql2java</outputDirectory>
            <propertiesFile>${project.basedir}/src/main/resources/sql2java.properties</propertiesFile>
            <driver>org.hsqldb.jdbc.JDBCDriver</driver>
            <url>jdbc:hsqldb:file:${project.build.directory}/databases/test</url>
            <user>SA</user>
            <password></password>
            <schema>PUBLIC</schema>
            <packageName>com.test</packageName>
          </configuration>
          <dependencies>
            <!-- Add your JDBC driver here -->
            <dependency>
              <groupId>org.hsqldb</groupId>
              <artifactId>hsqldb</artifactId>
              <version>${hsqldb.version}</version>
            </dependency>
          </dependencies>
        </plugin>
      </plugins>
    </build>

And run:

    mvn sql2java:sql2java compile

There is a log at target/velocity.log that will tell you if anything failed, and running Maven with the -e flag should be somewhat informative.

### Customizing: ###
The Velocity templates used by the code generator are in src/main/resources. If you add a new template, you must specify it in your properties file under mgrwriter.templates.perschema or mgrwriter.templates.pertable. 

### Dependencies: ###
Runtime dependencies for the generated code are sql2java-lib, slf4j for logging, and whatever JDBC driver you need for your database.

### Feedback: ###
Please submit a pull request if you'd like to see something changed. 
