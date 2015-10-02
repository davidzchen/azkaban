azkaban = import "azkaban/azkaban.jsonnet";

azkaban.Project {
  name: "embedded",
  description: "An example project with embedded flows.",
  flows: [
    import "inner_flow.jsonnet",
    import "embedded.jsonnet",
  ],
}
