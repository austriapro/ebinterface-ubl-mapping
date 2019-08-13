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
package com.helger.ebinterface.ubl.to;

import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.ubl.AbstractConverter;
import com.helger.ebinterface.v40.Ebi40AddressIdentifierType;
import com.helger.ebinterface.v40.Ebi40AddressType;
import com.helger.ebinterface.v40.Ebi40DeliveryType;
import com.helger.ebinterface.v40.Ebi40DocumentTypeType;
import com.helger.xsds.ccts.cct.schemamodule.CodeType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CountryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemPropertyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PersonType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;

/**
 * Base class for ebInterface to PEPPOL UBL converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractEbInterface40ToUBLConverter extends AbstractConverter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractEbInterface40ToUBLConverter.class);

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be
   *        <code>null</code>.
   */
  public AbstractEbInterface40ToUBLConverter (@Nonnull final Locale aDisplayLocale,
                                              @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @Nullable
  protected static <T extends CodeType> T getTypeCode (@Nullable final Ebi40DocumentTypeType eType,
                                                       @Nonnull final Supplier <T> aFactory)
  {
    String sID = null;
    if (eType != null)
      switch (eType)
      {
        case INVOICE:
        case CREDIT_MEMO:
        case FINAL_SETTLEMENT:
        case INVOICE_FOR_ADVANCE_PAYMENT:
        case SUBSEQUENT_CREDIT:
        case SUBSEQUENT_DEBIT:
          sID = "380";
          break;
        case INVOICE_FOR_PARTIAL_DELIVERY:
          sID = "326";
          break;
        case SELF_BILLING:
          sID = "389";
          break;
      }

    if (sID == null)
    {
      if (LOGGER.isWarnEnabled ())
        LOGGER.warn ("Failed to resolve document type " + eType);
      return null;
    }

    final T ret = aFactory.get ();
    ret.setValue (sID);
    ret.setListID ("UNCL1001");
    ret.setName (eType.value ());
    return ret;
  }

  @Nullable
  protected static AddressType convertAddress (@Nullable final Ebi40AddressType aEbiAddress)
  {
    if (aEbiAddress == null)
      return null;

    final AddressType ret = new AddressType ();
    if (aEbiAddress.getAddressIdentifier () != null)
    {
      // Only one ID allowed
      final Ebi40AddressIdentifierType aEbiType = aEbiAddress.getAddressIdentifier ();
      final IDType aUBLID = new IDType ();
      aUBLID.setValue (aEbiType.getContent ());
      if (aEbiType.getAddressIdentifierType () != null)
        aUBLID.setSchemeID (aEbiType.getAddressIdentifierType ().value ());
      ret.setID (aUBLID);
    }

    if (StringHelper.hasText (aEbiAddress.getStreet ()))
      ret.setStreetName (aEbiAddress.getStreet ());
    if (StringHelper.hasText (aEbiAddress.getPOBox ()))
      ret.setPostbox (aEbiAddress.getPOBox ());
    if (StringHelper.hasText (aEbiAddress.getTown ()))
      ret.setCityName (aEbiAddress.getTown ());
    if (StringHelper.hasText (aEbiAddress.getZIP ()))
      ret.setPostalZone (aEbiAddress.getZIP ());

    if (aEbiAddress.getCountry () != null)
    {
      final CountryType aUBLCountry = new CountryType ();
      if (aEbiAddress.getCountry ().getCountryCode () != null)
        aUBLCountry.setIdentificationCode (aEbiAddress.getCountry ().getCountryCode ().value ());
      aUBLCountry.setName (aEbiAddress.getCountry ().getContent ());
      ret.setCountry (aUBLCountry);
    }

    return ret;
  }

  @Nullable
  protected static PartyType convertParty (@Nullable final Ebi40AddressType aEbiAddress)
  {
    if (aEbiAddress == null)
      return null;

    final PartyType ret = new PartyType ();
    if (StringHelper.hasText (aEbiAddress.getName ()))
    {
      final PartyNameType aUBLPartyName = new PartyNameType ();
      aUBLPartyName.setName (aEbiAddress.getName ());
      ret.addPartyName (aUBLPartyName);
    }

    final ContactType aUBLContact = new ContactType ();
    boolean bHasData = false;
    if (StringHelper.hasText (aEbiAddress.getContact ()))
    {
      aUBLContact.setName (aEbiAddress.getContact ());
      bHasData = true;
    }
    if (StringHelper.hasText (aEbiAddress.getEmail ()))
    {
      aUBLContact.setElectronicMail (aEbiAddress.getEmail ());
      bHasData = true;
    }
    if (StringHelper.hasText (aEbiAddress.getPhone ()))
    {
      aUBLContact.setTelephone (aEbiAddress.getPhone ());
      bHasData = true;
    }
    if (bHasData)
      ret.setContact (aUBLContact);

    ret.setPostalAddress (convertAddress (aEbiAddress));

    if (StringHelper.hasText (aEbiAddress.getSalutation ()))
    {
      final PersonType aUBLPerson = new PersonType ();
      aUBLPerson.setGenderCode (aEbiAddress.getSalutation ());
      ret.addPerson (aUBLPerson);
    }

    return ret;
  }

  @Nullable
  protected static DeliveryType convertDelivery (@Nullable final Ebi40DeliveryType aEbiDelivery)
  {
    if (aEbiDelivery == null)
      return null;

    final DeliveryType aUBLDelivery = new DeliveryType ();
    if (aEbiDelivery.getDeliveryID () != null)
      aUBLDelivery.setID (aEbiDelivery.getDeliveryID ());
    if (aEbiDelivery.getDate () != null)
      aUBLDelivery.setActualDeliveryDate (aEbiDelivery.getDate ());
    else
      if (aEbiDelivery.getPeriod () != null)
      {
        // Delivery period is mapped to invoice period
        final PeriodType aUBLPeriod = new PeriodType ();
        aUBLPeriod.setStartDate (aEbiDelivery.getPeriod ().getFromDate ());
        aUBLPeriod.setEndDate (aEbiDelivery.getPeriod ().getToDate ());
        aUBLDelivery.setRequestedDeliveryPeriod (aUBLPeriod);
      }
    aUBLDelivery.setDeliveryAddress (convertAddress (aEbiDelivery.getAddress ()));
    aUBLDelivery.setDeliveryParty (convertParty (aEbiDelivery.getAddress ()));
    return aUBLDelivery;
  }

  @Nonnull
  protected static TaxSchemeType createTaxScheme (@Nonnull final String sID)
  {
    final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
    final IDType aUBLTSID = aUBLTaxScheme.setID (sID);
    aUBLTSID.setSchemeAgencyID ("6");
    aUBLTSID.setSchemeID (SUPPORTED_TAX_SCHEME_SCHEME_ID);
    return aUBLTaxScheme;
  }

  @Nonnull
  protected static TaxSchemeType createTaxSchemeVAT ()
  {
    return createTaxScheme (SUPPORTED_TAX_SCHEME_ID.getID ());
  }

  @Nonnull
  protected static TaxCategoryType createTaxCategoryVAT (@Nonnull final String sID)
  {
    final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
    final IDType aUBLTCID = aUBLTaxCategory.setID (sID);
    aUBLTCID.setSchemeAgencyID ("6");
    aUBLTCID.setSchemeID (SUPPORTED_TAX_SCHEME_SCHEME_ID);
    // Set default scheme
    aUBLTaxCategory.setTaxScheme (createTaxSchemeVAT ());
    return aUBLTaxCategory;
  }

  @Nonnull
  protected static ItemPropertyType createItemProperty (@Nullable final String sName, @Nullable final String sValue)
  {
    final ItemPropertyType ret = new ItemPropertyType ();
    ret.setName (sName);
    ret.setValue (sValue);
    return ret;
  }
}
