/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015 AUSTRIAPRO - www.austriapro.at
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
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.errorlist.ErrorList;
import com.helger.commons.locale.country.CountryCache;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.ubl.from.AbstractConverter.EText;
import com.helger.ebinterface.v42.Ebi42AddressIdentifierType;
import com.helger.ebinterface.v42.Ebi42AddressIdentifierTypeType;
import com.helger.ebinterface.v42.Ebi42AddressType;
import com.helger.ebinterface.v42.Ebi42CountryType;
import com.helger.ebinterface.v42.Ebi42DeliveryType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyIdentificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PersonType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;

public final class EbInterface42Helper
{
  private EbInterface42Helper ()
  {}

  public static void setAddressData (@Nullable final AddressType aUBLAddress,
                                     @Nonnull final Ebi42AddressType aEbiAddress,
                                     @Nonnull final String sPartyType,
                                     @Nonnull final ErrorList aTransformationErrorList,
                                     @Nonnull final Locale aContentLocale,
                                     @Nonnull final Locale aDisplayLocale)
  {
    final boolean bCountryErrorMsgEmitted = false;

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
        final Ebi42CountryType aEbiCountry = new Ebi42CountryType ();
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

    if (aEbiAddress.getStreet () == null)
      aTransformationErrorList.addError (sPartyType +
                                         "/PostalAddress/StreetName",
                                         EText.ADDRESS_NO_STREET.getDisplayText (aDisplayLocale));
    if (aEbiAddress.getTown () == null)
      aTransformationErrorList.addError (sPartyType +
                                         "/PostalAddress/CityName",
                                         EText.ADDRESS_NO_CITY.getDisplayText (aDisplayLocale));
    if (aEbiAddress.getZIP () == null)
      aTransformationErrorList.addError (sPartyType +
                                         "/PostalAddress/PostalZone",
                                         EText.ADDRESS_NO_ZIPCODE.getDisplayText (aDisplayLocale));
    if (aEbiAddress.getCountry () == null && !bCountryErrorMsgEmitted)
      aTransformationErrorList.addError (sPartyType +
                                         "/PostalAddress/Country/IdentificationCode",
                                         EText.ADDRESS_NO_COUNTRY.getDisplayText (aDisplayLocale));
  }

  @Nonnull
  public static Ebi42AddressType convertParty (@Nonnull final PartyType aUBLParty,
                                               @Nonnull final String sPartyType,
                                               @Nonnull final ErrorList aTransformationErrorList,
                                               @Nonnull final Locale aContentLocale,
                                               @Nonnull final Locale aDisplayLocale)
  {
    final Ebi42AddressType aEbiAddress = new Ebi42AddressType ();

    if (aUBLParty.getPartyNameCount () > 1)
      aTransformationErrorList.addWarning (sPartyType +
                                           "/PartyName",
                                           EText.MULTIPLE_PARTIES.getDisplayText (aDisplayLocale));

    // Convert name
    final PartyNameType aUBLPartyName = CollectionHelper.getAtIndex (aUBLParty.getPartyName (), 0);
    if (aUBLPartyName != null)
      aEbiAddress.setName (StringHelper.trim (aUBLPartyName.getNameValue ()));

    if (aEbiAddress.getName () == null)
      aTransformationErrorList.addError (sPartyType, EText.PARTY_NO_NAME.getDisplayText (aDisplayLocale));

    // Convert main address
    setAddressData (aUBLParty.getPostalAddress (),
                    aEbiAddress,
                    sPartyType,
                    aTransformationErrorList,
                    aContentLocale,
                    aDisplayLocale);

    // Contact
    final ContactType aUBLContact = aUBLParty.getContact ();
    if (aUBLContact != null)
    {
      aEbiAddress.setPhone (StringHelper.trim (aUBLContact.getTelephoneValue ()));
      aEbiAddress.setEmail (StringHelper.trim (aUBLContact.getElectronicMailValue ()));
    }

    // Person name
    final ICommonsList <String> ebContacts = new CommonsArrayList<> ();
    for (final PersonType aUBLPerson : aUBLParty.getPerson ())
    {
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

        for (final Ebi42AddressIdentifierTypeType eType : Ebi42AddressIdentifierTypeType.values ())
          if (eType.value ().equalsIgnoreCase (sSchemeIDToSearch))
          {
            final Ebi42AddressIdentifierType aEbiType = new Ebi42AddressIdentifierType ();
            aEbiType.setAddressIdentifierType (eType);
            aEbiType.setValue (sEndpointID);
            aEbiAddress.getAddressIdentifier ().add (aEbiType);
          }

        if (aEbiAddress.hasNoAddressIdentifierEntries ())
          aTransformationErrorList.addWarning (sPartyType,
                                               EText.PARTY_UNSUPPORTED_ENDPOINT.getDisplayTextWithArgs (aDisplayLocale,
                                                                                                        sEndpointID,
                                                                                                        aUBLParty.getEndpointID ()
                                                                                                                 .getSchemeID ()));
      }
    }

