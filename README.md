# ebinterface-ubl-mapping
Mapping ebInterface 4.2 from and to UBL 2.1.
Author: Philip Helger

This project is based on the project `ubl2ebinterface` which resides on Joinup (and will not be developed any further).

The reason why this project is not in the [ebinterface-mappings](https://github.com/austriapro/ebinterface-mappings) project is the totally different software stack used and the requirement of this project to be published on Maven central. Therefore the group name is still "com.helger" as I only have rights to publish below this tree node to Maven central.

# Status
Currently on the the conversion UBL -> ebInterface 4.2 is present. Next steps are to build the other way around as well.

# News and noteworthy

  * v2.2.1 - work in progress
    * Fixed potential NPE if UBL invoice has no `FinancialAccount` 
  * v2.2.0 - 2017-08-04
    * Allow to create ebInterface 4.3 as well
    * Added conversion from ebInterface 4.1/4.2/4.3 to UBL
  * v2.1.1 - 2016-09-27
    * Requires at least ph-common 8.5.2
  * v2.1.0 - 2016-09-12
    * Bind to ph-commons 8.5.x
  * v2.0.1 - 2016-08-01  
  * v2.0.0 - 2016-07-12
    * Bind to ph-commons 8.x
    * Requires JDK 1.8
  * v1.1.0 - 2016-01-26
    * Last version for JDK 6    

# Building
To build the project you need at least Java 1.8 (or newer) and Apache Maven 3.x. Build is quite easy: call `mvn clean install` in this directory and the final result will be in the `target` directory.
Additionally Eclipse project files for the latest Eclipse version are contained.

# Maven usage
Add the following to your pom.xml to use this artifact:
```
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ebinterface-ubl-mapping</artifactId>
  <version>2.2.0</version>
</dependency>
```

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodeingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>
