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
package at.austriapro.ebinterface.ubl.from.invoice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.CGlobal;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.numeric.BigHelper;
import com.helger.base.state.ETriState;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.base.string.StringParser;
import com.helger.collection.CollectionHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsHashMap;
import com.helger.collection.commons.ICommonsList;
import com.helger.collection.commons.ICommonsMap;
import com.helger.datetime.xml.XMLOffsetDate;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.ebinterface.codelist.EFurtherIdentification;
import com.helger.ebinterface.codelist.ETaxCode;
import com.helger.ebinterface.v43.*;

import at.austriapro.ebinterface.ubl.from.AbstractToEbInterface43Converter;
import at.austriapro.ebinterface.ubl.from.IToEbinterfaceSettings;
import at.austriapro.ebinterface.ubl.from.helper.SchemedID;
import at.austriapro.ebinterface.ubl.from.helper.TaxCategoryKey;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderLineReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyTaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
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
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Main converter between UBL 2.1 invoice and ebInterface 4.3 invoice.
 *
 * @author Philip Helger
 */
@Immutable
public final class InvoiceToEbInterface43Converter extends AbstractToEbInterface43Converter
{
  private ICustomInvoiceToEbInterface43Converter m_aCustomizer;

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be <code>null</code>.
   * @param aSettings
   *        Conversion settings to be used. May not be <code>null</code>.
   */
  public InvoiceToEbInterface43Converter (@Nonnull final Locale aDisplayLocale,
                                          @Nonnull final Locale aContentLocale,
                                          @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale, aSettings);
  }

  @Nonnull
  public InvoiceToEbInterface43Converter setCustomizer (@Nullable final ICustomInvoiceToEbInterface43Converter aCustomizer)
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
   * @return The created ebInterface document or <code>null</code> in case of a severe error.
   */
  @Nullable
  public Ebi43InvoiceType convertToEbInterface (@Nonnull final InvoiceType aUBLDoc,
                                                @Nonnull final ErrorList aTransformationErrorList)
  {
    ValueEnforcer.notNull (aUBLDoc, "UBLInvoice");
    ValueEnforcer.notNull (aTransformationErrorList, "TransformationErrorList");
    ValueEnforcer.isTrue (aTransformationErrorList.isEmpty (), "TransformationErrorList must be empty!");

    // Consistency check before starting the conversion
    checkInvoiceConsistency (aUBLDoc, aTransformationErrorList);
    if (aTransformationErrorList.containsAtLeastOneError ())
      return null;

    // Build ebInterface invoice
    final Ebi43InvoiceType aEbiDoc = new Ebi43InvoiceType ();
    aEbiDoc.setGeneratingSystem (EBI_GENERATING_SYSTEM_43);
    aEbiDoc.setDocumentType (getAsDocumentTypeType (aUBLDoc.getInvoiceTypeCode () == null ? null : aUBLDoc
                                                                                                          .getInvoiceTypeCode ()
                                                                                                          .getName (),
                                                    aUBLDoc.getInvoiceTypeCodeValue (),
                                                    Ebi43DocumentTypeType.INVOICE.value ()));

    // Cannot set the language, because the 3letter code is expected but we only
    // have the 2letter code!

    final String sUBLCurrencyCode = StringHelper.trim (aUBLDoc.getDocumentCurrencyCodeValue ());
    aEbiDoc.setInvoiceCurrency (sUBLCurrencyCode);

    // Invoice Number
    final String sInvoiceNumber = StringHelper.trim (aUBLDoc.getIDValue ());
    if (StringHelper.isEmpty (sInvoiceNumber))
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("ID")
                                               .errorText (EText.MISSING_INVOICE_NUMBER.getDisplayText (m_aDisplayLocale))
                                               .build ());
    aEbiDoc.setInvoiceNumber (sInvoiceNumber);

    // Ignore the time!
    aEbiDoc.setInvoiceDate (aUBLDoc.getIssueDateValue ());
    if (aEbiDoc.getInvoiceDate () == null)
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("IssueDate")
                                               .errorText (EText.MISSING_INVOICE_DATE.getDisplayText (m_aDisplayLocale))
                                               .build ());

    // Is duplicate/copy indicator?
    if (aUBLDoc.getCopyIndicator () != null)
      aEbiDoc.setIsDuplicate (Boolean.valueOf (aUBLDoc.getCopyIndicator ().isValue ()));

    // CancelledOriginalDocument
    convertRelatedDocuments (aUBLDoc.getBillingReference (), aEbiDoc);
    convertReferencedDocuments (aUBLDoc.getDespatchDocumentReference (), aEbiDoc);
    convertReferencedDocuments (aUBLDoc.getReceiptDocumentReference (), aEbiDoc);
    convertReferencedDocuments (aUBLDoc.getContractDocumentReference (), aEbiDoc);
    convertReferencedDocuments (aUBLDoc.getAdditionalDocumentReference (), aEbiDoc);
    convertReferencedDocuments (aUBLDoc.getStatementDocumentReference (), aEbiDoc);
    convertReferencedDocuments (aUBLDoc.getOriginatorDocumentReference (), aEbiDoc);

    // Global comment
    {
      final ICommonsList <String> aEbiComment = new CommonsArrayList <> ();
      for (final NoteType aNote : aUBLDoc.getNote ())
        if (StringHelper.isNotEmpty (aNote.getValue ()))
          aEbiComment.add (aNote.getValue ());
      if (!aEbiComment.isEmpty ())
        aEbiDoc.setComment (StringImplode.getImplodedNonEmpty ('\n', aEbiComment));
    }

    // Biller/Supplier (creator of the invoice)
    {
      final SupplierPartyType aUBLSupplier = aUBLDoc.getAccountingSupplierParty ();
      final PartyType aUBLParty = aUBLSupplier.getParty ();

      final Ebi43BillerType aEbiBiller = new Ebi43BillerType ();
      // Find the tax scheme that uses VAT
      if (aUBLParty != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLParty.getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (isVATSchemeID (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiBiller.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.isEmpty (aEbiBiller.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aEbiBiller.setVATIdentificationNumber (AT_UNDEFINED_VATIN);
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .errorFieldName ("AccountingSupplierParty/Party/PartyTaxScheme")
                                                 .errorText (EText.BILLER_VAT_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      // This was the BIS2 Mapping
      if (false)
        if (aUBLSupplier.getCustomerAssignedAccountID () != null)
        {
          // The customer's internal identifier for the supplier.
          aEbiBiller.setInvoiceRecipientsBillerID (StringHelper.trim (aUBLSupplier.getCustomerAssignedAccountIDValue ()));
        }

      if (StringHelper.isEmpty (aEbiBiller.getInvoiceRecipientsBillerID ()) &&
          aUBLParty != null &&
          aUBLParty.hasPartyIdentificationEntries ())
      {
        // New version for BIS V2
        aEbiBiller.setInvoiceRecipientsBillerID (StringHelper.trim (aUBLParty.getPartyIdentificationAtIndex (0)
                                                                             .getIDValue ()));
      }

      // Disabled because field is optional
      if (false)
        if (StringHelper.isEmpty (aEbiBiller.getInvoiceRecipientsBillerID ()))
        {
          // Mandatory field
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .errorFieldName ("AccountingSupplierParty/CustomerAssignedAccountID")
                                                   .errorText (EText.ERB_CUSTOMER_ASSIGNED_ACCOUNTID_MISSING.getDisplayText (m_aDisplayLocale))
                                                   .build ());
        }

      for (final AdditionalAccountIDType aUBLAddAccountID : aUBLSupplier.getAdditionalAccountID ())
      {
        aEbiBiller.addFurtherIdentification (createFurtherIdentification (EFurtherIdentification.CONSOLIDATOR.getID (),
                                                                          aUBLAddAccountID.getValue ()));
      }

      if (aUBLParty != null)
      {
        aEbiBiller.setAddress (convertParty (aUBLParty,
                                             "AccountingSupplierParty",
                                             aTransformationErrorList,
                                             m_aContentLocale,
                                             m_aDisplayLocale,
                                             true));

        // Ensure a fake biller email address is present
        if (StringHelper.isEmpty (aEbiBiller.getAddress ().getEmail ()))
          if (m_aSettings.isEnforceSupplierEmailAddress ())
            aEbiBiller.getAddress ().setEmail (m_aSettings.getEnforcedSupplierEmailAddress ());

        // Add all further identifications
        convertFurtherIdentifications (aUBLParty.getPartyIdentification (), aEbiBiller::addFurtherIdentification);
      }

      aEbiDoc.setBiller (aEbiBiller);
    }

    // Invoice recipient
    {
      final CustomerPartyType aUBLCustomer = aUBLDoc.getAccountingCustomerParty ();
      final PartyType aUBLParty = aUBLCustomer.getParty ();

      final Ebi43InvoiceRecipientType aEbiRecipient = new Ebi43InvoiceRecipientType ();
      // Find the tax scheme that uses VAT
      if (aUBLParty != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLParty.getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (isVATSchemeID (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiRecipient.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.isEmpty (aEbiRecipient.getVATIdentificationNumber ()))
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
        aEbiRecipient.setBillersInvoiceRecipientID (StringHelper.trim (aUBLCustomer.getSupplierAssignedAccountIDValue ()));
      }
      // BillersInvoiceRecipientID is no longer mandatory in ebi

      for (final AdditionalAccountIDType aUBLAddAccountID : aUBLCustomer.getAdditionalAccountID ())
      {
        aEbiRecipient.addFurtherIdentification (createFurtherIdentification (EFurtherIdentification.CONSOLIDATOR.getID (),
                                                                             aUBLAddAccountID.getValue ()));
      }

      if (aUBLParty != null)
      {
        aEbiRecipient.setAddress (convertParty (aUBLParty,
                                                "AccountingCustomerParty",
                                                aTransformationErrorList,
                                                m_aContentLocale,
                                                m_aDisplayLocale,
                                                true));

        // Add all further identifications
        convertFurtherIdentifications (aUBLParty.getPartyIdentification (), aEbiRecipient::addFurtherIdentification);
      }
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
      final PartyType aUBLParty = aUBLBuyer.getParty ();

      final Ebi43OrderingPartyType aEbiOrderingParty = new Ebi43OrderingPartyType ();
      // Find the tax scheme that uses VAT
      if (aUBLParty != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLParty.getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (isVATSchemeID (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiOrderingParty.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.isEmpty (aEbiOrderingParty.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .errorFieldName ("BuyerCustomerParty/PartyTaxScheme")
                                                 .errorText (EText.ORDERING_PARTY_VAT_MISSING.getDisplayText (m_aDisplayLocale))
                                                 .build ());
      }

      if (aUBLParty != null)
      {
        aEbiOrderingParty.setAddress (convertParty (aUBLParty,
                                                    "BuyerCustomerParty",
                                                    aTransformationErrorList,
                                                    m_aContentLocale,
                                                    m_aDisplayLocale,
                                                    true));

        // Add all further identifications
        convertFurtherIdentifications (aUBLParty.getPartyIdentification (),
                                       aEbiOrderingParty::addFurtherIdentification);
      }
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

      if (false)
        if (StringHelper.isEmpty (aEbiOrderingParty.getBillersOrderingPartyID ()) &&
            aUBLParty != null &&
            aUBLParty.hasPartyIdentificationEntries ())
        {
          // New version for BIS V2
          aEbiOrderingParty.setBillersOrderingPartyID (StringHelper.trim (aUBLParty.getPartyIdentificationAtIndex (0)
                                                                                   .getIDValue ()));
        }

      if (StringHelper.isEmpty (aEbiOrderingParty.getBillersOrderingPartyID ()) &&
          aEbiDoc.getInvoiceRecipient () != null)
      {
        // Use the same as the the invoice recipient ID
        // Heuristics, but what should I do :(
        aEbiOrderingParty.setBillersOrderingPartyID (aEbiDoc.getInvoiceRecipient ().getBillersInvoiceRecipientID ());
      }
      if (StringHelper.isEmpty (aEbiOrderingParty.getBillersOrderingPartyID ()))
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

      if (StringHelper.isEmpty (sUBLOrderReferenceID))
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

        final Ebi43OrderReferenceType aEbiOrderReference = new Ebi43OrderReferenceType ();
        aEbiOrderReference.setOrderID (sUBLOrderReferenceID);
        aEbiDoc.getInvoiceRecipient ().setOrderReference (aEbiOrderReference);
      }
    }

    // Tax totals
    // Map from tax category to percentage
    final ICommonsMap <TaxCategoryKey, BigDecimal> aTaxCategoryPercMap = new CommonsHashMap <> ();
    final Ebi43TaxType aEbiTax = new Ebi43TaxType ();
    final Ebi43VATType aEbiVAT = new Ebi43VATType ();
    {
      int nTaxTotalIndex = 0;
      for (final TaxTotalType aUBLTaxTotal : aUBLDoc.getTaxTotal ())
      {
        int nTaxSubtotalIndex = 0;
        for (final TaxSubtotalType aUBLSubtotal : aUBLTaxTotal.getTaxSubtotal ())
        {
          // Tax category is a mandatory element
          final TaxCategoryType aUBLTaxCategory = aUBLSubtotal.getTaxCategory ();

          final String sUBLTaxCategoryID = StringHelper.trim (aUBLTaxCategory.getIDValue ());
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
              aUBLPercentage = BigHelper.isEQ0 (aUBLTaxableAmount) ? BigDecimal.ZERO : aUBLTaxAmount.multiply (
                                                                                                               CGlobal.BIGDEC_100)
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
              if (BigHelper.isNE0 (aUBLPercentage))
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
                aUBLTaxAmount = BigHelper.isEQ0 (aUBLPercentage) ? BigDecimal.ZERO : aUBLTaxableAmount.multiply (
                                                                                                                 aUBLPercentage)
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
          if (StringHelper.isEmpty (sUBLTaxSchemeID))
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
          if (StringHelper.isEmpty (sUBLTaxCategoryID))
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
            // Is it VAT?
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
                  final Ebi43VATItemType aEbiVATItem = new Ebi43VATItemType ();
                  // Base amount
                  aEbiVATItem.setTaxedAmount (aUBLTaxableAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));
                  if (bTaxExemption)
                  {
                    String sReason = null;
                    if (aUBLTaxCategory.hasTaxExemptionReasonEntries ())
                      sReason = aUBLTaxCategory.getTaxExemptionReasonAtIndex (0).getValue ();
                    if (sReason == null && aUBLTaxCategory.getTaxExemptionReasonCode () != null)
                      sReason = aUBLTaxCategory.getTaxExemptionReasonCode ().getValue ();
                    if (StringHelper.isEmpty (sReason))
                      sReason = "Tax Exemption";
                    final Ebi43TaxExemptionType aEbiTaxEx = new Ebi43TaxExemptionType ();
                    aEbiTaxEx.setValue (sReason);
                    aEbiVATItem.setTaxExemption (aEbiTaxEx);
                  }
                  else
                  {
                    // tax rate
                    final Ebi43VATRateType aEbiVATVATRate = new Ebi43VATRateType ();
                    // Optional
                    if (false)
                      aEbiVATVATRate.setTaxCode (sUBLTaxCategoryID);
                    aEbiVATVATRate.setValue (aUBLPercentage);
                    aEbiVATItem.setVATRate (aEbiVATVATRate);
                  }
                  // Tax amount (mandatory)
                  aEbiVATItem.setAmount (aUBLTaxAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));
                  // Add to list
                  aEbiVAT.addVATItem (aEbiVATItem);
                }
            }
            else
            {
              // Other TAX
              final Ebi43OtherTaxType aOtherTax = new Ebi43OtherTaxType ();
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

      aEbiTax.setVAT (aEbiVAT);
      aEbiDoc.setTax (aEbiTax);
    }

    // Line items
    BigDecimal aTotalZeroPercLineExtensionAmount = BigDecimal.ZERO;
    {
      final Ebi43DetailsType aEbiDetails = new Ebi43DetailsType ();
      final Ebi43ItemListType aEbiItemList = new Ebi43ItemListType ();
      int nLineIndex = 0;
      for (final InvoiceLineType aUBLLine : aUBLDoc.getInvoiceLine ())
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
            if (StringHelper.isNotEmpty (sUBLTaxSchemeID) && StringHelper.isNotEmpty (sUBLTaxCategoryID))
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
                                                   .errorFieldName ("InvoiceLine[" +
                                                                    nLineIndex +
                                                                    "]/Item/ClassifiedTaxCategory")
                                                   .errorText (EText.DETAILS_TAX_PERCENTAGE_NOT_FOUND.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                              aUBLPercent))
                                                   .build ());
        }

        // Start creating ebInterface line
        final Ebi43ListLineItemType aEbiListLineItem = new Ebi43ListLineItemType ();

        // Invoice line number
        final String sUBLPositionNumber = StringHelper.trim (aUBLLine.getIDValue ());
        BigInteger aUBLPositionNumber = StringParser.parseBigInteger (sUBLPositionNumber);
        if (aUBLPositionNumber != null)
        {
          if (BigHelper.isLT1 (aUBLPositionNumber))
            if (m_aSettings.isErrorOnPositionNumber ())
            {
              // Must be &gt; 0
              aTransformationErrorList.add (SingleError.builderError ()
                                                       .errorFieldName ("InvoiceLine[" + nLineIndex + "]/ID")
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
                                                   .errorFieldName ("InvoiceLine[" + nLineIndex + "]/ID")
                                                   .errorText (EText.DETAILS_INVALID_POSITION_SET_TO_INDEX.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                                   sUBLPositionNumber,
                                                                                                                                   aUBLPositionNumber))
                                                   .build ());
        }
        aEbiListLineItem.setPositionNumber (aUBLPositionNumber);

        // Use item name as description
        final NameType aUBLName = aUBLLine.getItem ().getName ();
        if (aUBLName != null)
        {
          final String sDesc = StringHelper.trim (aUBLName.getValue ());
          if (StringHelper.isNotEmpty (sDesc))
            aEbiListLineItem.addDescription (sDesc);
        }
        // Descriptions
        for (final DescriptionType aUBLDescription : aUBLLine.getItem ().getDescription ())
        {
          final String sDesc = StringHelper.trim (aUBLDescription.getValue ());
          if (StringHelper.isNotEmpty (sDesc))
            aEbiListLineItem.addDescription (sDesc);
        }
        // Add the Note elements as well (IBM, 2016-11)
        for (final NoteType aUBLNote : aUBLLine.getNote ())
        {
          final String sDesc = StringHelper.trim (aUBLNote.getValue ());
          if (StringHelper.isNotEmpty (sDesc))
            aEbiListLineItem.addDescription (sDesc);
        }

        // Quantity
        final Ebi43UnitType aEbiQuantity = new Ebi43UnitType ();
        if (aUBLLine.getInvoicedQuantity () != null)
        {
          // Unit code is optional
          if (aUBLLine.getInvoicedQuantity ().getUnitCode () != null)
            aEbiQuantity.setUnit (StringHelper.trim (aUBLLine.getInvoicedQuantity ().getUnitCode ()));
          aEbiQuantity.setValue (aUBLLine.getInvoicedQuantityValue ());
        }
        if (aEbiQuantity.getUnit () == null)
        {
          // ebInterface requires a quantity!
          aEbiQuantity.setUnit (UOM_DEFAULT);
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("InvoiceLine[" +
                                                                    nLineIndex +
                                                                    "]/InvoicedQuantity/UnitCode")
                                                   .errorText (EText.DETAILS_INVALID_UNIT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                  aEbiQuantity.getUnit ()))
                                                   .build ());
        }
        if (aEbiQuantity.getValue () == null)
        {
          aEbiQuantity.setValue (BigDecimal.ONE);
          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .errorFieldName ("InvoiceLine[" + nLineIndex + "]/InvoicedQuantity")
                                                   .errorText (EText.DETAILS_INVALID_QUANTITY.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                      aEbiQuantity.getValue ()))
                                                   .build ());
        }
        aEbiListLineItem.setQuantity (aEbiQuantity);

        // Unit price
        if (aUBLLine.getPrice () != null)
        {
          final Ebi43UnitPriceType aEbiUnitPrice = new Ebi43UnitPriceType ();
          // Unit price = priceAmount/baseQuantity (mandatory)
          final BigDecimal aUBLPriceAmount = aUBLLine.getPrice ().getPriceAmountValue ();
          aEbiUnitPrice.setValue (aUBLPriceAmount);
          // If no base quantity is present, assume 1 (optional)
          final BigDecimal aUBLBaseQuantity = aUBLLine.getPrice ().getBaseQuantityValue ();
          if (aUBLBaseQuantity != null)
          {
            aEbiUnitPrice.setBaseQuantity (aUBLBaseQuantity);
            if (BigHelper.isEQ0 (aUBLBaseQuantity))
              aEbiUnitPrice.setValue (BigDecimal.ZERO);
          }
          aEbiListLineItem.setUnitPrice (aEbiUnitPrice);
        }
        else
        {
          // Unit price = lineExtensionAmount / quantity (mandatory)
          final BigDecimal aUBLLineExtensionAmount = aUBLLine.getLineExtensionAmountValue ();
          final Ebi43UnitPriceType aEbiUnitPrice = new Ebi43UnitPriceType ();
          if (BigHelper.isEQ0 (aEbiQuantity.getValue ()) || aUBLLineExtensionAmount == null)
            aEbiUnitPrice.setValue (BigDecimal.ZERO);
          else
            aEbiUnitPrice.setValue (aUBLLineExtensionAmount.divide (aEbiQuantity.getValue (),
                                                                    SCALE_PRICE4,
                                                                    ROUNDING_MODE));
          aEbiListLineItem.setUnitPrice (aEbiUnitPrice);
        }

        BigDecimal aEbiUnitPriceValue = aEbiListLineItem.getUnitPrice ().getValue ();
        final BigDecimal aBQ = aEbiListLineItem.getUnitPrice ().getBaseQuantity ();
        if (aBQ != null && BigHelper.isNE0 (aBQ))
          aEbiUnitPriceValue = aEbiUnitPriceValue.divide (aBQ, SCALE_PRICE4, ROUNDING_MODE);

        if (bTaxExemption)
        {
          // Tax exemption
          String sReason = null;
          if (aUBLTaxCategory.hasTaxExemptionReasonEntries ())
            sReason = aUBLTaxCategory.getTaxExemptionReasonAtIndex (0).getValue ();
          if (sReason == null && aUBLTaxCategory.getTaxExemptionReasonCode () != null)
            sReason = aUBLTaxCategory.getTaxExemptionReasonCode ().getValue ();
          if (StringHelper.isEmpty (sReason))
            sReason = "Tax Exemption";
          final Ebi43TaxExemptionType aEbiTaxEx = new Ebi43TaxExemptionType ();
          aEbiTaxEx.setValue (sReason);
          aEbiListLineItem.setTaxExemption (aEbiTaxEx);
        }
        else
        {
          // VAT rate (mandatory)
          final Ebi43VATRateType aEbiVATRate = new Ebi43VATRateType ();
          aEbiVATRate.setValue (aUBLPercent);
          if (aUBLTaxCategory != null)
          {
            // Optional
            if (false)
              aEbiVATRate.setTaxCode (aUBLTaxCategory.getIDValue ());
          }
          aEbiListLineItem.setVATRate (aEbiVATRate);
        }

        // Line item amount (quantity * unit price +- reduction / surcharge)
        aEbiListLineItem.setLineItemAmount (aUBLLine.getLineExtensionAmountValue () == null ? BigDecimal.ZERO : aUBLLine
                                                                                                                        .getLineExtensionAmountValue ()
                                                                                                                        .setScale (SCALE_PRICE2,
                                                                                                                                   ROUNDING_MODE));

        // Special handling in case no VAT item is present
        if (BigHelper.isEQ0 (aUBLPercent))
          aTotalZeroPercLineExtensionAmount = aTotalZeroPercLineExtensionAmount.add (aEbiListLineItem.getLineItemAmount ());

        // Order reference per line
        for (final OrderLineReferenceType aUBLOrderLineReference : aUBLLine.getOrderLineReference ())
          if (StringHelper.isNotEmpty (aUBLOrderLineReference.getLineIDValue ()))
          {
            final Ebi43OrderReferenceDetailType aEbiOrderRefDetail = new Ebi43OrderReferenceDetailType ();

            // order reference
            String sUBLLineOrderReferenceID = null;
            if (aUBLOrderLineReference.getOrderReference () != null)
              sUBLLineOrderReferenceID = StringHelper.trim (aUBLOrderLineReference.getOrderReference ().getIDValue ());
            if (StringHelper.isEmpty (sUBLLineOrderReferenceID))
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
                                                         .errorFieldName ("InvoiceLine[" +
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
            if (StringHelper.isNotEmpty (aEbiOrderRefDetail.getOrderPositionNumber ()) &&
                StringHelper.isEmpty (sUBLLineOrderReferenceID))
            {
              if (m_aSettings.isOrderReferenceIDMandatory ())
              {
                // The line order reference is mandatory
                aTransformationErrorList.add (SingleError.builderError ()
                                                         .errorFieldName ("InvoiceLine[" +
                                                                          nLineIndex +
                                                                          "]/OrderLineReference/OrderReference/ID")
                                                         .errorText (EText.ORDER_REFERENCE_MISSING.getDisplayText (m_aDisplayLocale))
                                                         .build ());
              }
              else
              {
                aEbiOrderRefDetail.setOrderPositionNumber (null);
                aTransformationErrorList.add (SingleError.builderWarn ()
                                                         .errorFieldName ("InvoiceLine[" +
                                                                          nLineIndex +
                                                                          "]/OrderLineReference/OrderReference/ID")
                                                         .errorText (EText.ORDER_REFERENCE_MISSING_IGNORE_ORDER_POS.getDisplayText (m_aDisplayLocale))
                                                         .build ());
              }
            }

            if (StringHelper.isNotEmpty (sUBLLineOrderReferenceID))
              aEbiListLineItem.setInvoiceRecipientsOrderReference (aEbiOrderRefDetail);
            break;
          }

        // Reduction and surcharge
        if (aUBLLine.hasAllowanceChargeEntries ())
        {
          // Start with quantity*unitPrice for base amount
          BigDecimal aEbiBaseAmount = aEbiListLineItem.getQuantity ().getValue ().multiply (aEbiUnitPriceValue);
          final Ebi43ReductionAndSurchargeListLineItemDetailsType aEbiRSDetails = new Ebi43ReductionAndSurchargeListLineItemDetailsType ();

          // ebInterface can handle only Reduction or only Surcharge
          ETriState eSurcharge = ETriState.UNDEFINED;
          for (final AllowanceChargeType aUBLAllowanceCharge : aUBLLine.getAllowanceCharge ())
          {
            final boolean bItemIsSurcharge = aUBLAllowanceCharge.getChargeIndicator ().isValue ();

            // Remember for next item
            if (eSurcharge.isUndefined ())
              eSurcharge = ETriState.valueOf (bItemIsSurcharge);
            final boolean bSwapSigns = bItemIsSurcharge != eSurcharge.isTrue ();

            final Ebi43ReductionAndSurchargeBaseType aEbiRSItem = new Ebi43ReductionAndSurchargeBaseType ();
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
              aEbiRSDetails.addReductionListLineItemOrSurchargeListLineItemOrOtherVATableTaxListLineItem (new ObjectFactory ().createSurchargeListLineItem (aEbiRSItem));
              aEbiBaseAmount = aEbiBaseAmount.add (aEbiRSItem.getAmount ());
            }
            else
            {
              aEbiRSDetails.addReductionListLineItemOrSurchargeListLineItemOrOtherVATableTaxListLineItem (new ObjectFactory ().createReductionListLineItem (aEbiRSItem));
              aEbiBaseAmount = aEbiBaseAmount.subtract (aEbiRSItem.getAmount ());
            }

            aEbiRSItem.setComment (getAllowanceChargeComment (aUBLAllowanceCharge));
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
            final Ebi43DeliveryType aEbiDelivery = convertDelivery (aUBLDelivery,
                                                                    "InvoiceLine[" +
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

    if (aEbiVAT.hasNoVATItemEntries ())
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("Invoice")
                                               .errorText (EText.VAT_ITEM_MISSING.getDisplayText (m_aDisplayLocale))
                                               .build ());
      if (false)
      {
        // No default in this case
        final Ebi43VATItemType aEbiVATItem = new Ebi43VATItemType ();
        aEbiVATItem.setTaxedAmount (aTotalZeroPercLineExtensionAmount);
        final Ebi43VATRateType aEbiVATVATRate = new Ebi43VATRateType ();
        aEbiVATVATRate.setValue (BigDecimal.ZERO);
        aEbiVATItem.setVATRate (aEbiVATVATRate);
        aEbiVATItem.setAmount (aTotalZeroPercLineExtensionAmount);
        aEbiVAT.addVATItem (aEbiVATItem);
      }
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
        for (final Ebi43ItemListType aEbiItemList : aEbiDoc.getDetails ().getItemList ())
          for (final Ebi43ListLineItemType aEbiListLineItem : aEbiItemList.getListLineItem ())
            tmp = tmp.add (aEbiListLineItem.getLineItemAmount ());
        aEbiBaseAmount = tmp;
      }
      final Ebi43ReductionAndSurchargeDetailsType aEbiRS = new Ebi43ReductionAndSurchargeDetailsType ();

      int nAllowanceChargeIndex = 0;
      for (final AllowanceChargeType aUBLAllowanceCharge : aUBLDoc.getAllowanceCharge ())
      {
        final boolean bItemIsSurcharge = aUBLAllowanceCharge.getChargeIndicator ().isValue ();

        final Ebi43ReductionAndSurchargeType aEbiRSItem = new Ebi43ReductionAndSurchargeType ();
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

        aEbiRSItem.setComment (getAllowanceChargeComment (aUBLAllowanceCharge));

        Ebi43VATRateType aEbiVATRate = null;
        for (final TaxCategoryType aUBLTaxCategory : aUBLAllowanceCharge.getTaxCategory ())
          if (aUBLTaxCategory.getPercent () != null)
          {
            aEbiVATRate = new Ebi43VATRateType ();
            aEbiVATRate.setValue (aUBLTaxCategory.getPercentValue ());
            if (false)
              aEbiVATRate.setTaxCode (aUBLTaxCategory.getIDValue ());
            break;
          }
        if (aEbiVATRate == null)
        {
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .errorFieldName ("Invoice/AllowanceCharge[" +
                                                                    nAllowanceChargeIndex +
                                                                    "]")
                                                   .errorText (EText.ALLOWANCE_CHARGE_NO_TAXRATE.getDisplayText (m_aDisplayLocale))
                                                   .build ());
          // No default in this case
          if (false)
          {
            aEbiVATRate = new Ebi43VATRateType ();
            aEbiVATRate.setValue (BigDecimal.ZERO);
            aEbiVATRate.setTaxCode (ETaxCode.NOT_TAXABLE.getID ());
          }
        }
        aEbiRSItem.setVATRate (aEbiVATRate);

        if (bItemIsSurcharge)
        {
          aEbiRS.addReductionOrSurchargeOrOtherVATableTax (new ObjectFactory ().createSurcharge (aEbiRSItem));
          aEbiBaseAmount = aEbiBaseAmount.add (aEbiRSItem.getAmount ());
        }
        else
        {
          aEbiRS.addReductionOrSurchargeOrOtherVATableTax (new ObjectFactory ().createReduction (aEbiRSItem));
          aEbiBaseAmount = aEbiBaseAmount.subtract (aEbiRSItem.getAmount ());
        }
        aEbiDoc.setReductionAndSurchargeDetails (aEbiRS);
        ++nAllowanceChargeIndex;
      }
    }

    // PrepaidAmount is not supported!
    final MonetaryTotalType aUBLMonetaryTotal = aUBLDoc.getLegalMonetaryTotal ();
    if (aUBLMonetaryTotal.getPrepaidAmount () != null && BigHelper.isNE0 (aUBLMonetaryTotal.getPrepaidAmountValue ()))
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .errorFieldName ("Invoice/LegalMonetaryTotal/PrepaidAmount")
                                               .errorText (EText.PREPAID_NOT_SUPPORTED.getDisplayText (m_aDisplayLocale))
                                               .build ());
    }

    // Total gross amount
    if (aUBLMonetaryTotal.getTaxInclusiveAmountValue () != null)
      aEbiDoc.setTotalGrossAmount (aUBLMonetaryTotal.getTaxInclusiveAmountValue ()
                                                    .setScale (SCALE_PRICE2, ROUNDING_MODE));
    else
      aEbiDoc.setTotalGrossAmount (aUBLMonetaryTotal.getPayableAmountValue ().setScale (SCALE_PRICE2, ROUNDING_MODE));
    // Payable amount
    aEbiDoc.setPayableAmount (aUBLMonetaryTotal.getPayableAmountValue ().setScale (SCALE_PRICE2, ROUNDING_MODE));

    // Payment method
    convertPayment (aUBLDoc::getDueDateValue,
                    aUBLDoc::getPaymentMeans,
                    aUBLDoc::getPayeeParty,
                    aUBLDoc::getAccountingSupplierParty,
                    aUBLDoc::getPaymentTerms,
                    aUBLDoc::getLegalMonetaryTotal,
                    aTransformationErrorList,
                    aEbiDoc,
                    false);

    // Delivery
    Ebi43DeliveryType aEbiDelivery = null;
    {
      // Delivery address
      int nDeliveryIndex = 0;
      for (final DeliveryType aUBLDelivery : aUBLDoc.getDelivery ())
      {
        // Use the first delivery with a delivery date
        if (aUBLDelivery.getActualDeliveryDate () != null)
        {
          aEbiDelivery = convertDelivery (aUBLDelivery,
                                          "Delivery[" + nDeliveryIndex + "]",
                                          aUBLDoc.getAccountingCustomerParty (),
                                          aTransformationErrorList,
                                          m_aContentLocale,
                                          m_aDisplayLocale);
          break;
        }
        ++nDeliveryIndex;
      }

      if (aEbiDelivery == null)
        aEbiDelivery = new Ebi43DeliveryType ();

      // No delivery date is present - check for service period
      final PeriodType aUBLInvoicePeriod = CollectionHelper.getAtIndex (aUBLDoc.getInvoicePeriod (), 0);
      if (aUBLInvoicePeriod != null)
      {
        final XMLOffsetDate aStartDate = aUBLInvoicePeriod.getStartDateValue ();
        final XMLOffsetDate aEndDate = aUBLInvoicePeriod.getEndDateValue ();
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
            final Ebi43PeriodType aEbiPeriod = new Ebi43PeriodType ();
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
                                                 .errorFieldName ("Invoice")
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
