/*
 * Copyright 2014 LinkedIn Corp.
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
import java.net.URL;
import java.net.URISyntaxException;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import azkaban.proto.JobConfig;
import azkaban.proto.JavaProcessJobConfig;
import azkaban.test.executions.TestExecutions;
import azkaban.utils.Props;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DirectoryFlowLoaderTest {
  private Project project;

  @Before
  public void setUp() {
    project = new Project(11, "myTestProject");
  }

  private void printErrors(DirectoryFlowLoader loader) {
    for (String error : loader.getErrors()) {
      System.out.println(error);
    }
  }

  @Test
  public void testDirectoryLoad() throws URISyntaxException {
    Logger logger = Logger.getLogger(this.getClass());
    DirectoryFlowLoader loader = new DirectoryFlowLoader(new Props(), logger);

    loader.load(project, TestExecutions.getFlowDir("exectest1"));
    printErrors(loader);
    Assert.assertEquals(0, loader.getErrors().size());
    Assert.assertEquals(5, loader.getFlowMap().size());
  }

  @Test
  public void testLoadEmbeddedFlow() throws URISyntaxException {
    Logger logger = Logger.getLogger(this.getClass());
    DirectoryFlowLoader loader = new DirectoryFlowLoader(new Props(), logger);

    loader.load(project, TestExecutions.getFlowDir("embedded"));
    printErrors(loader);
    Assert.assertEquals(0, loader.getErrors().size());
  }

  @Test
  public void testRecursiveLoadEmbeddedFlow() throws URISyntaxException {
    Logger logger = Logger.getLogger(this.getClass());
    DirectoryFlowLoader loader = new DirectoryFlowLoader(new Props(), logger);

    loader.load(project, TestExecutions.getFlowDir("embedded_bad"));
    printErrors(loader);

    // Should be 3 errors: jobe->innerFlow, innerFlow->jobe, innerFlow
    Assert.assertEquals(3, loader.getErrors().size());
  }

  @Test
  public void testJsonProjectConfig() throws InvalidProtocolBufferException {
    JavaProcessJobConfig.Builder builder = JavaProcessJobConfig.newBuilder()
        .setJavaClass("azkaban.test.executor.SleepJavaJob");
    builder.getMutableProperties().put("seconds", "1");
    JavaProcessJobConfig specificConfig = builder.build();

    JobConfig jobConfig = JobConfig.newBuilder()
        .setName("foo")
        .setType("javaprocess")
        .setOptions(Any.pack(specificConfig))
        .build();

    JsonFormat.TypeRegistry registry = JsonFormat.TypeRegistry.newBuilder()
        .add(JavaProcessJobConfig.getDescriptor()).build();
    JsonFormat.Printer printer = JsonFormat.printer().usingTypeRegistry(registry);

    Assert.assertEquals(
        "{\n" +
        "  \"name\": \"foo\",\n" +
        "  \"type\": \"javaprocess\",\n" +
        "  \"options\": {\n" +
        "    \"@type\": \"type.googleapis.com/azkaban.JavaProcessJobConfig\",\n" +
        "    \"javaClass\": \"azkaban.test.executor.SleepJavaJob\",\n" +
        "    \"properties\": {\n" +
        "      \"seconds\": \"1\"\n" +
        "    }\n" +
        "  }\n" +
        "}",
        printer.print(jobConfig));
  }

  @Test
  public void testLoadJsonFlow() throws URISyntaxException {
    Logger logger = Logger.getLogger(this.getClass());
    DirectoryFlowLoader loader = new DirectoryFlowLoader(new Props(), logger);

    loader.load(project, TestExecutions.getFlowDir("embedded_json"));
    printErrors(loader);
    Assert.assertEquals(0, loader.getErrors().size());
    Assert.assertEquals(2, loader.getFlowMap().size());
  }
}
