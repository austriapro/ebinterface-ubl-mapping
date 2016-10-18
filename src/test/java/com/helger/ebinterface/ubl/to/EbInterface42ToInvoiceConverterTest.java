package com.helger.ebinterface.ubl.to;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Test;

import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.filter.IFileFilter;
import com.helger.commons.io.file.iterate.FileSystemIterator;
import com.helger.ebinterface.builder.EbInterfaceReader;
import com.helger.ebinterface.builder.EbInterfaceWriter;
import com.helger.ebinterface.builder.EbInterfaceWriterBuilder;
import com.helger.ebinterface.ubl.from.invoice.InvoiceToEbInterface42Converter;
import com.helger.ebinterface.v42.Ebi42InvoiceType;
import com.helger.ubl21.UBL21Writer;
import com.helger.ubl21.UBL21WriterBuilder;

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
    final InvoiceToEbInterface42Converter aToEbi = new InvoiceToEbInterface42Converter (aLocale, aLocale, false);

    final EbInterfaceWriterBuilder <Ebi42InvoiceType> aEbiWriter = EbInterfaceWriter.ebInterface42 ()
                                                                                    .setFormattedOutput (true);
    final UBL21WriterBuilder <InvoiceType> aUBLWriter = UBL21Writer.invoice ().setFormattedOutput (true);

    for (final File aFile : new FileSystemIterator ("src/test/resources/ebi42").withFilter (IFileFilter.filenameEndsWith (".xml")))
    {
      final Ebi42InvoiceType aEbi = EbInterfaceReader.ebInterface42 ().read (aFile);
      assertNotNull (aEbi);

      // To UBL
      final InvoiceType aInvoice = aToUBL.convertInvoice (aEbi);
      assertNotNull (aInvoice);

      final String sUBL = aUBLWriter.getAsString (aInvoice);

      // Back to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi42InvoiceType aEbi2 = aToEbi.convertToEbInterface (aInvoice, aErrorList);
      assertNotNull (aEbi2);
      assertTrue (aErrorList.getAllErrors ().toString (), aErrorList.containsNoError ());

      // Convert both ebInterfaces to String and compare :)
      final String sEbi1 = aEbiWriter.getAsString (aEbi);
      final String sEbi2 = aEbiWriter.getAsString (aEbi2);

      if (true)
        System.err.println (sEbi1 + "\n" + sUBL + "\n" + sEbi2);

      assertEquals ("Difference after conversion: " + sUBL, sEbi1, sEbi2);
    }
  }
}
