azkaban = import "azkaban/azkaban.jsonnet";

local SleepOneSecondJob = azkaban.JavaProcessJob {
  java_class: "azkaban.test.executor.SleepJavaJob",
  properties: {
    seconds: 1,
    fail: false,
  }
};

local inner_job_a = SleepOneSecondJob {
  name: "inner_job_a",
};

local inner_job_b = SleepOneSecondJob {
  name: "inner_job_b",
  deps: [":inner_job_a"],
};

local inner_job_c = SleepOneSecondJob {
  name: "inner_job_c",
  deps: [":inner_job_a"],
};

local inner_job_d = SleepOneSecondJob {
  name: "inner_job_d",
  deps: [
    ":inner_job_b",
    ":inner_job_c",
  ],
};

azkaban.Flow {
  name: "inner_flow",
  jobs: [
    inner_job_a,
    inner_job_b,
    inner_job_c,
    inner_job_d,
  ]
}
