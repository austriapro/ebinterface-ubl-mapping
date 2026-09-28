/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2026 AUSTRIAPRO - www.austriapro.at
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

import java.io.File;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.ebinterface.EbInterface50Marshaller;
import com.helger.ebinterface.EbInterface60Marshaller;
import com.helger.ebinterface.EbInterface61Marshaller;
import com.helger.ebinterface.v50.Ebi50InvoiceType;
import com.helger.ebinterface.v60.Ebi60InvoiceType;
import com.helger.ebinterface.v61.Ebi61InvoiceType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxCategoryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.TaxSubtotalType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.TaxExemptionReasonType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Test class for class {@link AbstractEbInterfaceToUBLConverter}.
 *
 * @author Philip Helger
 */
public final class AbstractEbInterfaceToUBLConverterTest
{
  private static final Locale LOCALE = Locale.GERMANY;
  private static final String COMMENT = "USt-befreit gemäß § xxx";

  /**
   * @param aUBLDoc
   *        The UBL invoice to be evaluated. May not be <code>null</code>.
   * @return One entry per <code>TaxSubtotal</code>, in document order, of the form
   *         "&lt;TaxCategory ID&gt;:&lt;all TaxExemptionReason texts&gt;".
   */
  @NonNull
  private static ICommonsList <String> _getTaxExemptionReasons (@NonNull final InvoiceType aUBLDoc)
  {
    final ICommonsList <String> ret = new CommonsArrayList <> ();
    assertEquals (1, aUBLDoc.getTaxTotalCount ());
    for (final TaxSubtotalType aUBLTaxSubtotal : aUBLDoc.getTaxTotalAtIndex (0).getTaxSubtotal ())
    {
      final TaxCategoryType aUBLTaxCategory = aUBLTaxSubtotal.getTaxCategory ();
      final StringBuilder aSB = new StringBuilder (aUBLTaxCategory.getIDValue ()).append (':');
      for (final TaxExemptionReasonType aUBLReason : aUBLTaxCategory.getTaxExemptionReason ())
        aSB.append (aUBLReason.getValue ());
      ret.add (aSB.toString ());
    }
    return ret;
  }

  @Test
  public void testApplyTaxExemptionReason ()
  {
    // BR-S-10 and BR-Z-10 forbid BT-120 for these tax category codes
    for (final String sTaxCategoryCode : new String [] { "S", "Z" })
    {
      final TaxCategoryType aUBLTaxCategory = AbstractEbInterfaceToUBLConverter.createTaxCategoryVAT (sTaxCategoryCode);
      AbstractEbInterfaceToUBLConverter.applyTaxExemptionReason (aUBLTaxCategory, COMMENT);
      assertEquals (0, aUBLTaxCategory.getTaxExemptionReasonCount ());
    }

    // All other tax category codes may carry BT-120
    for (final String sTaxCategoryCode : new String [] { "AA", "E", "O", "K", "G" })
    {
      final TaxCategoryType aUBLTaxCategory = AbstractEbInterfaceToUBLConverter.createTaxCategoryVAT (sTaxCategoryCode);
      AbstractEbInterfaceToUBLConverter.applyTaxExemptionReason (aUBLTaxCategory, COMMENT);
      assertEquals (1, aUBLTaxCategory.getTaxExemptionReasonCount ());
      assertEquals (COMMENT, aUBLTaxCategory.getTaxExemptionReasonAtIndex (0).getValue ());
    }

    // Nothing to be added
    for (final String sComment : new String [] { null, "" })
    {
      final TaxCategoryType aUBLTaxCategory = AbstractEbInterfaceToUBLConverter.createTaxCategoryVAT ("E");
      AbstractEbInterfaceToUBLConverter.applyTaxExemptionReason (aUBLTaxCategory, sComment);
      assertEquals (0, aUBLTaxCategory.getTaxExemptionReasonCount ());
    }
  }

  @Test
  public void testTaxExemptionReason50 ()
  {
    final Ebi50InvoiceType aEbiDoc = new EbInterface50Marshaller ().read (new File ("src/test/resources/external/ebinterface/ebi50/ebinterface_5p0_sample.xml"));
    assertNotNull (aEbiDoc);

    final InvoiceType aUBLDoc = new EbInterface50ToInvoiceConverter (LOCALE, LOCALE).convertInvoice (aEbiDoc);
    assertNotNull (aUBLDoc);

    // The "S" TaxItem has a Comment as well, but BR-S-10 forbids BT-120 there.
    // The trailing "O:" entries are the OtherTax elements
    assertEquals (new CommonsArrayList <> ("S:",
                                           "AA:10% reduzierter Steuersatz",
                                           "E:Reverse Charge",
                                           "E:" + COMMENT,
                                           "O:",
                                           "O:",
                                           "O:"),
                  _getTaxExemptionReasons (aUBLDoc));
  }

  @Test
  public void testTaxExemptionReason60 ()
  {
    final Ebi60InvoiceType aEbiDoc = new EbInterface60Marshaller ().read (new File ("src/test/resources/external/ebinterface/ebi60/ebinterface_6p0_sample_dokumentation.xml"));
    assertNotNull (aEbiDoc);

    final InvoiceType aUBLDoc = new EbInterface60ToInvoiceConverter (LOCALE, LOCALE).convertInvoice (aEbiDoc);
    assertNotNull (aUBLDoc);

    assertEquals (new CommonsArrayList <> ("S:",
                                           "AA:10% reduzierter Steuersatz",
                                           "O:Abgabe - nicht steuerbar",
                                           "E:" + COMMENT,
                                           "O:"),
                  _getTaxExemptionReasons (aUBLDoc));
  }

  @Test
  public void testTaxExemptionReason61 ()
  {
    final Ebi61InvoiceType aEbiDoc = new EbInterface61Marshaller ().read (new File ("src/test/resources/external/ebinterface/ebi61/ebinterface_6p1_sample_dokumentation.xml"));
    assertNotNull (aEbiDoc);

    final InvoiceType aUBLDoc = new EbInterface61ToInvoiceConverter (LOCALE, LOCALE).convertInvoice (aEbiDoc);
    assertNotNull (aUBLDoc);

    assertEquals (new CommonsArrayList <> ("S:",
                                           "AA:10% reduzierter Steuersatz",
                                           "O:Abgabe - nicht steuerbar",
                                           "E:" + COMMENT,
                                           "O:"),
                  _getTaxExemptionReasons (aUBLDoc));
  }
}
