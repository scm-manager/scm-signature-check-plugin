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

import sonia.scm.repository.Namespace;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;


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
