/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.Extension;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public final class RestartEntry extends Entry {

    @DataBoundConstructor
    public RestartEntry(String remote, String instance) {
        super(remote, instance);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Entry> {
        @Override
        public String getDisplayName() {
            return "Restart Instance";
        }
    }
}
