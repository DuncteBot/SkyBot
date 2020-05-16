#!/bin/sh
git push
git checkout master
git pull
git rebase development
git push
git checkout development
