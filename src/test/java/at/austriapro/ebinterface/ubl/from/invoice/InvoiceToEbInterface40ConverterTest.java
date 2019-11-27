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
package at.austriapro.ebinterface.ubl.from.invoice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashSet;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FileOperations;
import com.helger.commons.io.file.FileSystemIterator;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.file.IFileFilter;
import com.helger.commons.io.resource.FileSystemResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.ebinterface.v40.Ebi40InvoiceType;
import com.helger.ubl21.UBL21Reader;

import at.austriapro.ebinterface.ubl.from.MockEbi40Marshaller;
import at.austriapro.ebinterface.ubl.from.ToEbinterfaceSettings;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Test class for class {@link InvoiceToEbInterface40Converter}.
 *
 * @author Philip Helger
 */
public final class InvoiceToEbInterface40ConverterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (InvoiceToEbInterface40ConverterTest.class);
  private static final String TARGET_FOLDER = "generated-ebi40-files/";

  private static final ICommonsSet <String> IGNORED_FILES = new CommonsHashSet <> ("20120822125754.482.xml",
                                                                                   "delivery-per-item.xml",
                                                                                   "good-no-orderid-no-ebi40.xml",
                                                                                   "invoice-with-all-elements.xml",
                                                                                   "other-tax.xml",
                                                                                   "payment-terms.xml",
                                                                                   "test-at-gov-new-creditorid.xml",
                                                                                   "test-at-gov-reverse-charge.xml",
                                                                                   "test-base-at-gov.xml",
                                                                                   "test-paymentmeans-code-59.xml",
                                                                                   "testbed-test-invoice.xml");

  @Before
  public void onInit ()
  {
    FileOperations.createDirRecursiveIfNotExisting (new File (TARGET_FOLDER));
  }

  @Test
  public void testConvertPEPPOLInvoiceLax ()
  {
    final ICommonsList <IReadableResource> aTestFiles = new CommonsArrayList <> ();
    for (final File aFile : new FileSystemIterator (new File ("src/test/resources/ubl/invoice")).withFilter (IFileFilter.filenameEndsWith (".xml")))
      if (!IGNORED_FILES.contains (aFile.getName ()))
        aTestFiles.add (new FileSystemResource (aFile));

    // For all PEPPOL test invoices
    for (final IReadableResource aRes : aTestFiles)
    {
      LOGGER.info (aRes.getPath ());
      assertTrue (aRes.exists ());

      // Read UBL
      final InvoiceType aUBLInvoice = UBL21Reader.invoice ().read (aRes);
      assertNotNull (aUBLInvoice);

      // Convert to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi40InvoiceType aEbInvoice = new InvoiceToEbInterface40Converter (Locale.GERMANY,
                                                                               Locale.GERMANY,
                                                                               new ToEbinterfaceSettings ()).convertToEbInterface (aUBLInvoice,
                                                                                                                                   aErrorList);
      assertTrue (aRes.getPath () + ": " + aErrorList.toString (),
                  aErrorList.isEmpty () || aErrorList.getMostSevereErrorLevel ().isLT (EErrorLevel.ERROR));
      assertNotNull (aEbInvoice);

      if (!aErrorList.isEmpty () && aErrorList.getMostSevereErrorLevel ().isGE (EErrorLevel.WARN))
        LOGGER.info ("  " + aErrorList.toString ());

      // Convert ebInterface to XML
      assertTrue (new MockEbi40Marshaller ().write (aEbInvoice,
                                                    new File (TARGET_FOLDER +
                                                              FilenameHelper.getWithoutPath (aRes.getPath ())))
                                            .isSuccess ());
    }
  }

  @Test
  public void testConvertPEPPOLInvoiceERB ()
  {
    final ICommonsList <IReadableResource> aTestFiles = new CommonsArrayList <> ();
    for (final File aFile : new FileSystemIterator (new File ("src/test/resources/ubl/invoice")).withFilter (IFileFilter.filenameEndsWith (".xml")))
      if (!IGNORED_FILES.contains (aFile.getName ()))
        aTestFiles.add (new FileSystemResource (aFile));

    // For all PEPPOL test invoices
    for (final IReadableResource aRes : aTestFiles)
    {
      LOGGER.info (aRes.getPath ());
      assertTrue (aRes.exists ());

      // Read UBL
      final InvoiceType aUBLInvoice = UBL21Reader.invoice ().read (aRes);
      assertNotNull (aUBLInvoice);

      // Convert to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi40InvoiceType aEbInvoice = new InvoiceToEbInterface40Converter (Locale.GERMANY,
                                                                               Locale.GERMANY,
                                                                               ToEbinterfaceSettings.getERechnungGvAtSettings ()).convertToEbInterface (aUBLInvoice,
                                                                                                                                                        aErrorList);
      assertTrue (aRes.getPath () + ": " + aErrorList.toString (),
                  aErrorList.getMostSevereErrorLevel ().isLT (EErrorLevel.ERROR));
      assertNotNull (aEbInvoice);

      if (aErrorList.getMostSevereErrorLevel ().isGE (EErrorLevel.WARN))
        LOGGER.info ("  " + aErrorList.toString ());

      // Convert ebInterface to XML
      assertTrue (new MockEbi40Marshaller ().write (aEbInvoice,
                                                    new File (TARGET_FOLDER +
                                                              FilenameHelper.getWithoutPath (aRes.getPath ())))
                                            .isSuccess ());
    }
  }

  @Test
  public void testConvertPEPPOLInvoiceLaxBad ()
  {
    final ICommonsList <IReadableResource> aTestFiles = new CommonsArrayList <> ();
    for (final File aFile : new FileSystemIterator (new File ("src/test/resources/ubl20/invoice_bad")).withFilter (IFileFilter.filenameEndsWith (".xml")))
      aTestFiles.add (new FileSystemResource (aFile));

    // For all PEPPOL test invoices
    for (final IReadableResource aRes : aTestFiles)
    {
      LOGGER.info (aRes.getPath ());
      assertTrue (aRes.exists ());

      // Read UBL
      final InvoiceType aUBLInvoice = UBL21Reader.invoice ().read (aRes);
      assertNotNull (aUBLInvoice);

      // Convert to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi40InvoiceType aEbInvoice = new InvoiceToEbInterface40Converter (Locale.GERMANY,
                                                                               Locale.GERMANY,
                                                                               new ToEbinterfaceSettings ()).convertToEbInterface (aUBLInvoice,
                                                                                                                                   aErrorList);
      assertNotNull (aEbInvoice);
      assertTrue (aRes.getPath () + ": " + aErrorList.toString (),
                  !aErrorList.isEmpty () && aErrorList.getMostSevereErrorLevel ().isGE (EErrorLevel.ERROR));

      // Convert ebInterface to XML
      final Document aDocEb = new MockEbi40Marshaller ().getAsDocument (aEbInvoice);
      assertNull (aDocEb);
    }
  }
}