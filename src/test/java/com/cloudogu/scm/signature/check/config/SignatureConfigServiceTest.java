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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryByteConfigurationStoreFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureConfigServiceTest {

  private final ConfigurationStoreFactory storeFactory = new InMemoryByteConfigurationStoreFactory();
  private final SignatureConfigService service = new SignatureConfigService(storeFactory);

  @Nested
  class GetGlobalConfig {

    @Test
    void shouldGetGlobalConfig() {
      GlobalSignatureConfig expectedConfig = new GlobalSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));
      createGlobalConfigStore().set(expectedConfig);

      GlobalSignatureConfig actualConfig = service.getGlobalConfig();

      assertThat(actualConfig.isChildrenConfigDisabled()).isTrue();
      assertThat(actualConfig.isEnabled()).isTrue();
      assertThat(actualConfig.getVerificationType()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE);
      assertThat(actualConfig.getProtectedBranches()).containsOnly("develop", "main");
    }
  }

  @Nested
  class GetRepoConfig {

    private final Repository repository = RepositoryTestData.create42Puzzle();

    @Test
    void shouldGetRepoConfig() {
      RepositorySignatureConfig expectedConfig = new RepositorySignatureConfig();
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));
      createRepoConfigStore(repository).set(expectedConfig);

      RepositorySignatureConfig actualConfig = service.getRepoConfig(repository);

      assertThat(actualConfig.isOverwriteParentConfig()).isTrue();
      assertThat(actualConfig.isEnabled()).isTrue();
      assertThat(actualConfig.getVerificationType()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE);
      assertThat(actualConfig.getProtectedBranches()).containsOnly("develop", "main");
    }
  }

  @Nested
  class GetNamespaceConfig {

    private final Namespace namespace = new Namespace("scmadmin-space");

    @Test
    void shouldGetNamespaceConfig() {
      NamespaceSignatureConfig expectedConfig = new NamespaceSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));
      createNamespaceConfigStore(namespace).set(expectedConfig);

      NamespaceSignatureConfig actualConfig = service.getNamespaceConfig(namespace);

      assertThat(actualConfig.isChildrenConfigDisabled()).isTrue();
      assertThat(actualConfig.isOverwriteParentConfig()).isTrue();
      assertThat(actualConfig.isEnabled()).isTrue();
      assertThat(actualConfig.getVerificationType()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE);
      assertThat(actualConfig.getProtectedBranches()).containsOnly("develop", "main");
    }
  }

  @Nested
  class SetGlobalConfig {

    @Test
    void shouldSetGlobalConfig() {
      ConfigurationStore<GlobalSignatureConfig> configStore = createGlobalConfigStore();
      configStore.set(new GlobalSignatureConfig());

      GlobalSignatureConfig expectedConfig = new GlobalSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));

      service.setGlobalConfig(expectedConfig);

      GlobalSignatureConfig actualConfig = createGlobalConfigStore().get();
      assertThat(actualConfig.isChildrenConfigDisabled()).isTrue();
      assertThat(actualConfig.isEnabled()).isTrue();
      assertThat(actualConfig.getVerificationType()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE);
      assertThat(actualConfig.getProtectedBranches()).containsOnly("develop", "main");
    }
  }

  @Nested
  class SetRepoConfig {

    private final Repository repository = RepositoryTestData.create42Puzzle();

    @Test
    void shouldSetRepoConfig() {
      ConfigurationStore<RepositorySignatureConfig> configStore = createRepoConfigStore(repository);
      configStore.set(new RepositorySignatureConfig());

      RepositorySignatureConfig expectedConfig = new RepositorySignatureConfig();
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));

      service.setRepoConfig(repository, expectedConfig);

      RepositorySignatureConfig actualConfig = createRepoConfigStore(repository).get();
      assertThat(actualConfig.isOverwriteParentConfig()).isTrue();
      assertThat(actualConfig.isEnabled()).isTrue();
      assertThat(actualConfig.getVerificationType()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE);
      assertThat(actualConfig.getProtectedBranches()).containsOnly("develop", "main");
    }
  }

  @Nested
  class SetNamespaceConfig {

    private final Namespace namespace = new Namespace("scmadmin-space");

    @Test
    void shouldSetNamespaceConfig() {
      ConfigurationStore<NamespaceSignatureConfig> configStore = createNamespaceConfigStore(namespace);
      configStore.set(new NamespaceSignatureConfig());

      NamespaceSignatureConfig expectedConfig = new NamespaceSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));

      service.setNamespaceConfig(namespace, expectedConfig);

      NamespaceSignatureConfig actualConfig = createNamespaceConfigStore(namespace).get();
      assertThat(actualConfig.isChildrenConfigDisabled()).isTrue();
      assertThat(actualConfig.isOverwriteParentConfig()).isTrue();
      assertThat(actualConfig.isEnabled()).isTrue();
      assertThat(actualConfig.getVerificationType()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE);
      assertThat(actualConfig.getProtectedBranches()).containsOnly("develop", "main");
    }
  }

  private ConfigurationStore<GlobalSignatureConfig> createGlobalConfigStore() {
    return storeFactory
      .withType(GlobalSignatureConfig.class)
      .withName(SignatureConfigService.GLOBAL_CONFIG_STORE_NAME)
      .build();
  }

  private ConfigurationStore<RepositorySignatureConfig> createRepoConfigStore(Repository repository) {
    return storeFactory
      .withType(RepositorySignatureConfig.class)
      .withName(SignatureConfigService.REPO_CONFIG_STORE_NAME)
      .forRepository(repository)
      .build();
  }

  private ConfigurationStore<NamespaceSignatureConfig> createNamespaceConfigStore(Namespace namespace) {
    return storeFactory
      .withType(NamespaceSignatureConfig.class)
      .withName(SignatureConfigService.NAMESPACE_CONFIG_STORE_NAME)
      .forNamespace(namespace.getNamespace())
      .build();
  }
}
