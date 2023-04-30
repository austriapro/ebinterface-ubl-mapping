/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2023 AUSTRIAPRO - www.austriapro.at
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
package at.austriapro.ebinterface.ubl.from;

import javax.xml.XMLConstants;

import com.helger.ebinterface.CEbInterface;
import com.helger.xml.CXML;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xsds.xmldsig.CXMLDSig;

/**
 * A special map-based namespace context that maps XML prefixes to namespace
 * URLs.
 *
 * @author Philip Helger
 */
public class EbiNamespaceContext extends MapBasedNamespaceContext
{
  public EbiNamespaceContext ()
  {
    addMapping (CXML.XML_NS_PREFIX_XSI, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
    addMapping (CXML.XML_NS_PREFIX_XSD, XMLConstants.W3C_XML_SCHEMA_NS_URI);
    addMapping (CXMLDSig.DEFAULT_PREFIX, CXMLDSig.NAMESPACE_URI);

    addMapping ("eb30", CEbInterface.EBINTERFACE_30_NS);

    addMapping ("eb302", CEbInterface.EBINTERFACE_302_NS);

    addMapping ("eb40", CEbInterface.EBINTERFACE_40_NS);
    addMapping ("eb40e", CEbInterface.EBINTERFACE_40_NS_EXT);
    addMapping ("eb40s", CEbInterface.EBINTERFACE_40_NS_SV);

    addMapping ("eb41", CEbInterface.EBINTERFACE_41_NS);
    addMapping ("eb41e", CEbInterface.EBINTERFACE_41_NS_EXT);
    addMapping ("eb41s", CEbInterface.EBINTERFACE_41_NS_SV);

    addMapping ("eb42", CEbInterface.EBINTERFACE_42_NS);
    addMapping ("eb42e", CEbInterface.EBINTERFACE_42_NS_EXT);
    addMapping ("eb42s", CEbInterface.EBINTERFACE_42_NS_SV);

    addMapping ("eb43", CEbInterface.EBINTERFACE_43_NS);
    addMapping ("eb43e", CEbInterface.EBINTERFACE_43_NS_EXT);
    addMapping ("eb43s", CEbInterface.EBINTERFACE_43_NS_SV);

    addMapping ("eb50", CEbInterface.EBINTERFACE_50_NS);

    addMapping ("eb60", CEbInterface.EBINTERFACE_60_NS);

    addMapping ("eb61", CEbInterface.EBINTERFACE_61_NS);
  }
}
