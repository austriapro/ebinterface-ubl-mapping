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
package at.austriapro.ebinterface.ubl.from.helper;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class contains the data of a single TaxtCategory. That is required when
 * the details items don't have a percentage and the percentage values must be
 * evaluated from the tax subtotal elements.
 *
 * @author philip
 */
@Immutable
public final class TaxCategoryKey
{
  private final SchemedID m_aTaxCategoryScheme;
  private final SchemedID m_aTaxCategoryID;

  public TaxCategoryKey (@Nonnull final SchemedID aTaxCategoryScheme, @Nonnull final SchemedID aTaxCategoryID)
  {
    ValueEnforcer.notNull (aTaxCategoryScheme, "TaxCategoryScheme");
    ValueEnforcer.notNull (aTaxCategoryID, "TaxCategoryID");
    m_aTaxCategoryScheme = aTaxCategoryScheme;
    m_aTaxCategoryID = aTaxCategoryID;
  }

  @Nonnull
  public SchemedID getTaxCategoryScheme ()
  {
    return m_aTaxCategoryScheme;
  }

  @Nonnull
  public SchemedID getTaxCategoryID ()
  {
    return m_aTaxCategoryID;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final TaxCategoryKey rhs = (TaxCategoryKey) o;
    return m_aTaxCategoryScheme.equals (rhs.m_aTaxCategoryScheme) && m_aTaxCategoryID.equals (rhs.m_aTaxCategoryID);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aTaxCategoryScheme).append (m_aTaxCategoryID).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("scheme", m_aTaxCategoryScheme)
                                       .append ("ID", m_aTaxCategoryID)
                                       .getToString ();
  }
}
