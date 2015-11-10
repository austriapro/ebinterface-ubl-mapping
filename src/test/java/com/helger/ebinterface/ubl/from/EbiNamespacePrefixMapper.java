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
package com.helger.ebinterface.ubl.from;

import com.helger.commons.xml.CXML;
import com.helger.ebinterface.CEbInterface;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class EbiNamespacePrefixMapper extends NamespacePrefixMapper
{
  @Override
  public String getPreferredPrefix (final String sNamespaceUri, final String sSuggestion, final boolean requirePrefix)
  {
    // XSI prefix
    if (sNamespaceUri.equals (CXML.XML_NS_XSI))
      return "xsi";

    // XS prefix
    if (sNamespaceUri.equals (CXML.XML_NS_XSD))
      return "xs";

    // ebInterface specific prefixes
    if (sNamespaceUri.equals (CEbInterface.EBINTERFACE_40_NS) || sNamespaceUri.equals (CEbInterface.EBINTERFACE_41_NS))
      return "eb";

    // XMLDsig
    if (sNamespaceUri.equals ("http://www.w3.org/2000/09/xmldsig#"))
      return "dsig";

    return sSuggestion;
  }
}
