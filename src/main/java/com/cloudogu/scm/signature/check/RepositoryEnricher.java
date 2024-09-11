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

import com.cloudogu.scm.signature.check.config.GlobalSignatureConfig;
import com.cloudogu.scm.signature.check.config.NamespaceSignatureConfig;
import com.cloudogu.scm.signature.check.config.SignatureConfigResource;
import com.cloudogu.scm.signature.check.config.SignatureConfigService;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> pathInfoStore;
  private final SignatureConfigService signatureConfigService;
  private final NamespaceManager namespaceManager;

  @Inject
  public RepositoryEnricher(Provider<ScmPathInfoStore> pathInfoStore, SignatureConfigService signatureConfigService, NamespaceManager namespaceManager) {
    this.pathInfoStore = pathInfoStore;
    this.signatureConfigService = signatureConfigService;
    this.namespaceManager = namespaceManager;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);

    GlobalSignatureConfig globalConfig = signatureConfigService.getGlobalConfig();
    if(globalConfig.isChildrenConfigDisabled()) {
      return;
    }

    //Without the Namespace the repository cannot exist
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    NamespaceSignatureConfig namespaceConfig = signatureConfigService.getNamespaceConfig(
            namespaceManager.get(repository.getNamespace()).get()
    );
    if(namespaceConfig.isChildrenConfigDisabled()) {
      return;
    }

    if(RepositoryPermissions.custom("signatureCheck", repository.getId()).isPermitted()) {
      LinkBuilder linkBuilder = new LinkBuilder(pathInfoStore.get().get(), SignatureConfigResource.class);
      appender.appendLink(
        "repoSignatureConfig",
        linkBuilder.method("getRepoConfig").parameters(repository.getNamespace(), repository.getName()).href()
      );
    }
  }
}
