/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2017 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.from;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v42.Ebi42DocumentTypeType;
import com.helger.ebinterface.v42.Ebi42InvoiceType;
import com.helger.ebinterface.v42.Ebi42RelatedDocumentType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BillingReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentDescriptionType;

/**
 * Base class for PEPPOL UBL to ebInterface 4.2 converter
 *
 * @author philip
 */
@Immutable
public abstract class AbstractToEbInterface42Converter extends AbstractToEbInterfaceConverter
{
  public AbstractToEbInterface42Converter (@Nonnull final Locale aDisplayLocale,
                                           @Nonnull final Locale aContentLocale,
                                           final boolean bStrictERBMode)
  {
    super (aDisplayLocale, aContentLocale, bStrictERBMode);
  }

  @Nullable
  protected static final Ebi42DocumentTypeType getAsDocumentTypeType (@Nullable final String... aValues)
  {
    if (aValues != null)
      for (final String s : aValues)
        if (s != null)
          try
          {
            // The first match wins
            return Ebi42DocumentTypeType.fromValue (s);
          }
          catch (final IllegalArgumentException ex)
          {
            // Ignore
          }
    return null;
  }

  protected static void convertRelatedDocuments (@Nonnull final List <BillingReferenceType> aUBLBillingReferences,
                                                 @Nonnull final Ebi42InvoiceType aEbiDoc)
  {
    for (final BillingReferenceType aUBLBillingReference : aUBLBillingReferences)
    {
      if (aUBLBillingReference.getInvoiceDocumentReference () != null &&
          aUBLBillingReference.getInvoiceDocumentReference ().getIDValue () != null)
      {
        final Ebi42RelatedDocumentType aEbiRelatedDocument = new Ebi42RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getInvoiceDocumentReference ().getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getInvoiceDocumentReference ().getIssueDateValue ());
        aEbiRelatedDocument.setDocumentType (Ebi42DocumentTypeType.INVOICE);
        aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
      }
      else
        if (aUBLBillingReference.getCreditNoteDocumentReference () != null &&
            aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue () != null)
        {
          final Ebi42RelatedDocumentType aEbiRelatedDocument = new Ebi42RelatedDocumentType ();
          aEbiRelatedDocument.setInvoiceNumber (aUBLBillingReference.getCreditNoteDocumentReference ().getIDValue ());
          aEbiRelatedDocument.setInvoiceDate (aUBLBillingReference.getCreditNoteDocumentReference ()
                                                                  .getIssueDateValue ());
          aEbiRelatedDocument.setDocumentType (Ebi42DocumentTypeType.CREDIT_MEMO);
          aEbiDoc.addRelatedDocument (aEbiRelatedDocument);
        }
      // Ignore other values
    }
  }

  protected static void convertReferencedDocuments (@Nonnull final List <DocumentReferenceType> aUBLDocumentReferences,
                                                    @Nonnull final Ebi42InvoiceType aEbiDoc)
  {
    for (final DocumentReferenceType aUBLDocumentReference : aUBLDocumentReferences)
      if (StringHelper.hasText (aUBLDocumentReference.getIDValue ()) && aUBLDocumentReference.getAttachment () == null)
      {
        final Ebi42RelatedDocumentType aEbiRelatedDocument = new Ebi42RelatedDocumentType ();
        aEbiRelatedDocument.setInvoiceNumber (aUBLDocumentReference.getIDValue ());
        aEbiRelatedDocument.setInvoiceDate (aUBLDocumentReference.getIssueDateValue ());
        final ICommonsList <String> aComments = new CommonsArrayList <> ();
        for (final DocumentDescriptionType aUBLDocDesc : aUBLDocumentReference.getDocumentDescription ())
          aComments.add (aUBLDocDesc.getValue ());
        aEbiRelatedDocument.setComment (StringHelper.getImplodedNonEmpty ('\n', aComments));
        if (aUBLDocumentReference.getDocumentTypeCode () != null)
        {
          aEbiRelatedDocument.setDocumentType (getAsDocumentTypeType (aUBLDocumentReference.getDocumentTypeCode ()
                                                                                           .getName (),
                                                                      aUBLDocumentReference.getDocumentTypeCodeValue ()));
        }
        aEbiDoc.getRelatedDocument ().add (aEbiRelatedDocument);
      }
  }
}
