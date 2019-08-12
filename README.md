# ebinterface-ubl-mapping

Mapping ebInterface 4.2/4.3 from and to UBL 2.1 and vice versa.

Author: Philip Helger

The reason why this project is not in the [ebinterface-mappings](https://github.com/austriapro/ebinterface-mappings) project is the totally different software stack used and the requirement of this project to be published on Maven central. Therefore the group name is still "com.helger" as I only have rights to publish below this tree node to Maven central.

# News and noteworthy

* v4.0.0 - work in progress
    * Added code to generate ebInterface 4.0 from UBL Invoice and Credit Note
    * Added code to generate ebInterface 4.1 from UBL Invoice and Credit Note
    * Added code to generate ebInterface 5.0 from UBL Invoice and Credit Note
* v3.0.8 - 2019-03-28
    * Fixed parsing of process identifiers in "Profile" element for CreditNotes to match the Invoice rules
    * Added support for the UBL version "2.2"
    * CreditNotes now also transform the `PaymentMethod` if present but fallback to `NoPayment` if none is provided
* v3.0.7 - 2019-02-27
    * Avoid creating negative `Percent` element values
* v3.0.6 - 2019-02-25
    * Improved handling of empty `TaxCategory/ID` and `TaxScheme/ID`
* v3.0.5 - 2019-02-22
    * Handling empty `PaymentChannelCode` like if it was `null`.
* v3.0.4 - 2018-11-22
    * Updated to ph-commons 9.2.0
* v3.0.3 - 2018-06-21
    * Fixed division by zero if BaseQuantity is 0
* v3.0.2 - 2018-04-13
    * Updated to peppol-commons 6.0.3
* v3.0.1 - 2018-04-06
    * Ignoring empty Description/Name/Note elements on line level
* v3.0.0 - 2018-02-23
    * Fixed potential NPE if UBL invoice has no `FinancialAccount`
    * Updated to ph-commons 9.0.0 
    * Conversion of `TaxExemption` was added
    * Some error handling details were improved
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

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>ebinterface-ubl-mapping</artifactId>
  <version>3.0.8</version>
</dependency>
```

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>
