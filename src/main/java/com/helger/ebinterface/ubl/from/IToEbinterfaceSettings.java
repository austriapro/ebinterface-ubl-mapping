package com.helger.ebinterface.ubl.from;

import java.io.Serializable;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;

public interface IToEbinterfaceSettings extends Serializable
{
  boolean isOrderReferenceIDMandatory ();

  @CheckForSigned
  int getOrderReferenceMaxLength ();

  boolean hasOrderReferenceMaxLength ();

  boolean isDeliveryDateMandatory ();

  boolean isEnforceSupplierEmailAddress ();

  @Nonnull
  @Nonempty
  String getEnforcedSupplierEmailAddress ();

  boolean isInvoicePaymentMethodMandatory ();
}
