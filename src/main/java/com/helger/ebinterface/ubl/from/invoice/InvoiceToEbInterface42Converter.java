/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.from.invoice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.datatype.XMLGregorianCalendar;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.errorlist.ErrorList;
import com.helger.commons.math.MathHelper;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.StringParser;
import com.helger.ebinterface.codelist.ETaxCode;
import com.helger.ebinterface.ubl.from.EbInterface42Helper;
import com.helger.ebinterface.ubl.from.helper.SchemedID;
import com.helger.ebinterface.ubl.from.helper.TaxCategoryKey;
import com.helger.ebinterface.v42.Ebi42AccountType;
import com.helger.ebinterface.v42.Ebi42BillerType;
import com.helger.ebinterface.v42.Ebi42DeliveryType;
import com.helger.ebinterface.v42.Ebi42DetailsType;
import com.helger.ebinterface.v42.Ebi42DirectDebitType;
import com.helger.ebinterface.v42.Ebi42DiscountType;
import com.helger.ebinterface.v42.Ebi42DocumentTypeType;
import com.helger.ebinterface.v42.Ebi42FurtherIdentificationType;
import com.helger.ebinterface.v42.Ebi42InvoiceRecipientType;
import com.helger.ebinterface.v42.Ebi42InvoiceType;
import com.helger.ebinterface.v42.Ebi42ItemListType;
import com.helger.ebinterface.v42.Ebi42ListLineItemType;
import com.helger.ebinterface.v42.Ebi42NoPaymentType;
import com.helger.ebinterface.v42.Ebi42OrderReferenceDetailType;
import com.helger.ebinterface.v42.Ebi42OrderReferenceType;
import com.helger.ebinterface.v42.Ebi42OrderingPartyType;
import com.helger.ebinterface.v42.Ebi42OtherTaxType;
import com.helger.ebinterface.v42.Ebi42PaymentConditionsType;
import com.helger.ebinterface.v42.Ebi42PaymentMethodType;
import com.helger.ebinterface.v42.Ebi42PaymentReferenceType;
import com.helger.ebinterface.v42.Ebi42PeriodType;
import com.helger.ebinterface.v42.Ebi42ReductionAndSurchargeBaseType;
import com.helger.ebinterface.v42.Ebi42ReductionAndSurchargeDetailsType;
import com.helger.ebinterface.v42.Ebi42ReductionAndSurchargeListLineItemDetailsType;
import com.helger.ebinterface.v42.Ebi42ReductionAndSurchargeType;
import com.helger.ebinterface.v42.Ebi42TaxType;
import com.helger.ebinterface.v42.Ebi42UnitPriceType;
import com.helger.ebinterface.v42.Ebi42UnitType;
import com.helger.ebinterface.v42.Ebi42UniversalBankTransactionType;
import com.helger.ebinterface.v42.Ebi42VATItemType;
import com.helger.ebinterface.v42.Ebi42VATRateType;
import com.helger.ebinterface.v42.Ebi42VATType;
import com.helger.ebinterface.v42.ObjectFactory;
import com.helger.peppol.codelist.ETaxSchemeID;
import com.helger.ubl21.codelist.EPaymentMeansCode21;
import com.helger.ubl21.codelist.EUnitOfMeasureCode21;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AllowanceChargeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialAccountType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderLineReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyTaxSchemeType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentMeansType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentTermsType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSubtotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxTotalType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionNoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NameType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.PaymentIDType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Main converter between UBL 2.1 invoice and ebInterface 4.2 invoice.
 *
 * @author philip
 */
@Immutable
public final class InvoiceToEbInterface42Converter extends AbstractInvoiceConverter
{
  public static final int PAYMENT_REFERENCE_MAX_LENGTH = 35;

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
  public InvoiceToEbInterface42Converter (@Nonnull final Locale aDisplayLocale,
                                          @Nonnull final Locale aContentLocale,
                                          final boolean bStrictERBMode)
  {
    super (aDisplayLocale, aContentLocale, bStrictERBMode);
  }

  private static void _setPaymentMeansComment (@Nonnull final PaymentMeansType aUBLPaymentMeans,
                                               @Nonnull final Ebi42PaymentMethodType aEbiPaymentMethod)
  {
    if (aUBLPaymentMeans.hasInstructionNoteEntries ())
    {
      final List <String> aNotes = new ArrayList <String> ();
      for (final InstructionNoteType aUBLNote : aUBLPaymentMeans.getInstructionNote ())
      {
        final String sNote = StringHelper.trim (aUBLNote.getValue ());
        if (StringHelper.hasText (sNote))
          aNotes.add (sNote);
      }
      if (!aNotes.isEmpty ())
        aEbiPaymentMethod.setComment (StringHelper.getImploded ('\n', aNotes));
    }
  }

