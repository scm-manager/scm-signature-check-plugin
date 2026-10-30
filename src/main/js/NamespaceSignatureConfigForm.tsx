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
import { ConfigurationForm, Form, Subtitle, useNamespaceFromParams } from "@scm-manager/ui-core";
import { HalRepresentation, Link } from "@scm-manager/ui-types";
import { NamespaceSignatureConfigDto } from "./types";
import BaseSignatureConfigFormElements from "./BaseSignatureConfigFormElements";

type Configuration = HalRepresentation & NamespaceSignatureConfigDto;

const NamespaceSignatureConfigForm: FC = () => {
  const [t] = useTranslation("plugins");
  const namespace = useNamespaceFromParams();
  const link = (namespace._links["namespaceSignatureConfig"] as Link).href;

  return (
    <>
      <Subtitle>{t("scm-signature-check-plugin.config.menuTitle")}</Subtitle>
      <ConfigurationForm<Configuration> link={link} translationPath={["plugins", "scm-signature-check-plugin.config"]}>
        {({ watch }) => (
          <>
            <Form.Row>
              <Form.Checkbox
                name="overwriteParentConfig"
                label={t("scm-signature-check-plugin.config.overwriteGlobalConfig")}
              />
            </Form.Row>
            {watch("overwriteParentConfig") ? (
              <>
                <Form.Row>
                  <Form.Checkbox
                    name="childrenConfigDisabled"
                    label={t("scm-signature-check-plugin.config.disableRepoConfig")}
                  />
                </Form.Row>
                <BaseSignatureConfigFormElements watch={watch} />
              </>
            ) : null}
          </>
        )}
      </ConfigurationForm>
    </>
  );
};

export default NamespaceSignatureConfigForm;
