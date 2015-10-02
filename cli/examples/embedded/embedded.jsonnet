azkaban = import "azkaban/azkaban.jsonnet";
inner_flow = import "inner_flow.jsonnet";

local job_a = azkaban.JavaProcessJob {
  name: "job_a",
  java_class: "azkaban.test.executor.SleepJavaJob",
  properties: {
    seconds: 1,
    fail: false,
  }
};

local EmbeddedFlowJob = azkaban.FlowJob {
  deps: [":job_a"],
  flow_name: inner_flow.name,
};

local job_b = EmbeddedFlowJob {
  name: "job_b",
};

local job_c = EmbeddedFlowJob {
  name: "job_c",
};

local job_d = EmbeddedFlowJob {
  name: "job_d",
};

local job_e = azkaban.JavaProcessJob {
  name: "job_e",
  deps: [
    ":job_b",
    ":job_c",
    ":job_d",
  ],
  java_class: "azkaban.test.executor.SleepJavaJob",
  properties: {
    seconds: 1,
    fail: false,
  }
}

azkaban.Flow {
  name: "embedded",
  jobs: [
    job_a,
    job_b,
    job_c,
    job_d,
    job_e,
  ]
}
