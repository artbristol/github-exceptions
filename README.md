
Slurps Github and runs PMD against commits.

You need a `~/.github` property file with your Github OAuth token in it:

`oauth=4d98173f7c075527cb64878561d1fe70`

(More details here http://github-api.kohsuke.org/)

Run with `./gradlew --daemon run` and hit http://localhost:8080/events.json

At first you will get an empty collection `[]` but after a while you should see PMD complaining about everyone's code:

```
[ {
  "pmdProblem" : "ClassWithOnlyPrivateConstructorsShouldBeFinal",
  "date" : 1439140221458,
  "commitUrl" : "https://github.com/PSSAppliedResearch/emr-data-analytics/blob/5643d69b03fcf4803b7e76fb676862b25f0ac957/models/src/main/java/emr/analytics/models/diagram/BasicDiagram.java#L11-L113",
  "lineNumber" : 11
}, {
  "pmdProblem" : "MissingStaticMethodInNonInstantiatableClass",
  "date" : 1439140221463,
  "commitUrl" : "https://github.com/PSSAppliedResearch/emr-data-analytics/blob/5643d69b03fcf4803b7e76fb676862b25f0ac957/models/src/main/java/emr/analytics/models/diagram/BasicDiagram.java#L11-L113",
  "lineNumber" : 11
}, {
  "pmdProblem" : "MissingSerialVersionUID",
  "date" : 1439140221464,
  "commitUrl" : "https://github.com/PSSAppliedResearch/emr-data-analytics/blob/5643d69b03fcf4803b7e76fb676862b25f0ac957/models/src/main/java/emr/analytics/models/diagram/BasicDiagram.java#L11-L113",
  "lineNumber" : 11
},
```

It will show the last 60 seconds' worth of complaints.

Angular2 frontend needs to go in src/main/resources/static
