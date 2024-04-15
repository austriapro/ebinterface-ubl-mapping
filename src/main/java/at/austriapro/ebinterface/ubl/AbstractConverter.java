/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2024 AUSTRIAPRO - www.austriapro.at
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
package at.austriapro.ebinterface.ubl;

import java.math.RoundingMode;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;

/**
 * Base class for Peppol UBL to/from ebInterface converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractConverter
{
  public static final String SCHEME_BIC = "BIC";
  public static final String REGEX_BIC = "[0-9A-Za-z]{8}([0-9A-Za-z]{3})?";
  public static final String SCHEME_IBAN = "IBAN";
  public static final int IBAN_MAX_LENGTH = 34;
  public static final String PAYMENT_CHANNEL_CODE_IBAN = "IBAN";
  public static final String UOM_DEFAULT = "C62";
  public static final String SUPPORTED_TAX_SCHEME_ID = "VAT";
  public static final String OTHER_TAX_SCHEME_ID = "OTH";
  public static final String SCHEME_SEPA = "SEPA";

  public static final int SCALE_PERC = 2;
  public static final int SCALE_PRICE2 = 2;
  public static final int SCALE_PRICE4 = 4;

  /** Austria uses HALF_UP mode! */
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  // UNCL 1001
  public static final String INVOICE_TYPE_CODE_FINAL_PAYMENT = "218";
  public static final String INVOICE_TYPE_CODE_PARTIAL = "326";
  /** The invoice type code to use (380) */
  public static final String INVOICE_TYPE_CODE_INVOICE = "380";
  public static final String INVOICE_TYPE_CODE_PREPAYMENT_INVOICE = "386";
  public static final String INVOICE_TYPE_CODE_SELF_BILLING = "389";
  // List taken from the EN 16931 validation artefacts 1.3.3
  // Also matching
  // https://docs.peppol.eu/poacc/billing/3.0/codelist/UNCL1001-inv/
  public static final Set <String> INVOICE_TYPE_CODES = new CommonsLinkedHashSet <> ("80",
                                                                                     "82",
                                                                                     "84",
                                                                                     "130",
                                                                                     "202",
                                                                                     "203",
                                                                                     "204",
                                                                                     "211",
                                                                                     INVOICE_TYPE_CODE_FINAL_PAYMENT,
                                                                                     "295",
                                                                                     "325",
                                                                                     INVOICE_TYPE_CODE_PARTIAL,
                                                                                     INVOICE_TYPE_CODE_INVOICE,
                                                                                     "383",
                                                                                     "384",
                                                                                     "385",
                                                                                     INVOICE_TYPE_CODE_PREPAYMENT_INVOICE,
                                                                                     "387",
                                                                                     "388",
                                                                                     INVOICE_TYPE_CODE_SELF_BILLING,
                                                                                     "390",
                                                                                     "393",
                                                                                     "394",
                                                                                     "395",
                                                                                     "456",
                                                                                     "457",
                                                                                     "527",
                                                                                     "575",
                                                                                     "623",
                                                                                     "633",
                                                                                     "751",
                                                                                     "780",
                                                                                     "935").getAsUnmodifiable ();
  // List taken from the EN 16931 validation artefacts 1.3.3
  // Also matching
  // https://docs.peppol.eu/poacc/billing/3.0/codelist/UNCL1001-cn/
  public static final String INVOICE_TYPE_CODE_CREDIT_NOTE = "381";
  public static final Set <String> CREDIT_NOTE_TYPE_CODES = new CommonsLinkedHashSet <> ("81",
                                                                                         "83",
                                                                                         "261",
                                                                                         "262",
                                                                                         "296",
                                                                                         "308",
                                                                                         INVOICE_TYPE_CODE_CREDIT_NOTE,
                                                                                         "396",
                                                                                         "420",
                                                                                         "458",
                                                                                         "532").getAsUnmodifiable ();

  public static final String AT_UNDEFINED_VATIN = "ATU00000000";

  // UNCL 4461
  public static final String PAYMENT_MEANS_CREDIT_TRANSFER = "30";
  public static final String PAYMENT_MEANS_DEBIT_TRANSFER = "31";
  public static final String PAYMENT_MEANS_PAYMENT_TO_BANK_ACCOUNT = "42";
  public static final String PAYMENT_MEANS_SEPA_CREDIT_TRANSFER = "58";

  public static final String PAYMENT_MEANS_DIRECT_DEBIT = "49";
  public static final String PAYMENT_MEANS_SEPA_DIRECT_DEBIT = "59";

  public static final String PAYMENT_MEANS_CREDIT_CARD = "54";
  public static final String PAYMENT_MEANS_DEBIT_CARD = "55";

  // UBL versions
  public static final String UBL_VERSION_20 = "2.0";
  public static final String UBL_VERSION_21 = "2.1";
  public static final String UBL_VERSION_22 = "2.2";
  public static final String UBL_VERSION_23 = "2.3";
  public static final String UBL_VERSION_24 = "2.4";

  /** The UBL customization ID to use */
  @Deprecated (forRemoval = true, since = "5.1.2")
  public static final String CUSTOMIZATION_SCHEMEID = "PEPPOL";

  protected final Locale m_aDisplayLocale;
  protected final Locale m_aContentLocale;

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages (the locale of the user). May not be
   *        <code>null</code>.
   * @param aContentLocale
   *        The locale for the created payload. May not be <code>null</code>.
   */
  public AbstractConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    m_aDisplayLocale = ValueEnforcer.notNull (aDisplayLocale, "DisplayLocale");
    m_aContentLocale = ValueEnforcer.notNull (aContentLocale, "ContentLocale");
  }
}
