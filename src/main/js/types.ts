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

export type VerificationType = "ANY_SIGNATURE" | "SCM_USER_SIGNATURE";
export const VERIFICATION_TYPES: VerificationType[] = ["ANY_SIGNATURE", "SCM_USER_SIGNATURE"];

export type BaseSignatureConfigDto = {
  enabled: boolean;
  protectedBranches: string[];
  verificationType: VerificationType;
};

export type GlobalSignatureConfigDto = BaseSignatureConfigDto & {
  childrenConfigDisabled: boolean;
};

export type NamespaceSignatureConfigDto = BaseSignatureConfigDto & {
  childrenConfigDisabled: boolean;
  overwriteParentConfig: boolean;
};

export type RepositorySignatureConfigDto = BaseSignatureConfigDto & {
  overwriteParentConfig: boolean;
};
