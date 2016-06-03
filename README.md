# ebinterface-ubl-mapping
Mapping ebInterface 4.2 from and to UBL 2.1.
Author: Philip Helger

This project is based on the project `ubl2ebinterface` which resides on Joinup (and will not be developed any further).

The reason why this project is not in the [ebinterface-mappings](https://github.com/austriapro/ebinterface-mappings) project is the totally different software stack used and the requirement of this project to be published on Maven central. Therefore the group name is still "com.helger" as I only have rights to publish below this tree node to Maven central.

# Status
Currently on the the conversion UBL -> ebInterface 4.2 is present. Next steps are to build the other way around as well.

Version 2.0 and on will require JDK 1.8. The last version for JDK 1.6 is `1.1.0`.

#Building
To build the project you need at least Java 1.8 (or newer) and Apache Maven 3.x. Build is quite easy: call `mvn clean install` in this directory and the final result will be in the `target` directory.
Additionally Eclipse project files for the latest Eclipse version are contained.

#Maven usage
Add the following to your pom.xml to use this artifact:
```
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ebinterface-ubl-mapping</artifactId>
  <version>1.1.0</version>
</dependency>
```
