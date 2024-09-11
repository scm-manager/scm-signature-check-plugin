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

import com.cloudogu.scm.signature.check.config.BaseSignatureConfig;
import com.cloudogu.scm.signature.check.config.ConfigEvaluator;
import com.cloudogu.scm.signature.check.config.GlobalSignatureConfig;
import com.cloudogu.scm.signature.check.config.GpgVerificationType;
import com.cloudogu.scm.signature.check.config.NamespaceSignatureConfig;
import com.cloudogu.scm.signature.check.config.RepositorySignatureConfig;
import com.cloudogu.scm.signature.check.config.SignatureConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.Signature;
import sonia.scm.repository.SignatureStatus;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SignatureCheckerTest {

  private final Repository repository = RepositoryTestData.create42Puzzle("git");
  private final Namespace namespace = new Namespace(repository.getNamespace());

  private final GlobalSignatureConfig globalConfig = new GlobalSignatureConfig();
  private final NamespaceSignatureConfig namespaceConfig = new NamespaceSignatureConfig();
  private final RepositorySignatureConfig repoConfig = new RepositorySignatureConfig();

  @InjectMocks
  private SignatureChecker signatureChecker;

  @Mock
  private SignatureConfigService signatureConfigService;

  @Mock
  private NamespaceManager namespaceManager;

  @Mock
  private ConfigEvaluator configEvaluator;

  @Mock
  private PreReceiveRepositoryHookEvent event;

  @Mock
  private HookContext context;

  @Mock
  private HookChangesetBuilder changesetBuilder;

  @Test
  void shouldDoNothingBecauseRepoTypeIsHg() {
    when(event.getRepository()).thenReturn(RepositoryTestData.create42Puzzle("hg"));

    signatureChecker.onPush(event);

    verifyNoInteractions(signatureConfigService);
    verifyNoInteractions(namespaceManager);
    verifyNoInteractions(configEvaluator);
  }

  @Test
  void shouldDoNothingBecauseRepoTypeIsSvn() {
    when(event.getRepository()).thenReturn(RepositoryTestData.create42Puzzle("svn"));

    signatureChecker.onPush(event);

    verifyNoInteractions(signatureConfigService);
    verifyNoInteractions(namespaceManager);
    verifyNoInteractions(configEvaluator);
  }

  @Test
  void shouldDoNothingBecauseFeatureIsDisabled() {
    when(event.getRepository()).thenReturn(repository);

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(false);
    setupConfigMocks(activeConfig);

    signatureChecker.onPush(event);

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForAnySignaturesButSignaturesNotSetForChangeset() {
    Changeset invalidChangeset = new Changeset();
    invalidChangeset.setId("invalidChangeset");
    setupEventMocks(List.of(invalidChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.ANY_SIGNATURE);
    setupConfigMocks(activeConfig);

    assertThatThrownBy(() -> signatureChecker.onPush(event))
      .isInstanceOf(InvalidSignatureException.class)
      .hasMessage("Changeset is missing a signature");

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForAnySignaturesButChangesetHasEmptySignatures() {
    Changeset invalidChangeset = new Changeset();
    invalidChangeset.setId("invalidChangeset");
    invalidChangeset.setSignatures(Collections.emptyList());
    setupEventMocks(List.of(invalidChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.ANY_SIGNATURE);
    setupConfigMocks(activeConfig);

    assertThatThrownBy(() -> signatureChecker.onPush(event))
      .isInstanceOf(InvalidSignatureException.class)
      .hasMessage("Changeset is missing a signature");

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForAnySignaturesButChangesetHasInvalidSignature() {
    Changeset invalidChangeset = new Changeset();
    invalidChangeset.setId("invalidChangeset");
    invalidChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.INVALID, null, Collections.emptySet()))
    );
    setupEventMocks(List.of(invalidChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.ANY_SIGNATURE);
    setupConfigMocks(activeConfig);

    assertThatThrownBy(() -> signatureChecker.onPush(event))
      .isInstanceOf(InvalidSignatureException.class)
      .hasMessage("Changeset has invalid signature");

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForAnyVerifiedSignatures() {
    Changeset validChangeset = new Changeset();
    validChangeset.setId("validChangeset");
    validChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.VERIFIED, null, Collections.emptySet()))
    );
    setupEventMocks(List.of(validChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.ANY_SIGNATURE);
    setupConfigMocks(activeConfig);

    signatureChecker.onPush(event);

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForAnyNotFoundSignatures() {
    Changeset validChangeset = new Changeset();
    validChangeset.setId("validChangeset");
    validChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.NOT_FOUND, null, Collections.emptySet()))
    );
    setupEventMocks(List.of(validChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.ANY_SIGNATURE);
    setupConfigMocks(activeConfig);

    signatureChecker.onPush(event);

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForNotFoundSignatureFromScmUser() {
    Changeset invalidChangeset = new Changeset();
    invalidChangeset.setId("invalidChangeset");
    invalidChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.NOT_FOUND, "scmadmin", Collections.emptySet()))
    );
    setupEventMocks(List.of(invalidChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
    setupConfigMocks(activeConfig);

    assertThatThrownBy(() -> signatureChecker.onPush(event))
      .isInstanceOf(InvalidSignatureException.class)
      .hasMessage("Changeset does not have a valid signature from a scm user");

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForVerifiedSignatureButNotFromScmUser() {
    Changeset invalidChangeset = new Changeset();
    invalidChangeset.setId("invalidChangeset");
    invalidChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.VERIFIED, null, Collections.emptySet()))
    );
    setupEventMocks(List.of(invalidChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
    setupConfigMocks(activeConfig);

    assertThatThrownBy(() -> signatureChecker.onPush(event))
      .isInstanceOf(InvalidSignatureException.class)
      .hasMessage("Changeset does not have a valid signature from a scm user");

    verifyConfigMocks();
  }

  @Test
  void shouldCheckForVerifiedSignatureFromScmUser() {
    Changeset validChangeset = new Changeset();
    validChangeset.setId("validChangeset");
    validChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.VERIFIED, "scmadmin", Collections.emptySet()))
    );
    setupEventMocks(List.of(validChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(new ArrayList<>(0));
    activeConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
    setupConfigMocks(activeConfig);

    signatureChecker.onPush(event);

    verifyConfigMocks();
  }

  @Test
  void shouldAllowInvalidSignatureForUnprotectedBranch() {
    Changeset invalidChangeset = new Changeset();
    invalidChangeset.setId("invalidChangeset");
    invalidChangeset.setBranches(List.of("unprotected"));
    invalidChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.INVALID, null, Collections.emptySet()))
    );
    setupEventMocks(List.of(invalidChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(List.of("protected"));
    activeConfig.setVerificationType(GpgVerificationType.ANY_SIGNATURE);
    setupConfigMocks(activeConfig);

    signatureChecker.onPush(event);

    verifyConfigMocks();
  }

  @Test
  void shouldNotAllowInvalidSignatureForProtectedBranch() {
    Changeset invalidChangeset = new Changeset();
    invalidChangeset.setId("invalidChangeset");
    invalidChangeset.setBranches(List.of("protected"));
    invalidChangeset.setSignatures(List.of(
      new Signature("keyId", "gpg", SignatureStatus.INVALID, null, Collections.emptySet()))
    );
    setupEventMocks(List.of(invalidChangeset));

    BaseSignatureConfig activeConfig = new BaseSignatureConfig();
    activeConfig.setEnabled(true);
    activeConfig.setProtectedBranches(List.of("protected"));
    activeConfig.setVerificationType(GpgVerificationType.ANY_SIGNATURE);
    setupConfigMocks(activeConfig);

    assertThatThrownBy(() -> signatureChecker.onPush(event))
      .isInstanceOf(InvalidSignatureException.class)
      .hasMessage("Changeset has invalid signature");

    verifyConfigMocks();
  }

  private void setupEventMocks(List<Changeset> changesets) {
    when(event.getRepository()).thenReturn(repository);
    when(event.getContext()).thenReturn(context);
    when(context.getChangesetProvider()).thenReturn(changesetBuilder);
    when(changesetBuilder.getChangesets()).thenReturn(changesets);
  }

  private void setupConfigMocks(BaseSignatureConfig activeConfig) {
    when(namespaceManager.get(repository.getNamespace())).thenReturn(Optional.of(namespace));
    when(signatureConfigService.getGlobalConfig()).thenReturn(globalConfig);
    when(signatureConfigService.getNamespaceConfig(namespace)).thenReturn(namespaceConfig);
    when(signatureConfigService.getRepoConfig(repository)).thenReturn(repoConfig);
    when(configEvaluator.evaluate(globalConfig, namespaceConfig, repoConfig)).thenReturn(activeConfig);
  }

  private void verifyConfigMocks() {
    verify(namespaceManager).get(repository.getNamespace());
    verify(signatureConfigService).getGlobalConfig();
    verify(signatureConfigService).getNamespaceConfig(namespace);
    verify(signatureConfigService).getRepoConfig(repository);
    verify(configEvaluator).evaluate(globalConfig, namespaceConfig, repoConfig);
  }

}
