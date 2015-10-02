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
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import azkaban.proto.FlowConfig;
import azkaban.proto.FlowJobConfig;
import azkaban.proto.JavaProcessJobConfig;
import azkaban.proto.JobConfig;
import azkaban.proto.ProjectConfig;
import azkaban.proto.ShellJobConfig;
import azkaban.flow.Flow;
import azkaban.utils.Props;

class JsonFlowLoader implements FlowLoader {
  private static final String PROJECT_JSON_FILE = "project.json";

  private Logger logger;
  private HashMap<String, Flow> flowMap;
  private HashMap<String, Props> jobPropsMap;
  private Set<String> errors;

  private Set<String> duplicateFlows;
  private Set<String> duplicateJobs;

  public JsonFlowLoader(Logger logger) {
    this.logger = logger;
    flowMap = new HashMap<>();
    jobPropsMap = new HashMap<>();
    errors = new HashSet<>();
    duplicateFlows = new HashSet<>();
    duplicateJobs = new HashSet<>();
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
    return new ArrayList<Props>();
  }

  /**
   * Loads all the flows in the given baseDirectory for the project.
   *
   * @param project The project to load flows for.
   * @param baseDirectory The base directory to load flows from.
   */
  @Override
  public void load(Project project, File baseDirectory) {
    File[] projectJsonFiles = baseDirectory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.equals(PROJECT_JSON_FILE);
      }
    });

    if (projectJsonFiles.length == 0) {
      return;
    }
    File projectJsonFile = projectJsonFiles[0];

    JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.newBuilder()
        .add(ShellJobConfig.getDescriptor())
        .add(JavaProcessJobConfig.getDescriptor())
        .add(FlowJobConfig.getDescriptor())
        .build();
    JsonFormat.Parser parser = JsonFormat.parser().usingTypeRegistry(registry);
    ProjectConfig projectConfig;
    try {
      ProjectConfig.Builder builder = ProjectConfig.newBuilder();
      parser.merge(new FileReader(projectJsonFile), builder);
      projectConfig = builder.build();
    } catch (InvalidProtocolBufferException e) {
      errors.add("Error parsing project.json: " + e.getMessage());
      return;
    } catch (IOException e) {
      errors.add("Error reading project.json: " + e.getMessage());
      return;
    }

    convertProjectConfig(projectConfig);
  }

  private String processJobName(final String flowName, final String jobName) {
    return flowName + ":" + jobName;
  }

  private void convertProjectConfig(final ProjectConfig projectConfig) {
    for (FlowConfig flowConfig : projectConfig.getFlowsList()) {
      final String flowName = flowConfig.getName();
      // If flow name is not unique, then leave it out.
      if (!duplicateFlows.contains(flowName) && flowMap.containsKey(flowName)) {
        errors.add("Duplicate flow names found: " + flowName);
        flowMap.remove(flowName);
        duplicateFlows.add(flowName);
        continue;
      }
      for (JobConfig jobConfig : flowConfig.getJobsList()) {
        final String jobName = processJobName(flowName, jobConfig.getName());
        // If job name is not unique, then leave it out.
        if (!duplicateJobs.contains(jobName) && jobPropsMap.containsKey(jobName)) {
          errors.add("Duplicate job names found: " + jobName);
          jobPropsMap.remove(jobName);
          duplicateJobs.add(jobName);
        }
      }
      Flow flow = new Flow(flowName);
      flowMap.put(flowName, flow);
    }
  }
}
