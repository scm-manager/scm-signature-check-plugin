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

import sonia.scm.repository.Namespace;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;


public class SignatureConfigService {

  public static final String GLOBAL_CONFIG_STORE_NAME = "global-signature-check";
  public static final String REPO_CONFIG_STORE_NAME = "repo-signature-check";

  public static final String NAMESPACE_CONFIG_STORE_NAME = "namespace-signature-check";
  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public SignatureConfigService(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public GlobalSignatureConfig getGlobalConfig() {
    return createGlobalConfigStore().getOptional().orElse(new GlobalSignatureConfig());
  }

  public void setGlobalConfig(GlobalSignatureConfig config) {
    createGlobalConfigStore().set(config);
  }

  public RepositorySignatureConfig getRepoConfig(Repository repository) {
    return createRepoConfigStore(repository).getOptional().orElse(new RepositorySignatureConfig());
  }

  public void setRepoConfig(Repository repository, RepositorySignatureConfig config) {
    createRepoConfigStore(repository).set(config);
  }

  public NamespaceSignatureConfig getNamespaceConfig(Namespace namespace) {
    return createNamespaceConfigStore(namespace).getOptional().orElse(new NamespaceSignatureConfig());
  }

  public void setNamespaceConfig(Namespace namespace, NamespaceSignatureConfig config) {
    createNamespaceConfigStore(namespace).set(config);
  }

  private ConfigurationStore<GlobalSignatureConfig> createGlobalConfigStore() {
    return storeFactory.withType(GlobalSignatureConfig.class).withName(GLOBAL_CONFIG_STORE_NAME).build();
  }

  private ConfigurationStore<RepositorySignatureConfig> createRepoConfigStore(Repository repository) {
    return storeFactory
      .withType(RepositorySignatureConfig.class)
      .withName(REPO_CONFIG_STORE_NAME)
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