  private void _convertPayment (@Nonnull final InvoiceType aUBLDoc,
                                @Nonnull final ErrorList aTransformationErrorList,
                                @Nonnull final Ebi42InvoiceType aEbiDoc)
  {
    final Ebi42PaymentMethodType aEbiPaymentMethod = new Ebi42PaymentMethodType ();
    final Ebi42PaymentConditionsType aEbiPaymentConditions = new Ebi42PaymentConditionsType ();

    {
      int nPaymentMeansIndex = 0;
      for (final PaymentMeansType aUBLPaymentMeans : aUBLDoc.getPaymentMeans ())
      {
        final String sPaymentMeansCode = StringHelper.trim (aUBLPaymentMeans.getPaymentMeansCodeValue ());
        final EPaymentMeansCode21 ePaymentMeans = EPaymentMeansCode21.getFromIDOrNull (sPaymentMeansCode);
        if (ePaymentMeans == EPaymentMeansCode21._30 ||
            ePaymentMeans == EPaymentMeansCode21._31 ||
            ePaymentMeans == EPaymentMeansCode21._42)
        {
          // Credit transfer (30)
          // Debit transfer (31)
          // Payment to bank account (42)

          // Is a payment channel code present?
          final String sPaymentChannelCode = StringHelper.trim (aUBLPaymentMeans.getPaymentChannelCodeValue ());
          // null for standard PEPPOL BIS
          if (sPaymentChannelCode == null || PAYMENT_CHANNEL_CODE_IBAN.equals (sPaymentChannelCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi42UniversalBankTransactionType aEbiUBTMethod = new Ebi42UniversalBankTransactionType ();

            // Find payment reference
            int nPaymentIDIndex = 0;
            for (final PaymentIDType aUBLPaymentID : aUBLPaymentMeans.getPaymentID ())
            {
              String sUBLPaymentID = StringHelper.trim (aUBLPaymentID.getValue ());
              if (StringHelper.hasText (sUBLPaymentID))
              {
                if (sUBLPaymentID.length () > PAYMENT_REFERENCE_MAX_LENGTH)
                {
                  // Reference
                  aTransformationErrorList.addWarning ("PaymentMeans[" +
                                                       nPaymentMeansIndex +
                                                       "]/PaymentID[" +
                                                       nPaymentIDIndex +
                                                       "]",
                                                       EText.PAYMENT_ID_TOO_LONG_CUT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                             sUBLPaymentID));
                  sUBLPaymentID = sUBLPaymentID.substring (0, PAYMENT_REFERENCE_MAX_LENGTH);
                }

                final Ebi42PaymentReferenceType aEbiPaymentReference = new Ebi42PaymentReferenceType ();
                aEbiPaymentReference.setValue (sUBLPaymentID);
                aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
              }
              ++nPaymentIDIndex;
            }

            // Beneficiary account
            final Ebi42AccountType aEbiAccount = new Ebi42AccountType ();

            // BIC
            final FinancialAccountType aUBLFinancialAccount = aUBLPaymentMeans.getPayeeFinancialAccount ();
            if (aUBLFinancialAccount.getFinancialInstitutionBranch () != null &&
                aUBLFinancialAccount.getFinancialInstitutionBranch ().getFinancialInstitution () != null)
            {
              final String sBIC = StringHelper.trim (aUBLFinancialAccount.getFinancialInstitutionBranch ()
                                                                         .getFinancialInstitution ()
                                                                         .getIDValue ());

              aEbiAccount.setBIC (sBIC);

              if (StringHelper.hasNoText (sBIC) || !RegExHelper.stringMatchesPattern (REGEX_BIC, sBIC))
              {
                aTransformationErrorList.addError ("PaymentMeans[" +
                                                   nPaymentMeansIndex +
                                                   "]/PayeeFinancialAccount/FinancialInstitutionBranch/FinancialInstitution/ID",
                                                   EText.BIC_INVALID.getDisplayTextWithArgs (m_aDisplayLocale, sBIC));
                aEbiAccount.setBIC (null);
              }
            }

            // IBAN
            final String sIBAN = StringHelper.trim (aUBLPaymentMeans.getPayeeFinancialAccount ().getIDValue ());
            aEbiAccount.setIBAN (sIBAN);
            if (StringHelper.getLength (sIBAN) > IBAN_MAX_LENGTH)
            {
              aTransformationErrorList.addWarning ("PaymentMeans[" +
                                                   nPaymentMeansIndex +
                                                   "]/PayeeFinancialAccount/ID",
                                                   EText.IBAN_TOO_LONG.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                               sIBAN,
                                                                                               Integer.valueOf (IBAN_MAX_LENGTH)));
              aEbiAccount.setIBAN (sIBAN.substring (0, IBAN_MAX_LENGTH));
            }

            // Bank Account Owner - no field present - check PayeePart or
            // SupplierPartyName
            String sBankAccountOwnerName = null;
            if (aUBLDoc.getPayeeParty () != null)
              for (final PartyNameType aPartyName : aUBLDoc.getPayeeParty ().getPartyName ())
              {
                sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                if (StringHelper.hasText (sBankAccountOwnerName))
                  break;
              }
            if (StringHelper.hasNoText (sBankAccountOwnerName))
            {
              final PartyType aSupplierParty = aUBLDoc.getAccountingSupplierParty ().getParty ();
              if (aSupplierParty != null)
                for (final PartyNameType aPartyName : aSupplierParty.getPartyName ())
                {
                  sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                  if (StringHelper.hasText (sBankAccountOwnerName))
                    break;
                }
            }
            aEbiAccount.setBankAccountOwner (sBankAccountOwnerName);

            aEbiUBTMethod.getBeneficiaryAccount ().add (aEbiAccount);
            aEbiPaymentMethod.setUniversalBankTransaction (aEbiUBTMethod);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

            break;
          }

          aTransformationErrorList.addWarning ("PaymentMeans[" +
                                               nPaymentMeansIndex +
                                               "]",
                                               EText.PAYMENTMEANS_UNSUPPORTED_CHANNELCODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                  sPaymentChannelCode));
        }
        else
          if (ePaymentMeans == EPaymentMeansCode21._49)
          {
            // Direct debit (49)

            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi42DirectDebitType aEbiDirectDebit = new Ebi42DirectDebitType ();
            aEbiPaymentMethod.setDirectDebit (aEbiDirectDebit);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

            break;
          }
          else
          {
            // No supported payment means code
            if (MathHelper.isEqualToZero (aEbiDoc.getPayableAmount ()))
            {
              // As nothing is to be paid we can safely use NoPayment
              _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
              final Ebi42NoPaymentType aEbiNoPayment = new Ebi42NoPaymentType ();
              aEbiPaymentMethod.setNoPayment (aEbiNoPayment);
              break;
            }

            aTransformationErrorList.addError ("PaymentMeans[" +
                                               nPaymentMeansIndex +
                                               "]",
                                               EText.PAYMENTMEANS_CODE_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                       ePaymentMeans.getID (),
                                                                                                       EPaymentMeansCode21._31.getID (),
                                                                                                       EPaymentMeansCode21._49.getID ()));
          }

