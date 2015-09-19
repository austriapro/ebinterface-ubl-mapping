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

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.EErrorLevel;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

public class ErrorType implements Comparable <ErrorType>
{
  private final EErrorLevel m_eLevel;
  private final String m_sErrorCode;

  private ErrorType (@Nonnull final EErrorLevel eLevel, @Nonnull @Nonempty final String sErrorCode)
  {
    ValueEnforcer.notNull (eLevel, "Level");
    ValueEnforcer.notEmpty (sErrorCode, "ErrorCode");
    m_eLevel = eLevel;
    m_sErrorCode = sErrorCode;
  }

  @Nonnull
  public EErrorLevel getLevel ()
  {
    return m_eLevel;
  }

  @Nonnull
  @Nonempty
  public String getErrorCode ()
  {
    return m_sErrorCode;
  }

  public int compareTo (@Nonnull final ErrorType rhs)
  {
    int i = m_eLevel.compareTo (rhs.m_eLevel);
    if (i == 0)
      i = m_sErrorCode.compareTo (rhs.m_sErrorCode);
    return i;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final ErrorType rhs = (ErrorType) o;
    return m_eLevel.equals (rhs.m_eLevel) && m_sErrorCode.equals (rhs.m_sErrorCode);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_eLevel).append (m_sErrorCode).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (null).append ("level", m_eLevel).append ("errorCode", m_sErrorCode).toString ();
  }

  @Nonnull
  public static ErrorType createError (@Nonnull @Nonempty final String sErrorCode)
  {
    return new ErrorType (EErrorLevel.FATAL_ERROR, sErrorCode);
  }

  @Nonnull
  public static ErrorType createWarning (@Nonnull @Nonempty final String sErrorCode)
  {
    return new ErrorType (EErrorLevel.WARN, sErrorCode);
  }
}
