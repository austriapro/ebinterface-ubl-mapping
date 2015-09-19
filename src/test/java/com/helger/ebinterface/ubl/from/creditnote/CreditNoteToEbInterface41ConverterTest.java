/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015 AUSTRIAPRO - www.austriapro.at
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.helger.commons.error.EErrorLevel;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.file.filter.FileFilterFilenameEndsWith;
import com.helger.commons.io.file.iterate.FileSystemIterator;
import com.helger.commons.io.file.iterate.FileSystemRecursiveIterator;
import com.helger.commons.io.resource.FileSystemResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.xml.serialize.write.XMLWriter;
import com.helger.ebinterface.EbInterface41Marshaller;
import com.helger.ebinterface.ubl.from.EbiNamespacePrefixMapper;
import com.helger.ebinterface.ubl.from.creditnote.CreditNoteToEbInterface41Converter;
import com.helger.ebinterface.v41.Ebi41InvoiceType;
import com.helger.jaxb.JAXBMarshallerHelper;
import com.helger.ubl21.UBL21Reader;
import com.helger.validation.error.ErrorList;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;

/**
 * Test class for class {@link CreditNoteToEbInterface41Converter}.
 *
 * @author PEPPOL.AT, BRZ, Philip Helger
 */
public class CreditNoteToEbInterface41ConverterTest
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (CreditNoteToEbInterface41ConverterTest.class);

  @Test
  public void testConvertPEPPOLCreditNoteLax ()
  {
    final List <IReadableResource> aTestFiles = new ArrayList <IReadableResource> ();
    for (final File aFile : FileSystemRecursiveIterator.create (new File ("src/test/resources/ubl20/creditnote"),
                                                                new FileFilterFilenameEndsWith (".xml")))
      aTestFiles.add (new FileSystemResource (aFile));

    // For all PEPPOL test invoices
    for (final IReadableResource aRes : aTestFiles)
    {
      s_aLogger.info (aRes.getPath ());
      assertTrue (aRes.exists ());

      // Read UBL
      final CreditNoteType aUBLCreditNote = UBL21Reader.readCreditNote (aRes);
      assertNotNull (aUBLCreditNote);

      // Convert to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi41InvoiceType aEbInvoice = new CreditNoteToEbInterface41Converter (Locale.GERMANY,
                                                                                  Locale.GERMANY,
                                                                                  false).convertToEbInterface (aUBLCreditNote,
                                                                                                               aErrorList);
      assertTrue (aRes.getPath () +
                  ": " +
                  aErrorList.toString (),
                  aErrorList.isEmpty () || aErrorList.getMostSevereErrorLevel ().isLessSevereThan (EErrorLevel.ERROR));
      assertNotNull (aEbInvoice);

      if (!aErrorList.isEmpty () && aErrorList.getMostSevereErrorLevel ().isMoreOrEqualSevereThan (EErrorLevel.WARN))
        s_aLogger.info ("  " + aErrorList.getAllItems ());

      // Convert ebInterface to XML
      final Document aDocEb = new EbInterface41Marshaller ()
      {
        @Override
        protected void customizeMarshaller (@Nonnull final Marshaller aMarshaller)
        {
          JAXBMarshallerHelper.setSunNamespacePrefixMapper (aMarshaller, new EbiNamespacePrefixMapper ());
        }
      }.write (aEbInvoice);
      assertNotNull (aDocEb);

      XMLWriter.writeToStream (aDocEb, FileHelper.getOutputStream ("generated-ebi41-files/" +
                                                                   FilenameHelper.getWithoutPath (aRes.getPath ())));
    }
  }

  @Test
  public void testConvertPEPPOLInvoiceERB ()
  {
    final List <IReadableResource> aTestFiles = new ArrayList <IReadableResource> ();
    for (final File aFile : FileSystemIterator.create (new File ("src/test/resources/ubl20/creditnote"),
                                                       new FileFilterFilenameEndsWith (".xml")))
      aTestFiles.add (new FileSystemResource (aFile));

    // For all PEPPOL test invoices
    for (final IReadableResource aRes : aTestFiles)
    {
      s_aLogger.info (aRes.getPath ());
      assertTrue (aRes.exists ());

      // Read UBL
      final CreditNoteType aUBLCreditNote = UBL21Reader.readCreditNote (aRes);
      assertNotNull (aUBLCreditNote);

      // Convert to ebInterface
      final ErrorList aErrorList = new ErrorList ();
      final Ebi41InvoiceType aEbInvoice = new CreditNoteToEbInterface41Converter (Locale.GERMANY,
                                                                                  Locale.GERMANY,
                                                                                  true).convertToEbInterface (aUBLCreditNote,
                                                                                                              aErrorList);
      assertTrue (aRes.getPath () +
                  ": " +
                  aErrorList.toString (),
                  aErrorList.getMostSevereErrorLevel ().isLessSevereThan (EErrorLevel.ERROR));
      assertNotNull (aEbInvoice);

      if (aErrorList.getMostSevereErrorLevel ().isMoreOrEqualSevereThan (EErrorLevel.WARN))
        s_aLogger.info ("  " + aErrorList.getAllItems ());

      // Convert ebInterface to XML
      final Document aDocEb = new EbInterface41Marshaller ()
      {
        @Override
        protected void customizeMarshaller (@Nonnull final Marshaller aMarshaller)
        {
          JAXBMarshallerHelper.setSunNamespacePrefixMapper (aMarshaller, new EbiNamespacePrefixMapper ());
        }
      }.write (aEbInvoice);
      assertNotNull (aDocEb);

      XMLWriter.writeToStream (aDocEb, FileHelper.getOutputStream ("generated-ebi41-files/" +
                                                                   FilenameHelper.getWithoutPath (aRes.getPath ())));
    }
  }
}
