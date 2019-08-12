/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2019 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.from;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.locale.country.CountryCache;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.ubl.from.AbstractToEbInterfaceConverter.EText;
import com.helger.ebinterface.v50.Ebi50AddressIdentifierType;
import com.helger.ebinterface.v50.Ebi50AddressType;
import com.helger.ebinterface.v50.Ebi50ContactType;
import com.helger.ebinterface.v50.Ebi50CountryType;
import com.helger.ebinterface.v50.Ebi50DeliveryType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyIdentificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyLegalEntityType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PersonType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;

public final class EbInterface50Helper
{
  private EbInterface50Helper ()
  {}

  public static void setAddressData (@Nullable final AddressType aUBLAddress,
                                     @Nonnull final Ebi50AddressType aEbiAddress,
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
        final Ebi50CountryType aEbiCountry = new Ebi50CountryType ();
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

  @Nullable
  public static Ebi50ContactType convertContact (@Nullable final ContactType aUBLContact,
                                                 @Nonnull final List <PersonType> aUBLPersons,
                                                 @Nonnull final String sPartyType,
                                                 @Nonnull final ErrorList aTransformationErrorList,
                                                 @Nonnull final Locale aDisplayLocale,
                                                 final boolean bValidate)
  {
    if (aUBLContact == null && aUBLPersons.isEmpty ())
      return null;

    final Ebi50ContactType aEbiContact = new Ebi50ContactType ();

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
    for (final PersonType aUBLPerson : aUBLPersons)
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

    if (bValidate)
    {
      if (aEbiContact.getName () == null)
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName (sPartyType + "/Contact/Name")
                                                 .setErrorText (EText.CONTACT_NO_NAME.getDisplayText (aDisplayLocale))
                                                 .build ());
    }

    return aEbiContact;
  }

  public static void validateAddressData (@Nonnull final Ebi50AddressType aEbiAddress,
                                          @Nonnull final String sPartyType,
                                          @Nonnull final ErrorList aTransformationErrorList,
                                          @Nonnull final Locale aDisplayLocale)
  {
    if (aEbiAddress.getStreet () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName (sPartyType + "/PostalAddress/StreetName")
                                               .setErrorText (EText.ADDRESS_NO_STREET.getDisplayText (aDisplayLocale))
                                               .build ());
    if (aEbiAddress.getTown () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName (sPartyType + "/PostalAddress/CityName")
                                               .setErrorText (EText.ADDRESS_NO_CITY.getDisplayText (aDisplayLocale))
                                               .build ());
    if (aEbiAddress.getZIP () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName (sPartyType + "/PostalAddress/PostalZone")
                                               .setErrorText (EText.ADDRESS_NO_ZIPCODE.getDisplayText (aDisplayLocale))
                                               .build ());
    if (aEbiAddress.getCountry () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName (sPartyType +
                                                                   "/PostalAddress/Country/IdentificationCode")
                                               .setErrorText (EText.ADDRESS_NO_COUNTRY.getDisplayText (aDisplayLocale))
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
                                               .setErrorFieldName (sPartyType + "/PartyName")
                                               .setErrorText (EText.MULTIPLE_PARTIES.getDisplayText (aDisplayLocale))
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
                                               .setErrorFieldName (sPartyType)
                                               .setErrorText (EText.PARTY_NO_NAME.getDisplayText (aDisplayLocale))
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
          final Ebi50AddressIdentifierType aEbiType = new Ebi50AddressIdentifierType ();
          aEbiType.setAddressIdentifierType (sSchemeIDToSearch);
          aEbiType.setValue (sEndpointID);
          aEbiAddress.getAddressIdentifier ().add (aEbiType);
        }

        if (aEbiAddress.hasNoAddressIdentifierEntries ())
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .setErrorFieldName (sPartyType)
                                                   .setErrorText (EText.PARTY_UNSUPPORTED_ENDPOINT.getDisplayTextWithArgs (aDisplayLocale,
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
          aEbiAddress.getAddressIdentifier ().add (aEbiType);
        }
        if (aEbiAddress.hasNoAddressIdentifierEntries ())
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .setErrorFieldName (sPartyType +
                                                                       "/PartyIdentification[" +
                                                                       nPartyIdentificationIndex +
                                                                       "]")
                                                   .setErrorText (EText.PARTY_UNSUPPORTED_ADDRESS_IDENTIFIER.getDisplayTextWithArgs (aDisplayLocale,
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

  protected static boolean isAddressIncomplete (@Nonnull final Ebi50AddressType aEbiAddress)
  {
    return StringHelper.hasNoText (aEbiAddress.getName ()) ||
           StringHelper.hasNoText (aEbiAddress.getTown ()) ||
           StringHelper.hasNoText (aEbiAddress.getZIP ()) ||
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
                                                 .setErrorFieldName (sDeliveryType + "/DeliveryParty")
                                                 .setErrorText (EText.DELIVERY_WITHOUT_NAME.getDisplayText (aDisplayLocale))
                                                 .build ());
      validateAddressData (aEbiAddress, sDeliveryType + "/DeliveryParty", aTransformationErrorList, aDisplayLocale);
    }

    return aEbiDelivery;
  }
}
