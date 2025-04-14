/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class RestoreSnapshotEntry extends Entry {

    private String snapshotName;

    @DataBoundConstructor
    public RestoreSnapshotEntry(String remoteName, String instance, String snapshotName) {
        super(remoteName, instance);
        this.snapshotName = snapshotName;
    }

    public String getSnapshotName() {
        return this.snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    @Extension
    public static class DescriptorImpl extends Entry.DescriptorImpl {
        @Override
        public String getDisplayName() {
            return "Restore Snapshot";
        }
    }
}
