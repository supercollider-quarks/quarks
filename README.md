quarks
======

This repository collects all of the community contributed Super Collider Quarks.

It is the successor to the old SVN repository formally used by the Quarks system for package distribution.

The new Quarks system will be able to install individual Quarks from any git URL, so for most users this combined repo will not be needed.

Its primary purpose is to provide downloadable releases that match Super Collider releases. Whenever this repo is tagged it will create a downloadable zip.

The other purpose is to record a central index of community contributions. The new Quarks system can download directory.txt or directory.json and use this to offer a menu with descriptions.

Adding or Updating a Quark
==========================

Fork this repository and edit the directory.text file

Add or update the URL and refspec for your quark::

    # default is the master branch
    cruciallib=git://github.com/crucialfelix/crucial-library

    # specify a tag (recommended for stable release pinning)
    cruciallib=git://github.com/crucialfelix/crucial-library@tags/4.1.3

    # pin to a specific commit
    cruciallib=git://github.com/crucialfelix/crucial-library@0214d3a0e805146cfbaded090da1a2aabebcec2c

Note that a refspec is either tags/{tagname} or {commit-hash} but not a branch name. A branch is a continually changing, and packages releases need to be pinned.

In terminal run the python script passing it your quark's name::

    python update.py cruciallib

To update all quarks:

    python update.py

This will clone, update and/or checkout tags as needed. If the repository URL changes then it will first remove the previous quark and clone the new one in its place.

The update script will `git add` the changes but will not commit them.

Commit your changes:

    git commit -m "updated cruciallib to version 4.1.3" -a

And submit a pull request.
