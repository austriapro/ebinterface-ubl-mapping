/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2024 AUSTRIAPRO - www.austriapro.at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.austriapro.ebinterface.ubl.from;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.datetime.XMLOffsetDate;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.locale.country.CountryCache;
import com.helger.commons.math.MathHelper;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v43.Ebi43AccountType;
import com.helger.ebinterface.v43.Ebi43AddressIdentifierType;
import com.helger.ebinterface.v43.Ebi43AddressIdentifierTypeType;
import com.helger.ebinterface.v43.Ebi43AddressType;
import com.helger.ebinterface.v43.Ebi43CountryType;
import com.helger.ebinterface.v43.Ebi43DeliveryType;
import com.helger.ebinterface.v43.Ebi43DirectDebitType;
import com.helger.ebinterface.v43.Ebi43DiscountType;
import com.helger.ebinterface.v43.Ebi43DocumentTypeType;
import com.helger.ebinterface.v43.Ebi43InvoiceType;
import com.helger.ebinterface.v43.Ebi43NoPaymentType;
import com.helger.ebinterface.v43.Ebi43PaymentConditionsType;
import com.helger.ebinterface.v43.Ebi43PaymentMethodType;
import com.helger.ebinterface.v43.Ebi43PaymentReferenceType;
import com.helger.ebinterface.v43.Ebi43RelatedDocumentType;
import com.helger.ebinterface.v43.Ebi43SEPADirectDebitType;
import com.helger.ebinterface.v43.Ebi43SEPADirectDebitTypeType;
import com.helger.ebinterface.v43.Ebi43UniversalBankTransactionType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BillingReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BranchType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialAccountType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialInstitutionType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyIdentificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyLegalEntityType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentMeansType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentTermsType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PersonType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentDescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionNoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.PaymentIDType;