        ++nPaymentMeansIndex;
      }
    }

    if (m_bStrictERBMode)
    {
      if (aEbiDoc.getPaymentMethod () == null)
        aTransformationErrorList.addError ("Invoice", EText.ERB_NO_PAYMENT_METHOD.getDisplayText (m_aDisplayLocale));
    }

    // Payment terms
    {
      final List <String> aPaymentConditionsNotes = new ArrayList <String> ();
      int nPaymentTermsIndex = 0;
      for (final PaymentTermsType aUBLPaymentTerms : aUBLDoc.getPaymentTerms ())
      {
        if (aUBLPaymentTerms.getSettlementDiscountPercent () != null)
        {
          if (aUBLPaymentTerms.getSettlementPeriod () == null ||
              aUBLPaymentTerms.getSettlementPeriod ().getEndDate () == null)
          {
            aTransformationErrorList.addWarning ("PaymentTerms[" +
                                                 nPaymentTermsIndex +
                                                 "]/SettlementPeriod",
                                                 EText.SETTLEMENT_PERIOD_MISSING.getDisplayText (m_aDisplayLocale));
          }
          else
          {
            // Add notes
            for (final NoteType aUBLNote : aUBLPaymentTerms.getNote ())
            {
              final String sUBLNote = StringHelper.trim (aUBLNote.getValue ());
              if (StringHelper.hasText (sUBLNote))
                aPaymentConditionsNotes.add (sUBLNote);
            }

            final Ebi42DiscountType aEbiDiscount = new Ebi42DiscountType ();
            aEbiDiscount.setPaymentDate (aUBLPaymentTerms.getSettlementPeriod ().getEndDateValue ());
            aEbiDiscount.setPercentage (aUBLPaymentTerms.getSettlementDiscountPercentValue ());
            // Optional amount value
            aEbiDiscount.setAmount (aUBLPaymentTerms.getAmountValue ());
            aEbiPaymentConditions.getDiscount ().add (aEbiDiscount);
          }
        }
        else
          if (aUBLPaymentTerms.getPenaltySurchargePercent () != null)
          {
            aTransformationErrorList.addWarning ("PaymentTerms[" +
                                                 nPaymentTermsIndex +
                                                 "]",
                                                 EText.PENALTY_NOT_ALLOWED.getDisplayText (m_aDisplayLocale));
          }

        ++nPaymentTermsIndex;
      }

      if (!aPaymentConditionsNotes.isEmpty ())
        aEbiPaymentConditions.setComment (StringHelper.getImploded ('\n', aPaymentConditionsNotes));
    }

    if (aEbiPaymentConditions.getDueDate () == null)
    {
      // ebInterface requires due date
      if (aEbiPaymentConditions.hasDiscountEntries ())
        aTransformationErrorList.addError ("PaymentMeans/PaymentDueDate",
                                           EText.DISCOUNT_WITHOUT_DUEDATE.getDisplayTextWithArgs (m_aDisplayLocale));
    }
    else
    {
      // Independent if discounts are present or not
      aEbiDoc.setPaymentConditions (aEbiPaymentConditions);
    }
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
  public Ebi42InvoiceType convertToEbInterface (@Nonnull final InvoiceType aUBLDoc,
                                                @Nonnull final ErrorList aTransformationErrorList)
  {
    ValueEnforcer.notNull (aUBLDoc, "UBLInvoice");
    ValueEnforcer.notNull (aTransformationErrorList, "TransformationErrorList");
    if (!aTransformationErrorList.isEmpty ())
      throw new IllegalArgumentException ("TransformationErrorList must be empty!");

    // Consistency check before starting the conversion
    _checkConsistency (aUBLDoc, aTransformationErrorList);
    if (aTransformationErrorList.containsAtLeastOneError ())
      return null;

    // Build ebInterface invoice
    final Ebi42InvoiceType aEbiDoc = new Ebi42InvoiceType ();
    aEbiDoc.setGeneratingSystem (EBI_GENERATING_SYSTEM_42);
    aEbiDoc.setDocumentType (Ebi42DocumentTypeType.INVOICE);

    // Cannot set the language, because the 3letter code is expected but we only
    // have the 2letter code!

    final String sUBLCurrencyCode = StringHelper.trim (aUBLDoc.getDocumentCurrencyCodeValue ());
    aEbiDoc.setInvoiceCurrency (sUBLCurrencyCode);

    // Invoice Number
    final String sInvoiceNumber = StringHelper.trim (aUBLDoc.getIDValue ());
    if (StringHelper.hasNoText (sInvoiceNumber))
      aTransformationErrorList.addError ("ID", EText.MISSING_INVOICE_NUMBER.getDisplayText (m_aDisplayLocale));
    aEbiDoc.setInvoiceNumber (sInvoiceNumber);

    // Ignore the time!
    aEbiDoc.setInvoiceDate (aUBLDoc.getIssueDateValue ());
    if (aEbiDoc.getInvoiceDate () == null)
      aTransformationErrorList.addError ("IssueDate", EText.MISSING_INVOICE_DATE.getDisplayText (m_aDisplayLocale));

    // Is duplicate/copy indicator?
    if (aUBLDoc.getCopyIndicator () != null)
      aEbiDoc.setIsDuplicate (Boolean.valueOf (aUBLDoc.getCopyIndicator ().isValue ()));

    // CancelledOriginalDocument
    convertRelatedDocuments (aUBLDoc.getBillingReference (), aEbiDoc);

    // Global comment
    {
      final List <String> aEbiComment = new ArrayList <String> ();
      for (final NoteType aNote : aUBLDoc.getNote ())
        if (StringHelper.hasText (aNote.getValue ()))
          aEbiComment.add (aNote.getValue ());
      if (!aEbiComment.isEmpty ())
        aEbiDoc.setComment (StringHelper.getImplodedNonEmpty ('\n', aEbiComment));
    }

    // Biller/Supplier (creator of the invoice)
    {
      final SupplierPartyType aUBLSupplier = aUBLDoc.getAccountingSupplierParty ();
      final Ebi42BillerType aEbiBiller = new Ebi42BillerType ();
      // Find the tax scheme that uses VAT
      if (aUBLSupplier.getParty () != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLSupplier.getParty ().getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (SUPPORTED_TAX_SCHEME_ID.getID ().equals (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiBiller.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.hasNoText (aEbiBiller.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aEbiBiller.setVATIdentificationNumber ("ATU00000000");
        aTransformationErrorList.addWarning ("AccountingSupplierParty/Party/PartyTaxScheme",
                                             EText.BILLER_VAT_MISSING.getDisplayText (m_aDisplayLocale));
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
        if (m_bStrictERBMode && StringHelper.hasNoText (aEbiBiller.getInvoiceRecipientsBillerID ()))
        {
          // Mandatory field
          aTransformationErrorList.addError ("AccountingSupplierParty/CustomerAssignedAccountID",
                                             EText.ERB_CUSTOMER_ASSIGNED_ACCOUNTID_MISSING.getDisplayText (m_aDisplayLocale));
        }
      if (aUBLSupplier.getParty () != null)
      {
        aEbiBiller.setAddress (EbInterface42Helper.convertParty (aUBLSupplier.getParty (),
                                                                 "AccountingSupplierParty",
                                                                 aTransformationErrorList,
                                                                 m_aContentLocale,
                                                                 m_aDisplayLocale));

        // Ensure a fake biller email address is present
        if (StringHelper.hasNoText (aEbiBiller.getAddress ().getEmail ()))
          aEbiBiller.getAddress ().setEmail (PEPPOL_FAKE_BILLER_EMAIL_ADDRESS);
      }
      aEbiDoc.setBiller (aEbiBiller);
    }

    // Invoice recipient
    {
      final CustomerPartyType aUBLCustomer = aUBLDoc.getAccountingCustomerParty ();
      final Ebi42InvoiceRecipientType aEbiRecipient = new Ebi42InvoiceRecipientType ();
      // Find the tax scheme that uses VAT
      if (aUBLCustomer.getParty () != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLCustomer.getParty ().getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (SUPPORTED_TAX_SCHEME_ID.getID ().equals (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiRecipient.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.hasNoText (aEbiRecipient.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aEbiRecipient.setVATIdentificationNumber ("ATU00000000");
        aTransformationErrorList.addWarning ("AccountingCustomerParty/PartyTaxScheme",
                                             EText.SUPPLIER_VAT_MISSING.getDisplayText (m_aDisplayLocale));
      }
      if (aUBLCustomer.getSupplierAssignedAccountID () != null)
      {
        // UBL: An identifier for the Customer's account, assigned by the
        // Supplier.
        // eb: Identifikation des Rechnungsempfängers beim Rechnungssteller.
        aEbiRecipient.setBillersInvoiceRecipientID (StringHelper.trim (aUBLCustomer.getSupplierAssignedAccountIDValue ()));
      }
      // BillersInvoiceRecipientID is no longer mandatory in ebi

      if (aUBLCustomer.getParty () != null)
        aEbiRecipient.setAddress (EbInterface42Helper.convertParty (aUBLCustomer.getParty (),
                                                                    "AccountingCustomerParty",
                                                                    aTransformationErrorList,
                                                                    m_aContentLocale,
                                                                    m_aDisplayLocale));
      aEbiDoc.setInvoiceRecipient (aEbiRecipient);
    }

    // Ordering party
    final CustomerPartyType aUBLBuyer = aUBLDoc.getBuyerCustomerParty ();
    if (aUBLBuyer != null)
    {
      final Ebi42OrderingPartyType aEbiOrderingParty = new Ebi42OrderingPartyType ();
      // Find the tax scheme that uses VAT
      if (aUBLBuyer.getParty () != null)
        for (final PartyTaxSchemeType aUBLPartyTaxScheme : aUBLBuyer.getParty ().getPartyTaxScheme ())
        {
          // TaxScheme is a mandatory field
          if (SUPPORTED_TAX_SCHEME_ID.getID ().equals (aUBLPartyTaxScheme.getTaxScheme ().getIDValue ()))
          {
            aEbiOrderingParty.setVATIdentificationNumber (StringHelper.trim (aUBLPartyTaxScheme.getCompanyIDValue ()));
            break;
          }
        }
      if (StringHelper.hasNoText (aEbiOrderingParty.getVATIdentificationNumber ()))
      {
        // Required by ebInterface
        aTransformationErrorList.addError ("BuyerCustomerParty/PartyTaxScheme",
                                           EText.SUPPLIER_VAT_MISSING.getDisplayText (m_aDisplayLocale));
      }

      if (aUBLBuyer.getParty () != null)
        aEbiOrderingParty.setAddress (EbInterface42Helper.convertParty (aUBLBuyer.getParty (),
                                                                        "BuyerCustomerParty",
                                                                        aTransformationErrorList,
                                                                        m_aContentLocale,
                                                                        m_aDisplayLocale));
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
        aTransformationErrorList.addError ("OrderReference/ID",
                                           EText.ORDER_REFERENCE_MISSING.getDisplayText (m_aDisplayLocale));
      }
      else
      {
        if (sUBLOrderReferenceID != null && sUBLOrderReferenceID.length () > ORDER_REFERENCE_MAX_LENGTH)
        {
          aTransformationErrorList.addWarning ("OrderReference/ID",
                                               EText.ORDER_REFERENCE_TOO_LONG.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                      sUBLOrderReferenceID,
                                                                                                      Integer.valueOf (ORDER_REFERENCE_MAX_LENGTH)));
          sUBLOrderReferenceID = sUBLOrderReferenceID.substring (0, ORDER_REFERENCE_MAX_LENGTH);
        }
      }

      final Ebi42OrderReferenceType aEbiOrderReference = new Ebi42OrderReferenceType ();
      aEbiOrderReference.setOrderID (sUBLOrderReferenceID);
      aEbiDoc.getInvoiceRecipient ().setOrderReference (aEbiOrderReference);

      // Add contract reference as further identification
      for (final DocumentReferenceType aDocumentReference : aUBLDoc.getContractDocumentReference ())
        if (StringHelper.hasTextAfterTrim (aDocumentReference.getIDValue ()))
        {
          final String sKey = StringHelper.hasText (aDocumentReference.getID ().getSchemeID ())
                                                                                                ? aDocumentReference.getID ()
                                                                                                                    .getSchemeID ()
                                                                                                : "Contract";

          final Ebi42FurtherIdentificationType aEbiFurtherIdentification = new Ebi42FurtherIdentificationType ();
          aEbiFurtherIdentification.setIdentificationType (sKey);
          aEbiFurtherIdentification.setValue (StringHelper.trim (aDocumentReference.getIDValue ()));
          aEbiDoc.getInvoiceRecipient ().getFurtherIdentification ().add (aEbiFurtherIdentification);
        }
    }

    // Tax totals
    // Map from tax category to percentage
    final Map <TaxCategoryKey, BigDecimal> aTaxCategoryPercMap = new HashMap <TaxCategoryKey, BigDecimal> ();
    final Ebi42TaxType aEbiTax = new Ebi42TaxType ();
    final Ebi42VATType aEbiVAT = new Ebi42VATType ();
    {
      int nTaxTotalIndex = 0;
      for (final TaxTotalType aUBLTaxTotal : aUBLDoc.getTaxTotal ())
      {
        int nTaxSubtotalIndex = 0;
        for (final TaxSubtotalType aUBLSubtotal : aUBLTaxTotal.getTaxSubtotal ())
        {
          // Tax category is a mandatory element
          final TaxCategoryType aUBLTaxCategory = aUBLSubtotal.getTaxCategory ();
          BigDecimal aUBLTaxAmount = aUBLSubtotal.getTaxAmountValue ();
          BigDecimal aUBLTaxableAmount = aUBLSubtotal.getTaxableAmountValue ();

          // Is the percentage value directly specified
          BigDecimal aUBLPercentage = aUBLTaxCategory.getPercentValue ();
          if (aUBLPercentage == null)
          {
            // no it is not :(
            if (aUBLTaxAmount != null && aUBLTaxableAmount != null)
            {
              // Calculate percentage
              aUBLPercentage = MathHelper.isEqualToZero (aUBLTaxableAmount) ? BigDecimal.ZERO
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
              if (MathHelper.isNotEqualToZero (aUBLPercentage))
              {
                // Calculate (inexact) subtotal
                aUBLTaxableAmount = aUBLTaxAmount.multiply (CGlobal.BIGDEC_100).divide (aUBLPercentage,
                                                                                        SCALE_PRICE4,
                                                                                        ROUNDING_MODE);
              }
            }
            else
              if (aUBLTaxableAmount != null && aUBLTaxAmount == null)
              {
                // Calculate (inexact) subtotal
                aUBLTaxAmount = MathHelper.isEqualToZero (aUBLPercentage) ? BigDecimal.ZERO
                                                                          : aUBLTaxableAmount.multiply (aUBLPercentage)
                                                                                             .divide (CGlobal.BIGDEC_100,
                                                                                                      SCALE_PRICE4,
                                                                                                      ROUNDING_MODE);
              }
          }

          // Save item and put in map
          final String sUBLTaxSchemeSchemeID = StringHelper.trim (aUBLTaxCategory.getTaxScheme ()
                                                                                 .getID ()
                                                                                 .getSchemeID ());
          final String sUBLTaxSchemeID = StringHelper.trim (aUBLTaxCategory.getTaxScheme ().getIDValue ());

          if (aUBLTaxCategory.getID () == null)
          {
            aTransformationErrorList.addError ("TaxTotal[" +
                                               nTaxTotalIndex +
                                               "]/TaxSubtotal[" +
                                               nTaxSubtotalIndex +
                                               "]/TaxCategory",
                                               EText.MISSING_TAXCATEGORY_ID.getDisplayText (m_aDisplayLocale));
            break;
          }

          final String sUBLTaxCategorySchemeID = StringHelper.trim (aUBLTaxCategory.getID ().getSchemeID ());
          final String sUBLTaxCategoryID = StringHelper.trim (aUBLTaxCategory.getID ().getValue ());

          aTaxCategoryPercMap.put (new TaxCategoryKey (new SchemedID (sUBLTaxSchemeSchemeID, sUBLTaxSchemeID),
                                                       new SchemedID (sUBLTaxCategorySchemeID, sUBLTaxCategoryID)),
                                   aUBLPercentage);

          if (isSupportedTaxSchemeSchemeID (sUBLTaxSchemeSchemeID))
          {
            // Resolve the tax scheme ID
            final ETaxSchemeID eUBLTaxScheme = ETaxSchemeID.getFromIDOrNull (sUBLTaxSchemeID);
            if (eUBLTaxScheme == null)
            {
              aTransformationErrorList.addError ("TaxTotal[" +
                                                 nTaxTotalIndex +
                                                 "]/TaxSubtotal[" +
                                                 nTaxSubtotalIndex +
                                                 "]/TaxCategory/TaxScheme/ID",
                                                 EText.UNSUPPORTED_TAX_SCHEME_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                         sUBLTaxSchemeID));
            }
            else
            {
              if (SUPPORTED_TAX_SCHEME_ID.equals (eUBLTaxScheme))
              {
                if (aUBLPercentage == null)
                {
                  aTransformationErrorList.addError ("TaxTotal[" +
                                                     nTaxTotalIndex +
                                                     "]/TaxSubtotal[" +
                                                     nTaxSubtotalIndex +
                                                     "]/TaxCategory/Percent",
                                                     EText.TAX_PERCENT_MISSING.getDisplayTextWithArgs (m_aDisplayLocale));
                }
                else
                  if (aUBLTaxableAmount == null)
                  {
                    aTransformationErrorList.addError ("TaxTotal[" +
                                                       nTaxTotalIndex +
                                                       "]/TaxSubtotal[" +
                                                       nTaxSubtotalIndex +
                                                       "]/TaxableAmount",
                                                       EText.TAXABLE_AMOUNT_MISSING.getDisplayText (m_aDisplayLocale));
                  }
                  else
                  {
                    // add VAT item
                    final Ebi42VATItemType aEbiVATItem = new Ebi42VATItemType ();
                    // Base amount
                    aEbiVATItem.setTaxedAmount (aUBLTaxableAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));
                    // tax rate
                    final Ebi42VATRateType aEbiVATVATRate = new Ebi42VATRateType ();
                    // Optional
                    if (false)
                      aEbiVATVATRate.setTaxCode (sUBLTaxCategoryID);
                    aEbiVATVATRate.setValue (aUBLPercentage);
                    aEbiVATItem.setVATRate (aEbiVATVATRate);
                    // Tax amount (mandatory)
                    aEbiVATItem.setAmount (aUBLTaxAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));
                    // Add to list
                    aEbiVAT.getVATItem ().add (aEbiVATItem);
                  }
              }
              else
              {
                // Other TAX
                final Ebi42OtherTaxType aOtherTax = new Ebi42OtherTaxType ();
                // As no comment is present, use the scheme ID
                aOtherTax.setComment (sUBLTaxSchemeID);
                // Tax amount (mandatory)
                aOtherTax.setAmount (aUBLTaxAmount.setScale (SCALE_PRICE2, ROUNDING_MODE));
                aEbiTax.getOtherTax ().add (aOtherTax);
              }
            }
          }
          else
          {
            aTransformationErrorList.addError ("TaxTotal[" +
                                               nTaxTotalIndex +
                                               "]/TaxSubtotal[" +
                                               nTaxSubtotalIndex +
                                               "]/TaxCategory/",
                                               EText.UNSUPPORTED_TAX_SCHEME.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                    sUBLTaxSchemeSchemeID,
                                                                                                    sUBLTaxSchemeID));
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
      final Ebi42DetailsType aEbiDetails = new Ebi42DetailsType ();
      final Ebi42ItemListType aEbiItemList = new Ebi42ItemListType ();
      int nLineIndex = 0;
      for (final InvoiceLineType aUBLLine : aUBLDoc.getInvoiceLine ())
      {
        // Try to resolve tax category
        TaxCategoryType aUBLTaxCategory = CollectionHelper.getSafe (aUBLLine.getItem ().getClassifiedTaxCategory (), 0);
        if (aUBLTaxCategory == null)
        {
          // No direct tax category -> check if it is somewhere in the tax total
          outer: for (final TaxTotalType aUBLTaxTotal : aUBLLine.getTaxTotal ())
            for (final TaxSubtotalType aUBLTaxSubTotal : aUBLTaxTotal.getTaxSubtotal ())
            {
              // Only handle VAT items
              if (SUPPORTED_TAX_SCHEME_ID.getID ()
                                         .equals (aUBLTaxSubTotal.getTaxCategory ().getTaxScheme ().getIDValue ()))
              {
                // We found one -> just use it
                aUBLTaxCategory = aUBLTaxSubTotal.getTaxCategory ();
                break outer;
              }
            }
        }

        // Try to resolve tax percentage
        BigDecimal aUBLPercent = null;
        if (aUBLTaxCategory != null)
        {
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
            final String sUBLTaxCategoryID = StringHelper.trim (aUBLTaxCategory.getIDValue ());

            final TaxCategoryKey aKey = new TaxCategoryKey (new SchemedID (sUBLTaxSchemeSchemeID, sUBLTaxSchemeID),
                                                            new SchemedID (sUBLTaxCategorySchemeID, sUBLTaxCategoryID));
            aUBLPercent = aTaxCategoryPercMap.get (aKey);
          }
        }
        if (aUBLPercent == null)
        {
          aUBLPercent = BigDecimal.ZERO;
          aTransformationErrorList.addWarning ("InvoiceLine[" +
                                               nLineIndex +
                                               "]/Item/ClassifiedTaxCategory",
                                               EText.DETAILS_TAX_PERCENTAGE_NOT_FOUND.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                              aUBLPercent));
        }

        // Start creating ebInterface line
        final Ebi42ListLineItemType aEbiListLineItem = new Ebi42ListLineItemType ();

        // Invoice line number
        final String sUBLPositionNumber = StringHelper.trim (aUBLLine.getIDValue ());
        BigInteger aUBLPositionNumber = StringParser.parseBigInteger (sUBLPositionNumber);
        if (aUBLPositionNumber == null)
        {
          aUBLPositionNumber = BigInteger.valueOf (nLineIndex + 1);
          aTransformationErrorList.addWarning ("InvoiceLine[" +
                                               nLineIndex +
                                               "]/ID",
                                               EText.DETAILS_INVALID_POSITION.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                      sUBLPositionNumber,
                                                                                                      aUBLPositionNumber));
        }
        aEbiListLineItem.setPositionNumber (aUBLPositionNumber);

        // Descriptions
        for (final DescriptionType aUBLDescription : aUBLLine.getItem ().getDescription ())
          aEbiListLineItem.getDescription ().add (StringHelper.trim (aUBLDescription.getValue ()));
        if (aEbiListLineItem.getDescription ().isEmpty ())
        {
          // Use item name as description
          final NameType aUBLName = aUBLLine.getItem ().getName ();
          if (aUBLName != null)
            aEbiListLineItem.getDescription ().add (StringHelper.trim (aUBLName.getValue ()));
        }

        // Quantity
        final Ebi42UnitType aEbiQuantity = new Ebi42UnitType ();
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
          aEbiQuantity.setUnit (EUnitOfMeasureCode21.C62.getID ());
          aTransformationErrorList.addWarning ("InvoiceLine[" +
                                               nLineIndex +
                                               "]/InvoicedQuantity/UnitCode",
                                               EText.DETAILS_INVALID_UNIT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                  aEbiQuantity.getUnit ()));
        }
        if (aEbiQuantity.getValue () == null)
        {
          aEbiQuantity.setValue (BigDecimal.ONE);
          aTransformationErrorList.addWarning ("InvoiceLine[" +
                                               nLineIndex +
                                               "]/InvoicedQuantity",
                                               EText.DETAILS_INVALID_QUANTITY.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                      aEbiQuantity.getValue ()));
        }
        aEbiListLineItem.setQuantity (aEbiQuantity);

        // Unit price
        if (aUBLLine.getPrice () != null)
        {
          final Ebi42UnitPriceType aEbiUnitPrice = new Ebi42UnitPriceType ();
          // Unit price = priceAmount/baseQuantity (mandatory)
          final BigDecimal aUBLPriceAmount = aUBLLine.getPrice ().getPriceAmountValue ();
          aEbiUnitPrice.setValue (aUBLPriceAmount);
          // If no base quantity is present, assume 1 (optional)
          final BigDecimal aUBLBaseQuantity = aUBLLine.getPrice ().getBaseQuantityValue ();
          if (aUBLBaseQuantity != null)
          {
            aEbiUnitPrice.setBaseQuantity (aUBLBaseQuantity);
            if (MathHelper.isEqualToZero (aUBLBaseQuantity))
              aEbiUnitPrice.setValue (BigDecimal.ZERO);
          }
          aEbiListLineItem.setUnitPrice (aEbiUnitPrice);
        }
        else
        {
          // Unit price = lineExtensionAmount / quantity (mandatory)
          final BigDecimal aUBLLineExtensionAmount = aUBLLine.getLineExtensionAmountValue ();
          final Ebi42UnitPriceType aEbiUnitPrice = new Ebi42UnitPriceType ();
          if (MathHelper.isEqualToZero (aEbiQuantity.getValue ()))
            aEbiUnitPrice.setValue (BigDecimal.ZERO);
          else
            aEbiUnitPrice.setValue (aUBLLineExtensionAmount.divide (aEbiQuantity.getValue (),
                                                                    SCALE_PRICE4,
                                                                    ROUNDING_MODE));
          aEbiListLineItem.setUnitPrice (aEbiUnitPrice);
        }

        BigDecimal aEbiUnitPriceValue = aEbiListLineItem.getUnitPrice ().getValue ();
        if (aEbiListLineItem.getUnitPrice ().getBaseQuantity () != null)
          aEbiUnitPriceValue = aEbiUnitPriceValue.divide (aEbiListLineItem.getUnitPrice ().getBaseQuantity (),
                                                          SCALE_PRICE4,
                                                          ROUNDING_MODE);

        // Tax rate (mandatory)
        final Ebi42VATRateType aEbiVATRate = new Ebi42VATRateType ();
        aEbiVATRate.setValue (aUBLPercent);
        if (aUBLTaxCategory != null)
          // Optional
          if (false)
            aEbiVATRate.setTaxCode (aUBLTaxCategory.getIDValue ());
        aEbiListLineItem.setVATRate (aEbiVATRate);

        // Line item amount (quantity * unit price +- reduction / surcharge)
        aEbiListLineItem.setLineItemAmount (aUBLLine.getLineExtensionAmountValue ().setScale (SCALE_PRICE2,
                                                                                              ROUNDING_MODE));

        // Special handling in case no VAT item is present
        if (MathHelper.isEqualToZero (aUBLPercent))
          aTotalZeroPercLineExtensionAmount = aTotalZeroPercLineExtensionAmount.add (aEbiListLineItem.getLineItemAmount ());

        // Order reference per line
        for (final OrderLineReferenceType aUBLOrderLineReference : aUBLLine.getOrderLineReference ())
          if (StringHelper.hasText (aUBLOrderLineReference.getLineIDValue ()))
          {
            final Ebi42OrderReferenceDetailType aEbiOrderRefDetail = new Ebi42OrderReferenceDetailType ();

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
                aTransformationErrorList.addError ("InvoiceLine[" +
                                                   nLineIndex +
                                                   "]/OrderLineReference/LineID",
                                                   EText.ORDERLINE_REF_ID_EMPTY.getDisplayText (m_aDisplayLocale));
              }
              else
              {
                aEbiOrderRefDetail.setOrderPositionNumber (sOrderPosNumber);
              }
            }
            aEbiListLineItem.setInvoiceRecipientsOrderReference (aEbiOrderRefDetail);
            break;
          }

        // Reduction and surcharge
        if (aUBLLine.hasAllowanceChargeEntries ())
        {
          // Start with quantity*unitPrice for base amount
          BigDecimal aEbiBaseAmount = aEbiListLineItem.getQuantity ().getValue ().multiply (aEbiUnitPriceValue);
          final Ebi42ReductionAndSurchargeListLineItemDetailsType aEbiRSDetails = new Ebi42ReductionAndSurchargeListLineItemDetailsType ();

          // ebInterface can handle only Reduction or only Surcharge
          ETriState eSurcharge = ETriState.UNDEFINED;
          for (final AllowanceChargeType aUBLAllowanceCharge : aUBLLine.getAllowanceCharge ())
          {
            final boolean bItemIsSurcharge = aUBLAllowanceCharge.getChargeIndicator ().isValue ();

            // Remember for next item
            if (eSurcharge.isUndefined ())
              eSurcharge = ETriState.valueOf (bItemIsSurcharge);
            final boolean bSwapSigns = bItemIsSurcharge != eSurcharge.isTrue ();

            final Ebi42ReductionAndSurchargeBaseType aEbiRSItem = new Ebi42ReductionAndSurchargeBaseType ();
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
              aEbiRSItem.setPercentage (bSwapSigns ? aPerc.negate () : aPerc);
            }

            if (eSurcharge.isTrue ())
            {
              aEbiRSDetails.getReductionListLineItemOrSurchargeListLineItemOrOtherVATableTaxListLineItem ()
                           .add (new ObjectFactory ().createSurchargeListLineItem (aEbiRSItem));
              aEbiBaseAmount = aEbiBaseAmount.add (aEbiRSItem.getAmount ());
            }
            else
            {
              aEbiRSDetails.getReductionListLineItemOrSurchargeListLineItemOrOtherVATableTaxListLineItem ()
                           .add (new ObjectFactory ().createReductionListLineItem (aEbiRSItem));
              aEbiBaseAmount = aEbiBaseAmount.subtract (aEbiRSItem.getAmount ());
            }

            aEbiRSItem.setComment (getAllowanceChargeComment (aUBLAllowanceCharge));
          }
          aEbiListLineItem.setReductionAndSurchargeListLineItemDetails (aEbiRSDetails);
        }

        // Delivery per line item
        if (aUBLLine.getDeliveryCount () > 0)
        {
          // Delivery address
          final int nDeliveryIndex = 0;
          final DeliveryType aUBLDelivery = aUBLLine.getDeliveryAtIndex (0);

          if (aUBLDelivery.getActualDeliveryDate () != null)
          {
            final Ebi42DeliveryType aEbiDelivery = EbInterface42Helper.convertDelivery (aUBLDelivery,
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

        // Add the item to the list
        aEbiItemList.getListLineItem ().add (aEbiListLineItem);
        nLineIndex++;
      }
      aEbiDetails.getItemList ().add (aEbiItemList);
      aEbiDoc.setDetails (aEbiDetails);
    }

    if (aEbiVAT.hasNoVATItemEntries ())
    {
      aTransformationErrorList.addError ("InvoiceLine", EText.VAT_ITEM_MISSING.getDisplayText (m_aDisplayLocale));
      if (false)
      {
        // No default in this case
        final Ebi42VATItemType aEbiVATItem = new Ebi42VATItemType ();
        aEbiVATItem.setTaxedAmount (aTotalZeroPercLineExtensionAmount);
        final Ebi42VATRateType aEbiVATVATRate = new Ebi42VATRateType ();
        aEbiVATVATRate.setValue (BigDecimal.ZERO);
        aEbiVATItem.setVATRate (aEbiVATVATRate);
        aEbiVATItem.setAmount (aTotalZeroPercLineExtensionAmount);
        aEbiVAT.getVATItem ().add (aEbiVATItem);
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
        for (final Ebi42ItemListType aEbiItemList : aEbiDoc.getDetails ().getItemList ())
          for (final Ebi42ListLineItemType aEbiListLineItem : aEbiItemList.getListLineItem ())
            tmp = tmp.add (aEbiListLineItem.getLineItemAmount ());
        aEbiBaseAmount = tmp;
      }
      final Ebi42ReductionAndSurchargeDetailsType aEbiRS = new Ebi42ReductionAndSurchargeDetailsType ();

      int nAllowanceChargeIndex = 0;
      for (final AllowanceChargeType aUBLAllowanceCharge : aUBLDoc.getAllowanceCharge ())
      {
        final boolean bItemIsSurcharge = aUBLAllowanceCharge.getChargeIndicator ().isValue ();

        final Ebi42ReductionAndSurchargeType aEbiRSItem = new Ebi42ReductionAndSurchargeType ();
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

        Ebi42VATRateType aEbiVATRate = null;
        for (final TaxCategoryType aUBLTaxCategory : aUBLAllowanceCharge.getTaxCategory ())
          if (aUBLTaxCategory.getPercent () != null)
          {
            aEbiVATRate = new Ebi42VATRateType ();
            aEbiVATRate.setValue (aUBLTaxCategory.getPercentValue ());
            if (false)
              aEbiVATRate.setTaxCode (aUBLTaxCategory.getIDValue ());
            break;
          }
        if (aEbiVATRate == null)
        {
          aTransformationErrorList.addError ("Invoice/AllowanceCharge[" +
                                             nAllowanceChargeIndex +
                                             "]",
                                             EText.ALLOWANCE_CHARGE_NO_TAXRATE.getDisplayText (m_aDisplayLocale));
          // No default in this case
          if (false)
          {
            aEbiVATRate = new Ebi42VATRateType ();
            aEbiVATRate.setValue (BigDecimal.ZERO);
            aEbiVATRate.setTaxCode (ETaxCode.NOT_TAXABLE.getID ());
          }
        }
        aEbiRSItem.setVATRate (aEbiVATRate);

        if (bItemIsSurcharge)
        {
          aEbiRS.getReductionOrSurchargeOrOtherVATableTax ().add (new ObjectFactory ().createSurcharge (aEbiRSItem));
          aEbiBaseAmount = aEbiBaseAmount.add (aEbiRSItem.getAmount ());
        }
        else
        {
          aEbiRS.getReductionOrSurchargeOrOtherVATableTax ().add (new ObjectFactory ().createReduction (aEbiRSItem));
          aEbiBaseAmount = aEbiBaseAmount.subtract (aEbiRSItem.getAmount ());
        }
        aEbiDoc.setReductionAndSurchargeDetails (aEbiRS);
        ++nAllowanceChargeIndex;
      }
    }

    // PrepaidAmount is not supported!
    final MonetaryTotalType aUBLMonetaryTotal = aUBLDoc.getLegalMonetaryTotal ();
    if (aUBLMonetaryTotal.getPrepaidAmount () != null &&
        !MathHelper.isEqualToZero (aUBLMonetaryTotal.getPrepaidAmountValue ()))
    {
      aTransformationErrorList.addError ("Invoice/LegalMonetaryTotal/PrepaidAmount",
                                         EText.PREPAID_NOT_SUPPORTED.getDisplayText (m_aDisplayLocale));
    }

    // Total gross amount
    if (aUBLMonetaryTotal.getTaxInclusiveAmountValue () != null)
      aEbiDoc.setTotalGrossAmount (aUBLMonetaryTotal.getTaxInclusiveAmountValue ().setScale (SCALE_PRICE2,
                                                                                             ROUNDING_MODE));
    else
      aEbiDoc.setTotalGrossAmount (aUBLMonetaryTotal.getPayableAmountValue ().setScale (SCALE_PRICE2, ROUNDING_MODE));
    // Payable amount
    aEbiDoc.setPayableAmount (aUBLMonetaryTotal.getPayableAmountValue ().setScale (SCALE_PRICE2, ROUNDING_MODE));

    // Payment method
    _convertPayment (aUBLDoc, aTransformationErrorList, aEbiDoc);

    // Delivery
    Ebi42DeliveryType aEbiDelivery = null;
    {
      // Delivery address
      int nDeliveryIndex = 0;
      for (final DeliveryType aUBLDelivery : aUBLDoc.getDelivery ())
      {
        // Use the first delivery with a delivery date
        if (aUBLDelivery.getActualDeliveryDate () != null)
        {
          aEbiDelivery = EbInterface42Helper.convertDelivery (aUBLDelivery,
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
        aEbiDelivery = new Ebi42DeliveryType ();

      if (aEbiDelivery.getDate () == null)
      {
        // No delivery date is present - check for service period
        final PeriodType aUBLInvoicePeriod = CollectionHelper.getSafe (aUBLDoc.getInvoicePeriod (), 0);
        if (aUBLInvoicePeriod != null)
        {
          final XMLGregorianCalendar aStartDate = aUBLInvoicePeriod.getStartDateValue ();
          final XMLGregorianCalendar aEndDate = aUBLInvoicePeriod.getEndDateValue ();
          if (aStartDate != null)
          {
            if (aEndDate == null)
            {
              // It's just a date
              aEbiDelivery.setDate (aStartDate);
            }
            else
            {
              // It's a period!
              final Ebi42PeriodType aEbiPeriod = new Ebi42PeriodType ();
              aEbiPeriod.setFromDate (aStartDate);
              aEbiPeriod.setToDate (aEndDate);
              aEbiDelivery.setPeriod (aEbiPeriod);
            }
          }
        }
      }
    }

    if (m_bStrictERBMode)
    {
      if (aEbiDelivery.getDate () == null && aEbiDelivery.getPeriod () == null)
        aTransformationErrorList.addError ("Invoice", EText.ERB_NO_DELIVERY_DATE.getDisplayText (m_aDisplayLocale));
    }

    if (aEbiDelivery.getDate () != null || aEbiDelivery.getPeriod () != null)
      aEbiDoc.setDelivery (aEbiDelivery);

    return aEbiDoc;
  }
}
