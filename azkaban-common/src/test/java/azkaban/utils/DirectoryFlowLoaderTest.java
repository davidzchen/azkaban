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

package azkaban.utils;

import java.io.File;
import java.net.URL;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import azkaban.project.Project;

public class DirectoryFlowLoaderTest {
  private Project project;

  @Before
  public void setUp() {
    project = new Project(11, "myTestProject");
  }

  private File getParentDirFile(final String path) throws URISyntaxException {
    URL url = getClass().getResource(path);
    Assert.assertNotNull(url);
    File resourceFile = new File(url.toURI());
    File parentDir = resourceFile.getParentFile();
    Assert.assertTrue(parentDir.isDirectory());
    return parentDir;
  }

  @Test
  public void testDirectoryLoad() throws Exception {
    Logger logger = Logger.getLogger(this.getClass());
    DirectoryFlowLoader loader = new DirectoryFlowLoader(new Props(), logger);

    loader.loadProjectFlow(project, getParentDirFile("exectest1/job1.job"));
    logger.info(loader.getFlowMap().size());
  }

  @Test
  public void testLoadEmbeddedFlow() throws Exception {
    Logger logger = Logger.getLogger(this.getClass());
    DirectoryFlowLoader loader = new DirectoryFlowLoader(new Props(), logger);

    loader.loadProjectFlow(project, getParentDirFile("embedded/joba.job"));
    Assert.assertEquals(0, loader.getErrors().size());
  }

  @Test
  public void testRecursiveLoadEmbeddedFlow() throws Exception {
    Logger logger = Logger.getLogger(this.getClass());
    DirectoryFlowLoader loader = new DirectoryFlowLoader(new Props(), logger);

    loader.loadProjectFlow(project, getParentDirFile("embedded_bad/joba.job"));
    for (String error : loader.getErrors()) {
      System.out.println(error);
    }

    // Should be 3 errors: jobe->innerFlow, innerFlow->jobe, innerFlow
    Assert.assertEquals(3, loader.getErrors().size());
  }
}
