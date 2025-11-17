/*
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2025 AUSTRIAPRO - www.austriapro.at
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
package at.austriapro.ebinterface.ubl.helper;

import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.concurrent.Immutable;
import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsTreeMap;
import com.helger.collection.commons.ICommonsCollection;
import com.helger.collection.commons.ICommonsMap;
import com.helger.text.locale.LocaleCache;
import com.helger.text.locale.LocaleHelper;
import com.helger.text.locale.country.CountryCache;

@Immutable
public final class MultilingualCountryCache
{
  // Cache all country codes in all display languages
  private static ICommonsMap <String, Locale> s_aNameToCodeMap = new CommonsTreeMap <> ();

  @NonNull
  private static String _unify (@NonNull final String s)
  {
    return s.toLowerCase (Locale.US);
  }

  static
  {
    final Locale aDefaultDisplayLocale = Locale.getDefault (Locale.Category.DISPLAY);

    final ICommonsCollection <Locale> aAllLocales = LocaleCache.getInstance ().getAllLocales ();
    for (final Locale aCountryLocale : aAllLocales)
    {
      s_aNameToCodeMap.put (_unify (aCountryLocale.getDisplayCountry (aDefaultDisplayLocale)), aCountryLocale);
      for (final Locale aDisplayLocale : aAllLocales)
        s_aNameToCodeMap.put (_unify (aCountryLocale.getDisplayCountry (aDisplayLocale)), aCountryLocale);
    }
  }

  private MultilingualCountryCache ()
  {}

  @Nullable
  public static String getRealCountryCode (@Nullable final String sCountry)
  {
    // Empty
    if (StringHelper.isEmpty (sCountry))
      return null;

    // Empty after trim?
    final String sRealCountry = sCountry.trim ();
    if (sRealCountry.length () == 0)
      return null;

    // Is the value already a code?
    if (CountryCache.getInstance ().containsCountry (sRealCountry))
      return LocaleHelper.getValidCountryCode (sRealCountry);

    // Is it a country display name?
    final Locale aCountry = s_aNameToCodeMap.get (_unify (sRealCountry));
    if (aCountry != null)
      return aCountry.getCountry ();

    // absolutely not found
    return null;
  }
}
