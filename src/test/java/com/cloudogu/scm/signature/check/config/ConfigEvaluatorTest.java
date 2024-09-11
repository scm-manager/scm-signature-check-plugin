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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ConfigEvaluatorTest {

  private final ConfigEvaluator configEvaluator = new ConfigEvaluator();

  @ParameterizedTest
  @CsvSource({
    "false,false,false,false,Global",
    "false,false,false,true,Repo",
    "false,false,true,false,Namespace",
    "false,false,true,true,Repo",
    "false,true,false,false,Global",
    "false,true,false,true,Global",
    "false,true,true,false,Namespace",
    "false,true,true,true,Namespace",
    "true,false,false,false,Global",
    "true,false,false,true,Global",
    "true,false,true,false,Global",
    "true,false,true,true,Global",
    "true,true,false,false,Global",
    "true,true,false,true,Global",
    "true,true,true,false,Global",
    "true,true,true,true,Global",
  })
  void shouldEvaluateConfig(boolean disableNamespaceAndRepo, boolean disableRepo, boolean overwriteGlobal, boolean overwriteNamespaceAndGlobal, String expectedConfig) {
    GlobalSignatureConfig globalConfig = new GlobalSignatureConfig();
    globalConfig.setChildrenConfigDisabled(disableNamespaceAndRepo);

    NamespaceSignatureConfig namespaceConfig = new NamespaceSignatureConfig();
    namespaceConfig.setChildrenConfigDisabled(disableRepo);
    namespaceConfig.setOverwriteParentConfig(overwriteGlobal);

    RepositorySignatureConfig repoConfig = new RepositorySignatureConfig();
    repoConfig.setOverwriteParentConfig(overwriteNamespaceAndGlobal);

    BaseSignatureConfig baseSignatureConfig = configEvaluator.evaluate(globalConfig, namespaceConfig, repoConfig);

    switch (expectedConfig) {
      case "Global":
        assertThat(baseSignatureConfig).isEqualTo(globalConfig);
        break;
      case "Namespace":
        assertThat(baseSignatureConfig).isEqualTo(namespaceConfig);
        break;
      case "Repo":
        assertThat(baseSignatureConfig).isEqualTo(repoConfig);
        break;
      default:
        fail("Invalid Config type in test data");
        break;
    }
  }
}
