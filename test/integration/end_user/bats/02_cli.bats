load '/opt/bats-support/load.bash'
load '/opt/bats-assert/load.bash'

@test "With IDE: gocd-json returns OK when file is valid" {
  run /bin/bash -c "ide --idefile Idefile.to_be_tested \"cd json-example && gocd-json syntax allmaterials.gopipeline.json\""
  assert_output --partial "OK"
  assert_equal "$status" 0
}
