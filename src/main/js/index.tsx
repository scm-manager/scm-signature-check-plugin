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

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalSignatureConfigForm from "./GlobalSignatureConfigForm";
import RepoSignatureConfigForm from "./RepoSignatureConfigForm";
import NamespaceSignatureConfigForm from "./NamespaceSignatureConfigForm";

cfgBinder.bindGlobal(
  "/signature-config",
  "scm-signature-check-plugin.config.menuTitle",
  "globalSignatureConfig",
  GlobalSignatureConfigForm,
  "SignatureCheck"
);

cfgBinder.bindRepositorySetting(
  "/signature-config",
  "scm-signature-check-plugin.config.menuTitle",
  "repoSignatureConfig",
  RepoSignatureConfigForm,
  "SignatureCheck"
);

cfgBinder.bindNamespaceSetting(
  "/signature-config",
  "scm-signature-check-plugin.config.menuTitle",
  "namespaceSignatureConfig",
  NamespaceSignatureConfigForm,
  "SignatureCheck"
);
