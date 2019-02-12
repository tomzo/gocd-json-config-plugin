[![Join the chat at https://gitter.im/gocd/gocd](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/gocd/configrepo-plugins?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# GoCD JSON configuration plugin

[![Build Status](https://travis-ci.com/tomzo/gocd-json-config-plugin.svg?branch=master)](https://travis-ci.com/tomzo/gocd-json-config-plugin)

**GoCD pipeline configuration as code**

This is a [GoCD](https://www.gocd.org) server plugin which allows to keep **pipelines** and **environments** configuration
in version control systems supported by GoCD (git, svn, mercurial, etc.).
See [this document](https://docs.gocd.org/current/advanced_usage/pipelines_as_code.html)
to find out what GoCD's configuration repositories are.

### Resources:

 * You can find an example repository at https://github.com/tomzo/gocd-json-config-example.git
 * Original GoCD [issue](https://github.com/gocd/gocd/issues/1133) on GitHub

## Quickstart

Make sure you are running at least `16.8.0` GoCD server.

### Installation

If you're using a GoCD version *older than 17.8.0*, you need to install the plugin in the GoCD server.

You'll have to drop `.jar` to `plugins/external` [directory](https://docs.gocd.org/current/extension_points/plugin_user_guide.html) in your server installation.
Plugin jars can be downloaded from [releases page](https://github.com/tomzo/gocd-json-config-plugin/releases).

### Add configuration repository

There is no UI to add configuration repositories so you'll have to edit the config XML.
You will need to add `config-repo` section within `config-repos`.
If `config-repos` does not exist yet then you add it **above first `<pipelines />` and `<artifactStores/>`**.

To add all configurations from git repository `https://github.com/tomzo/gocd-json-config-example.git`
a section like this should be added:

```xml
...
<config-repos>
   <config-repo pluginId="json.config.plugin" id="repo1">
     <git url="https://github.com/tomzo/gocd-json-config-example.git" />
   </config-repo>
</config-repos>
...
<artifactStores />
<pipelines />
...
```

## Configuration files

Using this plugin you can store any number of pipeline or environment
configurations without a versioned repository like git.

By default **pipelines** should be stored in `*.gopipeline.json` files
and **environments** should be stored in `*.goenvironment.json` files.

The file name pattern can be changed on plugin configuration page.

## Format

The pipeline configuration files should be stored in format *similar* to
one exposed by [GoCD API](https://api.gocd.org/current#get-pipeline-config).

The format of environment configuration files is much simpler,
you can find examples of correct environments at the [bottom](#environment).

### Format version

Please note that it is now recommended to declare `format_version` in each `*.gopipeline.json` or `*.goenvironment.json` file.
Version `3` will be introduced in GoCD 18.7.0
Currently it is recommended to declare consistent version in all your files:
```
{
  "format_version" : 3
}
```

# Syntax checking

Since `0.3.2` plugin is an executable and supports basic syntax checking.

## Usage with java installed

You need to download the `jar` from releases page and place it somewhere convenient.
For example `/usr/lib/gocd-json-plugin/json-config-plugin.jar`.
Then to validate your `gopipeline.json` file run something like:
```
java -jar /usr/lib/gocd-json-plugin/json-config-plugin.jar syntax mypipe.gopipeline.json
```

## Usage with `gocd` command-line helper

This is a product in development, so its command syntax is not stable and there are no distributed binaries yet.

The `gocd` tool is built in [golang](https://golang.org/) so you will need to familiarize yourself with how to set up your [go workspace](https://golang.org/doc/code.html#Workspaces).

Build the `gocd` binary:

```bash
go get github.com/gocd-contrib/gocd-cli
cd ${GOPATH:-~/go}/src/github.com/gocd-contrib/gocd-cli
./build.sh
```

Follow the steps on [https://github.com/gocd-contrib/gocd-cli](https://github.com/gocd-contrib/gocd-cli) to install the plugin jar to the correct place.

Then:

```bash
./gocd configrepo syntax -i json.config.plugin /path/to/your-pipeline.gopipeline.json
```

## Usage with IDE and docker

[IDE](https://github.com/ai-traders/ide) is a bash script, a cli wrapper around docker to help with running development tasks in docker.
You can install the `ide` script so that it is available on the PATH with:
```
sudo bash -c "`curl -L https://raw.githubusercontent.com/ai-traders/ide/master/install.sh`"
```

Add `Idefile` in your project with following content
```
IDE_DOCKER_IMAGE=tomzo/gocd-json-ide:<plugin-version>
```

To validate files run:
```
ide gocd-json syntax mypipe.gopipeline.json
```

Personally, I recommend the following project structure:

 * `gocd/` directory for all your GoCD configuration files.
 * `gocd/Idefile` file pointing which docker image can be used to validate configuration.

Then when working with gocd pipelines config, you can run from the root of your project
```
cd gocd
ide      # will open interactive shell
watch gocd-json syntax mypipe.gopipeline.json
```

## Usage with docker only

```
docker run -ti --rm --volume $(pwd):/ide/work tomzo/gocd-json-ide:<plugin-version> bash
```
Then you have an interactive shell as above.

#### Implementation note

This plugin leverages JSON message format used internally for GoCD server
and plugin communication.

GoCD pipeline and environment configuration has very deep structure. So instead
of reading a very long schema, below you can find examples of all configuration elements.

It is exactly like documented [here](https://github.com/tomzo/documentation/blob/1133-configrepo-extension/developer/writing_go_plugins/configrepo/version_1_0/config_objects.md)

It is **close to** [official xml schema](https://docs.gocd.org/16.1.0/configuration/configuration_reference.html)
and also [official JSONs in pipeline configuration API](https://api.gocd.org/current/#get-pipeline-config)

## JSON Configuration objects

1. [Environment](#environment)
1. [Environment variables](#environment-variables)
1. [Pipeline](#pipeline)
    * [Locking](#pipeline-locking)
    * [Mingle](#mingle)
    * [Tracking tool](#tracking tool)
    * [Timer](#timer)
1. [Stage](#stage)
    * [Approval](#approval)
1. [Job](#job)
    * [Property](#property)
    * [Tab](#tab)
    * [Many instances](#run-many-instances)
1. [Tasks](#tasks)
    * [rake](#rake)
    * [ant](#ant)
    * [nant](#nant)
    * [exec](#exec)
    * [fetch](#fetch)
    * [pluggabletask](#plugin)
1. [Materials](#materials)
    * [dependency](#dependency)
    * [package](#package)
    * [git](#git)
    * [svn](#svn)
    * [perforce](#perforce)
    * [tfs](#tfs)
    * [hg](#hg)
    * [pluggable scm](#pluggable-scm)
    * [configrepo](#configrepo)

# Environment

Configures a [GoCD environment](https://docs.gocd.org/current/configuration/managing_environments.html)

```json
{
  "name": "dev",
  "environment_variables": [
    {
      "name": "key1",
      "value": "value1"
    }
  ],
  "agents": [
    "123"
  ],
  "pipelines": [
    "mypipeline1"
  ]
}
```

# Environment variables

Environment variables is a JSON array that can be declared in Environments, Pipelines, Stages and Jobs.

Any variable must contain `name` and `value` or `encrypted_value`.

```json
{
  "environment_variables": [
    {
      "name": "key1",
      "value": "value1"
    },
    {
      "name": "keyd",
      "encrypted_value": "v12@SDfwez"
    }
  ]
}
```

# Pipeline

```json
{
  "format_version" : 1,
  "group": "group1",
  "name": "pipe2",
  "label_template": "foo-1.0-${COUNT}",
  "enable_pipeline_locking" : false,
  "parameters": [
    {
      "name": "param",
      "value": "parameter"
    }
  ],
  "mingle": {
    "base_url": "http://mingle.example.com",
    "project_identifier": "my_project"
  },
  "tracking_tool": null,
  "timer": {
    "spec": "0 15 10 * * ? *"
  },
  "environment_variables": [],
  "materials": [
    ...
  ],
  "stages": [
    ...
  ]
}
```

#### Referencing an existing template in a config repo:

```json
{
  "format_version" : 1,
  "group": "group1",
  "name": "pipe-with-template",
  "label_template": "foo-1.0-${COUNT}",
  "enable_pipeline_locking" : false,
  "template": "template1",
  "parameters": [
    {
      "name": "param",
      "value": "parameter"
    }
  ],
  "materials": [
    ...
  ]
}
```

Please note:

 * Pipeline declares a group to which it belongs

### Pipeline locking

Expected since GoCD v17.12, you need to use `lock_behavior` rather than `enable_pipeline_locking`.
```
"lock_behavior" : "none"
```

`lock_behavior` can be one of:
 * `lockOnFailure` - same as `enable_pipeline_locking: true`
 * `unlockWhenFinished` -
 * `none` - same `enable_pipeline_locking: false`


### Mingle

```json
{
    "base_url": "https://mingle.example.com",
    "project_identifier": "foobar_widgets",
    "mql_grouping_conditions": "status > 'In Dev'"
}
```

### Tracking tool

```json
{
  "link": "http://your-trackingtool/yourproject/${ID}",
  "regex": "evo-(\\d+)"
}
```

### Timer

```json
{
    "spec": "0 0 22 ? * MON-FRI",
    "only_on_changes": true
}
```

# Stage

```json
{
  "name": "test",
  "fetch_materials": true,
  "never_cleanup_artifacts": false,
  "clean_working_directory": false,
  "approval" : null,
  "environment_variables": [
    {
      "name": "TEST_NUM",
      "value": "1"
    }
  ],
  "jobs": [
    ...
  ]
}
```

### Approval

```json
{
  "type": "manual",
  "users": [],
  "roles": [
    "manager"
  ]
}
```

# Job

```json
{
  "name": "test",
  "run_instance_count" : null,
  "environment_variables": [],
  "timeout": 180,
  "elastic_profile_id": "docker-big-image-1",
  "tabs": [
     {
       "name": "test",
       "path": "results.xml"
     }
   ],
   "resources": [
    "linux"
  ],
  "artifacts": [
    {
      "source": "src",
      "destination": "dest",
      "type": "test"
    },
    {
      "type": "external",
      "id": "docker-release-candidate",
      "store_id": "dockerhub",
      "configuration": [
        {
          "key": "Image",
          "value": "gocd/gocd-demo"
        },
        {
          "key": "Tag",
          "value": "${GO_PIPELINE_COUNTER}"
        },
        {
          "key": "some_secure_property",
          "encrypted_value": "!@ESsdD323#sdu"
        }
      ]
    }
  ],
  "properties": [
    {
      "name": "perf",
      "source": "test.xml",
      "xpath": "substring-before(//report/data/all/coverage[starts-with(@type,\u0027class\u0027)]/@value, \u0027%\u0027)"
    }
  ],
  "tasks": [
    ...
  ]
}
```

### Artifacts

There are 3 types of artifacts recognized by GoCD. `Build` and `Test` artifacts are stored on the GoCD server.
The source and the destination of the artifact that should be stored on the GoCD server must be specified.

#### Build

```json
{
  "source": "src",
  "destination": "dest",
  "type": "build"
}
```

#### Test

```json
{
  "source": "src",
  "destination": "dest",
  "type": "test"
}
```

#### External

Artifacts of type `external` are stored in an artifact store outside of GoCD.
The external artifact store's configuration must be created in the main GoCD config. Support for external artifact store config to be checked in as yaml is not available.
The external artifact store is referenced by the `store_id`. The build specific artifact details that the artifact plugin needs to publish the artifact is provided as `configuration`.

```json
{
  "type": "external",
  "id": "docker-release-candidate",
  "store_id": "dockerhub",
  "configuration": [
    {
      "key": "Image",
      "value": "gocd/gocd-demo"
    },
    {
      "key": "Tag",
      "value": "${GO_PIPELINE_COUNTER}"
    },
    {
      "key": "some_secure_property",
      "encrypted_value": "!@ESsdD323#sdu"
    }
  ]
}
```

### Property

```json
{
  "name": "coverage.class",
  "source": "target/emma/coverage.xml",
  "xpath": "substring-before(//report/data/all/coverage[starts-with(@type,'class')]/@value, '%')"
}
```

### Tab

```json
{
      "name": "cobertura",
      "path": "target/site/cobertura/index.html"
}
```

### Run many instances

Part of **job** object can be [number of job to runs](https://docs.gocd.org/current/advanced_usage/admin_spawn_multiple_jobs.html)
```json
"run_instance_count" : 6
```
Or to run on all agents
```json
"run_instance_count" : "all"
```
Default is `null` which runs just one job.

# Materials

All materials:

 * must have `type` - `git`, `svn`, `hg`, `p4`, `tfs`, `dependency`, `package`, `plugin`.
 * can have `name` and must have `name` when there is more than one material in pipeline

SCM-related materials have `destination` field.

### Filter - blacklist and whitelist

All scm materials can have filter object:

 * for **blacklisting**:
```json
"filter": {
  "ignore": [
    "externals",
    "tools"
  ]
}
```

* for **whitelisting** (since Go `>=16.7.0`):
```json
"filter": {
 "whitelist": [
   "moduleA"
 ]
}
```

## Git

```json
{
  "url": "http://my.git.repository.com",
  "branch": "feature12",
  "filter": {
    "ignore": [
      "externals",
      "tools"
    ]
  },
  "destination": "dir1",
  "auto_update": false,
  "name": "gitMaterial1",
  "type": "git",
  "shallow_clone": true
}
```

## Svn

```json
{
  "url": "http://svn",
  "username": "user1",
  "password": "pass1",
  "check_externals": true,
  "filter": {
    "ignore": [
      "tools",
      "lib"
    ]
  },
  "destination": "destDir1",
  "auto_update": false,
  "name": "svnMaterial1",
  "type": "svn"
}
```

Instead of plain `password` you may specify `encrypted_password` with encrypted content
which usually makes more sense considering that value is stored in SCM.

## Hg

```json
{
  "url": "repos/myhg",
  "filter": {
    "ignore": [
      "externals",
      "tools"
    ]
  },
  "destination": "dir1",
  "auto_update": false,
  "name": "hgMaterial1",
  "type": "hg"
}
```

## Perforce

```json
{
  "port": "10.18.3.102:1666",
  "username": "user1",
  "password": "pass1",
  "use_tickets": false,
  "view": "//depot/dev/src...          //anything/src/...",
  "filter": {
    "ignore": [
      "lib",
      "tools"
    ]
  },
  "destination": "dir1",
  "auto_update": false,
  "name": "p4materialName",
  "type": "p4"
}
```

Instead of plain `password` you may specify `encrypted_password` with encrypted content
which usually makes more sense considering that value is stored in SCM.

## Tfs

```json
{
  "url": "url3",
  "username": "user4",
  "domain": "example.com",
  "password": "pass",
  "project": "projectDir",
  "filter": {
    "ignore": [
      "tools",
      "externals"
    ]
  },
  "destination": "dir1",
  "auto_update": false,
  "name": "tfsMaterialName",
  "type": "tfs"
}
```

Instead of plain `password` you may specify `encrypted_password` with encrypted content
which usually makes more sense considering that value is stored in SCM.

## Dependency

```json
{
  "pipeline": "pipeline2",
  "stage": "build",
  "name": "pipe2",
  "type": "dependency"
}
```

## Package

```json
{
  "package_id": "apt-repo-id",
  "name": "myapt",
  "type": "package"
}
```

## Pluggable SCM

```json
{
  "scm_id": "someScmGitRepositoryId",
  "destination": "destinationDir",
  "filter": {
    "ignore": [
      "dir1",
      "dir2"
    ]
  },
  "name": "myPluggableGit",
  "type": "plugin"
}
```

Since GoCD `>= 19.2.0` defining new pluggable materials that are not defined
in the GoCD server is supported.

```json
{
  "plugin_configuration": {
    "id": "plugin_id",
    "version": "1"
  },
  "configuration": [
    {
      "key": "url",
      "value": "git@github.com:tomzo/gocd-json-config-plugin.git"
    }
  ],
  "destination": "destinationDir",
  "filter": {
    "ignore": [
      "dir1",
      "dir2"
    ]
  },
  "name": "myPluggableGit",
  "type": "plugin"
}
```

## Configrepo

This is a convenience for shorter and more consistent material declaration.
When configuration repository is the same as one of pipeline materials,
then you usually need to repeat definitions in XML and in JSON, for example:

```json
...
  "materials": [
    {
      "url": "https://github.com/tomzo/gocd-json-config-example.git",
      "branch" : "ci",
      "type": "git",
      "name" : "mygit"
    }
  ],
...
```

And in server XML:
```xml
<config-repos>
   <config-repo pluginId="json.config.plugin" id="repo1">
     <git url="https://github.com/tomzo/gocd-json-config-example.git" branch="ci" />
   </config-repo>
</config-repos>
```

Notice that url and branch is repeated. This is inconvenient in case when you move repository,
because it requires 2 updates, in code and in server XML.

Using  **`configrepo` material type**, above repetition can be avoided,
last example can be refactored into:

```json
...
  "materials": [
    {
      "type": "configrepo",
      "name" : "mygit"
    }
  ],
...
```

Server interprets `configrepo` material in this way:

> Clone the material configuration of the repository we are parsing **as is in XML** and replace **name, destination and filters (whitelist/blacklist)**,
then use the modified clone in place of `configrepo` material.


# Tasks

Every task object must have `type` field. Which can be `exec`, `ant`, `nant`, `rake`, `fetch`, `plugin`

Optionally any task can have `run_if` and `on_cancel`.

 * `run_if` is a string. Valid values are `passed`, `failed`, `any`
 * `on_cancel` is a task object. Same rules apply as to tasks described on this page.

### Exec

```json
{
    "type": "exec",
    "run_if": "passed",
    "on_cancel" : null,
    "command": "make",
    "arguments": [
      "-j3",
      "docs",
      "install"
    ],
    "working_directory": null
}
```

### Ant

```json
{
  "build_file": "mybuild.xml",
  "target": "compile",
  "type": "ant",
  "run_if": "any",
  "on_cancel" : null,
}
```

### Nant

```json
{
  "type": "nant",
  "run_if": "passed",
  "working_directory": "script/build/123",
  "build_file": null,
  "target": null,
  "nant_path": null
}
```

### Rake

```json
{
  "type": "rake",
  "run_if": "passed",
  "working_directory": "sample-project",
  "build_file": null,
  "target": null
}
```

### Fetch

#### Fetch artifact from the GoCD server

```json
{
   "type": "fetch",
   "artifact_origin": "gocd",
   "run_if": "any",
   "pipeline": "upstream",
   "stage": "upstream_stage",
   "job": "upstream_job",
   "is_source_a_file": false,
   "source": "result",
   "destination": "test"
 }
```

#### Fetch artifact from an external artifact store

```json
{
   "type": "fetch",
   "artifact_origin": "external",
   "run_if": "any",
   "pipeline": "upstream",
   "stage": "upstream_stage",
   "job": "upstream_job",
   "artifact_id": "upstream_external_artifactid",
   "configuration": [
     {
       "key": "DestOnAgent",
       "value": "foo"
     },
     {
       "key": "some_secure_property",
       "encrypted_value": "ssd#%fFS*!Esx"
     }
   ]
 }
```

### Plugin

```json
{
  "type": "plugin",
  "configuration": [
    {
      "key": "ConverterType",
      "value": "jsunit"
    },
    {
      "key": "password",
      "encrypted_value": "ssd#%fFS*!Esx"
    }
  ],
  "run_if": "passed",
  "plugin_configuration": {
    "id": "xunit.converter.task.plugin",
    "version": "1"
  },
  "on_cancel": null
}
```

# Contributing

Create issues and PRs if
 * something does not work as you expect it,
 * documentation is not good enough
 * you have questions about GoCD behavior with remote configuration

There has been a long effort to make it possible to store configuration in SCMs,
so obviously there will be some errors in lots of new code. Please file issues
here or ask on [gitter chat for config-repo plugins](https://gitter.im/gocd/configrepo-plugins).


## Versioning

We use semantic versioning.

If you are submitting a new feature then please run a major version bump by
```
./tasks.sh set_version 0.X.0
```

If you are submitting a fix, then do not change any versions as patch bump is made right after each release.

# License and Authors

License: Apache 2.0

Authors:
 * Tomasz Sętkowski <tom@ai-traders.com>
