package com.helger.ebinterface.ubl.to;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.ebinterface.v42.Ebi42InvoiceType;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

public class EbInterface42ToInvoiceConverter extends AbstractToUBLConverter
{
  public EbInterface42ToInvoiceConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @Nonnull
  public InvoiceType convertInvoice (@Nonnull final Ebi42InvoiceType aEbiInvoice)
  {
    final InvoiceType aUBLInvoice = new InvoiceType ();
    aUBLInvoice.setUBLVersionID (UBL_VERSION_21);
    return aUBLInvoice;
  }
}
