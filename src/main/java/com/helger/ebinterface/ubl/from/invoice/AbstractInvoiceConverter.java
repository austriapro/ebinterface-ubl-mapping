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
package com.helger.ebinterface.ubl.from.invoice;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.ubl.from.AbstractToEbInterfaceConverter;
import com.helger.peppol.identifier.generic.process.IProcessIdentifier;
import com.helger.peppol.identifier.peppol.process.PeppolProcessIdentifier;
import com.helger.peppol.identifier.peppol.process.PredefinedProcessIdentifierManager;

import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InvoiceTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.ProfileIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.UBLVersionIDType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Base class for UBL 2.1 Invoice to ebInterface converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractInvoiceConverter extends AbstractToEbInterfaceConverter
{
  /**
   * Constructor
   *
   * @param aDisplayLocale
   *        The locale for error messages. May not be <code>null</code>.
   * @param aContentLocale
   *        The locale for the created ebInterface files. May not be
   *        <code>null</code>.
   * @param bStrictERBMode
   *        <code>true</code> if E-RECHNUNG.GV.AT specific checks should be
   *        performed
   */
  public AbstractInvoiceConverter (@Nonnull final Locale aDisplayLocale,
                                   @Nonnull final Locale aContentLocale,
                                   final boolean bStrictERBMode)
  {
    super (aDisplayLocale, aContentLocale, bStrictERBMode);
  }

  /**
   * Check if the passed UBL invoice is transformable
   *
   * @param aUBLInvoice
   *        The UBL invoice to check
   */
  protected final void _checkConsistency (@Nonnull final InvoiceType aUBLInvoice,
                                          @Nonnull final ErrorList aTransformationErrorList)
  {
    // Check UBLVersionID
    final UBLVersionIDType aUBLVersionID = aUBLInvoice.getUBLVersionID ();
    if (aUBLVersionID == null)
    {
      aTransformationErrorList.add (SingleError.builderError ()
                                               .setErrorFieldName ("UBLVersionID")
                                               .setErrorText (EText.NO_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                              UBL_VERSION_20,
                                                                                                              UBL_VERSION_21))
                                               .build ());
    }
    else
    {
      final String sUBLVersionID = StringHelper.trim (aUBLVersionID.getValue ());
      if (!UBL_VERSION_20.equals (sUBLVersionID) && !UBL_VERSION_21.equals (sUBLVersionID))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("UBLVersionID")
                                                 .setErrorText (EText.INVALID_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                     sUBLVersionID,
                                                                                                                     UBL_VERSION_20,
                                                                                                                     UBL_VERSION_21))
                                                 .build ());
      }
    }

    // Check ProfileID
    IProcessIdentifier aProcID = null;
    final ProfileIDType aProfileID = aUBLInvoice.getProfileID ();
    if (aProfileID == null)
    {
      aTransformationErrorList.add (SingleError.builderWarn ()
                                               .setErrorFieldName ("ProfileID")
                                               .setErrorText (EText.NO_PROFILE_ID.getDisplayText (m_aDisplayLocale))
                                               .build ());
    }
    else
    {
      final String sProfileID = StringHelper.trim (aProfileID.getValue ());
      aProcID = PredefinedProcessIdentifierManager.getProcessIdentifierOfID (sProfileID);
      if (aProcID == null)
      {
        // Parse basically
        aProcID = PeppolProcessIdentifier.createWithDefaultScheme (sProfileID);
      }

      if (aProcID == null)
      {
        aTransformationErrorList.add (SingleError.builderWarn ()
                                                 .setErrorFieldName ("ProfileID")
                                                 .setErrorText (EText.INVALID_PROFILE_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                 sProfileID))
                                                 .build ());
      }
    }

    // Check CustomizationID
    // I'm not quite sure whether the document ID or "PEPPOL" should be used!
    // if (false)
    // {
    // final CustomizationIDType aCustomizationID =
    // aUBLInvoice.getCustomizationID ();
    // if (aCustomizationID == null)
    // aTransformationErrorList.add (SingleError.builderError
    // ().setErrorFieldName ("CustomizationID",
    // EText.NO_CUSTOMIZATION_ID.getDisplayText (m_aDisplayLocale));
    // else
    // if (!CPeppolUBL.CUSTOMIZATION_SCHEMEID.equals
    // (aCustomizationID.getSchemeID ()))
    // aTransformationErrorList.add (SingleError.builderError
    // ().setErrorFieldName ("CustomizationID/schemeID",
    // EText.INVALID_CUSTOMIZATION_SCHEME_ID.getDisplayTextWithArgs
    // (m_aDisplayLocale,
    // aCustomizationID.getSchemeID (),
    // CPeppolUBL.CUSTOMIZATION_SCHEMEID));
    // else
    // if (aProcID != null)
    // {
    // final String sCustomizationID = StringHelper.trim
    // (aCustomizationID.getValue ());
    // IPeppolPredefinedDocumentTypeIdentifier aMatchingDocID = null;
    // for (final IPeppolPredefinedDocumentTypeIdentifier aDocID :
    // aProcID.getDocumentTypeIdentifiers ())
    // if (aDocID.getAsUBLCustomizationID ().equals (sCustomizationID))
    // {
    // // We found a match
    // aMatchingDocID = aDocID;
    // break;
    // }
    // if (aMatchingDocID == null)
    // aTransformationErrorList.add (SingleError.builderError
    // ().setErrorFieldName ("CustomizationID",
    // EText.INVALID_CUSTOMIZATION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
    // sCustomizationID));
    // }
    // }

    // Invoice type code
    final InvoiceTypeCodeType aInvoiceTypeCode = aUBLInvoice.getInvoiceTypeCode ();
    if (aInvoiceTypeCode == null)
    {
      // None present
      aTransformationErrorList.add (SingleError.builderWarn ()
                                               .setErrorFieldName ("InvoiceTypeCode")
                                               .setErrorText (EText.NO_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                INVOICE_TYPE_CODE))
                                               .build ());
    }
    else
    {
      // If one is present, it must match
      final String sInvoiceTypeCode = StringHelper.trim (aInvoiceTypeCode.getValue ());
      if (!INVOICE_TYPE_CODE.equals (sInvoiceTypeCode))
      {
        aTransformationErrorList.add (SingleError.builderError ()
                                                 .setErrorFieldName ("InvoiceTypeCode")
                                                 .setErrorText (EText.INVALID_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                                       sInvoiceTypeCode,
                                                                                                                       INVOICE_TYPE_CODE))
                                                 .build ());
      }
    }
  }
}
