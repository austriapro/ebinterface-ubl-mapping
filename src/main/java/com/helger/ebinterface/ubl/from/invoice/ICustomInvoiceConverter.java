package com.helger.ebinterface.ubl.from.invoice;

import javax.annotation.Nonnull;

import com.helger.ebinterface.v42.Ebi42InvoiceType;
import com.helger.ebinterface.v42.Ebi42ListLineItemType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Customization extension interface
 *
 * @author Philip Helger
 */
public interface ICustomInvoiceConverter
{
  /**
   * Perform optional mapping after the conversion of a single details item
   * finished.
   *
   * @param aUBLInvoiceLine
   *        Existing UBL invoice line. Never <code>null</code>.
   * @param aEbiInvoiceLine
   *        Existing pre-filled ebInterface invoice line. Never
   *        <code>null</code>.
   */
  void additionalItemMapping (@Nonnull InvoiceLineType aUBLInvoiceLine, @Nonnull Ebi42ListLineItemType aEbiInvoiceLine);

  /**
   * Perform optional mapping after the whole conversion finished.
   *
   * @param aUBLInvoice
   *        Existing UBL invoice. Never <code>null</code>.
   * @param aEbiInvoice
   *        Existing pre-filled ebInterface invoice. Never <code>null</code>.
   */
  void additionalGlobalMapping (@Nonnull InvoiceType aUBLInvoice, @Nonnull Ebi42InvoiceType aEbiInvoice);
}
