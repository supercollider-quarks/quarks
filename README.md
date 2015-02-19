# quarks

Quarks are packages of [SuperCollider](https://github.com/supercollider/supercollider) code containing classes, extension methods, documentation and server UGen plugins. The Quarks class manages downloading these packages and installing or uninstalling them.

This repository collects all of the community contributed Super Collider Quarks.

Do NOT clone or fork this repository. It is only a compilation, but the actual Quarks are all in their own separate repositories.

---

___Most importantly it contains the file `directory.txt` which is the index that the Quarks interface fetches.___

https://github.com/supercollider-quarks/quarks/blob/master/directory.txt

1. This specifies the location and latest version of each community Quarks.  Installing a Quark does so from the respository specified in this directory.txt file

2. It is editable. Follow the link above and just click edit.

3. This repository also includes each Quark as a git subtree. This allows the entire Quarks library to be downloaded as a single file for offline usage.
The update.py script reads the current directory.txt and updates the subtree items.

---


## Using Quarks

To install Quarks inside see the SuperCollider help file `Using Quarks`.

You can install Quarks:

### using the gui

```supercollider
Quarks.gui
```

This offers many community contributed packages most of which are hosted on github.

You can also add additional folders where you keep your personally developed quarks or those you have forked or manually downloaded.
These will also be shown on the GUI as options for installing.

```supercollider
// put this in your startup.scd
Quarks.addFolder("~/supercollider/quarks");
```


### by name

You can (un)install a Quark by name:

```supercollider
Quarks.install("UnitTesting");
Quarks.uninstall("UnitTesting");
```

### by git URL

```supercollider
Quarks.install("https://github.com/supercollider-quarks/UnitTesting.git");
Quarks.uninstall("https://github.com/supercollider-quarks/UnitTesting.git");
```

### from a local folder

```supercollider
// add your own classes
Quarks.install("~/supercollider/quarks/my-thing");
// install something that you downloaded and want to try out
Quarks.install("~/Downloads/something-i-found");
```


## Adding a Quark to this Directory

Go here, click edit and submit a pull request:

https://github.com/supercollider-quarks/quarks/blob/master/directory.txt

The format is:

    quarkname=git://github.com/you/quarkname

Ideally you should tag your releases with the version number and then specify it in the directory.txt:

    quarkname=git://github.com/you/quarkname@tags/0.1.0

The version is specified as a git refspec. `tags/x.y.z` is the preferred form.

## Tag your releases

Tagged version numbers will allow people to download a .tgz archive of the release and will enable people to switch between different versions.

Users can see and switch between versions in the interface, but only if they are tagged.

It will also enable people to specify an exact version of your quark when working on their own projects.

Note that a refspec is either tags/{tagname} or {commit-hash} but not a branch name. A branch is a continually changing, and packages releases need to be pinned.

## Migrated Quarks from SVN

I (@crucialfelix) migrated the old SVN repository to github and preserved all the commits, dates and authorship.

You will find your old work here:

https://github.com/supercollider-quarks

The easiest thing to do is to just fork your Quark from here into your own account. It will appear as "forked from ..." on your github.

Another option is to git clone your quark from here and then create a fresh repository in your own github/bitbucket account and push it to there. Then it will not say "forked from..." Remember to set your working copy's upstream origin to your own github version.

It is also possible to transfer ownership, but for that you need to have admin access to both the supercollider-quarks group and the account you are transferring it to. Contact us and we can add you as an admin to supercollider-quarks.

If you've already moved your code to github or some other git host and want to use that version then just edit directory.txt to point to your preferred newer version.

After you've moved we can delete the quark from here to reduce any confusion.

### Backward incompatibilities

Quarks with spaces in the name had to have those spaces removed ("SenseWorld MiniBee" -> SWMiniBee).

The 'name' field is no longer required in the quark file, but its nice to have. The official name of the Quark is now the same name as the folder and the name of the git repository.

Quarks nested inside other quarks (dewdrop_lib) are now un-nested.

The 'path' field is now obsolete and ignored.

## Download all quarks

This entire repository can be downloaded for use while offline or if cannot for some reason use git on your machine.

They will be downloadable here:

https://github.com/supercollider-quarks/quarks/releases

## Updating the subtree

After any updates to the directory.txt file someone should run the script `update.py` in this repository.
This will read the directory file and update all of the git subtrees .

In terminal run the python script passing it your quark's name

```shell
    python update.py quarkname
```

To update all quarks:

```shell
    python update.py
```

This will clone, update and/or checkout tags as needed. If the repository URL changes then it will first remove the previous quark and clone the new one in its place.

The update script will `git add` the changes but will not commit them.

Commit your changes:

    git commit -m "updated quarkname to version 4.1.3" -a

And submit a pull request.

## Releases of supercollider-quarks

By tagging this repository we will enable a downloadable release. We should do a release every once in a while, especially when SuperCollider itself publishes a release.

We will worry about that after everybody is using the new Quarks.  Maybe nobody will even need the downloaded version.

---

[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/supercollider-quarks/quarks?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
