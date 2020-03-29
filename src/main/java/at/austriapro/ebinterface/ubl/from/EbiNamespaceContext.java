/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2020 AUSTRIAPRO - www.austriapro.at
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
    addMapping ("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
    addMapping ("xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);
    addMapping ("eb30", CEbInterface.EBINTERFACE_30_NS);
    addMapping ("eb302", CEbInterface.EBINTERFACE_302_NS);
    addMapping ("eb40", CEbInterface.EBINTERFACE_40_NS);
    addMapping ("eb40e", "http://www.ebinterface.at/schema/4p0/extensions/ext");
    addMapping ("eb40s", "http://www.ebinterface.at/schema/4p0/extensions/sv");
    addMapping ("eb41", CEbInterface.EBINTERFACE_41_NS);
    addMapping ("eb41e", "http://www.ebinterface.at/schema/4p1/extensions/ext");
    addMapping ("eb41s", "http://www.ebinterface.at/schema/4p1/extensions/sv");
    addMapping ("eb42", CEbInterface.EBINTERFACE_42_NS);
    addMapping ("eb42e", "http://www.ebinterface.at/schema/4p2/extensions/ext");
    addMapping ("eb42s", "http://www.ebinterface.at/schema/4p2/extensions/sv");
    addMapping ("eb43", CEbInterface.EBINTERFACE_43_NS);
    addMapping ("eb43e", "http://www.ebinterface.at/schema/4p3/extensions/ext");
    addMapping ("eb43s", "http://www.ebinterface.at/schema/4p3/extensions/sv");
    addMapping ("eb50", CEbInterface.EBINTERFACE_50_NS);
    addMapping (CXMLDSig.DEFAULT_PREFIX, CXMLDSig.NAMESPACE_URI);
  }
}
