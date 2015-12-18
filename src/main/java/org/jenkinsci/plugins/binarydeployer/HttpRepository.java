/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Adrien Lecharpentier
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

package org.jenkinsci.plugins.binarydeployer;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.Result;
import hudson.model.Run;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.util.VirtualFile;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Adrien Lecharpentier
 */
public class HttpRepository extends Repository {

    private static final Logger log = Logger.getLogger(HttpRepository.class.getCanonicalName());

    private final String remoteLocation;
    private final String credentialsId;

    @DataBoundConstructor
    public HttpRepository(String remoteLocation, String credentialsId) {
        if (!remoteLocation.endsWith("/")) remoteLocation += "/";
        this.remoteLocation = remoteLocation;
        this.credentialsId = credentialsId;
    }

    public String getRemoteLocation() {
        return remoteLocation;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    protected void deploy(VirtualFile[] files, Run run) throws IOException {
        CloseableHttpClient client = null;
        try {
            if (credentialsId == null || credentialsId.isEmpty()) {
                client = HttpClients.createDefault();
            } else {
                BasicCredentialsProvider credentials = new BasicCredentialsProvider();
                StandardUsernamePasswordCredentials credentialById = CredentialsProvider.findCredentialById(credentialsId,
                    StandardUsernamePasswordCredentials.class, run, Lists.<DomainRequirement>newArrayList());
                credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                    credentialById.getUsername(), credentialById.getPassword().getPlainText()
                ));

                client = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentials)
                    .disableAutomaticRetries()
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                    .build();
            }

            for (VirtualFile file : files) {
                BufferedHttpEntity entity = new BufferedHttpEntity(new InputStreamEntity(file.open(), file.length()));
                HttpPost post = new HttpPost(remoteLocation + file.getName());
                post.setEntity(entity);

                CloseableHttpResponse response = null;
                try {
                    response = client.execute(post);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        log.fine("Deployed " + file.getName() + " to " + remoteLocation);
                    } else {
                        log.warning("Cannot deploy file " + file.getName() + ". Response from target was " + statusCode);
                        run.setResult(Result.FAILURE);
                        throw new IOException(response.getStatusLine().toString());
                    }
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Extension
    public static class HttpRepositoryDescriptor extends RepositoryDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.binarydeployer_httprepository_displayName();
        }

        public FormValidation doCheckRemoteLocation(@QueryParameter String value) {
            try {
                new URI(value);
                return FormValidation.ok();
            } catch (URISyntaxException e) {
                return FormValidation.error(Messages.binarydeployer_httprepository_invalidURI());
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup context) {
            List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class, context, ACL.SYSTEM, Lists.<DomainRequirement>newArrayList());
            return new StandardUsernameListBoxModel().withEmptySelection().withAll(credentials);
        }
    }
}
