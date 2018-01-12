/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2018 AUSTRIAPRO - www.austriapro.at
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

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.Translatable;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.commons.text.IMultilingualText;
import com.helger.commons.text.display.IHasDisplayTextWithArgs;
import com.helger.commons.text.resolve.DefaultTextResolver;
import com.helger.commons.text.util.TextHelper;
import com.helger.ebinterface.ubl.AbstractConverter;
import com.helger.peppol.identifier.factory.PeppolIdentifierFactory;
import com.helger.peppol.identifier.generic.process.IProcessIdentifier;
import com.helger.peppol.identifier.peppol.doctype.IPeppolPredefinedDocumentTypeIdentifier;
import com.helger.peppol.identifier.peppol.process.IPeppolPredefinedProcessIdentifier;
import com.helger.peppol.identifier.peppol.process.PredefinedProcessIdentifierManager;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.AllowanceChargeReasonType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.CustomizationIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InvoiceTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.ProfileIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.UBLVersionIDType;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Base class for PEPPOL UBL to ebInterface converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractToEbInterfaceConverter extends AbstractConverter
{
  @Translatable
  public static enum EText implements IHasDisplayTextWithArgs
  {
    OR ("oder", "or"),
    NO_UBL_VERSION_ID ("Die UBLVersionID fehlt. Es wird der Wert ''{0}'' oder ''{1}'' erwartet.",
                       "No UBLVersionID present. It must be ''{0}'' or ''{1}''."),
    INVALID_UBL_VERSION_ID ("Die UBLVersionID ''{0}'' ist ungültig. Diese muss den Wert ''{1}'' oder ''{2}'' haben.",
                            "Invalid UBLVersionID value ''{0}'' present. It must be ''{1}'' or ''{2}''."),
    NO_PROFILE_ID ("Die ProfileID fehlt", "No ProfileID present."),
    INVALID_PROFILE_ID ("Die ProfileID ''{0}'' ist ungültig.", "Invalid ProfileID value ''{0}'' present."),
    NO_CUSTOMIZATION_ID ("Die CustomizationID fehlt", "No CustomizationID present."),
    INVALID_CUSTOMIZATION_SCHEME_ID ("Die CustomizationID schemeID ''{0}'' ist ungültig. Diese muss den Wert ''{1}'' haben.",
                                     "Invalid CustomizationID schemeID value ''{0}'' present. It must be ''{1}''."),
    INVALID_CUSTOMIZATION_ID ("Die angegebene CustomizationID ''{0}'' ist ungültig. Sie wird vom angegebenen Profil nicht unterstützt.",
                              "Invalid CustomizationID value ''{0}'' present. It is not supported by the passed profile."),
    NO_INVOICE_TYPECODE ("Der InvoiceTypeCode fehlt. Es wird der Wert ''{0}'' erwartet.",
                         "No InvoiceTypeCode present. It must be ''{0}''."),
    INVALID_INVOICE_TYPECODE ("Der InvoiceTypeCode ''{0}'' ist ungültig. Dieser muss den Wert ''{1}'' haben.",
                              "Invalid InvoiceTypeCode value ''{0}'' present. It must be ''{1}''."),
    ADDRESS_NO_STREET ("In der Adresse fehlt die Straße.", "Address is missing a street name."),
    ADDRESS_NO_CITY ("In der Adresse fehlt der Name der Stadt.", "Address is missing a city name."),
    ADDRESS_NO_ZIPCODE ("In der Adresse fehlt die PLZ.", "Address is missing a ZIP code."),
    ADDRESS_INVALID_COUNTRY ("Der angegebene Ländercode ''{0}'' ist ungültig.",
                             "The provided country code ''{0}'' is invalid."),
    ADDRESS_NO_COUNTRY ("In der Adresse fehlt der Name des Landes.", "Address is missing a country."),
    MULTIPLE_PARTIES ("Es sind mehrere Partynamen vorhanden - nur der erste wird verwendet.",
                      "Multiple party names present - only the first one is used."),
    PARTY_NO_NAME ("Der Name der Party fehlt.", "Party name is missing."),
    PARTY_UNSUPPORTED_ENDPOINT ("Ignoriere den Enpunkt ''{0}'' des Typs ''{1}''.",
                                "Ignoring endpoint ID ''{0}'' of type ''{1}''."),
    PARTY_UNSUPPORTED_ADDRESS_IDENTIFIER ("Ignoriere die ID ''{0}'' des Typs ''{1}''.",
                                          "Ignoring identification ''{0}'' of type ''{1}''."),
    ORDERLINE_REF_ID_EMPTY ("Es muss ein Wert für die Bestellpositionsnummer angegeben werden.",
                            "A value must be provided for the order line reference ID."),
    ALPHANUM_ID_TYPE_CHANGE ("''{0}'' ist ein ungültiger Typ und wurde auf ''{1}'' geändert.",
                             "''{0}'' is an invalid value and was changed to ''{1}''."),
    INVALID_CURRENCY_CODE ("Der angegebene Währungscode ''{0}'' ist ungültig.",
                           "Invalid currency code ''{0}'' provided."),
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
    ORDERING_PARTY_VAT_MISSING ("Die UID-Nummer des Auftraggebers fehlt. Verwenden Sie 'ATU00000000' für österreichische Empfänger an wenn keine UID-Nummer notwendig ist.",
                                "Failed to get ordering party VAT identification number. Use 'ATU00000000' for Austrian invoice recipients if no VAT identification number is required."),
    ORDERING_PARTY_PARTY_MISSING ("Die Adressdaten des Auftraggebers fehlen.",
                                  "The party information of the ordering party are missing."),
    ORDERING_PARTY_SUPPLIER_ASSIGNED_ACCOUNT_ID_MISSING ("Die ID des Auftraggebers im System des Rechnungsstellers fehlt.",
                                                         "Failed to get supplier assigned account ID."),
    ORDER_REFERENCE_MISSING ("Die Auftragsreferenz fehlt.", "Failed to get order reference ID."),
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
    DETAILS_INVALID_POSITION ("Die Rechnungspositionsnummer ''{0}'' ist nicht numerisch. Es wird der Index {1} verwendet.",
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
    IBAN_TOO_LONG ("Der IBAN ''{0}'' ist zu lang. Er wurde nach {1} Zeichen abgeschnitten.",
                   "The IBAN ''{0}'' is too long and was cut to {1} characters."),
    PAYMENTMEANS_UNSUPPORTED_CHANNELCODE ("Die Zahlungsart mit dem ChannelCode ''{0}'' wird ignoriert.",
                                          "The payment means with ChannelCode ''{0}'' are ignored."),
    ERB_NO_PAYMENT_METHOD ("Es muss eine Zahlungsart angegeben werden.", "A payment method must be provided."),
    PAYMENT_DUE_DATE_ALREADY_CONTAINED ("Es wurde mehr als ein Zahlungsziel gefunden.",
                                        "More than one payment due date was found."),
    SETTLEMENT_PERIOD_MISSING ("Für Skontoeinträge muss mindestens ein Endedatum angegeben werden.",
                               "Discount items require a settlement end date."),
    PENALTY_NOT_ALLOWED ("Strafzuschläge werden in ebInterface nicht unterstützt.",
                         "Penalty surcharges are not supported in ebInterface."),
    DISCOUNT_WITHOUT_DUEDATE ("Skontoeinträge können nur angegeben werden, wenn auch ein Zahlungsziel angegeben wurde.",
                              "Discount items can only be provided if a payment due date is present."),
    DELIVERY_WITHOUT_NAME ("Wenn eine Delivery/DeliveryLocation/Address angegeben ist muss auch ein Delivery/DeliveryParty/PartyName/Name angegeben werden.",
                           "If a Delivery/DeliveryLocation/Address is present, a Delivery/DeliveryParty/PartyName/Name must also be present."),
    ERB_NO_DELIVERY_DATE ("Ein Lieferdatum oder ein Leistungszeitraum muss vorhanden sein.",
                          "A Delivery/DeliveryDate or an InvoicePeriod must be present."),
    PREPAID_NOT_SUPPORTED ("Das Element <PrepaidAmount> wird nicht unterstützt.",
                           "The <PrepaidAmount> element is not supported!"),
    MISSING_TAXCATEGORY_ID ("Das Element <ID> fehlt.", "Element <ID> is missing."),
    MISSING_TAXCATEGORY_ID_VALUE ("Das Element <ID> hat keinen Wert.", "Element <ID> has no value.");

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
   * The fake email address used by PEPPOL when no biller email address is in
   * the original XML file
   */
  public static final String PEPPOL_FAKE_BILLER_EMAIL_ADDRESS = "no-email-address-provided@peppol.eu";

  protected final boolean m_bStrictERBMode;

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be
   *        <code>null</code>.
   * @param bStrictERBMode
   *        <code>true</code> if E-RECHNUNG.GV.AT specific checks should be
   *        performed
   */
  public AbstractToEbInterfaceConverter (@Nonnull final Locale aDisplayLocale,
                                         @Nonnull final Locale aContentLocale,
                                         final boolean bStrictERBMode)
  {
    super (aDisplayLocale, aContentLocale);
    m_bStrictERBMode = bStrictERBMode;
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
   *        The UBL invoice to check
   */
  protected final void checkInvoiceConsistency (@Nonnull final InvoiceType aUBLInvoice,
                                                @Nonnull final ErrorList aTransformationErrorList)
  {
    // Check UBLVersionID
    final UBLVersionIDType aUBLVersionID = aUBLInvoice.getUBLVersionID ();
    if (aUBLVersionID == null)
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName ("UBLVersionID")
                                               .setErrorText (EText.NO_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                              UBL_VERSION_20,
                                                                                                              UBL_VERSION_21))
                                               .build ());
    }
    else
    {
      final String sUBLVersionID = StringHelper.trim (aUBLVersionID.getValue ());
      if (!UBL_VERSION_20.equals (sUBLVersionID) && !UBL_VERSION_21.equals (sUBLVersionID))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("UBLVersionID")
                                                 .setErrorText (EText.INVALID_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                     sUBLVersionID,
                                                                                                                     UBL_VERSION_20,
                                                                                                                     UBL_VERSION_21))
                                                 .build ());
      }
    }

    // Check ProfileID
    IProcessIdentifier aProcID = null;
    final ProfileIDType aProfileID = aUBLInvoice.getProfileID ();
    if (aProfileID == null)
    {
      aTransformationErrorList.add (SingleError.builderWarn ()
                                               .setErrorFieldName ("ProfileID")
                                               .setErrorText (EText.NO_PROFILE_ID.getDisplayText (m_aDisplayLocale))
                                               .build ());
    }
    else
    {
      final String sProfileID = StringHelper.trim (aProfileID.getValue ());
      aProcID = PredefinedProcessIdentifierManager.getProcessIdentifierOfID (sProfileID);
      if (aProcID == null)
      {
        // Parse basically
        aProcID = PeppolIdentifierFactory.INSTANCE.parseProcessIdentifier (sProfileID);
      }

      if (aProcID == null)
      {
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .setErrorFieldName ("ProfileID")
                                                 .setErrorText (EText.INVALID_PROFILE_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                 sProfileID))
                                                 .build ());
      }
    }

    // Check CustomizationID
    // I'm not quite sure whether the document ID or "PEPPOL" should be used!
    // if (false)
    // {
    // final CustomizationIDType aCustomizationID =
    // aUBLInvoice.getCustomizationID ();
    // if (aCustomizationID == null)
    // aTransformationErrorList.add (SingleError.builderError
    // ().setErrorFieldName ("CustomizationID",
    // EText.NO_CUSTOMIZATION_ID.getDisplayText (m_aDisplayLocale));
    // else
    // if (!CPeppolUBL.CUSTOMIZATION_SCHEMEID.equals
    // (aCustomizationID.getSchemeID ()))
    // aTransformationErrorList.add (SingleError.builderError
    // ().setErrorFieldName ("CustomizationID/schemeID",
    // EText.INVALID_CUSTOMIZATION_SCHEME_ID.getDisplayTextWithArgs
    // (m_aDisplayLocale,
    // aCustomizationID.getSchemeID (),
    // CPeppolUBL.CUSTOMIZATION_SCHEMEID));
    // else
    // if (aProcID != null)
    // {
    // final String sCustomizationID = StringHelper.trim
    // (aCustomizationID.getValue ());
    // IPeppolPredefinedDocumentTypeIdentifier aMatchingDocID = null;
    // for (final IPeppolPredefinedDocumentTypeIdentifier aDocID :
    // aProcID.getDocumentTypeIdentifiers ())
    // if (aDocID.getAsUBLCustomizationID ().equals (sCustomizationID))
    // {
    // // We found a match
    // aMatchingDocID = aDocID;
    // break;
    // }
    // if (aMatchingDocID == null)
    // aTransformationErrorList.add (SingleError.builderError
    // ().setErrorFieldName ("CustomizationID",
    // EText.INVALID_CUSTOMIZATION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
    // sCustomizationID));
    // }
    // }

    // Invoice type code
    final InvoiceTypeCodeType aInvoiceTypeCode = aUBLInvoice.getInvoiceTypeCode ();
    if (aInvoiceTypeCode == null)
    {
      // None present
      aTransformationErrorList.add (SingleError.builderWarn ()
                                               .setErrorFieldName ("InvoiceTypeCode")
                                               .setErrorText (EText.NO_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                INVOICE_TYPE_CODE))
                                               .build ());
    }
    else
    {
      // If one is present, it must match
      final String sInvoiceTypeCode = StringHelper.trim (aInvoiceTypeCode.getValue ());
      if (!INVOICE_TYPE_CODE.equals (sInvoiceTypeCode))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("InvoiceTypeCode")
                                                 .setErrorText (EText.INVALID_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                       sInvoiceTypeCode,
                                                                                                                       INVOICE_TYPE_CODE))
                                                 .build ());
      }
    }
  }

  /**
   * Check if the passed UBL invoice is transformable
   *
   * @param aUBLCreditNote
   *        The UBL invoice to check
   */
  protected final void checkCreditNoteConsistency (@Nonnull final CreditNoteType aUBLCreditNote,
                                                   @Nonnull final ErrorList aTransformationErrorList)
  {
    // Check UBLVersionID
    final UBLVersionIDType aUBLVersionID = aUBLCreditNote.getUBLVersionID ();
    if (aUBLVersionID == null)
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName ("UBLVersionID")
                                               .setErrorText (EText.NO_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                              UBL_VERSION_20,
                                                                                                              UBL_VERSION_21))
                                               .build ());
    }
    else
    {
      final String sUBLVersionID = StringHelper.trim (aUBLVersionID.getValue ());
      if (!UBL_VERSION_20.equals (sUBLVersionID) && !UBL_VERSION_21.equals (sUBLVersionID))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("UBLVersionID")
                                                 .setErrorText (EText.INVALID_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                     sUBLVersionID,
                                                                                                                     UBL_VERSION_20,
                                                                                                                     UBL_VERSION_21))
                                                 .build ());
      }
    }

    // Check ProfileID
    IPeppolPredefinedProcessIdentifier aProcID = null;
    final ProfileIDType aProfileID = aUBLCreditNote.getProfileID ();
    if (aProfileID == null)
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName ("ProfileID")
                                               .setErrorText (EText.NO_PROFILE_ID.getDisplayText (m_aDisplayLocale))
                                               .build ());
    }
    else
    {
      final String sProfileID = StringHelper.trim (aProfileID.getValue ());
      aProcID = PredefinedProcessIdentifierManager.getProcessIdentifierOfID (sProfileID);
      if (aProcID == null)
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("ProfileID")
                                                 .setErrorText (EText.INVALID_PROFILE_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                 sProfileID))
                                                 .build ());
      }
    }

    // Check CustomizationID
    // I'm not quite sure whether the document ID or "PEPPOL" should be used!
    if (false)
    {
      final CustomizationIDType aCustomizationID = aUBLCreditNote.getCustomizationID ();
      if (aCustomizationID == null)
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("CustomizationID")
                                                 .setErrorText (EText.NO_CUSTOMIZATION_ID.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      else
        if (!CUSTOMIZATION_SCHEMEID.equals (aCustomizationID.getSchemeID ()))
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .setErrorFieldName ("CustomizationID/schemeID")
                                                   .setErrorText (EText.INVALID_CUSTOMIZATION_SCHEME_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                                aCustomizationID.getSchemeID (),
                                                                                                                                CUSTOMIZATION_SCHEMEID))
                                                   .build ());
        else
          if (aProcID != null)
          {
            final String sCustomizationID = StringHelper.trim (aCustomizationID.getValue ());
            IPeppolPredefinedDocumentTypeIdentifier aMatchingDocID = null;
            for (final IPeppolPredefinedDocumentTypeIdentifier aDocID : aProcID.getDocumentTypeIdentifiers ())
              if (aDocID.getAsUBLCustomizationID ().equals (sCustomizationID))
              {
                // We found a match
                aMatchingDocID = aDocID;
                break;
              }
            if (aMatchingDocID == null)
              aTransformationErrorList.add (SingleError.builderError ()
                                                       .setErrorFieldName ("CustomizationID")
                                                       .setErrorText (EText.INVALID_CUSTOMIZATION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                             sCustomizationID))
                                                       .build ());
          }
    }
  }

  protected static final boolean isTaxExemptionCategoryID (@Nullable final String sUBLTaxCategoryID)
  {
    // https://www.unece.org/fileadmin/DAM/trade/untdid/d16b/tred/tred5305.htm
    // AE = VAT Reverse Charge
    // E = Exempt from tax
    // O = Services outside scope of tax
    return "AE".equals (sUBLTaxCategoryID) || "E".equals (sUBLTaxCategoryID) || "O".equals (sUBLTaxCategoryID);
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
   * @return
   */
  @Nonnull
  protected String getOrString (@Nonnull final String sSep, @Nullable final String... aValues)
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
}
