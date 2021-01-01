/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2021 AUSTRIAPRO - www.austriapro.at
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.locale.country.CountryCache;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.codelist.ETaxCategoryCode;
import com.helger.peppol.codelist.ETaxSchemeID;

import at.austriapro.ebinterface.ubl.AbstractConverter;
import at.austriapro.ebinterface.ubl.helper.MultilingualCountryCache;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CountryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemPropertyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;

/**
 * Base class for ebInterface to Peppol UBL converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractEbInterfaceToUBLConverter extends AbstractConverter
{
  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created UBL files. May not be <code>null</code>.
   */
  public AbstractEbInterfaceToUBLConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @Nonnull
  protected static final TaxSchemeType createTaxScheme (@Nonnull final String sID)
  {
    final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
    final IDType aUBLTSID = aUBLTaxScheme.setID (sID);
    aUBLTSID.setSchemeAgencyID ("6");
    aUBLTSID.setSchemeID (SUPPORTED_TAX_SCHEME_SCHEME_ID);
    return aUBLTaxScheme;
  }

  @Nonnull
  protected static final TaxSchemeType createTaxSchemeVAT ()
  {
    return createTaxScheme (SUPPORTED_TAX_SCHEME_ID.getID ());
  }

  @Nonnull
  protected static final TaxCategoryType createTaxCategory (@Nonnull final String sID)
  {
    final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
    final IDType aUBLTCID = aUBLTaxCategory.setID (sID);
    aUBLTCID.setSchemeAgencyID ("6");
    aUBLTCID.setSchemeID (SUPPORTED_TAX_SCHEME_SCHEME_ID);
    return aUBLTaxCategory;
  }

  @Nonnull
  protected static final TaxCategoryType createTaxCategoryVAT (@Nonnull final String sID)
  {
    final TaxCategoryType aUBLTaxCategory = createTaxCategory (sID);
    // Set default scheme
    aUBLTaxCategory.setTaxScheme (createTaxSchemeVAT ());
    return aUBLTaxCategory;
  }

  @Nonnull
  protected static final TaxCategoryType createTaxCategoryOther ()
  {
    final TaxCategoryType aUBLTaxCategory = createTaxCategory (ETaxCategoryCode.O.getID ());
    // Set default scheme
    aUBLTaxCategory.setTaxScheme (createTaxScheme (ETaxSchemeID.OTHER_TAXES.getID ()));
    return aUBLTaxCategory;
  }

  @Nonnull
  protected static final ItemPropertyType createItemProperty (@Nullable final String sName,
                                                              @Nullable final String sValue)
  {
    final ItemPropertyType ret = new ItemPropertyType ();
    ret.setName (sName);
    ret.setValue (sValue);
    return ret;
  }

  @Nullable
  protected static final CountryType createCountry (@Nullable final String sCode,
                                                    @Nullable final String sName,
                                                    @Nonnull final Locale aContentLocale)
  {
    final String sRealCode;
    final String sRealName;
    if (StringHelper.hasNoText (sCode))
    {
      if (StringHelper.hasNoText (sName))
        return null;

      // Find code from name
      sRealCode = MultilingualCountryCache.getRealCountryCode (sName);
      final Locale aResolvedCountry = CountryCache.getInstance ().getCountry (sRealCode);
      if (aResolvedCountry != null)
        sRealName = aResolvedCountry.getDisplayCountry (aContentLocale);
      else
        sRealName = sName;
    }
    else
      if (StringHelper.hasNoText (sName))
      {
        // Find name from code
        sRealCode = sCode;
        final Locale aResolvedCountry = CountryCache.getInstance ().getCountry (sRealCode);
        if (aResolvedCountry != null)
          sRealName = aResolvedCountry.getDisplayCountry (aContentLocale);
        else
          sRealName = sCode;
      }
      else
      {
        sRealCode = sCode;
        sRealName = sName;
      }

    final CountryType ret = new CountryType ();
    ret.setIdentificationCode (sRealCode);
    ret.setName (sRealName);
    return ret;
  }
}
