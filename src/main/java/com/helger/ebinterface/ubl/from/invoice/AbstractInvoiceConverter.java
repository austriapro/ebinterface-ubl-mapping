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

import com.helger.commons.errorlist.ErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.ubl.from.AbstractConverter;
import com.helger.ebinterface.ubl.from.CPeppolUBL;
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
public abstract class AbstractInvoiceConverter extends AbstractConverter
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
      aTransformationErrorList.addError ("UBLVersionID",
                                         EText.NO_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                         CPeppolUBL.UBL_VERSION_20,
                                                                                         CPeppolUBL.UBL_VERSION_21));
    }
    else
    {
      final String sUBLVersionID = StringHelper.trim (aUBLVersionID.getValue ());
      if (!CPeppolUBL.UBL_VERSION_20.equals (sUBLVersionID) && !CPeppolUBL.UBL_VERSION_21.equals (sUBLVersionID))
      {
        aTransformationErrorList.addError ("UBLVersionID",
                                           EText.INVALID_UBL_VERSION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                sUBLVersionID,
                                                                                                CPeppolUBL.UBL_VERSION_20,
                                                                                                CPeppolUBL.UBL_VERSION_21));
      }
    }

    // Check ProfileID
    IProcessIdentifier aProcID = null;
    final ProfileIDType aProfileID = aUBLInvoice.getProfileID ();
    if (aProfileID == null)
    {
      aTransformationErrorList.addWarning ("ProfileID", EText.NO_PROFILE_ID.getDisplayText (m_aDisplayLocale));
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
        aTransformationErrorList.addWarning ("ProfileID",
                                             EText.INVALID_PROFILE_ID.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                              sProfileID));
      }
    }

    // Check CustomizationID
    // I'm not quite sure whether the document ID or "PEPPOL" should be used!
    // if (false)
    // {
    // final CustomizationIDType aCustomizationID =
    // aUBLInvoice.getCustomizationID ();
    // if (aCustomizationID == null)
    // aTransformationErrorList.addError ("CustomizationID",
    // EText.NO_CUSTOMIZATION_ID.getDisplayText (m_aDisplayLocale));
    // else
    // if (!CPeppolUBL.CUSTOMIZATION_SCHEMEID.equals
    // (aCustomizationID.getSchemeID ()))
    // aTransformationErrorList.addError ("CustomizationID/schemeID",
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
    // aTransformationErrorList.addError ("CustomizationID",
    // EText.INVALID_CUSTOMIZATION_ID.getDisplayTextWithArgs (m_aDisplayLocale,
    // sCustomizationID));
    // }
    // }

    // Invoice type code
    final InvoiceTypeCodeType aInvoiceTypeCode = aUBLInvoice.getInvoiceTypeCode ();
    if (aInvoiceTypeCode == null)
    {
      // None present
      aTransformationErrorList.addWarning ("InvoiceTypeCode",
                                           EText.NO_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                             INVOICE_TYPE_CODE));
    }
    else
    {
      // If one is present, it must match
      final String sInvoiceTypeCode = StringHelper.trim (aInvoiceTypeCode.getValue ());
      if (!INVOICE_TYPE_CODE.equals (sInvoiceTypeCode))
      {
        aTransformationErrorList.addError ("InvoiceTypeCode",
                                           EText.INVALID_INVOICE_TYPECODE.getDisplayTextWithArgs (m_aDisplayLocale,
                                                                                                  sInvoiceTypeCode,
                                                                                                  INVOICE_TYPE_CODE));
      }
    }
  }
}
