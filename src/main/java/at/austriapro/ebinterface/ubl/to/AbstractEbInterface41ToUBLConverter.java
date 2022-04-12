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
package at.austriapro.ebinterface.ubl.to;

import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v41.Ebi41AddressIdentifierType;
import com.helger.ebinterface.v41.Ebi41AddressType;
import com.helger.ebinterface.v41.Ebi41DeliveryType;
import com.helger.ebinterface.v41.Ebi41DocumentTypeType;
import com.helger.xsds.ccts.cct.schemamodule.CodeType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PersonType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;

/**
 * Base class for ebInterface 4.1 to Peppol UBL converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractEbInterface41ToUBLConverter extends AbstractEbInterfaceToUBLConverter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractEbInterface41ToUBLConverter.class);

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created UBL files. May not be <code>null</code>.
   */
  public AbstractEbInterface41ToUBLConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @Nullable
  protected static <T extends CodeType> T getTypeCode (@Nullable final Ebi41DocumentTypeType eType, @Nonnull final Supplier <T> aFactory)
  {
    String sID = null;
    if (eType != null)
      switch (eType)
      {
        case INVOICE:
        case CREDIT_MEMO:
        case FINAL_SETTLEMENT:
        case SUBSEQUENT_CREDIT:
        case SUBSEQUENT_DEBIT:
          sID = INVOICE_TYPE_CODE_INVOICE;
          break;
        case INVOICE_FOR_PARTIAL_DELIVERY:
          sID = INVOICE_TYPE_CODE_PARTIAL;
          break;
        case INVOICE_FOR_ADVANCE_PAYMENT:
          sID = INVOICE_TYPE_CODE_PREPAYMENT_INVOICE;
          break;
        case SELF_BILLING:
          sID = INVOICE_TYPE_CODE_SELF_BILLING;
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
  protected static AddressType convertAddress (@Nullable final Ebi41AddressType aEbiAddress, @Nonnull final Locale aContentLocale)
  {
    if (aEbiAddress == null)
      return null;

    final AddressType ret = new AddressType ();
    if (aEbiAddress.getAddressIdentifierCount () > 0)
    {
      // Only one ID allowed
      final Ebi41AddressIdentifierType aEbiType = aEbiAddress.getAddressIdentifierAtIndex (0);
      final IDType aUBLID = new IDType ();
      aUBLID.setValue (aEbiType.getValue ());
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
      ret.setCountry (createCountry (aEbiAddress.getCountry ().getCountryCode () != null ? aEbiAddress.getCountry ()
                                                                                                      .getCountryCode ()
                                                                                                      .value ()
                                                                                         : null,
                                     aEbiAddress.getCountry ().getContent (),
                                     aContentLocale));
    }

    return ret;
  }

  @Nullable
  protected static PartyType convertParty (@Nullable final Ebi41AddressType aEbiAddress, @Nonnull final Locale aContentLocale)
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

    ret.setPostalAddress (convertAddress (aEbiAddress, aContentLocale));

    if (StringHelper.hasText (aEbiAddress.getSalutation ()))
    {
      final PersonType aUBLPerson = new PersonType ();
      aUBLPerson.setGenderCode (aEbiAddress.getSalutation ());
      ret.addPerson (aUBLPerson);
    }

    return ret;
  }

  @Nullable
  protected static DeliveryType convertDelivery (@Nullable final Ebi41DeliveryType aEbiDelivery, @Nonnull final Locale aContentLocale)
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
    aUBLDelivery.setDeliveryAddress (convertAddress (aEbiDelivery.getAddress (), aContentLocale));
    aUBLDelivery.setDeliveryParty (convertParty (aEbiDelivery.getAddress (), aContentLocale));
    return aUBLDelivery;
  }
}
