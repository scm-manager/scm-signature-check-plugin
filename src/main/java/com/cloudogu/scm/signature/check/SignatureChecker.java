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
import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Signature;
import sonia.scm.repository.SignatureStatus;

import jakarta.inject.Inject;

import java.util.List;

@Extension
@EagerSingleton
public class SignatureChecker {

  private final SignatureConfigService signatureConfigService;
  private final NamespaceManager namespaceManager;
  private final ConfigEvaluator configEvaluator;

  @Inject
  public SignatureChecker(SignatureConfigService signatureConfigService, NamespaceManager namespaceManager, ConfigEvaluator configEvaluator) {
    this.signatureConfigService = signatureConfigService;
    this.namespaceManager = namespaceManager;
    this.configEvaluator = configEvaluator;
  }

  @Subscribe(async = false)
  public void onPush(PreReceiveRepositoryHookEvent event) {
    if(!event.getRepository().getType().equals("git")) {
      return;
    }

    GlobalSignatureConfig globalConfig = signatureConfigService.getGlobalConfig();
    RepositorySignatureConfig repoConfig = signatureConfigService.getRepoConfig(event.getRepository());
    //Namespace will always be there, because the repository could not exist otherwise
    //noinspection OptionalGetWithoutIsPresent
    NamespaceSignatureConfig namespaceConfig = signatureConfigService.getNamespaceConfig(
      namespaceManager.get(event.getRepository().getNamespace()).get()
    );

    BaseSignatureConfig activeConfig = configEvaluator.evaluate(globalConfig, namespaceConfig, repoConfig);

    checkSignatures(event, activeConfig);
  }

  private void checkSignatures(PreReceiveRepositoryHookEvent event, BaseSignatureConfig activeConfig) {
    if(!activeConfig.isEnabled()) {
      return;
    }

    for(Changeset c : event.getContext().getChangesetProvider().getChangesets()) {
      if(!containsProtectedBranch(c, activeConfig.getProtectedBranches())) {
        continue;
      }

      checkSignatureExist(c, event.getRepository());
      for (Signature s : c.getSignatures()) {
        checkSignatureInvalid(s, c, event.getRepository());

        if(activeConfig.getVerificationType() == GpgVerificationType.SCM_USER_SIGNATURE) {
          checkSignatureFromScmUser(s, c, event.getRepository());
        }
      }
    }
  }

  private void checkSignatureExist(Changeset c, Repository r) {
    if(c.getSignatures() == null || c.getSignatures().isEmpty()) {
      throw new InvalidSignatureException(r, c, "Changeset is missing a signature");
    }
  }

  private void checkSignatureInvalid(Signature s, Changeset c, Repository r) {
    if(s.getStatus() == SignatureStatus.INVALID) {
      throw new InvalidSignatureException(r, c, "Changeset has invalid signature");
    }
  }

  private void checkSignatureFromScmUser(Signature s, Changeset c, Repository r) {
    if(s.getStatus() != SignatureStatus.VERIFIED || s.getOwner().isEmpty()) {
      throw new InvalidSignatureException(r, c, "Changeset does not have a valid signature from a scm user");
    }
  }

  private boolean containsProtectedBranch(Changeset c, List<String> protectedBranches) {
    if(protectedBranches.isEmpty()) {
      return true;
    }

    for(String changedBranch: c.getBranches()) {
      if(protectedBranches.contains(changedBranch)) {
        return true;
      }
    }

    return false;
  }
}
