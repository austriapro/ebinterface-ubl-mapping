/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2021 AUSTRIAPRO - www.austriapro.at
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

import javax.annotation.Nonnull;

import com.helger.ebinterface.v50.Ebi50InvoiceType;
import com.helger.ebinterface.v50.Ebi50ListLineItemType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Customization extension interface
 *
 * @author Philip Helger
 */
public interface ICustomInvoiceToEbInterface50Converter
{
  /**
   * Perform optional mapping after the conversion of a single details item
   * finished.
   *
   * @param aUBLInvoiceLine
   *        Existing UBL invoice line. Never <code>null</code>.
   * @param aEbiInvoiceLine
   *        Existing pre-filled ebInterface invoice line. Never
   *        <code>null</code>.
   */
  default void additionalItemMapping (@Nonnull final InvoiceLineType aUBLInvoiceLine,
                                      @Nonnull final Ebi50ListLineItemType aEbiInvoiceLine)
  {}

  /**
   * Perform optional mapping after the whole conversion finished.
   *
   * @param aUBLInvoice
   *        Existing UBL invoice. Never <code>null</code>.
   * @param aEbiInvoice
   *        Existing pre-filled ebInterface invoice. Never <code>null</code>.
   */
  default void additionalGlobalMapping (@Nonnull final InvoiceType aUBLInvoice,
                                        @Nonnull final Ebi50InvoiceType aEbiInvoice)
  {}
}
