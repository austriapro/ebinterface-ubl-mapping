/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2019 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.to;

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBElement;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.math.MathHelper;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v40.*;
import com.helger.ebinterface.v40.extensions.Ebi40TaxExtensionType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.CompanyIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentCurrencyCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionNoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InvoiceTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.ItemClassificationCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.PaymentIDType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Convert an ebInterface 4.0 invoice to a UBL invoice
 *
 * @author Philip Helger
 */
public class EbInterface40ToInvoiceConverter extends AbstractEbInterface40ToUBLConverter
{
  public EbInterface40ToInvoiceConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  private static void _convertPayment (@Nonnull final Ebi40InvoiceType aEbiDoc, @Nonnull final InvoiceType aUBLDoc)
  {
    final Ebi40PaymentMethodType aEbiPaymentMethod = aEbiDoc.getPaymentMethod ();
    final Ebi40PaymentConditionsType aEbiPaymentConditions = aEbiDoc.getPaymentConditions ();

    // PaymentMeans
    if (aEbiPaymentMethod != null)
    {
      if (aEbiPaymentMethod instanceof Ebi40NoPaymentType)
      {
        // no payment - nothing to emit
      }
      else
        if (aEbiPaymentMethod instanceof Ebi40DirectDebitType)
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
          if (aEbiPaymentMethod instanceof Ebi40UniversalBankTransactionType)
          {
            // TODO universal bank transaction
            final Ebi40UniversalBankTransactionType aEbiUBT = (Ebi40UniversalBankTransactionType) aEbiPaymentMethod;
            final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();
            // 30 = Credit transfer
            // 58 = SEPA credit transfer
            aUBLPaymentMeans.setPaymentMeansCode ("30");
            aUBLPaymentMeans.setPaymentChannelCode (PAYMENT_CHANNEL_CODE_IBAN);

            if (aEbiUBT.hasBeneficiaryAccountEntries ())
            {
              // First one only
              final Ebi40AccountType aEbiAccount = aEbiUBT.getBeneficiaryAccountAtIndex (0);
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
                  {
                    aUBLFIID.setValue (aEbiAccount.getBankName ());
                    aUBLFIID.setSchemeID ("name");
                  }
                aUBLFinancialInstitution.setID (aUBLFIID);
              }
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
                {
                  aUBLFAID.setValue (aEbiAccount.getBankAccountNr ());
                  aUBLFAID.setSchemeID ("local");
                }
                aUBLFinancialAccount.setID (aUBLFAID);
              }
              aUBLFinancialAccount.setName (aEbiAccount.getBankAccountOwner ());
              aUBLFinancialAccount.setFinancialInstitutionBranch (aUBLBranch);
              aUBLPaymentMeans.setPayeeFinancialAccount (aUBLFinancialAccount);
            }

            // PaymentReference
            if (aEbiUBT.getPaymentReference () != null)
              aUBLPaymentMeans.addPaymentID (new PaymentIDType (aEbiUBT.getPaymentReference ().getValue ()));

            if (aEbiPaymentConditions != null)
              aUBLPaymentMeans.setPaymentDueDate (aEbiPaymentConditions.getDueDate ());
            if (StringHelper.hasText (aEbiPaymentMethod.getComment ()))
              aUBLPaymentMeans.addInstructionNote (new InstructionNoteType (aEbiPaymentMethod.getComment ()));
            aUBLDoc.addPaymentMeans (aUBLPaymentMeans);
          }
          else
            throw new IllegalStateException ("Unsupported payment method present!");
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
                                                        .divide (aEbiDoc.getTotalGrossAmount (),
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
      for (final Ebi40DiscountType aEbiDiscount : aEbiPaymentConditions.getDiscount ())
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
  public InvoiceType convertInvoice (@Nonnull final Ebi40InvoiceType aEbiDoc)
  {
    ValueEnforcer.notNull (aEbiDoc, "ebInterfaceDocument");

    final String sCurrency = aEbiDoc.getInvoiceCurrency () != null ? aEbiDoc.getInvoiceCurrency ().value () : null;

    final InvoiceType aUBLDoc = new InvoiceType ();
    aUBLDoc.setUBLVersionID (UBL_VERSION_21);

    // Attributes
    // GeneratingSystem cannot be mapped
    aUBLDoc.setInvoiceTypeCode (getTypeCode (aEbiDoc.getDocumentType (), InvoiceTypeCodeType::new));
    final DocumentCurrencyCodeType aUBLCurrency = aUBLDoc.setDocumentCurrencyCode (sCurrency);
    aUBLCurrency.setListAgencyID ("6");
    aUBLCurrency.setListID ("ISO 4017 Alpha");
    // ManualProcessing cannot be mapped
    // DocumentTitle is not mapped
    // Language is not mapped
    // CopyIndicator is not mapped

    // Elements
    aUBLDoc.setID (aEbiDoc.getInvoiceNumber ());
    aUBLDoc.setIssueDate (aEbiDoc.getInvoiceDate ());

    // No document references in ebi 4.0

    // Handle Delivery
    {
      final DeliveryType aUBLDelivery = convertDelivery (aEbiDoc.getDelivery ());
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
      final Ebi40BillerType aEbiBiller = aEbiDoc.getBiller ();
      if (aEbiBiller != null)
      {
        final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
        PartyType aUBLParty = convertParty (aEbiBiller.getAddress ());
        if (StringHelper.hasText (aEbiBiller.getVATIdentificationNumber ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          final PartyTaxSchemeType aPTS = new PartyTaxSchemeType ();
          aPTS.setTaxScheme (createTaxSchemeVAT ());
          final CompanyIDType aCID = aPTS.setCompanyID (aEbiBiller.getVATIdentificationNumber ());
          aCID.setSchemeID (SUPPORTED_TAX_SCHEME_ID.getID ());
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
        for (final Ebi40FurtherIdentificationType aEbiFI : aEbiBiller.getFurtherIdentification ())
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
      final Ebi40InvoiceRecipientType aEbiIR = aEbiDoc.getInvoiceRecipient ();
      if (aEbiIR != null)
      {
        final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
        PartyType aUBLParty = convertParty (aEbiIR.getAddress ());
        if (StringHelper.hasText (aEbiIR.getVATIdentificationNumber ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          final PartyTaxSchemeType aPTS = new PartyTaxSchemeType ();
          aPTS.setTaxScheme (createTaxSchemeVAT ());
          final CompanyIDType aCID = aPTS.setCompanyID (aEbiIR.getVATIdentificationNumber ());
          aCID.setSchemeID (SUPPORTED_TAX_SCHEME_ID.getID ());
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

        // Handle order reference from invoice recipient
        final Ebi40OrderReferenceType aEbiOR = aEbiIR.getOrderReference ();
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
      final Ebi40OrderingPartyType aEbiOrdering = aEbiDoc.getOrderingParty ();
      if (aEbiOrdering != null)
      {
        final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
        PartyType aUBLParty = convertParty (aEbiOrdering.getAddress ());
        if (StringHelper.hasText (aEbiOrdering.getVATIdentificationNumber ()))
        {
          if (aUBLParty == null)
            aUBLParty = new PartyType ();

          final PartyTaxSchemeType aPTS = new PartyTaxSchemeType ();
          aPTS.setTaxScheme (createTaxSchemeVAT ());
          final CompanyIDType aCID = aPTS.setCompanyID (aEbiOrdering.getVATIdentificationNumber ());
          aCID.setSchemeID (SUPPORTED_TAX_SCHEME_ID.getID ());
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

        aUBLDoc.setBuyerCustomerParty (aUBLCustomer);
      }
    }

    // Details
    // Header and footer are not translated
    BigDecimal aTaxExclusiveAmount = BigDecimal.ZERO;
    final Ebi40DetailsType aEbiDetails = aEbiDoc.getDetails ();
    int nInvoiceLineIndex = 1;
    for (final Ebi40ItemListType aEbiItemList : aEbiDetails.getItemList ())
    {
      for (final Ebi40ListLineItemType aEbiItem : aEbiItemList.getListLineItem ())
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
        aUBLPrice.setPriceAmount (aEbiItem.getUnitPrice ()).setCurrencyID (sCurrency);
        aUBLLine.setPrice (aUBLPrice);

        if (aEbiItem.getDelivery () != null)
          aUBLLine.addDelivery (convertDelivery (aEbiItem.getDelivery ()));

        {
          final ItemType aUBLItem = new ItemType ();
          for (final String sEbiDesc : aEbiItem.getDescription ())
            aUBLItem.addDescription (new DescriptionType (sEbiDesc));
          aUBLItem.setPackSizeNumeric (BigDecimal.ONE);

          {
            TaxCategoryType aUBLTaxCategory;
            // Standard
            aUBLTaxCategory = createTaxCategoryVAT ("S");
            aUBLTaxCategory.setPercent (aEbiItem.getTaxRateValue ());
            if (StringHelper.hasText (aEbiItem.getTaxRate ().getTaxCode ()))
              aUBLTaxCategory.setName (aEbiItem.getTaxRate ().getTaxCode ());

            aUBLItem.addClassifiedTaxCategory (aUBLTaxCategory);
          }

          if (aEbiItem.getReductionAndSurchargeListLineItemDetails () != null)
          {
            for (final Ebi40ReductionAndSurchargeBaseType aEbiRSValue : aEbiItem.getReductionAndSurchargeListLineItemDetails ()
                                                                                .getReductionListLineItem ())
            {
              final AllowanceChargeType aUBLAC = new AllowanceChargeType ();
              aUBLAC.setChargeIndicator (false);
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

              aUBLLine.addAllowanceCharge (aUBLAC);
            }
            for (final Ebi40ReductionAndSurchargeBaseType aEbiRSValue : aEbiItem.getReductionAndSurchargeListLineItemDetails ()
                                                                                .getSurchargeListLineItem ())
            {
              final AllowanceChargeType aUBLAC = new AllowanceChargeType ();
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

              aUBLLine.addAllowanceCharge (aUBLAC);
            }
          }

          for (final Ebi40ArticleNumberType aArticleNumber : aEbiItem.getArticleNumber ())
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

          final Ebi40AdditionalInformationType aEbiAdditionalInfo = aEbiItem.getAdditionalInformation ();
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
            for (final Ebi40ClassificationType aEbiClassification : aEbiAdditionalInfo.getClassification ())
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
        final Ebi40OrderReferenceDetailType aEbiORLine = aEbiItem.getInvoiceRecipientsOrderReference ();
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
      for (final JAXBElement <Ebi40ReductionAndSurchargeType> aEbiRS : aEbiDoc.getReductionAndSurchargeDetails ()
                                                                              .getReductionOrSurcharge ())
      {
        final Ebi40ReductionAndSurchargeType aEbiRSValue = aEbiRS.getValue ();
        final AllowanceChargeType aUBLAC = new AllowanceChargeType ();

        final boolean bIsReduction = aEbiRS.getName ().getLocalPart ().equals ("Reduction");
        aUBLAC.setChargeIndicator (!bIsReduction);

        aUBLAC.setBaseAmount (aEbiRSValue.getBaseAmount ()).setCurrencyID (sCurrency);
        if (aEbiRSValue.getPercentage () != null)
          aUBLAC.setMultiplierFactorNumeric (aEbiRSValue.getPercentage ().divide (CGlobal.BIGDEC_100));

        BigDecimal aAmount = null;
        if (aEbiRSValue.getAmount () != null)
          aAmount = aEbiRSValue.getAmount ();
        else
          if (aEbiRSValue.getPercentage () != null)
            aAmount = MathHelper.getPercentValue (aEbiRSValue.getBaseAmount (), aEbiRSValue.getPercentage ());

        // add tax category
        final Ebi40TaxRateType aEbiTaxItem = aEbiRSValue.getTaxRate ();
        final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT ("S");
        aUBLTaxCategory.setPercent (aEbiTaxItem.getValue ());
        aUBLAC.addTaxCategory (aUBLTaxCategory);

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
      for (final Ebi40ItemType aEbiVATItem : aEbiDoc.getTax ().getVAT ().getItem ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();
        aUBLTaxSubtotal.setTaxableAmount (aEbiVATItem.getTaxedAmount ()).setCurrencyID (sCurrency);
        aUBLTaxSubtotal.setTaxAmount (aEbiVATItem.getAmount ()).setCurrencyID (sCurrency);

        // Standard
        final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT ("S");
        aUBLTaxCategory.setPercent (aEbiVATItem.getTaxRateValue ());
        if (StringHelper.hasText (aEbiVATItem.getTaxRate ().getTaxCode ()))
          aUBLTaxCategory.setName (aEbiVATItem.getTaxRate ().getTaxCode ());
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);
        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);

        aTaxSum = aTaxSum.add (aEbiVATItem.getAmount ());
      }
      final Ebi40TaxExtensionType aEbiTaxEx = aEbiDoc.getTax ().getTaxExtension ();
      if (aEbiTaxEx != null)
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();
        aUBLTaxSubtotal.setTaxAmount (BigDecimal.ZERO).setCurrencyID (sCurrency);

        final TaxCategoryType aUBLTaxCategory = createTaxCategoryVAT ("E");
        aUBLTaxCategory.setPercent (BigDecimal.ZERO);
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);
        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);
      }
      for (final Ebi40OtherTaxType aEbiOtherTax : aEbiDoc.getTax ().getOtherTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();
        aUBLTaxSubtotal.setTaxableAmount (aEbiOtherTax.getAmount ()).setCurrencyID (sCurrency);

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
      aUBLMT.setPayableAmount (aEbiDoc.getTotalGrossAmount ()).setCurrencyID (sCurrency);
      aUBLDoc.setLegalMonetaryTotal (aUBLMT);
    }

    return aUBLDoc;
  }
}
