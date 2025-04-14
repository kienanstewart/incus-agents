/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public final class RestartEntry extends Entry {

    @DataBoundConstructor
    public RestartEntry(String remoteName, String instance) {
        super(remoteName, instance);
    }

    @Extension
    public static class DescriptorImpl extends Entry.DescriptorImpl {
        @Override
        public String getDisplayName() {
            return "Restart Instance";
        }
    }
}
