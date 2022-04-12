/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2022 AUSTRIAPRO - www.austriapro.at
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
import com.helger.ebinterface.v60.Ebi60AccountType;
import com.helger.ebinterface.v60.Ebi60AddressIdentifierType;
import com.helger.ebinterface.v60.Ebi60AddressType;
import com.helger.ebinterface.v60.Ebi60ContactType;
import com.helger.ebinterface.v60.Ebi60CountryType;
import com.helger.ebinterface.v60.Ebi60DeliveryType;
import com.helger.ebinterface.v60.Ebi60DiscountType;
import com.helger.ebinterface.v60.Ebi60DocumentTypeType;
import com.helger.ebinterface.v60.Ebi60InvoiceType;
import com.helger.ebinterface.v60.Ebi60NoPaymentType;
import com.helger.ebinterface.v60.Ebi60PaymentConditionsType;
import com.helger.ebinterface.v60.Ebi60PaymentMethodType;
import com.helger.ebinterface.v60.Ebi60PaymentReferenceType;
import com.helger.ebinterface.v60.Ebi60RelatedDocumentType;
import com.helger.ebinterface.v60.Ebi60SEPADirectDebitType;
import com.helger.ebinterface.v60.Ebi60SEPADirectDebitTypeType;
import com.helger.ebinterface.v60.Ebi60UniversalBankTransactionType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BillingReferenceType;
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
 * Base class for Peppol UBL to ebInterface 6.0 converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractToEbInterface60Converter extends AbstractToEbInterfaceConverter
{
  public static final int PAYMENT_REFERENCE_MAX_LENGTH = 35;

  public AbstractToEbInterface60Converter (@Nonnull final Locale aDisplayLocale,
                                           @Nonnull final Locale aContentLocale,
                                           @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale, aSettings);
  }

  public static void setAddressData (@Nullable final AddressType aUBLAddress,
                                     @Nonnull final Ebi60AddressType aEbiAddress,
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
        final Ebi60CountryType aEbiCountry = new Ebi60CountryType ();
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

  public static void validateContactData (@Nonnull final Ebi60ContactType aEbiContact,
                                          @Nonnull final String sPartyType,
                                          @Nonnull final ErrorList aTransformationErrorList,
                                          @Nonnull final Locale aDisplayLocale)
  {
    if (aEbiContact.getName () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName (sPartyType + "/Contact/Name")
                                               .errorText (EText.CONTACT_NO_NAME.getDisplayText (aDisplayLocale))
                                               .build ());
  }

  @Nullable
  public static Ebi60ContactType convertContact (@Nonnull final PartyType aUBLParty,
                                                 @Nonnull final String sPartyType,
                                                 @Nullable final String sAddressNameFallback,
                                                 @Nonnull final ErrorList aTransformationErrorList,
                                                 @Nonnull final Locale aDisplayLocale,
                                                 final boolean bValidate)
  {
    final ContactType aUBLContact = aUBLParty.getContact ();
    if (aUBLContact == null && aUBLParty.getPerson ().isEmpty ())
      return null;

    final Ebi60ContactType aEbiContact = new Ebi60ContactType ();

    if (aUBLContact != null)
    {
      final String sPhone = StringHelper.trim (aUBLContact.getTelephoneValue ());
      if (StringHelper.hasText (sPhone))
        aEbiContact.addPhone (sPhone);

      final String sEmail = StringHelper.trim (aUBLContact.getElectronicMailValue ());
      if (StringHelper.hasText (sEmail))
        aEbiContact.addEmail (sEmail);
    }

    // Person name
    final ICommonsList <String> ebContacts = new CommonsArrayList <> ();
    if (aUBLContact != null)
      if (StringHelper.hasTextAfterTrim (aUBLContact.getNameValue ()))
        ebContacts.add (StringHelper.trim (aUBLContact.getNameValue ()));
    for (final PersonType aUBLPerson : aUBLParty.getPerson ())
    {
      if (StringHelper.hasNoText (aEbiContact.getSalutation ()))
        aEbiContact.setSalutation (StringHelper.trim (aUBLPerson.getGenderCodeValue ()));
      ebContacts.add (StringHelper.getImplodedNonEmpty (' ',
                                                        StringHelper.trim (aUBLPerson.getTitleValue ()),
                                                        StringHelper.trim (aUBLPerson.getFirstNameValue ()),
                                                        StringHelper.trim (aUBLPerson.getMiddleNameValue ()),
                                                        StringHelper.trim (aUBLPerson.getFamilyNameValue ()),
                                                        StringHelper.trim (aUBLPerson.getNameSuffixValue ())));
    }
    if (ebContacts.isNotEmpty ())
      aEbiContact.setName (StringHelper.getImplodedNonEmpty ('\n', ebContacts));
    if (aEbiContact.getName () == null)
      aEbiContact.setName (sAddressNameFallback);

    if (bValidate)
      validateContactData (aEbiContact, sPartyType, aTransformationErrorList, aDisplayLocale);

    return aEbiContact;
  }

  public static void validateAddressData (@Nonnull final Ebi60AddressType aEbiAddress,
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
                                               .errorFieldName (sPartyType + "/PostalAddress/Country/IdentificationCode")
                                               .errorText (EText.ADDRESS_NO_COUNTRY.getDisplayText (aDisplayLocale))
                                               .build ());
  }

  @Nonnull
  public static Ebi60AddressType convertParty (@Nonnull final PartyType aUBLParty,
                                               @Nonnull final String sPartyType,
                                               @Nonnull final ErrorList aTransformationErrorList,
                                               @Nonnull final Locale aContentLocale,
                                               @Nonnull final Locale aDisplayLocale,
                                               final boolean bValidate)
  {
    final Ebi60AddressType aEbiAddress = new Ebi60AddressType ();

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

    // GLN and DUNS number
    if (aUBLParty.getEndpointID () != null)
    {
      final String sEndpointID = StringHelper.trim (aUBLParty.getEndpointIDValue ());
      if (StringHelper.hasText (sEndpointID))
      {
        // We have an endpoint ID

        // Check all identifier types
        final String sSchemeIDToSearch = StringHelper.trim (aUBLParty.getEndpointID ().getSchemeID ());

        {
          final Ebi60AddressIdentifierType aEbiType = new Ebi60AddressIdentifierType ();
          aEbiType.setAddressIdentifierType (sSchemeIDToSearch);
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
        {
          // Add GLN/DUNS number
          final Ebi60AddressIdentifierType aEbiType = new Ebi60AddressIdentifierType ();
          aEbiType.setAddressIdentifierType (aUBLPartyID.getID ().getSchemeID ());
          aEbiType.setValue (sUBLPartyID);
          aEbiAddress.addAddressIdentifier (aEbiType);
        }
        if (aEbiAddress.hasNoAddressIdentifierEntries ())
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName (sPartyType + "/PartyIdentification[" + nPartyIdentificationIndex + "]")
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

  protected static boolean isAddressIncomplete (@Nonnull final Ebi60AddressType aEbiAddress)
  {
    return StringHelper.hasNoText (aEbiAddress.getName ()) ||
           StringHelper.hasNoText (aEbiAddress.getTown ()) ||
           StringHelper.hasNoText (aEbiAddress.getZIP ()) ||
           aEbiAddress.getCountry () == null;
  }

  @Nonnull
  public static Ebi60DeliveryType convertDelivery (@Nonnull final DeliveryType aUBLDelivery,
                                                   @Nonnull final String sDeliveryType,
                                                   @Nullable final CustomerPartyType aCustomerParty,
                                                   @Nonnull final ErrorList aTransformationErrorList,
                                                   @Nonnull final Locale aContentLocale,
                                                   @Nonnull final Locale aDisplayLocale)
  {
    final Ebi60DeliveryType aEbiDelivery = new Ebi60DeliveryType ();

    // Set the delivery ID
    aEbiDelivery.setDeliveryID (aUBLDelivery.getIDValue ());

    // Set the delivery date
    aEbiDelivery.setDate (aUBLDelivery.getActualDeliveryDateValue ());

    final PartyType aUBLParty = aUBLDelivery.getDeliveryParty ();
    Ebi60AddressType aEbiAddress = null;
    if (aUBLParty != null)
    {
      aEbiAddress = convertParty (aUBLParty, "DeliveryParty", aTransformationErrorList, aContentLocale, aDisplayLocale, false);
      aEbiDelivery.setAddress (aEbiAddress);

      aEbiDelivery.setContact (convertContact (aUBLParty,
                                               "DeliveryParty",
                                               aEbiAddress.getName (),
                                               aTransformationErrorList,
                                               aDisplayLocale,
                                               true));
    }

    // Address present?
    if (aEbiAddress == null || isAddressIncomplete (aEbiAddress))
    {
      final AddressType aUBLDeliveryAddress = aUBLDelivery.getDeliveryAddress ();
      if (aUBLDeliveryAddress != null)
      {
        if (aEbiAddress == null)
          aEbiAddress = new Ebi60AddressType ();
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
          aEbiAddress = new Ebi60AddressType ();
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
  protected static final Ebi60DocumentTypeType getAsDocumentTypeType (@Nullable final String... aValues)
  {
    if (aValues != null)
      for (final String s : aValues)
        if (s != null)
        {
          final String sClean = s.trim ();
          try
          {
            // The first match wins
            return Ebi60DocumentTypeType.fromValue (sClean);
          }
          catch (final IllegalArgumentException ex)
          {
            // Ignore
          }

          // Try the Invoice Type Codes
          if (INVOICE_TYPE_CODE_PARTIAL.equals (sClean))
            return Ebi60DocumentTypeType.INVOICE_FOR_PARTIAL_DELIVERY;
          if (INVOICE_TYPE_CODE_PREPAYMENT_INVOICE.equals (sClean))
            return Ebi60DocumentTypeType.INVOICE_FOR_ADVANCE_PAYMENT;
          if (INVOICE_TYPE_CODE_SELF_BILLING.equals (sClean))
            return Ebi60DocumentTypeType.SELF_BILLING;
        }
    return null;
  }

  protected static void convertRelatedDocuments (@Nonnull final List <BillingReferenceType> aUBLBillingReferences,
                                                 @Nonnull final Ebi60InvoiceType aEbiDoc)
  {
    for (final BillingReferenceType aUBLBillingReference : aUBLBillingReferences)
    {
      if (aUBLBillingReference.getInvoiceDocumentReference () != null &&
          aUBLBillingReference.getInvoiceDocumentReference ().getIDValue () != null)
      {
        final Ebi60RelatedDocumentType aEbiRelatedDocument = new Ebi60RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getInvoiceDocumentReference ().getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getInvoiceDocumentReference ().getIssueDateValue ());
        aEbiRelatedDocument.setDocumentType (Ebi60DocumentTypeType.INVOICE);
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
      else
        if (aUBLBillingReference.getCreditNoteDocumentReference () != null &&
            aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue () != null)
        {
          final Ebi60RelatedDocumentType aEbiRelatedDocument = new Ebi60RelatedDocumentType ();
          aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue ());
          aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getCreditNoteDocumentReference ().getIssueDateValue ());
          aEbiRelatedDocument.setDocumentType (Ebi60DocumentTypeType.CREDIT_MEMO);
          aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
        }
      // Ignore other values
    }
  }

  protected static void convertReferencedDocuments (@Nonnull final List <DocumentReferenceType> aUBLDocumentReferences,
                                                    @Nonnull final Ebi60InvoiceType aEbiDoc)
  {
    for (final DocumentReferenceType aUBLDocumentReference : aUBLDocumentReferences)
      if (StringHelper.hasText (aUBLDocumentReference.getIDValue ()) && aUBLDocumentReference.getAttachment () == null)
      {
        final Ebi60RelatedDocumentType aEbiRelatedDocument = new Ebi60RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLDocumentReference.getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLDocumentReference.getIssueDateValue ());
        final ICommonsList <String> aComments = new CommonsArrayList <> ();
        for (final DocumentDescriptionType aUBLDocDesc : aUBLDocumentReference.getDocumentDescription ())
          aComments.add (aUBLDocDesc.getValue ());
        aEbiRelatedDocument.setComment (StringHelper.getImplodedNonEmpty ('\n', aComments));
        if (aUBLDocumentReference.getDocumentTypeCode () != null)
        {
          aEbiRelatedDocument.setDocumentType (getAsDocumentTypeType (aUBLDocumentReference.getDocumentTypeCode ().getName (),
                                                                      aUBLDocumentReference.getDocumentTypeCodeValue ()));
        }
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
  }

  private static void _setPaymentMeansComment (@Nonnull final PaymentMeansType aUBLPaymentMeans,
                                               @Nonnull final Ebi60PaymentMethodType aEbiPaymentMethod)
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
                                 @Nonnull final Ebi60InvoiceType aEbiDoc,
                                 final boolean bIsCreditNote)
  {
    final Ebi60PaymentMethodType aEbiPaymentMethod = new Ebi60PaymentMethodType ();
    final Ebi60PaymentConditionsType aEbiPaymentConditions = new Ebi60PaymentConditionsType ();

    {
      int nPaymentMeansIndex = 0;
      for (final PaymentMeansType aUBLPaymentMeans : aUBLDocPaymentMeans.get ())
      {
        // https://www.unece.org/trade/untdid/d16b/tred/tred4461.htm
        final String sPaymentMeansCode = StringHelper.trim (aUBLPaymentMeans.getPaymentMeansCodeValue ());
        if (isUniversalBankTransaction (sPaymentMeansCode))
        {
          // Is a payment channel code present?
          final String sPaymentChannelCode = StringHelper.trim (aUBLPaymentMeans.getPaymentChannelCodeValue ());
          if (isIBAN (sPaymentChannelCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi60UniversalBankTransactionType aEbiUBTMethod = new Ebi60UniversalBankTransactionType ();

            // Find payment reference
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
                                                           .errorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]/InstructionID")
                                                           .errorText (EText.PAYMENT_ID_TOO_LONG_CUT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                             sUBLInstructionID))
                                                           .build ());
                  sUBLInstructionID = sUBLInstructionID.substring (0, PAYMENT_REFERENCE_MAX_LENGTH);
                }

                final Ebi60PaymentReferenceType aEbiPaymentReference = new Ebi60PaymentReferenceType ();
                aEbiPaymentReference.setValue (sUBLInstructionID);
                aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
              }
            }

            if (aEbiUBTMethod.getPaymentReference () == null)
            {
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

                  final Ebi60PaymentReferenceType aEbiPaymentReference = new Ebi60PaymentReferenceType ();
                  aEbiPaymentReference.setValue (sUBLPaymentID);
                  aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
                }
                ++nPaymentIDIndex;
              }
            }

            // Beneficiary account
            final Ebi60AccountType aEbiAccount = new Ebi60AccountType ();

            // BIC
            final FinancialAccountType aUBLFinancialAccount = aUBLPaymentMeans.getPayeeFinancialAccount ();
            if (aUBLFinancialAccount != null &&
                aUBLFinancialAccount.getFinancialInstitutionBranch () != null &&
                aUBLFinancialAccount.getFinancialInstitutionBranch ().getFinancialInstitution () != null)
            {
              final FinancialInstitutionType aUBLFI = aUBLFinancialAccount.getFinancialInstitutionBranch ().getFinancialInstitution ();
              if (aUBLFI.getID () != null)
              {
                final String sID = StringHelper.trim (aUBLFI.getID ().getValue ());
                final String sScheme = StringHelper.trim (aUBLFI.getID ().getSchemeID ());
                final boolean bIsBIC = isBIC (sScheme);

                if (bIsBIC)
                  aEbiAccount.setBIC (sID);
                else
                  aEbiAccount.setBankName (sID);

                if (bIsBIC)
                  if (StringHelper.hasNoText (sID) || !RegExHelper.stringMatchesPattern (REGEX_BIC, sID))
                  {
                    aTransformationErrorList.add (SingleError.builderError ()
                                                             .errorFieldName ("PaymentMeans[" +
                                                                              nPaymentMeansIndex +
                                                                              "]/PayeeFinancialAccount/FinancialInstitutionBranch/FinancialInstitution/ID")
                                                             .errorText (EText.BIC_INVALID.getDisplayTextWithArgs (m_aDisplayLocale, sID))
                                                             .build ());
                    aEbiAccount.setBIC (null);
                  }
              }
            }

            // IBAN
            final String sIBAN = aUBLFinancialAccount != null ? StringHelper.trim (aUBLFinancialAccount.getIDValue ()) : null;
            aEbiAccount.setIBAN (sIBAN);
            if (StringHelper.getLength (sIBAN) > IBAN_MAX_LENGTH)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .errorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]/PayeeFinancialAccount/ID")
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
            aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

            break;
          }

          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]")
                                                   .errorText (EText.PAYMENTMEANS_UNSUPPORTED_CHANNELCODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                                  sPaymentChannelCode))
                                                   .build ());
        }
        else
          if (isSEPADirectDebit (sPaymentMeansCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            // TODO use SEPA fields
            final Ebi60SEPADirectDebitType aEbiDirectDebit = new Ebi60SEPADirectDebitType ();
            aEbiDirectDebit.setType (Ebi60SEPADirectDebitTypeType.B_2_C);
            aEbiDirectDebit.setBIC (null);
            aEbiDirectDebit.setIBAN (null);
            aEbiDirectDebit.setBankAccountOwner (null);
            aEbiDirectDebit.setCreditorID (null);
            aEbiDirectDebit.setMandateReference (null);
            aEbiDirectDebit.setDebitCollectionDate ((XMLOffsetDate) null);
            aEbiPaymentMethod.setSEPADirectDebit (aEbiDirectDebit);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

            break;
          }
          else
          {
            // No supported payment means code
            if (MathHelper.isEQ0 (aEbiDoc.getPayableAmount ()))
            {
              // As nothing is to be paid we can safely use NoPayment
              _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
              final Ebi60NoPaymentType aEbiNoPayment = new Ebi60NoPaymentType ();
              aEbiPaymentMethod.setNoPayment (aEbiNoPayment);
              break;
            }

            aTransformationErrorList.add (SingleError.builderError ()
                                                     .errorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]")
                                                     .errorText (EText.PAYMENTMEANS_CODE_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                         sPaymentMeansCode,
                                                                                                                         getOrString (", ",
                                                                                                                                      "30",
                                                                                                                                      "31",
                                                                                                                                      "42",
                                                                                                                                      "58"),
                                                                                                                         getOrString (", ",
                                                                                                                                      "59")))
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
          final Ebi60NoPaymentType aEbiNoPayment = new Ebi60NoPaymentType ();
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

    // Set due date alternative
    if (aEbiPaymentConditions.getDueDate () == null)
      aEbiPaymentConditions.setDueDate (aUBLTopLevelDueDate.get ());

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
          final XMLOffsetDate aEbiDueDate = aEbiPaymentConditions.getDueDate ();
          if (aUBLDueDate != null && aEbiDueDate != null)
          {
            // Error only if due dates differ
            if (!aEbiDueDate.equals (aUBLDueDate))
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .errorFieldName ("PaymentTerms[" + nPaymentTermsIndex + "]/PaymentDueDate")
                                                       .errorText (EText.PAYMENT_DUE_DATE_ALREADY_CONTAINED.getDisplayText (m_aDisplayLocale))
                                                       .build ());
          }
          else
            aEbiPaymentConditions.setDueDate (aUBLDueDate);

          final BigDecimal aUBLPaymentPerc = aUBLPaymentTerms.getPaymentPercentValue ();
          if (aUBLPaymentPerc != null && MathHelper.isGT0 (aUBLPaymentPerc) && MathHelper.isLT100 (aUBLPaymentPerc))
          {
            final MonetaryTotalType aUBLTotal = aUBLDocLegalMonetaryTotal.get ();
            final BigDecimal aBaseAmount = aUBLTotal == null ? null : aUBLTotal.getPayableAmountValue ();
            if (aBaseAmount != null)
            {
              final BigDecimal aMinimumPayment = MathHelper.getPercentValue (aBaseAmount, aUBLPaymentPerc, SCALE_PRICE2, ROUNDING_MODE);
              aEbiPaymentConditions.setMinimumPayment (aMinimumPayment);
            }
          }
        }
        else
          if (aUBLPaymentTerms.getSettlementDiscountPercent () != null)
          {
            if (aUBLPaymentTerms.getSettlementPeriod () == null || aUBLPaymentTerms.getSettlementPeriod ().getEndDate () == null)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .errorFieldName ("PaymentTerms[" + nPaymentTermsIndex + "]/SettlementPeriod")
                                                       .errorText (EText.SETTLEMENT_PERIOD_MISSING.getDisplayText (m_aDisplayLocale))
                                                       .build ());
            }
            else
            {
              final Ebi60DiscountType aEbiDiscount = new Ebi60DiscountType ();
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
