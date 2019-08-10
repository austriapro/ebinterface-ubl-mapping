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
import com.helger.ebinterface.v40.Ebi40AccountType;
import com.helger.ebinterface.v40.Ebi40DirectDebitType;
import com.helger.ebinterface.v40.Ebi40DiscountType;
import com.helger.ebinterface.v40.Ebi40DocumentTypeType;
import com.helger.ebinterface.v40.Ebi40InvoiceType;
import com.helger.ebinterface.v40.Ebi40NoPaymentType;
import com.helger.ebinterface.v40.Ebi40PaymentConditionsType;
import com.helger.ebinterface.v40.Ebi40PaymentMethodType;
import com.helger.ebinterface.v40.Ebi40PaymentReferenceType;
import com.helger.ebinterface.v40.Ebi40UniversalBankTransactionType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialAccountType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.FinancialInstitutionType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentMeansType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PaymentTermsType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InstructionNoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.PaymentIDType;

/**
 * Base class for PEPPOL UBL to ebInterface 4.0 converter
 *
 * @author Philip Helger
 */
@Immutable
public abstract class AbstractToEbInterface40Converter extends AbstractToEbInterfaceConverter
{
  public static final int PAYMENT_REFERENCE_MAX_LENGTH = 35;

  public AbstractToEbInterface40Converter (@Nonnull final Locale aDisplayLocale,
                                           @Nonnull final Locale aContentLocale,
                                           @Nonnull final IToEbinterfaceSettings aSettings)
  {
    super (aDisplayLocale, aContentLocale, aSettings);
  }

  @Nullable
  protected static final Ebi40DocumentTypeType getAsDocumentTypeType (@Nullable final String... aValues)
  {
    if (aValues != null)
      for (final String s : aValues)
        if (s != null)
          try
          {
            // The first match wins
            return Ebi40DocumentTypeType.fromValue (s);
          }
          catch (final IllegalArgumentException ex)
          {
            // Ignore
          }
    return null;
  }

  private static void _setPaymentMeansComment (@Nonnull final PaymentMeansType aUBLPaymentMeans,
                                               @Nonnull final Ebi40PaymentMethodType aEbiPaymentMethod)
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
                                 @Nonnull final Ebi40InvoiceType aEbiDoc,
                                 final boolean bIsCreditNote)
  {
    final Ebi40PaymentConditionsType aEbiPaymentConditions = new Ebi40PaymentConditionsType ();

    {
      int nPaymentMeansIndex = 0;
      for (final PaymentMeansType aUBLPaymentMeans : aUBLDocPaymentMeans.get ())
      {
        final String sPaymentMeansCode = StringHelper.trim (aUBLPaymentMeans.getPaymentMeansCodeValue ());
        if (isUniversalBankTransaction (sPaymentMeansCode))
        {
          final Ebi40UniversalBankTransactionType aEbiPaymentMethod = new Ebi40UniversalBankTransactionType ();
          // Is a payment channel code present?
          final String sPaymentChannelCode = StringHelper.trim (aUBLPaymentMeans.getPaymentChannelCodeValue ());
          // null/empty for standard PEPPOL BIS
          if (StringHelper.hasNoText (sPaymentChannelCode) || PAYMENT_CHANNEL_CODE_IBAN.equals (sPaymentChannelCode))
          {
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            final Ebi40UniversalBankTransactionType aEbiUBTMethod = new Ebi40UniversalBankTransactionType ();

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

                final Ebi40PaymentReferenceType aEbiPaymentReference = new Ebi40PaymentReferenceType ();
                aEbiPaymentReference.setValue (sUBLPaymentID);
                aEbiUBTMethod.setPaymentReference (aEbiPaymentReference);
              }
              ++nPaymentIDIndex;
            }

            // Beneficiary account
            final Ebi40AccountType aEbiAccount = new Ebi40AccountType ();

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
            final Ebi40DirectDebitType aEbiPaymentMethod = new Ebi40DirectDebitType ();
            _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
            aEbiDoc.setPaymentMethod (aEbiPaymentMethod);

            // Set due date (optional)
            aEbiPaymentConditions.setDueDate (aUBLPaymentMeans.getPaymentDueDateValue ());

            break;
          }
          else
          {
            // No supported payment means code
            if (MathHelper.isEQ0 (aEbiDoc.getTotalGrossAmount ()))
            {
              // As nothing is to be paid we can safely use NoPayment
              final Ebi40NoPaymentType aEbiPaymentMethod = new Ebi40NoPaymentType ();
              _setPaymentMeansComment (aUBLPaymentMeans, aEbiPaymentMethod);
              aEbiDoc.setPaymentMethod (aEbiPaymentMethod);
              break;
            }

            aTransformationErrorList.add (SingleError.builderError ()
                                                     .setErrorFieldName ("PaymentMeans[" + nPaymentMeansIndex + "]")
                                                     .setErrorText (EText.PAYMENTMEANS_CODE_INVALID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                            sPaymentMeansCode,
                                                                                                                            getOrString (", ",
                                                                                                                                         "30",
                                                                                                                                         "31",
                                                                                                                                         "40",
                                                                                                                                         "58"),
                                                                                                                            getOrString (", ",
                                                                                                                                         "49")))
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
          final Ebi40NoPaymentType aEbiPaymentMethod = new Ebi40NoPaymentType ();
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
              final Ebi40DiscountType aEbiDiscount = new Ebi40DiscountType ();
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
