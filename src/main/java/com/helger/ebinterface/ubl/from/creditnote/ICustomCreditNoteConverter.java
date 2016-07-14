package com.helger.ebinterface.ubl.from.creditnote;

import javax.annotation.Nonnull;

import com.helger.ebinterface.v42.Ebi42InvoiceType;
import com.helger.ebinterface.v42.Ebi42ListLineItemType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CreditNoteLineType;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;

/**
 * Customization extension interface
 *
 * @author Philip Helger
 */
public interface ICustomCreditNoteConverter
{
  /**
   * Perform optional mapping after the conversion of a single details item
   * finished.
   *
   * @param aUBLCreditNoteLine
   *        Existing UBL credit note line. Never <code>null</code>.
   * @param aEbiInvoiceLine
   *        Existing pre-filled ebInterface invoice line. Never
   *        <code>null</code>.
   */
  void additionalItemMapping (@Nonnull CreditNoteLineType aUBLCreditNoteLine,
                              @Nonnull Ebi42ListLineItemType aEbiInvoiceLine);

  /**
   * Perform optional mapping.
   *
   * @param aUBLCreditNote
   *        Existing UBL credit note. Never <code>null</code>.
   * @param aEbiInvoice
   *        Existing pre-filled ebInterface invoice. Never <code>null</code>.
   */
  void additionalGlobalMapping (@Nonnull CreditNoteType aUBLCreditNote, @Nonnull Ebi42InvoiceType aEbiInvoice);
}
