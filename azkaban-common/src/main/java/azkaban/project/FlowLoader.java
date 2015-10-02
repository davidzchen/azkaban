/*
 * Copyright 2012 LinkedIn Corp.
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

package azkaban.project;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import azkaban.flow.Flow;
import azkaban.utils.Props;

interface FlowLoader {
  /**
   * Returns the flow map constructed from the loaded flows.
   *
   * @return Map of flow name to Flow.
   */
  public Map<String, Flow> getFlowMap();

  /**
   * Returns errors caught when loading flows.
   *
   * @return Set of error strings.
   */
  public Set<String> getErrors();

  /**
   * Returns job properties.
   *
   * @return Map of job name to properties.
   */
  public Map<String, Props> getJobProps();

  /**
   * Returns list of properties.
   *
   * @return List of Props.
   */
  public List<Props> getProps();

  /**
   * Loads all the flows in the given baseDirectory for the project.
   *
   * @param project The project to load flows for.
   * @param baseDirectory The base directory to load flows from.
   */
  public void load(Project project, File baseDirectory);
}
