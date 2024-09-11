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

package com.cloudogu.scm.signature.check.config;

public class ConfigEvaluator {

  @SuppressWarnings("unchecked")
  public <
    B,
    G extends WithDisableOption,
    N extends WithDisableOption & WithOverwriteOption,
    R extends WithOverwriteOption
    > B evaluate(G globalConfig, N namespaceConfig, R repoConfig) {
    if(repoConfig.isOverwriteParentConfig() && !namespaceConfig.isChildrenConfigDisabled() && !globalConfig.isChildrenConfigDisabled()) {
      return (B) repoConfig;
    }

    if(namespaceConfig.isOverwriteParentConfig() && !globalConfig.isChildrenConfigDisabled()) {
      return (B) namespaceConfig;
    }

    return (B) globalConfig;
  }
}
