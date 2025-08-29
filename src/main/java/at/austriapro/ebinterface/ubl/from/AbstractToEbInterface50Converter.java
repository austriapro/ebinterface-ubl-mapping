/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2025 AUSTRIAPRO - www.austriapro.at
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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.numeric.BigHelper;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.cache.regex.RegExHelper;
import com.helger.collection.CollectionHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.datetime.xml.XMLOffsetDate;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.ebinterface.codelist.EFurtherIdentification;
import com.helger.ebinterface.v50.Ebi50AccountType;
import com.helger.ebinterface.v50.Ebi50AddressIdentifierType;
import com.helger.ebinterface.v50.Ebi50AddressType;
import com.helger.ebinterface.v50.Ebi50ContactType;
import com.helger.ebinterface.v50.Ebi50CountryType;
import com.helger.ebinterface.v50.Ebi50DeliveryType;
import com.helger.ebinterface.v50.Ebi50DiscountType;
import com.helger.ebinterface.v50.Ebi50DocumentTypeType;
import com.helger.ebinterface.v50.Ebi50FurtherIdentificationType;
import com.helger.ebinterface.v50.Ebi50InvoiceType;
import com.helger.ebinterface.v50.Ebi50NoPaymentType;
import com.helger.ebinterface.v50.Ebi50PaymentConditionsType;
import com.helger.ebinterface.v50.Ebi50PaymentMethodType;
import com.helger.ebinterface.v50.Ebi50PaymentReferenceType;
import com.helger.ebinterface.v50.Ebi50RelatedDocumentType;
import com.helger.ebinterface.v50.Ebi50SEPADirectDebitType;
import com.helger.ebinterface.v50.Ebi50SEPADirectDebitTypeType;
import com.helger.ebinterface.v50.Ebi50UniversalBankTransactionType;
import com.helger.text.locale.country.CountryCache;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentDescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionNoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.PaymentIDType;

