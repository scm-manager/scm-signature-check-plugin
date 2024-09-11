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

import jakarta.inject.Provider;
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
