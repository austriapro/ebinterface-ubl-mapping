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
package com.helger.ebinterface.ubl.from;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.datatype.XMLGregorianCalendar;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.math.MathHelper;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v41.Ebi41AccountType;
import com.helger.ebinterface.v41.Ebi41DirectDebitType;
import com.helger.ebinterface.v41.Ebi41DiscountType;
import com.helger.ebinterface.v41.Ebi41DocumentTypeType;
import com.helger.ebinterface.v41.Ebi41InvoiceType;
import com.helger.ebinterface.v41.Ebi41NoPaymentType;
import com.helger.ebinterface.v41.Ebi41PaymentConditionsType;
import com.helger.ebinterface.v41.Ebi41PaymentMethodType;
import com.helger.ebinterface.v41.Ebi41PaymentReferenceType;
import com.helger.ebinterface.v41.Ebi41RelatedDocumentType;
import com.helger.ebinterface.v41.Ebi41SEPADirectDebitType;
import com.helger.ebinterface.v41.Ebi41SEPADirectDebitTypeType;
import com.helger.ebinterface.v41.Ebi41UniversalBankTransactionType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BillingReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialAccountType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialInstitutionType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentMeansType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentTermsType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentDescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionNoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.PaymentIDType;

/**
 * Base class for PEPPOL UBL to ebInterface 4.1 converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractToEbInterface41Converter extends AbstractToEbInterfaceConverter
{
  public static final int PAYMENT_REFERENCE_MAX_LENGTH = 35;

  public AbstractToEbInterface41Converter (@Nonnull final Locale aDisplayLocale,
                                           @Nonnull final Locale aContentLocale,
                                           @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale, aSettings);
  }

  @Nullable
  protected static final Ebi41DocumentTypeType getAsDocumentTypeType (@Nullable final String... aValues)
  {
    if (aValues != null)
      for (final String s : aValues)
        if (s != null)
          try
          {
            // The first match wins
            return Ebi41DocumentTypeType.fromValue (s);
          }
          catch (final IllegalArgumentException ex)
          {
            // Ignore
          }
    return null;
  }

  protected static void convertRelatedDocuments (@Nonnull final List <BillingReferenceType> aUBLBillingReferences,
                                                 @Nonnull final Ebi41InvoiceType aEbiDoc)
  {
    for (final BillingReferenceType aUBLBillingReference : aUBLBillingReferences)
    {
      if (aUBLBillingReference.getInvoiceDocumentReference () != null &&
          aUBLBillingReference.getInvoiceDocumentReference ().getIDValue () != null)
      {
        final Ebi41RelatedDocumentType aEbiRelatedDocument = new Ebi41RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getInvoiceDocumentReference ().getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getInvoiceDocumentReference ().getIssueDateValue ());
        aEbiRelatedDocument.setDocumentType (Ebi41DocumentTypeType.INVOICE);
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
      else
        if (aUBLBillingReference.getCreditNoteDocumentReference () != null &&
            aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue () != null)
        {
          final Ebi41RelatedDocumentType aEbiRelatedDocument = new Ebi41RelatedDocumentType ();
          aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue ());
          aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getCreditNoteDocumentReference ()
                                                                  .getIssueDateValue ());
          aEbiRelatedDocument.setDocumentType (Ebi41DocumentTypeType.CREDIT_MEMO);
          aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
        }
      // Ignore other values
    }
  }

  protected static void convertReferencedDocuments (@Nonnull final List <DocumentReferenceType> aUBLDocumentReferences,
                                                    @Nonnull final Ebi41InvoiceType aEbiDoc)
  {
    for (final DocumentReferenceType aUBLDocumentReference : aUBLDocumentReferences)
      if (StringHelper.hasText (aUBLDocumentReference.getIDValue ()) && aUBLDocumentReference.getAttachment () == null)
      {
        final Ebi41RelatedDocumentType aEbiRelatedDocument = new Ebi41RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLDocumentReference.getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLDocumentReference.getIssueDateValue ());
        final ICommonsList <String> aComments = new CommonsArrayList <> ();
        for (final DocumentDescriptionType aUBLDocDesc : aUBLDocumentReference.getDocumentDescription ())
          aComments.add (aUBLDocDesc.getValue ());
        aEbiRelatedDocument.setComment (StringHelper.getImplodedNonEmpty ('\n', aComments));
        if (aUBLDocumentReference.getDocumentTypeCode () != null)
        {
          aEbiRelatedDocument.setDocumentType (getAsDocumentTypeType (aUBLDocumentReference.getDocumentTypeCode ()
                                                                                           .getName (),
                                                                      aUBLDocumentReference.getDocumentTypeCodeValue ()));
        }
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
  }

  private static void _setPaymentMeansComment (@Nonnull final PaymentMeansType aUBLPaymentMeans,
                                               @Nonnull final Ebi41PaymentMethodType aEbiPaymentMethod)
  {
    if (aUBLPaymentMeans.hasInstructionNoteEntries ())
    {
      final ICommonsList <String> aNotes = new CommonsArrayList <> ();
      for (final InstructionNoteType aUBLNote : aUBLPaymentMeans.getInstructionNote ())
        aNotes.add (StringHelper.trim (aUBLNote.getValue ()));
      if (aNotes.isNotEmpty ())
        aEbiPaymentMethod.setComment (StringHelper.getImplodedNonEmpty ('\n', aNotes));
    }
  }

  protected void convertPayment (@Nonnull final Supplier <List <PaymentMeansType>> aUBLDocPaymentMeans,
                                 @Nonnull final Supplier <PartyType> aUBLDocPayeeParty,
                                 @Nonnull final Supplier <SupplierPartyType> aUBLDocAccountingSupplierParty,
                                 @Nonnull final Supplier <List <PaymentTermsType>> aUBLDocPaymentTerms,
                                 @Nonnull final Supplier <MonetaryTotalType> aUBLDocLegalMonetaryTotal,
                                 @Nonnull final ErrorList aTransformationErrorList,
                                 @Nonnull final Ebi41InvoiceType aEbiDoc,
                                 final boolean bIsCreditNote)
  {
    final Ebi41PaymentMethodType aEbiPaymentMethod = new Ebi41PaymentMethodType ();
    final Ebi41PaymentConditionsType aEbiPaymentConditions = new Ebi41PaymentConditionsType ();

    {
      int nPaymentMeansIndex = 0;
      for (final PaymentMeansType aUBLPaymentMeans : aUBLDocPaymentMeans.get ())
      {
        final String sPaymentMeansCode = StringHelper.trim (aUBLPaymentMeans.getPaymentMeansCodeValue ());
        if (isUniversalBankTransaction (sPaymentMeansCode))
        {
          // Is a payment channel code present?
          final String sPaymentChannelCode = StringHelper.trim (aUBLPaymentMeans.getPaymentChannelCodeValue ());
          // null/empty for standard PEPPOL BIS
          if (StringHelper.hasNoText (sPaymentChannelCode) || PAYMENT_CHANNEL_CODE_IBAN.equals (sPaymentChannelCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi41UniversalBankTransactionType aEbiUBTMethod = new Ebi41UniversalBankTransactionType ();

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
                  aTransformationErrorList.add (SingleError.builderWarn ()
                                                           .setErrorFieldName ("PaymentMeans[" +
                                                                               nPaymentMeansIndex +
                                                                               "]/PaymentID[" +
                                                                               nPaymentIDIndex +
                                                                               "]")
                                                           .setErrorText (EText.PAYMENT_ID_TOO_LONG_CUT.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                                sUBLPaymentID))
                                                           .build ());
                  sUBLPaymentID = sUBLPaymentID.substring (0, PAYMENT_REFERENCE_MAX_LENGTH);
                }

                final Ebi41PaymentReferenceType aEbiPaymentReference = new Ebi41PaymentReferenceType ();
                aEbiPaymentReference.setValue (sUBLPaymentID);
                aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
              }
              ++nPaymentIDIndex;
            }

            // Beneficiary account
            final Ebi41AccountType aEbiAccount = new Ebi41AccountType ();

            // BIC
            final FinancialAccountType aUBLFinancialAccount = aUBLPaymentMeans.getPayeeFinancialAccount ();
            if (aUBLFinancialAccount != null &&
                aUBLFinancialAccount.getFinancialInstitutionBranch () != null &&
                aUBLFinancialAccount.getFinancialInstitutionBranch ().getFinancialInstitution () != null)
            {
              final FinancialInstitutionType aUBLFI = aUBLFinancialAccount.getFinancialInstitutionBranch ()
                                                                          .getFinancialInstitution ();
              if (aUBLFI.getID () != null)
              {
                final String sID = StringHelper.trim (aUBLFI.getID ().getValue ());
                final String sScheme = StringHelper.trim (aUBLFI.getID ().getSchemeID ());
                final boolean bIsBIC = SCHEME_BIC.equalsIgnoreCase (sScheme) || StringHelper.hasNoText (sScheme);

                if (bIsBIC)
                  aEbiAccount.setBIC (sID);
                else
                  aEbiAccount.setBankName (sID);

                if (bIsBIC)
                  if (StringHelper.hasNoText (sID) || !RegExHelper.stringMatchesPattern (REGEX_BIC, sID))
                  {
                    aTransformationErrorList.add (SingleError.builderError ()
                                                             .setErrorFieldName ("PaymentMeans[" +
                                                                                 nPaymentMeansIndex +
                                                                                 "]/PayeeFinancialAccount/FinancialInstitutionBranch/FinancialInstitution/ID")
                                                             .setErrorText (EText.BIC_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                      sID))
                                                             .build ());
                    aEbiAccount.setBIC (null);
                  }
              }
            }

            // IBAN
            final String sIBAN = aUBLFinancialAccount != null ? StringHelper.trim (aUBLFinancialAccount.getIDValue ())
                                                              : null;
            aEbiAccount.setIBAN (sIBAN);
            if (StringHelper.getLength (sIBAN) > IBAN_MAX_LENGTH)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .setErrorFieldName ("PaymentMeans[" +
                                                                           nPaymentMeansIndex +
                                                                           "]/PayeeFinancialAccount/ID")
                                                       .setErrorText (EText.IBAN_TOO_LONG.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                  sIBAN,
                                                                                                                  Integer.valueOf (IBAN_MAX_LENGTH)))
                                                       .build ());
              aEbiAccount.setIBAN (sIBAN.substring (0, IBAN_MAX_LENGTH));
            }

            // Bank Account Owner - no field present - check PayeePart or
            // SupplierPartyName
            String sBankAccountOwnerName = aUBLFinancialAccount != null ? aUBLFinancialAccount.getNameValue () : null;
            if (StringHelper.hasNoText (sBankAccountOwnerName))
            {
              final PartyType aUBLPayeeParty = aUBLDocPayeeParty.get ();
              if (aUBLPayeeParty != null)
                for (final PartyNameType aPartyName : aUBLPayeeParty.getPartyName ())
                {
                  sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                  if (StringHelper.hasText (sBankAccountOwnerName))
                    break;
                }
            }
            if (StringHelper.hasNoText (sBankAccountOwnerName))
            {
              final PartyType aSupplierParty = aUBLDocAccountingSupplierParty.get ().getParty ();
              if (aSupplierParty != null)
                for (final PartyNameType aPartyName : aSupplierParty.getPartyName ())
                {
                  sBankAccountOwnerName = StringHelper.trim (aPartyName.getNameValue ());
                  if (StringHelper.hasText (sBankAccountOwnerName))
                    break;
                }
            }
            aEbiAccount.setBankAccountOwner (sBankAccountOwnerName);

            aEbiUBTMethod.addBeneficiaryAccount (aEbiAccount);
            aEbiPaymentMethod.setUniversalBankTransaction (aEbiUBTMethod);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

            break;
          }

          aTransformationErrorList.add (SingleError.builderWarn ()
                                                   .setErrorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]")
                                                   .setErrorText (EText.PAYMENTMEANS_UNSUPPORTED_CHANNELCODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                                     sPaymentChannelCode))
                                                   .build ());
        }
        else
          if (isDirectDebit (sPaymentMeansCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi41DirectDebitType aEbiDirectDebit = new Ebi41DirectDebitType ();
            aEbiPaymentMethod.setDirectDebit (aEbiDirectDebit);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

            break;
          }
          else
            if (isSEPADirectDebit (sPaymentMeansCode))
            {
              _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
              // TODO use SEPA fields
              if (true)
              {
                final Ebi41DirectDebitType aEbiDirectDebit = new Ebi41DirectDebitType ();
                aEbiPaymentMethod.setDirectDebit (aEbiDirectDebit);
                aEbiDoc.setPaymentMethod (aEbiPaymentMethod);
              }
              else
              {
                final Ebi41SEPADirectDebitType aEbiDirectDebit = new Ebi41SEPADirectDebitType ();
                aEbiDirectDebit.setType (Ebi41SEPADirectDebitTypeType.B_2_C);
                aEbiPaymentMethod.setSEPADirectDebit (aEbiDirectDebit);
                aEbiDoc.setPaymentMethod (aEbiPaymentMethod);
              }

              // Set due date (optional)
              aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

              break;
            }
            else
            {
              // No supported payment means code
              if (MathHelper.isEQ0 (aEbiDoc.getPayableAmount ()))
              {
                // As nothing is to be paid we can safely use NoPayment
                _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
                final Ebi41NoPaymentType aEbiNoPayment = new Ebi41NoPaymentType ();
                aEbiPaymentMethod.setNoPayment (aEbiNoPayment);
                break;
              }

              aTransformationErrorList.add (SingleError.builderError ()
                                                       .setErrorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]")
                                                       .setErrorText (EText.PAYMENTMEANS_CODE_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                              sPaymentMeansCode,
                                                                                                                              getOrString (", ",
                                                                                                                                           "30",
                                                                                                                                           "31",
                                                                                                                                           "41",
                                                                                                                                           "58"),
                                                                                                                              getOrString (", ",
                                                                                                                                           "49",
                                                                                                                                           "59")))
                                                       .build ());
            }

        ++nPaymentMeansIndex;
      }
    }

    if (aEbiDoc.getPaymentMethod () == null)
    {
      // No payment method found
      if (m_aSettings.isInvoicePaymentMethodMandatory ())
      {
        if (bIsCreditNote)
        {
          // Create a no-payment as fallback
          final Ebi41NoPaymentType aEbiNoPayment = new Ebi41NoPaymentType ();
          aEbiPaymentMethod.setNoPayment (aEbiNoPayment);
          aEbiDoc.setPaymentMethod (aEbiPaymentMethod);
        }
        else
        {
          aTransformationErrorList.add (SingleError.builderError ()
                                                   .setErrorFieldName (bIsCreditNote ? "CreditNote" : "Invoice")
                                                   .setErrorText (EText.ERB_NO_PAYMENT_METHOD.getDisplayText (m_aDisplayLocale))
                                                   .build ());
        }
      }
    }

    // Payment terms
    {
      final ICommonsList <String> aPaymentConditionsNotes = new CommonsArrayList <> ();
      int nPaymentTermsIndex = 0;
      for (final PaymentTermsType aUBLPaymentTerms : aUBLDocPaymentTerms.get ())
      {
        // Add notes
        for (final NoteType aUBLNote : aUBLPaymentTerms.getNote ())
        {
          final String sUBLNote = StringHelper.trim (aUBLNote.getValue ());
          if (StringHelper.hasText (sUBLNote))
            aPaymentConditionsNotes.add (sUBLNote);
        }

        if (aUBLPaymentTerms.getPaymentDueDate () != null)
        {
          final XMLGregorianCalendar aUBLDueDate = aUBLPaymentTerms.getPaymentDueDateValue ();
          final XMLGregorianCalendar aEbiDueDate = aEbiPaymentConditions.getDueDate ();
          if (aUBLDueDate != null && aEbiDueDate != null)
          {
            // Error only if due dates differ
            if (!aEbiDueDate.equals (aUBLDueDate))
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .setErrorFieldName ("PaymentTerms[" +
                                                                           nPaymentTermsIndex +
                                                                           "]/PaymentDueDate")
                                                       .setErrorText (EText.PAYMENT_DUE_DATE_ALREADY_CONTAINED.getDisplayText (m_aDisplayLocale))
                                                       .build ());
          }
          else
            aEbiPaymentConditions.setDueDate (aUBLDueDate);

          final BigDecimal aUBLPaymentPerc = aUBLPaymentTerms.getPaymentPercentValue ();
          if (aUBLPaymentPerc != null && MathHelper.isGT0 (aUBLPaymentPerc) && MathHelper.isLT100 (aUBLPaymentPerc))
          {
            final MonetaryTotalType aUBLTotal = aUBLDocLegalMonetaryTotal.get ();
            final BigDecimal aBaseAmount = aUBLTotal == null ? null : aUBLTotal.getPayableAmountValue ();
            if (aBaseAmount != null)
            {
              final BigDecimal aMinimumPayment = MathHelper.getPercentValue (aBaseAmount,
                                                                             aUBLPaymentPerc,
                                                                             SCALE_PRICE2,
                                                                             ROUNDING_MODE);
              aEbiPaymentConditions.setMinimumPayment (aMinimumPayment);
            }
          }
        }
        else
          if (aUBLPaymentTerms.getSettlementDiscountPercent () != null)
          {
            if (aUBLPaymentTerms.getSettlementPeriod () == null ||
                aUBLPaymentTerms.getSettlementPeriod ().getEndDate () == null)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .setErrorFieldName ("PaymentTerms[" +
                                                                           nPaymentTermsIndex +
                                                                           "]/SettlementPeriod")
                                                       .setErrorText (EText.SETTLEMENT_PERIOD_MISSING.getDisplayText (m_aDisplayLocale))
                                                       .build ());
            }
            else
            {
              final Ebi41DiscountType aEbiDiscount = new Ebi41DiscountType ();
              aEbiDiscount.setPaymentDate (aUBLPaymentTerms.getSettlementPeriod ().getEndDateValue ());
              aEbiDiscount.setPercentage (aUBLPaymentTerms.getSettlementDiscountPercentValue ());
              // Optional amount value
              aEbiDiscount.setAmount (aUBLPaymentTerms.getAmountValue ());
              aEbiPaymentConditions.addDiscount (aEbiDiscount);
            }
          }
          else
            if (aUBLPaymentTerms.getPenaltySurchargePercent () != null)
            {
              aTransformationErrorList.add (SingleError.builderWarn ()
                                                       .setErrorFieldName ("PaymentTerms[" + nPaymentTermsIndex + "]")
                                                       .setErrorText (EText.PENALTY_NOT_ALLOWED.getDisplayText (m_aDisplayLocale))
                                                       .build ());
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
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("PaymentMeans/PaymentDueDate")
                                                 .setErrorText (EText.DISCOUNT_WITHOUT_DUEDATE.getDisplayText (m_aDisplayLocale))
                                                 .build ());
    }
    else
    {
      // Independent if discounts are present or not
      aEbiDoc.setPaymentConditions (aEbiPaymentConditions);
    }
  }
}
