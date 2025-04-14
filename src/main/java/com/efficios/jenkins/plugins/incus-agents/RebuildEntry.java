/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.Extension;
import hudson.slaves.Cloud;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

public class RebuildEntry extends Entry {

    private String imageRemoteName;
    private String imageName;

    @DataBoundConstructor
    public RebuildEntry(String remoteName, String instance, String imageRemoteName, String imageName) {
        super(remoteName, instance);
        this.imageRemoteName = imageRemoteName;
        this.imageName = imageName;
    }

    public Cloud getImageRemote() {
        return Jenkins.get().getCloud(this.imageRemoteName);
    }

    public String getImageRemoteName() {
        return this.imageRemoteName;
    }

    public void setImageRemoteName(String imageRemoteName) {
        this.imageRemoteName = imageRemoteName;
    }

    public String getImageName() {
        return this.imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Extension
    public static class DescriptorImpl extends Entry.DescriptorImpl {
        @Override
        public String getDisplayName() {
            return "Rebuild Instance";
        }

        public ListBoxModel doFillImageRemoteNameItems() {
            return Entry.remoteNameItems();
        }
    }
}