    if (aEbiAddress.hasNoAddressIdentifierEntries ())
    {
      // check party identification
      int nPartyIdentificationIndex = 0;
      for (final PartyIdentificationType aUBLPartyID : aUBLParty.getPartyIdentification ())
      {
        final String sUBLPartyID = StringHelper.trim (aUBLPartyID.getIDValue ());
        for (final Ebi42AddressIdentifierTypeType eType : Ebi42AddressIdentifierTypeType.values ())
          if (eType.value ().equalsIgnoreCase (aUBLPartyID.getID ().getSchemeID ()))
          {
            // Add GLN/DUNS number
            final Ebi42AddressIdentifierType aEbiType = new Ebi42AddressIdentifierType ();
            aEbiType.setAddressIdentifierType (eType);
            aEbiType.setValue (sUBLPartyID);
            aEbiAddress.getAddressIdentifier ().add (aEbiType);
          }
        if (aEbiAddress.hasNoAddressIdentifierEntries ())
          aTransformationErrorList.addWarning (sPartyType +
                                               "/PartyIdentification[" +
                                               nPartyIdentificationIndex +
                                               "]",
                                               EText.PARTY_UNSUPPORTED_ADDRESS_IDENTIFIER.getDisplayTextWithArgs (aDisplayLocale,
                                                                                                                  sUBLPartyID,
                                                                                                                  aUBLPartyID.getID ()
                                                                                                                             .getSchemeID ()));
        ++nPartyIdentificationIndex;
      }
    }

    return aEbiAddress;
  }

  @Nonnull
  protected static final String getAggregated (@Nonnull final List <DescriptionType> aList)
  {
    final StringBuilder aSB = new StringBuilder ();
    for (final DescriptionType aItem : aList)
    {
      if (aSB.length () > 0)
        aSB.append ('\n');
      aSB.append (aItem.getValue ());
    }
    return aSB.toString ();
  }

  @Nonnull
  public static Ebi42DeliveryType convertDelivery (@Nonnull final DeliveryType aUBLDelivery,
                                                   @Nonnull final String sDeliveryType,
                                                   @Nullable final CustomerPartyType aCustomerParty,
                                                   @Nonnull final ErrorList aTransformationErrorList,
                                                   @Nonnull final Locale aContentLocale,
                                                   @Nonnull final Locale aDisplayLocale)
  {
    final Ebi42DeliveryType aEbiDelivery = new Ebi42DeliveryType ();

    // Set the delivery ID
    aEbiDelivery.setDeliveryID (aUBLDelivery.getIDValue ());

    // Set the delivery date
    aEbiDelivery.setDate (aUBLDelivery.getActualDeliveryDateValue ());

    // Address present?
    final LocationType aUBLDeliveryLocation = aUBLDelivery.getDeliveryLocation ();
    if (aUBLDeliveryLocation != null && aUBLDeliveryLocation.getAddress () != null)
    {
      // Optional description
      aEbiDelivery.setDescription (getAggregated (aUBLDeliveryLocation.getDescription ()));

      final Ebi42AddressType aEbiAddress = new Ebi42AddressType ();
      EbInterface42Helper.setAddressData (aUBLDeliveryLocation.getAddress (),
                                          aEbiAddress,
                                          sDeliveryType,
                                          aTransformationErrorList,
                                          aContentLocale,
                                          aDisplayLocale);

      // Check delivery party
      String sAddressName = null;
      if (aUBLDelivery.getDeliveryParty () != null)
        for (final PartyNameType aUBLPartyName : aUBLDelivery.getDeliveryParty ().getPartyName ())
        {
          sAddressName = StringHelper.trim (aUBLPartyName.getNameValue ());
          if (StringHelper.hasText (sAddressName))
            break;
        }

      // As fallback use location name
      if (StringHelper.hasNoText (sAddressName))
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
      }
      aEbiAddress.setName (sAddressName);

      if (StringHelper.hasNoText (aEbiAddress.getName ()))
        aTransformationErrorList.addError (sDeliveryType +
                                           "/DeliveryParty",
                                           EText.DELIVERY_WITHOUT_NAME.getDisplayText (aDisplayLocale));

      aEbiDelivery.setAddress (aEbiAddress);
    }

    return aEbiDelivery;
  }
}
