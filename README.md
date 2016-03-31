# DC4Cities Energy  SubSystem #

## Preparing your environment


These actions describe how to get the codebase and integrate it. It is mostly based on command-line. If you have specific IDE stuff, complete. Here are the pre-requisites:

* Java 7
* maven 3 (http://maven.apache.org)
* git 1.8 (http://git-scm.com)

### Retrieve and initialize its own copy of the repository

* Ask the repository maintainer to grant you the required developer credentials.

* Get the source code: `git clone git@dc4cities.inria.fr:fniederm/dc4es.git`

* Compile it: `mvn clean install`

### IDE Integration

#### Intellij specificities

To integrate the project within IDEA Intellij (http://www.jetbrains.com/idea/).

* clone the repository
* import a "maven project" and select your local repository
* indicates to consider maven modules as Intellij modules

#### Eclipse specificities

* Create Eclipse specific files in your Maven local repository: `mvn eclipse:eclipse`
* From Eclipse Workbench select the Java view: `Window->Open perspective->Java`
* Then import the project into your workspace: `File->Import->General->Existing Projects`
* Add the path to your Maven libraries directory:
* Go to `Window->Preferences->Java->Classpath Variables` and create a new variable entry named `M2_REPO`
* The path should be something like `C:\Documents and Settings\USERNAME\.m2\repository` for Windows and `~USERNAME/.m2/repository` for Linux
* Do a full rebuild

## Useful links (authentication required) ##

* Project evolution with sonar: http://dc4cities.inria.fr/sonar/dashboard/index/eu.dc4cities:dc4es
* Continuous Integration status with Jenkins
  * URL: http://dc4cities.inria.fr:8080/jenkins
  * Mailing-list to be notified by build status:
      * address: dc4cities_jenkins@dc4cities.eu
      * subscription address: dc4cities_jenkins-request@mail.dc4cities.eu (just write `subscribe` as mail content)

* Maven repositories
	*  snapshot releases: http://dc4cities.inria.fr/~dc4cities/maven/snapshots/eu/dc4cities/dc4es/
	*  stable releases: http://dc4cities.inria.fr/~dc4cities/maven/releases/eu/dc4cities/dc4es/
* Apidoc
	*  snapshot releases: http://dc4cities.inria.fr/~dc4cities/apidocs/snapshots/dc4es/
	*  stable releases: http://dc4cities.inria.fr/~dc4cities/apidocs/releases/dc4es/
* Webapp entry point: http://dc4cities.inria.fr:8080/dc4es-service/
## Development guidelines

### GIT Branching model

The branching model is derived from git flow (http://nvie.com/posts/a-successful-git-branching-model/). In practice, there is 2 persistent branches but you will never work directly in these branches.

* `master` contains the last release

* `develop` contains the next release.


### Typical workflow

The principles is to have persistent branches always as clean as possible. You work inside your own branches derived from a persistent one. Once your work is done, you merge your work within the persistent branch. Typically:

* Create a branch for one of the persistent branches. By default branch from `develop`.

* Once your development is done (it compiles, tests are ok), you merge your work inside the original branch. (checkout the original branch, do the merge, push)

* Each time one persistent branch is updated, integrate the changes inside your branches.

A sample workflow through command-lines:

```sh
#Get the last version of develop
$ git pull develop
$ git checkout develop

#Create a branch dedicated to the feature/stuff to develop
$ git checkout -b my-feature

#code, test, rince, repeat

# If needed, share/save your branch on the remote repository
$ git push -u origin my-feature

# Each time someone update the 'develop' branch , integrate the changes locally
$ git checkout develop
$ git pull

#then inside your forks of it
$ git checkout my-feature
$ git merge develop

#Once the work is done, integrate into develop
$ git checkout develop
$ git pull
$ git merge my-feature

# And push the brand new branch
$ git push -u origin develop

# If your work on our private branch is done, it is ok to remove the local branch
$ git branch -d my-feature

# and the remote one if exists
$ git push origin --delete my-feature
```

* * Warning * Usually, people do too many work in their branch. This delay the integration. Basically, you are working inside your feature, you observe a bug elsewhere:

    * if you fix the bug in your branch, then the fix cannot be integrated until your work is integrated which may takes day (because you are working on something hard).

    * if you fix the bug inside another branch, derived from the persistent branch that contains the bug, you will be able to integrate the fix earlier.


```sh
#do my duty inside my-branch
#A bug has been detected in the develop codebase
$ git checkout develop

# debug and publish
$ git push

# Integrate the update
$ git checkout my-branch
$ git merge develop
```
### Continuous integration

Each type the `develop` branch is updated:

* Jenkins (http://dc4cities.inria.fr:8080/jenkins)
    * builds the code
    * performs the unit tests
    * generates the javadocs
    * deploy the artifacts in the maven repository.
    * notifies on dc4cities_dev@dc4cities.eu

* Sonar (http://dc4cities.inria.fr/sonar/dashboard/index/eu.dc4cities:dc4es)
    * analyses the code quality to present its evolution with regards to standards.


## Integrate eu.dc4cities:dc4es inside another maven project ##

The produced maven artifacts are in private repositories so you have to provide your credential.

First, declare proxies inside your `settings.xml` file in your homedir (`~/.m2/settings.xml` on GNU/Linux, OS X system)
to declare the credentials:

```xml
<settings>
   <servers>
      <server>
          <id>dc4cities-releases</id>
          <username>myusername</username>
          <password>mypassword</password>
          <configuration>
              <authenticationInfo>
                  <userName>auth-user</userName>
                  <password>auth-pass</password>
              </authenticationInfo>
          </configuration>
      </server>
      <server>
          <id>dc4cities-snapshots</id>
          <username>myusername</username>
          <password>mypassword</password>
          <configuration>
              <authenticationInfo>
                  <userName>auth-user</userName>
                  <password>auth-pass</password>
              </authenticationInfo>
          </configuration>
      </server>
   </servers>
</settings>
```

Second, in the `pom.xml` of your project, declare our private repositories:

```xml
<repositories>
    <repository>
        <id>dc4cities-releases</id>
        <url>http://dc4cities.inria.fr/~dc4cities/maven/releases</url>
    </repository>
    <repository>
        <id>dc4cities-snapshots</id>
        <url>http://dc4cities.inria.fr/~dc4cities/maven/snapshots</url>
    </repository>
</repositories>
```

Finally, declare the dependency with the version of the artifact you want:

```xml
<dependency>
   <groupId>eu.dc4cities</groupId>
   <artifactId>dc4es</artifactId>
   <version>0.1-SNAPSHOT</version>
</dependency>
```

`eu.dc4cities:centralSystem` aggregates of different sub-modules. If you don't need all of then, it is still possible to declare each dependency separately.
See the `modules` tag in the `pom.xml` and their description in the corresponding folders.

