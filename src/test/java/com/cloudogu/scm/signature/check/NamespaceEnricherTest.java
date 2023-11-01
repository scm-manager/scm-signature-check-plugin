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

import javax.inject.Provider;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware("TrainerRed")
class NamespaceEnricherTest {

  private final Namespace namespace = new Namespace("scmadmin-space");

  @Mock
  private HalAppender halAppender;

  @Mock
  private SignatureConfigService signatureConfigService;

  private HalEnricherContext halEnricherContext;

  private NamespaceEnricher namespaceEnricher;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);

    namespaceEnricher = new NamespaceEnricher(scmPathInfoStoreProvider, signatureConfigService);
    halEnricherContext = HalEnricherContext.of(namespace);
  }

  @Test
  void shouldNotAddNamespaceConfigLinkBecauseOfMissingPermission() {
    GlobalSignatureConfig globalConfig = new GlobalSignatureConfig();
    globalConfig.setChildrenConfigDisabled(false);
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalConfig);

    namespaceEnricher.enrich(halEnricherContext, halAppender);

    verifyNoInteractions(halAppender);
  }

  @Test
  @SubjectAware(permissions = "namespace:signatureCheck:scmadmin-space")
  void shouldNotAddNamespaceConfigLinkBecauseNamespaceConfigIsDisabledGlobally() {
    GlobalSignatureConfig globalConfig = new GlobalSignatureConfig();
    globalConfig.setChildrenConfigDisabled(true);
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalConfig);

    namespaceEnricher.enrich(halEnricherContext, halAppender);

    verifyNoInteractions(halAppender);
  }

  @Test
  @SubjectAware(permissions = "namespace:signatureCheck:scmadmin-space")
  void shouldAddNamespaceConfigLink() {
    GlobalSignatureConfig globalConfig = new GlobalSignatureConfig();
    globalConfig.setChildrenConfigDisabled(false);
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalConfig);

    namespaceEnricher.enrich(halEnricherContext, halAppender);

    verify(halAppender).appendLink(
      "namespaceSignatureConfig", "https://scm-manager.org/scm/api/v2/signature-check/" + namespace.getNamespace()
    );
  }

}
