/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2023 AUSTRIAPRO - www.austriapro.at
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

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.math.MathHelper;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v41.*;

import jakarta.xml.bind.JAXBElement;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.AdditionalAccountIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.AllowanceChargeReasonType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.CompanyIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentCurrencyCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentDescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionNoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InvoiceTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.ItemClassificationCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.TaxExemptionReasonType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Convert an ebInterface 4.1 invoice to a UBL invoice
 *
 * @author Philip Helger
 */
public class EbInterface41ToInvoiceConverter extends AbstractEbInterface41ToUBLConverter
{
  public EbInterface41ToInvoiceConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  private static void _convertPayment (@Nonnull final Ebi41InvoiceType aEbiDoc, @Nonnull final InvoiceType aUBLDoc)
  {
    final Ebi41PaymentMethodType aEbiPaymentMethod = aEbiDoc.getPaymentMethod ();
    final Ebi41PaymentConditionsType aEbiPaymentConditions = aEbiDoc.getPaymentConditions ();

    // PaymentMeans
    if (aEbiPaymentMethod != null)
    {
      final Ebi41NoPaymentType aEbiNoPayment = aEbiPaymentMethod.getNoPayment ();
      if (aEbiNoPayment != null)
      {
        // no payment - nothing to emit
      }
      else
      {
        final Ebi41DirectDebitType aEbiDirectDebit = aEbiPaymentMethod.getDirectDebit ();
        if (aEbiDirectDebit != null)
        {
          // Direct debit (49)
          final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();
          aUBLPaymentMeans.setPaymentMeansCode ("49");
          if (aEbiPaymentConditions != null)
            aUBLPaymentMeans.setPaymentDueDate (aEbiPaymentConditions.getDueDate ());
          if (StringHelper.hasText (aEbiPaymentMethod.getComment ()))
            aUBLPaymentMeans.addInstructionNote (new InstructionNoteType (aEbiPaymentMethod.getComment ()));
          aUBLDoc.addPaymentMeans (aUBLPaymentMeans);
        }
        else
        {
          final Ebi41SEPADirectDebitType aEbiSepaDirectDebit = aEbiPaymentMethod.getSEPADirectDebit ();
          if (aEbiSepaDirectDebit != null)
          {
            // TODO SEPA Direct debit (59)
            // Back-mapping to ebInterface is also missing!!
            final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();
            aUBLPaymentMeans.setPaymentMeansCode ("59");
            if (aEbiPaymentConditions != null)
              aUBLPaymentMeans.setPaymentDueDate (aEbiPaymentConditions.getDueDate ());
            if (StringHelper.hasText (aEbiPaymentMethod.getComment ()))
              aUBLPaymentMeans.addInstructionNote (new InstructionNoteType (aEbiPaymentMethod.getComment ()));
            aUBLDoc.addPaymentMeans (aUBLPaymentMeans);
          }
          else
          {
            final Ebi41UniversalBankTransactionType aEbiUBT = aEbiPaymentMethod.getUniversalBankTransaction ();
            if (aEbiUBT != null)
            {
              // TODO universal bank transaction
              final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();
              // 30 = Credit transfer
              // 58 = SEPA credit transfer
              aUBLPaymentMeans.setPaymentMeansCode ("30");
              aUBLPaymentMeans.setPaymentChannelCode (PAYMENT_CHANNEL_CODE_IBAN);

              if (aEbiUBT.hasBeneficiaryAccountEntries ())
              {
                // First one only
                final Ebi41AccountType aEbiAccount = aEbiUBT.getBeneficiaryAccountAtIndex (0);
                final FinancialAccountType aUBLFinancialAccount = new FinancialAccountType ();
                final BranchType aUBLBranch = new BranchType ();
                final FinancialInstitutionType aUBLFinancialInstitution = new FinancialInstitutionType ();
                {
                  final IDType aUBLFIID = new IDType ();
                  if (StringHelper.hasText (aEbiAccount.getBIC ()))
                  {
                    aUBLFIID.setValue (aEbiAccount.getBIC ());
                    aUBLFIID.setSchemeID (SCHEME_BIC);
                  }
                  else
                    if (aEbiAccount.getBankCode () != null)
                    {
                      aUBLFIID.setValue (aEbiAccount.getBankCode ().getValue ().toString ());
                      aUBLFIID.setSchemeID ("local");
                    }
                    else
                      if (StringHelper.hasText (aEbiAccount.getBankName ()))
                      {
                        aUBLFIID.setValue (aEbiAccount.getBankName ());
                        aUBLFIID.setSchemeID ("name");
                      }
                  if (StringHelper.hasText (aUBLFIID.getValue ()))
                    aUBLFinancialInstitution.setID (aUBLFIID);
                }
                if (aUBLFinancialInstitution.getID () != null)
                  aUBLBranch.setFinancialInstitution (aUBLFinancialInstitution);
                {
                  final IDType aUBLFAID = new IDType ();
                  if (StringHelper.hasText (aEbiAccount.getIBAN ()))
                  {
                    // Could change payment means to SEPA, but code "58" is
                    // quite new, so stick with "30"
                    if (false)
                      aUBLPaymentMeans.setPaymentMeansCode ("58");
                    aUBLFAID.setValue (aEbiAccount.getIBAN ());
                    aUBLFAID.setSchemeID (SCHEME_IBAN);
                  }
                  else
                    if (StringHelper.hasText (aEbiAccount.getBankAccountNr ()))
                    {
                      aUBLFAID.setValue (aEbiAccount.getBankAccountNr ());
                      aUBLFAID.setSchemeID ("local");
                    }

                  if (StringHelper.hasText (aUBLFAID.getValue ()))
                    aUBLFinancialAccount.setID (aUBLFAID);
                }
                aUBLFinancialAccount.setName (aEbiAccount.getBankAccountOwner ());
                if (aUBLBranch.getFinancialInstitution () != null)
                  aUBLFinancialAccount.setFinancialInstitutionBranch (aUBLBranch);
                aUBLPaymentMeans.setPayeeFinancialAccount (aUBLFinancialAccount);
              }

              // PaymentReference
              if (aEbiUBT.getPaymentReference () != null)
                aUBLPaymentMeans.setInstructionID (new InstructionIDType (aEbiUBT.getPaymentReference ().getValue ()));

              if (aEbiPaymentConditions != null)
                aUBLPaymentMeans.setPaymentDueDate (aEbiPaymentConditions.getDueDate ());
              if (StringHelper.hasText (aEbiPaymentMethod.getComment ()))
                aUBLPaymentMeans.addInstructionNote (new InstructionNoteType (aEbiPaymentMethod.getComment ()));
              aUBLDoc.addPaymentMeans (aUBLPaymentMeans);
            }
            else
              throw new IllegalStateException ("Unsupported payment method present!");
          }
        }
      }
    }

    // PaymentTerms
    if (aEbiPaymentConditions != null)
    {
      // For the due date
      {
        final PaymentTermsType aUBLPaymentTerms = new PaymentTermsType ();
        aUBLPaymentTerms.setPaymentDueDate (aEbiPaymentConditions.getDueDate ());

        if (aEbiPaymentConditions.getMinimumPayment () != null)
        {
          final BigDecimal aPerc = aEbiPaymentConditions.getMinimumPayment ()
                                                        .divide (aEbiDoc.getPayableAmount (),
                                                                 SCALE_PRICE4,
                                                                 ROUNDING_MODE)
                                                        .multiply (CGlobal.BIGDEC_100);
          aUBLPaymentTerms.setPaymentPercent (aPerc);
        }

        if (StringHelper.hasText (aEbiPaymentConditions.getComment ()))
          aUBLPaymentTerms.addNote (new NoteType (aEbiPaymentConditions.getComment ()));

        aUBLDoc.addPaymentTerms (aUBLPaymentTerms);
      }

      // All the discounts
      final String sCurrency = aEbiDoc.getInvoiceCurrency () != null ? aEbiDoc.getInvoiceCurrency ().value () : null;
      for (final Ebi41DiscountType aEbiDiscount : aEbiPaymentConditions.getDiscount ())
      {
        final PaymentTermsType aUBLPaymentTerms = new PaymentTermsType ();

        final PeriodType aUBLSettlementPeriod = new PeriodType ();
        aUBLSettlementPeriod.setEndDate (aEbiDiscount.getPaymentDate ());
        aUBLPaymentTerms.setSettlementPeriod (aUBLSettlementPeriod);

        if (aEbiDiscount.getBaseAmount () != null)
          aUBLPaymentTerms.setAmount (aEbiDiscount.getBaseAmount ()).setCurrencyID (sCurrency);

        aUBLPaymentTerms.setSettlementDiscountPercent (aEbiDiscount.getPercentage ());

        if (aEbiDiscount.getAmount () != null)
          aUBLPaymentTerms.setSettlementDiscountAmount (aEbiDiscount.getAmount ()).setCurrencyID (sCurrency);

        aUBLDoc.addPaymentTerms (aUBLPaymentTerms);
      }
    }
  }

