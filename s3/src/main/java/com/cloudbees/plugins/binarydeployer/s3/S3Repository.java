/*
 * Copyright (c) 2015 CloudBees, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cloudbees.plugins.binarydeployer.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl;
import com.cloudbees.plugins.binarydeployer.core.Repository;
import com.cloudbees.plugins.binarydeployer.core.RepositoryDescriptor;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.util.VirtualFile;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

/**
 * @author Adrien Lecharpentier
 */
public class S3Repository extends Repository {
    private static final Logger log = Logger.getLogger(S3Repository.class.getCanonicalName());

    private final String credentialsId;
    private final String bucketName;

    @DataBoundConstructor
    public S3Repository(String credentialsId, String bucketName) {
        this.credentialsId = credentialsId;
        this.bucketName = bucketName;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Override
    protected void deploy(VirtualFile[] files, Run run) throws IOException {
        log.debug("Will deploy files to S3::{}" + bucketName);
        AWSCredentialsImpl credentials = CredentialsProvider.findCredentialById(
            credentialsId, AWSCredentialsImpl.class, run, Lists.<DomainRequirement>newArrayList()
        );

        TransferManager transferManager = new TransferManager(credentials);
        for (VirtualFile file : files) {
            transferManager.upload(prepareUpload(file, file.getName()));
        }
    }

    private PutObjectRequest prepareUpload(VirtualFile file, String name) throws IOException {
        log.debug("Preparing upload for " + name + " to S3::" + bucketName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.length());

        return new PutObjectRequest(
            bucketName, name, file.open(), metadata
        );
    }

    @Extension
    public static class DescriptorImpl extends RepositoryDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.binarydeployer_s3_displayName();
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup context) {
            List<AWSCredentialsImpl> credentials = CredentialsProvider.lookupCredentials(
                AWSCredentialsImpl.class, context, ACL.SYSTEM, Lists.<DomainRequirement>newArrayList()
            );
            return new StandardListBoxModel().withEmptySelection().withAll(credentials);
        }
    }
}
