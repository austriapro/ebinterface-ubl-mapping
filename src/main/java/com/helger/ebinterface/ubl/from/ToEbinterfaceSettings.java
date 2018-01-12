package com.helger.ebinterface.ubl.from;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;

public class ToEbinterfaceSettings implements Serializable
{
  /**
   * Is the OrderReference/ID element mandatory?
   */
  private boolean m_bOrderReferenceIDMandatory = false;
  /**
   * The maximum OrderReference/ID length. All values &lt; 0 mean "no max
   * length".
   */
  private int m_nOrderReferenceIDMaxLen = -1;
  /**
   * Is the delivery date or period required?
   */
  private boolean m_bDeliveryDateMandatory = false;
  /**
   * If no supplier email address is present, should we enforce one?
   */
  private boolean m_bEnforceSupplierEmailAddress = false;
  /**
   * The fake email address used by PEPPOL when no biller email address is in
   * the original XML file
   */
  private String m_sEnforcedSupplierEmailAddress = "no-email-address-provided@peppol.eu";
  /**
   * Is the payment method of an invoice mandatory? This does not apply to
   * credit notes!
   */
  private boolean m_bInvoicePaymentMethodMandatory = false;

  public ToEbinterfaceSettings ()
  {}

  public boolean isOrderReferenceIDMandatory ()
  {
    return m_bOrderReferenceIDMandatory;
  }

  @Nonnull
  public ToEbinterfaceSettings setOrderReferenceIDMandatory (final boolean b)
  {
    m_bOrderReferenceIDMandatory = b;
    return this;
  }

  public int getOrderReferenceMaxLength ()
  {
    return m_nOrderReferenceIDMaxLen;
  }

  public boolean hasOrderReferenceMaxLength ()
  {
    return m_nOrderReferenceIDMaxLen > 0;
  }

  @Nonnull
  public ToEbinterfaceSettings setOrderReferenceIDMaxLength (final int n)
  {
    m_nOrderReferenceIDMaxLen = n;
    return this;
  }

  public boolean isDeliveryDateMandatory ()
  {
    return m_bDeliveryDateMandatory;
  }

  @Nonnull
  public ToEbinterfaceSettings setDeliveryDateMandatory (final boolean b)
  {
    m_bDeliveryDateMandatory = b;
    return this;
  }

  public boolean isEnforceSupplierEmailAddress ()
  {
    return m_bEnforceSupplierEmailAddress;
  }

  @Nonnull
  public ToEbinterfaceSettings setEnforceSupplierEmailAddress (final boolean b)
  {
    m_bEnforceSupplierEmailAddress = b;
    return this;
  }

  @Nonnull
  @Nonempty
  public String getEnforcedSupplierEmailAddress ()
  {
    return m_sEnforcedSupplierEmailAddress;
  }

  @Nonnull
  public ToEbinterfaceSettings setEnforcedSupplierEmailAddress (@Nonnull @Nonempty final String s)
  {
    ValueEnforcer.notEmpty (s, "EmailAddress");
    m_sEnforcedSupplierEmailAddress = s;
    return this;
  }

  public boolean isInvoicePaymentMethodMandatory ()
  {
    return m_bInvoicePaymentMethodMandatory;
  }

  @Nonnull
  public ToEbinterfaceSettings setInvoicePaymentMethodMandatory (final boolean b)
  {
    m_bInvoicePaymentMethodMandatory = b;
    return this;
  }

  @Nonnull
  public static ToEbinterfaceSettings getERechnungGvAtSettings ()
  {
    return new ToEbinterfaceSettings ().setOrderReferenceIDMandatory (true)
                                       .setOrderReferenceIDMaxLength (54)
                                       .setDeliveryDateMandatory (true)
                                       .setEnforceSupplierEmailAddress (true)
                                       .setInvoicePaymentMethodMandatory (true);
  }
}