/**
 * Base class for Peppol UBL to ebInterface 5.0 converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractToEbInterface50Converter extends AbstractToEbInterfaceConverter
{
  protected AbstractToEbInterface50Converter (@Nonnull final Locale aDisplayLocale,
                                              @Nonnull final Locale aContentLocale,
                                              @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale, aSettings);
  }

  public static void setAddressData (@Nullable final AddressType aUBLAddress,
                                     @Nonnull final Ebi50AddressType aEbiAddress,
                                     @Nonnull final Locale aContentLocale)
  {
    // Convert main address
    if (aUBLAddress != null)
    {
      aEbiAddress.setStreet (StringImplode.getImplodedNonEmpty (' ',
                                                                StringHelper.trim (aUBLAddress.getStreetNameValue ()),
                                                                StringHelper.trim (aUBLAddress.getBuildingNumberValue ())));
      aEbiAddress.setPOBox (StringHelper.trim (aUBLAddress.getPostboxValue ()));
      aEbiAddress.setTown (StringHelper.trim (aUBLAddress.getCityNameValue ()));
      aEbiAddress.setZIP (StringHelper.trim (aUBLAddress.getPostalZoneValue ()));

      // Country
      if (aUBLAddress.getCountry () != null)
      {
        final Ebi50CountryType aEbiCountry = new Ebi50CountryType ();
        final String sEbiCountryCode = StringHelper.trim (aUBLAddress.getCountry ().getIdentificationCodeValue ());
        aEbiCountry.setCountryCode (sEbiCountryCode);

        final String sCountryName = StringHelper.trim (aUBLAddress.getCountry ().getNameValue ());
        aEbiCountry.setValue (sCountryName);
        if (StringHelper.isEmpty (sCountryName) && StringHelper.isNotEmpty (sEbiCountryCode))
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

  public static void validateContactData (@Nonnull final Ebi50ContactType aEbiContact,
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
  public static Ebi50ContactType convertContact (@Nonnull final PartyType aUBLParty,
                                                 @Nonnull final String sPartyType,
                                                 @Nullable final String sAddressNameFallback,
                                                 @Nonnull final ErrorList aTransformationErrorList,
                                                 @Nonnull final Locale aDisplayLocale,
                                                 final boolean bValidate)
  {
    final ContactType aUBLContact = aUBLParty.getContact ();
    if (aUBLContact == null && aUBLParty.getPerson ().isEmpty ())
      return null;

    final Ebi50ContactType aEbiContact = new Ebi50ContactType ();

    if (aUBLContact != null)
    {
      final String sPhone = StringHelper.trim (aUBLContact.getTelephoneValue ());
      if (StringHelper.isNotEmpty (sPhone))
        aEbiContact.addPhone (sPhone);

      final String sEmail = StringHelper.trim (aUBLContact.getElectronicMailValue ());
      if (StringHelper.isNotEmpty (sEmail))
        aEbiContact.addEmail (sEmail);
    }

    // Person name
    final ICommonsList <String> ebContacts = new CommonsArrayList <> ();
    if (aUBLContact != null)
      if (StringHelper.isNotEmptyAfterTrim (aUBLContact.getNameValue ()))
        ebContacts.add (StringHelper.trim (aUBLContact.getNameValue ()));
    for (final PersonType aUBLPerson : aUBLParty.getPerson ())
    {
      if (StringHelper.isEmpty (aEbiContact.getSalutation ()))
        aEbiContact.setSalutation (StringHelper.trim (aUBLPerson.getGenderCodeValue ()));
      ebContacts.add (StringImplode.getImplodedNonEmpty (' ',
                                                         StringHelper.trim (aUBLPerson.getTitleValue ()),
                                                         StringHelper.trim (aUBLPerson.getFirstNameValue ()),
                                                         StringHelper.trim (aUBLPerson.getMiddleNameValue ()),
                                                         StringHelper.trim (aUBLPerson.getFamilyNameValue ()),
                                                         StringHelper.trim (aUBLPerson.getNameSuffixValue ())));
    }
    if (ebContacts.isNotEmpty ())
      aEbiContact.setName (StringImplode.getImplodedNonEmpty ('\n', ebContacts));
    if (aEbiContact.getName () == null)
      aEbiContact.setName (sAddressNameFallback);

    if (bValidate)
      validateContactData (aEbiContact, sPartyType, aTransformationErrorList, aDisplayLocale);

    return aEbiContact;
  }

  public static void validateAddressData (@Nonnull final Ebi50AddressType aEbiAddress,
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
  public static Ebi50AddressType convertParty (@Nonnull final PartyType aUBLParty,
                                               @Nonnull final String sPartyType,
                                               @Nonnull final ErrorList aTransformationErrorList,
                                               @Nonnull final Locale aContentLocale,
                                               @Nonnull final Locale aDisplayLocale,
                                               final boolean bValidate)
  {
    final Ebi50AddressType aEbiAddress = new Ebi50AddressType ();

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
      if (StringHelper.isNotEmpty (sEndpointID))
      {
        // We have an endpoint ID

        // Check all identifier types
        final String sSchemeIDToSearch = StringHelper.trim (aUBLParty.getEndpointID ().getSchemeID ());

        {
          final Ebi50AddressIdentifierType aEbiType = new Ebi50AddressIdentifierType ();
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
          final Ebi50AddressIdentifierType aEbiType = new Ebi50AddressIdentifierType ();
          aEbiType.setAddressIdentifierType (aUBLPartyID.getID ().getSchemeID ());
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
  protected static String getAggregated (@Nonnull final Collection <DescriptionType> aList)
  {
    return StringImplode.getImplodedMapped ('\n', aList, DescriptionType::getValue);
  }

  protected static boolean isAddressIncomplete (@Nonnull final Ebi50AddressType aEbiAddress)
  {
    return StringHelper.isEmpty (aEbiAddress.getName ()) ||
           StringHelper.isEmpty (aEbiAddress.getTown ()) ||
           StringHelper.isEmpty (aEbiAddress.getZIP ()) ||
           aEbiAddress.getCountry () == null;
  }

  @Nonnull
  public static Ebi50DeliveryType convertDelivery (@Nonnull final DeliveryType aUBLDelivery,
                                                   @Nonnull final String sDeliveryType,
                                                   @Nullable final CustomerPartyType aCustomerParty,
                                                   @Nonnull final ErrorList aTransformationErrorList,
                                                   @Nonnull final Locale aContentLocale,
                                                   @Nonnull final Locale aDisplayLocale)
  {
    final Ebi50DeliveryType aEbiDelivery = new Ebi50DeliveryType ();

    // Set the delivery ID
    aEbiDelivery.setDeliveryID (aUBLDelivery.getIDValue ());

    // Set the delivery date
    aEbiDelivery.setDate (aUBLDelivery.getActualDeliveryDateValue ());

    final PartyType aUBLParty = aUBLDelivery.getDeliveryParty ();
    Ebi50AddressType aEbiAddress = null;
    if (aUBLParty != null)
    {
      aEbiAddress = convertParty (aUBLParty,
                                  "DeliveryParty",
                                  aTransformationErrorList,
                                  aContentLocale,
                                  aDisplayLocale,
                                  false);
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
          aEbiAddress = new Ebi50AddressType ();
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
          aEbiAddress = new Ebi50AddressType ();
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
          if (StringHelper.isNotEmpty (sAddressName))
            break;
        }

      // As fallback use delivery location name
      if (StringHelper.isEmpty (sAddressName) && aUBLDeliveryLocation != null)
        sAddressName = StringHelper.trim (aUBLDeliveryLocation.getNameValue ());

      // As fallback use accounting customer party
      if (StringHelper.isEmpty (sAddressName) && aCustomerParty != null && aCustomerParty.getParty () != null)
      {
        for (final PartyNameType aUBLPartyName : aCustomerParty.getParty ().getPartyName ())
        {
          sAddressName = StringHelper.trim (aUBLPartyName.getNameValue ());
          if (StringHelper.isNotEmpty (sAddressName))
            break;
        }

        if (StringHelper.isEmpty (sAddressName))
        {
          // For EN invoices
          for (final PartyLegalEntityType aUBLPartyLegalEntity : aCustomerParty.getParty ().getPartyLegalEntity ())
          {
            sAddressName = StringHelper.trim (aUBLPartyLegalEntity.getRegistrationNameValue ());
            if (StringHelper.isNotEmpty (sAddressName))
              break;
          }
        }
      }
      aEbiAddress.setName (sAddressName);

      if (StringHelper.isEmpty (aEbiAddress.getName ()))
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName (sDeliveryType + "/DeliveryParty")
                                                 .errorText (EText.DELIVERY_WITHOUT_NAME.getDisplayText (aDisplayLocale))
                                                 .build ());
      validateAddressData (aEbiAddress, sDeliveryType + "/DeliveryParty", aTransformationErrorList, aDisplayLocale);
    }

    if (aUBLDelivery.hasDeliveryTermsEntries ())
    {
      final DeliveryTermsType aDeliveryTerms = aUBLDelivery.getDeliveryTermsAtIndex (0);
      if (aDeliveryTerms.hasSpecialTermsEntries ())
        aEbiDelivery.setDescription (aDeliveryTerms.getSpecialTermsAtIndex (0).getValue ());
    }

    return aEbiDelivery;
  }

  @Nullable
  protected static final Ebi50DocumentTypeType getAsDocumentTypeType (@Nullable final String... aValues)
  {
    if (aValues != null)
      for (final String s : aValues)
        if (s != null)
        {
          final String sClean = s.trim ();
          try
          {
            // The first match wins
            return Ebi50DocumentTypeType.fromValue (sClean);
          }
          catch (final IllegalArgumentException ex)
          {
            // Ignore
          }

          // Try the Invoice Type Codes
          if (INVOICE_TYPE_CODE_FINAL_PAYMENT.equals (sClean))
            return Ebi50DocumentTypeType.FINAL_SETTLEMENT;
          if (INVOICE_TYPE_CODE_PARTIAL.equals (sClean))
            return Ebi50DocumentTypeType.INVOICE_FOR_PARTIAL_DELIVERY;
          if (INVOICE_TYPE_CODE_PREPAYMENT_INVOICE.equals (sClean))
            return Ebi50DocumentTypeType.INVOICE_FOR_ADVANCE_PAYMENT;
          if (INVOICE_TYPE_CODE_SELF_BILLING.equals (sClean))
            return Ebi50DocumentTypeType.SELF_BILLING;
        }
    return null;
  }

  protected static void convertRelatedDocuments (@Nonnull final List <BillingReferenceType> aUBLBillingReferences,
                                                 @Nonnull final Ebi50InvoiceType aEbiDoc)
  {
    for (final BillingReferenceType aUBLBillingReference : aUBLBillingReferences)
    {
      if (aUBLBillingReference.getInvoiceDocumentReference () != null &&
          aUBLBillingReference.getInvoiceDocumentReference ().getIDValue () != null)
      {
        final Ebi50RelatedDocumentType aEbiRelatedDocument = new Ebi50RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getInvoiceDocumentReference ().getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getInvoiceDocumentReference ().getIssueDateValue ());
        aEbiRelatedDocument.setDocumentType (Ebi50DocumentTypeType.INVOICE);
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
      else
        if (aUBLBillingReference.getCreditNoteDocumentReference () != null &&
            aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue () != null)
        {
          final Ebi50RelatedDocumentType aEbiRelatedDocument = new Ebi50RelatedDocumentType ();
          aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue ());
          aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getCreditNoteDocumentReference ()
                                                                  .getIssueDateValue ());
          aEbiRelatedDocument.setDocumentType (Ebi50DocumentTypeType.CREDIT_MEMO);
          aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
        }
      // Ignore other values
    }
  }

  protected static void convertReferencedDocuments (@Nonnull final List <DocumentReferenceType> aUBLDocumentReferences,
                                                    @Nonnull final Ebi50InvoiceType aEbiDoc)
  {
    for (final DocumentReferenceType aUBLDocumentReference : aUBLDocumentReferences)
      if (StringHelper.isNotEmpty (aUBLDocumentReference.getIDValue ()) &&
          aUBLDocumentReference.getAttachment () == null)
      {
        final Ebi50RelatedDocumentType aEbiRelatedDocument = new Ebi50RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLDocumentReference.getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLDocumentReference.getIssueDateValue ());
        final ICommonsList <String> aComments = new CommonsArrayList <> ();
        for (final DocumentDescriptionType aUBLDocDesc : aUBLDocumentReference.getDocumentDescription ())
          aComments.add (aUBLDocDesc.getValue ());

        final String sComment = StringImplode.getImplodedNonEmpty ('\n', aComments);
        if (StringHelper.isNotEmpty (sComment))
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
                                               @Nonnull final Ebi50PaymentMethodType aEbiPaymentMethod)
  {
    if (aUBLPaymentMeans.hasInstructionNoteEntries ())
    {
      final ICommonsList <String> aNotes = new CommonsArrayList <> ();
      for (final InstructionNoteType aUBLNote : aUBLPaymentMeans.getInstructionNote ())
        aNotes.add (StringHelper.trim (aUBLNote.getValue ()));
      if (aNotes.isNotEmpty ())
        aEbiPaymentMethod.setComment (StringImplode.getImplodedNonEmpty ('\n', aNotes));
    }
  }

  protected void convertPayment (@Nonnull final Supplier <XMLOffsetDate> aUBLTopLevelDueDate,
                                 @Nonnull final Supplier <List <PaymentMeansType>> aUBLDocPaymentMeans,
                                 @Nonnull final Supplier <PartyType> aUBLDocPayeeParty,
                                 @Nonnull final Supplier <SupplierPartyType> aUBLDocAccountingSupplierParty,
                                 @Nonnull final Supplier <List <PaymentTermsType>> aUBLDocPaymentTerms,
                                 @Nonnull final Supplier <MonetaryTotalType> aUBLDocLegalMonetaryTotal,
                                 @Nonnull final ErrorList aTransformationErrorList,
                                 @Nonnull final Ebi50InvoiceType aEbiDoc,
                                 final boolean bIsCreditNote)
  {
    final Ebi50PaymentMethodType aEbiPaymentMethod = new Ebi50PaymentMethodType ();
    final Ebi50PaymentConditionsType aEbiPaymentConditions = new Ebi50PaymentConditionsType ();

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
            final Ebi50UniversalBankTransactionType aEbiUBTMethod = new Ebi50UniversalBankTransactionType ();

            // Find payment reference
            int nPaymentIDIndex = 0;
            for (final PaymentIDType aUBLPaymentID : aUBLPaymentMeans.getPaymentID ())
            {
              String sUBLPaymentID = StringHelper.trim (aUBLPaymentID.getValue ());
              if (StringHelper.isNotEmpty (sUBLPaymentID))
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

                final Ebi50PaymentReferenceType aEbiPaymentReference = new Ebi50PaymentReferenceType ();
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
                if (StringHelper.isNotEmpty (sUBLInstructionID))
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

                  final Ebi50PaymentReferenceType aEbiPaymentReference = new Ebi50PaymentReferenceType ();
                  aEbiPaymentReference.setValue (sUBLInstructionID);
                  aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
                }
              }
            }

            // Beneficiary account
            final Ebi50AccountType aEbiAccount = new Ebi50AccountType ();

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
                if (StringHelper.isEmpty (sBIC) || !RegExHelper.stringMatchesPattern (REGEX_BIC, sBIC))
                {
                  final FinancialInstitutionType aUBLFI = aUBLBranch.getFinancialInstitution ();
                  if (aUBLFI != null && StringHelper.isNotEmpty (aUBLFI.getID ().getValue ()))
                  {
                    bUseFI = true;
                    sBIC = StringHelper.trim (aUBLFI.getID ().getValue ());
                    sBICScheme = StringHelper.trim (aUBLFI.getID ().getSchemeID ());
                  }
                }

                if (StringHelper.isNotEmpty (sBIC))
                {
                  final boolean bIsBIC = isBIC (sBICScheme);
                  if (bIsBIC)
                    aEbiAccount.setBIC (sBIC);
                  else
                    aEbiAccount.setBankName (sBIC);

                  if (bIsBIC && !RegExHelper.stringMatchesPattern (REGEX_BIC, sBIC))
                  {
                    aTransformationErrorList.add (SingleError.builderError ()
                                                             .errorFieldName ("PaymentMeans[" +
                                                                              nPaymentMeansIndex +
                                                                              "]/PayeeFinancialAccount/FinancialInstitutionBranch" +
                                                                              (bUseFI ? "/FinancialInstitution" : "") +
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
            if (StringHelper.isEmpty (sBankAccountOwnerName))
            {
              final PartyType aUBLPayeeParty = aUBLDocPayeeParty.get ();
              if (aUBLPayeeParty != null)
                for (final PartyNameType aPartyName : aUBLPayeeParty.getPartyName ())
                {
                  sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                  if (StringHelper.isNotEmpty (sBankAccountOwnerName))
                    break;
                }
            }
            if (StringHelper.isEmpty (sBankAccountOwnerName))
            {
              final PartyType aSupplierParty = aUBLDocAccountingSupplierParty.get ().getParty ();
              if (aSupplierParty != null)
                for (final PartyNameType aPartyName : aSupplierParty.getPartyName ())
                {
                  sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                  if (StringHelper.isNotEmpty (sBankAccountOwnerName))
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
          if (isSEPADirectDebit (sPaymentMeansCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);

            // Find SEPA fields
            final SEPADirectDebit aDD = extractSEPADirectDebit (aUBLDueDate,
                                                                aUBLPaymentMeans,
                                                                aUBLDocAccountingSupplierParty.get ().getParty (),
                                                                aUBLDocPayeeParty.get ());

            if (StringHelper.isNotEmpty (aDD.m_sBIC) && !RegExHelper.stringMatchesPattern (REGEX_BIC, aDD.m_sBIC))
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
            final Ebi50SEPADirectDebitType aEbiDirectDebit = new Ebi50SEPADirectDebitType ();
            aEbiDirectDebit.setType (Ebi50SEPADirectDebitTypeType.B_2_B);
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
            if (BigHelper.isEQ0 (aEbiDoc.getPayableAmount ()))
            {
              // As nothing is to be paid we can safely use NoPayment
              _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
              final Ebi50NoPaymentType aEbiNoPayment = new Ebi50NoPaymentType ();
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
          final Ebi50NoPaymentType aEbiNoPayment = new Ebi50NoPaymentType ();
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
          if (StringHelper.isNotEmpty (sUBLNote))
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
          if (aUBLPaymentPerc != null && BigHelper.isGT0 (aUBLPaymentPerc) && BigHelper.isLT100 (aUBLPaymentPerc))
          {
            final MonetaryTotalType aUBLTotal = aUBLDocLegalMonetaryTotal.get ();
            final BigDecimal aBaseAmount = aUBLTotal == null ? null : aUBLTotal.getPayableAmountValue ();
            if (aBaseAmount != null)
            {
              final BigDecimal aMinimumPayment = BigHelper.getPercentValue (aBaseAmount,
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
              final Ebi50DiscountType aEbiDiscount = new Ebi50DiscountType ();
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
        aEbiPaymentConditions.setComment (StringImplode.getImploded ('\n', aPaymentConditionsNotes));
    }

    // Set due date alternative
    if (aEbiPaymentConditions.getDueDate () == null)
      aEbiPaymentConditions.setDueDate (aUBLTopLevelDueDate.get ());

    if (aEbiPaymentConditions.getDueDate () != null ||
        aEbiPaymentConditions.getMinimumPayment () != null ||
        aEbiPaymentConditions.hasDiscountEntries () ||
        StringHelper.isNotEmpty (aEbiPaymentConditions.getComment ()))
    {
      aEbiDoc.setPaymentConditions (aEbiPaymentConditions);
    }
  }

  @Nonnull
  protected static Ebi50FurtherIdentificationType createFurtherIdentification (@Nonnull final String sKey,
                                                                               @Nonnull final String sValue)
  {
    final Ebi50FurtherIdentificationType aEbiFurtherIdentification = new Ebi50FurtherIdentificationType ();
    aEbiFurtherIdentification.setIdentificationType (StringHelper.trim (sKey));
    aEbiFurtherIdentification.setValue (StringHelper.trim (sValue));
    return aEbiFurtherIdentification;
  }

  protected static void convertFurtherIdentifications (@Nonnull final List <PartyIdentificationType> aPartyIDs,
                                                       @Nonnull final Consumer <? super Ebi50FurtherIdentificationType> aFIConsumer)
  {
    for (final PartyIdentificationType aUBLPartyID : aPartyIDs)
    {
      final IDType aID = aUBLPartyID.getID ();
      if (aID != null)
      {
        final String sValue = aID.getValue ();
        if (StringHelper.isNotEmpty (sValue))
        {
          // Take all of those, as they were created in the reverse mapping
          if (FURTHER_IDENTIFICATION_SCHEME_NAME_EBI2UBL.equals (aID.getSchemeName ()))
          {
            // Make sure key is not empty
            final String sKey = orDefault (aID.getSchemeID (), EFurtherIdentification.CONTRACT.getID ());
            aFIConsumer.accept (createFurtherIdentification (sKey, aID.getValue ()));
          }
        }
      }
    }
  }
}
