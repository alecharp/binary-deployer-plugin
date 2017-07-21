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

package com.cloudbees.plugins.binarydeployer.core;

import jenkins.util.VirtualFile;

/**
 * Represents the file to upload along with their name.
 * <p>
 * This enable to have a list of files, without any directories, to pass to the deploy implementations.
 * </p>
 *
 * @author Adrien Lecharpentier
 */
public class Binary {
    private final VirtualFile file;
    private final String name;

    private Binary(VirtualFile file, String name) {
        this.file = file;
        this.name = name;
    }

    public VirtualFile getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public static Binary from(VirtualFile file) {
        return new Binary(file, file.getName());
    }

    public static Binary from(VirtualFile file, String parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent shouldn't be null");
        }
        if (!parent.isEmpty() && !parent.endsWith("/")) parent += "/";
        return new Binary(file, parent + file.getName());
    }
}
