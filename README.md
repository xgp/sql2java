# sql2java #

This is a fork of a project on SourceForge by the same name <http://sql2java.sourceforge.net/>. It is heavily modified from the original version. I started using this around 2005 as a quick & dirty way to generate Java beans and data access managers from a SQL database. Here I am, 7+ years later, still using it, totally underwhelmed with the state of ORM tools like Hibernate. The short of it is, if you know SQL, there's no reason to use an ORM that hides it from you. If you don't, well, you're probably screwed on the performance front, so you should switch to Ruby On Rails anyways, as you'll be much more productive. The only thing that looks interesting to me these days is JDBI <http://jdbi.org/> which puts nice Java idioms on top of SQL and JDBC. Otherwise, for trivial projects where I'm just ripping strings in and out of databases, I'll probably keep using this for another 7 years.

This differs from the original project in the following ways:
- Build uses Maven. Will be packaged as a Maven plugin in the future.
- No more web widgets or factories. Just beans and managers.
- Manager class is gone. BaseManager now take a DataSource directly.
- Uses generics to make the code more concise (and requires Java 1.5)
- Not sure if anything but MySQL and HSQL support works anymore. PostgreSQL did work a while ago, but haven't checked in a bit.

### Using: ###
To try it out, edit src/config/test.properties to reflect your databases's properties, and run:

./run.sh src/config/test.properties

Which just uses Maven exec:java to run:

mvn -e exec:java -Dexec.classpathScope="runtime" -Dexec.mainClass="net.sourceforge.sql2java.Main" -Dexec.args="src/config/test.properties"

Generated classes will appear in target/generated-sources/ or wherever you configured in src/config/test.properties. There is a log at target/velocity.log that will tell you if anything failed, and running Maven with the -e flag should be somewhat informative.

### Customizing: ###
The Velocity templates used by the code generator are in src/main/resources/templates/velocity. The per-schema templates are in the global/ subdirectory, and the per-table templates are in the table/ subdirectory. If you add a new template to either directory, you must specify it in your properties file under mgrwriter.templates.perschema or mgrwriter.templates.pertable. 

### Dependencies: ###
- Runtime dependencies for the generated code are slf4j <http://www.slf4j.org> for logging, whatever JDBC driver you need for your database, and google-guava <https://code.google.com/p/guava-libraries/> for Immutable Lists/Maps and caching.

### Feedback: ###
Please submit a pull request if you'd like to see something changed. 

Does anyone else use this? Let me know if you are a user (of this or some sql2java variant). The occasion of meeting users of old/obscure projects that I use has been known to produce free beer in San Francisco.
