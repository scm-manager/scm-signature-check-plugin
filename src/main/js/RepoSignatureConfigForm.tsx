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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Subtitle } from "@scm-manager/ui-components";
import { HalRepresentation, Repository } from "@scm-manager/ui-types";
import { RepositorySignatureConfigDto } from "./types";
import { ConfigurationForm, Form } from "@scm-manager/ui-forms";
import BaseSignatureConfigFormElements from "./BaseSignatureConfigFormElements";
import { useDocumentTitleForRepository } from "@scm-manager/ui-core";

type Props = {
  link: string;
  repository: Repository;
};

type Configuration = HalRepresentation & RepositorySignatureConfigDto;

const RepoSignatureConfigForm: FC<Props> = ({ link, repository }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitleForRepository(repository, t("scm-signature-check-plugin.config.menuTitle"));
  return (
    <>
      <Subtitle>{t("scm-signature-check-plugin.config.menuTitle")}</Subtitle>
      <ConfigurationForm<Configuration> link={link} translationPath={["plugins", "scm-signature-check-plugin.config"]}>
        {({ watch }) => {
          return (
            <>
              <Form.Row>
                <Form.Checkbox
                  name="overwriteParentConfig"
                  label={t("scm-signature-check-plugin.config.overwriteNamespaceAndGlobalConfig")}
                />
              </Form.Row>
              {watch("overwriteParentConfig") ? <BaseSignatureConfigFormElements watch={watch} /> : null}
            </>
          );
        }}
      </ConfigurationForm>
    </>
  );
};

export default RepoSignatureConfigForm;
