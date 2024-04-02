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
package at.austriapro.ebinterface.ubl.from.creditnote;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.datetime.XMLOffsetDate;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.math.MathHelper;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.ebinterface.v40.*;

import at.austriapro.ebinterface.ubl.from.AbstractToEbInterface40Converter;
import at.austriapro.ebinterface.ubl.from.IToEbinterfaceSettings;
import at.austriapro.ebinterface.ubl.from.helper.SchemedID;
import at.austriapro.ebinterface.ubl.from.helper.TaxCategoryKey;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CreditNoteLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderLineReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyTaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSubtotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxTotalType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.AdditionalAccountIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NameType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;

/**
 * Main converter between UBL 2.1 credit note and ebInterface 4.0 credit note.
 *
 * @author Philip Helger
 */
@Immutable
public final class CreditNoteToEbInterface40Converter extends AbstractToEbInterface40Converter
{
  private ICustomCreditNoteToEbInterface40Converter m_aCustomizer;

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
  public CreditNoteToEbInterface40Converter (@Nonnull final Locale aDisplayLocale,
                                             @Nonnull final Locale aContentLocale,
                                             @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale, aSettings);
  }

  @Nonnull
  public CreditNoteToEbInterface40Converter setCustomizer (@Nullable final ICustomCreditNoteToEbInterface40Converter aCustomizer)
  {
    m_aCustomizer = aCustomizer;
    return this;
  }

  /**
   * Main conversion method to convert from UBL to ebInterface
   *
   * @param aUBLDoc
   *        The UBL invoice to be converted
   * @param aTransformationErrorList
   *        Error list. Must be empty!
   * @return The created ebInterface document or <code>null</code> in case of a
   *         severe error.
   */
  @Nullable
  public Ebi40InvoiceType convertToEbInterface (@Nonnull final CreditNoteType aUBLDoc,
                                                @Nonnull final ErrorList aTransformationErrorList)
  {
    ValueEnforcer.notNull (aUBLDoc, "UBLCreditNote");
    ValueEnforcer.notNull (aTransformationErrorList, "TransformationErrorList");
    ValueEnforcer.isTrue (aTransformationErrorList.isEmpty (), "TransformationErrorList must be empty!");

    // Consistency check before starting the conversion
    checkCreditNoteConsistency (aUBLDoc, aTransformationErrorList);
    if (aTransformationErrorList.containsAtLeastOneError ())
      return null;

    // Build ebInterface invoice
    final Ebi40InvoiceType aEbiDoc = new Ebi40InvoiceType ();
    aEbiDoc.setGeneratingSystem (EBI_GENERATING_SYSTEM_40);
    aEbiDoc.setDocumentType (getAsDocumentTypeType (aUBLDoc.getCreditNoteTypeCode () == null ? null
                                                                                             : aUBLDoc.getCreditNoteTypeCode ()
                                                                                                      .getName (),
                                                    aUBLDoc.getCreditNoteTypeCodeValue (),
                                                    Ebi40DocumentTypeType.CREDIT_MEMO.value ()));

    // Cannot set the language, because the 3letter code is expected but we only
    // have the 2letter code!

    final String sUBLCurrencyCode = StringHelper.trim (aUBLDoc.getDocumentCurrencyCodeValue ());
    aEbiDoc.setInvoiceCurrency (getCurrencyCode (sUBLCurrencyCode));

    // CreditNote Number
    final String sCreditNoteNumber = StringHelper.trim (aUBLDoc.getIDValue ());
    if (StringHelper.hasNoText (sCreditNoteNumber))
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("ID")
                                               .errorText (EText.MISSING_INVOICE_NUMBER.getDisplayText (m_aDisplayLocale))
                                               .build ());
    aEbiDoc.setInvoiceNumber (sCreditNoteNumber);

    // Ignore the time!
    aEbiDoc.setInvoiceDate (aUBLDoc.getIssueDateValue ());
    if (aEbiDoc.getInvoiceDate () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("IssueDate")
                                               .errorText (EText.MISSING_INVOICE_DATE.getDisplayText (m_aDisplayLocale))
                                               .build ());

    // Biller/Supplier (creator of the invoice)
    {
      final SupplierPartyType aUBLSupplier = aUBLDoc.getAccountingSupplierParty ();
      final Ebi40BillerType aEbiBiller = new Ebi40BillerType ();
      // Find the tax scheme that uses VAT
      if (aUBLSupplier.getParty () != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLSupplier.getParty ().getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (isVATSchemeID (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiBiller.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.hasNoText (aEbiBiller.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aEbiBiller.setVATIdentificationNumber (AT_UNDEFINED_VATIN);
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .errorFieldName ("AccountingSupplierParty/Party/PartyTaxScheme")
                                                 .errorText (EText.BILLER_VAT_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }
      if (aUBLSupplier.getCustomerAssignedAccountID () != null)
      {
        // The customer's internal identifier for the supplier.
        aEbiBiller.setInvoiceRecipientsBillerID (StringHelper.trim (aUBLSupplier.getCustomerAssignedAccountIDValue ()));
      }
      if (StringHelper.hasNoText (aEbiBiller.getInvoiceRecipientsBillerID ()) &&
          aUBLSupplier.getParty () != null &&
          aUBLSupplier.getParty ().hasPartyIdentificationEntries ())
      {
        // New version for BIS V2
        aEbiBiller.setInvoiceRecipientsBillerID (StringHelper.trim (aUBLSupplier.getParty ()
                                                                                .getPartyIdentificationAtIndex (0)
                                                                                .getIDValue ()));
      }

      // Disabled because field is optional
      if (false)
        if (StringHelper.hasNoText (aEbiBiller.getInvoiceRecipientsBillerID ()))
        {
          // Mandatory field
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .errorFieldName ("AccountingSupplierParty/CustomerAssignedAccountID")
                                                   .errorText (EText.ERB_CUSTOMER_ASSIGNED_ACCOUNTID_MISSING.getDisplayText (m_aDisplayLocale))
                                                   .build ());
        }

      for (final AdditionalAccountIDType aUBLAddAccountID : aUBLSupplier.getAdditionalAccountID ())
      {
        final Ebi40FurtherIdentificationType aFI = new Ebi40FurtherIdentificationType ();
        aFI.setIdentificationType ("Consolidator");
        aFI.setValue (StringHelper.trim (aUBLAddAccountID.getValue ()));
        aEbiBiller.addFurtherIdentification (aFI);
      }

      if (aUBLSupplier.getParty () != null)
      {
        aEbiBiller.setAddress (convertParty (aUBLSupplier.getParty (),
                                             "AccountingSupplierParty",
                                             aTransformationErrorList,
                                             m_aContentLocale,
                                             m_aDisplayLocale,
                                             true));

        // Ensure a fake biller email address is present
        if (StringHelper.hasNoText (aEbiBiller.getAddress ().getEmail ()))
          if (m_aSettings.isEnforceSupplierEmailAddress ())
            aEbiBiller.getAddress ().setEmail (m_aSettings.getEnforcedSupplierEmailAddress ());
      }

      // Add contract reference as further identification
      for (final DocumentReferenceType aDocumentReference : aUBLDoc.getContractDocumentReference ())
        if (StringHelper.hasTextAfterTrim (aDocumentReference.getIDValue ()))
        {
          final String sKey = StringHelper.hasText (aDocumentReference.getID ()
                                                                      .getSchemeID ()) ? aDocumentReference.getID ()
                                                                                                           .getSchemeID ()
                                                                                       : "Contract";

          final Ebi40FurtherIdentificationType aEbiFurtherIdentification = new Ebi40FurtherIdentificationType ();
          aEbiFurtherIdentification.setIdentificationType (sKey);
          aEbiFurtherIdentification.setValue (StringHelper.trim (aDocumentReference.getIDValue ()));
          aEbiBiller.addFurtherIdentification (aEbiFurtherIdentification);
        }

      aEbiDoc.setBiller (aEbiBiller);
    }

    // CreditNote recipient
    {
      final CustomerPartyType aUBLCustomer = aUBLDoc.getAccountingCustomerParty ();
      final Ebi40InvoiceRecipientType aEbiRecipient = new Ebi40InvoiceRecipientType ();
      // Find the tax scheme that uses VAT#
      if (aUBLCustomer.getParty () != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLCustomer.getParty ().getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (isVATSchemeID (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiRecipient.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.hasNoText (aEbiRecipient.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aEbiRecipient.setVATIdentificationNumber (AT_UNDEFINED_VATIN);
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .errorFieldName ("AccountingCustomerParty/PartyTaxScheme")
                                                 .errorText (EText.INVOICE_RECIPIENT_VAT_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }
      if (aUBLCustomer.getSupplierAssignedAccountID () != null)
      {
        // UBL: An identifier for the Customer's account, assigned by the
        // Supplier.
        // eb: Identifikation des Rechnungsempfängers beim Rechnungssteller.
        final String sBillersInvoiceRecipientID = StringHelper.trim (aUBLCustomer.getSupplierAssignedAccountIDValue ());
        aEbiRecipient.setBillersInvoiceRecipientID (sBillersInvoiceRecipientID);
      }
      if (StringHelper.hasNoText (aEbiRecipient.getBillersInvoiceRecipientID ()))
      {
        // Check if a fallback is present
        aEbiRecipient.setBillersInvoiceRecipientID (m_aSettings.getFallbackBillersInvoiceRecipientID ());
      }
      if (StringHelper.hasNoText (aEbiRecipient.getBillersInvoiceRecipientID ()))
      {
        // BillersInvoiceRecipientID is mandatory in ebi 4.0
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("AccountingCustomerParty/SupplierAssignedAccountID")
                                                 .errorText (EText.INVOICE_RECIPIENT_PARTY_SUPPLIER_ASSIGNED_ACCOUNT_ID_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      if (aUBLCustomer.getParty () != null)
        aEbiRecipient.setAddress (convertParty (aUBLCustomer.getParty (),
                                                "AccountingCustomerParty",
                                                aTransformationErrorList,
                                                m_aContentLocale,
                                                m_aDisplayLocale,
                                                true));
      if (aEbiRecipient.getAddress () == null)
      {
        // Required by ebInterface
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("AccountingCustomerParty/Party")
                                                 .errorText (EText.INVOICE_RECIPIENT_PARTY_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      aEbiDoc.setInvoiceRecipient (aEbiRecipient);
    }

    // Ordering party
    final CustomerPartyType aUBLBuyer = aUBLDoc.getBuyerCustomerParty ();
    if (aUBLBuyer != null)
    {
      final Ebi40OrderingPartyType aEbiOrderingParty = new Ebi40OrderingPartyType ();
      // Find the tax scheme that uses VAT
      if (aUBLBuyer.getParty () != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLBuyer.getParty ().getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (isVATSchemeID (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiOrderingParty.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.hasNoText (aEbiOrderingParty.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("BuyerCustomerParty/PartyTaxScheme")
                                                 .errorText (EText.ORDERING_PARTY_VAT_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      if (aUBLBuyer.getParty () != null)
        aEbiOrderingParty.setAddress (convertParty (aUBLBuyer.getParty (),
                                                    "BuyerCustomerParty",
                                                    aTransformationErrorList,
                                                    m_aContentLocale,
                                                    m_aDisplayLocale,
                                                    true));
      if (aEbiOrderingParty.getAddress () == null)
      {
        // Required by ebInterface
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("BuyerCustomerParty/Party")
                                                 .errorText (EText.ORDERING_PARTY_PARTY_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      if (aUBLBuyer.getSupplierAssignedAccountID () != null)
      {
        // The billers internal identifier for the ordering party.
        aEbiOrderingParty.setBillersOrderingPartyID (StringHelper.trim (aUBLBuyer.getSupplierAssignedAccountIDValue ()));
      }
      if (StringHelper.hasNoText (aEbiOrderingParty.getBillersOrderingPartyID ()) &&
          aUBLBuyer.getParty () != null &&
          aUBLBuyer.getParty ().hasPartyIdentificationEntries ())
      {
        // New version for BIS V2
        aEbiOrderingParty.setBillersOrderingPartyID (StringHelper.trim (aUBLBuyer.getParty ()
                                                                                 .getPartyIdentificationAtIndex (0)
                                                                                 .getIDValue ()));
      }
      if (StringHelper.hasNoText (aEbiOrderingParty.getBillersOrderingPartyID ()) &&
          aEbiDoc.getInvoiceRecipient () != null)
      {
        // Use the same as the the invoice recipient ID
        // Heuristics, but what should I do :(
        aEbiOrderingParty.setBillersOrderingPartyID (aEbiDoc.getInvoiceRecipient ().getBillersInvoiceRecipientID ());
      }
      if (StringHelper.hasNoText (aEbiOrderingParty.getBillersOrderingPartyID ()))
      {
        // Required by ebInterface
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("BuyerCustomerParty/SupplierAssignedAccountID")
                                                 .errorText (EText.ORDERING_PARTY_SUPPLIER_ASSIGNED_ACCOUNT_ID_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      aEbiDoc.setOrderingParty (aEbiOrderingParty);
    }

    // Order reference of invoice recipient
    String sUBLOrderReferenceID = null;
    {
      final OrderReferenceType aUBLOrderReference = aUBLDoc.getOrderReference ();
      if (aUBLOrderReference != null)
      {
        // Use directly from order reference
        sUBLOrderReferenceID = StringHelper.trim (aUBLOrderReference.getIDValue ());
      }

      if (StringHelper.hasNoText (sUBLOrderReferenceID))
      {
        if (m_aSettings.isOrderReferenceIDMandatory ())
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .errorFieldName ("OrderReference/ID")
                                                   .errorText (EText.ORDER_REFERENCE_MISSING.getDisplayText (m_aDisplayLocale))
                                                   .build ());
      }
      else
      {
        if (m_aSettings.hasOrderReferenceMaxLength ())
        {
          final int nMaxLen = m_aSettings.getOrderReferenceMaxLength ();
          if (sUBLOrderReferenceID.length () > nMaxLen)
          {
            aTransformationErrorList.add (SingleError.builderWarn ()
                                                     .errorFieldName ("OrderReference/ID")
                                                     .errorText (EText.ORDER_REFERENCE_TOO_LONG.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                        sUBLOrderReferenceID,
                                                                                                                        Integer.valueOf (nMaxLen)))
                                                     .build ());
            sUBLOrderReferenceID = sUBLOrderReferenceID.substring (0, nMaxLen);
          }
        }

        final Ebi40OrderReferenceType aEbiOrderReference = new Ebi40OrderReferenceType ();
        aEbiOrderReference.setOrderID (sUBLOrderReferenceID);
        aEbiDoc.getInvoiceRecipient ().setOrderReference (aEbiOrderReference);
      }
    }

    // Tax totals
    // Map from tax category to percentage
    final ICommonsMap <TaxCategoryKey, BigDecimal> aTaxCategoryPercMap = new CommonsHashMap <> ();
    final Ebi40TaxType aEbiTax = new Ebi40TaxType ();
    final Ebi40VATType aEbiVAT = new Ebi40VATType ();
    {
      int nTaxTotalIndex = 0;
      for (final TaxTotalType aUBLTaxTotal : aUBLDoc.getTaxTotal ())
      {
        int nTaxSubtotalIndex = 0;
        for (final TaxSubtotalType aUBLSubtotal : aUBLTaxTotal.getTaxSubtotal ())
        {
          // Tax category is a mandatory element
          final TaxCategoryType aUBLTaxCategory = aUBLSubtotal.getTaxCategory ();

          final String sUBLTaxCategoryID = StringHelper.trim (aUBLTaxCategory.getID ().getValue ());
          final boolean bTaxExemption = isTaxExemptionCategoryID (sUBLTaxCategoryID);
          BigDecimal aUBLTaxAmount = aUBLSubtotal.getTaxAmountValue ();
          BigDecimal aUBLTaxableAmount = aUBLSubtotal.getTaxableAmountValue ();

          // Is the percentage value directly specified
          BigDecimal aUBLPercentage = bTaxExemption ? BigDecimal.ZERO : aUBLTaxCategory.getPercentValue ();
          if (aUBLPercentage == null)
          {
            // no it is not :(
            if (aUBLTaxAmount != null && aUBLTaxableAmount != null)
            {
              // Calculate percentage
              aUBLPercentage = MathHelper.isEQ0 (aUBLTaxableAmount) ? BigDecimal.ZERO
                                                                    : aUBLTaxAmount.multiply (CGlobal.BIGDEC_100)
                                                                                   .divide (aUBLTaxableAmount,
                                                                                            SCALE_PERC,
                                                                                            ROUNDING_MODE);
            }
          }

          if (aUBLPercentage != null)
          {
            // We have at least the percentage
            if (aUBLTaxableAmount == null && aUBLTaxAmount != null)
            {
              // Cannot "back" calculate the taxable amount from 0 percentage!
              if (MathHelper.isNE0 (aUBLPercentage))
              {
                // Calculate (inexact) subtotal
                aUBLTaxableAmount = aUBLTaxAmount.multiply (CGlobal.BIGDEC_100)
                                                 .divide (aUBLPercentage, SCALE_PRICE4, ROUNDING_MODE);
              }
            }
            else
              if (aUBLTaxableAmount != null && aUBLTaxAmount == null)
              {
                // Calculate (inexact) subtotal
                aUBLTaxAmount = MathHelper.isEQ0 (aUBLPercentage) ? BigDecimal.ZERO
                                                                  : aUBLTaxableAmount.multiply (aUBLPercentage)
                                                                                     .divide (CGlobal.BIGDEC_100,
                                                                                              SCALE_PRICE4,
                                                                                              ROUNDING_MODE);
              }
          }

          // Save item and put in map
          final IDType aUBLTaxSchemeID = aUBLTaxCategory.getTaxScheme ().getID ();
          if (aUBLTaxSchemeID == null)
          {
            aTransformationErrorList.add (SingleError.builderError ()
                                                     .errorFieldName ("TaxTotal[" +
                                                                      nTaxTotalIndex +
                                                                      "]/TaxSubtotal[" +
                                                                      nTaxSubtotalIndex +
                                                                      "]/TaxCategory/TaxScheme/ID")
                                                     .errorText (EText.MISSING_TAXCATEGORY_TAXSCHEME_ID.getDisplayText (m_aDisplayLocale))
                                                     .build ());
            break;
          }
          final String sUBLTaxSchemeSchemeID = StringHelper.trim (aUBLTaxSchemeID.getSchemeID ());
          final String sUBLTaxSchemeID = StringHelper.trim (aUBLTaxSchemeID.getValue ());
          if (StringHelper.hasNoText (sUBLTaxSchemeID))
          {
            aTransformationErrorList.add (SingleError.builderError ()
                                                     .errorFieldName ("TaxTotal[" +
                                                                      nTaxTotalIndex +
                                                                      "]/TaxSubtotal[" +
                                                                      nTaxSubtotalIndex +
                                                                      "]/TaxCategory/TaxScheme/ID")
                                                     .errorText (EText.MISSING_TAXCATEGORY_TAXSCHEME_ID_VALUE.getDisplayText (m_aDisplayLocale))
                                                     .build ());
            break;
          }

          if (aUBLTaxCategory.getID () == null)
          {
            aTransformationErrorList.add (SingleError.builderError ()
                                                     .errorFieldName ("TaxTotal[" +
                                                                      nTaxTotalIndex +
                                                                      "]/TaxSubtotal[" +
                                                                      nTaxSubtotalIndex +
                                                                      "]/TaxCategory")
                                                     .errorText (EText.MISSING_TAXCATEGORY_ID.getDisplayText (m_aDisplayLocale))
                                                     .build ());
            break;
          }
          if (StringHelper.hasNoText (sUBLTaxCategoryID))
          {
            aTransformationErrorList.add (SingleError.builderError ()
                                                     .errorFieldName ("TaxTotal[" +
                                                                      nTaxTotalIndex +
                                                                      "]/TaxSubtotal[" +
                                                                      nTaxSubtotalIndex +
                                                                      "]/TaxCategory")
                                                     .errorText (EText.MISSING_TAXCATEGORY_ID_VALUE.getDisplayText (m_aDisplayLocale))
                                                     .build ());
            break;
          }

          final String sUBLTaxCategorySchemeID = StringHelper.trim (aUBLTaxCategory.getID ().getSchemeID ());

          aTaxCategoryPercMap.put (new TaxCategoryKey (new SchemedID (sUBLTaxSchemeSchemeID, sUBLTaxSchemeID),
                                                       new SchemedID (sUBLTaxCategorySchemeID, sUBLTaxCategoryID)),
                                   aUBLPercentage);

          {
            if (isVATSchemeID (sUBLTaxSchemeID))
            {
              if (aUBLPercentage == null)
              {
                aTransformationErrorList.add (SingleError.builderError ()
                                                         .errorFieldName ("TaxTotal[" +
                                                                          nTaxTotalIndex +
                                                                          "]/TaxSubtotal[" +
                                                                          nTaxSubtotalIndex +
                                                                          "]/TaxCategory/Percent")
                                                         .errorText (EText.TAX_PERCENT_MISSING.getDisplayText (m_aDisplayLocale))
                                                         .build ());
              }
              else
                if (aUBLTaxableAmount == null)
                {
                  aTransformationErrorList.add (SingleError.builderError ()
                                                           .errorFieldName ("TaxTotal[" +
                                                                            nTaxTotalIndex +
                                                                            "]/TaxSubtotal[" +
                                                                            nTaxSubtotalIndex +
                                                                            "]/TaxableAmount")
                                                           .errorText (EText.TAXABLE_AMOUNT_MISSING.getDisplayText (m_aDisplayLocale))
                                                           .build ());
                }
                else
                {
                  // add VAT item
                  final Ebi40ItemType aEbiVATItem = new Ebi40ItemType ();
                  // Base amount
                  aEbiVATItem.setTaxedAmount (aUBLTaxableAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));

                  if (bTaxExemption)
                  {
                    String sReason = null;
                    if (aUBLTaxCategory.hasTaxExemptionReasonEntries ())
                      sReason = aUBLTaxCategory.getTaxExemptionReasonAtIndex (0).getValue ();
                    if (sReason == null && aUBLTaxCategory.getTaxExemptionReasonCode () != null)
                      sReason = aUBLTaxCategory.getTaxExemptionReasonCode ().getValue ();
                    if (StringHelper.hasNoText (sReason))
                      sReason = "Tax Exemption";
                    aEbiVAT.setTaxExemption (StringHelper.getConcatenatedOnDemand (aEbiVAT.getTaxExemption (),
                                                                                   '\n',
                                                                                   sReason));
                  }
                  else
                  {
                    // tax rate
                    final Ebi40TaxRateType aEbiVATVATRate = new Ebi40TaxRateType ();
                    // Optional
                    if (false)
                      aEbiVATVATRate.setTaxCode (sUBLTaxCategoryID);
                    aEbiVATVATRate.setValue (aUBLPercentage);
                    aEbiVATItem.setTaxRate (aEbiVATVATRate);
                  }
                  // Tax amount (mandatory)
                  aEbiVATItem.setAmount (aUBLTaxAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));
                  // Add to list
                  aEbiVAT.addItem (aEbiVATItem);
                }
            }
            else
            {
              // Other TAX
              final Ebi40OtherTaxType aOtherTax = new Ebi40OtherTaxType ();
              // As no comment is present, use the scheme ID
              aOtherTax.setComment (sUBLTaxSchemeID);
              // Tax amount (mandatory)
              aOtherTax.setAmount (aUBLTaxAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));
              aEbiTax.addOtherTax (aOtherTax);
            }
          }
          ++nTaxSubtotalIndex;
        }
        ++nTaxTotalIndex;
      }

      if (aEbiVAT.hasItemEntries () && StringHelper.hasText (aEbiVAT.getTaxExemption ()))
      {
        // v4.0 cannot mix Tax item and Tax exemption
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorText (EText.EBI40_CANNOT_MIX_VAT_EXEMPTION.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      aEbiTax.setVAT (aEbiVAT);
      aEbiDoc.setTax (aEbiTax);
    }

    // Line items
    BigDecimal aTotalZeroPercLineExtensionAmount = BigDecimal.ZERO;
    {
      final Ebi40DetailsType aEbiDetails = new Ebi40DetailsType ();
      final Ebi40ItemListType aEbiItemList = new Ebi40ItemListType ();
      int nLineIndex = 0;
      for (final CreditNoteLineType aUBLLine : aUBLDoc.getCreditNoteLine ())
      {
        // Try to resolve tax category
        TaxCategoryType aUBLTaxCategory = CollectionHelper.getAtIndex (aUBLLine.getItem ().getClassifiedTaxCategory (),
                                                                       0);
        if (aUBLTaxCategory == null)
        {
          // No direct tax category -> check if it is somewhere in the tax total
          aUBLTaxCategory = findTaxCategory (aUBLLine.getTaxTotal ());
        }

        // Try to resolve tax percentage
        BigDecimal aUBLPercent = null;
        String sUBLTaxCategoryID = null;
        if (aUBLTaxCategory != null)
        {
          sUBLTaxCategoryID = StringHelper.trim (aUBLTaxCategory.getIDValue ());

          // Specified at tax category?
          if (aUBLTaxCategory.getPercent () != null)
            aUBLPercent = aUBLTaxCategory.getPercentValue ();

          if (aUBLPercent == null &&
              aUBLTaxCategory.getID () != null &&
              aUBLTaxCategory.getTaxScheme () != null &&
              aUBLTaxCategory.getTaxScheme ().getID () != null)
          {
            // Not specified - check from previous map
            final String sUBLTaxSchemeSchemeID = StringHelper.trim (aUBLTaxCategory.getTaxScheme ()
                                                                                   .getID ()
                                                                                   .getSchemeID ());
            final String sUBLTaxSchemeID = StringHelper.trim (aUBLTaxCategory.getTaxScheme ().getIDValue ());

            final String sUBLTaxCategorySchemeID = StringHelper.trim (aUBLTaxCategory.getID ().getSchemeID ());

            // Avoid Exception
            if (StringHelper.hasText (sUBLTaxSchemeID) && StringHelper.hasText (sUBLTaxCategoryID))
            {
              final TaxCategoryKey aKey = new TaxCategoryKey (new SchemedID (sUBLTaxSchemeSchemeID, sUBLTaxSchemeID),
                                                              new SchemedID (sUBLTaxCategorySchemeID,
                                                                             sUBLTaxCategoryID));
              aUBLPercent = aTaxCategoryPercMap.get (aKey);
            }
          }
        }

        final boolean bTaxExemption = isTaxExemptionCategoryID (sUBLTaxCategoryID);
        if (bTaxExemption && aUBLPercent == null)
          aUBLPercent = BigDecimal.ZERO;

        if (aUBLPercent == null)
        {
          aUBLPercent = BigDecimal.ZERO;
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("CreditNoteLine[" +
                                                                    nLineIndex +
                                                                    "]/Item/ClassifiedTaxCategory")
                                                   .errorText (EText.DETAILS_TAX_PERCENTAGE_NOT_FOUND.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                              aUBLPercent))
                                                   .build ());
        }

        // Start creating ebInterface line
        final Ebi40ListLineItemType aEbiListLineItem = new Ebi40ListLineItemType ();

        // CreditNote line number
        final String sUBLPositionNumber = StringHelper.trim (aUBLLine.getIDValue ());
        BigInteger aUBLPositionNumber = StringParser.parseBigInteger (sUBLPositionNumber);
        if (aUBLPositionNumber != null)
        {
          if (MathHelper.isLT1 (aUBLPositionNumber))
            if (m_aSettings.isErrorOnPositionNumber ())
            {
              // Must be &gt; 0
              aTransformationErrorList.add (SingleError.builderError ()
                                                       .errorFieldName ("CreditNoteLine[" + nLineIndex + "]/ID")
                                                       .errorText (EText.DETAILS_INVALID_POSITION.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                          sUBLPositionNumber))
                                                       .build ());
            }
            else
            {
              // Swallow the error
              aUBLPositionNumber = null;
            }
        }
        if (aUBLPositionNumber == null)
        {
          aUBLPositionNumber = BigInteger.valueOf (nLineIndex + 1L);
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("CreditNoteLine[" + nLineIndex + "]/ID")
                                                   .errorText (EText.DETAILS_INVALID_POSITION_SET_TO_INDEX.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                                   sUBLPositionNumber,
                                                                                                                                   aUBLPositionNumber))
                                                   .build ());
        }
        aEbiListLineItem.setPositionNumber (aUBLPositionNumber);

        // Descriptions
        for (final DescriptionType aUBLDescription : aUBLLine.getItem ().getDescription ())
        {
          final String sDesc = StringHelper.trim (aUBLDescription.getValue ());
          if (StringHelper.hasText (sDesc))
            aEbiListLineItem.addDescription (sDesc);
        }
        if (aEbiListLineItem.hasNoDescriptionEntries ())
        {
          // Use item name as description
          final NameType aUBLName = aUBLLine.getItem ().getName ();
          if (aUBLName != null)
          {
            final String sDesc = StringHelper.trim (aUBLName.getValue ());
            if (StringHelper.hasText (sDesc))
              aEbiListLineItem.addDescription (sDesc);
          }
        }
        // Add the Note elements as well (IBM, 2016-11)
        for (final NoteType aUBLNote : aUBLLine.getNote ())
        {
          final String sDesc = StringHelper.trim (aUBLNote.getValue ());
          if (StringHelper.hasText (sDesc))
            aEbiListLineItem.addDescription (sDesc);
        }

        // Quantity
        final Ebi40UnitType aEbiQuantity = new Ebi40UnitType ();
        if (aUBLLine.getCreditedQuantity () != null)
        {
          // Unit code is optional
          if (aUBLLine.getCreditedQuantity ().getUnitCode () != null)
            aEbiQuantity.setUnit (StringHelper.trim (aUBLLine.getCreditedQuantity ().getUnitCode ()));
          aEbiQuantity.setValue (aUBLLine.getCreditedQuantityValue ());
        }
        if (aEbiQuantity.getUnit () == null)
        {
          // ebInterface requires a quantity!
          aEbiQuantity.setUnit (UOM_DEFAULT);
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("CreditNoteLine[" +
                                                                    nLineIndex +
                                                                    "]/CreditNotedQuantity/UnitCode")
                                                   .errorText (EText.DETAILS_INVALID_UNIT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                  aEbiQuantity.getUnit ()))
                                                   .build ());
        }
        if (aEbiQuantity.getValue () == null)
        {
          aEbiQuantity.setValue (BigDecimal.ONE);
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("CreditNoteLine[" +
                                                                    nLineIndex +
                                                                    "]/CreditNotedQuantity")
                                                   .errorText (EText.DETAILS_INVALID_QUANTITY.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                      aEbiQuantity.getValue ()))
                                                   .build ());
        }
        aEbiListLineItem.setQuantity (aEbiQuantity);

        // Unit price
        if (aUBLLine.getPrice () != null)
        {
          // Unit price = priceAmount/baseQuantity (mandatory)
          final BigDecimal aUBLPriceAmount = aUBLLine.getPrice ().getPriceAmountValue ();
          // If no base quantity is present, assume 1 (optional)
          final BigDecimal aUBLBaseQuantity = aUBLLine.getPrice ().getBaseQuantityValue ();
          if (aUBLBaseQuantity != null)
          {
            if (MathHelper.isEQ0 (aUBLBaseQuantity))
              aEbiListLineItem.setUnitPrice (BigDecimal.ZERO);
            else
              aEbiListLineItem.setUnitPrice (aUBLPriceAmount.divide (aUBLBaseQuantity, SCALE_PRICE4, ROUNDING_MODE));
          }
          else
            aEbiListLineItem.setUnitPrice (aUBLPriceAmount);
        }
        else
        {
          // Unit price = lineExtensionAmount / quantity (mandatory)
          final BigDecimal aUBLLineExtensionAmount = aUBLLine.getLineExtensionAmountValue ();
          final BigDecimal aEbiUnitPrice;
          if (MathHelper.isEQ0 (aEbiQuantity.getValue ()))
            aEbiUnitPrice = BigDecimal.ZERO;
          else
            aEbiUnitPrice = aUBLLineExtensionAmount.divide (aEbiQuantity.getValue (), SCALE_PRICE4, ROUNDING_MODE);
          aEbiListLineItem.setUnitPrice (aEbiUnitPrice);
        }

        final BigDecimal aEbiUnitPriceValue = aEbiListLineItem.getUnitPrice ();

        if (bTaxExemption)
        {
          // Tax exemption on line level not supported in ebi 4.0
          final Ebi40TaxRateType aEbiVATRate = new Ebi40TaxRateType ();
          aEbiVATRate.setValue (BigDecimal.ZERO);
          aEbiListLineItem.setTaxRate (aEbiVATRate);
        }
        else
        {
          // Tax rate (mandatory)
          final Ebi40TaxRateType aEbiVATRate = new Ebi40TaxRateType ();
          aEbiVATRate.setValue (aUBLPercent);
          if (aUBLTaxCategory != null)
            // Optional
            if (false)
              aEbiVATRate.setTaxCode (aUBLTaxCategory.getIDValue ());
          aEbiListLineItem.setTaxRate (aEbiVATRate);
        }

        // Line item amount (quantity * unit price +- reduction / surcharge)
        aEbiListLineItem.setLineItemAmount (aUBLLine.getLineExtensionAmountValue ()
                                                    .setScale (SCALE_PRICE2, ROUNDING_MODE));

        // Special handling in case no VAT item is present
        if (MathHelper.isEQ0 (aUBLPercent))
          aTotalZeroPercLineExtensionAmount = aTotalZeroPercLineExtensionAmount.add (aEbiListLineItem.getLineItemAmount ());

        // Order reference per line (UBL 2.1 only)
        for (final OrderLineReferenceType aUBLOrderLineReference : aUBLLine.getOrderLineReference ())
          if (StringHelper.hasText (aUBLOrderLineReference.getLineIDValue ()))
          {
            final Ebi40OrderReferenceDetailType aEbiOrderRefDetail = new Ebi40OrderReferenceDetailType ();

            // order reference
            String sUBLLineOrderReferenceID = null;
            if (aUBLOrderLineReference.getOrderReference () != null)
              sUBLLineOrderReferenceID = StringHelper.trim (aUBLOrderLineReference.getOrderReference ().getIDValue ());
            if (StringHelper.hasNoText (sUBLLineOrderReferenceID))
            {
              // Use the global order reference from header level
              sUBLLineOrderReferenceID = sUBLOrderReferenceID;
            }
            aEbiOrderRefDetail.setOrderID (sUBLLineOrderReferenceID);

            // Order position number
            final String sOrderPosNumber = StringHelper.trim (aUBLOrderLineReference.getLineIDValue ());
            if (sOrderPosNumber != null)
            {
              if (sOrderPosNumber.length () == 0)
              {
                aTransformationErrorList.add (SingleError.builderError ()
                                                         .errorFieldName ("CreditNoteLine[" +
                                                                          nLineIndex +
                                                                          "]/OrderLineReference/LineID")
                                                         .errorText (EText.ORDERLINE_REF_ID_EMPTY.getDisplayText (m_aDisplayLocale))
                                                         .build ());
              }
              else
              {
                aEbiOrderRefDetail.setOrderPositionNumber (sOrderPosNumber);
              }
            }
            if (StringHelper.hasText (aEbiOrderRefDetail.getOrderPositionNumber ()) &&
                StringHelper.hasNoText (sUBLLineOrderReferenceID))
            {
              if (m_aSettings.isOrderReferenceIDMandatory ())
              {
                // The line order reference is mandatory
                aTransformationErrorList.add (SingleError.builderError ()
                                                         .errorFieldName ("CreditNoteLine[" +
                                                                          nLineIndex +
                                                                          "]/OrderLineReference/OrderReference/ID")
                                                         .errorText (EText.ORDER_REFERENCE_MISSING.getDisplayText (m_aDisplayLocale))
                                                         .build ());
              }
              else
              {
                aEbiOrderRefDetail.setOrderPositionNumber (null);
                aTransformationErrorList.add (SingleError.builderWarn ()
                                                         .errorFieldName ("CreditNoteLine[" +
                                                                          nLineIndex +
                                                                          "]/OrderLineReference/OrderReference/ID")
                                                         .errorText (EText.ORDER_REFERENCE_MISSING_IGNORE_ORDER_POS.getDisplayText (m_aDisplayLocale))
                                                         .build ());
              }
            }

            if (StringHelper.hasText (sUBLLineOrderReferenceID))
              aEbiListLineItem.setInvoiceRecipientsOrderReference (aEbiOrderRefDetail);
            break;
          }

        // Reduction and surcharge (UBL 2.1 only)
        if (aUBLLine.hasAllowanceChargeEntries ())
        {
          // Start with quantity*unitPrice for base amount
          BigDecimal aEbiBaseAmount = aEbiListLineItem.getQuantity ().getValue ().multiply (aEbiUnitPriceValue);
          final Ebi40ReductionAndSurchargeListLineItemDetailsType aEbiRSDetails = new Ebi40ReductionAndSurchargeListLineItemDetailsType ();

          // ebInterface can handle only Reduction or only Surcharge
          ETriState eSurcharge = ETriState.UNDEFINED;
          for (final AllowanceChargeType aUBLAllowanceCharge : aUBLLine.getAllowanceCharge ())
          {
            final boolean bItemIsSurcharge = aUBLAllowanceCharge.getChargeIndicator ().isValue ();

            // Remember for next item
            if (eSurcharge.isUndefined ())
              eSurcharge = ETriState.valueOf (bItemIsSurcharge);
            final boolean bSwapSigns = bItemIsSurcharge != eSurcharge.isTrue ();

            final Ebi40ReductionAndSurchargeBaseType aEbiRSItem = new Ebi40ReductionAndSurchargeBaseType ();
            // Amount is mandatory
            final BigDecimal aAmount = aUBLAllowanceCharge.getAmountValue ();
            aEbiRSItem.setAmount (bSwapSigns ? aAmount.negate () : aAmount);

            // Base amount is optional
            if (aUBLAllowanceCharge.getBaseAmount () != null)
              aEbiRSItem.setBaseAmount (aUBLAllowanceCharge.getBaseAmountValue ());
            if (aEbiRSItem.getBaseAmount () == null)
              aEbiRSItem.setBaseAmount (aEbiBaseAmount);

            if (aUBLAllowanceCharge.getMultiplierFactorNumeric () != null)
            {
              // Percentage is optional
              final BigDecimal aPerc = aUBLAllowanceCharge.getMultiplierFactorNumericValue ()
                                                          .multiply (CGlobal.BIGDEC_100);
              aEbiRSItem.setPercentage (aPerc);
            }

            if (eSurcharge.isTrue ())
            {
              aEbiRSDetails.addSurchargeListLineItem (aEbiRSItem);
              aEbiBaseAmount = aEbiBaseAmount.add (aEbiRSItem.getAmount ());
            }
            else
            {
              aEbiRSDetails.addReductionListLineItem (aEbiRSItem);
              aEbiBaseAmount = aEbiBaseAmount.subtract (aEbiRSItem.getAmount ());
            }
          }
          aEbiListLineItem.setReductionAndSurchargeListLineItemDetails (aEbiRSDetails);
        }

        // Delivery per line item
        if (aUBLLine.hasDeliveryEntries ())
        {
          // Delivery address
          final int nDeliveryIndex = 0;
          final DeliveryType aUBLDelivery = aUBLLine.getDeliveryAtIndex (0);

          if (aUBLDelivery.getActualDeliveryDate () != null)
          {
            final Ebi40DeliveryType aEbiDelivery = convertDelivery (aUBLDelivery,
                                                                    "CreditNoteLine[" +
                                                                                  nLineIndex +
                                                                                  "]/Delivery[" +
                                                                                  nDeliveryIndex +
                                                                                  "]",
                                                                    aUBLDoc.getAccountingCustomerParty (),
                                                                    aTransformationErrorList,
                                                                    m_aContentLocale,
                                                                    m_aDisplayLocale);
            aEbiListLineItem.setDelivery (aEbiDelivery);
          }
        }

        // Perform customizing as last action
        if (m_aCustomizer != null)
          m_aCustomizer.additionalItemMapping (aUBLLine, aEbiListLineItem);

        // Add the item to the list
        aEbiItemList.addListLineItem (aEbiListLineItem);
        nLineIndex++;
      }
      aEbiDetails.addItemList (aEbiItemList);
      aEbiDoc.setDetails (aEbiDetails);
    }

    if (aEbiVAT.hasNoItemEntries ())
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("CreditNote")
                                               .errorText (EText.VAT_ITEM_MISSING.getDisplayText (m_aDisplayLocale))
                                               .build ());
    }

    // Global reduction and surcharge
    if (aUBLDoc.hasAllowanceChargeEntries ())
    {
      // Start with quantity*unitPrice for base amount
      BigDecimal aEbiBaseAmount = aUBLDoc.getLegalMonetaryTotal ().getLineExtensionAmountValue ();
      if (aEbiBaseAmount == null)
      {
        // No global LineExtensionAmount is present - sum all rows
        BigDecimal tmp = BigDecimal.ZERO;
        for (final Ebi40ItemListType aEbiItemList : aEbiDoc.getDetails ().getItemList ())
          for (final Ebi40ListLineItemType aEbiListLineItem : aEbiItemList.getListLineItem ())
            tmp = tmp.add (aEbiListLineItem.getLineItemAmount ());
        aEbiBaseAmount = tmp;
      }

      final Ebi40ReductionAndSurchargeDetailsType aEbiRS = new Ebi40ReductionAndSurchargeDetailsType ();

      int nAllowanceChargeIndex = 0;
      for (final AllowanceChargeType aUBLAllowanceCharge : aUBLDoc.getAllowanceCharge ())
      {
        final boolean bItemIsSurcharge = aUBLAllowanceCharge.getChargeIndicator ().isValue ();

        final Ebi40ReductionAndSurchargeType aEbiRSItem = new Ebi40ReductionAndSurchargeType ();
        // Amount is mandatory
        final BigDecimal aAmount = aUBLAllowanceCharge.getAmountValue ();
        aEbiRSItem.setAmount (aAmount);

        // Base amount is optional
        if (aUBLAllowanceCharge.getBaseAmount () != null)
          aEbiRSItem.setBaseAmount (aUBLAllowanceCharge.getBaseAmountValue ());
        if (aEbiRSItem.getBaseAmount () == null)
          aEbiRSItem.setBaseAmount (aEbiBaseAmount);

        if (aUBLAllowanceCharge.getMultiplierFactorNumeric () != null)
        {
          // Percentage is optional
          final BigDecimal aPerc = aUBLAllowanceCharge.getMultiplierFactorNumericValue ().multiply (CGlobal.BIGDEC_100);
          aEbiRSItem.setPercentage (aPerc);
        }

        Ebi40TaxRateType aEbiVATRate = null;
        for (final TaxCategoryType aUBLTaxCategory : aUBLAllowanceCharge.getTaxCategory ())
          if (aUBLTaxCategory.getPercent () != null)
          {
            aEbiVATRate = new Ebi40TaxRateType ();
            aEbiVATRate.setValue (aUBLTaxCategory.getPercentValue ());
            if (false)
              aEbiVATRate.setTaxCode (aUBLTaxCategory.getIDValue ());
            break;
          }
        if (aEbiVATRate == null)
        {
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .errorFieldName ("CreditNote/AllowanceCharge[" +
                                                                    nAllowanceChargeIndex +
                                                                    "]")
                                                   .errorText (EText.ALLOWANCE_CHARGE_NO_TAXRATE.getDisplayText (m_aDisplayLocale))
                                                   .build ());
          // No default in this case
        }
        aEbiRSItem.setTaxRate (aEbiVATRate);

        if (bItemIsSurcharge)
        {
          aEbiRS.addReductionOrSurcharge (new ObjectFactory ().createSurcharge (aEbiRSItem));
          aEbiBaseAmount = aEbiBaseAmount.add (aEbiRSItem.getAmount ());
        }
        else
        {
          aEbiRS.addReductionOrSurcharge (new ObjectFactory ().createReduction (aEbiRSItem));
          aEbiBaseAmount = aEbiBaseAmount.subtract (aEbiRSItem.getAmount ());
        }
        aEbiDoc.setReductionAndSurchargeDetails (aEbiRS);
        ++nAllowanceChargeIndex;
      }
    }

    // PrepaidAmount is not supported!
    final MonetaryTotalType aUBLMonetaryTotal = aUBLDoc.getLegalMonetaryTotal ();
    if (aUBLMonetaryTotal.getPrepaidAmount () != null && !MathHelper.isEQ0 (aUBLMonetaryTotal.getPrepaidAmountValue ()))
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("CreditNote/LegalMonetaryTotal/PrepaidAmount")
                                               .errorText (EText.PREPAID_NOT_SUPPORTED.getDisplayText (m_aDisplayLocale))
                                               .build ());
    }

    // Total gross amount
    if (aUBLMonetaryTotal.getTaxInclusiveAmountValue () != null)
      aEbiDoc.setTotalGrossAmount (aUBLMonetaryTotal.getTaxInclusiveAmountValue ()
                                                    .setScale (SCALE_PRICE2, ROUNDING_MODE));
    else
      aEbiDoc.setTotalGrossAmount (aUBLMonetaryTotal.getPayableAmountValue ().setScale (SCALE_PRICE2, ROUNDING_MODE));

    // Payment method
    convertPayment ( () -> null,
                     aUBLDoc::getPaymentMeans,
                     aUBLDoc::getPayeeParty,
                     aUBLDoc::getAccountingSupplierParty,
                     aUBLDoc::getPaymentTerms,
                     aUBLDoc::getLegalMonetaryTotal,
                     aTransformationErrorList,
                     aEbiDoc,
                     true);

    // Delivery
    Ebi40DeliveryType aEbiDelivery = null;
    {
      // Delivery address (since UBL 2.1)
      int nDeliveryIndex = 0;
      for (final DeliveryType aUBLDelivery : aUBLDoc.getDelivery ())
      {
        // Use the first delivery with a delivery date
        if (aUBLDelivery.getActualDeliveryDate () != null)
        {
          aEbiDelivery = convertDelivery (aUBLDelivery,
                                          "/Delivery[" + nDeliveryIndex + "]",
                                          aUBLDoc.getAccountingCustomerParty (),
                                          aTransformationErrorList,
                                          m_aContentLocale,
                                          m_aDisplayLocale);
          break;
        }
        ++nDeliveryIndex;
      }

      if (aEbiDelivery == null)
        aEbiDelivery = new Ebi40DeliveryType ();

      // No delivery date is present - check for service period
      final PeriodType aUBLCreditNotePeriod = CollectionHelper.getAtIndex (aUBLDoc.getInvoicePeriod (), 0);
      if (aUBLCreditNotePeriod != null)
      {
        final XMLOffsetDate aStartDate = aUBLCreditNotePeriod.getStartDateValue ();
        final XMLOffsetDate aEndDate = aUBLCreditNotePeriod.getEndDateValue ();
        if (aStartDate != null)
        {
          if (aEndDate == null)
          {
            // It's just a date - prefer the delivery date over the
            // InvoicePeriod/StartDate
            if (aEbiDelivery.getDate () == null)
              aEbiDelivery.setDate (aStartDate);
          }
          else
          {
            // It's a period!
            final Ebi40PeriodType aEbiPeriod = new Ebi40PeriodType ();
            aEbiPeriod.setFromDate (aStartDate);
            aEbiPeriod.setToDate (aEndDate);
            aEbiDelivery.setPeriod (aEbiPeriod);
            // Has precedence over date!
            aEbiDelivery.setDate ((XMLOffsetDate) null);
          }
        }
      }
    }

    if (m_aSettings.isDeliveryDateMandatory ())
    {
      if (aEbiDelivery.getDate () == null && aEbiDelivery.getPeriod () == null)
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("CreditNote")
                                                 .errorText (EText.ERB_NO_DELIVERY_DATE.getDisplayText (m_aDisplayLocale))
                                                 .build ());
    }

    if (aEbiDelivery.getDate () != null || aEbiDelivery.getPeriod () != null)
      aEbiDoc.setDelivery (aEbiDelivery);

    // Perform customizing as last action
    if (m_aCustomizer != null)
      m_aCustomizer.additionalGlobalMapping (aUBLDoc, aEbiDoc);

    return aEbiDoc;
  }
}