  @Nonnull
  public InvoiceType convertInvoice (@Nonnull final Ebi41InvoiceType aEbiDoc)
  {
    ValueEnforcer.notNull (aEbiDoc, "ebInterfaceDocument");

    final String sCurrency = aEbiDoc.getInvoiceCurrency () != null ? aEbiDoc.getInvoiceCurrency ().value () : null;

    final InvoiceType aUBLDoc = new InvoiceType ();
    aUBLDoc.setUBLVersionID (UBL_VERSION_21);

    // Attributes
    // GeneratingSystem cannot be mapped
    aUBLDoc.setInvoiceTypeCode (getTypeCode (aEbiDoc.getDocumentType (), InvoiceTypeCodeType::new));
    final DocumentCurrencyCodeType aUBLCurrency = aUBLDoc.setDocumentCurrencyCode (sCurrency);
    aUBLCurrency.setListAgencyID (CURRENCY_LIST_AGENCY_ID);
    aUBLCurrency.setListID (CURRENCY_LIST_ID);
    // ManualProcessing cannot be mapped
    // DocumentTitle is not mapped
    // Language is not mapped
    if (aEbiDoc.isIsDuplicate () != null)
      aUBLDoc.setCopyIndicator (aEbiDoc.isIsDuplicate ().booleanValue ());

    // Elements
    aUBLDoc.setID (aEbiDoc.getInvoiceNumber ());
    aUBLDoc.setIssueDate (aEbiDoc.getInvoiceDate ());

    // Handle CancelledOriginalDocument
    {
      final Ebi41CancelledOriginalDocumentType aEbiCancelledDoc = aEbiDoc.getCancelledOriginalDocument ();
      if (aEbiCancelledDoc != null)
      {
        final DocumentReferenceType aUBLDocRef = new DocumentReferenceType ();
        aUBLDocRef.setID (aEbiCancelledDoc.getInvoiceNumber ());
        aUBLDocRef.setIssueDate (aEbiCancelledDoc.getInvoiceDate ());
        aUBLDocRef.setDocumentType ("CancelledOriginalDocument");
        aUBLDocRef.setDocumentTypeCode (getTypeCode (aEbiCancelledDoc.getDocumentType (), DocumentTypeCodeType::new));
        if (StringHelper.hasText (aEbiCancelledDoc.getComment ()))
          aUBLDocRef.addDocumentDescription (new DocumentDescriptionType (aEbiCancelledDoc.getComment ()));
        aUBLDoc.addAdditionalDocumentReference (aUBLDocRef);
      }
    }

    // Handle RelatedDocument
    {
      for (final Ebi41RelatedDocumentType aEbiRelatedItem : aEbiDoc.getRelatedDocument ())
      {
        final DocumentReferenceType aUBLDocRef = new DocumentReferenceType ();
        aUBLDocRef.setID (aEbiRelatedItem.getInvoiceNumber ());
        aUBLDocRef.setIssueDate (aEbiRelatedItem.getInvoiceDate ());
        aUBLDocRef.setDocumentType ("RelatedDocument");
        aUBLDocRef.setDocumentTypeCode (getTypeCode (aEbiRelatedItem.getDocumentType (), DocumentTypeCodeType::new));
        if (StringHelper.hasText (aEbiRelatedItem.getComment ()))
          aUBLDocRef.addDocumentDescription (new DocumentDescriptionType (aEbiRelatedItem.getComment ()));
        aUBLDoc.addAdditionalDocumentReference (aUBLDocRef);
      }
    }

    // Handle Delivery
    {
      final DeliveryType aUBLDelivery = convertDelivery (aEbiDoc.getDelivery (), m_aContentLocale);
      if (aUBLDelivery != null)
      {
        // Remember in invoice
        if (aUBLDoc.getInvoicePeriodCount () == 0 && aUBLDelivery.getRequestedDeliveryPeriod () != null)
          aUBLDoc.addInvoicePeriod (aUBLDelivery.getRequestedDeliveryPeriod ());

        aUBLDoc.addDelivery (aUBLDelivery);
      }
    }

    // Handle Biller
    {
      final Ebi41BillerType aEbiBiller = aEbiDoc.getBiller ();
      if (aEbiBiller != null)
      {
        final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
        PartyType aUBLParty = convertParty (aEbiBiller.getAddress (), m_aContentLocale);
        if (StringHelper.hasText (aEbiBiller.getVATIdentificationNumber ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          final PartyTaxSchemeType aPTS = new PartyTaxSchemeType ();
          aPTS.setTaxScheme (createTaxSchemeVAT ());
          final CompanyIDType aCID = aPTS.setCompanyID (aEbiBiller.getVATIdentificationNumber ());
          aCID.setSchemeID (SUPPORTED_TAX_SCHEME_ID);
          aUBLParty.addPartyTaxScheme (aPTS);
        }
        if (StringHelper.hasText (aEbiBiller.getInvoiceRecipientsBillerID ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          // Set in 2 different places
          aUBLSupplier.setCustomerAssignedAccountID (aEbiBiller.getInvoiceRecipientsBillerID ());
          final PartyIdentificationType aPI = new PartyIdentificationType ();
          aPI.setID (aEbiBiller.getInvoiceRecipientsBillerID ());
          aUBLParty.addPartyIdentification (aPI);
        }
        aUBLSupplier.setParty (aUBLParty);

        // Put this into global contract document references
        for (final Ebi41FurtherIdentificationType aEbiFI : aEbiBiller.getFurtherIdentification ())
        {
          final DocumentReferenceType aUBLContractDoc = new DocumentReferenceType ();
          final IDType aID = new IDType ();
          aID.setValue (aEbiFI.getValue ());
          aID.setSchemeID (aEbiFI.getIdentificationType ());
          aUBLContractDoc.setID (aID);
          aUBLDoc.addContractDocumentReference (aUBLContractDoc);
        }

        aUBLDoc.setAccountingSupplierParty (aUBLSupplier);
      }
    }

    // Handle Invoice Recipient
    {
      final Ebi41InvoiceRecipientType aEbiIR = aEbiDoc.getInvoiceRecipient ();
      if (aEbiIR != null)
      {
        final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
        PartyType aUBLParty = convertParty (aEbiIR.getAddress (), m_aContentLocale);
        if (StringHelper.hasText (aEbiIR.getVATIdentificationNumber ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          final PartyTaxSchemeType aPTS = new PartyTaxSchemeType ();
          aPTS.setTaxScheme (createTaxSchemeVAT ());
          final CompanyIDType aCID = aPTS.setCompanyID (aEbiIR.getVATIdentificationNumber ());
          aCID.setSchemeID (SUPPORTED_TAX_SCHEME_ID);
          aUBLParty.addPartyTaxScheme (aPTS);
        }
        if (StringHelper.hasText (aEbiIR.getBillersInvoiceRecipientID ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          // Set in 2 different places
          aUBLCustomer.setSupplierAssignedAccountID (aEbiIR.getBillersInvoiceRecipientID ());
          final PartyIdentificationType aPI = new PartyIdentificationType ();
          aPI.setID (aEbiIR.getBillersInvoiceRecipientID ());
          aUBLParty.addPartyIdentification (aPI);
        }
        aUBLCustomer.setParty (aUBLParty);

        // Put this into global contract document references
        for (final Ebi41FurtherIdentificationType aEbiFI : aEbiIR.getFurtherIdentification ())
        {
          final AdditionalAccountIDType aUBLAddAccID = new AdditionalAccountIDType ();
          aUBLAddAccID.setValue (aEbiFI.getValue ());
          aUBLAddAccID.setSchemeID (aEbiFI.getIdentificationType ());
          aUBLCustomer.addAdditionalAccountID (aUBLAddAccID);
        }

        // Handle order reference from invoice recipient
        final Ebi41OrderReferenceType aEbiOR = aEbiIR.getOrderReference ();
        if (aEbiOR != null)
        {
          final OrderReferenceType aUBLOR = new OrderReferenceType ();
          aUBLOR.setID (aEbiOR.getOrderID ());
          if (aEbiOR.getReferenceDate () != null)
            aUBLOR.setIssueDate (aEbiOR.getReferenceDate ());
          aUBLDoc.setOrderReference (aUBLOR);
        }

        aUBLDoc.setAccountingCustomerParty (aUBLCustomer);
      }
    }

    // Handle OrderingParty
    {
      final Ebi41OrderingPartyType aEbiOrdering = aEbiDoc.getOrderingParty ();
      if (aEbiOrdering != null)
      {
        final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
        PartyType aUBLParty = convertParty (aEbiOrdering.getAddress (), m_aContentLocale);
        if (StringHelper.hasText (aEbiOrdering.getVATIdentificationNumber ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          final PartyTaxSchemeType aPTS = new PartyTaxSchemeType ();
          aPTS.setTaxScheme (createTaxSchemeVAT ());
          final CompanyIDType aCID = aPTS.setCompanyID (aEbiOrdering.getVATIdentificationNumber ());
          aCID.setSchemeID (SUPPORTED_TAX_SCHEME_ID);
          aUBLParty.addPartyTaxScheme (aPTS);
        }
        if (StringHelper.hasText (aEbiOrdering.getBillersOrderingPartyID ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          // Set in 2 different places
          aUBLCustomer.setSupplierAssignedAccountID (aEbiOrdering.getBillersOrderingPartyID ());
          final PartyIdentificationType aPI = new PartyIdentificationType ();
          aPI.setID (aEbiOrdering.getBillersOrderingPartyID ());
          aUBLParty.addPartyIdentification (aPI);
        }
        aUBLCustomer.setParty (aUBLParty);

        // Put this into global contract document references
        for (final Ebi41FurtherIdentificationType aEbiFI : aEbiOrdering.getFurtherIdentification ())
        {
          final DocumentReferenceType aUBLContractDoc = new DocumentReferenceType ();
          final IDType aID = new IDType ();
          aID.setValue (aEbiFI.getValue ());
          aID.setSchemeID (aEbiFI.getIdentificationType ());
          aUBLContractDoc.setID (aID);
          aUBLDoc.addContractDocumentReference (aUBLContractDoc);
        }

        aUBLDoc.setBuyerCustomerParty (aUBLCustomer);
      }
    }

    // Details
    // Header and footer are not translated
    BigDecimal aTaxExclusiveAmount = BigDecimal.ZERO;
    final Ebi41DetailsType aEbiDetails = aEbiDoc.getDetails ();
    int nInvoiceLineIndex = 1;
    for (final Ebi41ItemListType aEbiItemList : aEbiDetails.getItemList ())
    {
      for (final Ebi41ListLineItemType aEbiItem : aEbiItemList.getListLineItem ())
      {
        final InvoiceLineType aUBLLine = new InvoiceLineType ();
        aUBLLine.setID (aEbiItem.getPositionNumber () != null ? aEbiItem.getPositionNumber ().toString ()
                                                              : Integer.toString (nInvoiceLineIndex));

        String sUOM = StringHelper.trim (aEbiItem.getQuantity ().getUnit ());
        if (sUOM == null)
          sUOM = UOM_DEFAULT;

        aUBLLine.setInvoicedQuantity (aEbiItem.getQuantity ().getValue ()).setUnitCode (sUOM);
        aUBLLine.setLineExtensionAmount (aEbiItem.getLineItemAmount ()).setCurrencyID (sCurrency);

        final PriceType aUBLPrice = new PriceType ();
        aUBLPrice.setPriceAmount (aEbiItem.getUnitPrice ().getValue ()).setCurrencyID (sCurrency);
        if (aEbiItem.getUnitPrice ().getBaseQuantity () != null)
          aUBLPrice.setBaseQuantity (aEbiItem.getUnitPrice ().getBaseQuantity ());
        aUBLLine.setPrice (aUBLPrice);

        if (aEbiItem.getDelivery () != null)
          aUBLLine.addDelivery (convertDelivery (aEbiItem.getDelivery (), m_aContentLocale));

        {
          final ItemType aUBLItem = new ItemType ();
          for (final String sEbiDesc : aEbiItem.getDescription ())
            aUBLItem.addDescription (new DescriptionType (sEbiDesc));
          aUBLItem.setPackSizeNumeric (BigDecimal.ONE);

          {
            TaxCategoryType aUBLTaxCategory;
            if (aEbiItem.getTaxExemption () != null)
            {
              // Exempt
              aUBLTaxCategory = createTaxCategoryVAT ("E");
              aUBLTaxCategory.setPercent (BigDecimal.ZERO);
              if (StringHelper.hasText (aEbiItem.getTaxExemption ().getValue ()))
                aUBLTaxCategory.addTaxExemptionReason (new TaxExemptionReasonType (aEbiItem.getTaxExemption ()
                                                                                           .getValue ()));
            }
            else
            {
              // Standard
              aUBLTaxCategory = createTaxCategoryVAT ("S");
              aUBLTaxCategory.setPercent (aEbiItem.getVATRate ().getValue ());
              if (StringHelper.hasText (aEbiItem.getVATRate ().getTaxCode ()))
                aUBLTaxCategory.setName (aEbiItem.getVATRate ().getTaxCode ());
            }

            aUBLItem.addClassifiedTaxCategory (aUBLTaxCategory);
          }

          if (aEbiItem.getReductionAndSurchargeListLineItemDetails () != null)
            for (final JAXBElement <?> aEbiRS : aEbiItem.getReductionAndSurchargeListLineItemDetails ()
                                                        .getReductionListLineItemOrSurchargeListLineItemOrOtherVATableTaxListLineItem ())
            {
              final Object aValue = aEbiRS.getValue ();
              final AllowanceChargeType aUBLAC = new AllowanceChargeType ();

              if (aValue instanceof Ebi41OtherVATableTaxBaseType)
              {
                // Other VAT-able tax
                final Ebi41OtherVATableTaxBaseType aEbiRSValue = (Ebi41OtherVATableTaxBaseType) aValue;

                aUBLAC.setChargeIndicator (true);
                aUBLAC.setBaseAmount (aEbiRSValue.getBaseAmount ()).setCurrencyID (sCurrency);
                if (aEbiRSValue.getPercentage () != null)
                  aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getPercentage ().divide (CGlobal.BIGDEC_100));
                if (aEbiRSValue.getAmount () != null)
                  aUBLAC.setAmount (aEbiRSValue.getAmount ()).setCurrencyID (sCurrency);
                else
                  if (aEbiRSValue.getPercentage () != null)
                    aUBLAC.setAmount (MathHelper.getPercentValue (aEbiRSValue.getBaseAmount (),
                                                                  aEbiRSValue.getPercentage ()))
                          .setCurrencyID (sCurrency);
                if (StringHelper.hasText (aEbiRSValue.getTaxID ()))
                  aUBLAC.setAllowanceChargeReasonCode (aEbiRSValue.getTaxID ());
                if (StringHelper.hasText (aEbiRSValue.getComment ()))
                  aUBLAC.addAllowanceChargeReason (new AllowanceChargeReasonType (aEbiRSValue.getComment ()));
              }
              else
              {
                // Reduction/surcharge
                final Ebi41ReductionAndSurchargeBaseType aEbiRSValue = (Ebi41ReductionAndSurchargeBaseType) aValue;
                final boolean bIsReduction = aEbiRS.getName ().getLocalPart ().equals ("ReductionListLineItem");

                aUBLAC.setChargeIndicator (!bIsReduction);
                aUBLAC.setBaseAmount (aEbiRSValue.getBaseAmount ()).setCurrencyID (sCurrency);
                if (aEbiRSValue.getPercentage () != null)
                  aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getPercentage ().divide (CGlobal.BIGDEC_100));
                if (aEbiRSValue.getAmount () != null)
                  aUBLAC.setAmount (aEbiRSValue.getAmount ()).setCurrencyID (sCurrency);
                else
                  if (aEbiRSValue.getPercentage () != null)
                    aUBLAC.setAmount (MathHelper.getPercentValue (aEbiRSValue.getBaseAmount (),
                                                                  aEbiRSValue.getPercentage ()))
                          .setCurrencyID (sCurrency);
                if (StringHelper.hasText (aEbiRSValue.getComment ()))
                  aUBLAC.addAllowanceChargeReason (new AllowanceChargeReasonType (aEbiRSValue.getComment ()));
              }

              aUBLLine.addAllowanceCharge (aUBLAC);
            }

          for (final Ebi41ArticleNumberType aArticleNumber : aEbiItem.getArticleNumber ())
          {
            final ItemIdentificationType aUBLIID = new ItemIdentificationType ();
            aUBLIID.setID (aArticleNumber.getContent ());
            if (aArticleNumber.getArticleNumberType () != null)
            {
              final PartyType aUBLIssuer = new PartyType ();
              final PartyIdentificationType aUBLPI = new PartyIdentificationType ();
              aUBLPI.setID (aArticleNumber.getArticleNumberType ().value ());
              aUBLIssuer.addPartyIdentification (aUBLPI);
              aUBLIID.setIssuerParty (aUBLIssuer);
            }
            aUBLItem.addManufacturersItemIdentification (aUBLIID);
          }

          final Ebi41AdditionalInformationType aEbiAdditionalInfo = aEbiItem.getAdditionalInformation ();
          if (aEbiAdditionalInfo != null)
          {
            for (final String sSerialNumber : aEbiAdditionalInfo.getSerialNumber ())
            {
              aUBLItem.addAdditionalItemProperty (createItemProperty ("SerialNumber", sSerialNumber));
            }
            for (final String sChargeNumber : aEbiAdditionalInfo.getSerialNumber ())
            {
              aUBLItem.addAdditionalItemProperty (createItemProperty ("ChargeNumber", sChargeNumber));
            }
            for (final Ebi41ClassificationType aEbiClassification : aEbiAdditionalInfo.getClassification ())
            {
              final CommodityClassificationType aUBLCC = new CommodityClassificationType ();
              final ItemClassificationCodeType aUBLICC = new ItemClassificationCodeType ();
              aUBLICC.setValue (aEbiClassification.getValue ());
              aUBLICC.setName (aEbiClassification.getClassificationSchema ());
              aUBLCC.setItemClassificationCode (aUBLICC);
              aUBLItem.addCommodityClassification (aUBLCC);
            }
            if (aEbiAdditionalInfo.getAlternativeQuantity () != null)
            {
              aUBLItem.addAdditionalItemProperty (createItemProperty ("AlternativeQuantity",
                                                                      aEbiAdditionalInfo.getAlternativeQuantity ()
                                                                                        .getValue ()
                                                                                        .toString ()));
            }
            if (aEbiAdditionalInfo.getSize () != null)
            {
              aUBLItem.addAdditionalItemProperty (createItemProperty ("Size", aEbiAdditionalInfo.getSize ()));
            }
            if (aEbiAdditionalInfo.getWeight () != null)
            {
              aUBLItem.addAdditionalItemProperty (createItemProperty ("Weight",
                                                                      aEbiAdditionalInfo.getWeight ()
                                                                                        .getValue ()
                                                                                        .toString ()));
            }
            if (aEbiAdditionalInfo.getBoxes () != null)
            {
              aUBLItem.addAdditionalItemProperty (createItemProperty ("Boxes",
                                                                      aEbiAdditionalInfo.getBoxes ().toString ()));
            }
            if (aEbiAdditionalInfo.getColor () != null)
            {
              aUBLItem.addAdditionalItemProperty (createItemProperty ("Color", aEbiAdditionalInfo.getColor ()));
            }
          }

          aUBLLine.setItem (aUBLItem);
        }

        // Order line ref
        final Ebi41OrderReferenceDetailType aEbiORLine = aEbiItem.getInvoiceRecipientsOrderReference ();
        if (aEbiORLine != null)
        {
          final OrderLineReferenceType aUBLOrderLineRef = new OrderLineReferenceType ();
          boolean bAny1 = false;
          {
            final OrderReferenceType aUBLOrderRef = new OrderReferenceType ();
            boolean bAny = false;
            if (aEbiORLine.getOrderID () != null)
            {
              aUBLOrderRef.setID (aEbiORLine.getOrderID ());
              bAny = true;
            }
            if (aEbiORLine.getReferenceDate () != null)
            {
              aUBLOrderRef.setIssueDate (aEbiORLine.getReferenceDate ());
              bAny = true;
            }
            if (bAny)
            {
              aUBLOrderLineRef.setOrderReference (aUBLOrderRef);
              bAny1 = true;
            }
          }
          if (aEbiORLine.getOrderPositionNumber () != null)
          {
            aUBLOrderLineRef.setLineID (aEbiORLine.getOrderPositionNumber ());
            bAny1 = true;
          }
          if (bAny1)
            aUBLLine.addOrderLineReference (aUBLOrderLineRef);
        }

        aUBLDoc.addInvoiceLine (aUBLLine);

        aTaxExclusiveAmount = aTaxExclusiveAmount.add (aEbiItem.getLineItemAmount ());
        ++nInvoiceLineIndex;
      }
    }

    // Below the line items
    BigDecimal aSumBelowTheLine = BigDecimal.ZERO;
    {
      int nBelowTheLineIndex = 1;
      for (final Ebi41BelowTheLineItemType aEbiItem : aEbiDetails.getBelowTheLineItem ())
      {
        final InvoiceLineType aUBLLine = new InvoiceLineType ();
        aUBLLine.setID ("BTL" + nBelowTheLineIndex);
        aUBLLine.addNote (new NoteType ("BelowTheLineItem"));
        aUBLLine.setInvoicedQuantity (BigDecimal.ONE).setUnitCode (UOM_DEFAULT);
        aUBLLine.setLineExtensionAmount (aEbiItem.getLineItemAmount ()).setCurrencyID (sCurrency);

        final PriceType aUBLPrice = new PriceType ();
        aUBLPrice.setPriceAmount (aEbiItem.getLineItemAmount ()).setCurrencyID (sCurrency);
        aUBLPrice.setBaseQuantity (BigDecimal.ONE);
        aUBLLine.setPrice (aUBLPrice);

        {
          final ItemType aUBLItem = new ItemType ();
          if (StringHelper.hasText (aEbiItem.getDescription ()))
            aUBLItem.addDescription (new DescriptionType (aEbiItem.getDescription ()));
          aUBLItem.setPackSizeNumeric (BigDecimal.ONE);

          {
            // Exempt
            final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT ("E");
            aUBLTaxCategory.setPercent (BigDecimal.ZERO);
            aUBLTaxCategory.addTaxExemptionReason (new TaxExemptionReasonType ("BelowTheLine - tax already handled"));

            aUBLItem.addClassifiedTaxCategory (aUBLTaxCategory);
          }
          aUBLLine.setItem (aUBLItem);
        }
        aUBLDoc.addInvoiceLine (aUBLLine);

        aTaxExclusiveAmount = aTaxExclusiveAmount.add (aEbiItem.getLineItemAmount ());
        aSumBelowTheLine = aSumBelowTheLine.add (aEbiItem.getLineItemAmount ());
        ++nBelowTheLineIndex;
      }
    }

    _convertPayment (aEbiDoc, aUBLDoc);

    // global allowances and charges
    BigDecimal aSumCharges = BigDecimal.ZERO;
    BigDecimal aSumAllowances = BigDecimal.ZERO;
    if (aEbiDoc.getReductionAndSurchargeDetails () != null)
      for (final JAXBElement <?> aEbiRS : aEbiDoc.getReductionAndSurchargeDetails ()
                                                 .getReductionOrSurchargeOrOtherVATableTax ())
      {
        final Object aValue = aEbiRS.getValue ();
        final AllowanceChargeType aUBLAC = new AllowanceChargeType ();
        BigDecimal aAmount = null;
        if (aValue instanceof Ebi41OtherVATableTaxType)
        {
          // Other VAT-able tax
          final Ebi41OtherVATableTaxType aEbiRSValue = (Ebi41OtherVATableTaxType) aValue;

          aUBLAC.setChargeIndicator (true);
          aUBLAC.setBaseAmount (aEbiRSValue.getBaseAmount ()).setCurrencyID (sCurrency);
          if (aEbiRSValue.getPercentage () != null)
            aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getPercentage ().divide (CGlobal.BIGDEC_100));
          if (aEbiRSValue.getAmount () != null)
            aAmount = aEbiRSValue.getAmount ();
          else
            if (aEbiRSValue.getPercentage () != null)
              aAmount = MathHelper.getPercentValue (aEbiRSValue.getBaseAmount (), aEbiRSValue.getPercentage ());
          if (StringHelper.hasText (aEbiRSValue.getTaxID ()))
            aUBLAC.setAllowanceChargeReasonCode (aEbiRSValue.getTaxID ());
          if (StringHelper.hasText (aEbiRSValue.getComment ()))
            aUBLAC.addAllowanceChargeReason (new AllowanceChargeReasonType (aEbiRSValue.getComment ()));

          // add tax category
          final TaxCategoryType aUBLTaxCategory = createTaxCategoryOther ();
          aUBLTaxCategory.setPercent (aEbiRSValue.getPercentage ());
          aUBLAC.addTaxCategory (aUBLTaxCategory);
        }
        else
        {
          // Reduction/surcharge
          final Ebi41ReductionAndSurchargeType aEbiRSValue = (Ebi41ReductionAndSurchargeType) aValue;
          final boolean bIsReduction = aEbiRS.getName ().getLocalPart ().equals ("Reduction");

          aUBLAC.setChargeIndicator (!bIsReduction);
          aUBLAC.setBaseAmount (aEbiRSValue.getBaseAmount ()).setCurrencyID (sCurrency);
          if (aEbiRSValue.getPercentage () != null)
            aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getPercentage ().divide (CGlobal.BIGDEC_100));
          if (aEbiRSValue.getAmount () != null)
            aAmount = aEbiRSValue.getAmount ();
          else
            if (aEbiRSValue.getPercentage () != null)
              aAmount = MathHelper.getPercentValue (aEbiRSValue.getBaseAmount (), aEbiRSValue.getPercentage ());
          if (StringHelper.hasText (aEbiRSValue.getComment ()))
            aUBLAC.addAllowanceChargeReason (new AllowanceChargeReasonType (aEbiRSValue.getComment ()));

          // add tax category
          final Ebi41VATRateType aEbiTaxItem = aEbiRSValue.getVATRate ();
          final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT ("S");
          aUBLTaxCategory.setPercent (aEbiTaxItem.getValue ());
          aUBLAC.addTaxCategory (aUBLTaxCategory);
        }

        if (aAmount != null)
        {
          aUBLAC.setAmount (aAmount).setCurrencyID (sCurrency);
          if (aUBLAC.isChargeIndicatorValue (false))
            aSumCharges = aSumCharges.add (aAmount);
          else
            aSumAllowances = aSumAllowances.add (aAmount);
        }

        aUBLDoc.addAllowanceCharge (aUBLAC);
      }

    // VAT total
    {
      final TaxTotalType aUBLTaxTotal = new TaxTotalType ();
      BigDecimal aTaxSum = BigDecimal.ZERO;
      for (final Ebi41VATItemType aEbiVATItem : aEbiDoc.getTax ().getVAT ().getVATItem ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();
        aUBLTaxSubtotal.setTaxableAmount (aEbiVATItem.getTaxedAmount ()).setCurrencyID (sCurrency);
        aUBLTaxSubtotal.setTaxAmount (aEbiVATItem.getAmount ()).setCurrencyID (sCurrency);

        TaxCategoryType aUBLTaxCategory;
        if (aEbiVATItem.getTaxExemption () != null)
        {
          // Exempt
          aUBLTaxCategory = createTaxCategoryVAT ("E");
          aUBLTaxCategory.setPercent (BigDecimal.ZERO);
          if (StringHelper.hasText (aEbiVATItem.getTaxExemption ().getValue ()))
            aUBLTaxCategory.addTaxExemptionReason (new TaxExemptionReasonType (aEbiVATItem.getTaxExemption ()
                                                                                          .getValue ()));
        }
        else
        {
          // Standard
          aUBLTaxCategory = createTaxCategoryVAT ("S");
          aUBLTaxCategory.setPercent (aEbiVATItem.getVATRate ().getValue ());
          if (StringHelper.hasText (aEbiVATItem.getVATRate ().getTaxCode ()))
            aUBLTaxCategory.setName (aEbiVATItem.getVATRate ().getTaxCode ());
        }
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);
        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);

        aTaxSum = aTaxSum.add (aEbiVATItem.getAmount ());
      }
      for (final Ebi41OtherTaxType aEbiOtherTax : aEbiDoc.getTax ().getOtherTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();
        aUBLTaxSubtotal.setTaxAmount (aEbiOtherTax.getAmount ()).setCurrencyID (sCurrency);

        final TaxCategoryType aUBLTaxCategory = createTaxCategoryOther ();
        if (StringHelper.hasText (aEbiOtherTax.getComment ()))
          aUBLTaxCategory.getTaxScheme ().setName (aEbiOtherTax.getComment ());
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);

        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);

        aTaxSum = aTaxSum.add (aEbiOtherTax.getAmount ());
      }
      aUBLTaxTotal.setTaxAmount (aTaxSum).setCurrencyID (sCurrency);
      aUBLDoc.addTaxTotal (aUBLTaxTotal);
    }

    // Monetary Totals
    {
      // LineExtensionAmount
      BigDecimal aSumLineExtension = BigDecimal.ZERO;
      for (final InvoiceLineType aInvoiceLine : aUBLDoc.getInvoiceLine ())
        aSumLineExtension = aSumLineExtension.add (aInvoiceLine.getLineExtensionAmountValue ());

      final MonetaryTotalType aUBLMT = new MonetaryTotalType ();
      aUBLMT.setLineExtensionAmount (aSumLineExtension).setCurrencyID (sCurrency);
      aUBLMT.setAllowanceTotalAmount (aSumAllowances).setCurrencyID (sCurrency);
      aUBLMT.setChargeTotalAmount (aSumCharges).setCurrencyID (sCurrency);
      aUBLMT.setTaxExclusiveAmount (aTaxExclusiveAmount).setCurrencyID (sCurrency);
      aUBLMT.setTaxInclusiveAmount (aEbiDoc.getTotalGrossAmount ()).setCurrencyID (sCurrency);
      aUBLMT.setPayableAmount (aEbiDoc.getPayableAmount ()).setCurrencyID (sCurrency);
      aUBLDoc.setLegalMonetaryTotal (aUBLMT);
    }

    // Comment
    if (StringHelper.hasText (aEbiDoc.getComment ()))
      aUBLDoc.addNote (new NoteType (aEbiDoc.getComment ()));

    return aUBLDoc;
  }
}
