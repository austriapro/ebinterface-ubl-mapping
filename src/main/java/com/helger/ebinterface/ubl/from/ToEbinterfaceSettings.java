/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2019 AUSTRIAPRO - www.austriapro.at
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;

public class ToEbinterfaceSettings implements IToEbinterfaceSettings
{
  /**
   * Is the "UBLVersionID" element mandatory?
   */
  private boolean m_bUBLVersionIDMandatory = false;
  /**
   * Is the "ProfileID" element mandatory?
   */
  private boolean m_bUBLProfileIDMandatory = true;
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
   * The fallback billers invoice recipient ID to be used if none is present
   * (for ebi 4.0)
   */
  private String m_sFallbackBillersInvoiceRecipientID = null;
  /**
   * Emit an error, if the item position number is &lt; 1.
   */
  private boolean m_bErrorOnPositionNumber = false;
  /**
   * Is the payment method of an invoice mandatory? This does not apply to
   * credit notes!
   */
  private boolean m_bInvoicePaymentMethodMandatory = false;

  public ToEbinterfaceSettings ()
  {}

  public boolean isUBLVersionIDMandatory ()
  {
    return m_bUBLVersionIDMandatory;
  }

  @Nonnull
  public ToEbinterfaceSettings setUBLVersionIDMandatory (final boolean bUBLVersionIDMandatory)
  {
    m_bUBLVersionIDMandatory = bUBLVersionIDMandatory;
    return this;
  }

  public boolean isUBLProfileIDMandatory ()
  {
    return m_bUBLProfileIDMandatory;
  }

  @Nonnull
  public ToEbinterfaceSettings setUBLProfileIDMandatory (final boolean bUBLProfileIDMandatory)
  {
    m_bUBLProfileIDMandatory = bUBLProfileIDMandatory;
    return this;
  }

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

  @Nullable
  public String getFallbackBillersInvoiceRecipientID ()
  {
    return m_sFallbackBillersInvoiceRecipientID;
  }

  @Nonnull
  public ToEbinterfaceSettings setFallbackBillersInvoiceRecipientID (@Nullable final String sFallbackBillersInvoiceRecipientID)
  {
    m_sFallbackBillersInvoiceRecipientID = sFallbackBillersInvoiceRecipientID;
    return this;
  }

  public boolean isErrorOnPositionNumber ()
  {
    return m_bErrorOnPositionNumber;
  }

  @Nonnull
  public ToEbinterfaceSettings setErrorOnPositionNumber (final boolean bErrorOnPositionNumber)
  {
    m_bErrorOnPositionNumber = bErrorOnPositionNumber;
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

  /**
   * @return Settings similar to what eRechnung.gv.at uses - mainly for testing
   *         purposes.
   */
  @Nonnull
  public static ToEbinterfaceSettings getERechnungGvAtSettings ()
  {
    return new ToEbinterfaceSettings ().setUBLVersionIDMandatory (false)
                                       .setUBLProfileIDMandatory (true)
                                       .setOrderReferenceIDMandatory (true)
                                       .setOrderReferenceIDMaxLength (54)
                                       .setDeliveryDateMandatory (true)
                                       .setEnforceSupplierEmailAddress (true)
                                       .setInvoicePaymentMethodMandatory (true);
  }
}
