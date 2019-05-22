### 0.3.10 - Unreleased

### 0.3.9 (2019-May-22)

 * updated README documenting support credentials attributes in materials

### 0.3.8 (2019-May-01)

 * switch build system to use open source openjdk-dojo image \#17574
 * remove docker image from this repo, use [new image](https://github.com/gocd-contrib/docker-gocd-cli-dojo) with gocd-cli

### 0.3.7 (2019-Feb-12)

* fix repo-level configuration not being applied \#40
* Generate reproducible binaries \#38

# 0.3.6 (21 Jan 2019)

* Changed JSON keys returned by `get-capabilities` call
* Changed JSON structure returned by `parse-content` call
* Implemented a new `get-icon` call that will return the icon for this plugin

# 0.3.5 (15 Jan 2019)

 * return json from CLI command

# 0.3.4 (09 Jan 2019)

 * Add export content metadata
 * Fix plugin settings request and implement handler for plugin config change notification

# 0.3.3 (03 Jan 2019)

 * Added support for `parse-content`.

# 0.3.2 (Dec 10 2018)

Accept stdin input in CLI tool

# 0.3.1 (Dec 5 2018)

Adds CLI to validate local files.

# 0.3.0 (Nov 12 2018)

Adds config-repo API 2.0 and ability to export XML pipelines to JSON

# 0.2.1 (Oct 24 2017)

 * adds recommended `format_version` to all files
 * (documentation change only) support for referencing templates and parameters, with GoCD >= 17.11

# 0.2.0 (Jun 21 2016)

Initial release
