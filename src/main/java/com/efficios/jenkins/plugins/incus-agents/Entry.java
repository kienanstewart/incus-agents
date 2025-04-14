/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.slaves.Cloud;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

public abstract class Entry extends AbstractDescribableImpl<Entry> {
    private String remoteName;
    private String instance;

    public Entry(String remoteName, String instance) {
        this.remoteName = remoteName;
        this.instance = instance;
    }

    public String getInstance() {
        return this.instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Cloud getRemote() {
        Cloud cloud = Jenkins.get().getCloud(this.remoteName);
        return cloud;
    }

    public String getRemoteName() {
        return this.remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    protected static ListBoxModel remoteNameItems() {
        ListBoxModel box = new ListBoxModel();
        box.add("(None)", "");
        Jenkins j = Jenkins.get();
        if (j != null) {
            for (Cloud cloud : j.clouds) {
                if (cloud instanceof Incus) {
                    box.add(cloud.getDisplayName(), cloud.name);
                }
            }
        }
        return box;
    }

    public static class DescriptorImpl extends Descriptor<Entry> {
        public ListBoxModel doFillRemoteNameItems() {
            return Entry.remoteNameItems();
        }
    }
}
