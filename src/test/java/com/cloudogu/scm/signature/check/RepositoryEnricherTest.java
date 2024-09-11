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
import com.cloudogu.scm.signature.check.config.SignatureConfigService;
import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import jakarta.inject.Provider;
import java.net.URI;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware("TrainerRed")
class RepositoryEnricherTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();

  private final Namespace namespace = new Namespace(repository.getNamespace());

  @Mock
  private HalAppender halAppender;

  @Mock
  private SignatureConfigService signatureConfigService;

  @Mock
  private NamespaceManager namespaceManager;

  private HalEnricherContext halEnricherContext;

  private RepositoryEnricher repositoryEnricher;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);

    repositoryEnricher = new RepositoryEnricher(scmPathInfoStoreProvider, signatureConfigService, namespaceManager);
    halEnricherContext = HalEnricherContext.of(repository);
  }

  @Test
  void shouldNotAddRepoConfigLinkBecauseOfMissingPermission() {
    GlobalSignatureConfig globalSignatureConfig = new GlobalSignatureConfig();
    globalSignatureConfig.setChildrenConfigDisabled(false);
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalSignatureConfig);

    NamespaceSignatureConfig namespaceSignatureConfig = new NamespaceSignatureConfig();
    namespaceSignatureConfig.setChildrenConfigDisabled(false);
    when(signatureConfigService.getNamespaceConfig(namespace)).thenReturn(namespaceSignatureConfig);
    when(namespaceManager.get(repository.getNamespace())).thenReturn(Optional.of(namespace));

    repositoryEnricher.enrich(halEnricherContext, halAppender);
    verifyNoInteractions(halAppender);
  }

  @Test
  @SubjectAware(permissions = "repository:signatureCheck:*")
  void shouldNotAddNamespaceConfigLinkBecauseRepoConfigIsDisabledGlobally() {
    GlobalSignatureConfig globalSignatureConfig = new GlobalSignatureConfig();
    globalSignatureConfig.setChildrenConfigDisabled(true);
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalSignatureConfig);

    repositoryEnricher.enrich(halEnricherContext, halAppender);

    verifyNoInteractions(halAppender);
  }

  @Test
  @SubjectAware(permissions = "repository:signatureCheck:*")
  void shouldNotAddNamespaceConfigLinkBecauseRepoConfigIsDisabledByNamespace() {
    GlobalSignatureConfig globalSignatureConfig = new GlobalSignatureConfig();
    globalSignatureConfig.setChildrenConfigDisabled(false);
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalSignatureConfig);

    NamespaceSignatureConfig namespaceSignatureConfig = new NamespaceSignatureConfig();
    namespaceSignatureConfig.setChildrenConfigDisabled(true);
    when(signatureConfigService.getNamespaceConfig(namespace)).thenReturn(namespaceSignatureConfig);
    when(namespaceManager.get(repository.getNamespace())).thenReturn(Optional.of(namespace));

    repositoryEnricher.enrich(halEnricherContext, halAppender);

    verifyNoInteractions(halAppender);
  }

  @Test
  @SubjectAware(permissions = "repository:signatureCheck:*")
  void shouldAddNamespaceConfigLink() {
    GlobalSignatureConfig globalSignatureConfig = new GlobalSignatureConfig();
    globalSignatureConfig.setChildrenConfigDisabled(false);
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalSignatureConfig);

    NamespaceSignatureConfig namespaceSignatureConfig = new NamespaceSignatureConfig();
    namespaceSignatureConfig.setChildrenConfigDisabled(false);
    when(signatureConfigService.getNamespaceConfig(namespace)).thenReturn(namespaceSignatureConfig);
    when(namespaceManager.get(repository.getNamespace())).thenReturn(Optional.of(namespace));

    repositoryEnricher.enrich(halEnricherContext, halAppender);

    verify(halAppender).appendLink(
      "repoSignatureConfig",
      "https://scm-manager.org/scm/api/v2/signature-check/" + repository.getNamespace() + "/" + repository.getName()
    );
  }
}
