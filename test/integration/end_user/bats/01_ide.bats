load '/opt/bats-support/load.bash'
load '/opt/bats-assert/load.bash'

@test "can git clone using https from github" {
  run /bin/bash -c "ide --idefile Idefile.to_be_tested \"rm -rf json-example &&\
    git clone https://github.com/tomzo/gocd-json-config-example.git json-example\""
  assert_output --partial "Cloning into 'json-example'..."
  assert_equal "$status" 0
}
