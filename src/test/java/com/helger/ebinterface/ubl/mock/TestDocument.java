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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.string.ToStringGenerator;

public class TestDocument
{
  private final String m_sFilename;
  private final Set <ErrorType> m_aExpectedErrors = new HashSet <ErrorType> ();

  public TestDocument (@Nonnull final String sFilename, @Nullable final ErrorType... aExpectedErrors)
  {
    ValueEnforcer.notNull (sFilename, "Filename");

    m_sFilename = sFilename;
    if (aExpectedErrors != null)
      for (final ErrorType aExpectedError : aExpectedErrors)
        if (aExpectedError != null)
          m_aExpectedErrors.add (aExpectedError);
  }

  @Nonnull
  public String getFilename ()
  {
    return m_sFilename;
  }

  @Nonnull
  @ReturnsMutableCopy
  public Set <ErrorType> getAllExpectedErrors ()
  {
    return CollectionHelper.newSet (m_aExpectedErrors);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("filename", m_sFilename)
                                       .append ("expectedErrors", m_aExpectedErrors)
                                       .toString ();
  }
}
