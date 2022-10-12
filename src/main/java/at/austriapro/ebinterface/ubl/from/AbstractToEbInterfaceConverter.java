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
package at.austriapro.ebinterface.ubl.from;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Translatable;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.commons.text.IMultilingualText;
import com.helger.commons.text.display.IHasDisplayTextWithArgs;
import com.helger.commons.text.resolve.DefaultTextResolver;
import com.helger.commons.text.util.TextHelper;
import com.helger.peppolid.IProcessIdentifier;

import at.austriapro.ebinterface.ubl.AbstractConverter;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSubtotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxTotalType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.AllowanceChargeReasonType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InvoiceTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.ProfileIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.UBLVersionIDType;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Base class for Peppol UBL to ebInterface converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractToEbInterfaceConverter extends AbstractConverter
{
  @Translatable
  public enum EText implements IHasDisplayTextWithArgs
  {
    OR ("oder", "or"),
    NO_UBL_VERSION_ID ("Die UBLVersionID fehlt. Es wird der Wert ''{0}'', ''{1}'', ''{2}'' oder ''{3}'' erwartet.",
                       "No UBLVersionID present. It must be ''{0}'', ''{1}'', ''{2}'' or ''{3}''."),
    INVALID_UBL_VERSION_ID ("Die UBLVersionID ''{0}'' ist ungültig. Diese muss den Wert ''{1}'', ''{2}'', ''{3}'' oder ''{4}'' haben.",
                            "Invalid UBLVersionID value ''{0}'' present. It must be ''{1}'', ''{2}'', ''{3}'' or ''{4}''."),
    NO_PROFILE_ID ("Die ProfileID fehlt", "No ProfileID present."),
    INVALID_PROFILE_ID ("Die ProfileID ''{0}'' ist ungültig.", "Invalid ProfileID value ''{0}'' present."),
    NO_CUSTOMIZATION_ID ("Die CustomizationID fehlt", "No CustomizationID present."),
    INVALID_CUSTOMIZATION_SCHEME_ID ("Die CustomizationID schemeID ''{0}'' ist ungültig. Diese muss den Wert ''{1}'' haben.",
                                     "Invalid CustomizationID schemeID value ''{0}'' present. It must be ''{1}''."),
    INVALID_CUSTOMIZATION_ID ("Die angegebene CustomizationID ''{0}'' ist ungültig. Sie wird vom angegebenen Profil nicht unterstützt.",
                              "Invalid CustomizationID value ''{0}'' present. It is not supported by the passed profile."),
    NO_INVOICE_TYPECODE ("Der InvoiceTypeCode fehlt. Es wird einer der folgenden Werte erwartet: {0}",
                         "No InvoiceTypeCode present. It must be one of the following values: {0}"),
    INVALID_INVOICE_TYPECODE ("Der InvoiceTypeCode ''{0}'' ist ungültig.Es wird einer der folgenden Werte erwartet: {1}",
                              "Invalid InvoiceTypeCode value ''{0}'' present. It must be one of the following values: {1}"),
    ADDRESS_NO_STREET ("In der Adresse fehlt die Straße.", "Address is missing a street name."),
    ADDRESS_NO_CITY ("In der Adresse fehlt der Name der Stadt.", "Address is missing a city name."),
    ADDRESS_NO_ZIPCODE ("In der Adresse fehlt die PLZ.", "Address is missing a ZIP code."),
    ADDRESS_INVALID_COUNTRY ("Der angegebene Ländercode ''{0}'' ist ungültig.", "The provided country code ''{0}'' is invalid."),
    ADDRESS_NO_COUNTRY ("In der Adresse fehlt der Name des Landes.", "Address is missing a country."),
    CONTACT_NO_NAME ("Im Kontakt fehlr der Name.", "Contact is missing a name."),
    MULTIPLE_PARTIES ("Es sind mehrere Partynamen vorhanden - nur der erste wird verwendet.",
                      "Multiple party names present - only the first one is used."),
    PARTY_NO_NAME ("Der Name der Party fehlt.", "Party name is missing."),
    PARTY_UNSUPPORTED_ENDPOINT ("Ignoriere den Enpunkt ''{0}'' des Typs ''{1}''.", "Ignoring endpoint ID ''{0}'' of type ''{1}''."),
    PARTY_UNSUPPORTED_ADDRESS_IDENTIFIER ("Ignoriere die ID ''{0}'' des Typs ''{1}''.", "Ignoring identification ''{0}'' of type ''{1}''."),
    ORDERLINE_REF_ID_EMPTY ("Es muss ein Wert für die Bestellpositionsnummer angegeben werden.",
                            "A value must be provided for the order line reference ID."),
    ALPHANUM_ID_TYPE_CHANGE ("''{0}'' ist ein ungültiger Typ und wurde auf ''{1}'' geändert.",
                             "''{0}'' is an invalid value and was changed to ''{1}''."),
    INVALID_CURRENCY_CODE ("Der angegebene Währungscode ''{0}'' ist ungültig.", "Invalid currency code ''{0}'' provided."),
    MISSING_INVOICE_NUMBER ("Es wurde keine Rechnungsnummer angegeben.", "No invoice number was provided."),
    MISSING_INVOICE_DATE ("Es wurde keine Rechnungsdatum angegeben.", "No invoice date was provided."),
    BILLER_VAT_MISSING ("Die UID-Nummer des Rechnungsstellers fehlt. Verwenden Sie 'ATU00000000' für österreichische Rechnungssteller an wenn keine UID-Nummer notwendig ist.",
                        "Failed to get biller VAT identification number. Use 'ATU00000000' for Austrian invoice recipients if no VAT identification number is required."),
    ERB_CUSTOMER_ASSIGNED_ACCOUNTID_MISSING ("Die ID des Rechnungsstellers beim Rechnungsempfänger fehlt.",
                                             "Failed to get customer assigned account ID for supplier."),
    INVOICE_RECIPIENT_VAT_MISSING ("Die UID-Nummer des Rechnungsempfängers fehlt. Verwenden Sie 'ATU00000000' für österreichische Empfänger an wenn keine UID-Nummer notwendig ist.",
                                   "Failed to get invoice recipient VAT identification number. Use 'ATU00000000' for Austrian invoice recipients if no VAT identification number is required."),
    INVOICE_RECIPIENT_PARTY_MISSING ("Die Adressdaten des Rechnungsempfängers fehlen.",
                                     "The party information of the invoice recipient are missing."),
    INVOICE_RECIPIENT_PARTY_SUPPLIER_ASSIGNED_ACCOUNT_ID_MISSING ("Die ID des Auftraggebers im System des Rechnungsstellers fehlt.",
                                                                  "Failed to get supplier assigned account ID."),
    ORDERING_PARTY_VAT_MISSING ("Die UID-Nummer des Auftraggebers fehlt. Verwenden Sie 'ATU00000000' für österreichische Empfänger an wenn keine UID-Nummer notwendig ist.",
                                "Failed to get ordering party VAT identification number. Use 'ATU00000000' for Austrian invoice recipients if no VAT identification number is required."),
    ORDERING_PARTY_PARTY_MISSING ("Die Adressdaten des Auftraggebers fehlen.", "The party information of the ordering party are missing."),
    ORDERING_PARTY_SUPPLIER_ASSIGNED_ACCOUNT_ID_MISSING ("Die ID des Auftraggebers im System des Rechnungsstellers fehlt.",
                                                         "Failed to get supplier assigned account ID."),
    ORDER_REFERENCE_MISSING ("Die Auftragsreferenz fehlt.", "The order reference is missing."),
    ORDER_REFERENCE_MISSING_IGNORE_ORDER_POS ("Die Auftragsreferenz fehlt, daher kann auch die Bestellpositionsnummer nicht übernommen werden.",
                                              "The order reference is missing and therefore the order position number cannot be used."),
    ORDER_REFERENCE_TOO_LONG ("Die Auftragsreferenz ''{0}'' ist zu lang und wurde nach {1} Zeichen abgeschnitten.",
                              "Order reference value ''{0}'' is too long and was cut to {1} characters."),
    UNSUPPORTED_TAX_SCHEME_ID ("Die Steuerschema ID ''{0}'' ist ungültig.", "The tax scheme ID ''{0}'' is invalid."),
    TAX_PERCENT_MISSING ("Es konnte kein Steuersatz für diese Steuerkategorie ermittelt werden.",
                         "No tax percentage could be determined for this tax category."),
    TAXABLE_AMOUNT_MISSING ("Es konnte kein Steuerbasisbetrag (der Betrag auf den die Steuer anzuwenden ist) für diese Steuerkategorie ermittelt werden.",
                            "No taxable amount could be determined for this tax category."),
    UNSUPPORTED_TAX_SCHEME ("Nicht unterstütztes Steuerschema gefunden: ''{0}'' und ''{1}''.",
                            "Other tax scheme found and ignored: ''{0}'' and ''{1}''."),
    DETAILS_TAX_PERCENTAGE_NOT_FOUND ("Der Steuersatz der Rechnungszeile konnte nicht ermittelt werden. Verwende den Standardwert {0}%.",
                                      "Failed to resolve tax percentage for invoice line. Defaulting to {0}%."),
    DETAILS_INVALID_POSITION ("Die Rechnungspositionsnummer ''{0}'' ist ungültig, sie muss größer als 0 sein.",
                              "The UBL invoice line ID ''{0}'' is invalid. The ID must be bigger than 0."),
    DETAILS_INVALID_POSITION_SET_TO_INDEX ("Die Rechnungspositionsnummer ''{0}'' ist nicht numerisch. Es wird der Index {1} verwendet.",
                                           "The UBL invoice line ID ''{0}'' is not numeric. Defaulting to index {1}."),
    DETAILS_INVALID_UNIT ("Die Rechnungszeile hat keine Mengeneinheit. Verwende den Standardwert ''{0}''.",
                          "The UBL invoice line has no unit of measure. Defaulting to ''{0}''."),
    DETAILS_INVALID_QUANTITY ("Die Rechnungszeile hat keine Menge. Verwende den Standardwert ''{0}''.",
                              "The UBL invoice line has no quantity. Defaulting to ''{0}''."),
    VAT_ITEM_MISSING ("Keine einzige Steuersumme gefunden", "No single VAT item found."),
    ALLOWANCE_CHARGE_NO_TAXRATE ("Die Steuerprozentrate für den globalen Zuschlag/Abschlag konnte nicht ermittelt werden.",
                                 "Failed to resolve tax rate percentage for global AllowanceCharge."),
    PAYMENTMEANS_CODE_INVALID ("Der PaymentMeansCode ''{0}'' ist ungültig. Für Überweisungen muss {1} verwenden werden und für Lastschriftverfahren {2}.",
                               "The PaymentMeansCode ''{0}'' is invalid. For credit/debit transfer use {1} and for direct debit use {2}."),
    PAYMENT_ID_TOO_LONG_CUT ("Die Zahlungsreferenz ''{0}'' ist zu lang und wird abgeschnitten.",
                             "The payment reference ''{0}'' is too long and therefore cut."),
    BIC_INVALID ("Der BIC ''{0}'' ist ungültig.", "The BIC ''{0}'' is invalid."),
    IBAN_TOO_LONG_STRIPPING ("Der IBAN ''{0}'' ist zu lang. Er wurde nach {1} Zeichen abgeschnitten.",
                             "The IBAN ''{0}'' is too long and was cut to {1} characters."),
    PAYMENTMEANS_UNSUPPORTED_CHANNELCODE ("Die Zahlungsart mit dem ChannelCode ''{0}'' wird ignoriert.",
                                          "The payment means with ChannelCode ''{0}'' are ignored."),
    ERB_NO_PAYMENT_METHOD ("Es muss eine Zahlungsart angegeben werden.", "A payment method must be provided."),
    PAYMENT_DUE_DATE_ALREADY_CONTAINED ("Es wurde mehr als ein Zahlungsziel gefunden.", "More than one payment due date was found."),
    SETTLEMENT_PERIOD_MISSING ("Für Skontoeinträge muss mindestens ein Endedatum angegeben werden.",
                               "Discount items require a settlement end date."),
    PENALTY_NOT_ALLOWED ("Strafzuschläge werden in ebInterface nicht unterstützt.", "Penalty surcharges are not supported in ebInterface."),
    DISCOUNT_WITHOUT_DUEDATE ("Skontoeinträge können nur angegeben werden, wenn auch ein Zahlungsziel angegeben wurde.",
                              "Discount items can only be provided if a payment due date is present."),
    DELIVERY_WITHOUT_NAME ("Wenn eine Delivery/DeliveryLocation/Address angegeben ist muss auch ein Delivery/DeliveryParty/PartyName/Name angegeben werden.",
                           "If a Delivery/DeliveryLocation/Address is present, a Delivery/DeliveryParty/PartyName/Name must also be present."),
    ERB_NO_DELIVERY_DATE ("Ein Lieferdatum oder ein Leistungszeitraum muss vorhanden sein.",
                          "A Delivery/DeliveryDate or an InvoicePeriod must be present."),
    PREPAID_NOT_SUPPORTED ("Das Element <PrepaidAmount> wird nicht unterstützt.", "The <PrepaidAmount> element is not supported!"),
    MISSING_TAXCATEGORY_ID ("Das Element <ID> fehlt.", "Element <ID> is missing."),
    MISSING_TAXCATEGORY_ID_VALUE ("Das Element <ID> hat keinen Wert.", "Element <ID> has no value."),
    MISSING_TAXCATEGORY_TAXSCHEME_ID ("Das Element <ID> fehlt.", "Element <ID> is missing."),
    MISSING_TAXCATEGORY_TAXSCHEME_ID_VALUE ("Das Element <ID> hat keinen Wert.", "Element <ID> has no value."),
    EBI40_CANNOT_MIX_VAT_EXEMPTION ("In ebInterface 4.0 können nicht USt-Informationen und Steuerbefreiungen gemischt werden",
                                    "ebInterface 4.0 cannot mix VAT information and tax exemptions");

    private final IMultilingualText m_aTP;

    EText (@Nonnull final String sDE, @Nonnull final String sEN)
    {
      m_aTP = TextHelper.create_DE_EN (sDE, sEN);
    }

    @Nullable
    public String getDisplayText (@Nonnull final Locale aContentLocale)
    {
      return DefaultTextResolver.getTextStatic (this, m_aTP, aContentLocale);
    }
  }

  public static final String EBI_GENERATING_SYSTEM_40 = "UBL 2.1 to ebInterface 4.0 converter";
  public static final String EBI_GENERATING_SYSTEM_41 = "UBL 2.1 to ebInterface 4.1 converter";
  public static final String EBI_GENERATING_SYSTEM_42 = "UBL 2.1 to ebInterface 4.2 converter";
  public static final String EBI_GENERATING_SYSTEM_43 = "UBL 2.1 to ebInterface 4.3 converter";
  public static final String EBI_GENERATING_SYSTEM_50 = "UBL 2.1 to ebInterface 5.0 converter";
  public static final String EBI_GENERATING_SYSTEM_60 = "UBL 2.1 to ebInterface 6.0 converter";
  public static final String EBI_GENERATING_SYSTEM_61 = "UBL 2.1 to ebInterface 6.1 converter";

  protected final IToEbinterfaceSettings m_aSettings;

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be
   *        <code>null</code>.
   * @param aSettings
   *        Conversion settings to be used. May not be <code>null</code>.
   */
  protected AbstractToEbInterfaceConverter (@Nonnull final Locale aDisplayLocale,
                                            @Nonnull final Locale aContentLocale,
                                            @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale);
    m_aSettings = ValueEnforcer.notNull (aSettings, "Settings");
  }

  @Nonnull
  protected static String getAllowanceChargeComment (@Nonnull final AllowanceChargeType aUBLAllowanceCharge)
  {
    // AllowanceChargeReason to Comment
    final StringBuilder aSB = new StringBuilder ();
    for (final AllowanceChargeReasonType aUBLReason : aUBLAllowanceCharge.getAllowanceChargeReason ())
    {
      final String sReason = StringHelper.trim (aUBLReason.getValue ());
      if (StringHelper.hasText (sReason))
      {
        if (aSB.length () > 0)
          aSB.append ('\n');
        aSB.append (sReason);
      }
    }
    return aSB.toString ();
  }

  /**
   * Check if the passed UBL invoice is transformable
   *
   * @param aUBLInvoice
   *        The UBL invoice to check. May not be <code>null</code>.
   * @param aTransformationErrorList
   *        The error list to be filled. May not be <code>null</code>.
   */
  protected final void checkInvoiceConsistency (@Nonnull final InvoiceType aUBLInvoice, @Nonnull final ErrorList aTransformationErrorList)
  {
    // Check UBLVersionID
    final UBLVersionIDType aUBLVersionID = aUBLInvoice.getUBLVersionID ();
    if (aUBLVersionID == null)
    {
      // E.g. optional for EN invoices
      if (m_aSettings.isUBLVersionIDMandatory ())
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("UBLVersionID")
                                                 .errorText (EText.NO_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                             UBL_VERSION_20,
                                                                                                             UBL_VERSION_21,
                                                                                                             UBL_VERSION_22,
                                                                                                             UBL_VERSION_23))
                                                 .build ());
    }
    else
    {
      final String sUBLVersionID = StringHelper.trim (aUBLVersionID.getValue ());
      if (!UBL_VERSION_20.equals (sUBLVersionID) &&
          !UBL_VERSION_21.equals (sUBLVersionID) &&
          !UBL_VERSION_22.equals (sUBLVersionID) &&
          !UBL_VERSION_23.equals (sUBLVersionID))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("UBLVersionID")
                                                 .errorText (EText.INVALID_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                  sUBLVersionID,
                                                                                                                  UBL_VERSION_20,
                                                                                                                  UBL_VERSION_21,
                                                                                                                  UBL_VERSION_22,
                                                                                                                  UBL_VERSION_23))
                                                 .build ());
      }
    }

    // Check ProfileID
    final ProfileIDType aProfileID = aUBLInvoice.getProfileID ();
    if (aProfileID == null)
    {
      if (m_aSettings.isUBLProfileIDMandatory ())
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .errorFieldName ("ProfileID")
                                                 .errorText (EText.NO_PROFILE_ID.getDisplayText (m_aDisplayLocale))
                                                 .build ());
    }
    else
    {
      final String sProfileID = StringHelper.trim (aProfileID.getValue ());
      final IProcessIdentifier aProcID = m_aSettings.getProfileIDResolver ().apply (sProfileID);
      if (aProcID == null)
      {
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .errorFieldName ("ProfileID")
                                                 .errorText (EText.INVALID_PROFILE_ID.getDisplayTextWithArgs (m_aDisplayLocale, sProfileID))
                                                 .build ());
      }
    }

    // The CustomizationID can be basically anything - we don't care here

    // Invoice type code
    final InvoiceTypeCodeType aInvoiceTypeCode = aUBLInvoice.getInvoiceTypeCode ();
    if (aInvoiceTypeCode == null)
    {
      // None present
      aTransformationErrorList.add (SingleError.builderWarn ()
                                               .errorFieldName ("InvoiceTypeCode")
                                               .errorText (EText.NO_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                             StringHelper.getImploded (", ",
                                                                                                                                       INVOICE_TYPE_CODES)))
                                               .build ());
    }
    else
    {
      // If one is present, it must match
      final String sInvoiceTypeCode = StringHelper.trim (aInvoiceTypeCode.getValue ());
      if (!INVOICE_TYPE_CODES.contains (sInvoiceTypeCode))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("InvoiceTypeCode")
                                                 .errorText (EText.INVALID_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                    sInvoiceTypeCode,
                                                                                                                    StringHelper.getImploded (", ",
                                                                                                                                              INVOICE_TYPE_CODES)))
                                                 .build ());
      }
    }
  }

  /**
   * Check if the passed UBL invoice is transformable
   *
   * @param aUBLCreditNote
   *        The UBL invoice to check. May not be <code>null</code>.
   * @param aTransformationErrorList
   *        The error list to be filled. May not be <code>null</code>.
   */
  protected final void checkCreditNoteConsistency (@Nonnull final CreditNoteType aUBLCreditNote,
                                                   @Nonnull final ErrorList aTransformationErrorList)
  {
    // Check UBLVersionID
    final UBLVersionIDType aUBLVersionID = aUBLCreditNote.getUBLVersionID ();
    if (aUBLVersionID == null)
    {
      // For EN invoices
      if (m_aSettings.isUBLVersionIDMandatory ())
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("UBLVersionID")
                                                 .errorText (EText.NO_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                             UBL_VERSION_20,
                                                                                                             UBL_VERSION_21,
                                                                                                             UBL_VERSION_22,
                                                                                                             UBL_VERSION_23))
                                                 .build ());
    }
    else
    {
      final String sUBLVersionID = StringHelper.trim (aUBLVersionID.getValue ());
      if (!UBL_VERSION_20.equals (sUBLVersionID) &&
          !UBL_VERSION_21.equals (sUBLVersionID) &&
          !UBL_VERSION_22.equals (sUBLVersionID) &&
          !UBL_VERSION_23.equals (sUBLVersionID))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("UBLVersionID")
                                                 .errorText (EText.INVALID_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                  sUBLVersionID,
                                                                                                                  UBL_VERSION_20,
                                                                                                                  UBL_VERSION_21,
                                                                                                                  UBL_VERSION_22,
                                                                                                                  UBL_VERSION_23))
                                                 .build ());
      }
    }

    // Check ProfileID
    final ProfileIDType aProfileID = aUBLCreditNote.getProfileID ();
    if (aProfileID == null)
    {
      if (m_aSettings.isUBLProfileIDMandatory ())
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .errorFieldName ("ProfileID")
                                                 .errorText (EText.NO_PROFILE_ID.getDisplayText (m_aDisplayLocale))
                                                 .build ());
    }
    else
    {
      final String sProfileID = StringHelper.trim (aProfileID.getValue ());
      final IProcessIdentifier aProcID = m_aSettings.getProfileIDResolver ().apply (sProfileID);
      if (aProcID == null)
      {
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .errorFieldName ("ProfileID")
                                                 .errorText (EText.INVALID_PROFILE_ID.getDisplayTextWithArgs (m_aDisplayLocale, sProfileID))
                                                 .build ());
      }
    }

    // The CustomizationID can be basically anything - we don't care here
  }

  protected static final boolean isTaxExemptionCategoryID (@Nullable final String sUBLTaxCategoryID)
  {
    // https://www.unece.org/fileadmin/DAM/trade/untdid/d16b/tred/tred5305.htm
    // AE = VAT Reverse Charge
    // E = Exempt from tax
    // O = Services outside scope of tax
    return "AE".equals (sUBLTaxCategoryID) || "E".equals (sUBLTaxCategoryID) || "O".equals (sUBLTaxCategoryID);
  }

  protected static boolean isVATSchemeID (final String sScheme)
  {
    // Peppol
    if (SUPPORTED_TAX_SCHEME_ID.equals (sScheme))
      return true;

    // EN invoices
    if ("VA".equals (sScheme))
      return true;

    return false;
  }

  @Nullable
  protected static TaxCategoryType findTaxCategory (@Nonnull final List <TaxTotalType> aUBLTaxTotals)
  {
    // No direct tax category -> check if it is somewhere in the tax total
    for (final TaxTotalType aUBLTaxTotal : aUBLTaxTotals)
    {
      for (final TaxSubtotalType aUBLTaxSubTotal : aUBLTaxTotal.getTaxSubtotal ())
      {
        // Only handle VAT items
        if (isVATSchemeID (aUBLTaxSubTotal.getTaxCategory ().getTaxScheme ().getIDValue ()))
        {
          // We found one -> just use it
          return aUBLTaxSubTotal.getTaxCategory ();
        }
      }
    }
    return null;
  }

  /**
   * Get a string in the form
   * [string][sep][string][sep][string][or][last-string]. So the last and the
   * second last entries are separated by " or " whereas the other entries are
   * separated by the provided separator.
   *
   * @param sSep
   *        Separator to use. May not be <code>null</code>.
   * @param aValues
   *        Values to be combined.
   * @return The combined string. Never <code>null</code>.
   */
  @Nonnull
  protected final String getOrString (@Nonnull final String sSep, @Nullable final String... aValues)
  {
    final StringBuilder aSB = new StringBuilder ();
    if (aValues != null)
    {
      final int nSecondLast = aSB.length () - 2;
      for (int i = 0; i < aValues.length; ++i)
      {
        if (i > 0)
        {
          if (i == nSecondLast)
            aSB.append (EText.OR.getDisplayText (m_aDisplayLocale));
          else
            aSB.append (sSep);
        }
        aSB.append (aValues[i]);
      }
    }
    return aSB.toString ();
  }

  protected static boolean isUniversalBankTransaction (@Nullable final String sPaymentMeansCode)
  {
    // 30 = Credit transfer
    // 31 = Debit transfer
    // 42 = Payment to bank account
    // 58 = SEPA credit transfer
    return "30".equals (sPaymentMeansCode) ||
           "31".equals (sPaymentMeansCode) ||
           "42".equals (sPaymentMeansCode) ||
           "58".equals (sPaymentMeansCode);
  }

  protected static boolean isDirectDebit (@Nullable final String sPaymentMeansCode)
  {
    // 49 = Direct debit
    return "49".equals (sPaymentMeansCode);
  }

  protected static boolean isSEPADirectDebit (@Nullable final String sPaymentMeansCode)
  {
    // 59 = SEPA direct debit
    return "59".equals (sPaymentMeansCode);
  }

  protected static boolean isIBAN (@Nullable final String sPaymentChannelCode)
  {
    // null/empty for standard Peppol BIS
    return StringHelper.hasNoText (sPaymentChannelCode) || PAYMENT_CHANNEL_CODE_IBAN.equals (sPaymentChannelCode);
  }

  protected static boolean isBIC (@Nullable final String sScheme)
  {
    return StringHelper.hasNoText (sScheme) || SCHEME_BIC.equalsIgnoreCase (sScheme);
  }
}
