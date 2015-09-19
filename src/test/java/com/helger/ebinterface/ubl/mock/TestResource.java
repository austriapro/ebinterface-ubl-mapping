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
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.string.ToStringGenerator;

public class TestResource
{
  private final IReadableResource m_aRes;
  private final Set <ErrorType> m_aExpectedErrors = new HashSet <ErrorType> ();

  public TestResource (@Nonnull final IReadableResource aRes,
                       @Nullable final Set <ErrorType> aExpectedErrors)
  {
    ValueEnforcer.notNull (aRes, "Resource");

    m_aRes = aRes;
    if (aExpectedErrors != null)
      m_aExpectedErrors.addAll (aExpectedErrors);
  }

  /**
   * @return The XML resource path
   */
  @Nonnull
  public IReadableResource getResource ()
  {
    return m_aRes;
  }

  /**
   * @return The filename of the underlying resources
   */
  @Nonnull
  public String getFilename ()
  {
    return m_aRes.getPath ();
  }

  /**
   * @return The expected validation errors
   */
  @Nonnull
  @ReturnsMutableCopy
  public Set <ErrorType> getAllExpectedErrors ()
  {
    return CollectionHelper.newSet (m_aExpectedErrors);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("resource", m_aRes)
                                       .append ("expectedErrors", m_aExpectedErrors)
                                       .toString ();
  }
}
