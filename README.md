quarks
======
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/supercollider-quarks/quarks?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This repository collects all of the community contributed Super Collider Quarks.

Most importantly it contains the file directory.txt which specifies the location and latest version of all of the community Quarks.

This file is fetched when using Quarks in SuperCollider.

It is editable, and this is how authors can add quarks or update them.

This repository also includes each Quark as a git subtree. This allows the entire Quarks community library to be downloaded as a single file.

## Using Quarks

To use Quarks inside of SuperCollider see the help file `Using Quarks`.

TODO link to online version

## Adding a Quark to the Directory

Go here:

https://github.com/supercollider-quarks/quarks/blob/master/directory.txt

click edit, make any changes and submit a pull request.

The format is:

    quarkname=git://github.com/you/quarkname

Ideally you should tag your releases with the version number and then specify it in the directory.txt:

    quarkname=git://github.com/you/quarkname@tags/0.1.0

The version is specified as a git refspec. `tags/x.y.z` is the preferred form.

## Tag your releases

Tagged version numbers will allow people to download a .tgz archive of the release and will enable people to switch between different versions.

It will also enable people to specify an exact version of your quark when working on their own projects.

Note that a refspec is either tags/{tagname} or {commit-hash} but not a branch name. A branch is a continually changing, and packages releases need to be pinned.

## Migrated Quarks from SVN

I (@crucialfelix) migrated the old SVN repository to github and preserved all the commits and authorship.

You may find your old work in one of these quarks:

https://github.com/supercollider-quarks

Contact me or the supercollider-quarks org to transfer ownership to your own github account.

You may also just fork any repo here.

If you've already moved your code to github and want to use that version then just edit directory.txt to point to your preferred newer version.

### Backward incompatibilities

Quarks with spaces in the name had to have those spaces removed. Quarks nested inside other quarks (dewdrop_lib) are now un-nested.


## Download all quarks

This entire repository can be downloaded for use while offline or if cannot for some reason use git on your machine.


## Updating the downloadable version

After any updates to the directory.txt file someone should run the script update.py in this repository.
This will checkout or update all of the git subtrees for all of the Quarks.

In terminal run the python script passing it your quark's name::

    python update.py quarkname

To update all quarks:

    python update.py

This will clone, update and/or checkout tags as needed. If the repository URL changes then it will first remove the previous quark and clone the new one in its place.

The update script will `git add` the changes but will not commit them.

Commit your changes:

    git commit -m "updated quarkname to version 4.1.3" -a

And submit a pull request.

## Releases of supercollider-quarks

By tagging this repository we will enable a downloadable release. We should do a release every once in a while, especially when SuperCollider itself publishes a release.
