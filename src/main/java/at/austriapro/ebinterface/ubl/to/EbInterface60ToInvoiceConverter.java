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
package at.austriapro.ebinterface.ubl.to;

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.math.MathHelper;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v60.*;

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
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Convert an ebInterface 6.0 invoice to a UBL invoice
 *
 * @author Philip Helger
 */
public class EbInterface60ToInvoiceConverter extends AbstractEbInterface60ToUBLConverter
{
  public EbInterface60ToInvoiceConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  private static void _convertPayment (@Nonnull final Ebi60InvoiceType aEbiDoc, @Nonnull final InvoiceType aUBLDoc)
  {
    final Ebi60PaymentMethodType aEbiPaymentMethod = aEbiDoc.getPaymentMethod ();
    final Ebi60PaymentConditionsType aEbiPaymentConditions = aEbiDoc.getPaymentConditions ();

    // PaymentMeans
    if (aEbiPaymentMethod != null)
    {
      final Ebi60NoPaymentType aEbiNoPayment = aEbiPaymentMethod.getNoPayment ();
      if (aEbiNoPayment != null)
      {
        // no payment - nothing to emit
      }
      else
      {
        final Ebi60OtherPaymentType aEbiOtherPayment = aEbiPaymentMethod.getOtherPayment ();
        if (aEbiOtherPayment != null)
        {
          // other payment - nothing to emit
        }
        else
        {
          final Ebi60SEPADirectDebitType aEbiSepaDirectDebit = aEbiPaymentMethod.getSEPADirectDebit ();
          if (aEbiSepaDirectDebit != null)
          {
            // SEPA Direct debit (59)
            final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();
            aUBLPaymentMeans.setPaymentMeansCode (PAYMENT_MEANS_SEPA_DIRECT_DEBIT);

            final FinancialAccountType aUBLFinancialAccount = new FinancialAccountType ();
            aUBLFinancialAccount.setID (aEbiSepaDirectDebit.getIBAN ());
            final BranchType aUBLBranch = new BranchType ();
            aUBLBranch.setID (aEbiSepaDirectDebit.getBIC ());
            aUBLFinancialAccount.setFinancialInstitutionBranch (aUBLBranch);
            aUBLPaymentMeans.setPayeeFinancialAccount (aUBLFinancialAccount);

            final PaymentMandateType aUBLMandate = new PaymentMandateType ();
            aUBLMandate.setID (aEbiSepaDirectDebit.getMandateReference ());
            aUBLPaymentMeans.setPaymentMandate (aUBLMandate);

            final PartyIdentificationType aPartyID = new PartyIdentificationType ();
            final IDType aPartyIDID = new IDType ();
            aPartyIDID.setSchemeID (SCHEME_SEPA);
            aPartyIDID.setValue (aEbiSepaDirectDebit.getCreditorID ());
            aPartyID.setID (aPartyIDID);
            aUBLDoc.getAccountingSupplierParty ().getParty ().addPartyIdentification (aPartyID);

            if (aEbiPaymentConditions != null)
              aUBLPaymentMeans.setPaymentDueDate (aEbiPaymentConditions.getDueDate ());
            if (StringHelper.hasText (aEbiPaymentMethod.getComment ()))
              aUBLPaymentMeans.addInstructionNote (new InstructionNoteType (aEbiPaymentMethod.getComment ()));
            aUBLDoc.addPaymentMeans (aUBLPaymentMeans);
          }
          else
          {
            final Ebi60UniversalBankTransactionType aEbiUBT = aEbiPaymentMethod.getUniversalBankTransaction ();
            if (aEbiUBT != null)
            {
              // TODO universal bank transaction
              final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();
              aUBLPaymentMeans.setPaymentMeansCode (PAYMENT_MEANS_CREDIT_TRANSFER);
              aUBLPaymentMeans.setPaymentChannelCode (PAYMENT_CHANNEL_CODE_IBAN);

              if (aEbiUBT.hasBeneficiaryAccountEntries ())
              {
                // First one only
                final Ebi60AccountType aEbiAccount = aEbiUBT.getBeneficiaryAccountAtIndex (0);
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
                      aUBLFIID.setSchemeID (aEbiAccount.getBankCode ().getBankCodeType ());
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
          final BigDecimal aPerc = MathHelper.isEQ0 (aEbiDoc.getPayableAmount ()) ? BigDecimal.ZERO
                                                                                  : aEbiPaymentConditions.getMinimumPayment ()
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
      for (final Ebi60DiscountType aEbiDiscount : aEbiPaymentConditions.getDiscount ())
      {
        final PaymentTermsType aUBLPaymentTerms = new PaymentTermsType ();

        final PeriodType aUBLSettlementPeriod = new PeriodType ();
        aUBLSettlementPeriod.setEndDate (aEbiDiscount.getPaymentDate ());
        aUBLPaymentTerms.setSettlementPeriod (aUBLSettlementPeriod);

        if (aEbiDiscount.getBaseAmount () != null)
          aUBLPaymentTerms.setAmount (aEbiDiscount.getBaseAmount ()).setCurrencyID (aEbiDoc.getInvoiceCurrency ());

        aUBLPaymentTerms.setSettlementDiscountPercent (aEbiDiscount.getPercentage ());

        if (aEbiDiscount.getAmount () != null)
          aUBLPaymentTerms.setSettlementDiscountAmount (aEbiDiscount.getAmount ())
                          .setCurrencyID (aEbiDoc.getInvoiceCurrency ());

        if (StringHelper.hasText (aEbiDiscount.getComment ()))
          aUBLPaymentTerms.addNote (new NoteType (aEbiDiscount.getComment ()));

        aUBLDoc.addPaymentTerms (aUBLPaymentTerms);
      }
    }
  }

  @Nonnull
  public InvoiceType convertInvoice (@Nonnull final Ebi60InvoiceType aEbiDoc)
  {
    ValueEnforcer.notNull (aEbiDoc, "ebInterfaceDocument");

    final String sCurrency = aEbiDoc.getInvoiceCurrency ();

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
      final Ebi60CancelledOriginalDocumentType aEbiCancelledDoc = aEbiDoc.getCancelledOriginalDocument ();
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
      for (final Ebi60RelatedDocumentType aEbiRelatedItem : aEbiDoc.getRelatedDocument ())
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
      final Ebi60BillerType aEbiBiller = aEbiDoc.getBiller ();
      if (aEbiBiller != null)
      {
        final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
        PartyType aUBLParty = convertParty (aEbiBiller.getAddress (), aEbiBiller.getContact (), m_aContentLocale);
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
        for (final Ebi60FurtherIdentificationType aEbiFI : aEbiBiller.getFurtherIdentification ())
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
      final Ebi60InvoiceRecipientType aEbiIR = aEbiDoc.getInvoiceRecipient ();
      if (aEbiIR != null)
      {
        final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
        PartyType aUBLParty = convertParty (aEbiIR.getAddress (), aEbiIR.getContact (), m_aContentLocale);
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
        for (final Ebi60FurtherIdentificationType aEbiFI : aEbiIR.getFurtherIdentification ())
        {
          final AdditionalAccountIDType aUBLAddAccID = new AdditionalAccountIDType ();
          aUBLAddAccID.setValue (aEbiFI.getValue ());
          aUBLAddAccID.setSchemeID (aEbiFI.getIdentificationType ());
          aUBLCustomer.addAdditionalAccountID (aUBLAddAccID);
        }

        // Handle order reference from invoice recipient
        final Ebi60OrderReferenceType aEbiOR = aEbiIR.getOrderReference ();
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
      final Ebi60OrderingPartyType aEbiOrdering = aEbiDoc.getOrderingParty ();
      if (aEbiOrdering != null)
      {
        final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
        PartyType aUBLParty = convertParty (aEbiOrdering.getAddress (), aEbiOrdering.getContact (), m_aContentLocale);
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
        for (final Ebi60FurtherIdentificationType aEbiFI : aEbiOrdering.getFurtherIdentification ())
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
    final Ebi60DetailsType aEbiDetails = aEbiDoc.getDetails ();
    int nInvoiceLineIndex = 1;
    for (final Ebi60ItemListType aEbiItemList : aEbiDetails.getItemList ())
    {
      for (final Ebi60ListLineItemType aEbiItem : aEbiItemList.getListLineItem ())
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

          final Ebi60TaxItemType aEbiTaxItem = aEbiItem.getTaxItem ();
          final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT (aEbiTaxItem.getTaxPercent ()
                                                                                   .getTaxCategoryCode ());
          aUBLTaxCategory.setPercent (aEbiTaxItem.getTaxPercentValue ());

          aUBLItem.addClassifiedTaxCategory (aUBLTaxCategory);

          if (aEbiItem.getReductionAndSurchargeListLineItemDetails () != null)
            for (final JAXBElement <?> aEbiRS : aEbiItem.getReductionAndSurchargeListLineItemDetails ()
                                                        .getReductionListLineItemOrSurchargeListLineItemOrOtherVATableTaxListLineItem ())
            {
              final Object aValue = aEbiRS.getValue ();
              final AllowanceChargeType aUBLAC = new AllowanceChargeType ();

              if (aValue instanceof Ebi60OtherVATableTaxType)
              {
                // Other VAT-able tax
                final Ebi60OtherVATableTaxType aEbiRSValue = (Ebi60OtherVATableTaxType) aValue;

                aUBLAC.setChargeIndicator (true);
                aUBLAC.setBaseAmount (aEbiRSValue.getTaxableAmount ()).setCurrencyID (sCurrency);
                aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getTaxPercentValue ().divide (CGlobal.BIGDEC_100));
                if (aEbiRSValue.getTaxAmount () != null)
                  aUBLAC.setAmount (aEbiRSValue.getTaxAmount ()).setCurrencyID (sCurrency);
                else
                  aUBLAC.setAmount (MathHelper.getPercentValue (aEbiRSValue.getTaxableAmount (),
                                                                aEbiRSValue.getTaxPercentValue ()))
                        .setCurrencyID (sCurrency);
                if (StringHelper.hasText (aEbiRSValue.getTaxID ()))
                  aUBLAC.setAllowanceChargeReasonCode (aEbiRSValue.getTaxID ());
                if (StringHelper.hasText (aEbiRSValue.getComment ()))
                  aUBLAC.addAllowanceChargeReason (new AllowanceChargeReasonType (aEbiRSValue.getComment ()));
              }
              else
              {
                // Reduction/surcharge
                final Ebi60ReductionAndSurchargeBaseType aEbiRSValue = (Ebi60ReductionAndSurchargeBaseType) aValue;
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

              if (aUBLAC.getAmount () != null)
                aUBLLine.addAllowanceCharge (aUBLAC);
            }

          for (final Ebi60ArticleNumberType aArticleNumber : aEbiItem.getArticleNumber ())
          {
            final ItemIdentificationType aUBLIID = new ItemIdentificationType ();
            aUBLIID.setID (aArticleNumber.getValue ());
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

          for (final Ebi60AdditionalInformationType aEbiAdditionalInfo : aEbiItem.getAdditionalInformation ())
          {
            aUBLItem.addAdditionalItemProperty (createItemProperty (aEbiAdditionalInfo.getKey (),
                                                                    aEbiAdditionalInfo.getValue ()));
          }

          aUBLLine.setItem (aUBLItem);
        }

        // Order line ref
        final Ebi60OrderReferenceDetailType aEbiORLine = aEbiItem.getInvoiceRecipientsOrderReference ();
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
        final BigDecimal aAmount;
        if (aValue instanceof Ebi60OtherVATableTaxType)
        {
          // Other VAT-able tax
          final Ebi60OtherVATableTaxType aEbiRSValue = (Ebi60OtherVATableTaxType) aValue;

          aUBLAC.setChargeIndicator (true);
          aUBLAC.setBaseAmount (aEbiRSValue.getTaxableAmount ()).setCurrencyID (sCurrency);
          aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getTaxPercentValue ().divide (CGlobal.BIGDEC_100));
          if (aEbiRSValue.getTaxAmount () != null)
            aAmount = aEbiRSValue.getTaxAmount ();
          else
            aAmount = MathHelper.getPercentValue (aEbiRSValue.getTaxableAmount (), aEbiRSValue.getTaxPercentValue ());
          if (StringHelper.hasText (aEbiRSValue.getTaxID ()))
            aUBLAC.setAllowanceChargeReasonCode (aEbiRSValue.getTaxID ());
          if (StringHelper.hasText (aEbiRSValue.getComment ()))
            aUBLAC.addAllowanceChargeReason (new AllowanceChargeReasonType (aEbiRSValue.getComment ()));

          // add tax category
          final TaxCategoryType aUBLTaxCategory = createTaxCategoryOther ();
          aUBLTaxCategory.setPercent (aEbiRSValue.getTaxPercentValue ());
          aUBLAC.addTaxCategory (aUBLTaxCategory);
        }
        else
        {
          // Reduction/surcharge
          final Ebi60ReductionAndSurchargeType aEbiRSValue = (Ebi60ReductionAndSurchargeType) aValue;
          final boolean bIsReduction = aEbiRS.getName ().getLocalPart ().equals ("Reduction");

          aUBLAC.setChargeIndicator (!bIsReduction);
          aUBLAC.setBaseAmount (aEbiRSValue.getBaseAmount ()).setCurrencyID (sCurrency);
          if (aEbiRSValue.getPercentage () != null)
            aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getPercentage ().divide (CGlobal.BIGDEC_100));
          if (aEbiRSValue.getAmount () != null)
            aAmount = aEbiRSValue.getAmount ();
          else
            aAmount = MathHelper.getPercentValue (aEbiRSValue.getBaseAmount (), aEbiRSValue.getPercentage ());
          if (StringHelper.hasText (aEbiRSValue.getComment ()))
            aUBLAC.addAllowanceChargeReason (new AllowanceChargeReasonType (aEbiRSValue.getComment ()));

          // add tax category
          final Ebi60TaxItemType aEbiTaxItem = aEbiRSValue.getTaxItem ();
          final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT (aEbiTaxItem.getTaxPercent ()
                                                                                   .getTaxCategoryCode ());
          aUBLTaxCategory.setPercent (aEbiTaxItem.getTaxPercentValue ());
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
      for (final Ebi60TaxItemType aEbiVATItem : aEbiDoc.getTax ().getTaxItem ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();
        aUBLTaxSubtotal.setTaxableAmount (aEbiVATItem.getTaxableAmount ()).setCurrencyID (sCurrency);

        final BigDecimal aAmount;
        if (aEbiVATItem.getTaxAmount () != null)
          aAmount = aEbiVATItem.getTaxAmount ();
        else
          aAmount = MathHelper.getPercentValue (aEbiVATItem.getTaxableAmount (), aEbiVATItem.getTaxPercentValue ());
        aUBLTaxSubtotal.setTaxAmount (aAmount).setCurrencyID (sCurrency);

        {
          final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT (aEbiVATItem.getTaxPercent ()
                                                                                   .getTaxCategoryCode ());
          aUBLTaxCategory.setPercent (aEbiVATItem.getTaxPercentValue ());
          aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);
        }
        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);

        if (aAmount != null)
          aTaxSum = aTaxSum.add (aAmount);
      }
      for (final Ebi60OtherTaxType aEbiOtherTax : aEbiDoc.getTax ().getOtherTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();
        aUBLTaxSubtotal.setTaxAmount (aEbiOtherTax.getTaxAmount ()).setCurrencyID (sCurrency);

        final TaxCategoryType aUBLTaxCategory = createTaxCategoryOther ();
        if (StringHelper.hasText (aEbiOtherTax.getComment ()))
          aUBLTaxCategory.getTaxScheme ().setName (aEbiOtherTax.getComment ());
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);

        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);

        aTaxSum = aTaxSum.add (aEbiOtherTax.getTaxAmount ());
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
