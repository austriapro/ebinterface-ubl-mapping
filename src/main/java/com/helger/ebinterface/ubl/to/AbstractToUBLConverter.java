/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2016 AUSTRIAPRO - www.austriapro.at
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

import com.helger.commons.annotation.Translatable;
import com.helger.commons.text.IMultilingualText;
import com.helger.commons.text.display.IHasDisplayTextWithArgs;
import com.helger.commons.text.resolve.DefaultTextResolver;
import com.helger.commons.text.util.TextHelper;
import com.helger.ebinterface.ubl.AbstractConverter;
import com.helger.ebinterface.v42.Ebi42AddressIdentifierType;
import com.helger.ebinterface.v42.Ebi42AddressType;
import com.helger.ebinterface.v42.Ebi42DocumentTypeType;
import com.helger.ubl21.codelist.EDocumentTypeCode21;
import com.helger.xsds.ccts.cct.schemamodule.CodeType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ContactType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CountryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;

/**
 * Base class for ebInterface to PEPPOL UBL converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractToUBLConverter extends AbstractConverter
{
  @Translatable
  public static enum EText implements IHasDisplayTextWithArgs
  {
    ;

    private final IMultilingualText m_aTP;

    private EText (@Nonnull final String sDE, @Nonnull final String sEN)
    {
      m_aTP = TextHelper.create_DE_EN (sDE, sEN);
    }

    @Nullable
    public String getDisplayText (@Nonnull final Locale aContentLocale)
    {
      return DefaultTextResolver.getTextStatic (this, m_aTP, aContentLocale);
    }
  }

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be
   *        <code>null</code>.
   */
  public AbstractToUBLConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @Nullable
  protected static <T extends CodeType> T getTypeCode (@Nullable final Ebi42DocumentTypeType eType,
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
          sID = EDocumentTypeCode21._380.getID ();
          break;
        case INVOICE_FOR_PARTIAL_DELIVERY:
          sID = EDocumentTypeCode21._326.getID ();
          break;
        case SELF_BILLING:
          sID = EDocumentTypeCode21._389.getID ();
          break;
      }

    if (sID == null)
      return null;

    final T ret = aFactory.get ();
    ret.setValue (sID);
    ret.setListID ("UNCL1001");
    return ret;
  }

  @Nullable
  protected static AddressType convertAddress (@Nullable final Ebi42AddressType aEbiAddress)
  {
    if (aEbiAddress == null)
      return null;

    final AddressType ret = new AddressType ();
    for (final Ebi42AddressIdentifierType aEbiType : aEbiAddress.getAddressIdentifier ())
    {
      final IDType aUBLID = new IDType ();
      aUBLID.setValue (aEbiType.getValue ());
      aUBLID.setSchemeID (aEbiType.getAddressIdentifierType ().value ());
      ret.setID (aUBLID);

      // Only one ID allowed
      break;
    }

    ret.setStreetName (aEbiAddress.getStreet ());
    ret.setPostbox (aEbiAddress.getPOBox ());
    ret.setCityName (aEbiAddress.getTown ());
    ret.setPostalZone (aEbiAddress.getZIP ());
    if (aEbiAddress.getCountry () != null)
    {
      final CountryType aUBLCountry = new CountryType ();
      aUBLCountry.setIdentificationCode (aEbiAddress.getCountry ().getCountryCode ());
      aUBLCountry.setName (aEbiAddress.getCountry ().getValue ());
      ret.setCountry (aUBLCountry);
    }

    return ret;
  }

  @Nullable
  protected static PartyType convertParty (@Nullable final Ebi42AddressType aEbiAddress)
  {
    if (aEbiAddress == null)
      return null;

    final PartyType ret = new PartyType ();
    if (aEbiAddress.getName () != null)
    {
      final PartyNameType aUBLPartyName = new PartyNameType ();
      aUBLPartyName.setName (aEbiAddress.getName ());
      ret.addPartyName (aUBLPartyName);
    }
    final ContactType aUBLContact = new ContactType ();
    aUBLContact.setName (aEbiAddress.getContact ());
    aUBLContact.setElectronicMail (aEbiAddress.getEmail ());
    aUBLContact.setTelephone (aEbiAddress.getPhone ());
    ret.setContact (aUBLContact);

    return ret;
  }
}
