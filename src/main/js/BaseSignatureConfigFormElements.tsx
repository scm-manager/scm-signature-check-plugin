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
