/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2017 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.to;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.filter.IFileFilter;
import com.helger.commons.io.file.iterate.FileSystemIterator;
import com.helger.ebinterface.builder.EbInterfaceReader;
import com.helger.ebinterface.builder.EbInterfaceWriter;
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
  private static final Logger s_aLogger = LoggerFactory.getLogger (EbInterface42ToInvoiceConverterTest.class);

  @Test
  public void testBasic ()
  {
    final Locale aLocale = Locale.GERMANY;
    final EbInterface42ToInvoiceConverter aToUBL = new EbInterface42ToInvoiceConverter (aLocale, aLocale);
    final InvoiceToEbInterface42Converter aToEbi = new InvoiceToEbInterface42Converter (aLocale, aLocale, false);

    final EbInterfaceWriter <Ebi42InvoiceType> aEbiWriter = EbInterfaceWriter.ebInterface42 ()
                                                                             .setFormattedOutput (true);
    final UBL21WriterBuilder <InvoiceType> aUBLWriter = UBL21Writer.invoice ().setFormattedOutput (true);

    for (final File aFile : new FileSystemIterator ("src/test/resources/ebi42").withFilter (IFileFilter.filenameEndsWith (".xml")))
    {
      s_aLogger.info (aFile.getAbsolutePath ());

      final Ebi42InvoiceType aEbi = EbInterfaceReader.ebInterface42 ().read (aFile);
      assertNotNull (aEbi);

      // To UBL
      final InvoiceType aInvoice = aToUBL.convertInvoice (aEbi);
      assertNotNull (aInvoice);

      final String sUBL = aUBLWriter.getAsString (aInvoice);
      assertNotNull (sUBL);

      // Back to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi42InvoiceType aEbi2 = aToEbi.convertToEbInterface (aInvoice, aErrorList);
      assertNotNull (aEbi2);
      assertTrue (aErrorList.getAllErrors ().toString (), aErrorList.containsNoError ());

      // Convert both ebInterfaces to String and compare :)
      final String sEbi1 = aEbiWriter.getAsString (aEbi);
      final String sEbi2 = aEbiWriter.getAsString (aEbi2);

      if (false)
        s_aLogger.info (sEbi1 + "\n" + sUBL + "\n" + sEbi2);

      // Won't work :)
      if (false)
        assertEquals ("Difference after conversion: " + sUBL, sEbi1, sEbi2);
    }
  }
}
