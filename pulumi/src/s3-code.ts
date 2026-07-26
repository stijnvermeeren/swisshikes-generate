import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";

export const codeBucket = new aws.s3.Bucket(
    "swisshikes-generate-code",
    {
        versioning: {
            enabled: true,
        }
    }
);

export const codeJarObject = new aws.s3.BucketObjectv2("codeJar", {
    key: "swisshikes-generate.jar",
    bucket: codeBucket.id,
    source: new pulumi.asset.FileAsset("../target/out/jvm/scala-3.3.4/swisshikes-generate/swisshikes-generate.jar")
});
