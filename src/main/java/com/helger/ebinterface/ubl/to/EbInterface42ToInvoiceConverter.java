package com.helger.ebinterface.ubl.to;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.commons.string.StringHelper;
import com.helger.ebinterface.v42.Ebi42CancelledOriginalDocumentType;
import com.helger.ebinterface.v42.Ebi42DeliveryType;
import com.helger.ebinterface.v42.Ebi42InvoiceType;
import com.helger.ebinterface.v42.Ebi42RelatedDocumentType;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DeliveryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentDescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.InvoiceTypeCodeType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Convert an ebInterface invoice to a UBL invoice
 *
 * @author Philip Helger
 */
public class EbInterface42ToInvoiceConverter extends AbstractToUBLConverter
{
  public EbInterface42ToInvoiceConverter (@Nonnull final Locale aDisplayLocale, @Nonnull final Locale aContentLocale)
  {
    super (aDisplayLocale, aContentLocale);
  }

  @Nonnull
  public InvoiceType convertInvoice (@Nonnull final Ebi42InvoiceType aEbiDoc)
  {
    final InvoiceType aUBLDoc = new InvoiceType ();
    aUBLDoc.setUBLVersionID (UBL_VERSION_21);

    // Attributes
    // GeneratingSystem cannot be mapped
    aUBLDoc.setInvoiceTypeCode (getTypeCode (aEbiDoc.getDocumentType (), () -> new InvoiceTypeCodeType ()));
    aUBLDoc.setDocumentCurrencyCode (aEbiDoc.getInvoiceCurrency ());
    // ManualProcessing cannot be mapped
    // DocumentTitle is not mapped
    // Language is not mapped
    if (aEbiDoc.isIsDuplicate () != null)
      aUBLDoc.setCopyIndicator (aEbiDoc.isIsDuplicate ().booleanValue ());

    // Elements
    aUBLDoc.setID (aEbiDoc.getInvoiceNumber ());
    aUBLDoc.setIssueDate (aEbiDoc.getInvoiceDate ());

    // Handle CancelledOriginalDocument
    {
      final Ebi42CancelledOriginalDocumentType aEbiCancelledDoc = aEbiDoc.getCancelledOriginalDocument ();
      if (aEbiCancelledDoc != null)
      {
        final DocumentReferenceType aUBLDocRef = new DocumentReferenceType ();
        aUBLDocRef.setID (aEbiCancelledDoc.getInvoiceNumber ());
        aUBLDocRef.setIssueDate (aEbiCancelledDoc.getInvoiceDate ());
        aUBLDocRef.setDocumentType ("CancelledOriginalDocument");
        aUBLDocRef.setDocumentTypeCode (getTypeCode (aEbiCancelledDoc.getDocumentType (),
                                                     () -> new DocumentTypeCodeType ()));
        if (StringHelper.hasText (aEbiCancelledDoc.getComment ()))
          aUBLDocRef.addDocumentDescription (new DocumentDescriptionType (aEbiCancelledDoc.getComment ()));
        aUBLDoc.addAdditionalDocumentReference (aUBLDocRef);
      }
    }

    // Handle RelatedDocument
    {
      for (final Ebi42RelatedDocumentType aEbiRelatedItem : aEbiDoc.getRelatedDocument ())
      {
        final DocumentReferenceType aUBLDocRef = new DocumentReferenceType ();
        aUBLDocRef.setID (aEbiRelatedItem.getInvoiceNumber ());
        aUBLDocRef.setIssueDate (aEbiRelatedItem.getInvoiceDate ());
        aUBLDocRef.setDocumentType ("RelatedDocument");
        aUBLDocRef.setDocumentTypeCode (getTypeCode (aEbiRelatedItem.getDocumentType (),
                                                     () -> new DocumentTypeCodeType ()));
        if (StringHelper.hasText (aEbiRelatedItem.getComment ()))
          aUBLDocRef.addDocumentDescription (new DocumentDescriptionType (aEbiRelatedItem.getComment ()));
        aUBLDoc.addAdditionalDocumentReference (aUBLDocRef);
      }
    }

    // Handle Delivery
    {
      final Ebi42DeliveryType aEbiDelivery = aEbiDoc.getDelivery ();
      if (aEbiDelivery != null)
      {
        final DeliveryType aUBLDelivery = new DeliveryType ();
        aUBLDelivery.setID (aEbiDelivery.getDeliveryID ());
        if (aEbiDelivery.getDate () != null)
          aUBLDelivery.setActualDeliveryDate (aEbiDelivery.getDate ());
        else
          if (aEbiDelivery.getPeriod () != null)
          {
            // Delivery period is mapped to invoice period
            final PeriodType aUBLPeriod = new PeriodType ();
            aUBLPeriod.setStartDate (aEbiDelivery.getPeriod ().getFromDate ());
            aUBLPeriod.setEndDate (aEbiDelivery.getPeriod ().getToDate ());
            if (aUBLDoc.getInvoicePeriodCount () == 0)
              aUBLDoc.addInvoicePeriod (aUBLPeriod);
            else
              aUBLDelivery.setRequestedDeliveryPeriod (aUBLPeriod);
          }
        aUBLDelivery.setDeliveryAddress (convertAddress (aEbiDelivery.getAddress ()));
        aUBLDoc.addDelivery (aUBLDelivery);
      }
    }

    return aUBLDoc;
  }
}
