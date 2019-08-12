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
package com.helger.ebinterface.ubl.from.creditnote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FileOperations;
import com.helger.commons.io.file.FileSystemIterator;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.file.IFileFilter;
import com.helger.commons.io.resource.FileSystemResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.ebinterface.ubl.from.MockEbi50Marshaller;
import com.helger.ebinterface.ubl.from.ToEbinterfaceSettings;
import com.helger.ebinterface.v50.Ebi50InvoiceType;
import com.helger.ubl21.UBL21Reader;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;

/**
 * Test class for class {@link CreditNoteToEbInterface50Converter}.
 *
 * @author PEPPOL.AT, BRZ, Philip Helger
 */
public final class CreditNoteToEbInterface50ConverterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CreditNoteToEbInterface50ConverterTest.class);
  private static final String TARGET_FOLDER = "generated-ebi50-files/";

  @Before
  public void onInit ()
  {
    FileOperations.createDirRecursiveIfNotExisting (new File (TARGET_FOLDER));
  }

  @Test
  public void testConvertPEPPOLCreditNoteLax ()
  {
    final ICommonsList <IReadableResource> aTestFiles = new CommonsArrayList <> ();
    for (final File aFile : new FileSystemIterator (new File ("src/test/resources/ubl20/creditnote")).withFilter (IFileFilter.filenameEndsWith (".xml")))
      aTestFiles.add (new FileSystemResource (aFile));

    // For all PEPPOL test invoices
    for (final IReadableResource aRes : aTestFiles)
    {
      LOGGER.info (aRes.getPath ());
      assertTrue (aRes.exists ());

      // Read UBL
      final CreditNoteType aUBLCreditNote = UBL21Reader.creditNote ().read (aRes);
      assertNotNull (aUBLCreditNote);

      // Convert to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi50InvoiceType aEbInvoice = new CreditNoteToEbInterface50Converter (Locale.GERMANY,
                                                                                  Locale.GERMANY,
                                                                                  new ToEbinterfaceSettings ()).convertToEbInterface (aUBLCreditNote,
                                                                                                                                      aErrorList);
      assertTrue (aRes.getPath () + ": " + aErrorList.toString (),
                  aErrorList.isEmpty () || aErrorList.getMostSevereErrorLevel ().isLT (EErrorLevel.ERROR));
      assertNotNull (aEbInvoice);

      if (!aErrorList.isEmpty () && aErrorList.getMostSevereErrorLevel ().isGE (EErrorLevel.WARN))
        LOGGER.info ("  " + aErrorList.toString ());

      // Convert ebInterface to XML
      assertTrue (new MockEbi50Marshaller ().write (aEbInvoice,
                                                    new File (TARGET_FOLDER +
                                                              FilenameHelper.getWithoutPath (aRes.getPath ())))
                                            .isSuccess ());
    }
  }

  @Test
  public void testConvertPEPPOLInvoiceERB ()
  {
    final ICommonsList <IReadableResource> aTestFiles = new CommonsArrayList <> ();
    for (final File aFile : new FileSystemIterator (new File ("src/test/resources/ubl20/creditnote")).withFilter (IFileFilter.filenameEndsWith (".xml")))
      aTestFiles.add (new FileSystemResource (aFile));

    // For all PEPPOL test invoices
    for (final IReadableResource aRes : aTestFiles)
    {
      LOGGER.info (aRes.getPath ());
      assertTrue (aRes.exists ());

      // Read UBL
      final CreditNoteType aUBLCreditNote = UBL21Reader.creditNote ().read (aRes);
      assertNotNull (aUBLCreditNote);

      // Convert to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi50InvoiceType aEbInvoice = new CreditNoteToEbInterface50Converter (Locale.GERMANY,
                                                                                  Locale.GERMANY,
                                                                                  ToEbinterfaceSettings.getERechnungGvAtSettings ()).convertToEbInterface (aUBLCreditNote,
                                                                                                                                                           aErrorList);
      assertTrue (aRes.getPath () + ": " + aErrorList.toString (),
                  aErrorList.getMostSevereErrorLevel ().isLT (EErrorLevel.ERROR));
      assertNotNull (aEbInvoice);

      if (aErrorList.getMostSevereErrorLevel ().isGE (EErrorLevel.WARN))
        LOGGER.info ("  " + aErrorList.toString ());

      // Convert ebInterface to XML
      assertTrue (new MockEbi50Marshaller ().write (aEbInvoice,
                                                    new File (TARGET_FOLDER +
                                                              FilenameHelper.getWithoutPath (aRes.getPath ())))
                                            .isSuccess ());
    }
  }
}