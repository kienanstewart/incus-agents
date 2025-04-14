/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import hudson.model.AbstractDescribableImpl;

public abstract class Entry extends AbstractDescribableImpl<Entry> {
    private String remote;
    private String instance;

    public Entry(String remote, String instance) {
        this.remote = remote;
        this.instance = instance;
    }

    public String getInstance() {
        return this.instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getRemote() {
        return this.remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }
}
