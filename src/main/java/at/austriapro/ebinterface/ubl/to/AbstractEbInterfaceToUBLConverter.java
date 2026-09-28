/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2026 AUSTRIAPRO - www.austriapro.at
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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.string.StringHelper;
import com.helger.ebinterface.codelist.ETaxCategoryCode;
import com.helger.text.locale.country.CountryCache;

import at.austriapro.ebinterface.ubl.AbstractEbInterfaceUBLConverter;
import at.austriapro.ebinterface.ubl.helper.MultilingualCountryCache;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CountryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ItemPropertyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.TaxExemptionReasonType;

/**
 * Base class for ebInterface to Peppol UBL converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractEbInterfaceToUBLConverter extends AbstractEbInterfaceUBLConverter
{
  public static final String CURRENCY_LIST_AGENCY_ID = "6";
  public static final String CURRENCY_LIST_ID = "ISO 4217 Alpha";

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created UBL files. May not be <code>null</code>.
   */
  protected AbstractEbInterfaceToUBLConverter (@NonNull final Locale aDisplayLocale,
                                               @NonNull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @NonNull
  protected static final TaxSchemeType createTaxScheme (@NonNull final String sID)
  {
    final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
    aUBLTaxScheme.setID (sID);
    return aUBLTaxScheme;
  }

  @NonNull
  protected static final TaxSchemeType createTaxSchemeVAT ()
  {
    return createTaxScheme (SUPPORTED_TAX_SCHEME_ID);
  }

  @NonNull
  protected static final TaxCategoryType createTaxCategory (@NonNull final String sID)
  {
    final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
    aUBLTaxCategory.setID (sID);
    return aUBLTaxCategory;
  }

  @NonNull
  protected static final TaxCategoryType createTaxCategoryVAT (@NonNull final String sID)
  {
    final TaxCategoryType aUBLTaxCategory = createTaxCategory (sID);
    // Set default scheme
    aUBLTaxCategory.setTaxScheme (createTaxSchemeVAT ());
    return aUBLTaxCategory;
  }

  @NonNull
  protected static final TaxCategoryType createTaxCategoryOther ()
  {
    final TaxCategoryType aUBLTaxCategory = createTaxCategory (ETaxCategoryCode.O.getID ());
    // Set default scheme
    aUBLTaxCategory.setTaxScheme (createTaxScheme (OTHER_TAX_SCHEME_ID));
    return aUBLTaxCategory;
  }

  /**
   * Add the provided text as the UBL <code>TaxExemptionReason</code> (EN 16931 BT-120) to the
   * provided tax category. The EN 16931 rules BR-S-10 and BR-Z-10 forbid BT-120 for the tax
   * category codes "S" and "Z", so for these codes nothing happens.
   *
   * @param aUBLTaxCategory
   *        The UBL tax category to be filled. May not be <code>null</code>.
   * @param sComment
   *        The tax exemption reason text to be used. May be <code>null</code>.
   */
  protected static final void applyTaxExemptionReason (@NonNull final TaxCategoryType aUBLTaxCategory,
                                                       @Nullable final String sComment)
  {
    if (StringHelper.isNotEmpty (sComment))
    {
      // BR-S-10 and BR-Z-10 forbid BT-120 for these tax category codes
      final String sTaxCategoryCode = aUBLTaxCategory.getIDValue ();
      if (!ETaxCategoryCode.S.getID ().equals (sTaxCategoryCode) &&
          !ETaxCategoryCode.Z.getID ().equals (sTaxCategoryCode))
      {
        aUBLTaxCategory.addTaxExemptionReason (new TaxExemptionReasonType (sComment));
      }
    }
  }

  @NonNull
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
                                                    @NonNull final Locale aContentLocale)
  {
    final String sRealCode;
    final String sRealName;
    if (StringHelper.isEmpty (sCode))
    {
      if (StringHelper.isEmpty (sName))
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
      if (StringHelper.isEmpty (sName))
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
