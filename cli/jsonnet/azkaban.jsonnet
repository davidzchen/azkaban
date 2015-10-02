{
  EmailConfig:: {
    failure_emails: [],
    success_emails: [].
    notify_emails: [],
  },

  Job:: {
    name: "",
    type: "",
    deps: [],
    options: "null",
    retries: 0,
    retry_backoff: 0,
    working_dir: "",
    env_variables: {},
    email_config: "null"
  },

  ShellJob:: Job {
    type: "process",
    command: "",

    local shell = self,
    options: {
      "@type": "azkaban.ShellJobConfig",
      command: shell.command,
    }
  },

  JavaProcessJob:: Job {
    type: "javaprocess",
    java_class: "",
    properties: {},

    local java_process = self,
    options: {
      "@type": "azkaban.JavaProcessJobConfig",
      java_class: java_process.java_class,
      properties: java_process.properties,
    }
  },

  FlowJob:: Job {
    type: "flow",
    flow_name: "",

    local flow = self,
    options: {
      "@type": "azkaban.FlowJobConfig",
      flow_name = flow.flow_name,
    },
  },

  Flow:: {
    name: "",
    jobs: [],
  },

  Project:: {
    name: "",
    description: "",
    flows: [],
  },
}
