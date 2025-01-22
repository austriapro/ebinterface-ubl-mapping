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
package at.austriapro.ebinterface.ubl.from.creditnote;

import com.helger.ebinterface.v43.Ebi43InvoiceType;
import com.helger.ebinterface.v43.Ebi43ListLineItemType;

/**
 * Customization extension interface
 *
 * @author Philip Helger
 */
public interface ICustomCreditNoteToEbInterface43Converter extends
                                                           ICustomCreditNoteToEbInterfaceConverter <Ebi43InvoiceType, Ebi43ListLineItemType>
{
  /* empty */
}
