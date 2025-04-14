/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.Extension;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class RebuildEntry extends Entry {

    private String imageRemote;
    private String imageName;

    @DataBoundConstructor
    public RebuildEntry(String remote, String instance, String imageRemote, String imageName) {
        super(remote, instance);
        this.imageRemote = imageRemote;
        this.imageName = imageName;
    }

    public String getImageRemote() {
        return this.imageRemote;
    }

    public void setImageRemote(String imageRemote) {
        this.imageRemote = imageRemote;
    }

    public String getImageName() {
        return this.imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Entry> {
        @Override
        public String getDisplayName() {
            return "Rebuild Instance";
        }
    }
}
