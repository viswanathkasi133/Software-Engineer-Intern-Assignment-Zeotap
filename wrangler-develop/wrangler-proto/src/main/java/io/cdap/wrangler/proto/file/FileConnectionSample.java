/*
 * Copyright © 2018-2019 Cask Data, Inc.
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
 */

package io.cdap.wrangler.proto.file;

import io.cdap.wrangler.proto.ConnectionSample;

/**
 * Information about a sample taken from a GCS Object.
 */
public class FileConnectionSample extends ConnectionSample {
  private final String uri;
  private final String path;
  private final String file;

  public FileConnectionSample(String id, String name, String connection, String sampler, String connectionid,
                              String uri, String path, String file) {
    super(id, name, connection, sampler, connectionid);
    this.uri = uri;
    this.path = path;
    this.file = file;
  }
}
