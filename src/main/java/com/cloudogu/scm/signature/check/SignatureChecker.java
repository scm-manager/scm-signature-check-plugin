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

import javax.inject.Inject;

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
