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
package com.helger.ebinterface.ubl.mock;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.resource.IReadableResource;

public enum ETestFileType
{
  CREDITNOTE ("test-creditnotes"),
  INVOICE ("test-invoices");

  private final String m_sDirName;

  private ETestFileType (@Nonnull @Nonempty final String sDirName)
  {
    m_sDirName = "/" + sDirName;
  }

  @Nonnull
  public IReadableResource getSuccessResource (@Nonnull @Nonempty final String sFilename)
  {
    return new ClassPathResource (m_sDirName + "/success/" + sFilename);
  }

  @Nonnull
  public IReadableResource getErrorResource (@Nonnull @Nonempty final String sFilename)
  {
    return new ClassPathResource (m_sDirName + "/error/" + sFilename);
  }
}
