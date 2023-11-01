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

import javax.inject.Provider;
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
