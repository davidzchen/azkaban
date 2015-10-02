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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import azkaban.flow.Flow;
import azkaban.project.validator.ProjectValidator;
import azkaban.project.validator.ValidationReport;
import azkaban.project.validator.XmlValidatorManager;
import azkaban.utils.Props;

public class DirectoryFlowLoader implements ProjectValidator, FlowLoader {
  private final Logger logger;
  private Props props;

  private HashMap<String, Flow> flowMap;
  private HashMap<String, Props> jobPropsMap;
  private Set<String> errors;
  private ArrayList<Props> propsList;

  /**
   * Creates a new DirectoryFlowLoader.
   *
   * @param props Properties to add.
   * @param logger The Logger to use.
   */
  public DirectoryFlowLoader(Props props, Logger logger) {
    this.logger = logger;
    this.props = props;
    flowMap = new HashMap<>();
    jobPropsMap = new HashMap<>();
    errors = new HashSet<>();
    propsList = new ArrayList<>();
  }

  /**
   * Returns the flow map constructed from the loaded flows.
   *
   * @return Map of flow name to Flow.
   */
  @Override
  public Map<String, Flow> getFlowMap() {
    return flowMap;
  }

  /**
   * Returns errors caught when loading flows.
   *
   * @return Set of error strings.
   */
  @Override
  public Set<String> getErrors() {
    return errors;
  }

  /**
   * Returns job properties.
   *
   * @return Map of job name to properties.
   */
  @Override
  public Map<String, Props> getJobProps() {
    return jobPropsMap;
  }

  /**
   * Returns list of properties.
   *
   * @return List of Props.
   */
  @Override
  public List<Props> getProps() {
    return propsList;
  }

  /**
   * Loads all flows from the directory into the project.
   *
   * @param project The project to load flows to.
   * @param baseDirectory The directory to load flows from.
   */
  @Override
  public void load(Project project, File baseDirectory) {
    JsonFlowLoader jsonLoader = new JsonFlowLoader(logger);
    PropertiesFlowLoader propertiesLoader = new PropertiesFlowLoader(props, logger);

    jsonLoader.load(project, baseDirectory);
    propertiesLoader.load(project, baseDirectory);

    merge(jsonLoader, propertiesLoader);
  }

  private void merge(JsonFlowLoader jsonLoader, PropertiesFlowLoader propertiesLoader) {
    // XXX(dzc): Do not add duplicates.
    flowMap.putAll(jsonLoader.getFlowMap());
    flowMap.putAll(propertiesLoader.getFlowMap());
    jobPropsMap.putAll(jsonLoader.getJobProps());
    jobPropsMap.putAll(propertiesLoader.getJobProps());

    errors.addAll(jsonLoader.getErrors());
    errors.addAll(propertiesLoader.getErrors());
    propsList.addAll(jsonLoader.getProps());
    propsList.addAll(propertiesLoader.getProps());
  }

  @Override
  public boolean initialize(Props configuration) {
    return true;
  }

  @Override
  public String getValidatorName() {
    return XmlValidatorManager.DEFAULT_VALIDATOR_KEY;
  }

  @Override
  public ValidationReport validateProject(Project project, File projectDir) {
    load(project, projectDir);
    ValidationReport report = new ValidationReport();
    report.addErrorMsgs(errors);
    return report;
  }
}
