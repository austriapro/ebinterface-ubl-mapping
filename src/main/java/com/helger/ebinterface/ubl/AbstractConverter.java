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
package com.helger.ebinterface.ubl;

import java.math.RoundingMode;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.peppol.codelist.EInvoiceTypeCode;
import com.helger.peppol.codelist.ETaxSchemeID;

/**
 * Base class for PEPPOL UBL to/from ebInterface converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractConverter
{
  public static final int ORDER_REFERENCE_MAX_LENGTH = 54;
  public static final String REGEX_BIC = "[0-9A-Za-z]{8}([0-9A-Za-z]{3})?";
  public static final String SUPPORTED_TAX_SCHEME_SCHEME_ID = "UN/ECE 5153";
  public static final String SUPPORTED_TAX_SCHEME_SCHEME_ID_SUBSET = SUPPORTED_TAX_SCHEME_SCHEME_ID + " Subset";
  public static final int IBAN_MAX_LENGTH = 34;
  public static final String PAYMENT_CHANNEL_CODE_IBAN = "IBAN";
  public static final ETaxSchemeID SUPPORTED_TAX_SCHEME_ID = ETaxSchemeID.VALUE_ADDED_TAX;
  public static final String EBI_GENERATING_SYSTEM_42 = "UBL 2.1 to ebInterface 4.2 converter";
  public static final int SCALE_PERC = 2;
  public static final int SCALE_PRICE2 = 2;
  public static final int SCALE_PRICE4 = 4;
  /** Austria uses HALF_UP mode! */
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
  /** The invoice type code to use */
  public static final String INVOICE_TYPE_CODE = EInvoiceTypeCode.COMMERCIAL_INVOICE.getID ();
  public static final String UBL_VERSION_20 = "2.0";
  public static final String UBL_VERSION_21 = "2.1";

  /** The UBL customization ID to use */
  public static final String CUSTOMIZATION_SCHEMEID = "PEPPOL";

  protected final Locale m_aDisplayLocale;
  protected final Locale m_aContentLocale;

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be
   *        <code>null</code>.
   */
  public AbstractConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    m_aDisplayLocale = ValueEnforcer.notNull (aDisplayLocale, "DisplayLocale");
    m_aContentLocale = ValueEnforcer.notNull (aContentLocale, "ContentLocale");
  }
}
