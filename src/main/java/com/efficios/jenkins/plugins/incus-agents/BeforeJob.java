/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

public class BeforeJob extends NodeProperty<Node> {

    private final List<Entry> entries;

    @DataBoundConstructor
    public BeforeJob(List<Entry> entries) {
        this.entries = entries != null ? new ArrayList<Entry>(entries) : Collections.<Entry>emptyList();
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {}
}
