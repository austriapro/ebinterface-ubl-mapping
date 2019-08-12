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
package com.helger.ebinterface.ubl.from;

import com.helger.ebinterface.EbInterface40Marshaller;

/**
 * Special ebInterface 4.0 marshaller providing a default namespace prefix
 * mapper
 *
 * @author Philip Helger
 */
public final class MockEbi40Marshaller extends EbInterface40Marshaller
{
  public MockEbi40Marshaller ()
  {
    setNamespaceContext (new EbiNamespaceContext ());
  }
}