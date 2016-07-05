[![Build Status](https://snap-ci.com/tomzo/gocd-json-config-plugin/branch/master/build_image)](https://snap-ci.com/tomzo/gocd-json-config-plugin/branch/master)

# Go JSON configuration plugin

**Go pipeline configuration as code**

This is a Go server plugin which allows to keep **pipelines** and **environments** configuration
in version control systems supported by Go (git,svn,mercurial, etc.).
See [this document](https://docs.google.com/document/d/1_eGZaqIz9ydnYQJ_Xrcb3obXc-T6jIfV_pgZQNCydVk/pub)
to find out what are Go's configuration repositories.

### Resources:

 * You can find an example repository at https://github.com/tomzo/gocd-json-config-example.git
 * Go [feature](https://github.com/gocd/gocd/issues/1133) on github

## Quickstart

### Early access

Official Go release `16.6.0` has experimental support of this feature - **do not use in production**.

### Installation

First you must install the plugin in Go server.
You'll have to drop `.jar` to `plugins/external` [directory](https://docs.go.cd/current/extension_points/plugin_user_guide.html) in your server installation.
Plugin jars can be downloaded from [releases page](https://github.com/tomzo/gocd-json-config-plugin/releases).

### Add configuration repository

There is no UI to add configuration repositories so you'll have to edit the
config XML.
You will need to add `config-repo` section within `config-repos`.
If `config-repos` does not exist yet then you add it **right above first `<pipelines />`**.

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
one exposed by [go API](https://api.go.cd/current#get-pipeline-config).

The format of environment configuration files is much simpler,
you can find examples of correct environments at the [bottom](#environment).

#### Implementation note

This plugin leverages JSON message format used internally for Go server
and plugin communication.

Go pipeline and environment configuration has very deep structure. So instead
of reading a very long schema, below you can find examples of all configuration elements.

It is exactly like documented [here](https://github.com/tomzo/documentation/blob/1133-configrepo-extension/developer/writing_go_plugins/configrepo/version_1_0/config_objects.md)

It is close to [official xml schema](http://www.go.cd/documentation/user/16.1.0/configuration/configuration_reference.html)
and also [official JSONs in pipeline configuration API](https://api.go.cd/16.1.0/#get-pipeline-config)

## JSON Configuration objects

1. [Environment](#environment)
1. [Environment variables](#environment-variables)
1. [Pipeline](#pipeline)
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
    * [pluggable scm](#pluggable)

# Environment

Configures a [Go environment](http://www.go.cd/documentation/user/current/configuration/managing_environments.html)

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
  "group": "group1",
  "name": "pipe2",
  "label_template": "foo-1.0-${COUNT}",
  "enable_pipeline_locking": true,
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

Please note:

 * templates are not supported
 * parameters are not supported
 * pipeline declares a group to which it belongs

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

Part of **job** object can be [number of job to runs](https://docs.go.cd/current/advanced_usage/admin_spawn_multiple_jobs.html)
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
  "type": "git"
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
      "instal"
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

```json
{
   "type": "fetch",
   "run_if": "any",
   "pipeline": "upstream",
   "stage": "upstream_stage",
   "job": "upstream_job",
   "is_source_a_file": false,
   "source": "result",
   "destination": "test"
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
 * you have questions about Go behavior with remote configuration

There has been a long effort to make it possible to store configuration in SCMs,
so obviously there will be some errors in lots of new code. Please file issues
here or ask on [gocd gitter chat](https://gitter.im/gocd/gocd)

# License and Authors

License: Apache 2.0

Authors:
 * Tomasz Sętkowski <tom@ai-traders.com>
