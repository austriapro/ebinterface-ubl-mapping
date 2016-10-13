/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2016 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.to;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.Translatable;
import com.helger.commons.text.IMultilingualText;
import com.helger.commons.text.display.IHasDisplayTextWithArgs;
import com.helger.commons.text.resolve.DefaultTextResolver;
import com.helger.commons.text.util.TextHelper;
import com.helger.ebinterface.ubl.AbstractConverter;

/**
 * Base class for ebInterface to PEPPOL UBL converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractToUBLConverter extends AbstractConverter
{
  @Translatable
  public static enum EText implements IHasDisplayTextWithArgs
  {
    ;

    private final IMultilingualText m_aTP;

    private EText (@Nonnull final String sDE, @Nonnull final String sEN)
    {
      m_aTP = TextHelper.create_DE_EN (sDE, sEN);
    }

    @Nullable
    public String getDisplayText (@Nonnull final Locale aContentLocale)
    {
      return DefaultTextResolver.getTextStatic (this, m_aTP, aContentLocale);
    }
  }

  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be
   *        <code>null</code>.
   */
  public AbstractToUBLConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  protected static final boolean isSupportedTaxSchemeSchemeID (@Nullable final String sUBLTaxSchemeSchemeID)
  {
    return sUBLTaxSchemeSchemeID == null ||
           sUBLTaxSchemeSchemeID.equals (SUPPORTED_TAX_SCHEME_SCHEME_ID) ||
           sUBLTaxSchemeSchemeID.equals (SUPPORTED_TAX_SCHEME_SCHEME_ID_SUBSET);
  }
}
