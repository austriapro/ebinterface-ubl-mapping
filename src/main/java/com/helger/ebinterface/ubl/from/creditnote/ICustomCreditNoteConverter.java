package com.helger.ebinterface.ubl.from.creditnote;

import javax.annotation.Nonnull;

import com.helger.ebinterface.v42.Ebi42InvoiceType;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;

/**
 * Customization extension interface
 *
 * @author Philip Helger
 */
public interface ICustomCreditNoteConverter
{
  /**
   * Perform optional mapping.
   *
   * @param aUBLCreditNote
   *        Existing UBL credit note. Never <code>null</code>.
   * @param aEbiInvoice
   *        Existing pre-filled ebInterface invoice. Never <code>null</code>.
   */
  void additionalMapping (@Nonnull CreditNoteType aUBLCreditNote, @Nonnull Ebi42InvoiceType aEbiInvoice);
}
