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

import React, { FC, useRef } from "react";
import { ChipInputField, Form } from "@scm-manager/ui-forms";
import { Level } from "@scm-manager/ui-components";
import { BaseSignatureConfigDto, VERIFICATION_TYPES } from "./types";
import { useTranslation } from "react-i18next";
import { UseFormWatch } from "react-hook-form";
import { HalRepresentation } from "@scm-manager/ui-types";

type Props = {
  watch: UseFormWatch<HalRepresentation & BaseSignatureConfigDto>;
};

const BaseSignatureConfigFormElements: FC<Props> = ({ watch }: Props) => {
  const [t] = useTranslation("plugins");
  const protectedBranchesRef = useRef<HTMLInputElement>(null);

  return (
    <>
      <Form.Row>
        <Form.Checkbox name="enabled" />
      </Form.Row>
      {watch("enabled") ? (
        <>
          <Form.Row>
            <Form.ChipInput name="protectedBranches" ref={protectedBranchesRef} />
          </Form.Row>
          <Level
            right={
              <ChipInputField.AddButton inputRef={protectedBranchesRef}>
                {t("scm-signature-check-plugin.config.protectedBranches.add")}
              </ChipInputField.AddButton>
            }
          ></Level>
          <hr />
          <Form.Row>
            <Form.RadioGroup name="verificationType">
              {VERIFICATION_TYPES.map(value => (
                <Form.RadioGroup.Option key={value} value={value} />
              ))}
            </Form.RadioGroup>
          </Form.Row>
        </>
      ) : null}
    </>
  );
};

export default BaseSignatureConfigFormElements;
