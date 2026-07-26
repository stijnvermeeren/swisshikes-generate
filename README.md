# Stijn's hikes in Switzerland: generate script

A Scala script that processes the `.gpx` tracks and associated metadata from the [stijnvermeeren/swisshikes-data](https://github.com/stijnvermeeren/swisshikes-data) repository, collects this data into `.kml` files (one per year) and uploads those to S3, so that they can be loaded and displayed on the _Stijn's hikes in Switzerland_ webpage ([stijnvermeeren.be/swisshikes](https://stijnvermeeren.be/swisshikes)).

When processing the GPS tracks, the script also applies some compression (removing redundant points, dropping excessively precise decimals) to reduce the file size.

## Assumptions on the data

The script makes the following assumptions on the GitHub repository containing the individual tracks:
- One folder for each year.
- Each folder contains one `.gpx` file per track, as well as an optional `metadata.yml` file.
- The `.gpx` files follow the naming convention `DATE_TYPE_NAME.gpx`
  - `DATE` preferably has the format `YYYY-MM-DD`.
  - The date from the filename (unless overridden in the `metadata.yml` file) is included in the track description in the `.kml` file.
  - `TYPE` is a hyphen-separated list of one or more of the following codes:
    - `H` for hike
    - `R` for run
    - `WH` for winter hike
    - `SS` for snowshoe hike
    - `ST` for ski tour
    - `VT` for via ferrata
    - `C` for climb
    - `M` for mountaineering
  - The type derived the filename (unless overridden in the `metadata.yml` file) is included in the track description in the `.kml` file.
- Each key in the `metadata.yml` file is the filename of a track in the same folder. The corresponding value is a map that can have the following keys:
  - `date`: overrides the date that is included in the track description in the `.kml` file. Useful for tracks that span multiple days.
  - `description`: overrides the activity type that is included in the track description in the `.kml` file. Useful for describing e.g. an unusual activity or an organised event. 
  - `albums`: a list of URLs, each pointing to an online photo album with photos of the corresponding activity. 

Relevant examples can be found in the GitHub repository [stijnvermeeren/swisshikes-data](https://github.com/stijnvermeeren/swisshikes-data).

## Executing and deploying

This repository also includes code for deploying this script to AWS with [Pulumi](https://www.pulumi.com/). The script will be run as a Lambda and the required S3 buckets (one to contain the `.jar` file of the script, and one for the output files) will be created. To do this deployment, first run `sbt assembly` and then `pulumi up` (in the [pulumi](./pulumi) directory). This assumes that [sbt](https://www.scala-sbt.org/) and [Pulumi](https://www.pulumi.com/) are installed.

Alternative, you can run the script locally with  `sbt run`, after setting the AWS region and S3 bucket as environment variables `AWS_REGION` and `AWS_BUCKET`.

Configuration is done using [Lightbend Config](https://github.com/lightbend/config). See [reference.conf](src/main/resources/reference.conf) for default values.
