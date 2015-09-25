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

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.util.VirtualFile;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Adrien Lecharpentier
 */
public class HTTPRepository extends Repository {

    private final URI remoteLocation;

    @DataBoundConstructor
    public HTTPRepository(String remoteLocation) throws URISyntaxException {
        this.remoteLocation = URI.create(remoteLocation);
    }

    public URI getRemoteLocation() {
        return remoteLocation;
    }

    @Override
    void deploy(VirtualFile[] files) {
    }

    @Extension
    public static class HTTPDescriptor extends RepositoryDescriptor {
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
    }
}
