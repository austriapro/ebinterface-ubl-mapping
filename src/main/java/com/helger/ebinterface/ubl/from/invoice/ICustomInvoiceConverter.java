package com.helger.ebinterface.ubl.from.invoice;

import javax.annotation.Nonnull;

import com.helger.ebinterface.v42.Ebi42InvoiceType;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Customization extension interface
 * 
 * @author Philip Helger
 */
public interface ICustomInvoiceConverter
{
  /**
   * Perform optional mapping.
   *
   * @param aUBLInvoice
   *        Existing UBL invoice. Never <code>null</code>.
   * @param aEbiInvoice
   *        Existing pre-filled ebInterface invoice. Never <code>null</code>.
   */
  void additionalMapping (@Nonnull InvoiceType aUBLInvoice, @Nonnull Ebi42InvoiceType aEbiInvoice);
}
