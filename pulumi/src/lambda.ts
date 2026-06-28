import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";

import {dataBucket} from './s3-data';
import {codeBucket, codeJarObject} from './s3-code';

const lambdaRole = new aws.iam.Role("lambda-role", {
    assumeRolePolicy: {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Action": "sts:AssumeRole",
                "Principal": {
                    "Service": "lambda.amazonaws.com",
                },
                "Effect": "Allow",
                "Sid": "",
            },
        ],
    }
});

const lambdaRolePolicy = new aws.iam.RolePolicyAttachment(
    'lambda-role-policy-attachment',
    {
        role: lambdaRole,
        policyArn: aws.iam.ManagedPolicy.AWSLambdaBasicExecutionRole,
    }
);

const lambdaS3Policy = new aws.iam.Policy(
    'lambda-s3-policy',
    {
        description: "IAM policy for Lambda to interact with S3",
        path: "/",
        policy: {
            "Version": "2012-10-17",
            "Statement": [
                {
                  "Action": "s3:PutObject",
                  "Resource": pulumi.interpolate`${dataBucket.arn}/*`,
                  "Effect": "Allow"
                }
            ]
        }
    }
)

const lambdaRolePolicy2 = new aws.iam.RolePolicyAttachment(
    'lambda-role-policy-attachment-2',
    {
        role: lambdaRole,
        policyArn: lambdaS3Policy.arn,
    }
);

const lambdaFunction = new aws.lambda.Function("lambda-swisshikes-generate", {
    runtime: aws.lambda.Java11Runtime,
    s3Bucket: codeBucket.id,
    s3Key: codeJarObject.key,
    s3ObjectVersion: codeJarObject.versionId,
    handler: "be.stijnvermeeren.swisshikesgenerate.Main",
    memorySize: 512,
    timeout: 240,
    environment: {
        variables: {
            AWS_BUCKET: dataBucket.id,
        }
    },
    role: lambdaRole.arn
});

export const lambdaFunctionUrl = new aws.lambda.FunctionUrl("lambdaFunctionUrl", {
    functionName: lambdaFunction.name,
    authorizationType: "NONE",
});
