# Releasing

Steps for releasing:
1. Update version in `build.sbt` file.

2. Run `release.sh $VERSION$` with version of msocket
**Note:** `PROD=true` environment variable needs to be set before running `release.sh`

3. update msocket tag version in csw, esw
