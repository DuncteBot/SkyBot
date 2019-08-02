#!/bin/sh
git push
git checkout master
git pull
git merge development
git push
git checkout development
