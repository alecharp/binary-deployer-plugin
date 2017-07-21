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

package com.cloudbees.plugins.binarydeployer.core;

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.util.VirtualFile;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Adrien Lecharpentier
 */
public class BinaryDeployerRecorder extends Recorder {

    private static final Logger log = Logger.getLogger(BinaryDeployerRecorder.class.getCanonicalName());

    private final Repository repository;
    private final boolean flatten;

    @DataBoundConstructor
    public BinaryDeployerRecorder(Repository repository, boolean flatten) {
        this.repository = repository;
        this.flatten = flatten;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
        throws InterruptedException, IOException {
        listener.getLogger().println("Deploying files");
        VirtualFile[] filesToDeploy = build.pickArtifactManager().root().list();
        repository.deploy(crossDirectories(filesToDeploy), build);
        return true;
    }

    private List<Binary> crossDirectories(VirtualFile[] files) throws IOException {
        return crossDirectories(files, "");
    }

    private List<Binary> crossDirectories(VirtualFile[] files, String parentName) throws IOException {
        List<Binary> binaries = Lists.newArrayList();
        for (VirtualFile file : files) {
            if (file.isDirectory()) {
                binaries.addAll(crossDirectories(
                    file.list(),
                    flatten ?
                        parentName :
                        (parentName.isEmpty() ? parentName : parentName + "/") + file.getName()
                    )
                );
            } else {
                log.fine("Prepare " + parentName + file.getName() + " for deployment");
                binaries.add(Binary.from(file, parentName));
            }
        }
        return binaries;
    }

    public Repository getRepository() {
        return repository;
    }

    public boolean isFlatten() {
        return flatten;
    }

    @Extension
    public static final class BinaryDeployerDescriptor extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override @Nonnull
        public String getDisplayName() {
            return Messages.binarydeployer_core_displayName();
        }

        public List<RepositoryDescriptor> getRepositoryDescriptors() {
            return RepositoryDescriptor.all();
        }
    }
}
