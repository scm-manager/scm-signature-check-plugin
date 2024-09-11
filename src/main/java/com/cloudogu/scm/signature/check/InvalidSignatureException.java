/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.signature.check;

import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class InvalidSignatureException extends ExceptionWithContext {

  private static final String CODE = "DDTuAVZpr1";

  InvalidSignatureException(Repository repository, Changeset c, String message) {
    super(entity("Changeset", c.getId()).in(repository).build(), message);
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
