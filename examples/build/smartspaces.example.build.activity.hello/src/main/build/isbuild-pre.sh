#!/bin/bash

FULLPATH="${1}/smartspaces/build/test"
mkdir -p "${FULLPATH}"
NEWFILE="${FULLPATH}/Foo.java"
echo $NEWFILE

read -d '' classdef <<- EOF
package smartspaces.build.test;

public class Foo {
}
EOF

echo "$classdef" >$NEWFILE

