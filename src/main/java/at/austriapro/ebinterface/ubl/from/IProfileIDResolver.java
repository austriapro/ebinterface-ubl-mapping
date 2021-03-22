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
package at.austriapro.ebinterface.ubl.from;

import java.util.function.Function;

import javax.annotation.Nonnull;

import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.PeppolIdentifierFactory;
import com.helger.peppolid.peppol.process.PredefinedProcessIdentifierManager;

/**
 * A utility interface to resolve a profile ID to a parsed process identifier
 *
 * @author Philip Helger
 */
public interface IProfileIDResolver extends Function <String, IProcessIdentifier>
{
  /* empty */
  @Nonnull
  static IProfileIDResolver getDefault ()
  {
    return sProfileID -> {
      IProcessIdentifier aProcID = PredefinedProcessIdentifierManager.getProcessIdentifierOfID (sProfileID);
      if (aProcID == null)
      {
        // Parse basically
        aProcID = PeppolIdentifierFactory.INSTANCE.parseProcessIdentifier (sProfileID);
      }
      return aProcID;
    };
  }
}
