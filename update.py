#!/usr/bin/env/python
"""
    update.py

    Using the directory.txt file,
    clone or update one or all repositories.

    Usage::
        python update.py

        python update.py cruciallib

    directory.txt format::

        # master branch
        cruciallib=git://github.com/crucialfelix/crucial-library
        # tag (recommended for stable releases)
        cruciallib=git://github.com/crucialfelix/crucial-library@tags/4.1.3
        # pin to a specific commit
        cruciallib=git://github.com/crucialfelix/crucial-library@0214d3a0e805146cfbaded090da1a2aabebcec2c

    Rather than specifying a branch, tag your releases and specify using the tag.

"""
import sys
import re
import shutil
import os.path
import subprocess


if(len(sys.argv) > 1):
    quark = sys.argv[1]
else:
    quark = None


def parse(line):
    return re.match(r'^(?P<name>[a-zA-Z0-9\_\-\.]+)=(?P<url>[^@]+)@?(?P<refspec>.*)$', line)


def update(match):
    name = match.group('name')
    url = match.group('url')
    refspec = match.group('refspec')
    print "Updating: ", name, url, refspec

    quark_dir = os.path.join(os.path.dirname(os.path.realpath(__file__)), name)
    exists = os.path.exists(name)
    if exists:
        # check if repository URL has been changed
        current = subprocess.check_output("cd %s; git config --get remote.origin.url" % quark_dir, shell=True).strip()
        if current != url:
            print "Repository URL has changed, removing old: %s" % current
            shutil.rmtree(quark_dir)
            exists = False
    # clone the repo
    if not exists:
        err = subprocess.call(["git", "clone", url, name])
        if err:
            raise Exception("Failed to clone %s" % url)
    # checkout any tag or hash
    if refspec:
        child = subprocess.Popen(["git", "checkout", refspec], cwd=quark_dir)
        if child.returncode:
            raise Exception("Failed to checkout %s %s" % (url, refspec))
    # stage any changes
    err = subprocess.call(["git", "add", "%s/*" % name])
    if err:
        raise Exception("Failed to git add %s/*" % name)


d = open('directory.txt', 'r')
for line in d.readlines():
    line = line.strip()
    if quark:
        m = parse(line)
        if not m:
            print "Broken line in directory.txt: %s" % line
            continue
        if quark == m.group('name'):
            update(m)
            exit(0)
    else:
        m = parse(line)
        if not m:
            print "Broken line in directory.txt: %s" % line
            continue
        update(m)
