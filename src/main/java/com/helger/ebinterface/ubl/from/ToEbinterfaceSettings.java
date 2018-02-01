/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015-2018 AUSTRIAPRO - www.austriapro.at
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

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;

public class ToEbinterfaceSettings implements IToEbinterfaceSettings
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

  @Override
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

  @Override
  public int getOrderReferenceMaxLength ()
  {
    return m_nOrderReferenceIDMaxLen;
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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
    return new ToEbinterfaceSettings ().setOrderReferenceIDMandatory (true)
                                       .setOrderReferenceIDMaxLength (54)
                                       .setDeliveryDateMandatory (true)
                                       .setEnforceSupplierEmailAddress (true)
                                       .setInvoicePaymentMethodMandatory (true);
  }
}
