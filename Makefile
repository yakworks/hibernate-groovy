# check for build/shipkit and clone if not there, this should come first
SHIPKIT_DIR = build/shipkit
$(shell [ ! -e $(SHIPKIT_DIR) ] && git clone -b v2.0.11 https://github.com/yakworks/shipkit.git $(SHIPKIT_DIR) --depth=1 >/dev/null 2>&1)
# Shipkit.make first, which does all the lifting to create makefile.env for the BUILD_VARS
include $(SHIPKIT_DIR)/Shipkit.make
include $(SHIPKIT_MAKEFILES)/circle.make
include $(SHIPKIT_MAKEFILES)/vault.make
include $(SHIPKIT_MAKEFILES)/git-tools.make
include $(SHIPKIT_MAKEFILES)/gradle-tools.make
include $(SHIPKIT_MAKEFILES)/ship-version.make

## Run lint (spotlessApply and codenarc) and gradle check
check: lint
	$(gradlew) check

# should run vault.decrypt before this,
# sets up github, kubernetes and docker login
# sets up github, kubernetes and docker login, commented out # kubectl.config dockerhub.login
ship.authorize: git.config-bot-user
	$(logr.done)

## publish the java jar lib to repo.9ci for snapshot and to both for prod Sonatype Maven Central
publish:
	if [ "$(dry_run)" ]; then
		echo "🌮 dry_run ->  $(gradlew) publish"
	else
		if [ "$(IS_SNAPSHOT)" ]; then
			$(logr) "publishing SNAPSHOT"
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
		else
			# ${gradlew} rally-domain:verifyNoSnapshots
			$(logr) "publishing to repo.9ci"
			$(gradlew) publishJavaLibraryPublicationToMavenRepository
			$(logr) "publishing to Sonatype Maven Central"
			$(gradlew) publishToSonatype closeAndReleaseSonatypeStagingRepository
		fi
		$(logr.done) "published"
	fi

## publish snapsot to repo.9ci
publish.snapshot:
	if [ "$(IS_SNAPSHOT)" ]; then
		$(gradlew) publishJavaLibraryPublicationToMavenRepository
		$(logr.done) "- libs with version $(VERSION)$(VERSION_SUFFIX) published to snapshot repo"
	fi

## Build snapshot and publishes to your local maven.
snapshot:
	# snapshot task comes from the yakworks shipkit plugin.
	$(gradlew) snapshot
	$(logr.done) "- libs with version $(VERSION)$(VERSION_SUFFIX) published to local ~/.m2 maven"

ifdef PUBLISHABLE_BRANCH_OR_DRY_RUN

 # removed  ship.docker kube.deploy for now
 ship.release: build publish
	$(logr.done)

 ship.docker: docker.app-build docker.app-push
	$(logr.done) "docker built and pushed"

 kube.deploy: kube.create-ns kube.clean
	$(kube_tools) apply_tpl $(APP_KUBE_SRC)/app-configmap.tpl.yml
	$(kube_tools) apply_tpl $(APP_KUBE_SRC)/app-deploy.tpl.yml
	$(logr.done)

else

 ship.release:
	$(logr.done) "not on a PUBLISHABLE_BRANCH, nothing to do"

endif # end PUBLISHABLE_BRANCH_OR_DRY_RUN

# ---- Docmark -------

# the "dockmark-build" target depends on this. depend on the docmark-copy-readme to move readme to index
docmark.build-prep: docmark.copy-readme

# --- Testing and misc, here below is for testing and debugging ----

# -- helpers --

ifdef IS_SNAPSHOT
# publish snapsot to repo.9ci
 publish.snapshot.repo:
	./gradlew publishJavaLibraryPublicationToMavenRepository
endif

## shows dependencies
gradle.dependencies:
	./gradlew hibernate-groovy-proxy:dependencies --configuration compileClasspath
	./gradlew examples:spring-jpa-java:dependencies --configuration compileClasspath

# for testing circle image. set up .env or export both GITHUB_TOKEN and the base64 enocded GPG_KEY from lastpass.
docker.circle.shell:
	docker volume create gradle_cache
	docker run -it --rm \
	-e GITHUB_TOKEN \
	-e GPG_KEY \
	-v gradle_cache:/root/.gradle \
	-v "$$PWD":/root/project \
	$(DOCKER_SHELL) $(BIN_BASH)
	#	-v ~/.gradle_docker:/root/.gradle \

