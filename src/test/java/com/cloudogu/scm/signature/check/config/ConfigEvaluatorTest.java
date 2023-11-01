/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
