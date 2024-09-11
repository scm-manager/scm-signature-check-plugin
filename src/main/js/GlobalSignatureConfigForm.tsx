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
import { GlobalSignatureConfigDto } from "./types";
import { HalRepresentation } from "@scm-manager/ui-types";
import { ConfigurationForm, Form } from "@scm-manager/ui-forms";
import { Title } from "@scm-manager/ui-components";
import BaseSignatureConfigFormElements from "./BaseSignatureConfigFormElements";

type Props = {
  link: string;
};

type Configuration = HalRepresentation & GlobalSignatureConfigDto;

const GlobalSignatureConfigForm: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");

  return (
    <>
      <Title>{t("scm-signature-check-plugin.config.menuTitle")}</Title>
      <ConfigurationForm<Configuration> link={link} translationPath={["plugins", "scm-signature-check-plugin.config"]}>
        {({ watch }) => (
          <>
            <Form.Row>
              <Form.Checkbox
                name="childrenConfigDisabled"
                label={t("scm-signature-check-plugin.config.disableNamespaceAndRepoConfig")}
              />
            </Form.Row>
            <BaseSignatureConfigFormElements watch={watch} />
          </>
        )}
      </ConfigurationForm>
    </>
  );
};

export default GlobalSignatureConfigForm;
