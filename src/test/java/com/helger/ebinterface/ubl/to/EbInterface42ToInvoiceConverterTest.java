package com.helger.ebinterface.ubl.to;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Locale;

import org.junit.Test;

import com.helger.commons.io.file.filter.IFileFilter;
import com.helger.commons.io.file.iterate.FileSystemIterator;
import com.helger.ebinterface.builder.EbInterfaceReader;
import com.helger.ebinterface.v42.Ebi42InvoiceType;
import com.helger.ubl21.UBL21Writer;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Test class for class {@link EbInterface42ToInvoiceConverter}.
 *
 * @author Philip Helger
 */
public final class EbInterface42ToInvoiceConverterTest
{
  @Test
  public void testBasic ()
  {
    final Locale aLocale = Locale.GERMANY;
    final EbInterface42ToInvoiceConverter aToUBL = new EbInterface42ToInvoiceConverter (aLocale, aLocale);

    for (final File aFile : new FileSystemIterator ("src/test/resources/ebi42").withFilter (IFileFilter.filenameEndsWith (".xml")))
    {
      final Ebi42InvoiceType aEbi = EbInterfaceReader.ebInterface42 ().read (aFile);
      assertNotNull (aEbi);

      final InvoiceType aInvoice = aToUBL.convertInvoice (aEbi);
      assertNotNull (aInvoice);

      System.err.println (UBL21Writer.invoice ().getAsString (aInvoice));
    }
  }
}