/**
 * Base class for Peppol UBL to ebInterface 4.3 converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractToEbInterface43Converter extends AbstractToEbInterfaceConverter
{
  public static final int PAYMENT_REFERENCE_MAX_LENGTH = 35;

  protected AbstractToEbInterface43Converter (@Nonnull final Locale aDisplayLocale,
                                              @Nonnull final Locale aContentLocale,
                                              @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale, aSettings);
  }

  public static void setAddressData (@Nullable final AddressType aUBLAddress,
                                     @Nonnull final Ebi43AddressType aEbiAddress,
                                     @Nonnull final Locale aContentLocale)
  {
    // Convert main address
    if (aUBLAddress != null)
    {
      aEbiAddress.setStreet (StringHelper.getImplodedNonEmpty (' ',
                                                               StringHelper.trim (aUBLAddress.getStreetNameValue ()),
                                                               StringHelper.trim (aUBLAddress.getBuildingNumberValue ())));
      aEbiAddress.setPOBox (StringHelper.trim (aUBLAddress.getPostboxValue ()));
      aEbiAddress.setTown (StringHelper.trim (aUBLAddress.getCityNameValue ()));
      aEbiAddress.setZIP (StringHelper.trim (aUBLAddress.getPostalZoneValue ()));

      // Country
      if (aUBLAddress.getCountry () != null)
      {
        final Ebi43CountryType aEbiCountry = new Ebi43CountryType ();
        final String sEbiCountryCode = StringHelper.trim (aUBLAddress.getCountry ().getIdentificationCodeValue ());
        aEbiCountry.setCountryCode (sEbiCountryCode);

        final String sCountryName = StringHelper.trim (aUBLAddress.getCountry ().getNameValue ());
        aEbiCountry.setValue (sCountryName);
        if (StringHelper.hasNoText (sCountryName) && StringHelper.hasText (sEbiCountryCode))
        {
          // Write locale of country in content locale
          final Locale aLocale = CountryCache.getInstance ().getCountry (sEbiCountryCode);
          if (aLocale != null)
            aEbiCountry.setValue (aLocale.getDisplayCountry (aContentLocale));
        }
        aEbiAddress.setCountry (aEbiCountry);
      }
    }
  }

  public static void validateAddressData (@Nonnull final Ebi43AddressType aEbiAddress,
                                          @Nonnull final String sPartyType,
                                          @Nonnull final ErrorList aTransformationErrorList,
                                          @Nonnull final Locale aDisplayLocale)
  {
    if (aEbiAddress.getStreet () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName (sPartyType + "/PostalAddress/StreetName")
                                               .errorText (EText.ADDRESS_NO_STREET.getDisplayText (aDisplayLocale))
                                               .build ());
    if (aEbiAddress.getTown () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName (sPartyType + "/PostalAddress/CityName")
                                               .errorText (EText.ADDRESS_NO_CITY.getDisplayText (aDisplayLocale))
                                               .build ());
    if (aEbiAddress.getZIP () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName (sPartyType + "/PostalAddress/PostalZone")
                                               .errorText (EText.ADDRESS_NO_ZIPCODE.getDisplayText (aDisplayLocale))
                                               .build ());
    if (aEbiAddress.getCountry () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName (sPartyType +
                                                                "/PostalAddress/Country/IdentificationCode")
                                               .errorText (EText.ADDRESS_NO_COUNTRY.getDisplayText (aDisplayLocale))
                                               .build ());
  }

  @Nonnull
  public static Ebi43AddressType convertParty (@Nonnull final PartyType aUBLParty,
                                               @Nonnull final String sPartyType,
                                               @Nonnull final ErrorList aTransformationErrorList,
                                               @Nonnull final Locale aContentLocale,
                                               @Nonnull final Locale aDisplayLocale,
                                               final boolean bValidate)
  {
    final Ebi43AddressType aEbiAddress = new Ebi43AddressType ();

    if (aUBLParty.getPartyNameCount () > 1)
      aTransformationErrorList.add (SingleError.builderWarn ()
                                               .errorFieldName (sPartyType + "/PartyName")
                                               .errorText (EText.MULTIPLE_PARTIES.getDisplayText (aDisplayLocale))
                                               .build ());

    // Convert name
    final PartyNameType aUBLPartyName = CollectionHelper.getAtIndex (aUBLParty.getPartyName (), 0);
    if (aUBLPartyName != null)
      aEbiAddress.setName (StringHelper.trim (aUBLPartyName.getNameValue ()));

    if (aEbiAddress.getName () == null && aUBLParty.hasPartyLegalEntityEntries ())
    {
      // For EN set from cac:PartyLegalEntity/cbc:RegistrationName
      aEbiAddress.setName (StringHelper.trim (aUBLParty.getPartyLegalEntityAtIndex (0).getRegistrationNameValue ()));
    }

    if (aEbiAddress.getName () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName (sPartyType)
                                               .errorText (EText.PARTY_NO_NAME.getDisplayText (aDisplayLocale))
                                               .build ());

    // Convert main address
    setAddressData (aUBLParty.getPostalAddress (), aEbiAddress, aContentLocale);

    // Contact
    final ContactType aUBLContact = aUBLParty.getContact ();
    if (aUBLContact != null)
    {
      aEbiAddress.setPhone (StringHelper.trim (aUBLContact.getTelephoneValue ()));
      aEbiAddress.setEmail (StringHelper.trim (aUBLContact.getElectronicMailValue ()));
    }

    // Person name
    final ICommonsList <String> ebContacts = new CommonsArrayList <> ();
    if (aUBLContact != null)
      if (StringHelper.hasTextAfterTrim (aUBLContact.getNameValue ()))
        ebContacts.add (StringHelper.trim (aUBLContact.getNameValue ()));
    for (final PersonType aUBLPerson : aUBLParty.getPerson ())
    {
      if (StringHelper.hasNoText (aEbiAddress.getSalutation ()))
        aEbiAddress.setSalutation (StringHelper.trim (aUBLPerson.getGenderCodeValue ()));
      ebContacts.add (StringHelper.getImplodedNonEmpty (' ',
                                                        StringHelper.trim (aUBLPerson.getTitleValue ()),
                                                        StringHelper.trim (aUBLPerson.getFirstNameValue ()),
                                                        StringHelper.trim (aUBLPerson.getMiddleNameValue ()),
                                                        StringHelper.trim (aUBLPerson.getFamilyNameValue ()),
                                                        StringHelper.trim (aUBLPerson.getNameSuffixValue ())));
    }
    if (!ebContacts.isEmpty ())
      aEbiAddress.setContact (StringHelper.getImplodedNonEmpty ('\n', ebContacts));

    // GLN and DUNS number
    if (aUBLParty.getEndpointID () != null)
    {
      final String sEndpointID = StringHelper.trim (aUBLParty.getEndpointIDValue ());
      if (StringHelper.hasText (sEndpointID))
      {
        // We have an endpoint ID

        // Check all identifier types
        final String sSchemeIDToSearch = StringHelper.trim (aUBLParty.getEndpointID ().getSchemeID ());

        for (final Ebi43AddressIdentifierTypeType eType : Ebi43AddressIdentifierTypeType.values ())
          if (eType.value ().equalsIgnoreCase (sSchemeIDToSearch))
          {
            final Ebi43AddressIdentifierType aEbiType = new Ebi43AddressIdentifierType ();
            aEbiType.setAddressIdentifierType (eType);
            aEbiType.setValue (sEndpointID);
            aEbiAddress.addAddressIdentifier (aEbiType);
          }

        if (aEbiAddress.hasNoAddressIdentifierEntries ())
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName (sPartyType)
                                                   .errorText (EText.PARTY_UNSUPPORTED_ENDPOINT.getDisplayTextWithArgs (aDisplayLocale,
                                                                                                                        sEndpointID,
                                                                                                                        aUBLParty.getEndpointID ()
                                                                                                                                 .getSchemeID ()))
                                                   .build ());
      }
    }

    if (aEbiAddress.hasNoAddressIdentifierEntries ())
    {
      // check party identification
      int nPartyIdentificationIndex = 0;
      for (final PartyIdentificationType aUBLPartyID : aUBLParty.getPartyIdentification ())
      {
        final String sUBLPartyID = StringHelper.trim (aUBLPartyID.getIDValue ());
        for (final Ebi43AddressIdentifierTypeType eType : Ebi43AddressIdentifierTypeType.values ())
          if (eType.value ().equalsIgnoreCase (aUBLPartyID.getID ().getSchemeID ()))
          {
            // Add GLN/DUNS number
            final Ebi43AddressIdentifierType aEbiType = new Ebi43AddressIdentifierType ();
            aEbiType.setAddressIdentifierType (eType);
            aEbiType.setValue (sUBLPartyID);
            aEbiAddress.addAddressIdentifier (aEbiType);
          }
        if (aEbiAddress.hasNoAddressIdentifierEntries ())
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName (sPartyType +
                                                                    "/PartyIdentification[" +
                                                                    nPartyIdentificationIndex +
                                                                    "]")
                                                   .errorText (EText.PARTY_UNSUPPORTED_ADDRESS_IDENTIFIER.getDisplayTextWithArgs (aDisplayLocale,
                                                                                                                                  sUBLPartyID,
                                                                                                                                  aUBLPartyID.getID ()
                                                                                                                                             .getSchemeID ()))
                                                   .build ());
        ++nPartyIdentificationIndex;
      }
    }

    if (bValidate)
      validateAddressData (aEbiAddress, sPartyType, aTransformationErrorList, aDisplayLocale);
    return aEbiAddress;
  }

  @Nonnull
  protected static String getAggregated (@Nonnull final Iterable <DescriptionType> aList)
  {
    return StringHelper.getImplodedMapped ('\n', aList, DescriptionType::getValue);
  }

  protected static boolean isAddressIncomplete (@Nonnull final Ebi43AddressType aEbiAddress)
  {
    return StringHelper.hasNoText (aEbiAddress.getName ()) ||
           StringHelper.hasNoText (aEbiAddress.getTown ()) ||
           StringHelper.hasNoText (aEbiAddress.getZIP ()) ||
           aEbiAddress.getCountry () == null;
  }

  @Nonnull
  public static Ebi43DeliveryType convertDelivery (@Nonnull final DeliveryType aUBLDelivery,
                                                   @Nonnull final String sDeliveryType,
                                                   @Nullable final CustomerPartyType aCustomerParty,
                                                   @Nonnull final ErrorList aTransformationErrorList,
                                                   @Nonnull final Locale aContentLocale,
                                                   @Nonnull final Locale aDisplayLocale)
  {
    final Ebi43DeliveryType aEbiDelivery = new Ebi43DeliveryType ();

    // Set the delivery ID
    aEbiDelivery.setDeliveryID (aUBLDelivery.getIDValue ());

    // Set the delivery date
    aEbiDelivery.setDate (aUBLDelivery.getActualDeliveryDateValue ());

    final PartyType aUBLParty = aUBLDelivery.getDeliveryParty ();
    Ebi43AddressType aEbiAddress = null;
    if (aUBLParty != null)
    {
      aEbiAddress = convertParty (aUBLParty,
                                  "DeliveryParty",
                                  aTransformationErrorList,
                                  aContentLocale,
                                  aDisplayLocale,
                                  false);
      aEbiDelivery.setAddress (aEbiAddress);
    }

    // Address present?
    if (aEbiAddress == null || isAddressIncomplete (aEbiAddress))
    {
      final AddressType aUBLDeliveryAddress = aUBLDelivery.getDeliveryAddress ();
      if (aUBLDeliveryAddress != null)
      {
        if (aEbiAddress == null)
          aEbiAddress = new Ebi43AddressType ();
        setAddressData (aUBLDeliveryAddress, aEbiAddress, aContentLocale);
        aEbiDelivery.setAddress (aEbiAddress);
      }
    }

    final LocationType aUBLDeliveryLocation = aUBLDelivery.getDeliveryLocation ();
    if (aUBLDeliveryLocation != null && aUBLDeliveryLocation.getAddress () != null)
    {
      // Optional description
      aEbiDelivery.setDescription (getAggregated (aUBLDeliveryLocation.getDescription ()));

      if (aEbiAddress == null || isAddressIncomplete (aEbiAddress))
      {
        // No Delivery/DeliveryAddress present
        if (aEbiAddress == null)
          aEbiAddress = new Ebi43AddressType ();
        setAddressData (aUBLDeliveryLocation.getAddress (), aEbiAddress, aContentLocale);
        aEbiDelivery.setAddress (aEbiAddress);
      }
    }

    if (aEbiAddress != null)
    {
      String sAddressName = null;

      // Check delivery party
      if (aUBLDelivery.getDeliveryParty () != null)
        for (final PartyNameType aUBLPartyName : aUBLDelivery.getDeliveryParty ().getPartyName ())
        {
          sAddressName = StringHelper.trim (aUBLPartyName.getNameValue ());
          if (StringHelper.hasText (sAddressName))
            break;
        }

      // As fallback use delivery location name
      if (StringHelper.hasNoText (sAddressName) && aUBLDeliveryLocation != null)
        sAddressName = StringHelper.trim (aUBLDeliveryLocation.getNameValue ());

      // As fallback use accounting customer party
      if (StringHelper.hasNoText (sAddressName) && aCustomerParty != null && aCustomerParty.getParty () != null)
      {
        for (final PartyNameType aUBLPartyName : aCustomerParty.getParty ().getPartyName ())
        {
          sAddressName = StringHelper.trim (aUBLPartyName.getNameValue ());
          if (StringHelper.hasText (sAddressName))
            break;
        }

        if (StringHelper.hasNoText (sAddressName))
        {
          // For EN invoices
          for (final PartyLegalEntityType aUBLPartyLegalEntity : aCustomerParty.getParty ().getPartyLegalEntity ())
          {
            sAddressName = StringHelper.trim (aUBLPartyLegalEntity.getRegistrationNameValue ());
            if (StringHelper.hasText (sAddressName))
              break;
          }
        }
      }
      aEbiAddress.setName (sAddressName);

      if (StringHelper.hasNoText (aEbiAddress.getName ()))
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName (sDeliveryType + "/DeliveryParty")
                                                 .errorText (EText.DELIVERY_WITHOUT_NAME.getDisplayText (aDisplayLocale))
                                                 .build ());
      validateAddressData (aEbiAddress, sDeliveryType + "/DeliveryParty", aTransformationErrorList, aDisplayLocale);
    }

    return aEbiDelivery;
  }

  @Nullable
  protected static final Ebi43DocumentTypeType getAsDocumentTypeType (@Nullable final String... aValues)
  {
    if (aValues != null)
      for (final String s : aValues)
        if (s != null)
        {
          final String sClean = s.trim ();
          try
          {
            // The first match wins
            return Ebi43DocumentTypeType.fromValue (sClean);
          }
          catch (final IllegalArgumentException ex)
          {
            // Ignore
          }

          // Try the Invoice Type Codes
          if (INVOICE_TYPE_CODE_FINAL_PAYMENT.equals (sClean))
            return Ebi43DocumentTypeType.FINAL_SETTLEMENT;
          if (INVOICE_TYPE_CODE_PARTIAL.equals (sClean))
            return Ebi43DocumentTypeType.INVOICE_FOR_PARTIAL_DELIVERY;
          if (INVOICE_TYPE_CODE_PREPAYMENT_INVOICE.equals (sClean))
            return Ebi43DocumentTypeType.INVOICE_FOR_ADVANCE_PAYMENT;
          if (INVOICE_TYPE_CODE_SELF_BILLING.equals (sClean))
            return Ebi43DocumentTypeType.SELF_BILLING;
        }
    return null;
  }

  protected static void convertRelatedDocuments (@Nonnull final List <BillingReferenceType> aUBLBillingReferences,
                                                 @Nonnull final Ebi43InvoiceType aEbiDoc)
  {
    for (final BillingReferenceType aUBLBillingReference : aUBLBillingReferences)
    {
      if (aUBLBillingReference.getInvoiceDocumentReference () != null &&
          aUBLBillingReference.getInvoiceDocumentReference ().getIDValue () != null)
      {
        final Ebi43RelatedDocumentType aEbiRelatedDocument = new Ebi43RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getInvoiceDocumentReference ().getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getInvoiceDocumentReference ().getIssueDateValue ());
        aEbiRelatedDocument.setDocumentType (Ebi43DocumentTypeType.INVOICE);
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
      else
        if (aUBLBillingReference.getCreditNoteDocumentReference () != null &&
            aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue () != null)
        {
          final Ebi43RelatedDocumentType aEbiRelatedDocument = new Ebi43RelatedDocumentType ();
          aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue ());
          aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getCreditNoteDocumentReference ()
                                                                  .getIssueDateValue ());
          aEbiRelatedDocument.setDocumentType (Ebi43DocumentTypeType.CREDIT_MEMO);
          aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
        }
      // Ignore other values
    }
  }

  protected static void convertReferencedDocuments (@Nonnull final List <DocumentReferenceType> aUBLDocumentReferences,
                                                    @Nonnull final Ebi43InvoiceType aEbiDoc)
  {
    for (final DocumentReferenceType aUBLDocumentReference : aUBLDocumentReferences)
      if (StringHelper.hasText (aUBLDocumentReference.getIDValue ()) && aUBLDocumentReference.getAttachment () == null)
      {
        final Ebi43RelatedDocumentType aEbiRelatedDocument = new Ebi43RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLDocumentReference.getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLDocumentReference.getIssueDateValue ());
        final ICommonsList <String> aComments = new CommonsArrayList <> ();
        for (final DocumentDescriptionType aUBLDocDesc : aUBLDocumentReference.getDocumentDescription ())
          aComments.add (aUBLDocDesc.getValue ());

        final String sComment = StringHelper.getImplodedNonEmpty ('\n', aComments);
        if (StringHelper.hasText (sComment))
          aEbiRelatedDocument.setComment (sComment);

        if (aUBLDocumentReference.getDocumentTypeCode () != null)
        {
          aEbiRelatedDocument.setDocumentType (getAsDocumentTypeType (aUBLDocumentReference.getDocumentTypeCode ()
                                                                                           .getName (),
                                                                      aUBLDocumentReference.getDocumentTypeCodeValue ()));
        }
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
  }

  private static void _setPaymentMeansComment (@Nonnull final PaymentMeansType aUBLPaymentMeans,
                                               @Nonnull final Ebi43PaymentMethodType aEbiPaymentMethod)
  {
    if (aUBLPaymentMeans.hasInstructionNoteEntries ())
    {
      final ICommonsList <String> aNotes = new CommonsArrayList <> ();
      for (final InstructionNoteType aUBLNote : aUBLPaymentMeans.getInstructionNote ())
        aNotes.add (StringHelper.trim (aUBLNote.getValue ()));
      if (aNotes.isNotEmpty ())
        aEbiPaymentMethod.setComment (StringHelper.getImplodedNonEmpty ('\n', aNotes));
    }
  }

  protected void convertPayment (@Nonnull final Supplier <XMLOffsetDate> aUBLTopLevelDueDate,
                                 @Nonnull final Supplier <List <PaymentMeansType>> aUBLDocPaymentMeans,
                                 @Nonnull final Supplier <PartyType> aUBLDocPayeeParty,
                                 @Nonnull final Supplier <SupplierPartyType> aUBLDocAccountingSupplierParty,
                                 @Nonnull final Supplier <List <PaymentTermsType>> aUBLDocPaymentTerms,
                                 @Nonnull final Supplier <MonetaryTotalType> aUBLDocLegalMonetaryTotal,
                                 @Nonnull final ErrorList aTransformationErrorList,
                                 @Nonnull final Ebi43InvoiceType aEbiDoc,
                                 final boolean bIsCreditNote)
  {
    final Ebi43PaymentMethodType aEbiPaymentMethod = new Ebi43PaymentMethodType ();
    final Ebi43PaymentConditionsType aEbiPaymentConditions = new Ebi43PaymentConditionsType ();

    {
      int nPaymentMeansIndex = 0;
      for (final PaymentMeansType aUBLPaymentMeans : aUBLDocPaymentMeans.get ())
      {
        // Use the top-level due date
        XMLOffsetDate aUBLDueDate = aUBLPaymentMeans.getPaymentDueDateValue ();
        if (aUBLDueDate == null)
        {
          // Fallback
          aUBLDueDate = aUBLTopLevelDueDate.get ();
        }

        // https://www.unece.org/trade/untdid/d16b/tred/tred4461.htm
        final String sPaymentMeansCode = StringHelper.trim (aUBLPaymentMeans.getPaymentMeansCodeValue ());
        if (isUniversalBankTransaction (sPaymentMeansCode))
        {
          // Is a payment channel code present?
          final String sPaymentChannelCode = StringHelper.trim (aUBLPaymentMeans.getPaymentChannelCodeValue ());
          if (isIBAN (sPaymentChannelCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi43UniversalBankTransactionType aEbiUBTMethod = new Ebi43UniversalBankTransactionType ();

            // Find payment reference
            int nPaymentIDIndex = 0;
            for (final PaymentIDType aUBLPaymentID : aUBLPaymentMeans.getPaymentID ())
            {
              String sUBLPaymentID = StringHelper.trim (aUBLPaymentID.getValue ());
              if (StringHelper.hasText (sUBLPaymentID))
              {
                if (sUBLPaymentID.length () > PAYMENT_REFERENCE_MAX_LENGTH)
                {
                  // Reference
                  aTransformationErrorList.add (SingleError.builderWarn ()
                                                           .errorFieldName ("PaymentMeans[" +
                                                                            nPaymentMeansIndex +
                                                                            "]/PaymentID[" +
                                                                            nPaymentIDIndex +
                                                                            "]")
                                                           .errorText (EText.PAYMENT_ID_TOO_LONG_CUT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                             sUBLPaymentID))
                                                           .build ());
                  sUBLPaymentID = sUBLPaymentID.substring (0, PAYMENT_REFERENCE_MAX_LENGTH);
                }

                final Ebi43PaymentReferenceType aEbiPaymentReference = new Ebi43PaymentReferenceType ();
                aEbiPaymentReference.setValue (sUBLPaymentID);
                aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
              }
              ++nPaymentIDIndex;
            }

            if (aEbiUBTMethod.getPaymentReference () == null)
            {
              // Legacy (see #3)
              final InstructionIDType aUBLInstructionID = aUBLPaymentMeans.getInstructionID ();
              if (aUBLInstructionID != null)
              {
                // Prefer InstructionID over payment reference
                String sUBLInstructionID = StringHelper.trim (aUBLInstructionID.getValue ());
                if (StringHelper.hasText (sUBLInstructionID))
                {
                  if (sUBLInstructionID.length () > PAYMENT_REFERENCE_MAX_LENGTH)
                  {
                    // Reference
                    aTransformationErrorList.add (SingleError.builderWarn ()
                                                             .errorFieldName ("PaymentMeans[" +
                                                                              nPaymentMeansIndex +
                                                                              "]/InstructionID")
                                                             .errorText (EText.PAYMENT_ID_TOO_LONG_CUT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                               sUBLInstructionID))
                                                             .build ());
                    sUBLInstructionID = sUBLInstructionID.substring (0, PAYMENT_REFERENCE_MAX_LENGTH);
                  }

                  final Ebi43PaymentReferenceType aEbiPaymentReference = new Ebi43PaymentReferenceType ();
                  aEbiPaymentReference.setValue (sUBLInstructionID);
                  aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
                }
              }
            }

            // Beneficiary account
            final Ebi43AccountType aEbiAccount = new Ebi43AccountType ();

            // BIC
            final FinancialAccountType aUBLFinancialAccount = aUBLPaymentMeans.getPayeeFinancialAccount ();
            if (aUBLFinancialAccount != null)
            {
              final BranchType aUBLBranch = aUBLFinancialAccount.getFinancialInstitutionBranch ();
              if (aUBLBranch != null)
              {
                // Prefer FinancialInstitutionBranch over FinancialInstitution
                boolean bUseFI = false;
                String sBIC = null;
                String sBICScheme = null;
                if (aUBLBranch.getID () != null)
                {
                  sBIC = StringHelper.trim (aUBLBranch.getID ().getValue ());
                  sBICScheme = StringHelper.trim (aUBLBranch.getID ().getSchemeID ());
                }
                if (StringHelper.hasNoText (sBIC) || !RegExHelper.stringMatchesPattern (REGEX_BIC, sBIC))
                {
                  final FinancialInstitutionType aUBLFI = aUBLBranch.getFinancialInstitution ();
                  if (aUBLFI != null && StringHelper.hasText (aUBLFI.getID ().getValue ()))
                  {
                    bUseFI = true;
                    sBIC = StringHelper.trim (aUBLFI.getID ().getValue ());
                    sBICScheme = StringHelper.trim (aUBLFI.getID ().getSchemeID ());
                  }
                }

                if (StringHelper.hasText (sBIC) || StringHelper.hasText (sBICScheme))
                {
                  final boolean bIsBIC = isBIC (sBICScheme);
                  if (bIsBIC)
                    aEbiAccount.setBIC (sBIC);
                  else
                    aEbiAccount.setBankName (sBIC);

                  if (bIsBIC)
                    if (StringHelper.hasNoText (sBIC) || !RegExHelper.stringMatchesPattern (REGEX_BIC, sBIC))
                    {
                      aTransformationErrorList.add (SingleError.builderError ()
                                                               .errorFieldName ("PaymentMeans[" +
                                                                                nPaymentMeansIndex +
                                                                                "]/PayeeFinancialAccount/FinancialInstitutionBranch" +
                                                                                (bUseFI ? "/FinancialInstitution"
                                                                                        : "") +
                                                                                "/ID")
                                                               .errorText (EText.BIC_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                     sBIC))
                                                               .build ());
                      aEbiAccount.setBIC (null);
                    }
                }
              }
            }

            // IBAN
            final String sIBAN = aUBLFinancialAccount != null ? StringHelper.trim (aUBLFinancialAccount.getIDValue ())
                                                              : null;
            aEbiAccount.setIBAN (sIBAN);
            if (StringHelper.getLength (sIBAN) > IBAN_MAX_LENGTH)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .errorFieldName ("PaymentMeans[" +
                                                                        nPaymentMeansIndex +
                                                                        "]/PayeeFinancialAccount/ID")
                                                       .errorText (EText.IBAN_TOO_LONG_STRIPPING.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                         sIBAN,
                                                                                                                         Integer.valueOf (IBAN_MAX_LENGTH)))
                                                       .build ());
              aEbiAccount.setIBAN (sIBAN.substring (0, IBAN_MAX_LENGTH));
            }

            // Bank Account Owner - no field present - check PayeePart or
            // SupplierPartyName
            String sBankAccountOwnerName = aUBLFinancialAccount != null ? aUBLFinancialAccount.getNameValue () : null;
            if (StringHelper.hasNoText (sBankAccountOwnerName))
            {
              final PartyType aUBLPayeeParty = aUBLDocPayeeParty.get ();
              if (aUBLPayeeParty != null)
                for (final PartyNameType aPartyName : aUBLPayeeParty.getPartyName ())
                {
                  sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                  if (StringHelper.hasText (sBankAccountOwnerName))
                    break;
                }
            }
            if (StringHelper.hasNoText (sBankAccountOwnerName))
            {
              final PartyType aSupplierParty = aUBLDocAccountingSupplierParty.get ().getParty ();
              if (aSupplierParty != null)
                for (final PartyNameType aPartyName : aSupplierParty.getPartyName ())
                {
                  sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                  if (StringHelper.hasText (sBankAccountOwnerName))
                    break;
                }
            }
            aEbiAccount.setBankAccountOwner (sBankAccountOwnerName);

            aEbiUBTMethod.addBeneficiaryAccount (aEbiAccount);
            aEbiPaymentMethod.setUniversalBankTransaction (aEbiUBTMethod);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLDueDate);

            break;
          }

          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]")
                                                   .errorText (EText.PAYMENTMEANS_UNSUPPORTED_CHANNELCODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                                  sPaymentChannelCode))
                                                   .build ());
        }
        else
          if (isDirectDebit (sPaymentMeansCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi43DirectDebitType aEbiDirectDebit = new Ebi43DirectDebitType ();
            aEbiPaymentMethod.setDirectDebit (aEbiDirectDebit);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLDueDate);

            break;
          }
          else
            if (isSEPADirectDebit (sPaymentMeansCode))
            {
              _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);

              // Find SEPA fields
              final SEPADirectDebit aDD = extractSEPADirectDebit (aUBLDueDate,
                                                                  aUBLPaymentMeans,
                                                                  aUBLDocAccountingSupplierParty.get ().getParty (),
                                                                  aUBLDocPayeeParty.get ());

              if (StringHelper.hasText (aDD.m_sBIC) && !RegExHelper.stringMatchesPattern (REGEX_BIC, aDD.m_sBIC))
              {
                aTransformationErrorList.add (SingleError.builderError ()
                                                         .errorFieldName ("PaymentMeans[" +
                                                                          nPaymentMeansIndex +
                                                                          "]/PayeeFinancialAccount/FinancialInstitutionBranch" +
                                                                          (aDD.m_bUseBICFromFinancialInstitution ? "/FinancialInstitution"
                                                                                                                 : "") +
                                                                          "/ID")
                                                         .errorText (EText.BIC_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                               aDD.m_sBIC))
                                                         .build ());
              }

              // use SEPA fields
              final Ebi43SEPADirectDebitType aEbiDirectDebit = new Ebi43SEPADirectDebitType ();
              aEbiDirectDebit.setType (Ebi43SEPADirectDebitTypeType.B_2_B);
              aEbiDirectDebit.setBIC (aDD.m_sBIC);
              aEbiDirectDebit.setIBAN (aDD.m_sIBAN);
              aEbiDirectDebit.setBankAccountOwner (aDD.m_sBankAccountOwnerName);
              aEbiDirectDebit.setCreditorID (aDD.m_sCreditorID);
              aEbiDirectDebit.setMandateReference (aDD.m_sMandateReference);
              aEbiDirectDebit.setDebitCollectionDate (aDD.m_aDebitCollectionDate);
              aEbiPaymentMethod.setSEPADirectDebit (aEbiDirectDebit);
              aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

              // Set due date (optional)
              aEbiPaymentConditions.setDueDate (aUBLDueDate);

              break;
            }
            else
            {
              // No supported payment means code
              if (MathHelper.isEQ0 (aEbiDoc.getPayableAmount ()))
              {
                // As nothing is to be paid we can safely use NoPayment
                _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
                final Ebi43NoPaymentType aEbiNoPayment = new Ebi43NoPaymentType ();
                aEbiPaymentMethod.setNoPayment (aEbiNoPayment);
                break;
              }

              aTransformationErrorList.add (SingleError.builderError ()
                                                       .errorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]")
                                                       .errorText (EText.PAYMENTMEANS_CODE_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                           sPaymentMeansCode,
                                                                                                                           getOrString (", ",
                                                                                                                                        PAYMENT_MEANS_CREDIT_TRANSFER,
                                                                                                                                        PAYMENT_MEANS_DEBIT_TRANSFER,
                                                                                                                                        PAYMENT_MEANS_PAYMENT_TO_BANK_ACCOUNT,
                                                                                                                                        PAYMENT_MEANS_SEPA_CREDIT_TRANSFER),
                                                                                                                           getOrString (", ",
                                                                                                                                        PAYMENT_MEANS_DIRECT_DEBIT,
                                                                                                                                        PAYMENT_MEANS_SEPA_DIRECT_DEBIT)))
                                                       .build ());
            }

        ++nPaymentMeansIndex;
      }
    }

    if (aEbiDoc.getPaymentMethod () == null)
    {
      // No payment method found
      if (m_aSettings.isInvoicePaymentMethodMandatory ())
      {
        if (bIsCreditNote)
        {
          // Create a no-payment as fallback
          final Ebi43NoPaymentType aEbiNoPayment = new Ebi43NoPaymentType ();
          aEbiPaymentMethod.setNoPayment (aEbiNoPayment);
          aEbiDoc.setPaymentMethod (aEbiPaymentMethod);
        }
        else
        {
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .errorFieldName (bIsCreditNote ? "CreditNote" : "Invoice")
                                                   .errorText (EText.ERB_NO_PAYMENT_METHOD.getDisplayText (m_aDisplayLocale))
                                                   .build ());
        }
      }
    }

    // Payment terms
    {
      final ICommonsList <String> aPaymentConditionsNotes = new CommonsArrayList <> ();
      int nPaymentTermsIndex = 0;
      for (final PaymentTermsType aUBLPaymentTerms : aUBLDocPaymentTerms.get ())
      {
        // Add notes
        for (final NoteType aUBLNote : aUBLPaymentTerms.getNote ())
        {
          final String sUBLNote = StringHelper.trim (aUBLNote.getValue ());
          if (StringHelper.hasText (sUBLNote))
            aPaymentConditionsNotes.add (sUBLNote);
        }

        if (aUBLPaymentTerms.getPaymentDueDate () != null)
        {
          final XMLOffsetDate aUBLDueDate = aUBLPaymentTerms.getPaymentDueDateValue ();
          if (aUBLDueDate != null)
          {
            final XMLOffsetDate aEbiDueDate = aEbiPaymentConditions.getDueDate ();
            if (aEbiDueDate != null)
            {
              // Error only if due dates differ
              if (!aEbiDueDate.equals (aUBLDueDate))
                aTransformationErrorList.add (SingleError.builderWarn ()
                                                         .errorFieldName ("PaymentTerms[" +
                                                                          nPaymentTermsIndex +
                                                                          "]/PaymentDueDate")
                                                         .errorText (EText.PAYMENT_DUE_DATE_ALREADY_CONTAINED.getDisplayText (m_aDisplayLocale))
                                                         .build ());
            }
            else
              aEbiPaymentConditions.setDueDate (aUBLDueDate);
          }

          final BigDecimal aUBLPaymentPerc = aUBLPaymentTerms.getPaymentPercentValue ();
          if (aUBLPaymentPerc != null && MathHelper.isGT0 (aUBLPaymentPerc) && MathHelper.isLT100 (aUBLPaymentPerc))
          {
            final MonetaryTotalType aUBLTotal = aUBLDocLegalMonetaryTotal.get ();
            final BigDecimal aBaseAmount = aUBLTotal == null ? null : aUBLTotal.getPayableAmountValue ();
            if (aBaseAmount != null)
            {
              final BigDecimal aMinimumPayment = MathHelper.getPercentValue (aBaseAmount,
                                                                             aUBLPaymentPerc,
                                                                             SCALE_PRICE2,
                                                                             ROUNDING_MODE);
              aEbiPaymentConditions.setMinimumPayment (aMinimumPayment);
            }
          }
        }
        else
          if (aUBLPaymentTerms.getSettlementDiscountPercent () != null)
          {
            if (aUBLPaymentTerms.getSettlementPeriod () == null ||
                aUBLPaymentTerms.getSettlementPeriod ().getEndDate () == null)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .errorFieldName ("PaymentTerms[" +
                                                                        nPaymentTermsIndex +
                                                                        "]/SettlementPeriod")
                                                       .errorText (EText.SETTLEMENT_PERIOD_MISSING.getDisplayText (m_aDisplayLocale))
                                                       .build ());
            }
            else
            {
              final Ebi43DiscountType aEbiDiscount = new Ebi43DiscountType ();
              aEbiDiscount.setPaymentDate (aUBLPaymentTerms.getSettlementPeriod ().getEndDateValue ());
              aEbiDiscount.setPercentage (aUBLPaymentTerms.getSettlementDiscountPercentValue ());
              // Optional amount value
              aEbiDiscount.setAmount (aUBLPaymentTerms.getAmountValue ());
              aEbiPaymentConditions.addDiscount (aEbiDiscount);
            }
          }
          else
            if (aUBLPaymentTerms.getPenaltySurchargePercent () != null)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .errorFieldName ("PaymentTerms[" + nPaymentTermsIndex + "]")
                                                       .errorText (EText.PENALTY_NOT_ALLOWED.getDisplayText (m_aDisplayLocale))
                                                       .build ());
            }

        ++nPaymentTermsIndex;
      }

      if (!aPaymentConditionsNotes.isEmpty ())
        aEbiPaymentConditions.setComment (StringHelper.getImploded ('\n', aPaymentConditionsNotes));
    }

    if (aEbiPaymentConditions.getDueDate () == null)
    {
      // ebInterface requires due date
      if (aEbiPaymentConditions.hasDiscountEntries ())
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("PaymentMeans/PaymentDueDate")
                                                 .errorText (EText.DISCOUNT_WITHOUT_DUEDATE.getDisplayText (m_aDisplayLocale))
                                                 .build ());
    }
    else
    {
      // Independent if discounts are present or not
      aEbiDoc.setPaymentConditions (aEbiPaymentConditions);
    }
  }
}
