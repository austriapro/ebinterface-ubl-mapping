/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2025 AUSTRIAPRO - www.austriapro.at
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

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Customization extension interface
 *
 * @author Philip Helger
 * @since 4.8.3
 * @param <INVOICETYPE>
 *        Invoice type
 * @param <LINETYPE>
 *        Invoice line type
 */
public interface ICustomInvoiceToEbInterfaceConverter <INVOICETYPE, LINETYPE>
{
  /**
   * Perform optional mapping after the whole conversion finished.
   *
   * @param aUBLInvoice
   *        Existing UBL invoice. Never <code>null</code>.
   * @param aEbiInvoice
   *        Existing pre-filled ebInterface invoice. Never <code>null</code>.
   */
  default void additionalGlobalMapping (@Nonnull final InvoiceType aUBLInvoice, @Nonnull final INVOICETYPE aEbiInvoice)
  {}

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
  default void additionalItemMapping (@Nonnull final InvoiceLineType aUBLInvoiceLine, @Nonnull final LINETYPE aEbiInvoiceLine)
  {}
}
