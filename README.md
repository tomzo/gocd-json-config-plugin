[![Build Status](https://snap-ci.com/tomzo/gocd-json-config-plugin/branch/master/build_image)](https://snap-ci.com/tomzo/gocd-json-config-plugin/branch/master)

# Go JSON configuration plugin

This is a Go server plugin which allows to keep pipelines and environments configuration
in version control systems supported by Go (git,svn,mercurial, etc.)

You can find an example repository at https://github.com/tomzo/gocd-json-config-example.git

## Quickstart

### Early access

This plugin uses configuration repository extension in Go.
[Feature](https://github.com/gocd/gocd/issues/1133) is not
merged into official gocd repository. Currently you have to
build Go from [my fork](https://github.com/tomzo/gocd) to try this out.

### Installation

First you must install the plugin in Go server.
You'll have to drop `.jar` to `/plugins` directory in your server installation.
Plugin jars can be downloaded from [snap](https://snap-ci.com/tomzo/gocd-json-config-plugin/branch/master)

### Add configuration repository

There is no UI to add configuration repositories so you'll have to edit the
config XML.
You will need to add `config-repo` section within `config-repos`.
If `config-repos` does not exist yet then you add it right above first `<pipelines />`.

To add all configurations from git repository `https://github.com/tomzo/gocd-json-config-example.git`
a section like this should be added:

```xml
...
<config-repos>
   <config-repo plugin="json.config.plugin">
     <git url="https://github.com/tomzo/gocd-json-config-example.git" />
   </config-repo>
</config-repos>
...
```

## Configuration files

Using this plugin you can store any number of pipeline or environment
configurations without a versioned repository like git.

By default pipelines should be stored in `*.gopipeline.json` files
and environments should be stored in `*.goenvironment.json` files.

The file name pattern can be changed on plugin configuration page.

## Format

The pipeline configuration files should be stored in format similar to
one exposed by [go API](https://api.go.cd/16.1.0/#get-pipeline-config).

The format of environment configuration files is much simpler,
you can find examples of correct environments at the [bottom](#environment).

#### Implementation note

This plugin leverages JSON message format used internally for Go server
and plugin communication. The only difference between these schemas is the
fact that in plugins format a pipeline can contain a `group` property. Plugin
transforms that into official schema where groups contain many pipelines.

Go pipeline and environment configuration has very deep structure. So instead
of reading a very long schema, below you can find examples of all configuration elements.

It is very close to [official xml schema](http://www.go.cd/documentation/user/16.1.0/configuration/configuration_reference.html)
and also [official JSONs in pipeline configuration API](https://api.go.cd/16.1.0/#get-pipeline-config)

1. [Pipeline](#pipeline)
 * [Mingle](#mingle)
 * [Tracking tool](#tracking tool)
1. [Stage](#stage)
1. [Job](#job)
1. [Tasks](#tasks)
 * [rake](#rake task)
 * [ant](#ant task)
 * [nant](#nant task)
 * [exec](#exec task)
 * [pluggabletask](#pluggable task)
1. [Materials](#materials)
 * [pipeline](#pipeline material)
 * [package](#package material)
 * [git](#git material)
 * [svn](#svn material)
 * [perforce](#perforce material)
 * [tfs](#tfs material)
 * [hg](#hg material)
 * [pluggablescm](#pluggable scm material)
1. [Environment](#environment)


### Pipeline

Example 'customPipeline':
```json
{
  "name": "pipe2",
  "labelTemplate": "foo-1.0-${COUNT}",
  "isLocked": true,
  "mingle": {
    "baseUrl": "http://mingle.example.com",
    "projectId": "my_project"
  },
  "timer": {
    "timerSpec": "0 15 10 * * ? *",
    "onlyOnChanges": false
  },
  "environmentVariables": [],
  "materials": [
    {
      "url": "gitrepo",
      "branch": "feature12",
      "filter": [
        "externals",
        "tools"
      ],
      "folder": "dir1",
      "autoUpdate": false,
      "materialName": "gitMaterial1",
      "type": "git"
    },
    {
      "pipelineName": "pipe1",
      "stageName": "build",
      "materialName": "pipe1",
      "type": "pipeline"
    }
  ],
  "stages": [
    {
      "name": "build",
      "fetchMaterials": true,
      "artifactCleanupProhibited": false,
      "cleanWorkingDir": false,
      "environmentVariables": [],
      "jobs": [
        {
          "name": "build",
          "environmentVariables": [],
          "tabs": [],
          "resources": [],
          "artifacts": [],
          "artifactPropertiesGenerators": [],
          "runOnAllAgents": false,
          "runInstanceCount": 0,
          "timeout": 0,
          "tasks": [
            {
              "type": "rake"
            }
          ]
        }
      ]
    }
  ]
}
```

Example 'pipe1':
```json
{
  "name": "pipe1",
  "isLocked": false,
  "environmentVariables": [],
  "materials": [
    {
      "url": "gitrepo",
      "branch": "feature12",
      "filter": [
        "externals",
        "tools"
      ],
      "folder": "dir1",
      "autoUpdate": false,
      "materialName": "gitMaterial1",
      "type": "git"
    }
  ],
  "stages": [
    {
      "name": "build",
      "fetchMaterials": true,
      "artifactCleanupProhibited": false,
      "cleanWorkingDir": false,
      "environmentVariables": [],
      "jobs": [
        {
          "name": "build",
          "environmentVariables": [],
          "tabs": [],
          "resources": [],
          "artifacts": [],
          "artifactPropertiesGenerators": [],
          "runOnAllAgents": false,
          "runInstanceCount": 0,
          "timeout": 0,
          "tasks": [
            {
              "type": "rake"
            }
          ]
        }
      ]
    }
  ]
}
```

#### Tracking tool

Example 'tracking':
```json
{
  "link": "http://your-trackingtool/yourproject/${ID}",
  "regex": "evo-(\\d+)"
}
```

#### Mingle

Example 'mingle':
```json
{
  "baseUrl": "http://mingle.example.com",
  "projectId": "my_project"
}
```

Example 'invalidNoId':
```json
{
  "baseUrl": "http://mingle.example.com"
}
```

Example 'invalidNoUrl':
```json
{
  "projectId": "my_project"
}
```

#### Stage

Example 'invalidSameJobNameTwice':
```json
{
  "name": "build",
  "fetchMaterials": true,
  "artifactCleanupProhibited": false,
  "cleanWorkingDir": false,
  "environmentVariables": [],
  "jobs": [
    {
      "name": "build",
      "environmentVariables": [],
      "tabs": [],
      "resources": [],
      "artifacts": [],
      "artifactPropertiesGenerators": [],
      "runOnAllAgents": false,
      "runInstanceCount": 0,
      "timeout": 0,
      "tasks": [
        {
          "type": "rake"
        }
      ]
    },
    {
      "name": "build",
      "environmentVariables": [],
      "tabs": [],
      "resources": [],
      "artifacts": [],
      "artifactPropertiesGenerators": [],
      "runOnAllAgents": false,
      "runInstanceCount": 0,
      "timeout": 0,
      "tasks": [
        {
          "type": "rake"
        },
        {
          "buildFile": "Rakefile.rb",
          "target": "compile",
          "type": "rake"
        }
      ]
    }
  ]
}
```


Example 'stageWith2Jobs':
```json
{
  "name": "build",
  "fetchMaterials": true,
  "artifactCleanupProhibited": false,
  "cleanWorkingDir": false,
  "environmentVariables": [],
  "jobs": [
    {
      "name": "build",
      "environmentVariables": [],
      "tabs": [],
      "resources": [],
      "artifacts": [],
      "artifactPropertiesGenerators": [],
      "runOnAllAgents": false,
      "runInstanceCount": 0,
      "timeout": 0,
      "tasks": [
        {
          "type": "rake"
        },
        {
          "buildFile": "Rakefile.rb",
          "target": "compile",
          "type": "rake"
        }
      ]
    },
    {
      "name": "test",
      "environmentVariables": [],
      "tabs": [],
      "resources": [
        "linux"
      ],
      "artifacts": [],
      "artifactPropertiesGenerators": [],
      "runOnAllAgents": false,
      "runInstanceCount": 0,
      "timeout": 0,
      "tasks": [
        {
          "type": "ant"
        }
      ]
    }
  ]
}
```


Example 'stageWithApproval':
```json
{
  "name": "deploy",
  "fetchMaterials": true,
  "artifactCleanupProhibited": false,
  "cleanWorkingDir": false,
  "approval": {
    "type": "manual",
    "authorizedUsers": [],
    "authorizedRoles": [
      "manager"
    ]
  },
  "environmentVariables": [],
  "jobs": [
    {
      "name": "build",
      "environmentVariables": [],
      "tabs": [],
      "resources": [],
      "artifacts": [],
      "artifactPropertiesGenerators": [],
      "runOnAllAgents": false,
      "runInstanceCount": 0,
      "timeout": 0,
      "tasks": [
        {
          "type": "rake"
        }
      ]
    }
  ]
}
```

Example 'stage':
```json
{
  "name": "build",
  "fetchMaterials": true,
  "artifactCleanupProhibited": false,
  "cleanWorkingDir": false,
  "environmentVariables": [],
  "jobs": [
    {
      "name": "build",
      "environmentVariables": [],
      "tabs": [],
      "resources": [],
      "artifacts": [],
      "artifactPropertiesGenerators": [],
      "runOnAllAgents": false,
      "runInstanceCount": 0,
      "timeout": 0,
      "tasks": [
        {
          "type": "rake"
        }
      ]
    }
  ]
}
```

Example 'stageWithEnv':
```json
{
  "name": "test",
  "fetchMaterials": true,
  "artifactCleanupProhibited": false,
  "cleanWorkingDir": false,
  "environmentVariables": [
    {
      "name": "TEST_NUM",
      "value": "1"
    }
  ],
  "jobs": [
    {
      "name": "test",
      "environmentVariables": [],
      "tabs": [],
      "resources": [
        "linux"
      ],
      "artifacts": [],
      "artifactPropertiesGenerators": [],
      "runOnAllAgents": false,
      "runInstanceCount": 0,
      "timeout": 0,
      "tasks": [
        {
          "type": "ant"
        }
      ]
    }
  ]
}
```


#### Job

Example 'jobWithVar':
```json
{
  "name": "build",
  "environmentVariables": [
    {
      "name": "key1",
      "value": "value1"
    }
  ],
  "tabs": [],
  "resources": [],
  "artifacts": [],
  "artifactPropertiesGenerators": [],
  "runOnAllAgents": false,
  "runInstanceCount": 0,
  "timeout": 0,
  "tasks": [
    {
      "type": "rake"
    }
  ]
}
```

Example 'jobWithTab':
```json
{
  "name": "test",
  "environmentVariables": [],
  "tabs": [
    {
      "name": "test",
      "path": "results.xml"
    }
  ],
  "resources": [],
  "artifacts": [],
  "artifactPropertiesGenerators": [],
  "runOnAllAgents": false,
  "runInstanceCount": 0,
  "timeout": 0,
  "tasks": [
    {
      "type": "ant"
    }
  ]
}
```

Example 'jobWithProp':
```json
{
  "name": "perfTest",
  "environmentVariables": [],
  "tabs": [],
  "resources": [],
  "artifacts": [],
  "artifactPropertiesGenerators": [
    {
      "name": "perf",
      "src": "test.xml",
      "xpath": "substring-before(//report/data/all/coverage[starts-with(@type,\u0027class\u0027)]/@value, \u0027%\u0027)"
    }
  ],
  "runOnAllAgents": false,
  "runInstanceCount": 0,
  "timeout": 0,
  "tasks": [
    {
      "type": "rake"
    }
  ]
}
```

Example 'jobWithResource':
```json
{
  "name": "test",
  "environmentVariables": [],
  "tabs": [],
  "resources": [
    "linux"
  ],
  "artifacts": [],
  "artifactPropertiesGenerators": [],
  "runOnAllAgents": false,
  "runInstanceCount": 0,
  "timeout": 0,
  "tasks": [
    {
      "type": "ant"
    }
  ]
}
```

Example 'buildRake':
```json
{
  "name": "build",
  "environmentVariables": [],
  "tabs": [],
  "resources": [],
  "artifacts": [],
  "artifactPropertiesGenerators": [],
  "runOnAllAgents": false,
  "runInstanceCount": 0,
  "timeout": 0,
  "tasks": [
    {
      "type": "rake"
    }
  ]
}
```

Example 'build2Rakes':
```json
{
  "name": "build",
  "environmentVariables": [],
  "tabs": [],
  "resources": [],
  "artifacts": [],
  "artifactPropertiesGenerators": [],
  "runOnAllAgents": false,
  "runInstanceCount": 0,
  "timeout": 0,
  "tasks": [
    {
      "type": "rake"
    },
    {
      "buildFile": "Rakefile.rb",
      "target": "compile",
      "type": "rake"
    }
  ]
}
```


#### PropertyGenerator


Example 'propGen':
```json
{
  "name": "coverage.class",
  "src": "target/emma/coverage.xml",
  "xpath": "substring-before(//report/data/all/coverage[starts-with(@type,\u0027class\u0027)]/@value, \u0027%\u0027)"
}
```


#### Timer

Example 'timer':
```json
{
  "timerSpec": "0 15 10 * * ? *",
  "onlyOnChanges": false
}
```

#### Approval

Approval must have `type` - either "manual" or "success".

Example 'manualWithAuth':
```json
{
  "type": "manual",
  "authorizedUsers": [],
  "authorizedRoles": [
    "manager"
  ]
}
```

Example 'manual':
```json
{
  "type": "manual",
  "authorizedUsers": [],
  "authorizedRoles": []
}
```


Example 'success':
```json
{
  "type": "success",
  "authorizedUsers": [],
  "authorizedRoles": []
}
```

#### Tab

Example 'tab':
```json
{
  "name": "results",
  "path": "test.xml"
}
```

#### Artifact

Artifact must have source directory

Example 'artifact':
```json
{
  "source": "src",
  "destination": "dest"
}
```

#### Tasks

Any task must have `type`. Valid types are:
 * rake
 * ant
 * nant
 * exec
 * pluggabletask

##### Ant task

Example 'antCompileTask':
```json
{
  "target": "compile",
  "type": "ant"
}
```

Example 'antTask':
```json
{
  "type": "ant"
}
```

Example 'antCompileFileTask':
```json
{
  "buildFile": "mybuild.xml",
  "target": "compile",
  "type": "ant"
}
```

Example 'antWithDirTask':
```json
{
  "target": "build",
  "workingDirectory": "src/tasks",
  "type": "ant"
}
```

#### Rake task

Example 'rakeWithDirTask':
```json
{
  "target": "build",
  "workingDirectory": "src/tasks",
  "type": "rake"
}
```

Example 'rakeCompileTask':
```json
{
  "target": "compile",
  "type": "rake"
}
```

Example 'rakeCompileFileTask':
```json
{
  "buildFile": "Rakefile.rb",
  "target": "compile",
  "type": "rake"
}
```

Example 'rakeTask':
```json
{
  "type": "rake"
}
```

#### Exec task

Example 'execInDir':
```json
{
  "command": "/usr/local/bin/rake",
  "workingDirectory": "myProjectDir",
  "args": [],
  "type": "exec"
}
```

Example 'customExec':
```json
{
  "command": "rake",
  "workingDirectory": "dir",
  "timeout": 120,
  "args": [
    "-f",
    "Rakefile.rb"
  ],
  "type": "exec",
  "runIf": "any",
  "onCancel": {
    "command": "/usr/local/bin/ruby",
    "args": [],
    "type": "exec"
  }
}
```

Example 'simpleExec':
```json
{
  "command": "/usr/local/bin/ruby",
  "args": [],
  "type": "exec"
}
```

Example 'simpleExecWithArgs':
```json
{
  "command": "/usr/local/bin/ruby",
  "args": [
    "backup.rb"
  ],
  "type": "exec"
}
```

Example 'simpleExecRunIf':
```json
{
  "command": "/usr/local/bin/ruby",
  "args": [],
  "type": "exec",
  "runIf": "failed"
}
```

Example 'invalidNoCommand':
```json
{
  "args": [],
  "type": "exec"
}
```


#### Fetch artifact task

Example 'fetch':
```json
{
  "stage": "build",
  "job": "buildjob",
  "source": "bin",
  "sourceIsDir": true,
  "type": "fetchartifact"
}
```

Example 'fetchFromPipe':
```json
{
  "pipelineName": "pipeline1",
  "stage": "build",
  "job": "buildjob",
  "source": "bin",
  "sourceIsDir": true,
  "type": "fetchartifact"
}
```

Example 'fetchToDest':
```json
{
  "stage": "build",
  "job": "buildjob",
  "source": "bin",
  "sourceIsDir": true,
  "destination": "lib",
  "type": "fetchartifact"
}
```

Example 'invalidFetchNoSource':
```json
{
  "stage": "build",
  "job": "buildjob",
  "sourceIsDir": true,
  "type": "fetchartifact"
}
```

Example 'invalidFetchNoStage':
```json
{
  "job": "buildjob",
  "source": "bin",
  "sourceIsDir": true,
  "type": "fetchartifact"
}
```

Example 'invalidFetchNoJob':
```json
{
  "stage": "build",
  "source": "bin",
  "sourceIsDir": true,
  "type": "fetchartifact"
}
```

#### Nant task

Example 'nantWithDirTask':
```json
{
  "target": "build",
  "workingDirectory": "src/tasks",
  "type": "nant"
}
```

Example 'nantWithPath':
```json
{
  "nantPath": "/path/to/nant",
  "buildFile": "mybuild.xml",
  "target": "build",
  "workingDirectory": "src/tasks",
  "type": "nant"
}
```

Example 'nantCompileTask':
```json
{
  "target": "compile",
  "type": "nant"
}
```

Example 'nantTask':
```json
{
  "type": "nant"
}
```

Example 'nantCompileFileTask':
```json
{
  "buildFile": "mybuild.xml",
  "target": "compile",
  "type": "nant"
}
```

#### Pluggable task

Example 'invalidNoPlugin':
```json
{
  "type": "pluggabletask"
}
```

Example 'invalidDuplicatedKeys':
```json
{
  "pluginConfiguration": {
    "id": "curl.task.plugin",
    "version": "1"
  },
  "configuration": [
    {
      "key": "Url",
      "value": "http://www.google.com"
    },
    {
      "key": "Url",
      "value": "http://www.gg.com"
    }
  ],
  "type": "pluggabletask"
}
```

Example 'curl':
```json
{
  "pluginConfiguration": {
    "id": "curl.task.plugin",
    "version": "1"
  },
  "configuration": [
    {
      "key": "Url",
      "value": "http://www.google.com"
    },
    {
      "key": "SecureConnection",
      "value": "no"
    },
    {
      "key": "RequestType",
      "value": "no"
    }
  ],
  "type": "pluggabletask"
}
```

Example 'example':
```json
{
  "pluginConfiguration": {
    "id": "example.task.plugin",
    "version": "1"
  },
  "configuration": [],
  "type": "pluggabletask"
}
```

### Materials

Material must have `type`. Valid types are:
 * pipeline
 * package
 * git
 * svn
 * p4
 * tfs
 * hg
 * pluggablescm

#### Pipeline material

Example 'dependsOnPipeline':
```json
{
  "pipelineName": "pipeline2",
  "stageName": "build",
  "type": "pipeline"
}
```

Example 'namedDependsOnPipeline':
```json
{
  "pipelineName": "pipeline2",
  "stageName": "build",
  "materialName": "pipe2",
  "type": "pipeline"
}
```

Example 'invalidNoPipeline':
```json
{
  "stageName": "build",
  "type": "pipeline"
}
```

Example 'invalidNoStage':
```json
{
  "pipelineName": "pipeline1",
  "type": "pipeline"
}
```

#### Package material

Example 'namedPackageMaterial':
```json
{
  "packageId": "apt-repo-id",
  "materialName": "myapt",
  "type": "package"
}
```

Example 'packageMaterial':
```json
{
  "packageId": "apt-package-plugin-id",
  "type": "package"
}
```

Example 'invalidPackageMaterialNoId':
```json
{
  "type": "package"
}
```

#### Git material

Example 'simpleGit':
```json
{
  "url": "http://my.git.repository.com",
  "filter": [],
  "autoUpdate": true,
  "type": "git"
}
```

Example 'invalidNoUrl':
```json
{
  "branch": "feature12",
  "filter": [
    "externals",
    "tools"
  ],
  "folder": "dir1",
  "autoUpdate": false,
  "materialName": "gitMaterial1",
  "type": "git"
}
```

Example 'simpleGitBranch':
```json
{
  "url": "http://other.git.repository.com",
  "branch": "develop",
  "filter": [],
  "autoUpdate": true,
  "type": "git"
}
```

Example 'veryCustomGit':
```json
{
  "url": "http://my.git.repository.com",
  "branch": "feature12",
  "filter": [
    "externals",
    "tools"
  ],
  "folder": "dir1",
  "autoUpdate": false,
  "materialName": "gitMaterial1",
  "type": "git"
}
```


#### Hg material

Example 'invalidHgNoUrl':
```json
{
  "filter": [],
  "autoUpdate": true,
  "type": "hg"
}
```

Example 'customHg':
```json
{
  "url": "repos/myhg",
  "filter": [
    "externals",
    "tools"
  ],
  "folder": "dir1",
  "autoUpdate": false,
  "materialName": "hgMaterial1",
  "type": "hg"
}
```

Example 'simpleHg':
```json
{
  "url": "myHgRepo",
  "filter": [],
  "autoUpdate": true,
  "type": "hg"
}
```

#### Perforce material

Example 'p4simple':
```json
{
  "serverAndPort": "10.18.3.102:1666",
  "view": "//depot/dev/src...          //anything/src/...",
  "filter": [],
  "autoUpdate": true,
  "type": "p4"
}
```

Example 'p4custom':
```json
{
  "serverAndPort": "10.18.3.102:1666",
  "userName": "user1",
  "password": "pass1",
  "useTickets": false,
  "view": "//depot/dev/src...          //anything/src/...",
  "filter": [
    "lib",
    "tools"
  ],
  "folder": "dir1",
  "autoUpdate": false,
  "materialName": "p4materialName",
  "type": "p4"
}
```

Example 'invalidP4NoServer':
```json
{
  "view": "//depot/dev/src...          //anything/src/...",
  "filter": [],
  "autoUpdate": true,
  "type": "p4"
}
```

Example 'invalidP4NoView':
```json
{
  "serverAndPort": "10.18.3.102:1666",
  "filter": [],
  "autoUpdate": true,
  "type": "p4"
}
```

Example 'invalidPasswordAndEncyptedPasswordSet':
```json
{
  "serverAndPort": "10.18.3.102:1666",
  "password": "pa$sw0rd",
  "encryptedPassword": "26t\u003d$j64",
  "filter": [],
  "autoUpdate": true,
  "type": "p4"
}
```


#### Svn material

Example 'simpleSvnAuth':
```json
{
  "url": "http://myprivaterepo",
  "userName": "john",
  "password": "pa$sw0rd",
  "checkExternals": false,
  "filter": [],
  "autoUpdate": true,
  "type": "svn"
}
```

Example 'customSvn':
```json
{
  "url": "http://svn",
  "userName": "user1",
  "password": "pass1",
  "checkExternals": true,
  "filter": [
    "tools",
    "lib"
  ],
  "folder": "destDir1",
  "autoUpdate": false,
  "materialName": "svnMaterial1",
  "type": "svn"
}
```

Example 'simpleSvn':
```json
{
  "url": "http://mypublicrepo",
  "checkExternals": false,
  "filter": [],
  "autoUpdate": true,
  "type": "svn"
}
```

Example 'invalidPasswordAndEncyptedPasswordSet':
```json
{
  "url": "http://myprivaterepo",
  "password": "pa$sw0rd",
  "encryptedPassword": "26t\u003d$j64",
  "checkExternals": false,
  "filter": [],
  "autoUpdate": true,
  "type": "svn"
}
```

Example 'invalidNoUrl':
```json
{
  "checkExternals": false,
  "filter": [],
  "autoUpdate": true,
  "type": "svn"
}
```

#### Tfs material

Example 'simpleTfs':
```json
{
  "url": "url1",
  "userName": "user1",
  "projectPath": "projectDir",
  "filter": [],
  "autoUpdate": true,
  "type": "tfs"
}
```

Example 'customTfs':
```json
{
  "url": "url3",
  "userName": "user4",
  "domain": "example.com",
  "password": "pass",
  "projectPath": "projectDir",
  "filter": [
    "tools",
    "externals"
  ],
  "folder": "dir1",
  "autoUpdate": false,
  "materialName": "tfsMaterialName",
  "type": "tfs"
}
```

#### Pluggable scm material

Example 'invalidNoScmId':
```json
{
  "type": "pluggablescm"
}
```

Example 'simpleNamedPluggableGit':
```json
{
  "scmId": "mygit-id",
  "materialName": "myGitMaterial",
  "type": "pluggablescm"
}
```

Example 'pluggableGitWithFilter':
```json
{
  "scmId": "someScmGitRepositoryId",
  "folder": "destinationDir",
  "filter": [
    "mydir"
  ],
  "materialName": "myPluggableGit",
  "type": "pluggablescm"
}
```

Example 'simplePluggableGit':
```json
{
  "scmId": "mygit-id",
  "type": "pluggablescm"
}
```

Example 'pluggableGit':
```json
{
  "scmId": "someScmGitRepositoryId",
  "folder": "destinationDir",
  "filter": [],
  "materialName": "myPluggableGit",
  "type": "pluggablescm"
}
```

Example 'pluggableGitWith2Filters':
```json
{
  "scmId": "someScmGitRepositoryId",
  "folder": "destinationDir",
  "filter": [
    "dir1",
    "dir2"
  ],
  "materialName": "myPluggableGit",
  "type": "pluggablescm"
}
```

#### Environment


Example 'devWithVariable':
```json
{
  "name": "dev",
  "environmentVariables": [
   {
     "name": "key1",
     "value": "value1"
   }
 ],
}
```

Example 'empty':
```json
{
  "name": "dev",
  "environmentVariables": [],
  "agents": [],
  "pipelines": []
}
```

Example 'uatWithPipeline':
```json
{
  "name": "UAT",
  "environmentVariables": [],
  "agents": [],
  "pipelines": [
    "pipeline1"
  ]
}
```

##### Invalid environment examples

Example 'invalidSamePipelineTwice':
```json
{
  "name": "badenv3",
  "environmentVariables": [],
  "agents": [],
  "pipelines": [
    "pipe1",
    "pipe1"
  ]
}
```

Example 'invalidSameEnvironmentVariableTwice':
```json
{
  "name": "badenv",
  "environmentVariables": [
    {
      "name": "key",
      "value": "value1"
    },
    {
      "name": "key",
      "value": "value2"
    }
  ],
  "agents": [],
  "pipelines": []
}
```

Example 'invalidSameAgentTwice':
```json
{
  "name": "badenv2",
  "environmentVariables": [],
  "agents": [
    "123",
    "123"
  ],
  "pipelines": []
}
```

#### EnvironmentVariable

Environment variable can be used in environments, pipelines, stages and jobs.
All support encrypted and plain values.

Example 'key1':
```json
{
  "name": "key1",
  "value": "value1"
}
```

##### invalid environment variables

Example 'invalid2ValuesSet':
```json
{
  "name": "keyd",
  "value": "value1",
  "encryptedValue": "v123445"
}
```

Example 'invalidValueNotSet':
```json
{
  "name": "key5"
}
```

Example 'invalidNameNotSet':
```json
{
  "value": "23"
}
```

#### PluginConfiguration

Example 'pluginConfig':
```json
{
  "id": "curl.task.plugin",
  "version": "1"
}
```

#### ConfigurationProperty

Example 'configProperty':
```json
{
  "key": "key1",
  "value": "value1"
}
```

Example 'configPropertyEncrypted':
```json
{
  "key": "secret",
  "encryptedValue": "213476%$"
}
```
