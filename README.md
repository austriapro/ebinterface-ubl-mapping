# ebinterface-ubl-mapping

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/at.austriapro/ebinterface-ubl-mapping/badge.svg)](https://maven-badges.herokuapp.com/maven-central/at.austriapro/ebinterface-ubl-mapping) 
[![javadoc](https://javadoc.io/badge2/at.austriapro/ebinterface-ubl-mapping/javadoc.svg)](https://javadoc.io/doc/at.austriapro/ebinterface-ubl-mapping)
[![CodeCov](https://codecov.io/gh/austriapro/ebinterface-ubl-mapping/branch/master/graph/badge.svg)](https://codecov.io/gh/austriapro/ebinterface-ubl-mapping)

Mapping ebInterface 4.x/5.x/6.x from and to UBL 2.1.

Author: Philip Helger

The reason why this project is not in the [ebinterface-mappings](https://github.com/austriapro/ebinterface-mappings) project is the totally different software stack used and the requirement of this project to be published on Maven central. 
Since v4.5.0 the Maven group ID changed to `at.austriapro` - before that, the group ID was "com.helger".

# Building

To build the project you need at least Java 11 (or newer) and Apache Maven 3.x. Build is quite easy: call `mvn clean install` in this directory and the final result will be in the `target` directory.
Additionally Eclipse project files for the latest Eclipse version are contained.

Versions prior to v5 use Java 1.8 as the baseline.

# Maven usage

Add the following to your pom.xml to use this artifact, replacing `x.y.z` with the real version:

```xml
<dependency>
  <groupId>at.austriapro</groupId>
  <artifactId>ebinterface-ubl-mapping</artifactId>
  <version>x.y.z</version>
</dependency>
```

# News and noteworthy

* v5.2.7 - 2024-10-17
    * Made sure the UBL invoice line `Item/Name` element has higher precedence than `Item/Description`
* v5.2.6 - 2024-10-11
    * Added mapping the ebInterface `Delivery/Description` element to UBL `Delivery/DeliveryTerms[0]/SpecialTerms[0]`
* v5.2.5 - 2024-10-10
    * The UBL `OrderLineReference` element is only created if the mandatory `LineID` element is present
* v5.2.4 - 2024-09-05
    * Payment Conditions are also created in ebInterface 4.2 onwards, if any other field then just the DueDate is set
* v5.2.3 - 2024-07-30
    * Fixed BIC mapping for output format ebInterface 4.0
    * Improved empty BIC handling when creating ebInterface
* v5.2.2 - 2024-05-02
    * Avoid overwriting `PaymentConditions/DueDate` with `null` when it is already set
    * Re-added using top-level `Invoice/DueDate` when creating ebInterface document
* v5.2.1 - 2024-04-15
    * Fixed the SEPA Direct Debit mapping from UBL to ebInterface
    * Fixed the SEPA Direct Debit mapping from ebInterface to UBL
    * Removed the `schemeID` and `schemeAgency` attributes from `TaxScheme` and `TaxCategory`
    * Fixed payment reference mapping (`PaymentID` preferred over `InstructionID`). See [#3](https://github.com/austriapro/ebinterface-ubl-mapping/issues/3)
* v5.2.0 - 2024-04-02
    * Ensured Java 21 compatibility
    * Updated to ph-ubl 9.x
* v5.1.4 - 2024-02-26
    * Avoid empty `RelatedDocument/Comment` elements in ebInterface 4.1+
    * Added support for invoice type code `218` as a `FinalSettlement`
* v5.1.3 - 2023-11-14
    * ebInterface 5.0+ `Discount/Comment` is converted to a UBL `PaymentTerms/Note` element
* v5.1.2 - 2023-11-09
    * Fixed the mapping of ebInterface DocumentType `CreditNote` to UBL Invoice Type Code `381`
* v5.1.1 - 2023-09-26
    * Fixed the `DocumentCurrencyCode/@listID` attribute for all ebInterface versions != 4.2
    * Avoid creating empty UBL elements
* v5.1.0 - 2023-04-30
    * Updated to ph-ubl 8.x
* v5.0.0 - 2023-03-02
    * Using Java 11 as the baseline
    * Updated to ph-commons 11
    * Using JAXB 4.0 as the baseline
* v4.8.3 - 2022-10-12
    * Added mapping of `BelowTheLineItem` in ebInterface 6.1 as done in 4.3 and previously
    * Extracted shared customization base interfaces
* v4.8.2 - 2022-08-11
    * Updated to peppol-commons 8.7.6
    * Added support for ebInterface 6.1 from and to UBL
* v4.8.1 - 2022-04-12
    * Added specific support for `InvoiceTypeCode` mapping of `326`, `386` and `389` from UBL to ebInterface
* v4.8.0 - 2021-05-03
    * Updated to ph-commons 10.1
* v4.7.0 - 2021-03-22
    * Updated to ph-commons 10
* v4.6.3 - 2020-10-28
    * Fixed the application of the "EnforcedSupplierEmailAddress" when converting to ebInterface 5.0 and 6.0
* v4.6.2 - 2020-10-13
    * Heavily extended the allowed UBL Invoice type codes. Allowed values are now: `80`, `82`, `84`, `130`, `202`, `203`, `204`, `211`, `295`, `325`, `326`, `380`, `383`, `384`, `385`, `386`, `387`, `388`, `389`, `390`, `393`, `394`, `395`, `456`, `457`, `527`, `575`, `623`, `633`, `751`, `780`, `935`
    * Added support for UBLVersionID `2.3`
* v4.6.1 - 2020-09-17
    * Updated to Jakarta JAXB 2.3.3
* v4.6.0 - 2020-08-30
    * Updated to ph-ubl 6.4.0
    * Updated to ph-ebinterface 6.2.0
* v4.5.8 - 2020-08-13
    * Mapping UBL `AdditionalItemProperty` to ebInterface `AdditionalInformation` (ebInterface 5.0 or higher) on line level
* v4.5.7 - 2020-07-08
    * Using `Invoice/DueDate` as an alternative to `Invoice/PaymentMeans/PaymentDueDate`
* v4.5.6 - 2020-05-26
    * Updated to ph-ebinterface 6.1.5 with ph-xsds 2.3.0 (new Maven groupId)
* v4.5.5 - 2020-05-14
    * Updated to support ebInterface 6.0 from and to UBL
    * Fixed a divide by zero error if payableAmount is 0
* v4.5.4 - 2020-04-01
    * Now ebInterface 5.0 `Contact` email addresses and telephone numbers are also mapped to UBL (see [issue #2](https://github.com/austriapro/ebinterface-ubl-mapping/issues/2))
* v4.5.3 - 2020-02-07
    * Preferring `PaymentMeans/InstructionID` over `PaymentMeans/PaymentID`
    * Changed the mapping of ebInterface from `PaymentReference` to `PaymentMeans/InstructionID`
    * Updated to peppol-commons 8.x
* v4.5.2 - 2019-12-17
    * Made the "profile ID to process Identifier" mapping customizable
    * Unified error levels between Invoice and CreditNote on the ProfileID
* v4.5.1 - 2019-10-14
    * Fixed a regression that payment type "41" was checked instead of "42"
* v4.5.0 - 2019-10-01
    * Changed all package names to `at.austriapro`
    * Changed the Maven group `at.austriapro`
* v4.0.1 - 2019-09-18
    * Fixed potential NPE in county code mapping from ebInterface to UBL
* v4.0.0 - 2019-09-13
    * Added code to convert UBL Invoice/CreditNote to ebInterface 4.0, 4.1 and 5.0
    * Added code to convert ebInterface 4.0, 4.1 and 5.0 to UBL Invoice
    * Added check that order reference is mandatory if an order position number is used
    * Improved customizability of conversion
    * Improved consistency of existing conversions
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

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a>