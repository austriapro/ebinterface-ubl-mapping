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
package at.austriapro.ebinterface.ubl.to;

import java.util.Locale;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.collection.commons.CommonsLinkedHashSet;
import com.helger.collection.commons.ICommonsOrderedSet;
import com.helger.ebinterface.v60.Ebi60AddressIdentifierType;
import com.helger.ebinterface.v60.Ebi60AddressType;
import com.helger.ebinterface.v60.Ebi60ContactType;
import com.helger.ebinterface.v60.Ebi60DeliveryType;
import com.helger.ebinterface.v60.Ebi60DocumentTypeType;
import com.helger.xsds.ccts.cct.schemamodule.CodeType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryTermsType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PersonType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.SpecialTermsType;

/**
 * Base class for ebInterface 6.0 to Peppol UBL converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractEbInterface60ToUBLConverter extends AbstractEbInterfaceToUBLConverter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractEbInterface60ToUBLConverter.class);

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created UBL files. May not be <code>null</code>.
   */
  protected AbstractEbInterface60ToUBLConverter (@Nonnull final Locale aDisplayLocale,
                                                 @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @Nullable
  protected static <T extends CodeType> T getTypeCode (@Nullable final Ebi60DocumentTypeType eType,
                                                       @Nonnull final Supplier <T> aFactory)
  {
    String sID = null;
    if (eType != null)
      switch (eType)
      {
        case INVOICE:
        case SUBSEQUENT_CREDIT:
        case SUBSEQUENT_DEBIT:
          sID = INVOICE_TYPE_CODE_INVOICE;
          break;
        case CREDIT_MEMO:
          sID = INVOICE_TYPE_CODE_CREDIT_NOTE;
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
        case FINAL_SETTLEMENT:
          sID = INVOICE_TYPE_CODE_FINAL_PAYMENT;
          break;
      }

    if (sID == null)
    {
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
  protected static AddressType convertAddress (@Nullable final Ebi60AddressType aEbiAddress,
                                               @Nonnull final Locale aContentLocale)
  {
    if (aEbiAddress == null)
      return null;

    final AddressType ret = new AddressType ();
    if (aEbiAddress.hasAddressIdentifierEntries ())
    {
      // Only one ID allowed
      final Ebi60AddressIdentifierType aEbiType = aEbiAddress.getAddressIdentifierAtIndex (0);
      final IDType aUBLID = new IDType ();
      aUBLID.setValue (aEbiType.getValue ());
      if (aEbiType.getAddressIdentifierType () != null)
        aUBLID.setSchemeID (aEbiType.getAddressIdentifierType ());
      ret.setID (aUBLID);
    }

    if (StringHelper.isNotEmpty (aEbiAddress.getStreet ()))
      ret.setStreetName (aEbiAddress.getStreet ());
    if (StringHelper.isNotEmpty (aEbiAddress.getPOBox ()))
      ret.setPostbox (aEbiAddress.getPOBox ());
    if (StringHelper.isNotEmpty (aEbiAddress.getTown ()))
      ret.setCityName (aEbiAddress.getTown ());
    if (StringHelper.isNotEmpty (aEbiAddress.getZIP ()))
      ret.setPostalZone (aEbiAddress.getZIP ());

    if (aEbiAddress.getCountry () != null)
    {
      ret.setCountry (createCountry (aEbiAddress.getCountry ().getCountryCode (),
                                     aEbiAddress.getCountry ().getValue (),
                                     aContentLocale));
    }

    return ret;
  }

  @Nullable
  protected static PartyType convertParty (@Nullable final Ebi60AddressType aEbiAddress,
                                           @Nullable final Ebi60ContactType aEbiContact,
                                           @Nonnull final Locale aContentLocale)
  {
    if (aEbiAddress == null && aEbiContact == null)
      return null;

    // Combine email addresses and phone numbers from Contact and Address
    final ICommonsOrderedSet <String> aEmails = new CommonsLinkedHashSet <> ();
    final ICommonsOrderedSet <String> aPhones = new CommonsLinkedHashSet <> ();

    final PartyType ret = new PartyType ();
    if (aEbiAddress != null)
    {
      if (StringHelper.isNotEmpty (aEbiAddress.getName ()))
      {
        final PartyNameType aUBLPartyName = new PartyNameType ();
        aUBLPartyName.setName (aEbiAddress.getName ());
        ret.addPartyName (aUBLPartyName);
      }

      ret.setPostalAddress (convertAddress (aEbiAddress, aContentLocale));

      aEmails.addAll (aEbiAddress.getEmail ());
      aPhones.addAll (aEbiAddress.getPhone ());
    }

    final ContactType aUBLContact = new ContactType ();
    boolean bHasContactData = false;

    if (aEbiContact != null)
    {
      if (StringHelper.isNotEmpty (aEbiContact.getSalutation ()))
      {
        final PersonType aUBLPerson = new PersonType ();
        aUBLPerson.setGenderCode (aEbiContact.getSalutation ());
        ret.addPerson (aUBLPerson);
      }

      if (StringHelper.isNotEmpty (aEbiContact.getName ()))
      {
        aUBLContact.setName (aEbiContact.getName ());
        bHasContactData = true;
      }

      aEmails.addAll (aEbiContact.getEmail ());
      aPhones.addAll (aEbiContact.getPhone ());
    }

    if (aEmails.isNotEmpty ())
    {
      aUBLContact.setElectronicMail (StringImplode.getImploded (' ', aEmails));
      bHasContactData = true;
    }

    if (aPhones.isNotEmpty ())
    {
      aUBLContact.setTelephone (StringImplode.getImploded (' ', aPhones));
      bHasContactData = true;
    }
    if (bHasContactData)
      ret.setContact (aUBLContact);

    return ret;
  }

  @Nullable
  protected static DeliveryType convertDelivery (@Nullable final Ebi60DeliveryType aEbiDelivery,
                                                 @Nonnull final Locale aContentLocale)
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
    aUBLDelivery.setDeliveryParty (convertParty (aEbiDelivery.getAddress (),
                                                 aEbiDelivery.getContact (),
                                                 aContentLocale));

    // UBL has no address info field - add all
    String sDeliveryInfo = aEbiDelivery.getDescription ();
    if (aEbiDelivery.getAddress () != null)
    {
      // AdditionalInformation since v6.0
      for (final var aAddInfo : aEbiDelivery.getAddress ().getAdditionalInformation ())
      {
        if (StringHelper.isNotEmpty (sDeliveryInfo))
        {
          sDeliveryInfo += "\n\n";
          sDeliveryInfo += aAddInfo.getValue ();
        }
        else
          sDeliveryInfo = aAddInfo.getValue ();
      }
    }
    if (StringHelper.isNotEmpty (sDeliveryInfo))
    {
      final DeliveryTermsType aDeliveryTerms = new DeliveryTermsType ();
      aDeliveryTerms.addSpecialTerms (new SpecialTermsType (sDeliveryInfo));
      aUBLDelivery.addDeliveryTerms (aDeliveryTerms);
    }

    return aUBLDelivery;
  }
}
