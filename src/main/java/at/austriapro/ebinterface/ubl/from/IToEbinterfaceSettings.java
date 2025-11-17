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
package at.austriapro.ebinterface.ubl.from;

import java.io.Serializable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.CheckForSigned;
import com.helger.annotation.Nonempty;

/**
 * Interface for the settings for the conversion from UBL to ebInterface.
 *
 * @author Philip Helger
 */
public interface IToEbinterfaceSettings extends Serializable
{
  /**
   * @return <code>true</code> if the element "UBLVersionID" is mandatory, <code>false</code> if
   *         not.
   */
  boolean isUBLVersionIDMandatory ();

  /**
   * @return <code>true</code> if the element "ProfileID" is mandatory, <code>false</code> if not.
   */
  boolean isUBLProfileIDMandatory ();

  /**
   * @return The custom process ID resolver to be used. Never <code>null</code>.
   */
  @NonNull
  IProfileIDResolver getProfileIDResolver ();

  /**
   * @return <code>true</code> if the order reference ID is mandatory, <code>false</code> if not.
   */
  boolean isOrderReferenceIDMandatory ();

  /**
   * @return The maximum length of the order reference or a value &le; 0 if no length restrictions
   *         are defined.
   * @see #hasOrderReferenceMaxLength()
   */
  @CheckForSigned
  int getOrderReferenceMaxLength ();

  default boolean hasOrderReferenceMaxLength ()
  {
    return getOrderReferenceMaxLength () > 0;
  }

  /**
   * @return <code>true</code> if the delivery date is mandatory, <code>false</code> if not.
   */
  boolean isDeliveryDateMandatory ();

  /**
   * @return <code>true</code> if the supplier email address should be enforced, if it is missing.
   * @see #getEnforcedSupplierEmailAddress()
   */
  boolean isEnforceSupplierEmailAddress ();

  /**
   * @return The email address to be used when {@link #isEnforceSupplierEmailAddress()} is
   *         <code>true</code> and an email address is missing in the invoice.
   */
  @NonNull
  @Nonempty
  String getEnforcedSupplierEmailAddress ();

  /**
   * @return The fallback "BillersInvoiceRecipientID" for ebInterface 4.0 only.
   */
  @Nullable
  String getFallbackBillersInvoiceRecipientID ();

  /**
   * @return <code>true</code> if invalid line numbers (row index &lt; 1) leads to an error,
   *         <code>false</code> if not.
   */
  boolean isErrorOnPositionNumber ();

  /**
   * @return <code>true</code> if the payment method is mandatory, <code>false</code> if not.
   */
  boolean isInvoicePaymentMethodMandatory ();

  /**
   * @return <code>true</code> if prepaid amount is supported, <code>false</code> if not.
   * @since v5.3.3
   */
  boolean isPrepaidAmountSupported ();
}
