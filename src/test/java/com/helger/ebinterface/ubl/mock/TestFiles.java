/**
 * Copyright (c) 2010-2015 Bundesrechenzentrum GmbH - www.brz.gv.at
 * Copyright (c) 2015 AUSTRIAPRO - www.austriapro.at
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
package com.helger.ebinterface.ubl.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.io.resource.IReadableResource;

/**
 * Contains the utility methods to retrieve the test files for a certain
 * document type.
 *
 * @author PEPPOL.AT, BRZ, Philip Helger
 */
@Immutable
public final class TestFiles
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (TestFiles.class);
  private static final String [] CREDITNOTES_SUCCESS = new String [] { "BII05 TRDM014 example is.xml" };
  private static final String [] CREDITNOTES_AT_SUCCESS = new String [] { "atgov_BIS5aCreditNote.xml",
                                                                          "atgov-t14-BIS5A-valid.xml",
                                                                          "atgov-t14-BIS5A-with-all-elements.xml" };
  private static final String [] INVOICES_SUCCESS = new String [] { "BII04 minimal invoice example 03.xml",
                                                                    "BII04 minimal invoice wo addr id.xml",
                                                                    "BII04 minimal VAT invoice example 02.xml",
                                                                    "BII04 XML example full core data 01.xml",
                                                                    "BII04 XML example full core data 02.xml",
                                                                    "invoice-it-c98fe7f7-8b46-4972-b61b-3b8824f16658.xml",
                                                                    "invoice-it-uuid8a69941e-52ae-4cbb-b284-a3a78fb89d07.xml",
                                                                    "PEP BII04 minimal invoice example 03.xml",
                                                                    "PEP BII04 minimal Reverce Charge VAT invoice example 01.xml",
                                                                    "PEP BII04 minimal Reverce Charge VAT invoice example no line 01.xml",
                                                                    "PEP BII04 minimal VAT invoice example 02.xml",
                                                                    "SubmitInvoice.008660-AA.b1478257-5bd1-4756-bd20-3262afb22923.xml",
                                                                    "TC10.3.TS1.xml",
                                                                    "TC10.4.TS1.xml",
                                                                    "TC10.15.TS1.xml",
                                                                    "test-invoice.xml" };
  private static final String [] INVOICES_AT_SUCCESS = new String [] { "atgov_BIS4aInvoice.xml",
                                                                       "atgov_BIS5aInvoice.xml",
                                                                       "atgov-t10-BIS4A-valid.xml",
                                                                       "atgov-t10-BIS5A-valid.xml",
                                                                       "atgov-ubl-42-8.xml" };
  public static final TestDocument [] INVOICES_ERROR = new TestDocument [] { new TestDocument ("ERR-2 BII04 minimal invoice example 02.xml",
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R002"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003"),
                                                                                               ErrorType.createWarning ("BIICORE-T10-R392")),
                                                                             new TestDocument ("ERR-3 BII04 minimal VAT invoice example 02.xml",
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R003"),
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R009"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("ERR-4 BII04 minimal invoice example 02.xml",
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R004"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R002"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("ERR-5 BII04 minimal VAT invoice example 02.xml",
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R005"),
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R009")),
                                                                             new TestDocument ("ERR-9 BII04 minimal VAT invoice example 02.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R009"),
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R009")),
                                                                             new TestDocument ("ERR-10 BII04 minimal VAT invoice example 02.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R010"),
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R009"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("ERR-11 BII04 minimal invoice example 02.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R011"),
                                                                                               ErrorType.createError ("BIIRULE-T10-R012"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("ERR-13 BII04 minimal invoice example 02.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R013"),
                                                                                               ErrorType.createError ("BIIRULE-T10-R017"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("ERR-18 BII04 minimal invoice example 02.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R018"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("ERR-19 BII04 minimal invoice example 02.xml",
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R019"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("ERR-20 BII04 reverse charge invoice example 01.xml",
                                                                                               ErrorType.createError ("EUGEN-T10-R015"),
                                                                                               ErrorType.createError ("EUGEN-T10-R016"),
                                                                                               ErrorType.createError ("EUGEN-T10-R017"),
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R003"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R009")),
                                                                             new TestDocument ("TC10.1.TS1.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R052"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.2.TS1.xml",
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.5.TS1.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R011"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.6.TS1.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R013"),
                                                                                               ErrorType.createError ("BIIRULE-T10-R017"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.7.TS1.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R009"),
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.8.TS1.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R010"),
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.9.TS1.xml",
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R004"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R002"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.10.TS1.xml",
                                                                                               ErrorType.createWarning ("BIICORE-T10-R392"),
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R002"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.11.TS1.xml",
                                                                                               ErrorType.createError ("BIIRULE-T10-R018"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.12.TS1.xml",
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R019"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.13.TS1.xml",
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R005"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.14.TS1.xml",
                                                                                               ErrorType.createError ("EUGEN-T10-R008"),
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R003"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R001"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                             new TestDocument ("TC10.16.TS1.xml"),
                                                                             new TestDocument ("TC10.17.TS1.xml",
                                                                                               ErrorType.createError ("EUGEN-T10-R015"),
                                                                                               ErrorType.createError ("EUGEN-T10-R016"),
                                                                                               ErrorType.createError ("EUGEN-T10-R017"),
                                                                                               ErrorType.createWarning ("BIIRULE-T10-R003"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R009")),
                                                                             new TestDocument ("TC10.18.TS1.xml",
                                                                                               ErrorType.createError ("PCL-010-007"),
                                                                                               ErrorType.createError ("PCL-010-008"),
                                                                                               ErrorType.createWarning ("BIICORE-T10-R114"),
                                                                                               ErrorType.createWarning ("BIICORE-T10-R185"),
                                                                                               ErrorType.createWarning ("EUGEN-T10-R023")) };
  private static final TestDocument [] INVOICES_AT_ERROR = new TestDocument [] { new TestDocument ("atnat-t10-fail-r001.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R001"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atnat-t10-fail-r002a.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R002"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323")),
                                                                                 new TestDocument ("atnat-t10-fail-r002b.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R002"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323")),
                                                                                 new TestDocument ("atnat-t10-fail-r003.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R003"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atnat-t10-fail-r004.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R004"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createError ("EUGEN-T10-R015")),
                                                                                 new TestDocument ("atnat-t10-fail-r005.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R005"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003"),
                                                                                                   ErrorType.createError ("EUGEN-T10-R007")),
                                                                                 new TestDocument ("atnat-t10-fail-r006.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R006"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createError ("EUGEN-T10-R018")),
                                                                                 new TestDocument ("atnat-t10-fail-r007.xml",
                                                                                                   ErrorType.createError ("ATNAT-T10-R007"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r001.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R001"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r002.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R002"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323")),
                                                                                 new TestDocument ("atgov-t10-fail-r003.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R003"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r004.xml",
                                                                                                   ErrorType.createWarning ("ATGOV-T10-R004"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r005.xml",
                                                                                                   ErrorType.createWarning ("ATGOV-T10-R005"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r007a.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R007"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r007b.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R007"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R004"),
                                                                                                   ErrorType.createWarning ("PCL-010-002")),
                                                                                 new TestDocument ("atgov-t10-fail-r008.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R008"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r009.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R009"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r010a.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R010"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r010b.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R010"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r011.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R011"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r012.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R012"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r013.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R013"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r014.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R014"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")),
                                                                                 new TestDocument ("atgov-t10-fail-r015.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R015"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323")),
                                                                                 new TestDocument ("atgov-t10-fail-r016.xml",
                                                                                                   ErrorType.createError ("ATGOV-T10-R016"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323")),
                                                                                 new TestDocument ("atgov-t10-fail-r017.xml",
                                                                                                   ErrorType.createWarning ("ATGOV-T10-R005"),
                                                                                                   ErrorType.createError ("ATGOV-T10-R017"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R263"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R266"),
                                                                                                   ErrorType.createWarning ("BIICORE-T10-R323"),
                                                                                                   ErrorType.createWarning ("EUGEN-T10-R003")) };
  private static final TestDocument [] CREDITNOTES_AT_ERROR = new TestDocument [] { new TestDocument ("atnat-t14-fail-r001.xml",
                                                                                                      ErrorType.createError ("ATNAT-T14-R001"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atnat-t14-fail-r002.xml",
                                                                                                      ErrorType.createError ("ATNAT-T14-R002"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atnat-t14-fail-r003.xml",
                                                                                                      ErrorType.createError ("ATNAT-T14-R003"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atnat-t14-fail-r004.xml",
                                                                                                      ErrorType.createError ("ATNAT-T14-R001"),
                                                                                                      ErrorType.createError ("ATNAT-T14-R004"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R015"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R016"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R017"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R018"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atnat-t14-fail-r005.xml",
                                                                                                      ErrorType.createError ("ATNAT-T14-R005"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R007"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atnat-t14-fail-r006.xml",
                                                                                                      ErrorType.createError ("ATNAT-T14-R006"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R016"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R017"),
                                                                                                      ErrorType.createError ("EUGEN-T14-R018"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r001.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R001"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r002.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R002"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r003.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R003"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    // new
                                                                                    // TestDocument
                                                                                    // ("atgov-t14-fail-r004.xml",
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("ATGOV-T14-R004"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("BIICORE-T14-R051"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r005.xml",
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051")),
                                                                                    // new
                                                                                    // TestDocument
                                                                                    // ("atgov-t14-fail-r007a.xml",
                                                                                    // new
                                                                                    // FatalError
                                                                                    // ("ATGOV-T14-R007"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("BIICORE-T14-R051"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("ATGOV-T14-R005")),
                                                                                    // new
                                                                                    // TestDocument
                                                                                    // ("atgov-t14-fail-r007b.xml",
                                                                                    // new
                                                                                    // FatalError
                                                                                    // ("ATGOV-T14-R007"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("BIICORE-T14-R051"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r008.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R008"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    // new
                                                                                    // TestDocument
                                                                                    // ("atgov-t14-fail-r009.xml",
                                                                                    // new
                                                                                    // FatalError
                                                                                    // ("ATGOV-T14-R009"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("BIICORE-T14-R051"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("ATGOV-T14-R005")),
                                                                                    // new
                                                                                    // TestDocument
                                                                                    // ("atgov-t14-fail-r010a.xml",
                                                                                    // new
                                                                                    // FatalError
                                                                                    // ("ATGOV-T14-R010"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("BIICORE-T14-R051"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("ATGOV-T14-R005")),
                                                                                    // new
                                                                                    // TestDocument
                                                                                    // ("atgov-t14-fail-r010b.xml",
                                                                                    // new
                                                                                    // FatalError
                                                                                    // ("ATGOV-T14-R010"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("BIICORE-T14-R051"),
                                                                                    // new
                                                                                    // Warning
                                                                                    // ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r011.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R011"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r012.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R012"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r013.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R013"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r014.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R014"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r015.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R015"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")),
                                                                                    new TestDocument ("atgov-t14-fail-r016.xml",
                                                                                                      ErrorType.createError ("ATGOV-T14-R016"),
                                                                                                      ErrorType.createWarning ("BIICORE-T14-R051"),
                                                                                                      ErrorType.createWarning ("ATGOV-T14-R005")) };

  /**
   * TC01.4.TS1.xml, TC01.5.TS1.xml, TC01.10.TS1.xml, TC01.39.TS1.xml,
   * TC01.40.TS1.xml, TC01.44.TS1.xml not XSD compliant
   */
  public static final TestDocument [] ORDERS_ERROR = new TestDocument [] { new TestDocument ("TC01.1.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R001")),
                                                                           new TestDocument ("TC01.2.TS1.xml",
                                                                                             ErrorType.createWarning ("BIICORE-T01-R000"),
                                                                                             ErrorType.createError ("BIIRULE-T01-R002")),
                                                                           new TestDocument ("TC01.3.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R003")),
                                                                           new TestDocument ("TC01.6.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R027")),
                                                                           new TestDocument ("TC01.7.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R027")),
                                                                           new TestDocument ("TC01.8.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R030")),
                                                                           new TestDocument ("TC01.9.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R004")),
                                                                           new TestDocument ("TC01.11.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R008")),
                                                                           new TestDocument ("TC01.12.TS1.xml",
                                                                                             ErrorType.createWarning ("BIICORE-T01-R436"),
                                                                                             ErrorType.createError ("BIIRULE-T01-R009")),
                                                                           new TestDocument ("TC01.13.TS1.xml",
                                                                                             ErrorType.createWarning ("BIICORE-T01-R439"),
                                                                                             ErrorType.createError ("BIIRULE-T01-R010")),
                                                                           new TestDocument ("TC01.14.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R015")),
                                                                           new TestDocument ("TC01.15.TS1.XML",
                                                                                             ErrorType.createWarning ("BIICORE-T01-R080"),
                                                                                             ErrorType.createWarning ("EUGEN-T01-R002"),
                                                                                             ErrorType.createError ("BIIRULE-T01-R028")),
                                                                           new TestDocument ("TC01.16.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R001")),
                                                                           new TestDocument ("TC01.17.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R001")),
                                                                           new TestDocument ("TC01.18.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R001")),
                                                                           new TestDocument ("TC01.19.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R001")),
                                                                           new TestDocument ("TC01.20.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R001")),
                                                                           new TestDocument ("TC01.21.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R002")),
                                                                           new TestDocument ("TC01.22.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R002")),
                                                                           new TestDocument ("TC01.23.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R002")),
                                                                           new TestDocument ("TC01.24.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R002")),
                                                                           new TestDocument ("TC01.25.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R018")),
                                                                           new TestDocument ("TC01.26.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R019")),
                                                                           new TestDocument ("TC01.27.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R020")),
                                                                           new TestDocument ("TC01.28.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021")),
                                                                           new TestDocument ("TC01.29.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021")),
                                                                           new TestDocument ("TC01.30.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021")),
                                                                           new TestDocument ("TC01.31.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021")),
                                                                           new TestDocument ("TC01.32.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021")),
                                                                           new TestDocument ("TC01.33.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R029")),
                                                                           new TestDocument ("TC01.34.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R018"),
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021"),
                                                                                             ErrorType.createError ("EUGEN-T01-R008")),
                                                                           new TestDocument ("TC01.35.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R018"),
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021"),
                                                                                             ErrorType.createError ("EUGEN-T01-R009")),
                                                                           new TestDocument ("TC01.36.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R019"),
                                                                                             ErrorType.createError ("EUGEN-T01-R006")),
                                                                           new TestDocument ("TC01.37.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R018"),
                                                                                             ErrorType.createError ("BIIRULE-T01-R026"),
                                                                                             ErrorType.createError ("EUGEN-T01-R009")),
                                                                           new TestDocument ("TC01.38.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R013")),
                                                                           new TestDocument ("TC01.41.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R005"),
                                                                                             ErrorType.createError ("EUGEN-T01-R010")),
                                                                           new TestDocument ("TC01.42.TS1.xml",
                                                                                             ErrorType.createError ("EUGEN-T01-R010")),
                                                                           new TestDocument ("TC01.43.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R024")),
                                                                           new TestDocument ("TC01.45.TS1.xml",
                                                                                             ErrorType.createError ("BIIRULE-T01-R011")),
                                                                           new TestDocument ("TC01.46.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R003")),
                                                                           new TestDocument ("TC01.47.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R003")),
                                                                           new TestDocument ("TC01.48.TS1.xml",
                                                                                             ErrorType.createWarning ("EUGEN-T01-R003")),
                                                                           new TestDocument ("TC01.49.TS1.xml",
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R020"),
                                                                                             ErrorType.createWarning ("BIIRULE-T01-R021")) };

  private TestFiles ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static List <IReadableResource> getSuccessFiles (@Nonnull final ETestFileType eFileType)
  {
    return getSuccessFiles (eFileType, null);
  }

  @Nonnull
  @ReturnsMutableCopy
  public static List <IReadableResource> getSuccessFiles (@Nonnull final ETestFileType eFileType,
                                                          @Nullable final Locale aCountry)
  {
    ValueEnforcer.notNull (eFileType, "FileType");

    final String sCountry = aCountry == null ? null : aCountry.getCountry ();

    String [] aFilenames;
    switch (eFileType)
    {
      case CREDITNOTE:
        if ("AT".equals (sCountry))
          aFilenames = CREDITNOTES_AT_SUCCESS;
        else
          aFilenames = ArrayHelper.getConcatenated (CREDITNOTES_SUCCESS, CREDITNOTES_AT_SUCCESS);
        break;
      case INVOICE:
        if ("AT".equals (sCountry))
          aFilenames = INVOICES_AT_SUCCESS;
        else
          aFilenames = ArrayHelper.getConcatenated (INVOICES_SUCCESS, INVOICES_AT_SUCCESS);
        break;
      default:
        s_aLogger.warn ("No success test files present for type " +
                        eFileType +
                        (sCountry == null ? "" : " and country " + sCountry));
        aFilenames = new String [0];
        break;
    }

    // Build result list
    final List <IReadableResource> ret = new ArrayList <IReadableResource> ();
    for (final String sFilename : aFilenames)
      ret.add (eFileType.getSuccessResource (sFilename));
    return ret;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static List <TestResource> getErrorFiles (@Nonnull final ETestFileType eFileType)
  {
    return getErrorFiles (eFileType, null);
  }

  @Nonnull
  @ReturnsMutableCopy
  public static List <TestResource> getErrorFiles (@Nonnull final ETestFileType eFileType,
                                                   @Nullable final Locale aCountry)
  {
    ValueEnforcer.notNull (eFileType, "FileType");

    final String sCountry = aCountry == null ? null : aCountry.getCountry ();

    TestDocument [] aFilenames;
    switch (eFileType)
    {
      case CREDITNOTE:
        if ("AT".equals (sCountry))
          aFilenames = CREDITNOTES_AT_ERROR;
        else
        {
          s_aLogger.warn ("No error test files present for type " + eFileType);
          aFilenames = new TestDocument [0];
        }
        break;
      case INVOICE:
        if ("AT".equals (sCountry))
          aFilenames = INVOICES_AT_ERROR;
        else
          aFilenames = INVOICES_ERROR;
        break;
      default:
        s_aLogger.warn ("No error test files present for type " +
                        eFileType +
                        (sCountry == null ? "" : " and country " + sCountry));
        aFilenames = new TestDocument [0];
        break;
    }

    // Build result list
    final List <TestResource> ret = new ArrayList <TestResource> ();
    for (final TestDocument aTestDoc : aFilenames)
      ret.add (new TestResource (eFileType.getErrorResource (aTestDoc.getFilename ()),
                                 aTestDoc.getAllExpectedErrors ()));
    return ret;
  }
}
