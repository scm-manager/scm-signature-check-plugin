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
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import jakarta.inject.Provider;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware("TrainerRed")
class IndexEnricherTest {

  private final Index index = new Index();

  @Mock
  private HalAppender halAppender;

  private HalEnricherContext halEnricherContext;

  private IndexEnricher indexEnricher;

  @BeforeEach
  void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);

    indexEnricher = new IndexEnricher(scmPathInfoStoreProvider);
    halEnricherContext = HalEnricherContext.of(index);
  }

  @Test
  void shouldNotAddGlobalConfigLinkBecauseOfMissingPermission() {
    indexEnricher.enrich(halEnricherContext, halAppender);
    verifyNoInteractions(halAppender);
  }

  @Test
  @SubjectAware(permissions = "configuration:read:signatureCheck")
  void shouldAddGlobalConfigLink() {
    indexEnricher.enrich(halEnricherContext, halAppender);

    verify(halAppender).appendLink(
      "globalSignatureConfig", "https://scm-manager.org/scm/api/v2/signature-check/global-config"
    );
  }

}
