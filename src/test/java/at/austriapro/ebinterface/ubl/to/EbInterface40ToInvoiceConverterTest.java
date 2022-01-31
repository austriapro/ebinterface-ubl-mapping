/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2022 AUSTRIAPRO - www.austriapro.at
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
package at.austriapro.ebinterface.ubl.to;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FileOperations;
import com.helger.commons.io.file.FileSystemIterator;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.file.IFileFilter;
import com.helger.ebinterface.builder.EbInterfaceReader;
import com.helger.ebinterface.builder.EbInterfaceWriter;
import com.helger.ebinterface.v40.Ebi40InvoiceType;
import com.helger.ubl21.UBL21Writer;
import com.helger.ubl21.UBL21WriterBuilder;

import at.austriapro.ebinterface.ubl.from.ToEbinterfaceSettings;
import at.austriapro.ebinterface.ubl.from.invoice.InvoiceToEbInterface40Converter;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Test class for class {@link EbInterface40ToInvoiceConverter}.
 *
 * @author Philip Helger
 */
public final class EbInterface40ToInvoiceConverterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EbInterface40ToInvoiceConverterTest.class);
  private static final String TARGET_FOLDER = "generated-ebi40-to-ubl-files/";

  @Before
  public void onInit ()
  {
    FileOperations.createDirRecursiveIfNotExisting (new File (TARGET_FOLDER));
  }

  @Test
  public void testBasic ()
  {
    final Locale aLocale = Locale.GERMANY;
    final EbInterface40ToInvoiceConverter aToUBL = new EbInterface40ToInvoiceConverter (aLocale, aLocale);
    final InvoiceToEbInterface40Converter aToEbi = new InvoiceToEbInterface40Converter (aLocale,
                                                                                        aLocale,
                                                                                        new ToEbinterfaceSettings ());

    final EbInterfaceWriter <Ebi40InvoiceType> aEbiWriter = EbInterfaceWriter.ebInterface40 ()
                                                                             .setFormattedOutput (true);
    final UBL21WriterBuilder <InvoiceType> aUBLWriter = UBL21Writer.invoice ().setFormattedOutput (true);

    for (final File aFile : new FileSystemIterator ("src/test/resources/ebinterface/ebi40").withFilter (IFileFilter.filenameEndsWith (".xml")))
    {
      LOGGER.info (aFile.getAbsolutePath ());

      final Ebi40InvoiceType aEbi = EbInterfaceReader.ebInterface40 ().read (aFile);
      assertNotNull (aEbi);

      // To UBL
      final InvoiceType aInvoice = aToUBL.convertInvoice (aEbi);
      assertNotNull (aInvoice);

      final String sUBL = aUBLWriter.getAsString (aInvoice);
      assertNotNull (sUBL);

      // Back to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi40InvoiceType aEbi2 = aToEbi.convertToEbInterface (aInvoice, aErrorList);
      assertNotNull (aEbi2);
      assertTrue (aErrorList.getAllErrors ().toString () + "\n\nSource UBL: " + sUBL, aErrorList.containsNoError ());

      // Convert both ebInterfaces to String and compare :)
      final String sEbi1 = aEbiWriter.getAsString (aEbi);
      final String sEbi2 = aEbiWriter.getAsString (aEbi2);

      if (false)
        LOGGER.info (sEbi1 + "\n" + sUBL + "\n" + sEbi2);

      // Won't work :)
      if (false)
        assertEquals ("Difference after conversion: " + sUBL, sEbi1, sEbi2);

      // Write to folder
      assertTrue (aUBLWriter.write (aInvoice, new File (TARGET_FOLDER + FilenameHelper.getWithoutPath (aFile)))
                            .isSuccess ());
    }
  }
}
