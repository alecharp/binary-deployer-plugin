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

import hudson.model.AbstractDescribableImpl;
import hudson.model.Run;
import jenkins.util.VirtualFile;

import java.io.IOException;

/**
 * @author Adrien Lecharpentier
 */
public abstract class Repository extends AbstractDescribableImpl<Repository> {

    /**
     * Handle the file deployment for each implementation.
     *
     * @param files list of {@link jenkins.util.VirtualFile} to deploy
     * @param run   the context in which the deploy is taking place
     * @throws IOException in case of issue with the file manipulation
     */
    abstract protected void deploy(VirtualFile[] files, Run run) throws IOException;

    @Override
    public RepositoryDescriptor getDescriptor() {
        return (RepositoryDescriptor) super.getDescriptor();
    }
}
