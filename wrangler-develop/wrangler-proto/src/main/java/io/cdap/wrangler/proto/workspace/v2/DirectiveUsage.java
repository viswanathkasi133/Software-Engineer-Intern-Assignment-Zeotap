/*
 * Copyright © 2021 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package io.cdap.wrangler.proto.workspace.v2;

import io.cdap.wrangler.api.parser.UsageDefinition;

/**
 * V2 version for directives
 */
public class DirectiveUsage {
  private final String directive;
  private final String usage;
  private final String description;
  private final String scope;
  private final UsageDefinition arguments;
  private final String[] categories;

  public DirectiveUsage(String directive, String usage, String description, String scope,
                        UsageDefinition arguments, String[] categories) {
    this.directive = directive;
    this.usage = usage;
    this.description = description;
    this.scope = scope;
    this.arguments = arguments;
    this.categories = categories;
  }

  public String getDirective() {
    return directive;
  }

  public String getUsage() {
    return usage;
  }

  public String getDescription() {
    return description;
  }

  public String getScope() {
    return scope;
  }

  public UsageDefinition getArguments() {
    return arguments;
  }

  public String[] getCategories() {
    return categories;
  }
}
