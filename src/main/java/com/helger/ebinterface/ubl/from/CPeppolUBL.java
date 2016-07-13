/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2016 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.from;

import javax.annotation.concurrent.Immutable;

/**
 * Contains some constants for the conversion from PEPPOL UBL to ebInterface.
 * 
 * @author philip
 */
@Immutable
public final class CPeppolUBL
{
  public static final String UBL_VERSION_20 = "2.0";
  public static final String UBL_VERSION_21 = "2.1";

  /** The UBL customization ID to use */
  public static final String CUSTOMIZATION_SCHEMEID = "PEPPOL";

  private CPeppolUBL ()
  {}
}
